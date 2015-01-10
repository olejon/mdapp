package net.olejon.mdapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;

import com.squareup.picasso.Picasso;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MedicationPictureActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Intent
        Intent intent = getIntent();
        final String uri = intent.getStringExtra("uri");

        // Layout
        setContentView(R.layout.activity_medication_picture);

        // Picture
        ImageViewTouch imageViewTouch = (ImageViewTouch) findViewById(R.id.medication_picture_picture);
        imageViewTouch.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        Picasso.with(mContext).load(uri).into(imageViewTouch);

        // Toast
        mTools.showToast("Bruk fingrene for å zoome", 1);
    }
}
