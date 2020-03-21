package com.hfad.a8tadah.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hfad.a8tadah.App.MyApplication;
import com.hfad.a8tadah.Model.User;
import com.hfad.a8tadah.R;
import com.hfad.a8tadah.dialog.ProfileDialogFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment
        implements ProfileDialogFragment.ProfileDailogInterface {

    private final String TAG = ProfileFragment.class.getName();

    private ImageView profilePic;
    private TextView mUsername;
    private TextView mStatus;

    private RelativeLayout mNameLayout, mStatusLayout;

    private User self = MyApplication.getInstance().getPreferences().getUser();
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users")
            .child(self.getUserId());

    private static String KEY;

    private final String USERNAME = "userName";
    private final String STATUS = "status";

    private View view;
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        displayData(view);


        mStatusLayout = view.findViewById(R.id.statusLayout);
        mNameLayout = view.findViewById(R.id.nameLayout);

        mNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(), "clicked...", Toast.LENGTH_SHORT).show();

                String value = null;

                if(mUsername.getText() != null){
                    value = mUsername.getText().toString();
                }
                KEY = USERNAME;
                ProfileDialogFragment dialogFragment = new ProfileDialogFragment(value, new ProfileFragment());
                dialogFragment.show(getChildFragmentManager(), "");


            }
        });

        mStatusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KEY = STATUS;
                Toast.makeText(view.getContext(), "clicked...", Toast.LENGTH_SHORT).show();

                String value = null;

                if(mStatus.getText() != null){
                    value = mStatus.getText().toString();
                }

                Log.d(TAG, "onClick: => "+KEY);
                ProfileDialogFragment dialogFragment = new ProfileDialogFragment(value, new ProfileFragment());
                dialogFragment.show(getChildFragmentManager(), "");
            }
        });

        Log.d(TAG, "onCreateView: this will runn => "+KEY);

        return view;
    }

    @Override
    public void onChangeField(String value) {
        Log.d(TAG, "onChangeField: => " + KEY);

//        Toast.makeText(getContext(), "This should work...", Toast.LENGTH_LONG).show();
        updateUserData(value);
    }

    private void updateUserData(final String value) {

        Log.d(TAG, "updateUserData: KEY => "+KEY);
        Log.d(TAG, "updateUserData: this should work.. value => "+ value);
        Map<String,  Object> newData = new HashMap<>();
        newData.put("status", value);
        dbRef.updateChildren(newData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (KEY.equals(USERNAME)) {
                        self.setUserName(value);
                        Log.d(TAG, "onComplete: username is added..");
                    }

                    if (KEY.equals(STATUS)) {
                        self.setStatus(value);
                        Log.d(TAG, "onComplete: status is added..");
                        Log.d(TAG, "onComplete: view => "+getView());
//                        displayData(view);
                        Log.d(TAG, "onComplete: it worked...");
                    }

                    MyApplication.getInstance().getPreferences().storeUser(self);
//                    mUsername.setText(null);
//                    mUsername.setText(self.getUserName());

                    Log.d(TAG, "onComplete: successful... => ");
//                    Toast.makeText(getContext(), "Successful...", Toast.LENGTH_LONG).show();
                }

                if (!task.isSuccessful()){
                    Log.d(TAG, "onComplete: not successful... => ");
                }
            }
        });
    }

    private void displayData(View view){
        profilePic = view.findViewById(R.id.my_profile_pic);
        mUsername = view.findViewById(R.id.username);
        mStatus = view.findViewById(R.id.status);

        if (self != null) {

            mUsername.setText(self.getUserName());
            mStatus.setText(self.getStatus());
            Log.d(TAG, "onCreateView: status => " + self.getStatus());

            if (self.getImageUrl() != null && !self.getImageUrl().isEmpty()) {
                Glide.with(view.getContext()).load(self.getImageUrl())
                        .into(profilePic);
            }

            mStatus.setText(self.getStatus());
        }

    }
}
