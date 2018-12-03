package com.example.wquist.goshna;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.example.wquist.goshna.Api.Message;
import com.example.wquist.goshna.ApiResponse.MessageResponse;

public class MessageActivity extends AppCompatActivity {
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
            mMessages.clear();
            mMessages.addAll(response.messages);

            mAdapter.notifyDataSetChanged();
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
}
