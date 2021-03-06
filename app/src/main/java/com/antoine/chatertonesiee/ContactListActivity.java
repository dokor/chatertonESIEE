package com.antoine.chatertonesiee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Antoine on 16/02/2017.
 */

public class ContactListActivity extends AppCompatActivity {

    private static final String TAG = "ContactListActivity";

    private RecyclerView contactsRecyclerView;
    private ContactAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        contactsRecyclerView = (RecyclerView) findViewById(R.id.contact_list_recycler_view);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        ContactModel model = ContactModel.get(getBaseContext());
        List<Contact> contacts = model.getContacts();

        mAdapter = new ContactAdapter(contacts);
        contactsRecyclerView.setAdapter(mAdapter);
    }

    private class ContactHolder extends RecyclerView.ViewHolder
    {
        private TextView contactTextView;
        private Contact mContact;
        public ContactHolder ( View itemView)
        {
            super(itemView);
            contactTextView = (TextView) itemView.findViewById(R.id.contact_jid);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ContactListActivity.this
                            ,ChatActivity.class);
                    intent.putExtra("EXTRA_CONTACT_JID",mContact.getJid());
                    startActivity(intent);
                }
            });
        }

        public void bindContact( Contact contact)
        {
            mContact = contact;
            if (mContact == null)
            {
                return;
            }
            contactTextView.setText(mContact.getJid());
        }
    }


    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder>
    {
        private List<Contact> mContacts;

        public ContactAdapter( List<Contact> contactList)
        {
            mContacts = contactList;
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.list_item_contact, parent,
                            false);
            return new ContactHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            Contact contact = mContacts.get(position);
            holder.bindContact(contact);
            holder.itemView.getLayoutParams().height = 105;
            if ((position % 2) == 0) {
                holder.itemView.setBackgroundColor(0xffcccccc);
            }
            else {
                holder.itemView.setBackgroundColor(0xff888888);
            }
        }

        @Override
        public int getItemCount() {
            return mContacts.size();
        }
    }

}
