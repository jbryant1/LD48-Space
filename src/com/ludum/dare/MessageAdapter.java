package com.ludum.dare;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kris on 8/24/14.
 */
public class MessageAdapter extends ArrayAdapter<MessageObject> {


    Context mContext;

    public MessageAdapter(Context context, int resource, List<MessageObject> objects) {
        super(context, resource, objects);
        mContext = context;
    }


    @Override
    public View getView(int position,View v, ViewGroup parent){

        if(v == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.messageview, parent, false);
        }

        TextView message = (TextView)v.findViewById(R.id.text);

        //If me or you
        if(getItem(position).UserID == "ME"){
            //Set Message for me

            message.setPadding(25,15,15,15);
            message.setGravity(Gravity.RIGHT);      //Not sure if Text Gravity or layout..


        }else{
            //Setup Message for other

            message.setPadding(15,15,25,15);
            message.setGravity(Gravity.LEFT);

        }

        return v;
    }

}
