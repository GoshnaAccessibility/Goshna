package com.example.wquist.goshna;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.example.wquist.goshna.Api.Message;
import com.example.wquist.goshna.TranslateResponse.TextResponse;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private Context mContext;
    private List<Message> mMessages;

    private String mFlightName;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView time;
        public TextView flight;
        public TextView body;

        private Callback<TextResponse> translateCallback = new Callback<TextResponse>() {
            @Override
            public void success(TextResponse response, Response clientResponse) {
                body.setText(response.text.get(0));
            }

            @Override
            public void failure(RetrofitError error) {
                String err = mContext.getResources().getString(R.string.no_translate);
                Toast.makeText(mContext, err, Toast.LENGTH_LONG).show();
            }
        };

        public ViewHolder(View v) {
            super(v);

            time = v.findViewById(R.id.time);
            flight = v.findViewById(R.id.flight);
            body = v.findViewById(R.id.body);
        }

        public void update(Message m) {
            time.setText(m.getTime());
            flight.setText("Flight " + mFlightName);
            body.setText(m.body);

            String lang = Locale.getDefault().getLanguage();
            Goshna.getTranslator().translate(Goshna.TRANSLATE_KEY, m.body, lang, translateCallback);
        }
    }

    public MessagesAdapter(Context c, List<Message> ms, String fn) {
        mContext = c;
        mMessages = ms;

        mFlightName = fn;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.update(mMessages.get(position));

        // set clicker
    }

    @Override
    public int getItemCount() { return mMessages.size(); }
}
