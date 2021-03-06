/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.example.Dare;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ludum.dare.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main activity to send messages to the receiver.
 */
public class MainActivity extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int REQUEST_CODE = 1;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;


    private Cast.Listener mCastListener;
    //private ConnectionCallbacks mConnectionCallbacks;
    //private ConnectionFailedListener mConnectionFailedListener;


    //private HelloWorldChannel mHelloWorldChannel;

    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private String mSessionId;

    private EditText username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(
        android.R.color.transparent));

        username = (EditText) findViewById(R.id.input_username);

        // Configure Cast device discovery
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(
                CastMediaControlIntent.categoryForCast(getResources()
                        .getString(R.string.app_id))).build();
        mMediaRouterCallback = new MyMediaRouterCallback();

	}


	/*
	 * Handle the voice recognition response
	 *
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
	 * android.content.Intent)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches.size() > 0) {
				Log.d(TAG, matches.get(0));
				sendMessage(matches.get(0));
                Log.d("Hit Here", "Right Here");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
    */

	@Override
	protected void onResume() {
		super.onResume();
		// Start media router discovery
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
				MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
	}

	@Override
	protected void onPause() {
		if (isFinishing()) {
			// End media router discovery
			mMediaRouter.removeCallback(mMediaRouterCallback);
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		//teardown();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
				.getActionProvider(mediaRouteMenuItem);
		// Set the MediaRouteActionProvider selector for device discovery.
		mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		return true;
	}

    /**
     * Callback for MediaRouter events
     */

    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteSelected ");
            // Handle the user route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            TextTrackStyle textTrackStyle = new TextTrackStyle();
            textTrackStyle.setForegroundColor(Color.RED);

            Intent startService = new Intent(getApplicationContext(), SimpleService.class);
            startService.putExtra("device", mSelectedDevice);
            startService.putExtra("username", username.getText().toString());
            startService(startService);


            Intent lobbyActivity = new Intent(getApplicationContext(), LobbyActivity.class);
            lobbyActivity.putExtra("device", mSelectedDevice);
            lobbyActivity.putExtra("username", username.getText().toString());
            startActivity(lobbyActivity);

            //launchReceiver();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            //teardown();
            stopNewService();
            mSelectedDevice = null;
        }
    }

    /**
     * Start the receiver app
    private void launchReceiver() {
        try {
            mCastListener = new Cast.Listener() {

                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d(TAG, "application has stopped");
                    teardown();
                }

            };

            // Connect to Google Play services
            mConnectionCallbacks = new ConnectionCallbacks();
            mConnectionFailedListener = new ConnectionFailedListener();
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastListener);
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .build();
            mApiClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "Failed launchReceiver", e);
        }

     */

    /**
     * Google Play services callbacks

    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected");

            if (mApiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                Log.d(TAG, "Disconnected From Route");
                return;
            }

            try {
                if (mWaitingForReconnect) {
                    mWaitingForReconnect = false;

                    // Check if the receiver app is still running
                    if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        Log.d(TAG, "App  is no longer running");
                        teardown();
                    } else {
                        // Re-create the custom message channel
                        try {
                            Cast.CastApi.setMessageReceivedCallbacks(
                                    mApiClient,
                                    mHelloWorldChannel.getNamespace(),
                                    mHelloWorldChannel);
                            Log.d("Set Callbacks", "Here");
                        } catch (IOException e) {
                            Log.e(TAG, "Exception while creating channel", e);
                        }
                    }
                } else {Cast.CastApi.launchApplication(mApiClient,  getString(R.string.app_id) , false)
                        .setResultCallback(
                                new ResultCallback<Cast.ApplicationConnectionResult>() {
                                    @Override
                                    public void onResult(Cast.ApplicationConnectionResult result) {
                                        Status status = result.getStatus();
                                        if (status.isSuccess()) {
                                            ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
                                            String sessionId = result.getSessionId();
                                            String applicationStatus = result.getApplicationStatus();
                                            boolean wasLaunched = result.getWasLaunched();

                                            mApplicationStarted = true;

                                            mHelloWorldChannel = new HelloWorldChannel();
                                            try {
                                                Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
                                                        mHelloWorldChannel.getNamespace(),
                                                        mHelloWorldChannel);
                                            } catch (IOException e) {
                                                Log.e(TAG, "Exception while creating channel", e);
                                            }
                                        }
                                    }
                                });
                    // Launch the receiver app
                    Cast.CastApi.launchApplication(mApiClient, getString(R.string.app_id), false)
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                Cast.ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            Log.d(TAG, "ApplicationConnectionResultCallback.onResult: statusCode" + status.getStatusCode());
                                            if (status.isSuccess()) {
                                                ApplicationMetadata applicationMetadata = result
                                                        .getApplicationMetadata();

                                                mSessionId = result.getSessionId();
                                                String applicationStatus = result.getApplicationStatus();
                                                boolean wasLaunched = result.getWasLaunched();
                                                Log.d(TAG,"application name: "+ applicationMetadata.getName()
                                                        + ", status: "
                                                        + applicationStatus
                                                        + ", sessionId: "
                                                        + mSessionId
                                                        + ", wasLaunched: "
                                                        + wasLaunched);
                                                mApplicationStarted = true;

                                                // Create the custom message
                                                // channel
                                                mHelloWorldChannel = new HelloWorldChannel();
                                                try {
                                                    Cast.CastApi
                                                            .setMessageReceivedCallbacks(
                                                                    mApiClient,
                                                                    mHelloWorldChannel
                                                                            .getNamespace(),
                                                                    mHelloWorldChannel);
                                                } catch (IOException e) {
                                                    Log.e(TAG, "Exception while creating channel", e);
                                                }

                                                // set the initial instructions
                                                // on the receiver
                                                Log.d("JSON OBJECT", createJSONString(mSessionId, true));
                                                Log.d("INFO", String.valueOf(mSelectedDevice.getFriendlyName()));
                                                sendMessage(createJSONString(mSessionId, true));
                                                Intent i = new Intent(getApplicationContext(), LobbyActivity.class);
                                                startActivity(i);
                                            } else {
                                                Log.e(TAG, "application could not launch");
                                                teardown();
                                            }
                                        }
                                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended");
            mWaitingForReconnect = true;
        }
    }
     */

    /*
    private String createJSONString(String sessionId, boolean hasJoined){
        JSONObject player = new JSONObject();
        try{
            player.put("id", sessionId);
            if(!username.getText().equals("")) {
                player.put("name", username.getText().toString());
            }
            else{
                player.put("name", "player");
            }

            if(hasJoined) {
                player.put("command", "join");
            }
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(player);

        //playerObject.put("players", jsonArray);
        return player.toString();
    }
    */

    /**
     * Google Play services callbacks

    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "onConnectionFailed ");

            teardown();
        }
    }
     */

    /**
     * Tear down the connection to the receiver

    private void teardown() {
        Log.d(TAG, "teardown");
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected()  || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient, mSessionId);
                        if (mHelloWorldChannel != null) {
                            Cast.CastApi.removeMessageReceivedCallbacks(
                                    mApiClient,
                                    mHelloWorldChannel.getNamespace());
                            mHelloWorldChannel = null;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while removing channel", e);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        mSessionId = null;
    }
    */

    /**
     * Send a text message to the receiver
     *
     * @param

    private void sendMessage(String message) {
        if (mApiClient != null && mHelloWorldChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient, mHelloWorldChannel.getNamespace(), message).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "Sending message failed");
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message", e);
            }
        } else {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
     */

    // Start the  service
    public void startNewService() {

        startService(new Intent(this, SimpleService.class));
    }

    // Stop the  service
    public void stopNewService() {

        stopService(new Intent(this, SimpleService.class));
    }

    /**
     * Custom message channel
     */
   // public class HelloWorldChannel implements Cast.MessageReceivedCallback {
        /**
         * @return custom namespace

        public String getNamespace() {
            return getString(R.string.namespace);
        }
        */
        /*
         * Receive message from the receiver app

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.d("MEssage", "onMessageReceived: " + message);
        }
        */

    //}
}
