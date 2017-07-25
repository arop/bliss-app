package com.arop.bliss_app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arop.bliss_app.apiObjects.Question;

import java.util.List;

/**
 * Created by andre on 25/07/2017.
 */

public class ChoicesAdapter extends RecyclerView.Adapter<ChoicesAdapter.ViewHolder> {
    private List<Question.QuestionChoice> mDatasetQuestionChoices;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChoicesAdapter(List<Question.QuestionChoice> f, Context c) {
        mDatasetQuestionChoices = f;
        context = c;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChoicesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_choices_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ChoicesAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Question.QuestionChoice qt = mDatasetQuestionChoices.get(position);

        // Set choice
        ((TextView)holder.mView.findViewById(R.id.choiceTextView)).setText(qt.getName());
        // Set vote
        ((TextView)holder.mView.findViewById(R.id.votesTextView)).setText(qt.getVotes());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDatasetQuestionChoices.size();
    }
}


