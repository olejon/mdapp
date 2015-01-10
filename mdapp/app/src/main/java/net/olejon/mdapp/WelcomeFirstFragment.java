package net.olejon.mdapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class WelcomeFirstFragment extends Fragment
{
    private boolean mViewIsShown = false;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_welcome_first, container, false);

        if(!mViewIsShown)
        {
            TextView textView = (TextView) viewGroup.findViewById(R.id.welcome_page_1_guide);
            animateTextView(textView);
        }

        return viewGroup;
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);

        if(getView() == null)
        {
            mViewIsShown = false;
        }
        else
        {
            mViewIsShown = true;

            TextView textView = (TextView) getView().getRootView().findViewById(R.id.welcome_page_1_guide);
            animateTextView(textView);
        }
    }

    private void animateTextView(TextView textView)
    {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.welcome_page_guide);

        textView.setVisibility(View.VISIBLE);
        textView.startAnimation(animation);
    }
}
