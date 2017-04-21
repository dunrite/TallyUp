package com.dunrite.tallyup.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dunrite.tallyup.R;
import com.dunrite.tallyup.activities.PollActivity;
import com.dunrite.tallyup.pojo.PollItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for poll choices
 */

public class PollChoiceAdapter extends RecyclerView.Adapter<PollChoiceAdapter.ViewHolder> {
    // Allows to remember the last item shown on screen
    private int lastPosition = -1;
    private boolean pollIsComplete = false;
    private int selectedPos = -1;
    private int lastSelectedPos = -1;
    private PollActivity activity;
    private ArrayList<PollItem> choices;
    private PollItem pollItem;
    private static Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.choice_card_view) CardView card;
        @BindView(R.id.choice_name) TextView  choiceName;
        @BindView(R.id.vote_count) TextView voteCount;
        @BindView(R.id.checkmark) ImageView checkmark;
        @BindView(R.id.winner) TextView winnerText;
        @BindView(R.id.location) Button location;


        public ViewHolder(Context c, View v) {
            super(v);
            ButterKnife.bind(this, v);
            context = c;
            card.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(selectedPos != getAdapterPosition() && !pollIsComplete) {
                lastSelectedPos = selectedPos;
                selectedPos = getAdapterPosition();
                notifyItemChanged(selectedPos);
                notifyItemChanged(lastSelectedPos);
                activity.updateChoice(lastSelectedPos, selectedPos);
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PollChoiceAdapter(ArrayList<PollItem> pc, int selected, PollActivity pa) {
        choices = pc;
        activity = pa;
        selectedPos = selected;
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
        String voteString = pollItem.getVotes() + " Votes";
        holder.voteCount.setText(voteString);
        holder.winnerText.setVisibility(View.INVISIBLE);
        if (position == selectedPos) {
            holder.checkmark.setVisibility(View.VISIBLE);
            if (pollIsComplete)
                holder.card.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
            else
                holder.card.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.choiceName.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.voteCount.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
        } else {
            holder.checkmark.setVisibility(View.GONE);
            holder.card.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.choiceName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
            holder.voteCount.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        }
        if (pollIsComplete && isWinner(position)) {
            holder.winnerText.setVisibility(View.VISIBLE);
            holder.card.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            holder.choiceName.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.voteCount.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
            holder.location.setVisibility(View.VISIBLE);
            final String choiceStr = holder.choiceName.getText().toString();
            holder.location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q="+choiceStr);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                    mapIntent.setPackage("com.google.android.apps.maps");
                    if(context instanceof PollActivity){
                        context.startActivity(mapIntent);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    private boolean isWinner(int pos) {
        int max = 0;
        for(PollItem i: choices) {
            if (i.getVotes() > max) {
                max = i.getVotes();
            }
        }
        return choices.get(pos).getVotes() == max;
    }

    public void setPollComplete() {
        pollIsComplete = true;
        notifyDataSetChanged();
    }

}
