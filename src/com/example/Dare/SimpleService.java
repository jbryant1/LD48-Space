package com.example.Dare;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ludum.dare.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SimpleService extends Service {

    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;

    private String username;

    private Cast.Listener mCastListener;
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;


    private HelloWorldChannel mHelloWorldChannel;

    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private String mSessionId;
    public SimpleService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "The new Service was Created", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        mSelectedDevice =(CastDevice) intent.getExtras().get("device");
        username = (String) intent.getExtras().get("username");
        launchReceiver();

        return START_STICKY;
    }

    /**
     * Start the receiver app
     */
    private void launchReceiver() {
        try {
            mCastListener = new Cast.Listener() {

                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d("Application Stopped", "application has stopped");
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
            Log.e("Failed", "Failed launchReceiver", e);
        }
    }

    /**
     * Send a text message to the receiver
     *
     * @param message
     */
    private void sendMessage(String message) {
        if (mApiClient != null && mHelloWorldChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient, mHelloWorldChannel.getNamespace(), message).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status result) {
                        if (!result.isSuccess()) {
                            Log.e("Bad Message", "Sending message failed");
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("Exception", "Exception while sending message", e);
            }
        } else {
            //Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Custom message channel
     */
    public class HelloWorldChannel implements Cast.MessageReceivedCallback {
        /**
         * @return custom namespace
         */
        public String getNamespace() {
            return getString(R.string.namespace);
        }

        /*
         * Receive message from the receiver app
         */
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.d("MEssage", "onMessageReceived: " + message);
            //startNewService();
        }

    }

    private void teardown() {
        Log.d("CLOSING", "teardown");
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
                        Log.e("Error", "Exception while removing channel", e);
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

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e("Failed", "onConnectionFailed ");

            teardown();
        }
    }


    private String createJSONString(String sessionId, boolean hasJoined){
        JSONObject player = new JSONObject();
        try{
            player.put("id", sessionId);
            if(!username.equals("")) {
                player.put("name", username);
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
    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d("Connected", "onConnected");

            if (mApiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                Log.d("Disconnected", "Disconnected From Route");
                return;
            }

            try {
                if (mWaitingForReconnect) {
                    mWaitingForReconnect = false;

                    // Check if the receiver app is still running
                    if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        Log.d("App Closed", "App  is no longer running");
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
                            Log.e("Bad Channel", "Exception while creating channel", e);
                        }
                    }
                } else {Cast.CastApi.launchApplication(mApiClient,  getString(R.string.app_id) , false)
                        .setResultCallback(
                                new ResultCallback<Cast.ApplicationConnectionResult>() {
                                    @Override
                                    public void onResult(Cast.ApplicationConnectionResult result) {
                                        Status status = result.getStatus();
                                        if (status.isSuccess()) {
                                            mApplicationStarted = true;

                                            mHelloWorldChannel = new HelloWorldChannel();
                                            try {
                                                Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
                                                        mHelloWorldChannel.getNamespace(),
                                                        mHelloWorldChannel);
                                            } catch (IOException e) {
                                                Log.e("Bad Channel", "Exception while creating channel", e);
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
                                            Log.d("Application", "ApplicationConnectionResultCallback.onResult: statusCode" + status.getStatusCode());
                                            if (status.isSuccess()) {
                                                ApplicationMetadata applicationMetadata = result
                                                        .getApplicationMetadata();

                                                mSessionId = result.getSessionId();
                                                String applicationStatus = result.getApplicationStatus();
                                                boolean wasLaunched = result.getWasLaunched();
                                                Log.d("Name","application name: "+ applicationMetadata.getName()
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
                                                    Log.e("Exception", "Exception while creating channel", e);
                                                }

                                                // set the initial instructions
                                                // on the receiver
                                                Log.d("JSON OBJECT", createJSONString(mSessionId, true));
                                                Log.d("INFO", String.valueOf(mSelectedDevice.getFriendlyName()));
                                                sendMessage(createJSONString(mSessionId, true));
                                                //startNewService();
                                                //Intent i = new Intent(getApplicationContext(), LobbyActivity.class);
                                                //startActivity(i);
                                            } else {
                                                Log.e("Failed Launch", "application could not launch");
                                                teardown();
                                            }
                                        }
                                    });
                }
            } catch (Exception e) {
                Log.e("Fail Launch", "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d("Suspended", "onConnectionSuspended");
            mWaitingForReconnect = true;
        }
    }
}
