package net.olejon.mdapp;

/*

Copyright 2016 Ole Jon Bjørkum

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see http://www.gnu.org/licenses/.

*/

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.vending.billing.IInAppBillingService;

import org.json.JSONObject;

import java.util.ArrayList;

public class DonateActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private IInAppBillingService mIInAppBillingService;

    private Button mMakeSmallDonationButton;
    private Button mMakeMediumDonationButton;
    private Button mMakeBigDonationButton;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected())
        {
            mTools.showToast(getString(R.string.device_not_connected), 1);

            finish();

            return;
        }

        // Layout
        setContentView(R.layout.activity_donate);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.donate_toolbar);
        toolbar.setTitle(getString(R.string.donate_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // In-app billing
        Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        intent.setPackage("com.android.vending");

        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        // Buttons
        mMakeSmallDonationButton = (Button) findViewById(R.id.donate_make_small_donation);
        mMakeMediumDonationButton = (Button) findViewById(R.id.donate_make_medium_donation);
        mMakeBigDonationButton = (Button) findViewById(R.id.donate_make_big_donation);

        mMakeSmallDonationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                makeDonation("small_donation");
            }
        });

        mMakeMediumDonationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                makeDonation("medium_donation");
            }
        });

        mMakeBigDonationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                makeDonation("big_donation");
            }
        });
    }

    // Activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1)
        {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if(resultCode == RESULT_OK)
            {
                try
                {
                    JSONObject purchaseDataJsonObject = new JSONObject(purchaseData);

                    consumeDonation(purchaseDataJsonObject.getString("purchaseToken"));

                    mTools.showToast(getString(R.string.donate_thank_you), 1);

                    finish();
                }
                catch(Exception e)
                {
                    mTools.showToast(getString(R.string.donate_something_went_wrong), 1);
                }
            }
        }
    }

    // Destroy activity
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(mServiceConnection != null) unbindService(mServiceConnection);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_donate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.donate_menu_reset:
            {
                resetDonations();
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Donations
    private void makeDonation(String product)
    {
        try
        {
            Bundle bundle = mIInAppBillingService.getBuyIntent(3, getPackageName(), product, "inapp", "");
            PendingIntent pendingIntent = bundle.getParcelable("BUY_INTENT");

            IntentSender intentSender = (pendingIntent != null) ? pendingIntent.getIntentSender() : null;

            startIntentSenderForResult(intentSender, 1, new Intent(), 0, 0, 0);
        }
        catch(Exception e)
        {
            new MaterialDialog.Builder(mContext).title(getString(R.string.device_not_supported_dialog_title)).content(getString(R.string.device_not_supported_dialog_message)).positiveText(getString(R.string.device_not_supported_dialog_positive_button)).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
        }
    }

    private void consumeDonation(String purchaseToken)
    {
        try
        {
            mIInAppBillingService.consumePurchase(3, getPackageName(), purchaseToken);
        }
        catch(Exception e)
        {
            new MaterialDialog.Builder(mContext).title(getString(R.string.device_not_supported_dialog_title)).content(getString(R.string.device_not_supported_dialog_message)).positiveText(getString(R.string.device_not_supported_dialog_positive_button)).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
        }
    }

    private void resetDonations()
    {
        try
        {
            Bundle bundle = mIInAppBillingService.getPurchases(3, getPackageName(), "inapp", null);

            int responseCode = bundle.getInt("RESPONSE_CODE");

            if(responseCode == 0)
            {
                ArrayList<String> purchaseDataArrayList = bundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                if(purchaseDataArrayList != null)
                {
                    for(String purchaseData : purchaseDataArrayList)
                    {
                        JSONObject purchaseDataJsonObject = new JSONObject(purchaseData);

                        consumeDonation(purchaseDataJsonObject.getString("purchaseToken"));
                    }

                    mTools.showToast(getString(R.string.donate_reset_successful), 0);
                }
            }
            else
            {
                new MaterialDialog.Builder(mContext).title(getString(R.string.device_not_supported_dialog_title)).content(getString(R.string.device_not_supported_dialog_message)).positiveText(getString(R.string.device_not_supported_dialog_positive_button)).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
            }
        }
        catch(Exception e)
        {
            new MaterialDialog.Builder(mContext).title(getString(R.string.device_not_supported_dialog_title)).content(getString(R.string.device_not_supported_dialog_message)).positiveText(getString(R.string.device_not_supported_dialog_positive_button)).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
        }
    }

    // Get products
    private class GetProductsTask extends AsyncTask<Void, Void, Bundle>
    {
        @Override
        protected void onPostExecute(Bundle skuDetailsBundle)
        {
            if(skuDetailsBundle == null)
            {
                mTools.showToast(getString(R.string.donate_something_went_wrong), 1);
            }
            else
            {
                try
                {
                    int responseCode = skuDetailsBundle.getInt("RESPONSE_CODE");

                    if(responseCode == 0)
                    {
                        ArrayList<String> responseArrayList = skuDetailsBundle.getStringArrayList("DETAILS_LIST");

                        if(responseArrayList != null)
                        {
                            for(String details : responseArrayList)
                            {
                                JSONObject detailsJsonObject = new JSONObject(details);

                                String sku = detailsJsonObject.getString("productId");
                                String price = detailsJsonObject.getString("price");

                                switch(sku)
                                {
                                    case "small_donation":
                                    {
                                        mMakeSmallDonationButton.setText(getString(R.string.donate_donate)+" "+price);
                                        break;
                                    }
                                    case "medium_donation":
                                    {
                                        mMakeMediumDonationButton.setText(getString(R.string.donate_donate)+" "+price);
                                        break;
                                    }
                                    case "big_donation":
                                    {
                                        mMakeBigDonationButton.setText(getString(R.string.donate_donate)+" "+price);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    mTools.showToast(getString(R.string.donate_something_went_wrong), 1);
                }
            }
        }

        @Override
        protected Bundle doInBackground(Void... voids)
        {
            ArrayList<String> skusArrayList = new ArrayList<>();

            skusArrayList.add("small_donation");
            skusArrayList.add("medium_donation");
            skusArrayList.add("big_donation");

            Bundle skusBundle = new Bundle();
            skusBundle.putStringArrayList("ITEM_ID_LIST", skusArrayList);

            Bundle skuDetailsBundle = null;

            try
            {
                skuDetailsBundle = mIInAppBillingService.getSkuDetails(3, getPackageName(), "inapp", skusBundle);
            }
            catch(Exception e)
            {
                Log.e("DonateActivity", Log.getStackTraceString(e));
            }

            return skuDetailsBundle;
        }
    }

    // Service
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mIInAppBillingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mIInAppBillingService = IInAppBillingService.Stub.asInterface(service);

            GetProductsTask getProductsTask = new GetProductsTask();
            getProductsTask.execute();
        }
    };
}