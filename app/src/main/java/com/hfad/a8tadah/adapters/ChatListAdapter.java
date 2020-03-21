package com.hfad.a8tadah.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hfad.a8tadah.App.MyApplication;
import com.hfad.a8tadah.ChatActivity;
import com.hfad.a8tadah.Model.Chat;
import com.hfad.a8tadah.Model.Message;
import com.hfad.a8tadah.Model.User;
import com.hfad.a8tadah.R;
import com.hfad.a8tadah.helper.MyDatabaseHelper;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {

    public interface onChatListener{
        void onItemClick(int i);
    }

    private String TAG = ChatListAdapter.class.getName();
    private ArrayList<Chat> chatArrayList;
    private ArrayList<Message> incomingMessages;
    private ArrayList<String> chatMessagesId;
    private Context mContext;
    private MyDatabaseHelper myDatabaseHelper;

    private MyViewHolder myViewHolder;

    private User self = MyApplication.getInstance().getPreferences().getUser();

    public ChatListAdapter(ArrayList<Chat> chatArrayList, Context context) {
        this.chatArrayList = chatArrayList;
        this.mContext = context;
        myDatabaseHelper = new MyDatabaseHelper(context);
        this.incomingMessages = new ArrayList<>();
        this.chatMessagesId = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, null, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        myViewHolder = holder;
        final Chat chat = chatArrayList.get(position);
        Log.d(TAG, "onBindViewHolder: chat "+ ++position +" : id => "+chat.getChatId());
        readChatData(chat);
        readNewMessagesFromDb(chat);
        readUserDataFromDb(chat.getUser(), holder);
        holder.name.setText(chat.getUser().getName());
        if (chat.getLastMessge() != null) {
            holder.lastMessage.setText(chat.getLastMessge());
        }

        Log.d(TAG, "onBindViewHolder: " + chat.getLastMessge());

        if (chat.getUnReadCount() > 0) {
            holder.newMessageCount.setVisibility(View.VISIBLE);
            holder.newMessageCount.setText(String.valueOf(chat.getUnReadCount()));
        } else {
            holder.newMessageCount.setVisibility(View.GONE);
        }

        bindUserData(chat.getUser(), holder);
//        getUserData(chat.getUser());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(ChatActivity.USER_INFO, chat.getUser());
                intent.putExtras(bundle);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
                Toast.makeText(mContext, "it is clicked...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, lastMessage, newMessageCount, online, offline;
        ImageView profile_pic;

        public MyViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.name_text);
            lastMessage = view.findViewById(R.id.last_text);
            newMessageCount = view.findViewById(R.id.new_message_count);
            profile_pic = view.findViewById(R.id.pro_pic);
            online = view.findViewById(R.id.online);
            offline = view.findViewById(R.id.offline);
        }
    }

    private void bindUserData(final User user, MyViewHolder holder) {
        if (user.isOnline()) {
            holder.online.setVisibility(View.VISIBLE);
            holder.offline.setVisibility(View.GONE);
        } else {
            holder.online.setVisibility(View.GONE);
            holder.offline.setVisibility(View.VISIBLE);
        }

        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Glide.with(mContext).load(user.getImageUrl())
                    .into(holder.profile_pic);
            Log.d(TAG, "bindUserData: " + user.getImageUrl());
        }

    }

    private void readNewMessagesFromDb(final Chat chat) {
        FirebaseDatabase.getInstance().getReference("chats")
                .child(chat.getChatId().trim()).child("messages").orderByChild("isOpened").equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        incomingMessages.clear();
                        if (dataSnapshot.exists()) {
//                            Log.d(TAG, "onDataChange: there is new data"+dataSnapshot);
                            for (DataSnapshot newMessage : dataSnapshot.getChildren()) {
                                String messageKey = newMessage.getKey();
                                Message newMessageObj = newMessage.getValue(Message.class);
                                newMessageObj.setMessageId(messageKey);
                                Log.d(TAG, "onDataChange: new message => " + messageKey);
                                Log.d(TAG, "onDataChange: => isOpened" + newMessageObj.getIsOpened());

//                                Log.d(TAG, "onDataChange: "+newMessageObj.getIsOpened());

                                if (!newMessageObj.getSender().equals(self.getUserId()))
                                    storeNewMessage(newMessageObj, chat);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void storeNewMessage(Message newMessage, Chat chat) {

        try {
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

            Cursor cursor = db.query("CHAT_MESSAGES",
                    new String[]{"MESSAGE_ID", "CHAT_ID"},
                    "MESSAGE_ID = ? AND CHAT_ID = ?",
                    new String[]{newMessage.getMessageId(), chat.getChatId()},
                    null,
                    null,
                    null,
                    null
            );

            Log.d(TAG, "storeNewMessage: count => "+ cursor.getCount());
            if (cursor.getCount() == 0) {
                ContentValues cv = new ContentValues();
                cv.put("SENDER_ID", newMessage.getSender());
                cv.put("MESSAGE_ID", newMessage.getMessageId());
                cv.put("MESSAGE_TEXT", newMessage.getMessage());
                cv.put("MESSAGE_OPENED", newMessage.getIsOpened());
                cv.put("CHAT_ID", chat.getChatId().trim());
                Log.d(TAG, "storeNewMessage: => " + newMessage.getChatId());

                db.insert("CHAT_MESSAGES", null, cv);
            }

            readChatData(chat);

            cursor.close();
            db.close();
        } catch (SQLException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void readChatData(Chat chat) {
        try {
            SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("CHAT_MESSAGES",
                    new String[]{"MESSAGE_TEXT", "MESSAGE_ID", "CHAT_ID"},
                    "CHAT_ID = ?",
                    new String[]{chat.getChatId().trim()},
                    "",
                    "",
                    "");
            Log.d(TAG, "readChatData: username : "+ chat.getUser().getName()+"\nChatid : "+ chat.getChatId());

            if (cursor.moveToLast()) {
                chat.setLastMessge(cursor.getString(0));
                myViewHolder.lastMessage.setText(chat.getLastMessge());

                Log.d(TAG, "readChatData: last message = > "+ chat.getLastMessge());
            } else{
                myViewHolder.lastMessage.setText("");
            }

            Cursor cursor1 = db.query("CHAT_MESSAGES", new String[]{"MESSAGE_OPENED", "SENDER_ID", "CHAT_ID", "MESSAGE_TEXT"},
                    "CHAT_ID = ? AND MESSAGE_OPENED = ?",
                    new String[]{chat.getChatId().trim(), "0"},
                    "",
                    "",
                    "",
                    "");
            int count = 0;
            while (cursor1.moveToNext()) {
//                ++count;
                Log.d(TAG, "readChatData: => sent " + cursor1.getString(0));
                Log.d(TAG, "readChatData: new message count <==> " + cursor1.getString(1));

                if (!cursor1.getString(1).trim().equals(self.getUserId().trim())) {
                    ++count;
                    Log.d(TAG, "readChatData: => user 1 " + self.getUserId());
                    Log.d(TAG, "readChatData: => user 2 " + cursor1.getString(1).trim());
                    Log.d(TAG, "readChatData: we have a new message => " + cursor1.getString(3));
                }

                Log.d(TAG, "readChatData: => user count " + count);
            }

            Log.d(TAG, "readChatData: " + count);
            if (count > 0) {
                chat.setUnReadCount(count);
                Log.d(TAG, "readChatData: => count " + count);
                myViewHolder.newMessageCount.setVisibility(View.VISIBLE);
                myViewHolder.newMessageCount.setText(String.valueOf(chat.getUnReadCount()));
//                myViewHolder.newMessageCount.setText(String.valueOf(count));
            } else {
                chat.setUnReadCount(0);
                myViewHolder.newMessageCount.setVisibility(View.GONE);
                Log.d(TAG, "readChatData: we have nothing => count " + count);
            }
            Log.d(TAG, "readChatData: " + count);

        } catch (SQLException e) {

        }
    }


    private void readUserDataFromDb(final User user, final MyViewHolder holder) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: user data => " + dataSnapshot.getValue());
                    String userName = (String) dataSnapshot.child("userName").getValue();
                    String imageUrl = (String) dataSnapshot.child("imageUrl").getValue();
                    String status = (String) dataSnapshot.child("status").getValue();

                    user.setStatus(status);
                    user.setImageUrl(imageUrl);
                    user.setUserName(userName);

                    bindUserData(user, holder);

                    saveUserData(user);

                    Log.d(TAG, "onDataChange: u");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserData(User user) {
        try {
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("IMG", user.getImageUrl());
            cv.put("STATUS", user.getStatus());

            db.update("USER", cv, "USER_ID = ?", new String[]{user.getUserId()});
        } catch (SQLException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
