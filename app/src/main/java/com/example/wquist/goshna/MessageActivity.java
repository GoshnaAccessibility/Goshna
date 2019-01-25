package com.example.wquist.goshna;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.example.wquist.goshna.Api.Message;
import com.example.wquist.goshna.ApiResponse.MessageResponse;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private int mFlightId;
    private String mFlightName;
    private String mGateNumber;

    private RecyclerView mRecycler;

    private ArrayList<Message> mMessages;
    private MessagesAdapter mAdapter;

    private Callback<MessageResponse> messagesCallback = new Callback<MessageResponse>() {
        @Override
        public void success(MessageResponse response, Response clientResponse) {
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
                Toast.makeText(mContext, iNewMessages + " new messages", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "No new messages", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(mContext, R.string.no_messages, Toast.LENGTH_LONG).show();
        }
    };

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
        txtGateNumber.setText(String.format("%s " + mGateNumber, getResources().getString(R.string.gate)));

        mRecycler = findViewById(R.id.cards);

        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 1);
        mRecycler.setLayoutManager(lm);

        mMessages = new ArrayList<>();
        mAdapter = new MessagesAdapter(this, mMessages, mFlightName);
        mRecycler.setAdapter(mAdapter);

        String annPrefix = getResources().getString(R.string.title_activity_message);
        setTitle(annPrefix + ": Gate " + mGateNumber + " (" + mFlightName + ")"); // FIXME: needs localization

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
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
        Goshna.getApi().getFlightMessages(mFlightId, messagesCallback);
    }

    @Override
    public void onClick(View view) {
        // Allow the user to acknowledge they have read the message
        Message m = (Message) view.getTag();
        m.read = true;
        view.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
    }
}
