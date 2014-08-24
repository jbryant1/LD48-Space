package com.ludum.dare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.nhaarman.listviewanimations.appearance.SingleAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
//import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

public class UnitList extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        View view = inflater.inflate(R.layout.fragment_unit_list, container, false);
        ListView list = (ListView) view.findViewById(R.id.unit_list);


        ArrayList<UnitlistModel> unitList = new ArrayList<UnitlistModel>();
        UnitlistModel u = new UnitlistModel();
        u.name = "Kris";
        unitList.add(u);


        UnitAdapter uniAdapter = new UnitAdapter(getActivity(),unitList);


        SwingBottomInAnimationAdapter animation = new SwingBottomInAnimationAdapter(uniAdapter) ;
        animation.setAbsListView(list);
        //animation.setInitialDelayMillis(500);
        list.setAdapter(animation);




        //list.setAdapter(new UnitAdapter(getActivity(),unitList));

        return view;


    }




}
