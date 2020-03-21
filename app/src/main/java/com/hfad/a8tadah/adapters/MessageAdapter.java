package com.hfad.a8tadah.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.a8tadah.ChatActivity;
import com.hfad.a8tadah.Model.Chat;
import com.hfad.a8tadah.Model.Message;
import com.hfad.a8tadah.R;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private String TAG = ChatListAdapter.class.getName();
    private ArrayList<Message> messagesArrayList;
    private Context mContext;
    private static final int SELF = 100;
    private String myId = "YhHvXZqJXtU5hkU7yvHUPwionU43";

    public MessageAdapter(ArrayList<Message> messagesArrayList, Context context){
        this.messagesArrayList = messagesArrayList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == SELF){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item_self, null, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item_other, null, false);
        }


        return new MyViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesArrayList.get(position).getSender().equals(myId)){
            return SELF;
        }

        return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final Message message = messagesArrayList.get(position);
        Log.d(TAG, "onBindViewHolder: "+message.getMessage());
        holder.message.setText(message.getMessage());
    }
    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView message;
        public MyViewHolder(View view){
            super(view);
            message = view.findViewById(R.id.message);
        }
    }
}
