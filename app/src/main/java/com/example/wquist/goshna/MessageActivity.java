package com.example.wquist.goshna;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wquist.goshna.Api.Message;
import com.example.wquist.goshna.ApiResponse.MessageResponse;
import com.example.wquist.goshna.ApiResponse.MessageStreamTask;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private int mFlightId;
    private String mFlightName;
    private String mGateNumber;

    private ArrayList<Message> mMessages;
    private MessagesAdapter mAdapter;

    @SuppressWarnings("deprecation") // v.vibrate(milliseconds) // SDK < 26
    private void addMessagesToList(MessageResponse response) {
        // Find if message already shown or if it is new
        int iNewMessages = 0;
        for (int i = response.messages.size() - 1; i >= 0; i--) {
            Message m = response.messages.get(i);
            boolean bMsgExists = false;
            for (int j = 0; j < mMessages.size(); j++) {
                if (m.id == mMessages.get(j).id) {
                    bMsgExists = true;
                    break;
                }
            }
            if (!bMsgExists) {
                m.read = false;
                mMessages.add(0, m);
                iNewMessages++;
            }
        }
        // update UI and Inform user, once other messages checked.
        if (iNewMessages > 0) {
            mAdapter.notifyDataSetChanged();
            // UX - Vibrate to alert user to new messages.
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                if (Build.VERSION.SDK_INT >= 26) {
                    v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(150);
                }
            }

            // Hide default 'no messages' message
            TextView t = findViewById(R.id.text_no_messages);
            t.setVisibility(View.GONE);

            Toast.makeText(mContext, iNewMessages + " new messages", Toast.LENGTH_SHORT).show();
        } else {
            if (mMessages.size() == 0) {
                // Show default 'no messages' message
                TextView t = findViewById(R.id.text_no_messages);
                t.setVisibility(View.VISIBLE);
            }
            Toast.makeText(mContext, "No new messages", Toast.LENGTH_SHORT).show();
        }

        // Remove the indefinite Loading ProgressBar
        ProgressBar pb = findViewById(R.id.progress_announcements_loading);
        pb.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        Intent i = getIntent();
        mFlightId = i.getIntExtra(GateActivity.FLIGHT_ID, -1);
        mFlightName = i.getStringExtra(GateActivity.FLIGHT_NAME);
        mGateNumber = i.getStringExtra(GateActivity.GATE_NUMBER);

        TextView txtGateNumber = findViewById(R.id.announcements_gate_id);
        txtGateNumber.setText(String.format(getResources().getString(R.string.gate_number), mGateNumber));

        TextView txtFlightNumber = findViewById(R.id.announcements_flight_id);
        txtFlightNumber.setText(String.format(getResources().getString(R.string.flight_name_prefix), mFlightName));

        RecyclerView mRecycler = findViewById(R.id.cards);

        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 1);
        mRecycler.setLayoutManager(lm);

        mMessages = new ArrayList<>();
        mAdapter = new MessagesAdapter(this, mMessages, mFlightName);
        mRecycler.setAdapter(mAdapter);

        setTitle(getResources().getString(R.string.title_activity_message));

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onBackPressed() {
        // Warn user about not getting any more notifications for this gate
        new AlertDialog.Builder(mContext) // FIXME: needs localization
                .setTitle("Flight " + mFlightName + " at Gate " + mGateNumber)
                .setMessage("If you go back you will not receive any more notifications for " +
                        "Gate " + mGateNumber + ". Continue?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MessageActivity.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("GoshnaMessageActivity", "Activity Destroyed - Cancelling streamTask");
        streamTask.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    MessageStreamTask streamTask = new MessageStreamTask() {
        @Override
        protected void onProgressUpdate(MessageResponse... values) {
            onStreamTaskProgressUpdate(values);
        }
    };

    private void onStreamTaskProgressUpdate(MessageResponse... values) {
        for (MessageResponse msgResponse : values) {
            if (msgResponse.isEmpty()) {
                // Connected - awaiting messages
                // Show default 'no messages' message
                TextView t = findViewById(R.id.text_no_messages);
                t.setVisibility(View.VISIBLE);
                // Remove the indefinite Loading ProgressBar
                ProgressBar pb = findViewById(R.id.progress_announcements_loading);
                pb.setVisibility(View.GONE);
            } else {
                // Messages received
                addMessagesToList(msgResponse);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        if(streamTask.getStatus() == AsyncTask.Status.FINISHED) {
            // TODO migrate to using a Service instead of AsyncTask
            Log.d("GoshnaRefresh", "Restarting streamTask");
            streamTask = new MessageStreamTask() {
                @Override
                protected void onProgressUpdate(MessageResponse... values) {
                    onStreamTaskProgressUpdate(values);
                }
            };
        }
        if(streamTask.getStatus() != AsyncTask.Status.RUNNING) {
            Log.d("GoshnaRefresh", "Starting new streamTask");
            try {
                streamTask.execute(new URL(Goshna.getFlightMessagesStreamUrl(mFlightId)));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(mContext, R.string.no_messages, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mContext, "Connected - new messages will appear when ready", Toast.LENGTH_SHORT).show(); // FIXME localise and strings.xml
            Log.d("GoshnaRefresh", "streamTask already running");
        }
    }

    @Override
    public void onClick(View view) {
        // Allow the user to acknowledge they have read the message
        Message m = (Message) view.getTag();
        m.read = true;
        view.setBackgroundColor(Color.WHITE);
    }
}
