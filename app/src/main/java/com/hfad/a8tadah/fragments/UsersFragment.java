package com.hfad.a8tadah.fragments;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hfad.a8tadah.Model.User;
import com.hfad.a8tadah.R;
import com.hfad.a8tadah.adapters.ContactAdapter;

import java.util.ArrayList;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    private String TAG = UsersFragment.class.getName();

    private ArrayList<User> contactList;
    private ContactAdapter contactAdapter;
    private RecyclerView recyclerView;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactList = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        getContactList(view.getContext());


        contactAdapter = new ContactAdapter(contactList, getContext());
//
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(contactAdapter);
        return view;
    }

    public void getContactList(Context context) {
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        contactList.clear();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(") ", "");

            if (!String.valueOf(phone.charAt(0)).equals("+")){
                int size = phone.length();
                phone = "+27" + phone.substring(1, size);
            }
            Log.d(TAG, "getContactList: "+name+" <= name && phone => "+phone);

            User contact = new User("", "", name, null, phone, "", 0);
            contactList.add(contact);
        }

    }

}
