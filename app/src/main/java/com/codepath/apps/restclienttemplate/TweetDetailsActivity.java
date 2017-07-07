package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    public ImageView ivProfileImageD, ibReplyD, ivRetweetD, ivLikeD, ivTweetedImageD;
    public TextView tvNameD, tvScreenNameD, tvBodyD, tvCreatedAtD, tvRetweetsD, tvLikesD;
    public long tUid;
    public boolean tRetweeted, tLiked, tMediaFound;
    private static TwitterClient client;

    public String tName, tScreenName, tBody, tCreatedAt, tProfileImageUrl;
    public long tRetweets, tLikes;

    // toolbar stuffs
    Toolbar tweet_detail_toolbar;
    TextView tweet_detail_toolbar_title;
    ImageView tweet_detail_toolbar_back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        // get the twitter client
        client = TwitterApp.getRestClient();

        // get/set the toolbar, set the title to home
        tweet_detail_toolbar = (Toolbar) findViewById(R.id.tweet_detail_toolbar);
        setSupportActionBar(tweet_detail_toolbar);
        tweet_detail_toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        getSupportActionBar().setTitle(""); // remove the default title
        tweet_detail_toolbar_title = (TextView) findViewById(R.id.tweet_detail_toolbar_title);
        tweet_detail_toolbar_back_button = (ImageView) findViewById(R.id.tweet_detail_toolbar_back_button);
        // create the link to the using user's profile
        tweet_detail_toolbar_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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

        // set the texts
        tvNameD.setText(tName);
        tvScreenNameD.setText("@"+tScreenName);
        TweetText finalTweet = new TweetText(tBody);
        tvBodyD.setText(Html.fromHtml(finalTweet.finalText));
        tvCreatedAtD.setText(tCreatedAt);
        tvRetweetsD.setText(Long.toString(tRetweets));
        tvLikesD.setText(Long.toString(tLikes));

        if (tRetweeted) ivRetweetD.setImageDrawable(ContextCompat.getDrawable(TweetDetailsActivity.this, R.drawable.ic_retweet_green_svg));
        else ivRetweetD.setImageDrawable(ContextCompat.getDrawable(TweetDetailsActivity.this, R.drawable.ic_retweet_svg));
        if (tLiked) ivLikeD.setImageDrawable(ContextCompat.getDrawable(TweetDetailsActivity.this, R.drawable.ic_heart_solid_red_svg));
        else ivLikeD.setImageDrawable(ContextCompat.getDrawable(TweetDetailsActivity.this, R.drawable.ic_heart_clear_light_blue_svg));
        if (tMediaFound) {
            ivTweetedImageD.setVisibility(View.VISIBLE);
            Glide.with(TweetDetailsActivity.this).load(getIntent().getStringExtra("tMediaUrlHTTPS"))
                    .bitmapTransform(new RoundedCornersTransformation(TweetDetailsActivity.this, 20, 0))
                    .placeholder(R.drawable.ic_picture_placeholder_svg)
                    .error(R.drawable.ic_picture_placeholder_svg)
                    .into(ivTweetedImageD);
        } else {
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
                            ivRetweetD.setImageDrawable(ContextCompat.getDrawable(TweetDetailsActivity.this, R.drawable.ic_retweet_green_svg));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(TweetDetailsActivity.this, "Error occured while processing retweet action", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    client.unretweetTweet(tUid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            tvRetweetsD.setText(Long.toString(tRetweets));
                            tRetweeted = false;
                            ivRetweetD.setImageDrawable(ContextCompat.getDrawable(TweetDetailsActivity.this, R.drawable.ic_retweet_svg));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(TweetDetailsActivity.this, "Error occured while processing unretweet action", Toast.LENGTH_SHORT).show();
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
                            ivLikeD.setImageDrawable(ContextCompat.getDrawable(TweetDetailsActivity.this, R.drawable.ic_heart_solid_red_svg));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(TweetDetailsActivity.this, "Error occured while processing retweet action", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    client.unlikeTweet(tUid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            tvLikesD.setText(Long.toString(tLikes));
                            tLiked = false;
                            ivLikeD.setImageDrawable(ContextCompat.getDrawable(TweetDetailsActivity.this, R.drawable.ic_heart_clear_light_blue_svg));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(TweetDetailsActivity.this, "Error occured while processing unretweet action", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        ivProfileImageD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TweetDetailsActivity.this, UserProfileActivity.class);
                i.putExtra("user_uid", tUid);
                i.putExtra("screenName", tScreenName);
                startActivity(i);
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
