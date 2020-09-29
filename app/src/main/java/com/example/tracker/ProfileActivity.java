package com.example.tracker;

import android.Manifest.permission;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    TextView logTxt;

   private Button sync_btn;

    private ProgressDialog progressDialog;
    private DatabaseReference userLogRef;
    private FirebaseAuth userAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(17, 0, 17));
        setContentView(R.layout.activity_profile);
        progressDialog = new ProgressDialog(this);
//
//        referenceCallLog = FirebaseDatabase.getInstance().getReference("Log Table");
//
//        sync_btn = findViewById(R.id.sync_btn);
//
//        sync_btn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getCallDetails();
//            }
//        });
        userLogRef = FirebaseDatabase.getInstance().getReference().child("users");
        progressDialog.setMessage("Uploading Call Logs");
        userAuth = FirebaseAuth.getInstance();

        sync_btn = findViewById(R.id.sync_btn);

        sync_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadLog();
            }
        });


        if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this
                    , permission.READ_CALL_LOG)) {
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{permission.READ_CALL_LOG}, 1);
            } else {
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{permission.READ_CALL_LOG}, 1);
            }
        } else {
            TextView log_details = findViewById(R.id.log_detsTxt);
            log_details.setText(getCallDetails());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                            permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        TextView log_details = findViewById(R.id.log_detsTxt);
                        log_details.setText(getCallDetails());
                    }
                } else {
                    Toast.makeText(this, "No Permission Granted", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private String getCallDetails() {
        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursorManager = getContentResolver().query(Calls.CONTENT_URI, null, null, null, null);
        int number = cursorManager.getColumnIndex(Calls.NUMBER);
        int type = cursorManager.getColumnIndex(Calls.TYPE);
        int date = cursorManager.getColumnIndex(Calls.DATE);
        int duration = cursorManager.getColumnIndex(Calls.DURATION);
        stringBuffer.append("Log Details \n");

        while (cursorManager.moveToNext()) {
            String phNum = cursorManager.getString(number);
            String callType = cursorManager.getString(type);
            String callDate = cursorManager.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
            String dateString = dateFormat.format(callDayTime);
            String callDuration = cursorManager.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            stringBuffer.append("\nPhone Number: " + phNum
                    + "\ncallType: " + dir
                    + "\nCall Date: " + dateString
                    + "\nCall Duration: " + callDuration);
            stringBuffer.append("\n________________________");
        }
        cursorManager.close();
        return stringBuffer.toString();
    }
    private void uploadLog(){
        Cursor logCursor = getContentResolver().query(Calls.CONTENT_URI, new String[]{
                        Calls.DATE.toString(),
                        Calls.DURATION.toString()},
                null,
                null,
                null
        );
        HashMap map = new HashMap();
        if (logCursor != null) {
            while (logCursor.moveToNext()) {
                map.put(
                        logCursor.getString(logCursor.getColumnIndex(Calls.DATE)),
                        logCursor.getString(logCursor.getColumnIndex( Calls.DURATION)));
            }
            logCursor.close();
        }
        progressDialog.show();
        userLogRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

}