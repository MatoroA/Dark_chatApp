package com.hfad.a8tadah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hfad.a8tadah.App.MyApplication;
import com.hfad.a8tadah.Model.Chat;
import com.hfad.a8tadah.Model.Message;
import com.hfad.a8tadah.Model.User;
import com.hfad.a8tadah.adapters.MessageAdapter;
import com.hfad.a8tadah.helper.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = ChatActivity.class.getName();

    public static final String CHAT_ID = "CHAT_ID";
    public static final String USER_INFO = "USER_INFO";
    private static final int NEW_MESSAGE = 0;
    private String mChatId;
    private Toolbar toolbar;
    private MyDatabaseHelper myDatabaseHelper;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messageArrayList;
    private RecyclerView recyclerView;

    private TextView mUsername;
    private User user;
    private ImageButton sendMessageBtn;
    private EditText mMessageEditText;
    private LinearLayout mUsernameLayout;

    private User self = MyApplication.getInstance().getPreferences().getUser();
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//


        myDatabaseHelper = new MyDatabaseHelper(this);
        messageArrayList = new ArrayList<>();
        mMessageEditText = findViewById(R.id.message_field);
        sendMessageBtn = findViewById(R.id.send_message);
        mUsername = findViewById(R.id.name_text);

        ImageButton back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "back ...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mUsernameLayout = findViewById(R.id.layout_username);
        mUsernameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "back ...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        user = (User) getIntent().getSerializableExtra(USER_INFO);

        if (user == null) {
            finish();
        }

        mUsername.setText(user.getName());
        mChatId = user.getCurrentChatId();
        Log.d(TAG, "onCreate: chat id " + mChatId);
        if (mChatId == null) {
            readChatFromInternalDb();
        }

        messageAdapter = new MessageAdapter(messageArrayList, this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);


        dbRef = FirebaseDatabase.getInstance().getReference("chats");

        if (user.getUserId() == null) {
            searchForUser(user);
        }

        if (mChatId != null) {
            getChatDataFromInternalDb();
            chatMessagesFromFirebase();
        }

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getUserId() == null) {
                    Toast.makeText(ChatActivity.this, "This bugger doesnt have the app, sorry!", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d(TAG, "onClick: " + user.getUserId());
                sendMessage();
            }
        });
    }

    private void searchForUser(final User user) {
        Log.d(TAG, "searchForUser: " + user.getContact());
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("contact").equalTo(user.getContact()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: " + data.getValue());

                        String userId = (String) data.child("userId").getValue();

                        user.setUserId(userId);
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "This user doesnt have the app!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readChatFromInternalDb() {
        try {
            SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("CHATS",
                    new String[]{"USER_ID", "USER_CONTACT", "CHAT_ID"},
                    "USER_CONTACT = ?",
                    new String[]{user.getContact()},
                    null,
                    null,
                    null);

            while (cursor.moveToNext()) {
                mChatId = cursor.getString(2);
                Log.d(TAG, "readChatFromInternalDb: " + mChatId);
            }

            cursor.close();
            db.close();

        } catch (SQLException e) {
            Log.d(TAG, "readFromDb: " + e.getMessage());
        }
    }

    private void chatMessagesFromFirebase() {

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("chats")
                .child(mChatId).child("messages");

        dbRef.orderByChild("isOpened").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot newMessage : dataSnapshot.getChildren()) {
                        String messageKey = newMessage.getKey();
                        Message newMessageObj = newMessage.getValue(Message.class);
                        newMessageObj.setMessageId(messageKey);
                        newMessageObj.setChatId(mChatId);
                        Log.d(TAG, "onDataChange: => " + newMessageObj.getMessage());
                        Log.d(TAG, "onDataChange: => " + newMessageObj.getIsOpened());

                        storeChatMessage(newMessageObj);
                    }
                } else {
                    Log.d(TAG, "onDataChange: there is nothing =>");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void storeChatMessage(final Message message) {
        try {
            Log.d(TAG, "storeChat: " + message.getMessage());
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

            Log.d(TAG, "storeChat: id : " + message.getMessageId()
                    + "\nsenderId : " + message.getSender() + "\ntext : " + message.getMessage() + "\ntime : " + message.getTimeStamp()
                    + "chatId : " + message.getChatId() + "\nisOpened : " + message.getIsOpened());

            Cursor queryCursor = db.query("CHAT_MESSAGES",
                    new String[]{"CHAT_ID", "MESSAGE_ID"},
                    "CHAT_ID = ? AND MESSAGE_ID = ?",
                    new String[]{message.getChatId().trim(), message.getMessageId()},
                    null,
                    null,
                    null);

            Log.d(TAG, "storeChat: this message is new!!!!!!!111 count = " + queryCursor.getCount());

            if (queryCursor.getCount() == 0) {
                ContentValues cv = new ContentValues();
                cv.put("SENDER_ID", message.getSender());
                cv.put("MESSAGE_ID", message.getMessageId());
                cv.put("MESSAGE_TEXT", message.getMessage());
                cv.put("MESSAGE_TIME", message.getTimeStamp());
                cv.put("MESSAGE_SENT", message.isSent());
                cv.put("MESSAGE_OPENED", message.getIsOpened());
                cv.put("CHAT_ID", mChatId);
                db.insert("CHAT_MESSAGES", null, cv);
            }

            queryCursor.close();
            getChatDataFromInternalDb();
        } catch (SQLException e) {
            Log.d(TAG, "storeChat: " + e.getMessage());
        }

    }

    private void getChatDataFromInternalDb() {

        try {
            final SQLiteDatabase sqLiteDatabase = myDatabaseHelper.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.query("CHAT_MESSAGES",
                    new String[]{"SENDER_ID", "MESSAGE_ID", "MESSAGE_TEXT", "MESSAGE_OPENED",
                            "MESSAGE_TIME", "MESSAGE_SENT", "CHAT_ID"},
                    "CHAT_ID = ?",
                    new String[]{mChatId},
                    null,
                    null,
                    null);

            messageArrayList.clear();
            while (cursor.moveToNext()) {
                String senderId = cursor.getString(0);
                String messageId = cursor.getString(1);
                String text = cursor.getString(2);
                Long timeStamp = cursor.getLong(4);
                String messageChatId = cursor.getString(6);
                Log.d(TAG, "getChatDataFromInternalDb: value recieved => " + cursor.getInt(3));
                boolean isOpened = cursor.getInt(3) == 0 ? false : true;
                boolean isSent = cursor.getInt(5) == 0 ? false : true;

                Log.d(TAG, "getChatDataFromInternalDb: " + isOpened);
                Log.d(TAG, "getChatDataFromDb: " + messageId);
                final Message message = new Message(messageId, text, senderId, timeStamp, messageChatId, isSent, isOpened);
                messageArrayList.add(message);
                messageAdapter.notifyDataSetChanged();

                if (!message.isSent()) {
                    sendMessage(message);
                }


                Log.d(TAG, "getChatDataFromInternalDb: <====> " + message.getIsOpened());
                if (!message.getIsOpened() && !self.getUserId().trim().equals(message.getSender().trim())) {
                    Log.d(TAG, "getChatDataFromInternalDb: <====> " + message.getMessageId());
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("chats")
                            .child(mChatId).child("messages").child(message.getMessageId());

                    Map<String, Object> updateIsOpened = new HashMap<>();
                    updateIsOpened.put("isOpened", true);

                    dbRef.updateChildren(updateIsOpened).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                try {
                                    SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

                                    Log.d(TAG, "onComplete: mesageId => " + message.getMessageId());
                                    Log.d(TAG, "onComplete: change sqldb status...");
                                    ContentValues cv = new ContentValues();
                                    cv.put("MESSAGE_OPENED", true);
                                    db.update("CHAT_MESSAGES",
                                            cv,
                                            "MESSAGE_ID = ?",
                                            new String[]{message.getMessageId()});

                                    message.setOpened(true);
                                } catch (SQLException e) {
                                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onComplete: exception => " + e.getMessage());
                                }
                            }
                        }
                    });
                }

                if (messageAdapter.getItemCount() > 1) {
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, messageAdapter.getItemCount() - 1);
                }

            }

            Log.d(TAG, "getChatDataFromDb: size => " + messageArrayList.size());
        } catch (SQLException e) {
            Log.d(TAG, "getChatDataFromDb: " + e.getMessage());
        }
    }


    private void sendMessage() {
        String message = mMessageEditText.getText().toString();

        if (message.isEmpty()) {
            Toast.makeText(this, "Cant send an empty string", Toast.LENGTH_SHORT).show();
            return;
        }


        Log.d(TAG, "sendMessage: " + message);

        if (mChatId == null) {
            mChatId = dbRef.push().getKey();

            Map<String, Object> setUsers = new HashMap<>();
            setUsers.put(self.getUserId(), true);
            setUsers.put(user.getUserId(), true);

            dbRef.child(mChatId).child("users").updateChildren(setUsers);

            Map<String, Object> chatList = new HashMap<>();
            chatList.put(mChatId, true);

            DatabaseReference selfRef = FirebaseDatabase.getInstance().getReference("users").child(self.getUserId());
            selfRef.child("chatList").updateChildren(chatList);
            DatabaseReference otherRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUserId());
            otherRef.child("chatList").updateChildren(chatList);
        }

        String messageKey = dbRef.child(mChatId).child("messages")
                .push().getKey();

        Date date = new Date();
        Long time = date.getTime();
        Message messageObj = new Message(messageKey, message, self.getUserId(), time, mChatId, false, false);
        storeChatMessage(messageObj);
        sendMessage(messageObj);

        mMessageEditText.setText(null);
    }

    private void sendMessage(final Message messageObj) {

        Map<String, Object> messageToSend = new HashMap<>();
        messageToSend.put("message", messageObj.getMessage());
        messageToSend.put("sender", messageObj.getSender());
        messageToSend.put("isOpened", messageObj.getIsOpened());
        messageToSend.put("timeStamp", messageObj.getTimeStamp());
        dbRef.child(messageObj.getChatId()).child("messages")
                .child(messageObj.getMessageId()).updateChildren(messageToSend)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            upDateIsSent(messageObj);
                        }
                    }
                });
    }

    private void upDateIsSent(Message message) {
        try {
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("MESSAGE_SENT", true);

            db.update("CHAT_MESSAGES", cv, "MESSAGE_ID = ? ", new String[]{message.getMessageId().trim()});
//            db.close();
        } catch (SQLException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
