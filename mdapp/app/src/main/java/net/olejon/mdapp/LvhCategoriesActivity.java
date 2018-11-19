package net.olejon.mdapp;

/*

Copyright 2018 Ole Jon Bjørkum

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
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class LvhCategoriesActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Transition
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

		// Intent
		Intent intent = getIntent();

		String categoryColor = intent.getStringExtra("color");
		String categoryIcon = intent.getStringExtra("icon");
		String categoryTitle = intent.getStringExtra("title");

		JSONArray subCategories;

		try
		{
			subCategories = new JSONArray(intent.getStringExtra("subcategories"));
		}
		catch(Exception e)
		{
			subCategories = new JSONArray();

			Log.e("LvhCategoriesActivity", Log.getStackTraceString(e));
		}

		// Layout
		setContentView(R.layout.activity_lvh_categories);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.lvh_categories_toolbar);
		toolbar.setTitle(categoryTitle);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Recycler view
		RecyclerView recyclerView = findViewById(R.id.lvh_categories_cards);

		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

		if(mTools.isTablet())
		{
			int spanCount = (subCategories.length() == 1) ? 1 : 2;
			recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
		}

		// Get categories
		recyclerView.setAdapter(new LvhCategoriesAdapter(subCategories, categoryColor, categoryIcon));
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}

	// Menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
			{
				finish();
				overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	// Adapter
	class LvhCategoriesAdapter extends RecyclerView.Adapter<LvhCategoriesAdapter.LvhSubCategoriesViewHolder>
	{
		final LayoutInflater mLayoutInflater;

		final JSONArray mSubCategories;

		final String mColor;
		final String mIcon;

		int mLastPosition = - 1;

		LvhCategoriesAdapter(JSONArray jsonArray, String color, String icon)
		{
			mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			mSubCategories = jsonArray;

			mColor = color;
			mIcon = icon;
		}

		class LvhSubCategoriesViewHolder extends RecyclerView.ViewHolder
		{
			final CardView card;
			final ImageView icon;
			final TextView title;
			final LinearLayout categories;

			LvhSubCategoriesViewHolder(View view)
			{
				super(view);

				card = view.findViewById(R.id.lvh_categories_card);
				icon = view.findViewById(R.id.lvh_categories_card_icon);
				title = view.findViewById(R.id.lvh_categories_card_title);
				categories = view.findViewById(R.id.lvh_categories_card_categories);
			}
		}

		@NonNull
		@Override
		public LvhSubCategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
		{
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_lvh_categories_card, viewGroup, false);
			return new LvhSubCategoriesViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull LvhSubCategoriesViewHolder viewHolder, int i)
		{
			try
			{
				JSONObject subCategoryJsonObject = mSubCategories.getJSONObject(i);

				viewHolder.card.setCardBackgroundColor(Color.parseColor(mColor));

				switch(mIcon)
				{
					case "lvh_urgent":
					{
						viewHolder.icon.setImageResource(R.drawable.ic_favorite_white_24dp);
						break;
					}
					case "lvh_symptoms":
					{
						viewHolder.icon.setImageResource(R.drawable.stethoscope);
						break;
					}
					case "lvh_injuries":
					{
						viewHolder.icon.setImageResource(R.drawable.ic_healing_white_24dp);
						break;
					}
					case "lvh_administrative":
					{
						viewHolder.icon.setImageResource(R.drawable.ic_library_books_white_24dp);
						break;
					}
				}

				viewHolder.title.setText(subCategoryJsonObject.getString("title"));

				viewHolder.categories.removeAllViews();

				JSONArray categoryJsonArray = subCategoryJsonObject.getJSONArray("items");

				for(int f = 0; f < categoryJsonArray.length(); f++)
				{
					JSONObject categoryJsonObject = categoryJsonArray.getJSONObject(f);

					final String name = categoryJsonObject.getString("name");
					final String uri = categoryJsonObject.getString("uri");

					TextView textView = (TextView) mLayoutInflater.inflate(R.layout.activity_lvh_categories_card_categories_item, null);
					textView.setText(name);

					textView.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View view)
						{
							Intent intent = new Intent(mContext, MainWebViewActivity.class);
							intent.putExtra("title", name);
							intent.putExtra("uri", uri);
							mContext.startActivity(intent);
						}
					});

					viewHolder.categories.addView(textView);
				}

				animateCard(viewHolder.card, i);
			}
			catch(Exception e)
			{
				Log.e("LvhCategoriesAdapter", Log.getStackTraceString(e));
			}
		}

		@Override
		public int getItemCount()
		{
			return mSubCategories.length();
		}

		private void animateCard(View view, int position)
		{
			if(position > mLastPosition)
			{
				mLastPosition = position;

				view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.card));
			}
		}
	}
}