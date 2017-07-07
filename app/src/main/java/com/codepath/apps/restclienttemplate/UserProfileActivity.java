package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.fragments.UserTimelineFragment;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
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

    private ProgressBar pb;

    // get the views
    public TextView tvUserNameP, tvScreenNameP, tvSinceP, tvFollowersCount, tvFollowers, tvFollowingsCount, tvFollowings, tvUserDescription, tvFollowsYou;
    public ImageView bannerImage;
    public Button followButton;
    public FloatingActionButton ivProfileImageP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        client = TwitterApp.getRestClient();

        pb = (ProgressBar) findViewById(R.id.pbLoading);

        userId = getIntent().getLongExtra("user_uid", -1);
        screenName = getIntent().getStringExtra("screenName");
        if (userId == -1){
            onError(0, null);
        } else if (screenName == null) {
            onError(0, "Screen name was null");
        }

        // set the title to the screen name
        // getSupportActionBar().setTitle(screenName);

        tvUserNameP = (TextView) findViewById(R.id.tvUserNameP);
        tvScreenNameP = (TextView) findViewById(R.id.tvScreenNameP);
        tvSinceP = (TextView) findViewById(R.id.tvSinceP);
        tvFollowersCount = (TextView) findViewById(R.id.tvFollowersCount);
        tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        tvFollowingsCount = (TextView) findViewById(R.id.tvFollowingsCount);
        tvFollowings = (TextView) findViewById(R.id.tvFollowings);
        tvUserDescription = (TextView) findViewById(R.id.tvUserDescription);
        tvFollowsYou = (TextView) findViewById(R.id.tvFollowsYou);

        followButton = (Button) findViewById(R.id.followButton);

        ivProfileImageP = (FloatingActionButton) findViewById(R.id.ivProfileImageP);
        bannerImage = (ImageView) findViewById(R.id.bannerImage);
        // get the user from the following request
        client.user(userId, screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    user = User.fromJSON(response);

                    // populate the headline, see method below
                    populateUserHeadline(user);

                } catch (JSONException e) {
                    onError(1, null);
                    e.printStackTrace();
                }

                // create the fragment
                UserTimelineFragment userTimelineFragment = UserTimelineFragment.newInstance(screenName);
                // display the user timeline fragment inside the container
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                // make change
                ft.replace(R.id.flContainer, userTimelineFragment);
                // commit
                ft.commit();

                pb.setVisibility(View.INVISIBLE); // remove the progress bar
            }
        });

    }

    public void populateUserHeadline(final User user) {
        // populate the screen with info gathered
        tvUserNameP.setText(user.name);
        tvScreenNameP.setText("@"+user.screenName);
        tvSinceP.setText("On Twitter since " + user.createdAt);
        tvFollowersCount.setText(Long.toString(user.followersCount));
        tvFollowingsCount.setText(Long.toString(user.followingsCount));
        tvUserDescription.setText(user.description);
        if (user.description.length() <= 1) {
            tvUserDescription.setVisibility(View.GONE);
        }
        // add the images
        Glide.with(getBaseContext()).load(user.profileImageUrl)
                .bitmapTransform(new RoundedCornersTransformation(getBaseContext(), 2000, 0))
                .placeholder(R.drawable.ic_person_v1_svg)
                .error(R.drawable.ic_person_v1_svg)
                .into(ivProfileImageP);

        Glide.with(getBaseContext()).load(user.profileBannerUrl)
                .placeholder(Integer.parseInt(user.profileBackgroundColor, 16)+0xFF000000)
                .error(Integer.parseInt(user.profileBackgroundColor, 16)+0xFF000000)
                .into(bannerImage);
        if (getIntent().getBooleanExtra("using_user", false)) {
            followButton.setVisibility(View.GONE);
        } else {
            followButton.setVisibility(View.VISIBLE);
            client.userLookup(screenName, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    Log.e(TAG, response.toString());
                    try {
                        JSONArray connections = response.getJSONObject(0).getJSONArray("connections");
                        for (int i = 0; i < connections.length(); i++){
                            if (connections.get(i).equals("following")) {
                                user.following = true;
                                followButton.setBackground(getResources().getDrawable(R.drawable.button_unfollow));
                                followButton.setTextColor(getResources().getColor(R.color.colorWhite));
                                followButton.setText("Unfollow");
                            }
                            if (connections.get(i).equals("followed_by")) {
                                tvFollowsYou.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, String.format("Error occured while parsing json in unfollow/follow"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    Log.e(TAG, errorResponse.toString());
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e(TAG, errorResponse.toString());
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.e(TAG, responseString);
                }
            });
        }
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle to button to not following (to false) the person (thus to "follow") if we follow the person
                // toggle to button to following (to true) the person (thus to "unfollow") if we don't follow the person
                toggleFollowButton(!user.following);

                // true means we follow the user and want to unfollow them,
                // changes the status of following for the user if good
                followToggle(user.following, screenName, user);
            }
        });

    }

    public void toggleFollowButton(boolean toFollowing) {
        if (toFollowing) {
            followButton.setBackground(getResources().getDrawable(R.drawable.button_unfollow));
            followButton.setTextColor(getResources().getColor(R.color.colorWhite));
            followButton.setText("Unfollow");
        } else {
            followButton.setBackground(getResources().getDrawable(R.drawable.button_follow));
            followButton.setTextColor(getResources().getColor(R.color.colorAccent));
            followButton.setText("Follow");
        }
    }

    public void followToggle(final boolean followsUser, String screenName, final User user) {
        final String text = followsUser ? "unfollowing" : "following";
        client.followToggle(screenName, followsUser, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Toast.makeText(getBaseContext(), String.format("Success %s", text), Toast.LENGTH_SHORT).show();
                // if it worked, change the status of user to the opposite of what it used to be
                user.following = !followsUser;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(client.TAG, String.format("Failure %s: %s", text, errorResponse.toString()));
                throwable.printStackTrace();
                Toast.makeText(getBaseContext(), String.format("Failure %s", text), Toast.LENGTH_SHORT).show();
                toggleFollowButton(followsUser);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.e(client.TAG, String.format("Failure %s: %s", text, errorResponse.toString()));
                throwable.printStackTrace();
                Toast.makeText(getBaseContext(), String.format("Failure %s", text), Toast.LENGTH_SHORT).show();
                toggleFollowButton(followsUser);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(client.TAG, String.format("Failure %s: %s", text, responseString));
                throwable.printStackTrace();
                Toast.makeText(getBaseContext(), String.format("Failure %s", text), Toast.LENGTH_SHORT).show();
                toggleFollowButton(followsUser);
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


