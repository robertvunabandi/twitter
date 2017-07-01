package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {
    private TwitterClient client;
    // private final int REQUEST_CODE = 200;

    public TextView tvCharacterCount;
    public EditText etTweet;
    public Button bTweet;
    private ProgressBar pb;
    public String text;
    public long reply_uid;

    public static final String TAG = "ComposeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // pick up different views
        tvCharacterCount = (TextView) findViewById(R.id.tvCharacterCount);
        etTweet = (EditText) findViewById(R.id.etTweet);
        bTweet = (Button) findViewById(R.id.bTweet);
        pb = (ProgressBar) findViewById(R.id.pbLoading);

        // get the text sent from the intent, this text could be a user to reply to
        text = getIntent().getStringExtra("text");
        getSupportActionBar().setTitle("New Tweet");
        if (text != null){
            // I know I need to better check, but when i had "!text.isEmpty() &&", it would crash the app
            // in case the text is not empty (meaning no reply), then we set the text of etTweet to that text (for replies)
            etTweet.setText("@"+text+" ");
            //place the cursor at the end of the text
            etTweet.setSelection(etTweet.getText().length());
            reply_uid = getIntent().getLongExtra("reply_uid", 0);
            getSupportActionBar().setTitle("In reply to "+ text);
        }

        // set the client up
        client = TwitterApp.getRestClient();

        etTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = 140 - s.length();
                tvCharacterCount.setText(Integer.toString(length));
                //String redLightString = getString(R.color.colorRedLight).substring(1);

                int redLight = ContextCompat.getColor(getApplicationContext(), R.color.colorRedLight);
                int redDark = ContextCompat.getColor(getApplicationContext(), R.color.colorRed);
                int blueLight = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDarkLight);
                int whiteColor = ContextCompat.getColor(getApplicationContext(), R.color.colorWhite);

                // Log.i(TAG, String.format("Hey BUTTON %s", bTweet.getText()));
                if (length < 20) {
                    tvCharacterCount.setTextColor(redLight);
                    if (length < 0) {
                        bTweet.setEnabled(false);
                        bTweet.setTextColor(redLight);
                        bTweet.setBackgroundColor(redDark); // 0xFF992020
                    } else {
                        bTweet.setEnabled(true);
                        bTweet.setTextColor(whiteColor);
                        bTweet.setBackgroundColor(blueLight); // 0xFF173144
                    }
                } else {
                    tvCharacterCount.setTextColor(whiteColor);
                }
            }
        });
    }

    public void onSubmit(View v) {
        pb.setVisibility(ProgressBar.VISIBLE); // set the progress bar to visible
        EditText tweet = (EditText) findViewById(R.id.etTweet);
        // String text = String.valueOf(tweet.getText());


        client.sendTweet(tweet.getText().toString(), reply_uid, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // super.onSuccess(statusCode, headers, response);

                Tweet tweet;
                // data.putExtra("code", REQUEST_CODE);
                try {
                    Intent data = new Intent();
                    tweet = Tweet.fromJSON(response);

                    // after getting the tweet parced, we parcialize it with Parcels.wrap(tweet)
                    data.putExtra("tweet", Parcels.wrap(tweet));
                    if (text != null) {
                        // if the text is not null, then we start from another activity
                        Intent timelineIntent = new Intent(ComposeActivity.this, TimelineActivity.class);
                        startActivity(timelineIntent);
                    } else {
                        setResult(RESULT_OK, data);
                        finish();
                    }

                    // bc we start from 0, the total number of tweets added to our array list will be the size of tweets - 1
                } catch (JSONException e) {
                    //data.putExtra("code", 404);
                    //setResult(RESULT_CANCELED, data);
                    e.printStackTrace();
                }

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                 super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e(client.TAG, "ERRR ! !" + errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                // super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e(client.TAG, "ERRR ( (" + errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // super.onFailure(statusCode, headers, responseString, throwable);
                Log.e(client.TAG, "ERRR < <" + responseString.toString());
                throwable.printStackTrace();
            }
        });

        // closes the activity and returns to first screen

    }
}
