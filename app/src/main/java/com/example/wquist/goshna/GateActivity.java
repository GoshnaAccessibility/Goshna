package com.example.wquist.goshna;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

    private Context mContext;

    private EditText mGate;
    private Button mSubmit;

    private RecyclerView mRecycler;

    private ArrayList<Flight> mFlights;
    private FlightsAdapter mAdapter;

    private Flight mTarget;

    private Callback<FlightResponse> allFlightsCallback = new Callback<FlightResponse>() {
        @Override
        public void success(FlightResponse response, Response clientResponse) {
            mFlights.clear();
            mFlights.addAll(response.flights);

            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError error) {
            String err = mContext.getResources().getString(R.string.no_flights);
            Toast.makeText(mContext, err, Toast.LENGTH_LONG).show();
        }
    };

    private DialogInterface.OnClickListener submitYesListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mSubmit.setEnabled(true);

            Intent it = new Intent(mContext, MessageActivity.class);
            it.putExtra(FLIGHT_ID, mTarget.id);
            it.putExtra(FLIGHT_NAME, mTarget.airline_short + mTarget.number);

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

            new AlertDialog.Builder(mContext)
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
            String err = mContext.getResources().getString(R.string.bad_gate);
            Toast.makeText(mContext, err, Toast.LENGTH_LONG).show();

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

        Goshna.getApi().getAllFlights(allFlightsCallback);
    }

    public void submit(View v) {
        mSubmit.setEnabled(false);

        Goshna.getApi().findFlight(mGate.getText().toString(), flightCallback);
    }
}
