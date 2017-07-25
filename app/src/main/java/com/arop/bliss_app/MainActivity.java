package com.arop.bliss_app;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arop.bliss_app.api.RetrofitClient;
import com.arop.bliss_app.api.APIInterface;
import com.arop.bliss_app.apiObjects.Health;
import com.arop.bliss_app.apiObjects.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    APIInterface apiInterface;
    ProgressBar progressBar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        questions = new ArrayList<>();

        /////////////////////////
        setFloatingButton();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);

        setRecyclerView();
        checkHealth();
    }

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
        Call getHealthCall = apiInterface.getHealth();
        getHealthCall.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Health health = (Health) response.body();
                String status = health.getStatus();
                if(Objects.equals(status, "OK")) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                    getQuestions();
                    setRecyclerView();
                }
                else if(Objects.equals(status, "NOT OK"))
                    Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getQuestions() {
        Call getQuestionsCall = apiInterface.getQuestions(10,1,"");
        getQuestionsCall.enqueue(new Callback<ArrayList<Question>>() {
            @Override
            public void onResponse(Call<ArrayList<Question>> call, Response<ArrayList<Question>> response) {
                ArrayList<Question> qts = response.body();
                questions = qts;

                // specify an adapter (see also next example)
                mAdapter = new QuestionAdapter(questions, getApplicationContext());
                mRecyclerView.setAdapter(mAdapter);
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
}
