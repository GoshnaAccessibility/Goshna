package com.example.wquist.goshna;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wquist.goshna.Api.Flight;

public class FlightsAdapter extends RecyclerView.Adapter<FlightsAdapter.ViewHolder> {
    private Context mContext;
    private List<Flight> mFlights;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView flight;
        public TextView gate;
        public TextView destination;
        public TextView airline;
        public TextView departure;

        private Flight mFlight;

        private View.OnClickListener submitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(mContext, MessageActivity.class);
                it.putExtra(GateActivity.FLIGHT_ID, mFlight.id);
                it.putExtra(GateActivity.FLIGHT_NAME, mFlight.airline_short + mFlight.number);
                it.putExtra(GateActivity.GATE_NUMBER, mFlight.gate);

                mContext.startActivity(it);
            }
        };

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(submitListener);

            flight = v.findViewById(R.id.flight);
            gate = v.findViewById(R.id.gate);
            destination = v.findViewById(R.id.destination);
            airline = v.findViewById(R.id.airline);
            departure = v.findViewById(R.id.departure);
        }

        public void update(Flight f) {
            flight.setText(mContext.getResources().getString(R.string.flight_name_prefix, f.airline_short + f.number));
            gate.setText(mContext.getResources().getString(R.string.gate_number, f.gate));
            destination.setText(mContext.getResources().getString(R.string.flight_to, f.dest_short));
            airline.setText(f.airline);
            departure.setText(f.getTime());

            mFlight = f;
        }
    }

    public FlightsAdapter(Context c, List<Flight> fs) {
        mContext = c;
        mFlights = fs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_gate, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.update(mFlights.get(position));

        // set clicker
    }

    @Override
    public int getItemCount() {
        return Math.min(mFlights.size(), 5);
    }
}
