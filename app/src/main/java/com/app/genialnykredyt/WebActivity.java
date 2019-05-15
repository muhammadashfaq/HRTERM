package com.app.genialnykredyt;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.genialnykredyt.Contants.BaseUrl;
import com.app.genialnykredyt.Service.PingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebActivity extends AppCompatActivity
{
    WebView webView;
    WebSettings webSettings;
    ProgressDialog progressDialog;
    Intent mServiceIntent;
    String apiresponse;
    private PingService pingService;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait for a litte while");
        progressDialog.setCancelable(false);

        Context context=getApplicationContext();
        UpdateToServer serverClass=new UpdateToServer(context);
        trimCache(this);
        serverClass.addtoServer(context);

//        pingService = new PingService(this);
//        mServiceIntent = new Intent(this, PingService.class);
//        if (!isMyServiceRunning(PingService.class)) {
//            startService(mServiceIntent);
//        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);

        webView=findViewById(R.id.webview);
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);

        getWebstireUrlFromServer();

    }

    private String getWebstireUrlFromServer() {

        progressDialog.show();
        trimCache(this);
        String url = BaseUrl.baseUrl + getResources().getString(R.string.get_website_url);
        StringRequest request=new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        {

            @Override
            public void onResponse( String response) {

                try {
                   JSONObject jsonObject = new JSONObject(response);
                    JSONArray array=jsonObject.getJSONArray("result");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        apiresponse=object.getString("website_url");
                    }
                    startWebView(apiresponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(WebActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> params=new HashMap<>();

                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);

        return apiresponse;
    }


    private void startWebView(final String url)
    {
        WebSettings settings = webView.getSettings();
        webView.setWebChromeClient(new WebChromeClient());

        settings.setJavaScriptEnabled(true);

        //webView1.setScrollBarStyle(webView1.SCROLLBARS_OUTSIDE_OVERLAY);




        webView.setWebViewClient(new WebViewClient()
        {
            public void onPageFinished(WebView view, String url)
            {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getBaseContext(), "Error:" + description, Toast.LENGTH_SHORT).show();

            }
        });
        webView.loadUrl(url);
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
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
