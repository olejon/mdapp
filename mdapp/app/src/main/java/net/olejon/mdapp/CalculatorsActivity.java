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
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class CalculatorsActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Input manager
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Layout
        setContentView(R.layout.activity_calculators);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.calculators_toolbar);
        toolbar.setTitle(getString(R.string.calculators_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // BMI
        final TextView bmiButton = (TextView) findViewById(R.id.calculators_bmi_button);

        bmiButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                calculateBmi();
            }
        });

        final EditText bmiEditText = (EditText) findViewById(R.id.calculators_bmi_height);

        bmiEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_GO || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    inputMethodManager.toggleSoftInputFromWindow(bmiEditText.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    calculateBmi();

                    return true;
                }

                return false;
            }
        });

        // Waist measurement
        final TextView waistMeasurementButton = (TextView) findViewById(R.id.calculators_waist_measurement_button);

        waistMeasurementButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                calculateWaistMeasurement();
            }
        });

        final EditText waistMeasurementEditText = (EditText) findViewById(R.id.calculators_waist_measurement);

        waistMeasurementEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_GO || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    inputMethodManager.toggleSoftInputFromWindow(waistMeasurementEditText.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    calculateWaistMeasurement();

                    return true;
                }

                return false;
            }
        });

        // Information dialog
        boolean hideInformationDialog = mTools.getSharedPreferencesBoolean("CALCULATORS_HIDE_INFORMATION_DIALOG");

        if(!hideInformationDialog) showInformationDialog();
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_calculators, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.calculators_menu_information:
            {
                showInformationDialog();
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Information dialog
    private void showInformationDialog()
    {
        new MaterialDialog.Builder(mContext).title(getString(R.string.calculators_information_dialog_title)).content(Html.fromHtml(getString(R.string.calculators_information_dialog_message))).positiveText(getString(R.string.calculators_information_dialog_positive_button)).neutralText(getString(R.string.calculators_information_dialog_neutral_button)).callback(new MaterialDialog.ButtonCallback()
        {
            @Override
            public void onPositive(MaterialDialog dialog)
            {
                mTools.setSharedPreferencesBoolean("CALCULATORS_HIDE_INFORMATION_DIALOG", true);
            }

            @Override
            public void onNeutral(MaterialDialog dialog)
            {
                mTools.setSharedPreferencesBoolean("CALCULATORS_HIDE_INFORMATION_DIALOG", true);

                mTools.openUri("https://helsenorge.no/kosthold-og-ernaring/overvekt/vekt-bmi-og-maling-av-midjen");
            }
        }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.dark_blue).show();
    }

    // Calculations
    private void calculateBmi()
    {
        EditText weightEditText = (EditText) findViewById(R.id.calculators_bmi_weight);
        EditText heightEditText = (EditText) findViewById(R.id.calculators_bmi_height);

        try
        {
            float weight = Float.parseFloat(weightEditText.getText().toString());
            float height = Float.parseFloat(heightEditText.getText().toString());

            float result = (weight) / ((height / 100) * (height / 100));

            String interpretation = "<font color=\"#4caf50\">"+getString(R.string.calculators_bmi_normal_weight)+"</font>";

            if(result < 18)
            {
                interpretation = "<font color=\"#ff9800\">"+getString(R.string.calculators_bmi_under_weight)+"</font>";
            }
            else if(result >= 25 && result < 30)
            {
                interpretation = "<font color=\"#ff9800\">"+getString(R.string.calculators_bmi_over_weight)+"</font>";
            }
            else if(result >= 30 && result < 35)
            {
                interpretation = "<font color=\"#f44336\">"+getString(R.string.calculators_bmi_obesity_1_weight)+"</font>";
            }
            else if(result >= 35 && result < 40)
            {
                interpretation = "<font color=\"#f44336\">"+getString(R.string.calculators_bmi_obesity_2_weight)+"</font>";
            }
            else if(result >= 40)
            {
                interpretation = "<font color=\"#f44336\">"+getString(R.string.calculators_bmi_obesity_3_weight)+"</font>";
            }

            String bmi = String.format("%.1f", result);

            new MaterialDialog.Builder(mContext).title(getString(R.string.calculators_bmi_dialog_title)).content(Html.fromHtml(getString(R.string.calculators_bmi_dialog_message_first)+"<br><b>"+bmi+"</b><br><br>"+getString(R.string.calculators_bmi_dialog_message_second)+"<br><b>"+interpretation+"</b><br><br><small><i>"+getString(R.string.calculators_bmi_dialog_message_third)+"</i></small>")).positiveText(getString(R.string.calculators_bmi_dialog_positive_button)).neutralText(getString(R.string.calculators_bmi_dialog_neutral_button)).callback(new MaterialDialog.ButtonCallback()
            {
                @Override
                public void onNeutral(MaterialDialog dialog)
                {
                    showInformationDialog();
                }
            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.dark_blue).show();
        }
        catch(Exception e)
        {
            mTools.showToast(getString(R.string.calculators_bmi_invalid_values), 1);

            Log.e("CalculatorsActivity", Log.getStackTraceString(e));
        }
    }

    private void calculateWaistMeasurement()
    {
        EditText weightEditText = (EditText) findViewById(R.id.calculators_waist_measurement);

        try
        {
            String value = weightEditText.getText().toString();

            int waistMeasurement = Integer.parseInt(value);

            String interpretation = "<font color=\"#4caf50\"><b>"+getString(R.string.calculators_waist_measurement_men)+": "+getString(R.string.calculators_waist_measurement_normal_weight)+"</b></font><br><font color=\"#4caf50\"><b>"+getString(R.string.calculators_waist_measurement_women)+": "+getString(R.string.calculators_waist_measurement_normal_weight)+"</b></font>";

            if(waistMeasurement > 80 && waistMeasurement <= 88)
            {
                interpretation = "<font color=\"#4caf50\"><b>"+getString(R.string.calculators_waist_measurement_men)+": "+getString(R.string.calculators_waist_measurement_normal_weight)+"</b></font><br><font color=\"#ff9800\"><b>"+getString(R.string.calculators_waist_measurement_women)+": "+getString(R.string.calculators_waist_measurement_over_weight)+"</b></font>";
            }
            else if(waistMeasurement > 88 && waistMeasurement <= 94)
            {
                interpretation = "<font color=\"#4caf50\"><b>"+getString(R.string.calculators_waist_measurement_men)+": "+getString(R.string.calculators_waist_measurement_normal_weight)+"</b></font><br><font color=\"#f44336\"><b>"+getString(R.string.calculators_waist_measurement_women)+": "+getString(R.string.calculators_waist_measurement_obesity_weight)+"</b></font>";
            }
            else if(waistMeasurement > 94 && waistMeasurement <= 102)
            {
                interpretation = "<font color=\"#ff9800\"><b>"+getString(R.string.calculators_waist_measurement_men)+": "+getString(R.string.calculators_waist_measurement_over_weight)+"</b></font><br><font color=\"#f44336\"><b>"+getString(R.string.calculators_waist_measurement_women)+": "+getString(R.string.calculators_waist_measurement_obesity_weight)+"</b></font>";
            }
            else if(waistMeasurement > 102)
            {
                interpretation = "<font color=\"#f44336\"><b>"+getString(R.string.calculators_waist_measurement_men)+": "+getString(R.string.calculators_waist_measurement_obesity_weight)+"</b></font><br><font color=\"#f44336\"><b>"+getString(R.string.calculators_waist_measurement_women)+": "+getString(R.string.calculators_waist_measurement_obesity_weight)+"</b></font>";
            }

            new MaterialDialog.Builder(mContext).title(getString(R.string.calculators_waist_measurement_dialog_title)).content(Html.fromHtml(getString(R.string.calculators_waist_measurement_dialog_message_first)+"<br><b>"+value+" cm</b><br><br>"+getString(R.string.calculators_waist_measurement_dialog_message_second)+"<br>"+interpretation+"<br><br><small><i>"+getString(R.string.calculators_waist_measurement_dialog_message_third)+"</i></small>")).positiveText(getString(R.string.calculators_waist_measurement_dialog_positive_button)).neutralText(getString(R.string.calculators_waist_measurement_dialog_neutral_button)).callback(new MaterialDialog.ButtonCallback()
            {
                @Override
                public void onNeutral(MaterialDialog dialog)
                {
                    showInformationDialog();
                }
            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.dark_blue).show();
        }
        catch(Exception e)
        {
            mTools.showToast(getString(R.string.calculators_waist_measurement_invalid_value), 1);

            Log.e("CalculatorsActivity", Log.getStackTraceString(e));
        }
    }
}
