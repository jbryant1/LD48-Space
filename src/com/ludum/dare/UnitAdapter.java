package com.ludum.dare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem.ExpandableListItemAdapter;

import java.util.List;

/**
 * Created by kris on 8/23/14.
 */
public class UnitAdapter extends ExpandableListItemAdapter<UnitlistModel> {


    private Context mContext;

    public UnitAdapter(Context context, List<UnitlistModel> units){

        super(context,R.layout.activity_expandablelistitem_card, R.id.activity_expandablelistitem_card_title, R.id.activity_expandablelistitem_card_content,units);
        mContext = context;

    }




    @NonNull
    @Override
    public View getTitleView(int i, @Nullable View v, @NonNull ViewGroup viewGroup) {

        if(v == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.unit_title, viewGroup, false);

        }

        return v;
    }

    @NonNull
    @Override
    public View getContentView(int i, @Nullable View v, @NonNull ViewGroup viewGroup) {
        if(v == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.unit_content, viewGroup, false);

        }

        return v;
    }
}
