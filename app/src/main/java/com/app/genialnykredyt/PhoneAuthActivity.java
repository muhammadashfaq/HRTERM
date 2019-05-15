package com.app.genialnykredyt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.genialnykredyt.Contants.BaseUrl;
import com.app.genialnykredyt.Session.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhoneAuthActivity extends AppCompatActivity {

    private EditText mPhoneNumber;
    private Button mSmsButton;
    SessionManager sessionManager;

    MaterialSpinner materialSpinner;

    String[] apppermissions={
            Manifest.permission.INTERNET,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
    };

    ProgressDialog progressDialog;

    EditText edtTxtName,edtTxtEmail,edtTxtPhone;


    ConnectivityManager connectivityManager;
    public static int PERMISSION_CODE = 100;
    int deniedCount;

    Toolbar toolbar;
    Button btn;
    String DEVICE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        mSmsButton = findViewById(R.id.smsVerificationButton);
        mPhoneNumber = findViewById(R.id.phoneNumber);
        materialSpinner = findViewById(R.id.spinner);

        materialSpinner.setItems(CountryData.countryNames);

//        sessionManager = new SessionManager(this);
//        if(sessionManager.getUserLogin().equalsIgnoreCase("true")){
//            startActivity(new Intent(this,WebActivity.class));
//        }

        mSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAndRequestPermissions())
                {
                    if(IsNetworkConnected())
                    {
                        doLoginProcess();
                    }
                    else
                    {
                        Toast.makeText(PhoneAuthActivity.this, "Oops No internet connection", Toast.LENGTH_SHORT).show();
                    }


                }else{
                    Toast.makeText(PhoneAuthActivity.this, "Permissions are necessary to use the site features correctly", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    private boolean IsNetworkConnected() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null;
    }




    private boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission: apppermissions){
            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(permission);
            }
        }

        if(!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_CODE);
            return false;
        }
        return true;
    }

    private AlertDialog showDialog(String title, String msg, String positivelable, DialogInterface.OnClickListener possitiveOnClick, String negeativeLable, DialogInterface.OnClickListener negativeOnClick, boolean isCancelable) {
        AlertDialog.Builder alertDailog = new AlertDialog.Builder(this);
        alertDailog.setTitle(title);
        alertDailog.setCancelable(isCancelable);
        alertDailog.setMessage(msg);
        alertDailog.setPositiveButton(positivelable,possitiveOnClick);
        alertDailog.setNegativeButton(negeativeLable,negativeOnClick);
        AlertDialog alert = alertDailog.create();
        alert.show();
        return alert;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            HashMap<String,Integer> permissionResults=new HashMap<>();
            deniedCount = 0;

            for(int i=0;i<grantResults.length;i++){

                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    permissionResults.put(permissions[i],grantResults[i]);
                    deniedCount++;
                }
            }


            //check if all permissions are granted
            if(deniedCount == 0){
                doLoginProcess();
                //goAhead();
            }
            //At least one or all permission are denied
            else {
                for(Map.Entry<String,Integer> entry: permissionResults.entrySet()){
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    //permission is denied. (This is first time ,when "Never Ask Again" is not checked)
                    //so Ask again explaining usage of permission
                    //shouldShowRequestPermissionResultRational return true

                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,permName)){

                        //show dailog for explainaiton
                        showDialog("","This app needs all asked permission to work. Please Grant All Permission ",
                                "YES, Grant Permission", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        checkAndRequestPermissions();
                                    }
                                },
                                "No ,Exit App", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                },false);


                    }
                    //pemission is denied and never ask again is checked
                    //shouldShowRequestPermisionResultsRational return false
                    else{
                        showDialog("",
                                "You have denied some permissions.  Allow all permission at [Setting] > [Permission]",
                                "Go to Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    }
                                },
                                "NO ,Exit App", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                },false);
                        break;
                    }
                }
            }
        }
    }

    private void doLoginProcess() {
        String code = CountryData.countryAreaCodes[materialSpinner.getSelectedIndex()];
        String number = mPhoneNumber.getText().toString().trim();

        if(number.isEmpty()){
            mPhoneNumber.setError("Enter Phone Number Please");
            mPhoneNumber.requestFocus();
            return;
        }
        String phone_number= "+"+code+number;

        Intent intent=new Intent(PhoneAuthActivity.this,VerificationActivity.class);
        intent.putExtra("phonenumber",phone_number);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent=new Intent(PhoneAuthActivity.this,WebActivity.class);
            startActivity(intent);
        }
    }
}
