package com.arop.bliss_app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arop.bliss_app.adapters.ChoicesAdapter;
import com.arop.bliss_app.api.APIInterface;
import com.arop.bliss_app.api.RetrofitClient;
import com.arop.bliss_app.apiObjects.Question;
import com.arop.bliss_app.apiObjects.Share;
import com.arop.bliss_app.networkUtils.ConnectivityEvent;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
    private Dialog dialog;
    private Button voteButton;

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

        voteButton = (Button) findViewById(R.id.voteButton);
        voteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = ((ChoicesAdapter) mAdapter).getSelectedChoice();
                if (i >= 0) {
                    qt.addVote(qt.getChoices().get(i).getName());
                    vote(qt);
                } else {
                    Toast.makeText(ShowQuestionDetailsActivity.this, "Please select an option!", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share) {
            showShareDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        builder.setTitle("Share this question?")
                .setMessage("Please enter destination email")
                .setView(editText)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String email = editText.getText().toString();
                        shareQuestion(email);
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

    ///////////////////////////////
    /////// API requests
    /**
     * Vote on a choice and update view
     *
     * @param q question
     */

    public void vote(Question q) {
        voteButton.setEnabled(false);
        Call voteCall = apiInterface.vote(q.getId(), q);
        voteCall.enqueue(new Callback<Question>() {
            @Override
            public void onResponse(@NonNull Call<Question> call, @NonNull Response<Question> response) {
                qt = response.body();
                setView();
                setRecyclerView();
                voteButton.setEnabled(true);
                MainActivity.updateQuestion(qt);
            }

            @Override
            public void onFailure(@NonNull Call<Question> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                voteButton.setEnabled(true);
            }
        });
    }

    /**
     * Send share api call
     */
    private void shareQuestion(String email) {
        String url = "blissrecruitment://questions?question_id=" + qt.getId();

        Call shareQuestionCall = apiInterface.share(email, url);
        shareQuestionCall.enqueue(new Callback<Share>() {
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

}
