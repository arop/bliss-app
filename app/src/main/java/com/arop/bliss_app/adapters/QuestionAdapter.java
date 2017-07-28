package com.arop.bliss_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arop.bliss_app.R;
import com.arop.bliss_app.ShowQuestionDetailsActivity;
import com.arop.bliss_app.apiObjects.Question;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by andre on 25/07/2017.
 */

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
    private ArrayList<Question> mDatasetQuestions;
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
    public QuestionAdapter(ArrayList<Question> f, Context c) {
        mDatasetQuestions = f;
        context = c;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public QuestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_question_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(QuestionAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Question qt = mDatasetQuestions.get(position);

        // Set thumbnail
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageView imgView = (ImageView) holder.mView.findViewById(R.id.thumbImageView);
        imageLoader.displayImage(qt.getThumb_url(), imgView);

        // Set question
        ((TextView) holder.mView.findViewById(R.id.questionTextView)).setText(qt.getQuestion());
        // Set date

        ((TextView) holder.mView.findViewById(R.id.dateTextView)).setText(qt.getFormattedDate());

        OnQuestionClickListener clickListener = new OnQuestionClickListener(position);
        holder.mView.setOnClickListener(clickListener);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDatasetQuestions.size();
    }

    /**
     * Set questions
     *
     * @param dq questions list
     */
    public void setmDatasetQuestions(ArrayList<Question> dq) {
        this.mDatasetQuestions = dq;
        this.notifyDataSetChanged();
    }

    /**
     * Click listener on question to start new activity for question details
     */
    private class OnQuestionClickListener implements View.OnClickListener {

        private final int itemPosition;

        OnQuestionClickListener(int position) {
            this.itemPosition = position;
        }

        @Override
        public void onClick(final View view) {
            Question q = mDatasetQuestions.get(itemPosition);

            Intent intent = new Intent(context, ShowQuestionDetailsActivity.class);
            intent.putExtra("Question", q);
            context.startActivity(intent);
        }
    }

}

