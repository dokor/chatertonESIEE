package com.antoine.chatertonesiee;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Antoine on 16/02/2017.
 */

public class ContactModel {

    private static ContactModel sContactModel;
    private List<Contact> mContacts;

    public static ContactModel get(Context context)
    {
        if(sContactModel == null)
        {
            sContactModel = new ContactModel(context);
        }
        return  sContactModel;
    }

    private ContactModel(Context context)
    {
        mContacts = new ArrayList<>();
        populateWithInitialContacts(context);

    }

    private void populateWithInitialContacts(Context context)
    {
        //Crée les contacts puis les ajoute a la liste qui sera affichée
        Contact contact1 = new Contact("chaterton1@chinwag.im");
        mContacts.add(contact1);
        Contact contact2 = new Contact("chaterton2@chinwag.im");
        mContacts.add(contact2);
        Contact contact3 = new Contact("chaterton3@chinwag.im");
        mContacts.add(contact3);
    }
    public List<Contact> getContacts()
    {
        return mContacts;
    }

}
