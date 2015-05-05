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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class DrawerFrameLayout extends FrameLayout
{
    private Drawable mDrawable;

    private final Rect mTempRect = new Rect();

    private Rect mRect;

    public DrawerFrameLayout(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DrawerFrameLayoutView, 0, 0);

        if(typedArray == null) return;

        mDrawable = typedArray.getDrawable(R.styleable.DrawerFrameLayoutView_drawer_frame_inset_color);

        typedArray.recycle();

        setWillNotDraw(true);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean fitSystemWindows(@NonNull Rect rect)
    {
        mRect = rect;
        return true;
    }

    @Override
    public void draw(@NonNull Canvas canvas)
    {
        super.draw(canvas);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mDrawable != null)
        {
            int saveCount = canvas.save();

            canvas.translate(getScrollX(), getScrollY());

            mTempRect.set(0, 0, getWidth(), mRect.top);
            mDrawable.setBounds(mTempRect);
            mDrawable.draw(canvas);

            canvas.restoreToCount(saveCount);
        }
    }
}