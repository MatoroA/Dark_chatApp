package com.hfad.a8tadah.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hfad.a8tadah.ChatActivity;
import com.hfad.a8tadah.Model.Chat;
import com.hfad.a8tadah.Model.User;
import com.hfad.a8tadah.R;
import com.hfad.a8tadah.helper.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.Set;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    private String TAG = ContactAdapter.class.getName();
    private ArrayList<User> userList;
    private Context mContext;

    private String userId;
    private String userStatus;
    private String userImg;
    private MyDatabaseHelper myDatabaseHelper;

    public ContactAdapter(ArrayList<User> userList, Context context){
        this.userList = userList;
        this.mContext = context;
        this.myDatabaseHelper = new MyDatabaseHelper(mContext);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, null, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final User user = userList.get(position);
        searchForUser(user, holder);
        getDataFromInternalDb(user, holder);
        holder.name.setText(user.getName());



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(ChatActivity.USER_INFO, user);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });
    }

    private void searchForUser(final User user, final MyViewHolder holder){
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("contact").equalTo(user.getContact().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot data: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: "+data.getValue());

                        String userId = (String) data.child("userId").getValue();
                        String userImg = (String) data.child("imageUrl").getValue();
                        String userStatus = (String) data.child("status").getValue();

                        user.setUserId(userId);
                        user.setImageUrl(userImg);
                        user.setStatus(userStatus);
//
                        bindData(user, holder);
                        saveUserWithApp(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView mUserpic;
        TextView name, status;
        public MyViewHolder(View view){
            super(view);

            name = view.findViewById(R.id.name);
            status = view.findViewById(R.id.status);
            mUserpic = view.findViewById(R.id.userpic);
        }
    }

    private void saveUserWithApp(User user){

        Log.d(TAG, "saveUserWithApp: Save data");
        try {
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
//
            Cursor cursor = db.query("USER",
                    new String[]{"CONTACT"},
                    "CONTACT = ?",
                    new String[]{user.getContact().trim()},
                    "",
                    "",
                    "");

            ContentValues cv = new ContentValues();

            if (cursor.getCount() > 0){
                cv.put("USERNAME", user.getUserName());
                cv.put("STATUS", user.getStatus());
//                cv.put("IMG", user.getImageUrl());

                Log.d(TAG, "saveUserWithApp: About to update data of the user");
                db.update("USER",cv , "CONTACT = ? ", new String[]{user.getContact().trim()});
            } else {
                cv.put("USERNAME", user.getUserName());
                cv.put("STATUS", user.getStatus());
//                cv.put("IMG", user.getImageUrl());
                cv.put("CONTACT", user.getContact());
                cv.put("USER_ID", user.getUserId());

                Log.d(TAG, "saveUserWithApp: About to insert new record");
                db.insert("USER", null, cv);
            }
        } catch (SQLException e){
            Log.d(TAG, "saveUserWithApp: "+ e.getMessage());
        }
    }

    private void getDataFromInternalDb(User user, MyViewHolder holder){

        Log.d(TAG, "getDataFromInternalDb: Read data");
        try {
            SQLiteDatabase db = myDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("USER",
                    new String[]{"CONTACT", "USERNAME", "STATUS", "IMG"},
                    "CONTACT = ?",
                    new String[]{user.getContact().trim()},
                    "",
                    "",
                    "");

            Log.d(TAG, "getDataFromInternalDb: cursor.getcount => "+cursor.getCount());
            while(cursor.moveToNext()){
                Log.d(TAG, "getDataFromInternalDb: => "+cursor.getString(2));
                user.setStatus(cursor.getString(2));
                user.setImageUrl(cursor.getString(3));

            }
            //assign data to the holder....
            bindData(user, holder);

        } catch (SQLException e){

        } catch (CursorIndexOutOfBoundsException e){
            Log.d(TAG, "getDataFromInternalDb: => "+ e.getMessage());
        }

    }

    private void bindData(User user, MyViewHolder holder){

        if (!TextUtils.isEmpty(user.getImageUrl())){
//            Glide.with(mContext).load(user.getImageUrl())
//                    .into(holder.mUserpic);

            Log.d(TAG, "bindData: contact : "+user.getName()+"\n imageUrl : "+user.getImageUrl());
        }
        if (!TextUtils.isEmpty(user.getStatus())){
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText(user.getStatus());
        } else {
            holder.status.setVisibility(View.GONE);
        }
    }
}

