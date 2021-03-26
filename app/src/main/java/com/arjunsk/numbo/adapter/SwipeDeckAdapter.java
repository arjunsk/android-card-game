package com.arjunsk.numbo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arjunsk.numbo.R;

import java.util.List;

import mx.com.quiin.contactpicker.SimpleContact;

public class SwipeDeckAdapter extends BaseAdapter {

    private List<SimpleContact> data;
    private Context context;

    public SwipeDeckAdapter(List<SimpleContact> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public String getPlainPhone(String phone){
        return phone.replaceAll("[\\D]", "");
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public SimpleContact getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.card_contact, parent, false);
        }

        TextView user_name = (TextView) v.findViewById(R.id.user_name);
        TextView user_number_hint= (TextView) v.findViewById(R.id.user_phone_number_digit);

        String str_name = getItem(position).getDisplayName();
        int str_phone_numer_len = getPlainPhone(getItem(position).getCommunication()).length();


        user_name.setText(str_name);
        user_number_hint.setText(str_phone_numer_len+" digits");

        v.setTag(position);

        return v;
    }
}