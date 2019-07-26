package com.example.wquist.goshna;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.example.wquist.goshna.Api.Flight;
import com.example.wquist.goshna.ApiResponse.FlightResponse;

public class GateActivity extends AppCompatActivity {
    public static final String FLIGHT_ID = "com.example.wquist.goshna.FLIGHT_ID";
    public static final String FLIGHT_NAME = "com.example.wquist.goshna.FLIGHT_NAME";
    public static final String GATE_NUMBER = "com.example.wquist.goshna.GATE_NUMBER";

    private Context mContext;

    private EditText mGate;
    private Button mSubmit;

    private RecyclerView mRecycler;

    private ArrayList<Flight> mFlights;
    private FlightsAdapter mAdapter;

    private Flight mTarget;

    private EditText.OnKeyListener inputCallback = new EditText.OnKeyListener() {
        public boolean onKey(View v, int key, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_ENTER) {
                submit(v);
                return true;
            }

            return false;
        }
    };

    private DialogInterface.OnClickListener retryYesListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Goshna.getApi().getAllFlights(allFlightsCallback);
        }
    };

    private Callback<FlightResponse> allFlightsCallback = new Callback<FlightResponse>() {
        @Override
        public void success(FlightResponse response, Response clientResponse) {
            mSubmit.setEnabled(true);

            mFlights.clear();
            mFlights.addAll(response.flights);

            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError error) {
            mSubmit.setEnabled(false);

            new AlertDialog.Builder(mContext) // FIXME: needs localization
                    .setTitle("Connection Error")
                    .setMessage("Could not connect to the Goshna airport server.")
                    .setPositiveButton(R.string.refresh, retryYesListener)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    };

    private DialogInterface.OnClickListener submitYesListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mSubmit.setEnabled(true);

            Intent it = new Intent(mContext, MessageActivity.class);
            it.putExtra(FLIGHT_ID, mTarget.id);
            it.putExtra(FLIGHT_NAME, mTarget.airline_short + mTarget.number);
            it.putExtra(GATE_NUMBER, mTarget.gate);

            startActivity(it);
        }
    };

    private DialogInterface.OnClickListener submitNoListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mSubmit.setEnabled(true);
        }
    };

    private Callback<FlightResponse> flightCallback = new Callback<FlightResponse>() {
        @Override
        public void success(FlightResponse response, Response clientResponse) {
            Flight f = response.flights.get(0);
            mTarget = f;

            new AlertDialog.Builder(mContext) // FIXME: needs localization
                    .setTitle("Flight " + f.airline_short + f.number + " at Gate " + f.gate)
                    .setMessage("You entered the gate for a " + f.airline + " flight to " +
                            f.dest_short + " at " + f.getTime() + ". Is this the correct flight?")
                    .setPositiveButton(android.R.string.yes, submitYesListener)
                    .setNegativeButton(android.R.string.no, submitNoListener)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(mContext, R.string.bad_gate, Toast.LENGTH_LONG).show();
            mSubmit.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mGate = findViewById(R.id.gate);
        mSubmit = findViewById(R.id.submit);

        mRecycler = findViewById(R.id.cards);

        RecyclerView.LayoutManager lm = new GridLayoutManager(this, 1);
        mRecycler.setLayoutManager(lm);

        mFlights = new ArrayList<>();
        mAdapter = new FlightsAdapter(this, mFlights);
        mRecycler.setAdapter(mAdapter);

        mGate.setOnKeyListener(inputCallback);

        mSubmit.setEnabled(false);
        Goshna.getApi().getAllFlights(allFlightsCallback);
    }

    public void submit(View v) {
        mSubmit.setEnabled(false);

        Goshna.getApi().findFlight(mGate.getText().toString(), flightCallback);
    }
}
