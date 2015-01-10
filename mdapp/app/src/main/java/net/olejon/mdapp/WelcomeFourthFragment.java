package net.olejon.mdapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WelcomeFourthFragment extends Fragment
{
    private Activity mActivity;

    private MyTools mTools;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mActivity = getActivity();

        mTools = new MyTools(mActivity);

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_welcome_fourth, container, false);

        TextView textView = (TextView) viewGroup.findViewById(R.id.welcome_page_button);

        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mTools.setSharedPreferencesBoolean("WELCOME_ACTIVITY_HAS_BEEN_SHOWN", true);

                mActivity.finish();
            }
        });

        return viewGroup;
    }
}