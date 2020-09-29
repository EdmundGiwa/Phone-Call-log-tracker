package com.example.tracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
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
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MobileAuthActivity extends AppCompatActivity {

    private static final String TAG = " MobileAuth";
    RelativeLayout otpContainer;
    PinView pinInputBox;
    String number;
    CountryCodePicker countryCodePicker;
    private EditText mobileTxtfield;
    private Button verifyCode, sendCode;
    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationStateChangedCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(17, 0, 17));
        setContentView(R.layout.activity_mobile_auth);

        mobileTxtfield = findViewById(R.id.num_textField);
        pinInputBox = findViewById(R.id.otp_input_box);
        sendCode = findViewById(R.id.verify_number);
        verifyCode = findViewById(R.id.verify_otp_number);
        otpContainer = findViewById(R.id.otp_container);

        countryCodePicker = findViewById(R.id.countcodepicker);
        countryCodePicker.registerCarrierNumberEditText(mobileTxtfield);

        fbAuth = FirebaseAuth.getInstance();


    }

    public void sendCode(View view) {
        number = countryCodePicker.getFullNumberWithPlus();

        setUpVerificationsCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                verificationStateChangedCallbacks
        );

        otpContainer.setVisibility(View.VISIBLE);
    }

    private void setUpVerificationsCallbacks() {
        verificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//                otpContainer.setVisibility(View.VISIBLE);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.d(TAG, "Invalid credential: " + e.getLocalizedMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.d(TAG, "SMS Quota exceeded");
                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                phoneVerificationId = s;
                resendingToken = forceResendingToken;
            }
        };
    }

    public void verifyCode(View view) {
        String code = pinInputBox.getText().toString();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fbAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = task.getResult().getUser();
                    String phoneNumber = user.getPhoneNumber();

                    Intent intent = new Intent(MobileAuthActivity.this, ProfileActivity.class);
                    intent.putExtra("phone", phoneNumber);
                    startActivity(intent);
                    finish();
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    }
                }
            }
        });
    }
}