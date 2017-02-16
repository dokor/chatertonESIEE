package com.antoine.chatertonesiee;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatMessageListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

/**
 * Created by Antoine on 16/02/2017.
 */

public class Connection implements ConnectionListener,ChatMessageListener {

    private static final String TAG = "Connection";

    private  final Context mApplicationContext;
    private  final String mUsername;
    private  final String mPassword;
    private  final String mServiceName;
    private XMPPTCPConnection mConnection;
    private BroadcastReceiver uiThreadMessageReceiver;

    public static enum ConnectionState
    {
        CONNECTED ,AUTHENTICATED, CONNECTING ,DISCONNECTING ,DISCONNECTED;
    }

    public static enum LoggedInState
    {
        LOGGED_IN , LOGGED_OUT;
    }

    public Connection( Context context)
    {
        mApplicationContext = context.getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid",null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password",null);

        if( jid != null)
        {
            mUsername = jid.split("@")[0];
            mServiceName = jid.split("@")[1];
        }else
        {
            mUsername ="";
            mServiceName="";
        }
    }


    public void connect() throws IOException,XMPPException,SmackException
    {
        Log.d(TAG, "Connection serveur : " + mServiceName);
        XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder=
                XMPPTCPConnectionConfiguration.builder();
        builder.setServiceName(mServiceName);
        builder.setUsernameAndPassword(mUsername, mPassword);
        builder.setRosterLoadedAtLogin(true);

        builder.setResource("Chaterton");

        setupUiThreadBroadCastMessageReceiver();
        mConnection = new XMPPTCPConnection(builder.build());
        mConnection.addConnectionListener(this);
        mConnection.connect();
        mConnection.login();

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

    }

    private void setupUiThreadBroadCastMessageReceiver()
    {
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if( action.equals(ConnectionService.SEND_MESSAGE))
                {
                    //Envoie du message
                    sendMessage(intent.getStringExtra(ConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(ConnectionService.BUNDLE_TO));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectionService.SEND_MESSAGE);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver,filter);

    }

    private void sendMessage ( String body ,String toJid)
    {
        Chat chat = ChatManager.getInstanceFor(mConnection)
                .createChat(toJid,this);
        try
        {
            chat.sendMessage(body);
        }catch (SmackException.NotConnectedException | XMPPException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void processMessage(Chat chat, Message message) {
        String from = message.getFrom();
        String contactJid="";
        if ( from.contains("/"))
        {
            contactJid = from.split("/")[0];
        }else
        {
            contactJid=from;
        }
        Intent intent = new Intent(ConnectionService.NEW_MESSAGE);
        intent.setPackage(mApplicationContext.getPackageName());
        intent.putExtra(ConnectionService.BUNDLE_FROM_JID,contactJid);
        intent.putExtra(ConnectionService.BUNDLE_MESSAGE_BODY,message.getBody());
        mApplicationContext.sendBroadcast(intent);

    }

    public void disconnect()
    {
        try
        {
            if (mConnection != null)
            {
                mConnection.disconnect();
            }
        }catch (SmackException.NotConnectedException e)
        {
            ConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
            e.printStackTrace();
        }
        mConnection = null;
        if( uiThreadMessageReceiver != null)
        {
            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            uiThreadMessageReceiver = null;
        }
    }

    @Override
    public void connected(XMPPConnection connection) {
        ConnectionService.sConnectionState=ConnectionState.CONNECTED;
    }

    @Override
    public void authenticated(XMPPConnection connection) {
        ConnectionService.sConnectionState=ConnectionState.CONNECTED;
        showContactListActivityWhenAuthenticated();

    }

    @Override
    public void connectionClosed() {
        ConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        ConnectionService.sConnectionState=ConnectionState.DISCONNECTED;

    }

    @Override
    public void reconnectingIn(int seconds) {
        ConnectionService.sConnectionState = ConnectionState.CONNECTING;
    }

    @Override
    public void reconnectionSuccessful() {
        ConnectionService.sConnectionState = ConnectionState.CONNECTED;
    }

    @Override
    public void reconnectionFailed(Exception e) {
        ConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
    }

    private void showContactListActivityWhenAuthenticated()
    {
        Intent i = new Intent(ConnectionService.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
    }
}