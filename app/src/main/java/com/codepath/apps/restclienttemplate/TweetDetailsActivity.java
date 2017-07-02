package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.TweetText;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetDetailsActivity extends AppCompatActivity {
    public View mediaSeparatorD;
    public ImageView ivProfileImageD, ibReplyD, ivRetweetD, ivLikeD, ivTweetedImageD;
    public TextView tvNameD, tvScreenNameD, tvBodyD, tvCreatedAtD, tvRetweetsD, tvLikesD;
    public long tUid;
    public boolean tRetweeted, tLiked, tMediaFound;
    private static TwitterClient client;

    public String tName, tScreenName, tBody, tCreatedAt, tProfileImageUrl;
    public long tRetweets, tLikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);
        getSupportActionBar().setTitle("Tweet");

        // get the twitter client
        client = TwitterApp.getRestClient();

        // set those variables to values received from the intent
        tName = getIntent().getStringExtra("tName");
        tScreenName = getIntent().getStringExtra("tScreenName");
        tBody = getIntent().getStringExtra("tBody");
        tCreatedAt = getIntent().getStringExtra("tCreatedAt");
        tProfileImageUrl = getIntent().getStringExtra("tProfileImageUrl");
        tRetweets = getIntent().getLongExtra("tRetweets", 0);
        tLikes = getIntent().getLongExtra("tLikes", 0);
        tUid = getIntent().getLongExtra("tUid", -1);
        tLiked = getIntent().getBooleanExtra("tLiked", false);
        tRetweeted = getIntent().getBooleanExtra("tRetweeted", false);
        tMediaFound = getIntent().getBooleanExtra("tMediaFound", false);

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
        ivTweetedImageD = (ImageView) findViewById(R.id.ivTweetedImageD);
        mediaSeparatorD = findViewById(R.id.mediaSeparatorD);

        // set the texts
        tvNameD.setText(tName);
        tvScreenNameD.setText("@"+tScreenName);
        TweetText finalTweet = new TweetText(tBody);
        tvBodyD.setText(Html.fromHtml(finalTweet.finalText));
        tvCreatedAtD.setText(tCreatedAt);
        tvRetweetsD.setText(Long.toString(tRetweets));
        tvLikesD.setText(Long.toString(tLikes));

        if (tRetweeted) ivRetweetD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_retweet_green_svg));
        else ivRetweetD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_retweet_svg));
        if (tLiked) ivLikeD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_heart_solid_red_svg));
        else ivLikeD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_heart_clear_light_blue_svg));
        if (tMediaFound) {
            mediaSeparatorD.setVisibility(View.VISIBLE);
            ivTweetedImageD.setVisibility(View.VISIBLE);
            Glide.with(getBaseContext()).load(getIntent().getStringExtra("tMediaUrlHTTPS"))
                    .bitmapTransform(new RoundedCornersTransformation(getBaseContext(), 20, 0))
                    .placeholder(R.drawable.ic_person_v3_svg)
                    .error(R.drawable.ic_person_v3_svg)
                    .into(ivTweetedImageD);
        } else {
            mediaSeparatorD.setVisibility(View.GONE);
            ivTweetedImageD.setVisibility(View.GONE);
        }

        // set the image profile
        Glide.with(this).load(tProfileImageUrl)
                .bitmapTransform(new RoundedCornersTransformation(this, 2000, 0))
                .placeholder(R.drawable.ic_person_v3_svg)
                .error(R.drawable.ic_person_v3_svg)
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
        ivRetweetD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tRetweeted){
                    client.retweetTweet(tUid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            tvRetweetsD.setText(Long.toString(tRetweets + 1));
                            tRetweeted = true;
                            ivRetweetD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_retweet_green_svg));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getBaseContext(), "Error occured while processing retweet action", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    client.unretweetTweet(tUid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            tvRetweetsD.setText(Long.toString(tRetweets));
                            tRetweeted = false;
                            ivRetweetD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_retweet_svg));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getBaseContext(), "Error occured while processing unretweet action", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // Like listener event
        ivLikeD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tLiked){
                    client.likeTweet(tUid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            tvLikesD.setText(Long.toString(tLikes + 1));
                            tLiked = true;
                            ivLikeD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_heart_solid_red_svg));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getBaseContext(), "Error occured while processing retweet action", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    client.unlikeTweet(tUid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            tvLikesD.setText(Long.toString(tLikes));
                            tLiked = false;
                            ivLikeD.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_heart_clear_light_blue_svg));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getBaseContext(), "Error occured while processing unretweet action", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
