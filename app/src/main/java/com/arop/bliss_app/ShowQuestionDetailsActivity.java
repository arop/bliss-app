package com.arop.bliss_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.arop.bliss_app.apiObjects.Question;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ShowQuestionDetailsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView questionTitleTextView;
    private TextView dateTextView;
    private ImageView imgView;

    private Question qt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_question_details);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        questionTitleTextView = (TextView) findViewById(R.id.questionTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        imgView = (ImageView) findViewById(R.id.imageView);

        qt = (Question) getIntent().getSerializableExtra("Question");
        setView();
        setRecyclerView();
    }

    /**
     * Sets view elements
     */
    private void setView() {
        questionTitleTextView.setText(qt.getQuestion());
        dateTextView.setText(qt.getFormattedDate());
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(qt.getImage_url(),imgView);
    }

    /**
     * Set choices recycler view
     */
    private void setRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.choicesRecyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ChoicesAdapter(qt.getChoices(), this);
        mRecyclerView.setAdapter(mAdapter);
    }
}
