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
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder>
{
    private final Context mContext;

    private final Cursor mCursor;

    private int mLastPosition = -1;

    public NotesAdapter(Context context, Cursor cursor)
    {
        mContext = context;

        mCursor = cursor;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final TextView title;
        private final TextView text;

        public NoteViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.notes_card);
            title = (TextView) view.findViewById(R.id.notes_card_title);
            text = (TextView) view.findViewById(R.id.notes_card_text);
        }
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_notes_card, viewGroup, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder viewHolder, int i)
    {
        if(mCursor.moveToPosition(i))
        {
            final int id = mCursor.getInt(mCursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_ID));

            String title = mCursor.getString(mCursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_TITLE));
            String text = mCursor.getString(mCursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_TEXT));

            viewHolder.title.setText(title);
            viewHolder.text.setText(text);

            viewHolder.card.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(mContext, NotesEditActivity.class);
                    intent.putExtra("id", id);
                    mContext.startActivity(intent);
                }
            });

            animateView(viewHolder.card, i);
        }
    }

    @Override
    public int getItemCount()
    {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    private void animateView(View view, int position)
    {
        if(position > mLastPosition)
        {
            mLastPosition = position;

            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.card);
            view.startAnimation(animation);
        }
    }
}
