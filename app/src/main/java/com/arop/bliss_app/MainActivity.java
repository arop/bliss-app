package com.arop.bliss_app;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arop.bliss_app.api.RetrofitClient;
import com.arop.bliss_app.api.APIInterface;
import com.arop.bliss_app.apiObjects.Health;
import com.arop.bliss_app.apiObjects.Question;
import com.arop.bliss_app.networkUtils.ConnectivityEvent;
import com.arop.bliss_app.networkUtils.NetworkStateReceiver;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    static APIInterface apiInterface;
    ProgressBar progressBar;
    View currentLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Question> questions;

    private ImageLoader imageLoader;

    private EventBus bus = EventBus.getDefault();
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        currentLayout = findViewById(R.id.coordinatorLayout);

        questions = new ArrayList<>();

        /////////////////////////
        setFloatingButton();

        // Register as a subscriber
        bus.register(this);
        setDialog();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);

        setImageLoader();
        setRecyclerView();

        checkHealth();
    }

    /**
     * Sets image loader settings
     */
    private void setImageLoader() {
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024)
                .build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    /**
     * Sets questions list recycler view
     */
    private void setRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.questionsRecyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new QuestionAdapter(questions, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * Request server health status
     */
    private void checkHealth() {
        progressBar.setVisibility(View.VISIBLE);
        Call getHealthCall = apiInterface.getHealth();
        getHealthCall.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Health health = (Health) response.body();
                String status = health.getStatus();
                if (Objects.equals(status, "OK")) {
                    progressBar.setVisibility(View.GONE);
                    getQuestions(0);
                } else if (Objects.equals(status, "NOT OK")) {
                    progressBar.setVisibility(View.GONE);
                    showRetrySnackbar();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRetrySnackbar() {
        Snackbar snackbar = Snackbar
                .make(currentLayout, "Connection failed, try again!", Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkHealth();
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    /**
     * Get more questions from server
     *
     * @param currentNumberItems
     */
    private void getQuestions(final int currentNumberItems) {
        Log.e("temp", currentNumberItems + "");
        Call getQuestionsCall = apiInterface.getQuestions(10, currentNumberItems, "");
        getQuestionsCall.enqueue(new Callback<ArrayList<Question>>() {
            @Override
            public void onResponse(Call<ArrayList<Question>> call, Response<ArrayList<Question>> response) {
                ArrayList<Question> qts = response.body();
                questions.addAll(qts);
                // update dataset
                ((QuestionAdapter) mAdapter).setmDatasetQuestions(qts);
            }

            @Override
            public void onFailure(Call<ArrayList<Question>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // Unregister
        bus.unregister(this);
        super.onDestroy();
    }

    /**
     * Perform action when connectivity changes
     * @param event
     */
    @Subscribe
    public void onEvent(ConnectivityEvent event){
        if(!event.isConnected())
            dialog.show();
        else dialog.dismiss();
    }

    /**
     * Sets dialog for lost connection
     */
    private void setDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.connectivity_lost_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }
}
