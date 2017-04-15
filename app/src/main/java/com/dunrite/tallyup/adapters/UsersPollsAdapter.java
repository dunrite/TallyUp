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

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.activities.MainActivity;
import com.dunrite.tallyup.pojo.Poll;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for the main activity to show user's current and previous polls
 */
public class UsersPollsAdapter extends RecyclerView.Adapter<UsersPollsAdapter.ViewHolder> {
    // Allows to remember the last item shown on screen
    private int lastPosition = -1;
    private MainActivity mActivity;
    private ArrayList<Poll> mPolls;
    private Poll poll;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.poll_card_view) CardView card;
        @BindView(R.id.poll_question) TextView question;
        @BindView(R.id.poll_description) TextView description;

        public ViewHolder(Context c, View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UsersPollsAdapter(ArrayList<Poll> polls, MainActivity activity) {
        mPolls = polls;
        mActivity = activity;
    }

    public Poll getPositionInfo(int position) {
        return mPolls.get(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UsersPollsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.poll_desc_card, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(parent.getContext(), v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        poll = mPolls.get(position);
        int voteCount = poll.getVoteCount();
        String descText;
        if (voteCount == 1)
            descText = poll.getVoteCount() + " person has voted. " + poll.getLeadingAnswers();
        else
            descText = poll.getVoteCount() + " people have voted. " + poll.getLeadingAnswers();

        holder.question.setText(poll.getQuestion());
        holder.description.setText(descText);
        setAnimation(holder.card, position);
    }

    @Override
    public int getItemCount() {
        return mPolls.size();
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