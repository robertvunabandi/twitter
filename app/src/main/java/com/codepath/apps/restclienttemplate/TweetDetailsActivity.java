package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetText;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetDetailsActivity extends AppCompatActivity {
    public ImageView ivProfileImageD, ibReplyD, ivRetweetD, ivLikeD;
    public TextView tvNameD, tvScreenNameD, tvBodyD, tvCreatedAtD;
    public TextView tvRetweetsD, tvLikesD;
    public long tUid;
    public boolean tRetweeted, tLiked;
    private static TwitterClient client;

    public String tName, tScreenName, tBody, tCreatedAt, tProfileImageUrl;
    public String tRetweets, tLikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        getSupportActionBar().setTitle("Tweet");

        // enables the back press
        // ActionBar actionBar = getActionBar();
        // actionBar.setDisplayHomeAsUpEnabled(true);

        // declare the variables to be used



        client = TwitterApp.getRestClient(); // get the twitter client

        // set those variables to values received from the intent
        tName = getIntent().getStringExtra("tName");
        tScreenName = getIntent().getStringExtra("tScreenName");
        tBody = getIntent().getStringExtra("tBody");
        tCreatedAt = getIntent().getStringExtra("tCreatedAt");
        tProfileImageUrl = getIntent().getStringExtra("tProfileImageUrl");
        tRetweets = getIntent().getStringExtra("tRetweets");
        tLikes = getIntent().getStringExtra("tLikes");
        tUid = getIntent().getLongExtra("tUid", -1);
        tLiked = getIntent().getBooleanExtra("tLiked", false);
        tRetweeted = getIntent().getBooleanExtra("tRetweeted", false);

        // create the correct variables
        ivProfileImageD = (ImageView) findViewById(R.id.ivProfileImageD);
        tvNameD = (TextView) findViewById(R.id.tvNameD);
        tvScreenNameD = (TextView) findViewById(R.id.tvScreenNameD);
        tvBodyD = (TextView) findViewById(R.id.tvBodyD);
        tvCreatedAtD = (TextView) findViewById(R.id.tvCreatedAtD);
        ibReplyD = (ImageView) findViewById(R.id.ibReplyD);

        tvRetweetsD = (TextView) findViewById(R.id.tvRetweetsD);
        tvLikesD = (TextView) findViewById(R.id.tvLikesD);
        ivRetweetD = (ImageView) findViewById(R.id.ivRetweetD);
        ivLikeD = (ImageView) findViewById(R.id.ivLoveTweetD);

        // set the texts
        tvNameD.setText(tName);
        tvScreenNameD.setText("@"+tScreenName);
        TweetText finalTweet = new TweetText(tBody);
        tvBodyD.setText(Html.fromHtml(finalTweet.finalText));
        tvCreatedAtD.setText(tCreatedAt);
        tvRetweetsD.setText(tRetweets);
        tvLikesD.setText(tLikes);

        if (tRetweeted) ivRetweetD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_retweet_green));

        // set the image profile
        Glide.with(this).load(tProfileImageUrl)
                .bitmapTransform(new RoundedCornersTransformation(this, 2000, 0))
                .placeholder(R.drawable.ic_face_placeholder)
                .error(R.drawable.ic_face_placeholder)
                .into(ivProfileImageD);
                // .override(2048, 2048)

        // Reply listener event
        ibReplyD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TweetDetailsActivity.this, ComposeActivity.class);
                i.putExtra("text", tScreenName);
                startActivity(i);
            }
        });

        // Retweet listener event
        ivRetweetD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Retweet not implemented", Toast.LENGTH_SHORT).show();
                if (!tRetweeted) {
                    client.retweetTweet(tUid, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet tweet;
                            try {
                                tweet = Tweet.fromJSON(response);
                                tRetweeted = tweet.tweetRetweeted;
                                ivRetweetD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_retweet_green));

                                tRetweets = Integer.toString(tweet.retweetCount);
                                tvRetweetsD.setText(tRetweets);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            super.onSuccess(statusCode, headers, response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(client.TAG, String.format("JSON ARRAY FAILURE: %s", errorResponse.toString()));
                            throwable.printStackTrace();

                            Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            Log.e(client.TAG, String.format("JSON ARRAY FAILURE: %s", errorResponse.toString()));
                            throwable.printStackTrace();

                            Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(client.TAG, String.format("JSON ARRAY FAILURE: %s", responseString));
                            throwable.printStackTrace();

                            Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    client.unretweetTweet(tUid, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet tweet;
                            try {
                                tweet = Tweet.fromJSON(response);
                                tRetweeted = tweet.tweetRetweeted;
                                ivRetweetD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_retweet));

                                tRetweets = Integer.toString(tweet.retweetCount);
                                tvRetweetsD.setText(tRetweets);
                                // CHANGE IMAGE
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            super.onSuccess(statusCode, headers, response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(client.TAG, String.format("JSON ARRAY FAILURE: %s", errorResponse.toString()));
                            throwable.printStackTrace();

                            Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            Log.e(client.TAG, String.format("JSON ARRAY FAILURE: %s", errorResponse.toString()));
                            throwable.printStackTrace();

                            Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(client.TAG, String.format("JSON ARRAY FAILURE: %s", responseString));
                            throwable.printStackTrace();

                            Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the data. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            super.onSuccess(statusCode, headers, responseString);
                        }
                    });
                }

            }
        });

        // Like listener event
        ivLikeD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Like not implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        finish();
        // Intent i = new Intent(TweetDetailsActivity.this, TimelineActivity.class);
        // startActivity(i);
    }
}
