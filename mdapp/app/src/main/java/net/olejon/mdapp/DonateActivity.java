package net.olejon.mdapp;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONObject;

import java.util.ArrayList;

public class DonateActivity extends ActionBarActivity
{
    private final MyTools mTools = new MyTools(this);

    private IInAppBillingService mIInAppBillingService;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
    }

    // Destroy activity
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(mIInAppBillingService != null) unbindService(mServiceConnection);
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

                    Log.e("DonateActivity", Log.getStackTraceString(e));
                }
            }
        }
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

            startIntentSenderForResult(pendingIntent.getIntentSender(), 1, new Intent(), 0, 0, 0);
        }
        catch(Exception e)
        {
            mTools.showToast(getString(R.string.donate_something_went_wrong), 1);

            Log.e("DonateActivity", Log.getStackTraceString(e));
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
            mTools.showToast(getString(R.string.donate_something_went_wrong), 1);

            Log.e("DonateActivity", Log.getStackTraceString(e));
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

                for(String purchaseData : purchaseDataArrayList)
                {
                    JSONObject purchaseDataJsonObject = new JSONObject(purchaseData);

                    consumeDonation(purchaseDataJsonObject.getString("purchaseToken"));
                }

                mTools.showToast(getString(R.string.donate_reset_successful), 0);
            }
            else
            {
                mTools.showToast(getString(R.string.donate_something_went_wrong), 1);
            }
        }
        catch(Exception e)
        {
            mTools.showToast(getString(R.string.donate_something_went_wrong), 1);

            Log.e("DonateActivity", Log.getStackTraceString(e));
        }
    }

    // Get products
    private class GetProductsTask extends AsyncTask<Void, Void, Bundle>
    {
        @Override
        protected void onPostExecute(Bundle skuDetails)
        {
            if(skuDetails == null)
            {
                mTools.showToast(getString(R.string.donate_something_went_wrong), 1);
            }
            else
            {
                try
                {
                    int responseCode = skuDetails.getInt("RESPONSE_CODE");

                    if(responseCode == 0)
                    {
                        Button makeSmallDonationButton = (Button) findViewById(R.id.donate_make_small_donation);
                        Button makeMediumDonationButton = (Button) findViewById(R.id.donate_make_medium_donation);
                        Button makeBigDonationButton = (Button) findViewById(R.id.donate_make_big_donation);

                        ArrayList<String> responseArrayList = skuDetails.getStringArrayList("DETAILS_LIST");

                        for(String details : responseArrayList)
                        {
                            JSONObject detailsJsonObject = new JSONObject(details);

                            String sku = detailsJsonObject.getString("productId");
                            String price = detailsJsonObject.getString("price");

                            switch(sku)
                            {
                                case "small_donation":
                                {
                                    makeSmallDonationButton.setText(getString(R.string.donate_donate)+" "+price);

                                    makeSmallDonationButton.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            makeDonation("small_donation");
                                        }
                                    });

                                    break;
                                }
                                case "medium_donation":
                                {
                                    makeMediumDonationButton.setText(getString(R.string.donate_donate)+" "+price);

                                    makeMediumDonationButton.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            makeDonation("medium_donation");
                                        }
                                    });

                                    break;
                                }
                                case "big_donation":
                                {
                                    makeBigDonationButton.setText(getString(R.string.donate_donate)+" "+price);

                                    makeBigDonationButton.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            makeDonation("big_donation");
                                        }
                                    });

                                    break;
                                }
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    mTools.showToast(getString(R.string.donate_something_went_wrong), 1);

                    Log.e("DonateActivity", Log.getStackTraceString(e));
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

            Bundle skuDetailsBundle;

            try
            {
                skuDetailsBundle = mIInAppBillingService.getSkuDetails(3, getPackageName(), "inapp", skusBundle);
            }
            catch(Exception e)
            {
                Log.e("DonateActivity", Log.getStackTraceString(e));

                skuDetailsBundle = null;
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