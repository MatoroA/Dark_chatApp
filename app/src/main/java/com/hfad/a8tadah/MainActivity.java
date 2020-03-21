package com.hfad.a8tadah;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hfad.a8tadah.App.MyApplication;
import com.hfad.a8tadah.Model.Chat;
import com.hfad.a8tadah.Model.User;
import com.hfad.a8tadah.fragments.ChatFragment;
import com.hfad.a8tadah.fragments.ProfileFragment;
import com.hfad.a8tadah.fragments.UsersFragment;
import com.hfad.a8tadah.helper.MyDatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();
    private ArrayList<String> chatList;
    MyDatabaseHelper myDatabaseHelper;
    private ArrayList<User> contactList;
    private ArrayList<Chat> chatArrayList;
    private ArrayList<String> chatsFromDb;

    private BottomNavigationView bottomNavigationView;

    private User self = MyApplication.getInstance().getPreferences().getUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        myDatabaseHelper = new MyDatabaseHelper(this);
        chatList = new ArrayList<>();

        contactList = new ArrayList<>();
        chatArrayList = new ArrayList<>();
        chatsFromDb = new ArrayList<>();

//        try {
//            SQLiteDatabase database = myDatabaseHelper.getReadableDatabase();
//            database.delete("CHAT_MESSAGES","", null);
//        } catch (SQLException e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }


        readMessagesFromFirebase();
        fragmentDisplay(new ChatFragment());
        getContactList();
        readUserChat();
        connection();

    }

    private void connection() {
        // Since I can connect from multiple devices, we store each connection instance separately
// any time that connectionsRef's value is null (i.e. has no children) I am offline
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myConnectionsRef = database.getReference("users")
                .child(self.getUserId()).child("connections");

// Stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("users")
                .child(self.getUserId()).child("lastOnline");

        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.d(TAG, "onDataChange: change in connection");
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d(TAG, "onDataChange: " + connected);
                if (connected) {
                    DatabaseReference con = myConnectionsRef.push();

                    // When this device disconnects, remove it
                    con.onDisconnect().removeValue();

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                    // Add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    con.setValue(Boolean.TRUE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Listener was cancelled at .info/connected");
            }
        });
    }


    BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment fragment;

                    switch (item.getItemId()) {
                        case R.id.chats:
                            fragment = new ChatFragment();
                            fragmentDisplay(fragment);
                            return true;
                        case R.id.profile:
                            fragment = new ProfileFragment();
                            fragmentDisplay(fragment);
                            return true;
                        case R.id.contacts:
                            fragment = new UsersFragment();
                            fragmentDisplay(fragment);
                            return true;
                    }
                    return false;
                }
            };

    private void fragmentDisplay(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }


    public void getContactList() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null, null, null);
        contactList.clear();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(") ", "");

//
            if (!String.valueOf(phone.charAt(0)).equals("+")) {
                int size = phone.length();
                phone = "+27" + phone.substring(1, size);
            }
            User contact = new User("", "", name, "", phone, "", 0);
            contactList.add(contact);
        }

    }

    private void readUserChat() {
        FirebaseDatabase.getInstance().getReference("users")
                .child(self.getUserId()).child("chatList")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: " + dataSnapshot1.getKey());
                                chatsFromDb.add(dataSnapshot1.getKey());
                            }

                            for (final String newChatId : chatsFromDb) {
                                boolean isNew = true;
                                for (Chat chat : chatArrayList) {
                                    if (newChatId.trim().equals(chat.getChatId().trim())) {
                                        isNew = false;
                                        break;
                                    }
                                }

                                Log.d(TAG, "onDataChange: is chat new => " + isNew);

                                if (isNew) {

                                    Log.d(TAG, "onDataChange: chat is new => " + newChatId);

                                    FirebaseDatabase.getInstance().getReference("chats")
                                            .child(newChatId).child("users")
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        for (DataSnapshot users : dataSnapshot.getChildren()) {
                                                            if (!users.getKey().equals(self.getUserId())) {
                                                                Log.d(TAG, "onDataChange:user key => " + users.getKey());
                                                                readUserData(users.getKey(), newChatId);
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void readUserData(String userId, final String currentChatId) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String contact = (String) dataSnapshot.child("contact").getValue();
                    String userName = (String) dataSnapshot.child("userName").getValue();
                    String imageUrl = (String) dataSnapshot.child("imageUrl").getValue();
                    String userId = (String) dataSnapshot.child("userId").getValue();
                    String status = (String) dataSnapshot.child("status").getValue();

                    Log.d(TAG, "onDataChange: user image => " + imageUrl);
                    User user = new User(imageUrl, userName, "", userId, contact, status, 0);
                    user.setCurrentChatId(currentChatId);

                    for (User contactPerson : contactList) {
                        if (user.getContact().equals(contactPerson.getContact())) {
                            user.setName(contactPerson.getName());
                            Log.d(TAG, "onDataChange: username => " + user.getName());
                            insertIntoDb(user);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void insertIntoDb(User user) {


        Log.d(TAG, "insertIntoDb: this chat is new => ");
        try {
            SQLiteDatabase sqLiteDatabase = myDatabaseHelper.getWritableDatabase();

            Cursor cursor = sqLiteDatabase.query("CHATS",
                    new String[]{"CHAT_ID"},
                    "CHAT_ID = ? ",
                    new String[]{user.getCurrentChatId()},
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                Log.d(TAG, "insertIntoDb: this is a new record ");
                ContentValues cvChats = new ContentValues();
                cvChats.put("USER_ID", user.getUserId());
                cvChats.put("CHAT_ID", user.getCurrentChatId());

                sqLiteDatabase.insert("CHATS", null, cvChats);

                ContentValues cvUser = new ContentValues();

                cvUser.put("USERNAME", user.getName());
                cvUser.put("CONTACT", user.getContact());
                cvUser.put("USER_ID", user.getUserId());
                cvUser.put("STATUS", user.getStatus());

                sqLiteDatabase.insert("USER", null, cvUser);

                Log.d(TAG, "insertIntoDb: chat is added... =>");
            } else {
                Log.d(TAG, "insertIntoDb: old record on the roll");
            }

            Cursor cursor1 = sqLiteDatabase.query("USER",
                    new String[]{"CONTACT"},
                    "CONTACT = ?",
                    new String[]{user.getContact().trim()},
                    null,
                    null,
                    null,
                    null);

            if (cursor1.getCount() > 0){
                ContentValues cv = new ContentValues();
                cv.put("USERNAME", user.getName());

                sqLiteDatabase.update("USER", cv, "CONTACT = ?", new String[]{user.getContact().trim()});

                Log.d(TAG, "insertIntoDb: name updated: name => "+user.getName());
            }
            cursor.close();
            cursor1.close();
            sqLiteDatabase.close();

        } catch (SQLException e) {
            Log.d(TAG, "insertIntoDb: " + e.getMessage());
        }
    }

    private void readMessagesFromFirebase() {
        FirebaseDatabase.getInstance().getReference("chats")
                .orderByChild("isOpened").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot newMessage : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: => " + newMessage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
