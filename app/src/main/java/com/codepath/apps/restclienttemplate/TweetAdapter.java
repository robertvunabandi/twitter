package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetText;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by robertvunabandi on 6/26/17.
 */

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {
    static private List<Tweet> mTweets;
    private Context context;
    public long max_id = Long.MAX_VALUE; // lowest id
    // public static final String TAG = "TweetAdapter";
    private static TwitterClient client; // assign a variable for the twitter client

    // pass in the tweet array
    public TweetAdapter(List<Tweet> tweets) {
        mTweets = tweets;
    }
    // for each row, we need to inflate the layout and cache reference into ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        client = TwitterApp.getRestClient(); // get the twitter client

        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    // bind the values based on position of the element
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get data according to position
        Tweet tweet = mTweets.get(position);
        // populate the views according to this data
        holder.tvUserName.setText(tweet.user.name);
        // to change the colors and stuffs
        TweetText finalTweet = new TweetText(tweet.body);

        holder.tvBody.setText(Html.fromHtml(finalTweet.finalText));
        holder.tvCreatedAt.setText(tweet.createdAt);
        holder.tvScreenName.setText("@"+tweet.user.screenName);
        holder.tvRetweets.setText(String.valueOf(tweet.retweetCount)); // set the retweets count
        if (tweet.tweetRetweeted) holder.ivRetweet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_retweet_green));
        holder.tvLikes.setText(String.valueOf(tweet.likeCount)); // set the likes count
        Glide.with(context).load(tweet.user.profileImageUrl)
                .bitmapTransform(new RoundedCornersTransformation(context, 2000, 0))
                .placeholder(R.drawable.ic_face_placeholder)
                .error(R.drawable.ic_face_placeholder)
                .override(2048, 2048)
                .into(holder.ivProfileImage);

        // update the max id of this adapter
        max_id = tweet.uid < max_id ? tweet.uid : max_id;
    }

    // item count
    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    // create ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvUserName, tvBody, tvCreatedAt, tvScreenName;
        public TextView tvRetweets, tvLikes;
        public ImageView ibReply, ivRetweet, ivLike;

        public ViewHolder (View itemView) {
            super(itemView);

             // perform findViewById lookups
            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvBody = (TextView) itemView.findViewById(R.id.tvBodyD);
            tvCreatedAt = (TextView) itemView.findViewById(R.id.tvCreatedAtD);
            tvScreenName = (TextView) itemView.findViewById(R.id.tvScreenName);
            ibReply = (ImageView) itemView.findViewById(R.id.ibReply);

            tvRetweets = (TextView) itemView.findViewById(R.id.tvRetweets);
            tvLikes = (TextView) itemView.findViewById(R.id.tvLikes);
            ivRetweet = (ImageView) itemView.findViewById(R.id.ivRetweet);
            ivLike = (ImageView) itemView.findViewById(R.id.ivLoveTweet);

            tvScreenName.setOnClickListener(this);
            ivProfileImage.setOnClickListener(this);
            tvUserName.setOnClickListener(this);
            tvBody.setOnClickListener(this);
            tvCreatedAt.setOnClickListener(this);
            ibReply.setOnClickListener(this); // listener for reply specifically
            // tvRetweets.setOnClickListener(this);
            // tvLikes.setOnClickListener(this);
            ivRetweet.setOnClickListener(this);
            ivLike.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            int position = getAdapterPosition();
            if (v.getId() == ibReply.getId()) {
                // Toast.makeText(v.getContext(), "ITEM PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
                // if we get the reply click, then we want to create another intent from the context of the item clicked (v.context)
                long reply_uid;

                Intent i = new Intent(context, ComposeActivity.class);
                i.putExtra("text", mTweets.get(getAdapterPosition()).user.screenName);
                reply_uid = mTweets.get(getAdapterPosition()).uid;
                i.putExtra("reply_uid", reply_uid);
                context.startActivity(i);

            } else if (v.getId() == tvUserName.getId() || v.getId() == tvBody.getId() || v.getId() == tvScreenName.getId()) {
                // Send the user to a tweet details class
                Intent i = new Intent(context, TweetDetailsActivity.class);
                // create the variables to be sent
                String tName, tScreenName, tBody, tCreatedAt, tProfileImageUrl, tLikes, tRetweets;

                // get the variables' value and send them to the next activity
                tName = mTweets.get(getAdapterPosition()).user.name;
                tScreenName = mTweets.get(getAdapterPosition()).user.screenName;
                tBody =  mTweets.get(getAdapterPosition()).body;
                tCreatedAt = mTweets.get(getAdapterPosition()).createdAt;
                tProfileImageUrl = mTweets.get(getAdapterPosition()).user.profileImageUrl;
                tLikes = String.valueOf(mTweets.get(getAdapterPosition()).likeCount);
                tRetweets = String.valueOf(mTweets.get(getAdapterPosition()).retweetCount);


                // put each of those variables into the intent
                i.putExtra("tName", tName);
                i.putExtra("tScreenName", tScreenName);
                i.putExtra("tBody", tBody);
                i.putExtra("tCreatedAt", tCreatedAt);
                i.putExtra("tProfileImageUrl", tProfileImageUrl);
                i.putExtra("tLikes", tLikes);
                i.putExtra("tRetweets", tRetweets);
                i.putExtra("tUid", mTweets.get(getAdapterPosition()).uid);
                i.putExtra("tRetweeted", mTweets.get(getAdapterPosition()).tweetRetweeted);
                i.putExtra("tLiked", mTweets.get(getAdapterPosition()).tweetLiked);
                i.putExtra("tPosition", getAdapterPosition());

                // start the activity
                context.startActivity(i);
            } else if (v.getId() == ivLike.getId()) {
                Toast.makeText(v.getContext(), "LIKE NOT IMPLEMENTED", Toast.LENGTH_LONG).show();
                long like_uid = mTweets.get(getAdapterPosition()).uid;
                boolean liked = mTweets.get(getAdapterPosition()).tweetLiked;
                if (!liked) {}
                else {}

            } else if (v.getId() == ivRetweet.getId()) {
                Toast.makeText(v.getContext(), "RETWEET NOT IMPLEMENTED", Toast.LENGTH_LONG).show();
                long retweet_uid = mTweets.get(getAdapterPosition()).uid;
                boolean retweeted = mTweets.get(getAdapterPosition()).tweetRetweeted;

                /* if (!retweeted) {
                    client.retweetTweet(retweet_uid, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet tweet;
                            try {
                                tweet = Tweet.fromJSON(response);
                                // replace the tweet at the position with this new tweet
                                mTweets.remove(getAdapterPosition());
                                mTweets.add(getAdapterPosition(), tweet);
                                notifyAll();
                                // IMPLEMENT;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else {
                    client.unretweetTweet(retweet_uid, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet tweet;
                            try {
                                tweet = Tweet.fromJSON(response);
                                // replace the tweet at the position with this new tweet
                                mTweets.remove(getAdapterPosition());
                                mTweets.add(getAdapterPosition(), tweet);
                                notifyAll();
                                Toast.makeText(v.getContext(), "UNRETWEET", Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } */
            }
            else {
                Toast.makeText(v.getContext(), "ITEM CLICKED NOT IMPLEMENTED AT POSITION " + String.valueOf(getAdapterPosition()), Toast.LENGTH_LONG).show();
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        mTweets.addAll(list);
        notifyDataSetChanged();
    }
}
