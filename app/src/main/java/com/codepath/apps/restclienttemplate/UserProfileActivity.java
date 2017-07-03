package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class UserProfileActivity extends AppCompatActivity {
    public User user;
    private TwitterClient client;
    public Long userId;
    public String screenName, TAG = "UserProfileActivity";

    // logging times
    Calendar CAL = Calendar.getInstance();
    SimpleDateFormat HMS = new SimpleDateFormat("HH:mm:ss");

    // recycler view for tweets shown on user timeline
    int totalTweets = 30;
    TwitterAdapterProfile tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweetsP;
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    private ProgressBar pb;

    // Others
    ScrollView scrollProfile;
    android.app.ActionBar actionBar;

    // get the views
    public TextView tvUserNameP, tvScreenNameP, tvSinceP, tvFollowersCount, tvFollowers, tvFollowingsCount, tvFollowings, tvUserDescription;
    public ImageView bannerImage;
    public FloatingActionButton ivProfileImageP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        client = TwitterApp.getRestClient();

        rvTweetsP = (RecyclerView) findViewById(R.id.rvTweetP);
        pb = (ProgressBar) findViewById(R.id.pbLoading);
        scrollProfile = (ScrollView) findViewById(R.id.scrollProfile);
        // actionBar = getActionBar();


        userId = getIntent().getLongExtra("user_uid", -1);
        screenName = getIntent().getStringExtra("screenName");
        if (userId == -1){
            onError(0, null);
        } else if (screenName == null) {
            onError(0, "Screen name was null");
        }

        // set the title


        tvUserNameP = (TextView) findViewById(R.id.tvUserNameP);
        tvScreenNameP = (TextView) findViewById(R.id.tvScreenNameP);
        tvSinceP = (TextView) findViewById(R.id.tvSinceP);
        tvFollowersCount = (TextView) findViewById(R.id.tvFollowersCount);
        tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        tvFollowingsCount = (TextView) findViewById(R.id.tvFollowingsCount);
        tvFollowings = (TextView) findViewById(R.id.tvFollowings);
        tvUserDescription = (TextView) findViewById(R.id.tvUserDescription);

        ivProfileImageP = (FloatingActionButton) findViewById(R.id.ivProfileImageP);
        bannerImage = (ImageView) findViewById(R.id.bannerImage);
        // get the user from the following request
        client.user(userId, screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    user = User.fromJSON(response);

                    // populate the screen with info gathered
                    tvUserNameP.setText(user.name);
                    tvScreenNameP.setText("@"+user.screenName);
                    tvSinceP.setText("On Twitter since " + user.createdAt);
                    tvFollowersCount.setText(Long.toString(user.followersCount));
                    tvFollowingsCount.setText(Long.toString(user.followingsCount));
                    tvUserDescription.setText(user.description);
                    // add the images
                    Glide.with(getBaseContext()).load(user.profileImageUrl)
                            .bitmapTransform(new RoundedCornersTransformation(getBaseContext(), 2000, 0))
                            .placeholder(R.drawable.ic_person_v1_svg)
                            .error(R.drawable.ic_person_v1_svg)
                            .into(ivProfileImageP);

                    Glide.with(getBaseContext()).load(user.profileBackgroundImageUrl)
                            .placeholder(Integer.parseInt(user.profileBackgroundColor, 16)+0xFF000000)
                            .error(Integer.parseInt(user.profileBackgroundColor, 16)+0xFF000000)
                            .into(bannerImage);


                } catch (JSONException e) {
                    onError(1, null);
                    e.printStackTrace();
                }

                // Set up for adapter then populate it with tweets with the length of totalTweets
                reinitializeTweetsAndAdapter();
                populateUserTimeline(user.screenName, totalTweets);
            }
        });

        scrollProfile.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollProfile.getScrollY(); // For ScrollView
                int newRadius = 100 - scrollY < 0 ? 0 : 100 - scrollY;

                //if (scrollY > 200) { actionBar.show();
                //} else { actionBar.hide();
                //}

            }
        });

    }

    public void reinitializeTweetsAndAdapter(){
        pb.setVisibility(ProgressBar.VISIBLE); // set the progress bar to visible
        tweets = new ArrayList<>(); // init the array list (data source)
        tweetAdapter = new TwitterAdapterProfile(tweets); // construct adapter from data source
        // tweetAdapter.max_id = max_id_from_timeline; // TEMP
        rvTweetsP.setLayoutManager(layoutManager); // RecyclerView setup (layout manager, use adapter)
        rvTweetsP.setAdapter(tweetAdapter); // set the Adapter

        Log.d(TAG, String.format("Tweets reinitialized at %s", HMS.format(CAL.getTime())));
    }

    private void populateUserTimeline(String userScreenName, int count) {
        // here, the progress bar is not set to visible because this function is always
        // called after reinitializeTweetsAndAdapter(), which makes the progress bar visible
        // so we get straight into it with the client.getHomeTimeline
        // MAX UID (2nd parameter) is to be changed later
        client.getUserTimeline(userScreenName, count, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // FOR JSON OBJECTS
                Log.d(client.TAG, response.toString());
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // iterate through the JSON array, for each entry deserialize the JSON Object
                Log.d(TAG, String.format("populateUserTimeline | RESPONSE LENGTH %s SUCCESS at %s", response.length(),  HMS.format(CAL.getTime())));

                for (int i = 0; i < response.length(); i++){
                    // convert each object into a tweet model inserted in the following tweet object
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
                    Log.d(TAG, String.format("populateUserTimeline | Tweet at index %s SUCCESS at %s", i,  HMS.format(CAL.getTime())));
                }
                Log.d(TAG, String.format("populateUserTimeline SUCCESS at %s", HMS.format(CAL.getTime())));
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(client.TAG, String.format("REGULAR FAILURE: %s" , responseString));
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the user tweets. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(client.TAG, String.format("JSON OBJECT FAILURE: %s", errorResponse.toString()));
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the user tweets. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.e(client.TAG, String.format("JSON ARRAY FAILURE: %s" ,errorResponse.toString()));
                throwable.printStackTrace();

                Toast.makeText(getBaseContext(), String.format("An error occurred while acquiring the user tweets. Please try again in 2 minutes."), Toast.LENGTH_LONG).show();
                pb.setVisibility(ProgressBar.INVISIBLE); // remove the progress bar
            }
        });
    }

    public void onError(int code, String message) {
        String onErrorString = "onError error occurred";
        switch (code) {
            case 0: {
                // error while parsing the userId
                Toast.makeText(getBaseContext(), String.format("Error occurred while parsing the userId. %s", message), Toast.LENGTH_LONG).show();
                break;
            }
            case 1: {
                // error while parsing the json from twitter server response
                Toast.makeText(getBaseContext(), String.format("Error occurred while while parsing the json from the twitter server response. %s", message), Toast.LENGTH_LONG).show();
                break;
            }
            case 2: {
                // error while sending a request to twitter server
                Toast.makeText(getBaseContext(), String.format("Error occurred while while sending a request to the twitter server. %s", message), Toast.LENGTH_LONG).show();
                break;
            }
            default: {
                // unidentified error
                Toast.makeText(getBaseContext(), String.format("Unidentified error occured."), Toast.LENGTH_LONG).show();
                break;
            }
        }
        Log.e(TAG, String.format("%s at %s: %s", onErrorString, code, message));
        finish(); // finish because bad error
    }


    // Toast statement skeleton
    // Toast.makeText(getBaseContext(), String.format("%s"), Toast.LENGTH_LONG).show();
}


