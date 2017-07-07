package com.codepath.apps.restclienttemplate.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TweetAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by robertvunabandi on 7/3/17.
 */

public class TweetsListFragment extends Fragment {

    public final int REQUEST_CODE = 200;
    public static final String TAG = "TweetsListFragment";
    public long max_id = Long.MAX_VALUE; // lowest id

    // for swiping to refresh tweets
    public SwipeRefreshLayout swipeContainer;

    // declare important variables
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    public RecyclerView rvTweets;

    private HomeTimelineFragment timelineFragment;
    private MentionsTimelineFragment mentionFragment;

    // the linear layout
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

    // Timer
    Calendar CAL = Calendar.getInstance();
    SimpleDateFormat HMS = new SimpleDateFormat("HH:mm:ss");

    // inflation happens inside onCreateView
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflate layout
        View v = inflater.inflate(R.layout.fragments_tweets_list, container, false);
        rvTweets = (RecyclerView) v.findViewById(R.id.rvTweet);

        // swipe container
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainerRefresher);
        swipeContainer.setColorSchemeResources(R.color.colorPrimaryDarkLight, R.color.colorLightGrey, R.color.colorAccent, R.color.colorPrimaryLight);

        reinitializeTweetsAndAdapter();
        return v;
    }

    public void reinitializeTweetsAndAdapter(){
        tweets = new ArrayList<>(); // init the array list (data source)
        tweetAdapter = new TweetAdapter(tweets); // construct adapter from data source
        // tweetAdapter.max_id = max_id_from_timeline; // TEMP
        rvTweets.setLayoutManager(layoutManager); // RecyclerView setup (layout manager, use adapter)
        rvTweets.setAdapter(tweetAdapter); // set the Adapter

        Log.d(TAG, String.format("Tweets reinitialized at %s", HMS.format(CAL.getTime())));
    }

    public void addItems(JSONArray response, boolean following){
        for (int i = 0; i < response.length(); i++){
            // convert each object into a tweet model inserted in the following tweet object
            Tweet tweet;
            try {
                tweet = Tweet.fromJSON(response.getJSONObject(i));
                tweet.user.following = following;
                // Log.d(TAG, String.format("New tweet at index %s of response on addItems. MAXID: %s, Tweet's id: %s", i, max_id, tweet.uid));
                // add the tweet model to our data source
                tweets.add(tweet);
                // notify the adapter that we've added an item
                tweetAdapter.notifyItemInserted(tweets.size() - 1);
                // bc we start from 0, the total number of tweets added to our array list will be the size of tweets - 1

                // update the max id of this adapter
                max_id = tweet.uid < max_id ? tweet.uid : max_id;
                Log.d(TAG, String.format("PopulateTimeline | Tweet at index %s SUCCESS at %s", i,  HMS.format(CAL.getTime())));
            } catch (JSONException e) {
                Log.d(TAG, String.format("PopulateTimeline | Tweet at index %s FAILED at %s", i,  HMS.format(CAL.getTime())));
                e.printStackTrace();
            }

        }
        Log.d(TAG, String.format("PopulateTimeline SUCCESS at %s", HMS.format(CAL.getTime())));
    }

    public void addItemOne(Tweet tweet, int position) {
        tweets.add(position, tweet);
        tweetAdapter.notifyItemInserted(position);
        // scrolls back to position 0 to see the tweet
        rvTweets.scrollToPosition(position);
    }

    public long getMaxId() {
        return max_id - 1;
    }
    public int getTweetSize() {
        return tweets.size();
    }

    public String concatTag(String TAG1, String TAG2) {
        return TAG1 + ": " + TAG2;
    }

    private HomeTimelineFragment getTimelineFragment() {
        if (timelineFragment == null) {
            timelineFragment = new HomeTimelineFragment();
        }
        return timelineFragment;
    }

    private MentionsTimelineFragment getMentionFragment() {
        if (timelineFragment == null) {
            mentionFragment = new MentionsTimelineFragment();
        }
        return mentionFragment;
    }
}
