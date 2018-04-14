package net.olejon.mdapp;

/*

Copyright 2017 Ole Jon Bjørkum

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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class CalculatorsActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private TextInputLayout mBmiWeightInputLayout;
	private TextInputLayout mBmiHeightInputLayout;
	private TextInputLayout mWaistMeasurementInputLayout;
	private TextInputLayout mQtIntervalEditTextLayout;
	private TextInputLayout mRrIntervalEditTextLayout;

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
		Toolbar toolbar = findViewById(R.id.calculators_toolbar);
		toolbar.setTitle(getString(R.string.calculators_title));

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// BMI
		mBmiWeightInputLayout = findViewById(R.id.calculators_bmi_weight_layout);
		mBmiHeightInputLayout = findViewById(R.id.calculators_bmi_height_layout);
		mBmiWeightInputLayout.setHintAnimationEnabled(true);
		mBmiHeightInputLayout.setHintAnimationEnabled(true);

		final EditText bmiEditText = findViewById(R.id.calculators_bmi_height);
		Button bmiButton = findViewById(R.id.calculators_bmi_button);

		bmiButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(bmiEditText.getWindowToken(), 0);

				calculateBmi();
			}
		});

		bmiEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				if(i == EditorInfo.IME_ACTION_GO || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					if(inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(bmiEditText.getWindowToken(), 0);

					calculateBmi();

					return true;
				}

				return false;
			}
		});

		// Waist measurement
		mWaistMeasurementInputLayout = findViewById(R.id.calculators_waist_measurement_layout);
		mWaistMeasurementInputLayout.setHintAnimationEnabled(true);

		final EditText waistMeasurementEditText = findViewById(R.id.calculators_waist_measurement);
		Button waistMeasurementButton = findViewById(R.id.calculators_waist_measurement_button);

		waistMeasurementButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(waistMeasurementEditText.getWindowToken(), 0);

				calculateWaistMeasurement();
			}
		});

		waistMeasurementEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				if(i == EditorInfo.IME_ACTION_GO || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					if(inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(waistMeasurementEditText.getWindowToken(), 0);

					calculateWaistMeasurement();

					return true;
				}

				return false;
			}
		});

		// Corrected QT time
		mQtIntervalEditTextLayout = findViewById(R.id.calculators_corrected_qt_time_qt_interval_layout);
		mRrIntervalEditTextLayout = findViewById(R.id.calculators_corrected_qt_time_rr_interval_layout);
		mQtIntervalEditTextLayout.setHintAnimationEnabled(true);
		mRrIntervalEditTextLayout.setHintAnimationEnabled(true);

		final EditText correctedQtTimeEditText = findViewById(R.id.calculators_corrected_qt_time_rr_interval);
		Button correctedQtTimeButton = findViewById(R.id.calculators_corrected_qt_time_button);

		correctedQtTimeButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(correctedQtTimeEditText.getWindowToken(), 0);

				calculateCorrectedQtTime();
			}
		});

		correctedQtTimeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				if(i == EditorInfo.IME_ACTION_GO || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					if(inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(correctedQtTimeEditText.getWindowToken(), 0);

					calculateCorrectedQtTime();

					return true;
				}

				return false;
			}
		});
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
		new MaterialDialog.Builder(mContext).title(R.string.calculators_information_dialog_title).content(getString(R.string.calculators_information_dialog_message)).positiveText(R.string.calculators_information_dialog_positive_button).neutralText(R.string.calculators_information_dialog_neutral_button).onNeutral(new MaterialDialog.SingleButtonCallback()
		{
			@Override
			public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
			{
				Intent intent = new Intent(mContext, MainWebViewActivity.class);
				intent.putExtra("title", getString(R.string.calculators_information_dialog_title));
				intent.putExtra("uri", "https://helsenorge.no/kosthold-og-ernaring/overvekt/vekt-bmi-og-maling-av-midjen");
				startActivity(intent);
			}
		}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.dark_blue).show();
	}

	// Calculations
	private void calculateBmi()
	{
		EditText weightEditText = findViewById(R.id.calculators_bmi_weight);
		EditText heightEditText = findViewById(R.id.calculators_bmi_height);

		weightEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
				mBmiWeightInputLayout.setError(null);
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

			@Override
			public void afterTextChanged(Editable editable) { }
		});

		heightEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
				mBmiHeightInputLayout.setError(null);
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

			@Override
			public void afterTextChanged(Editable editable) { }
		});

		String weightEditTextValue = weightEditText.getText().toString();
		String heightEditTextValue = heightEditText.getText().toString();

		if(weightEditTextValue.equals(""))
		{
			mBmiWeightInputLayout.setError(getString(R.string.calculators_bmi_invalid_values));
		}
		else if(heightEditTextValue.equals(""))
		{
			mBmiHeightInputLayout.setError(getString(R.string.calculators_bmi_invalid_values));
		}
		else
		{
			try
			{
				double weight = Double.parseDouble(weightEditTextValue);
				double height = Double.parseDouble(heightEditTextValue);

				double result = (weight) / ((height / 100) * (height / 100));

				String interpretation = getString(R.string.calculators_bmi_normal_weight);

				if(result < 18.5)
				{
					interpretation = getString(R.string.calculators_bmi_under_weight);
				}
				else if(result >= 25 && result < 30)
				{
					interpretation = getString(R.string.calculators_bmi_over_weight);
				}
				else if(result >= 30 && result < 35)
				{
					interpretation = getString(R.string.calculators_bmi_obesity_1_weight);
				}
				else if(result >= 35 && result < 40)
				{
					interpretation = getString(R.string.calculators_bmi_obesity_2_weight);
				}
				else if(result >= 40)
				{
					interpretation = getString(R.string.calculators_bmi_obesity_3_weight);
				}

				new MaterialDialog.Builder(mContext).title(R.string.calculators_bmi_dialog_title).content(getString(R.string.calculators_bmi_dialog_message, result, interpretation)).positiveText(R.string.calculators_bmi_dialog_positive_button).neutralText(R.string.calculators_bmi_dialog_neutral_button).onNeutral(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
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
	}

	private void calculateWaistMeasurement()
	{
		EditText waistMeasurementEditText = findViewById(R.id.calculators_waist_measurement);

		waistMeasurementEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
				mWaistMeasurementInputLayout.setError(null);
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

			@Override
			public void afterTextChanged(Editable editable) { }
		});

		String waistMeasurementEditTextValue = waistMeasurementEditText.getText().toString();

		if(waistMeasurementEditTextValue.equals(""))
		{
			mWaistMeasurementInputLayout.setError(getString(R.string.calculators_waist_measurement_invalid_value));
		}
		else
		{
			try
			{
				int waistMeasurement = Integer.parseInt(waistMeasurementEditTextValue);

				String interpretation = getString(R.string.calculators_waist_measurement_men_normal_weight)+getString(R.string.calculators_waist_measurement_women_normal_weight);

				if(waistMeasurement > 80 && waistMeasurement <= 88)
				{
					interpretation = getString(R.string.calculators_waist_measurement_men_normal_weight)+getString(R.string.calculators_waist_measurement_women_over_weight);
				}
				else if(waistMeasurement > 88 && waistMeasurement <= 94)
				{
					interpretation = getString(R.string.calculators_waist_measurement_men_normal_weight)+getString(R.string.calculators_waist_measurement_women_obesity_weight);
				}
				else if(waistMeasurement > 94 && waistMeasurement <= 102)
				{
					interpretation = getString(R.string.calculators_waist_measurement_men_over_weight)+getString(R.string.calculators_waist_measurement_women_obesity_weight);
				}
				else if(waistMeasurement > 102)
				{
					interpretation = getString(R.string.calculators_waist_measurement_men_obesity_weight)+getString(R.string.calculators_waist_measurement_women_obesity_weight);
				}

				new MaterialDialog.Builder(mContext).title(R.string.calculators_waist_measurement_dialog_title).content(getString(R.string.calculators_waist_measurement_dialog_message, waistMeasurementEditTextValue, interpretation)).positiveText(R.string.calculators_waist_measurement_dialog_positive_button).neutralText(R.string.calculators_waist_measurement_dialog_neutral_button).onNeutral(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
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

	private void calculateCorrectedQtTime()
	{
		EditText qtIntervalEditText = findViewById(R.id.calculators_corrected_qt_time_qt_interval);
		EditText rrIntervalEditText = findViewById(R.id.calculators_corrected_qt_time_rr_interval);

		qtIntervalEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
				mQtIntervalEditTextLayout.setError(null);
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

			@Override
			public void afterTextChanged(Editable editable) { }
		});

		rrIntervalEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
				mRrIntervalEditTextLayout.setError(null);
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

			@Override
			public void afterTextChanged(Editable editable) { }
		});

		String qtIntervalEditTextValue = qtIntervalEditText.getText().toString();
		String rrIntervalEditTextValue = rrIntervalEditText.getText().toString();

		if(qtIntervalEditTextValue.equals(""))
		{
			mQtIntervalEditTextLayout.setError(getString(R.string.calculators_corrected_qt_time_invalid_values));
		}
		else if(rrIntervalEditTextValue.equals(""))
		{
			mRrIntervalEditTextLayout.setError(getString(R.string.calculators_corrected_qt_time_invalid_values));
		}
		else
		{
			try
			{
				double qtInterval = Double.parseDouble(qtIntervalEditTextValue) * 0.001;
				double rrInterval = Double.parseDouble(rrIntervalEditTextValue) * 0.001;

				int result = (int) Math.round(qtInterval / Math.sqrt(rrInterval) * 1000);

				new MaterialDialog.Builder(mContext).title(R.string.calculators_corrected_qt_time_dialog_title).content(getString(R.string.calculators_corrected_qt_time_dialog_message, result)).positiveText(R.string.calculators_corrected_qt_time_dialog_positive_button).neutralText(R.string.calculators_corrected_qt_time_dialog_neutral_button).onNeutral(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.calculators_corrected_qt_time_dialog_title));
						intent.putExtra("uri", "http://tidsskriftet.no/2000/11/merkesteiner-i-norsk-medisin/lang-qt-tid-som-bivirkning-risiko-fatale-arytmier");
						startActivity(intent);
					}
				}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.dark_blue).show();
			}
			catch(Exception e)
			{
				mTools.showToast(getString(R.string.calculators_corrected_qt_time_invalid_values), 1);

				Log.e("CalculatorsActivity", Log.getStackTraceString(e));
			}
		}
	}
}