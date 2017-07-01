package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {
    // TODO - Cite face icon placeholder: https://material.io/icons/#ic_face (FROM MATERIAL DESIGN)

    private TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer; // for swiping to refresh tweets
    private ProgressBar pb;
    int totalTweets = 40;
    Calendar CAL = Calendar.getInstance();
    SimpleDateFormat HMS = new SimpleDateFormat("HH:mm:ss");
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);;

    public static final String TAG = "TimelineActivityTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        client = TwitterApp.getRestClient();

        // find the RecyclerView
        rvTweets = (RecyclerView) findViewById(R.id.rvTweet);
        // find the progressBar
        pb = (ProgressBar) findViewById(R.id.pbLoading);

        reinitializeTweetsAndAdapter();
        populateTimeline(totalTweets);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainerRefresher);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                reinitializeTweetsAndAdapter();
                // once the network request has completed successfully.
                populateTimeline(totalTweets);
                swipeContainer.setRefreshing(false);
            }
        });
        rvTweets.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int lastPosition = layoutManager.findLastVisibleItemPosition();
                    if (lastPosition > tweets.size() - 5) {
                        addToTimeline();
                    }
                    //findFirstVisibleItemPosition();
                }
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimaryDarkLight, R.color.colorLightGrey, R.color.colorAccent, R.color.colorPrimaryLight);

        // setup the listener on creation
        setupListViewListener();
        getSupportActionBar().setTitle("Home");
    }


    public void reinitializeTweetsAndAdapter(){
        pb.setVisibility(ProgressBar.VISIBLE); // set the progress bar to visible
        tweets = new ArrayList<>(); // init the array list (data source)
        tweetAdapter = new TweetAdapter(tweets); // construct adapter from data source
        // tweetAdapter.max_id = max_id_from_timeline; // TEMP
        rvTweets.setLayoutManager(layoutManager); // RecyclerView setup (layout manager, use adapter)
        rvTweets.setAdapter(tweetAdapter); // set the Adapter

        Log.d(TAG, String.format("Tweets reinitialized at %s", HMS.format(CAL.getTime())));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.compose, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.aCompose:
                composeMessage(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private final int REQUEST_CODE = 200;
    public void composeMessage(String text) {
        Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
        i.putExtra("text", text);
        startActivityForResult(i, REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        Log.d(TAG, String.format("%s %s %s RESULT_OK: %s", "TIMELINE START", resultCode, requestCode, RESULT_OK));
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            // add the tweet to the set of tweets at position 0
            tweets.add(0, tweet);
            tweetAdapter.notifyItemInserted(0);
            // scrolls back to position 0 to see the tweet
            rvTweets.scrollToPosition(0);
            pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
        }
    }

    private void populateTimeline(int count) {
        Log.d(TAG, String.format("PopulateTimeline at %s", HMS.format(CAL.getTime())));
        client.getHomeTimeline(count, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // FOR JSON OBJECTS
                Log.d(client.TAG, response.toString());
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // FOR JSON ARRAYS
                // Log.d(client.TAG, response.toString());
                // iterate through the JSON array, for each entry deserialize the JSON Object
                Log.d(TAG, String.format("PopulateTimeline | RESPONSE LENGTH %s SUCCESS at %s", response.length(),  HMS.format(CAL.getTime())));
                for (int i = 0; i < response.length(); i++){

                    // convert each object into a tweet model
                    Tweet tweet;
                    try {
                        tweet = Tweet.fromJSON(response.getJSONObject(i));
                        // add the tweet model to our data source
                        tweets.add(tweet);
                        // notify the adapter that we've added an item
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);
                        // bc we start from 0, the total number of tweets added to our array list will be the size of tweets - 1
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, String.format("PopulateTimeline | Tweet at index %s SUCCESS at %s", i,  HMS.format(CAL.getTime())));
                }
                Log.d(TAG, String.format("PopulateTimeline SUCCESS at %s", HMS.format(CAL.getTime())));
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(client.TAG, String.format("REGULAR FAILURE: %s" , responseString));
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(client.TAG, String.format("JSON OBJECT FAILURE: %s" ,errorResponse.toString()));
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.e(client.TAG, String.format("JSON ARRAY FAILURE: %s" ,errorResponse.toString()));
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
        });
    }

    private void addToTimeline() {
        pb.setVisibility(ProgressBar.VISIBLE);
        Log.d(TAG, String.format("addToTimeline at %s", HMS.format(CAL.getTime())));
        client.addToTimeline(tweetAdapter.max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                for (int i = 0; i < response.length(); i++){

                    // convert each object into a tweet model
                    Tweet tweet;
                    try {
                        tweet = Tweet.fromJSON(response.getJSONObject(i));
                        // add the tweet model to our data source
                        tweets.add(tweet);
                        // notify the adapter that we've added an item
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);
                        // bc we start from 0, the total number of tweets added to our array list will be the size of tweets - 1
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                totalTweets = tweets.size(); // changes the number of total tweets
                Log.d(TAG, String.format("PopulateTimeline SUCCESS at %s", HMS.format(CAL.getTime())));
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(client.TAG, response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(client.TAG, errorResponse.toString());
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 15 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.e(client.TAG, errorResponse.toString());
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 15 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(client.TAG, responseString);
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 15 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
        });
    }


    private void setupListViewListener() {
        /*rvTweets.setOnClickListener(new AdapterView.OnItemClickListener(){
            Intent i = new Intent(TimelineActivity.this, SingleTweetActivity.class);
            startActivity(i);
        });*/
        // new AdapterView.OnItemClickListener(){}
    }

    /*
    REALLY GOOD HELP I GOT FROM STACK OVERFLOW
    ===========================================
    LINK: https://stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview

    LinearLayoutManager mLayoutManager;
    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);
    * */
}
