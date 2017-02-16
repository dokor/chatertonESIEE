package com.antoine.chatertonesiee;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import co.devcenter.androiduilibrary.ChatView;
import co.devcenter.androiduilibrary.ChatViewEventListener;
import co.devcenter.androiduilibrary.SendButton;

/**
 * Created by Antoine on 16/02/2017.
 */

public class ChatActivity extends AppCompatActivity {

    private static final String TAG ="ChatActivity";
    private String contactJid;
    private ChatView mChatView;
    private SendButton mSendButton;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatView =(ChatView) findViewById(R.id.rooster_chat_view);
        mChatView.setEventListener(new ChatViewEventListener() {
            @Override
            public void userIsTyping() {
              //Utilisateur en train d'ecrire
            }

            @Override
            public void userHasStoppedTyping() {
                //Utilisateur n'est plus en train d'ecrire
            }
        });

        mSendButton = mChatView.getSendButton();
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionService.getState().equals(Connection.ConnectionState.CONNECTED)) {
                    Log.d(TAG, "The client is connected to the server,Sendint Message");
                    //Envoie du message vers le serveur
                    Intent intent = new Intent(ConnectionService.SEND_MESSAGE);
                    intent.putExtra(ConnectionService.BUNDLE_MESSAGE_BODY,
                            mChatView.getTypedString());
                    intent.putExtra(ConnectionService.BUNDLE_TO, contactJid);
                    sendBroadcast(intent);
                    //Met a jour la vue des messages
                    mChatView.sendMessage();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Le client n'est pas connecté ,Message non envoyé !",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        Intent intent = getIntent();
        contactJid = intent.getStringExtra("EXTRA_CONTACT_JID");
        setTitle(contactJid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action)
                {
                    case ConnectionService.NEW_MESSAGE:
                        String from = intent.getStringExtra(ConnectionService.BUNDLE_FROM_JID);
                        String body = intent.getStringExtra(ConnectionService.BUNDLE_MESSAGE_BODY);

                        if ( from.equals(contactJid))
                        {
                            mChatView.receiveMessage(body);

                        }else
                        {
                            Log.d(TAG,"Vous avez un message de :"+from);
                        }

                        return;
                }

            }
        };

        IntentFilter filter = new IntentFilter(ConnectionService.NEW_MESSAGE);
        registerReceiver(mBroadcastReceiver,filter);


    }
}