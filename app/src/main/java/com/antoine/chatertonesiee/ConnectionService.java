package com.antoine.chatertonesiee;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

/**
 * Created by Antoine on 16/02/2017.
 */

public class ConnectionService extends Service {
    private static final String TAG ="Service";

    public static final String UI_AUTHENTICATED = "com.blikoon.rooster.uiauthenticated";
    public static final String SEND_MESSAGE = "com.blikoon.rooster.sendmessage";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_TO = "b_to";

    public static final String NEW_MESSAGE = "com.blikoon.rooster.newmessage";
    public static final String BUNDLE_FROM_JID = "b_from";

    public static Connection.ConnectionState sConnectionState;
    public static Connection.LoggedInState sLoggedInState;
    private boolean mActive;
    private Thread mThread;
    private Handler mTHandler;
    private Connection mConnection;

    public ConnectionService() {
    }
    public static Connection.ConnectionState getState()
    {
        if (sConnectionState == null)
        {
            return Connection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static Connection.LoggedInState getLoggedInState()
    {
        if (sLoggedInState == null)
        {
            return Connection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void initConnection()
    {
        if( mConnection == null)
        {
            mConnection = new Connection(this);
        }
        try
        {
            mConnection.connect();

        }catch (IOException |SmackException |XMPPException e)
        {
            e.printStackTrace();
            //Stop tout
            stopSelf();
        }
    }

    public void start()
    {
        if(!mActive)
        {
            mActive = true;
            if( mThread ==null || !mThread.isAlive())
            {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        Looper.loop();

                    }
                });
                mThread.start();
            }
        }
    }

    public void stop()
    {
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if( mConnection != null)
                {
                    mConnection.disconnect();
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        return Service.START_STICKY;
        }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }
}
