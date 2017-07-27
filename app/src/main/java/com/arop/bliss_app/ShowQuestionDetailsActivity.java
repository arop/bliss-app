package com.arop.bliss_app;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.arop.bliss_app.api.APIInterface;
import com.arop.bliss_app.api.RetrofitClient;
import com.arop.bliss_app.apiObjects.Question;
import com.arop.bliss_app.networkUtils.ConnectivityEvent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowQuestionDetailsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView questionTitleTextView;
    private TextView dateTextView;
    private ImageView imgView;

    private Question qt;

    APIInterface apiInterface;
    private EventBus bus = EventBus.getDefault();
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_question_details);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        questionTitleTextView = (TextView) findViewById(R.id.questionTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        imgView = (ImageView) findViewById(R.id.imageView);

        apiInterface = RetrofitClient.getClient().create(APIInterface.class);

        qt = (Question) getIntent().getSerializableExtra("Question");
        setView();
        setRecyclerView();

        // Register as a subscriber
        bus.register(this);
        setDialog();
    }

    /**
     * Sets view elements
     */
    private void setView() {
        questionTitleTextView.setText(qt.getQuestion());
        dateTextView.setText(qt.getFormattedDate());
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(qt.getImage_url(), imgView);
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

        mAdapter = new ChoicesAdapter(qt, qt.getChoices(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Vote on a choice and update view
     *
     * @param q question
     */
    public void vote(Question q) {
        Call getQuestionsCall = apiInterface.vote(q.getId(), q);
        getQuestionsCall.enqueue(new Callback<Question>() {
            @Override
            public void onResponse(Call<Question> call, Response<Question> response) {
                //TODO check bad votes
                Question qr = response.body();
                qt = qr;
                setView();
                setRecyclerView();
            }

            @Override
            public void onFailure(Call<Question> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Unregister
        bus.unregister(this);
        super.onDestroy();
    }

    /**
     * Perform action when connectivity changes
     *
     * @param event
     */
    @Subscribe
    public void onEvent(ConnectivityEvent event) {
        if (!event.isConnected())
            dialog.show();
        else dialog.dismiss();
    }

    /**
     * Sets connectivityDialog for lost connection
     */
    private void setDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.connectivity_lost_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }

}
