package com.app.genialnykredyt;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.genialnykredyt.Contants.BaseUrl;
import com.app.genialnykredyt.Session.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    EditText edtTxtCode;
    Button btnSignIn;
    String verificaitonID;
    String DEVICE_NAME;
    FirebaseAuth mAuth;
    String phone;
    ConnectivityManager connectivityManager;
SessionManager sessionManager;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        mAuth = FirebaseAuth.getInstance();
        String phoneNumber = getIntent().getStringExtra("phonenumber");

        if(phoneNumber != null){
            phone=phoneNumber;
            sendVarificationCode(phoneNumber);
        }

        sessionManager = new SessionManager(this);
        DEVICE_NAME = android.os.Build.MODEL;
        TextView phoneText = findViewById(R.id.numberText);
        btnSignIn=findViewById(R.id.codeInputButton);
        edtTxtCode=findViewById(R.id.inputCode);
        progressBar = findViewById(R.id.progress_bar);
        phoneText.setText(phoneNumber);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = edtTxtCode.getText().toString().trim();
                if(code.isEmpty() || code.length()<6){
                    edtTxtCode.setError("Enter Valid Code");
                    edtTxtCode.requestFocus();
                    return;
                }
                verfiyCode(code);
            }
        });

    }
    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }



    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    private void saveDataToServer(final String phoneNumber) {
        String url = BaseUrl.baseUrl + getResources().getString(R.string.save_mobile_info);
        StringRequest request=new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse( String response) {


                if(response.equalsIgnoreCase("true")){

                }else{

                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {

            }
        })
        {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> params=new HashMap<>();

                params.put("device_name",DEVICE_NAME);
                params.put("phone", phoneNumber);

                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(VerificationActivity.this);
        requestQueue.add(request);
    }

    private void sendVarificationCode(String number){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Toast.makeText(VerificationActivity.this, "Instant Verifcation Completed", Toast.LENGTH_SHORT).show();
            signInWithCredention(phoneAuthCredential);
            String code = phoneAuthCredential.getSmsCode();
            if(code!=null){
                edtTxtCode.setText(code);
                verfiyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerificationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificaitonID=s;
        }
    };

    //In case if automatic detection doesn't work
    private void verfiyCode(String code){
        try{
            PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificaitonID,code);
            signInWithCredention(credential);
        }catch (Exception e){

        }
    }

    private void signInWithCredention(PhoneAuthCredential credential) {
        Intent intent=new Intent(VerificationActivity.this, WebActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    BaseUrl.phoneNumber=phone;
                    sessionManager.setUserLogin("true");
                    sessionManager.setPhoneNumber(phone);
                    Log.i("In",phone);
                    saveDataToServer(phone);
                    Intent intent=new Intent(VerificationActivity.this, WebActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }
}
