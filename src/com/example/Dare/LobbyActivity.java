package com.example.Dare;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.ludum.dare.GameActivity;
import com.ludum.dare.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyActivity extends Activity {

    private List<String> usernames;
    private ListView listView;
    private Button button;
    private UserAdapter userAdapter;

    private String username;
    private CastDevice mSelectedDevice;

    public String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_layout);

        //Log.d("Message HERE", message);

        button = (Button) findViewById(R.id.start);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(getApplicationContext(), GameActivity.class);
                startActivity(gameIntent);
            }
        });

        listView = (ListView) findViewById(R.id.player_list);
        //usernames.addAll(getUsernames());
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String jsonString = intent.getStringExtra("json");
            usernames = new ArrayList<String>();
            usernames.addAll(getUsernames(jsonString));
            userAdapter = new UserAdapter(getApplicationContext(), R.layout.lobby_adapter, usernames);
            userAdapter.notifyDataSetChanged();
            listView.setAdapter(userAdapter);
            Log.d("Uasdfasfd", jsonString);
        }
    };

    private List<String> getUsernames(String jsonMessage){
        List<String> names = new ArrayList<String>();
        try {
            JSONArray jsonArray = new JSONArray(jsonMessage);

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject playerName = jsonArray.getJSONObject(i);
                names.add(playerName.get("name").toString());
            }
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }

        Log.d("Names Here", names.toString());

        return names;
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(receiver, new IntentFilter("com.example.Dare.lobby.retriever"));

        //usernames = getUsernames();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private class UserAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> names;
        Integer resource;

        public UserAdapter(Context context, int resource, List<String> names) {
            super(context, resource, names);
            this.context = context;
            this.names = names;
            this.resource = resource;
        }

        public int getCount(){
            return names.size();
        }

        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View rowView = convertView;

            LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null){
                rowView = inflator.inflate(resource, null);
            }

            TextView textView = (TextView) rowView.findViewById(R.id.display_username);
            //ImageView imageView = (ImageView) rowView.findViewById(R.id.race_picture);


            textView.setText(names.get(position));

            return rowView;
        }
    }
}
