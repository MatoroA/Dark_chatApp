package com.hfad.a8tadah.fragments;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hfad.a8tadah.App.MyApplication;
import com.hfad.a8tadah.Model.Chat;
import com.hfad.a8tadah.Model.Message;
import com.hfad.a8tadah.Model.User;
import com.hfad.a8tadah.R;
import com.hfad.a8tadah.adapters.ChatListAdapter;
import com.hfad.a8tadah.helper.MyDatabaseHelper;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private final String TAG = ChatFragment.class.getName();

    private MyDatabaseHelper myDatabaseHelper;
    private ArrayList<Chat> chatArrayList;
    private ArrayList<Chat> chatArrayListFiltered;
    private ChatListAdapter chatListAdapter;
    private EditText mSearchEditText;
    private RecyclerView recyclerView;

    private ImageView profilePic;

    private User self = MyApplication.getInstance().getPreferences().getUser();

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        TextView selfUserName = view.findViewById(R.id.username);
        mSearchEditText = view.findViewById(R.id.search);
        profilePic = view.findViewById(R.id.my_profile_pic);

        if (self.getUserName() != null) {
            selfUserName.setText(self.getUserName());

            if (self.getImageUrl() != null && !self.getImageUrl().isEmpty()){
                Glide.with(view.getContext()).load(self.getImageUrl())
                        .into(profilePic);
            }
        } else {
            selfUserName.setText(self.getName());
        }

        myDatabaseHelper = new MyDatabaseHelper(getContext());
        chatArrayList = new ArrayList<>();
        chatArrayListFiltered = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatArrayList, view.getContext());

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(chatListAdapter);
        getChatsFromInternalDb();

        return view;
    }

    private void getChatsFromInternalDb() {
        try {
            SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("CHATS",
                    new String[]{"USER_ID", "CHAT_ID"},
                    null,
                    null,
                    null,
                    null,
                    null);

            Log.d(TAG, "readFromDb: number of chats" + cursor.getCount());

            chatArrayList.clear();
            while (cursor.moveToNext()) {

                Chat chat = new Chat();
                String userId = cursor.getString(0);
                String chatId = cursor.getString(1);

                Cursor userCursor = db.query("USER",
                        new String[]{"CONTACT", "USER_ID", "USERNAME", "STATUS", "IMG"},
                        "USER_ID = ? ",
                        new String[]{userId.trim()},
                        null,
                        null,
                        null,
                        null);

                Log.d(TAG, "readFromDb: user id => "+userId);
                while (userCursor.moveToNext()) {
                    String imgUrl = userCursor.getString(4);
                    String username = userCursor.getString(2);
                    String status = userCursor.getString(3);
                    String contact = userCursor.getString(0);

                    User user = new User(imgUrl, "", username, userId, contact, status, 0);
                    user.setCurrentChatId(chatId);
                    chat.setUser(user);
                    chat.setChatId(chatId);

                    Log.d(TAG, "readFromDb: user in chats => " + chat.getUser().getName()+
                            "\nname => "+chat.getUser().getName()+"\ncontact => "+chat.getUser().getContact()+
                            "\nimg => "+chat.getUser().getImageUrl()+"\nStatus => "+chat.getUser().getStatus()+"\n\n");
                    chatArrayList.add(chat);
                    chatListAdapter.notifyDataSetChanged();
                }

                Log.d(TAG, "getChatsFromInternalDb: chatlist array size => "+ chatArrayList.size());
                userCursor.close();
            }

            cursor.close();
            db.close();

        } catch (SQLException e) {
            Log.d(TAG, "readFromDb: " + e.getMessage());
        }
    }
}
