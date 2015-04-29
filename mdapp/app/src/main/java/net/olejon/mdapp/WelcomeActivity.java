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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private int mViewPagerPosition = 0;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Transition
        overridePendingTransition(R.anim.welcome_start, 0);

        // Layout
        setContentView(R.layout.activity_welcome);

        // View pager
        PagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        final ViewPager viewPager = (ViewPager) findViewById(R.id.welcome_pager);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setPageTransformer(true, new ViewPagerTransformer());

        ImageView imageView = (ImageView) findViewById(R.id.welcome_pager_indicator_page_1);
        imageView.setImageResource(R.drawable.welcome_indicator_active);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            private LinearLayout linearLayout;
            private ImageView imageView;
            private TextView textView;

            @Override
            public void onPageSelected(int position)
            {
                mViewPagerPosition = position;

                linearLayout = (LinearLayout) findViewById(R.id.welcome_pager_indicator_layout);

                for(int i = 0; i < linearLayout.getChildCount(); i++)
                {
                    imageView = (ImageView) linearLayout.getChildAt(i);
                    imageView.setImageResource(R.drawable.welcome_indicator_inactive);
                }

                imageView = (ImageView) linearLayout.getChildAt(position);
                imageView.setImageResource(R.drawable.welcome_indicator_active);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
                textView = (TextView) viewPager.getChildAt(0).findViewById(R.id.welcome_page_1_guide);
                textView.setVisibility(View.INVISIBLE);

                textView = (TextView) viewPager.getChildAt(1).findViewById(R.id.welcome_page_2_guide);
                textView.setVisibility(View.INVISIBLE);

                textView = (TextView) viewPager.getChildAt(2).findViewById(R.id.welcome_page_3_guide);
                textView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
        });
    }

    // Back button
    @Override
    public void onBackPressed()
    {
        if(mViewPagerPosition == 3)
        {
            mTools.showToast(getString(R.string.welcome_page_4_back_button), 1);
        }
        else
        {
            mTools.showToast(getString(R.string.welcome_page_guide), 1);
        }
    }

    // View pager
    private class ViewPagerAdapter extends FragmentStatePagerAdapter
    {
        private final String[] pages = getResources().getStringArray(R.array.welcome_pages);

        public ViewPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position)
        {
            if(position == 1)
            {
                return new WelcomeSecondFragment();
            }
            else if(position == 2)
            {
                return new WelcomeThirdFragment();
            }
            else if(position == 3)
            {
                return new WelcomeFourthFragment();
            }

            return new WelcomeFirstFragment();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return pages[position];
        }

        @Override
        public int getCount()
        {
            return pages.length;
        }
    }

    private class ViewPagerTransformer implements ViewPager.PageTransformer
    {
        private final float minimumScale = 0.75f;

        public void transformPage(View view, float position)
        {
            int pageWidth = view.getWidth();

            if(position < -1)
            {
                view.setAlpha(0);
            }
            else if(position <= 0)
            {
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            }
            else if(position <= 1)
            {
                view.setAlpha(1 - position);
                view.setTranslationX(pageWidth * -position);

                float scaleFactor = minimumScale + (1 - minimumScale) * (1 - Math.abs(position));

                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            }
            else
            {
                view.setAlpha(0);
            }
        }
    }
}