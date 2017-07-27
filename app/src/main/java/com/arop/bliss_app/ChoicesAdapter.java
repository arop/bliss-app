package com.arop.bliss_app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.arop.bliss_app.apiObjects.Question;

import java.util.List;
import java.util.Locale;

/**
 * Created by andre on 25/07/2017.
 */

class ChoicesAdapter extends RecyclerView.Adapter<ChoicesAdapter.ViewHolder> {
    private List<Question.QuestionChoice> mDatasetQuestionChoices;
    private final Question question;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View mView;

        ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    ChoicesAdapter(Question q, List<Question.QuestionChoice> f, Context c) {
        mDatasetQuestionChoices = f;
        context = c;
        this.question = q;
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
    public void onBindViewHolder(ChoicesAdapter.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Question.QuestionChoice qt = mDatasetQuestionChoices.get(position);

        // Set choice
        Button rb = (Button) holder.mView.findViewById(R.id.choiceTextView);
        rb.setText(qt.getName());
        // Set vote
        ((TextView) holder.mView.findViewById(R.id.votesTextView)).setText(String.format(Locale.getDefault(), "%d", qt.getVotes()));

        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                question.addVote(qt.getName());
                ((ShowQuestionDetailsActivity)context).vote(question);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDatasetQuestionChoices.size();
    }
}


