package com.arop.bliss_app;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arop.bliss_app.adapters.QuestionAdapter;
import com.arop.bliss_app.api.RetrofitClient;
import com.arop.bliss_app.api.APIInterface;
import com.arop.bliss_app.apiObjects.Health;
import com.arop.bliss_app.apiObjects.Question;
import com.arop.bliss_app.apiObjects.Share;
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
    private static ArrayList<Question> questions, searchQuestions;
    private String searchQuery = "";
    private FloatingActionButton shareSearchFab;
    private TextView searchResultsTextView;

    private ImageLoader imageLoader;

    private EventBus bus = EventBus.getDefault();
    private Dialog connectivityDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        currentLayout = findViewById(R.id.coordinatorLayout);

        questions = new ArrayList<>();
        searchQuestions = new ArrayList<>();

        searchResultsTextView = (TextView) findViewById(R.id.searchResultsTextView);

        showMoreButton = (Button) findViewById(R.id.showMoreButton);
        showMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchResultsTextView.getVisibility() == View.VISIBLE) {
                    searchQuestions(searchQuestions.size(), searchQuery);
                } else {
                    getQuestions(questions.size());
                }
            }
        });

        shareSearchFab = (FloatingActionButton) findViewById(R.id.shareFloatingActionButton);
        shareSearchFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showShareDialog();
            }
        });
        shareSearchFab.setVisibility(View.GONE);

        // Register as a subscriber
        bus.register(this);
        setConnectivityDialog();

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
                if (question_filter.length() > 0) {
                    searchView.setQuery(question_filter, true);
                    shareSearchFab.setVisibility(View.INVISIBLE);
                } else {
                    searchView.setQuery(question_filter, false);
                }
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
                shareSearchFab.setVisibility(View.GONE);
                searchResultsTextView.setVisibility(View.GONE);
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
    private void setConnectivityDialog() {
        connectivityDialog = new Dialog(this);
        connectivityDialog.setContentView(R.layout.connectivity_lost_dialog);
        connectivityDialog.setCanceledOnTouchOutside(false);
        connectivityDialog.setCancelable(false);
    }


    /////////////////////////////////////////////////////////
    ///////// API REQUESTS //////////////////////////////////

    /**
     * Request server health status
     */
    private void checkHealth() {
        progressBar.setVisibility(View.VISIBLE);
        Call getHealthCall = apiInterface.getHealth();
        getHealthCall.enqueue(new Callback<Health>() {
            @Override
            public void onResponse(@NonNull Call<Health> call, @NonNull Response<Health> response) {
                Health health = response.body();
                if (health != null) {
                    String status = health.getStatus();
                    if (Objects.equals(status, "OK")) {
                        progressBar.setVisibility(View.GONE);
                        getQuestions(0);
                    } else if (Objects.equals(status, "NOT OK")) {
                        progressBar.setVisibility(View.GONE);
                        showRetrySnackbar();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Health> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                if (qts != null) {
                    questions.addAll(qts);
                    // update dataset
                    ((QuestionAdapter) mAdapter).setmDatasetQuestions(questions);
                    showMoreButton.setVisibility(View.VISIBLE);
                    shareSearchFab.setVisibility(View.GONE);
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
    private void searchQuestions(final int currentNumberItems, final String searchQuery1) {
        Call searchQuestionsCall = apiInterface.getQuestions(10, currentNumberItems, searchQuery1);
        searchQuestionsCall.enqueue(new Callback<ArrayList<Question>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Question>> call, @NonNull Response<ArrayList<Question>> response) {
                ArrayList<Question> qts = response.body();
                if (qts != null) {
                    searchQuestions.addAll(qts);
                    // update dataset
                    ((QuestionAdapter) mAdapter).setmDatasetQuestions(searchQuestions);
                    shareSearchFab.setVisibility(View.VISIBLE);
                    searchQuery = searchQuery1;
                    searchResultsTextView.setText(getResources().getString(R.string.search_query, searchQuery));
                    searchResultsTextView.setVisibility(View.VISIBLE);
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

    /**
     * Send share api call
     */
    private void shareSearchQuery(String email) {
        String url = "blissrecruitment://questions?question_filter=" + searchQuery;

        Call shareSearchCall = apiInterface.share(email, url);
        shareSearchCall.enqueue(new Callback<Share>() {
            @Override
            public void onResponse(@NonNull Call<Share> call, @NonNull Response<Share> response) {
                Share s = response.body();
                if (s != null) {
                    Toast.makeText(getApplicationContext(), s.getStatus(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Share> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show share dialog
     */
    private void showShareDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("Destination email");
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Share this search?")
                .setMessage("Search query: " + searchQuery + "\nPlease enter destination email")
                .setView(editText)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String email = editText.getText().toString();
                        shareSearchQuery(email);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_menu_share)
                .show();
    }

    /**
     * Update question for when vote occurs in show details
     *
     * @param q question
     */
    public static void updateQuestion(Question q) {
        int i = questions.indexOf(q);
        if (i >= 0) {
            questions.set(i, q);
        }

        int j = searchQuestions.indexOf(q);
        if (j >= 0) {
            searchQuestions.set(j, q);
        }
    }
}
