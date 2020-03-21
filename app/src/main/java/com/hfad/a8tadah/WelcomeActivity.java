package com.hfad.a8tadah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hfad.a8tadah.App.MyApplication;
import com.hfad.a8tadah.Model.User;
import com.hfad.a8tadah.helper.Country2PhonePrefix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class WelcomeActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getName();

    private String[] mCoutryNames;
    private String coutryCodes[];
    private EditText mCodeEditText;
    private EditText sentCodeEditText;
    private String phoneNumber;
    private EditText mPhone;
    private Button mBtnEnter;
    private LinearLayout inputsLayout;
    private View progressBar;
    private boolean isCodeSent = false;

    private String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if (MyApplication.getInstance().getPreferences().getUser() != null){
            goToNextActivity();
        }

        Log.d(TAG, "onCreate: "+MyApplication.getInstance());

        final Spinner spinner = findViewById(R.id.spinner);
        mCodeEditText = findViewById(R.id.country_code);
        sentCodeEditText = findViewById(R.id.code);
        mBtnEnter = findViewById(R.id.btn_contact);
        mPhone = findViewById(R.id.contact);
        progressBar = findViewById(R.id.progress_bar);
        inputsLayout = findViewById(R.id.inputs_layout);

        mCoutryNames = getResources().getStringArray(R.array.countries);
        coutryCodes = getResources().getStringArray(R.array.codes);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                mCoutryNames
        );
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: it works...");
                String selectedCountryCode = Country2PhonePrefix.getPhone(coutryCodes[position]);
                mCodeEditText.setText(selectedCountryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mBtnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isCodeSent){
                    phoneNumber = mCodeEditText.getText().toString() + mPhone.getText().toString();
                    startPhoneNumberVerification();
                } else {
                    verifyPhoneNumberWithCode();
                }

//                goToNextActivity();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                mPhone.setVisibility(View.GONE);
                mCodeEditText.setVisibility(View.GONE);
                sentCodeEditText.setVisibility(View.VISIBLE);

                isCodeSent = true;

                // ...

            }
        };

        Log.d(TAG, "onCreate: "+coutryCodes[spinner.getSelectedItemPosition()]);
    }

    private void verifyPhoneNumberWithCode() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,
                sentCodeEditText.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }

    private void startPhoneNumberVerification() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        getPermission();

        progressBar.setVisibility(View.VISIBLE);
        inputsLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        inputsLayout.setVisibility(View.GONE);
        mBtnEnter.setVisibility(View.GONE);

        Log.d(TAG, "signInWithPhoneAuthCredential: => we are here");

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            final String uid = user.getUid();
                            String username = phoneNumber;

                            User userData = new User("", username, "", uid, phoneNumber, "", 0);
                            MyApplication.getInstance().getPreferences().storeUser(userData);

                            final Map<String, Object> userDbInfo = new HashMap<>();
                            userDbInfo.put("userName", userData.getUserName());
                            userDbInfo.put("userId", userData.getUserId());
                            userDbInfo.put("contact", userData.getContact());
                            userDbInfo.put("imageUrl", userData.getImageUrl());

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String contact = (String) dataSnapshot.child("contact").getValue();
                                        String userId = (String) dataSnapshot.child("userId").getValue();
                                        String imageUrl = (String) dataSnapshot.child("imageUrl").getValue();
                                        String userName = (String) dataSnapshot.child("userName").getValue();
                                        String status = (String) dataSnapshot.child("status").getValue();

                                        User user = new User(imageUrl, userName, "", userId, phoneNumber, status, 0);
                                        MyApplication.getInstance().getPreferences().storeUser(user);
                                    } else {
                                        FirebaseDatabase.getInstance().getReference("users")
                                                .child(uid)
                                                .updateChildren(userDbInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "onComplete: user data is stored in the database.");
                                                } else {
                                                    Log.d(TAG, "onComplete: failed to store data");
                                                }

                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                                    }

                                    goToNextActivity();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void goToNextActivity(){
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS
            }, 1);
        }
    }
}
