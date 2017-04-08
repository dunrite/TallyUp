package com.dunrite.tallyup.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.dunrite.tallyup.PollItem;
import com.dunrite.tallyup.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for poll choices
 */

public class PollChoiceAdapter extends RecyclerView.Adapter<PollChoiceAdapter.ViewHolder> {
    // Allows to remember the last item shown on screen
    private int lastPosition = -1;
    private ArrayList<PollItem> choices;
    private PollItem pollItem;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.choice_card_view)
        CardView card;
        @BindView(R.id.choice_name)
        TextView  choiceName;

        public ViewHolder(Context c, View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PollChoiceAdapter(ArrayList<PollItem> pc) {
        choices = pc;
    }

    public PollItem getPositionInfo(int position) {
        return choices.get(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PollChoiceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.poll_choice_card, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new PollChoiceAdapter.ViewHolder(parent.getContext(), v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PollChoiceAdapter.ViewHolder holder, int position) {
        pollItem = choices.get(position);

        holder.choiceName.setText(pollItem.getName());
        setAnimation(holder.card, position);
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
