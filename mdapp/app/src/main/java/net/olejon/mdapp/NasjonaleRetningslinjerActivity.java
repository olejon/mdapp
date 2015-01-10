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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class NasjonaleRetningslinjerActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private InputMethodManager mInputMethodManager;

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout mToolbarSearchLayout;
    private EditText mToolbarSearchEditText;
    private FloatingActionButton mFloatingActionButton;
    private ListView mListView;

    private JSONArray mResponse;

    private ArrayList<String> mTitlesArrayList;
    private ArrayList<String> mUrisArrayList;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected()) mTools.showToast(getString(R.string.device_not_connected), 1);

        // Layout
        setContentView(R.layout.activity_nasjonale_retningslinjer);

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.nasjonale_retningslinjer_toolbar);
        toolbar.setTitle(getString(R.string.nasjonale_retningslinjer_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.nasjonale_retningslinjer_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.nasjonale_retningslinjer_toolbar_search);

        mToolbarSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    return true;
                }

                return false;
            }
        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.nasjonale_retningslinjer_toolbar_clear_search);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchEditText.setText("");
            }
        });

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.nasjonale_retningslinjer_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.nasjonale_retningslinjer_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_green, R.color.accent_purple, R.color.accent_orange);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
                {
                    mToolbarSearchLayout.setVisibility(View.GONE);
                    mToolbarSearchEditText.setText("");
                }

                getData(false);
            }
        });

        // Floating action button
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.nasjonale_retningslinjer_fab);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchLayout.setVisibility(View.VISIBLE);
                mToolbarSearchEditText.requestFocus();

                mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });

        // List
        mListView = (ListView) findViewById(R.id.nasjonale_retningslinjer_list);

        // Get data
        getData(true);
    }

    // Back button
    @Override
    public void onBackPressed()
    {
        if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
        {
            mToolbarSearchLayout.setVisibility(View.GONE);
            mToolbarSearchEditText.setText("");
        }
        else
        {
            super.onBackPressed();
        }
    }

    // Search button
    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_SEARCH)
        {
            mToolbarSearchLayout.setVisibility(View.VISIBLE);
            mToolbarSearchEditText.requestFocus();

            mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_nasjonale_retningslinjer, menu);
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
            case R.id.nasjonale_retningslinjer_uri:
            {
                mTools.openUri("http://helsedirektoratet.no/publikasjoner/Sider/default.aspx?Kategori=Nasjonale+faglige+retningslinjer");
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get data
    private void getData(boolean cache)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        String apiUri = getString(R.string.project_website)+"api/1/nasjonale-retningslinjer/";

        if(!cache) requestQueue.getCache().remove(apiUri);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(apiUri, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                mResponse = response;

                mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
                    {
                        populateListView(charSequence.toString().trim());
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });

                populateListView(null);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
                    {
                        new MaterialDialog.Builder(mContext).title(mTitlesArrayList.get(i)).items(R.array.nasjonale_retningslinjer_list_item).itemsCallback(new MaterialDialog.ListCallback()
                        {
                            @Override
                            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence)
                            {
                                if(i == 0)
                                {
                                    Intent intent = new Intent(mContext, NasjonaleRetningslinjerWebViewActivity.class);
                                    intent.putExtra("title", mTitlesArrayList.get(i));
                                    intent.putExtra("uri", mUrisArrayList.get(i));
                                    startActivity(intent);
                                }
                                else if(i == 1)
                                {
                                    mTools.showToast(getString(R.string.nasjonale_retningslinjer_downloading_pdf), 1);

                                    downloadPdf(mTitlesArrayList.get(i), mUrisArrayList.get(i));
                                }
                            }
                        }).show();
                    }
                });

                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);
                mFloatingActionButton.startAnimation(animation);

                mFloatingActionButton.setVisibility(View.VISIBLE);

                boolean hideNasjonaleRetningslinjerTipDialog = mTools.getSharedPreferencesBoolean("HIDE_NASJONALE_RETNINGSLINJER_TIP_DIALOG");

                if(!hideNasjonaleRetningslinjerTipDialog)
                {
                    new MaterialDialog.Builder(mContext).title(getString(R.string.nasjonale_retningslinjer_tip_dialog_title)).content(getString(R.string.nasjonale_retningslinjer_tip_dialog_message)).positiveText(getString(R.string.nasjonale_retningslinjer_tip_dialog_positive_button)).callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onPositive(MaterialDialog dialog)
                        {
                            mTools.setSharedPreferencesBoolean("HIDE_NASJONALE_RETNINGSLINJER_TIP_DIALOG", true);
                        }
                    }).show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                mTools.showToast(getString(R.string.nasjonale_retningslinjer_could_not_get_data), 1);

                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                Log.e("NasjonaleRetningslinjerActivity", error.toString());

                finish();
            }
        });

        requestQueue.add(jsonArrayRequest);
    }

    // Download PDF
    private void downloadPdf(final String title, String uri)
    {
        try
        {
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website)+"api/1/nasjonale-retningslinjer/pdf/?uri="+URLEncoder.encode(uri, "utf-8"), null, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    try
                    {
                        String uri = response.getString("uri");

                        if(uri.equals(""))
                        {
                            mTools.showToast(getString(R.string.nasjonale_retningslinjer_could_not_get_pdf), 1);
                        }
                        else
                        {
                            mTools.downloadFile(title, uri);
                        }
                    }
                    catch(Exception e)
                    {
                        mTools.showToast(getString(R.string.nasjonale_retningslinjer_could_not_get_pdf), 1);

                        Log.e("NasjonaleRetningslinjerActivity", Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    mTools.showToast(getString(R.string.nasjonale_retningslinjer_could_not_get_pdf), 1);

                    Log.e("NasjonaleRetningslinjerActivity", error.toString());
                }
            });

            requestQueue.add(jsonObjectRequest);
        }
        catch(Exception e)
        {
            Log.e("NasjonaleRetningslinjerActivity", Log.getStackTraceString(e));
        }
    }

    // Populate list view
    private void populateListView(String searchString)
    {
        final ArrayList<HashMap<String, String>> itemsArrayList = new ArrayList<>();

        mTitlesArrayList = new ArrayList<>();
        mUrisArrayList = new ArrayList<>();

        String[] fromColumns = new String[] {"title", "date"};
        int[] toViews = new int[] {R.id.nasjonale_retningslinjer_list_item_title, R.id.nasjonale_retningslinjer_list_item_date};

        try
        {
            for(int i = 0; i < mResponse.length(); i++)
            {
                HashMap<String, String> item = new HashMap<>();

                JSONObject itemJsonObject = mResponse.getJSONObject(i);

                String title = itemJsonObject.getString("title");
                String date = itemJsonObject.getString("date");
                String uri = itemJsonObject.getString("uri");

                if(searchString == null)
                {
                    item.put("title", title);
                    item.put("date", date);

                    itemsArrayList.add(item);

                    mTitlesArrayList.add(title);
                    mUrisArrayList.add(uri);
                }
                else if(title.matches("(?i).*?"+searchString+".*"))
                {
                    item.put("title", title);
                    item.put("date", date);

                    itemsArrayList.add(item);

                    mTitlesArrayList.add(title);
                    mUrisArrayList.add(uri);
                }
            }
        }
        catch(Exception e)
        {
            Log.e("NasjonaleRetningslinjerActivity", Log.getStackTraceString(e));
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, itemsArrayList, R.layout.activity_nasjonale_retningslinjer_list_item, fromColumns, toViews);

        mListView.setAdapter(simpleAdapter);

        View listViewEmpty = findViewById(R.id.nasjonale_retningslinjer_list_empty);
        mListView.setEmptyView(listViewEmpty);
    }
}
