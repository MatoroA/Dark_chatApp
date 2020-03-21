package com.hfad.a8tadah.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.hfad.a8tadah.Model.User;

public class MyPreferences {

    private Context mContext;

    private String PREF_NAME = "PREFERNCES_NAME";
    private String USER_ID = "USER_ID_";
    private String USER_CONTACT = "USER_CONTACT";
    private String USER_IMAGEUrl = "IMAGE_URL";
    private String USER_USERNAME = "USER_NAME";
    private String USER_CONTACT_NAME = "CONTACT_NAME";
    private String USER_STATUS = "MY_STATUS";
    private String USER_LAST_SEEN = "MY_LAST_SEEN";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public MyPreferences(Context context){
        this.mContext = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    public void storeUser(User user){
        editor.putString(USER_ID, user.getUserId());
        editor.putString(USER_CONTACT, user.getContact());
        editor.putString(USER_IMAGEUrl, user.getImageUrl());
        editor.putString(USER_USERNAME, user.getUserName());
        editor.putString(USER_STATUS, user.getStatus());
        editor.putLong(USER_LAST_SEEN, user.getLastSeen());
        editor.commit();
    }

    public User getUser(){
        if (pref.getString(USER_ID, null) != null){
            String username = pref.getString(USER_USERNAME, null);
            String contact = pref.getString(USER_CONTACT, null);
            String userId = pref.getString(USER_ID, null);
            String imageUrl = pref.getString(USER_IMAGEUrl, null);
            String name = pref.getString(USER_CONTACT_NAME, null);
            String status = pref.getString(USER_STATUS, null);
            Long lastSeen = pref.getLong(USER_LAST_SEEN, 0);

            return new User(imageUrl,username,name,userId, contact,status, lastSeen);
        }

        return null;
    }
}
