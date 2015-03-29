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
import android.os.Handler;
import android.speech.RecognizerIntent;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class Icd10ChapterActivity extends ActionBarActivity
{
    private static final int VOICE_SEARCH_REQUEST_CODE = 1;

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

    private ArrayList<String> mCodesArrayList;

    private boolean mActivityPaused = false;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected()) mTools.showToast(getString(R.string.device_not_connected), 1);

        // Intent
        final Intent intent = getIntent();

        final String title = intent.getStringExtra("title");
        final int chapter = intent.getIntExtra("chapter", 0);

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Layout
        setContentView(R.layout.activity_icd10_chapter);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.icd10_chapter_toolbar);
        toolbar.setTitle(title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.icd10_chapter_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.icd10_chapter_toolbar_search);

        ImageButton imageButton = (ImageButton) findViewById(R.id.icd10_chapter_toolbar_clear_search);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchEditText.setText("");
            }
        });

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.icd10_chapter_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.icd10_chapter_swipe_refresh_layout);
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

                getData(chapter, false);
            }
        });

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.icd10_chapter_fab);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchLayout.setVisibility(View.VISIBLE);
                mToolbarSearchEditText.requestFocus();

                mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
            }
        });

        // List
        mListView = (ListView) findViewById(R.id.icd10_chapter_list);

        // Get data
        getData(chapter, true);
    }

    // Activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == VOICE_SEARCH_REQUEST_CODE && data != null)
        {
            ArrayList<String> voiceSearchArrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String voiceSearchString = voiceSearchArrayList.get(0);

            populateListView(voiceSearchString);
        }
    }

    // Pause activity
    @Override
    protected void onPause()
    {
        super.onPause();

        mActivityPaused = true;
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

            mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_icd10_chapter, menu);
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
            case R.id.icd10_chapter_menu_voice_search:
            {
                try
                {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nb-NO");
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
                }
                catch(Exception e)
                {
                    Log.e("Icd10ChapterActivity", Log.getStackTraceString(e));
                }

                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Populate list view
    private void populateListView(String searchString)
    {
        final ArrayList<HashMap<String, String>> itemsArrayList = new ArrayList<>();

        mCodesArrayList = new ArrayList<>();

        String[] fromColumns = new String[] {"code", "name"};
        int[] toViews = new int[] {R.id.icd10_chapter_list_item_code, R.id.icd10_chapter_list_item_name};

        try
        {
            for(int i = 0; i < mResponse.length(); i++)
            {
                HashMap<String, String> item = new HashMap<>();

                JSONObject itemJsonObject = mResponse.getJSONObject(i);

                String code = itemJsonObject.getString("code");
                String name = itemJsonObject.getString("name");

                if(searchString == null)
                {
                    item.put("code", code);
                    item.put("name", name);

                    itemsArrayList.add(item);

                    mCodesArrayList.add(code);
                }
                else if(name.matches("(?i).*?"+searchString+".*") || code.matches("(?i).*?"+searchString+".*"))
                {
                    item.put("code", code);
                    item.put("name", name);

                    itemsArrayList.add(item);

                    mCodesArrayList.add(code);
                }
            }
        }
        catch(Exception e)
        {
            Log.e("Icd10ChapterActivity", Log.getStackTraceString(e));
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, itemsArrayList, R.layout.activity_icd10_chapter_list_item, fromColumns, toViews);

        mListView.setAdapter(simpleAdapter);

        View listViewEmpty = findViewById(R.id.icd10_chapter_list_empty);
        mListView.setEmptyView(listViewEmpty);
    }

    // Get data
    private void getData(int chapter, boolean cache)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        String apiUri = getString(R.string.project_website_uri)+"api/1/icd-10/chapter/?chapter="+chapter;

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

                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);

                mFloatingActionButton.startAnimation(animation);
                mFloatingActionButton.setVisibility(View.VISIBLE);

                populateListView(null);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                    {
                        try
                        {
                            Intent intent = new Intent(mContext, Icd10WebViewActivity.class);
                            intent.putExtra("title", mCodesArrayList.get(i));
                            intent.putExtra("uri", "http://www.icd10data.com/Search.aspx?search="+URLEncoder.encode(mCodesArrayList.get(i), "utf-8"));
                            startActivity(intent);
                        }
                        catch(Exception e)
                        {
                            Log.e("Icd10ChapterActivity", Log.getStackTraceString(e));
                        }
                    }
                });

                if(!mActivityPaused)
                {
                    Handler handler = new Handler();

                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mToolbarSearchLayout.setVisibility(View.VISIBLE);
                            mToolbarSearchEditText.requestFocus();

                            mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
                        }
                    }, 500);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                Log.e("Icd10ChapterActivity", error.toString());
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
    }
}
