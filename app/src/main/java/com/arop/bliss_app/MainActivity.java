package com.arop.bliss_app;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arop.bliss_app.api.RetrofitClient;
import com.arop.bliss_app.api.APIInterface;
import com.arop.bliss_app.apiObjects.Health;
import com.arop.bliss_app.apiObjects.Question;
import com.arop.bliss_app.networkUtils.ConnectivityEvent;
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

    private APIInterface apiInterface;
    private ProgressBar progressBar;
    private View currentLayout;
    private SearchView searchView;
    private Button showMoreButton;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Question> questions, searchQuestions;

    private ImageLoader imageLoader;

    private EventBus bus = EventBus.getDefault();
    Dialog connectivityDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        currentLayout = findViewById(R.id.coordinatorLayout);

        questions = new ArrayList<>();
        searchQuestions = new ArrayList<>();

        showMoreButton = (Button) findViewById(R.id.showMoreButton);
        showMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getQuestions(questions.size());
            }
        });

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
     * Checks if app was opened by an external link
     */
    private void checkIfOpenedExternally() {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Objects.equals(action, Intent.ACTION_VIEW)) {
            Uri data = intent.getData();
            String question_id = data.getQueryParameter("question_id");
            if (question_id != null) {
                try {
                    int q_id = Integer.parseInt(question_id);
                    getQuestion(getApplicationContext(), q_id);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Invalid question ID", Toast.LENGTH_SHORT).show();
                }
            }

            String question_filter = data.getQueryParameter("question_filter");
            if (question_filter != null) {
                searchView.setIconified(false);
                boolean isEmpty = (question_filter.length() > 0);
                searchView.setQuery(question_filter, isEmpty);

            }
        }
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
                    showMoreButton.setVisibility(View.VISIBLE);
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

    /**
     * Shows retry snackbar when server health is NOT OK
     */
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
     * @param currentNumberItems current number of items
     */
    private void getQuestions(final int currentNumberItems) {
        Call getQuestionsCall = apiInterface.getQuestions(10, currentNumberItems, "");
        getQuestionsCall.enqueue(new Callback<ArrayList<Question>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Question>> call, @NonNull Response<ArrayList<Question>> response) {
                ArrayList<Question> qts = response.body();
                if(qts != null) {
                    questions.addAll(qts);
                    // update dataset
                    ((QuestionAdapter) mAdapter).setmDatasetQuestions(questions);
                } else {
                    Toast.makeText(getApplicationContext(), "Error fetching questions!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Question>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get more questions from server
     *
     * @param currentNumberItems current number of items
     */
    private void searchQuestions(final int currentNumberItems, final String searchQuery) {
        Call getQuestionsCall = apiInterface.getQuestions(10, currentNumberItems, searchQuery);
        getQuestionsCall.enqueue(new Callback<ArrayList<Question>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Question>> call, @NonNull Response<ArrayList<Question>> response) {
                ArrayList<Question> qts = response.body();
                if (qts != null) {
                    searchQuestions.addAll(qts);
                    // update dataset
                    ((QuestionAdapter) mAdapter).setmDatasetQuestions(qts);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error fetching questions!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Question>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

        // On search click show search questions
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Search query
                searchQuestions(0, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // On dismiss clear search data and show default questions
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                ((QuestionAdapter) mAdapter).setmDatasetQuestions(questions);
                searchView.onActionViewCollapsed();
                searchQuestions.clear();
                return false;
            }
        });

        checkIfOpenedExternally();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
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
     *
     * @param event connectivity event
     */
    @Subscribe
    public void onEvent(ConnectivityEvent event) {
        if (!event.isConnected())
            connectivityDialog.show();
        else connectivityDialog.dismiss();
    }

    /**
     * Sets connectivityDialog for lost connection
     */
    private void setDialog() {
        connectivityDialog = new Dialog(this);
        connectivityDialog.setContentView(R.layout.connectivity_lost_dialog);
        connectivityDialog.setCanceledOnTouchOutside(false);
        connectivityDialog.setCancelable(false);
    }

    /**
     * Get question from server
     *
     * @param id question id
     */
    private void getQuestion(final Context context, int id) {
        Call getQuestionCall = apiInterface.getQuestion(id);
        getQuestionCall.enqueue(new Callback<Question>() {
            @Override
            public void onResponse(@NonNull Call<Question> call, @NonNull Response<Question> response) {
                Question qt = response.body();
                Intent intent = new Intent(context, ShowQuestionDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Question", qt);
                context.startActivity(intent);
            }

            @Override
            public void onFailure(@NonNull Call<Question> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
