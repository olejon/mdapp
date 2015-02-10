package net.olejon.mdapp;

/*

Copyright 2015 Ole Jon Bjørkum

This file is part of LegeAppen.

LegeAppen is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

LegeAppen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with LegeAppen.  If not, see <http://www.gnu.org/licenses/>.

*/

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;

import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerActivity extends Activity implements ZXingScannerView.ResultHandler
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private ZXingScannerView mZXingScannerView;

    // Create activity
    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);

        // Window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Barcode scanner
        mZXingScannerView = new ZXingScannerView(this);

        setContentView(mZXingScannerView);
    }

    // Resume activity
    @Override
    public void onResume()
    {
        super.onResume();

        mZXingScannerView.setResultHandler(this);

        mZXingScannerView.startCamera();
    }

    // Pause activity
    @Override
    public void onPause()
    {
        super.onPause();

        mZXingScannerView.stopCamera();
    }

    // Result
    @Override
    public void handleResult(Result result)
    {
        mTools.showToast(getString(R.string.barcode_scanner_wait), 0);

        String barcode = result.getText();

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website)+"api/1/barcode/?search="+barcode, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    String uri = response.getString("uri");

                    if(uri.equals(""))
                    {
                        mTools.showToast(getString(R.string.barcode_scanner_no_results), 1);

                        finish();
                    }
                    else
                    {
                        mTools.getMedicationWithFullContent(uri);

                        finish();
                    }
                }
                catch(Exception e)
                {
                    mTools.showToast(getString(R.string.barcode_scanner_no_results), 1);

                    Log.e("BarcodeScannerActivity", Log.getStackTraceString(e));

                    finish();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("FelleskatalogenService", error.toString());
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);
    }
}