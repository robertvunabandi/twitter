package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import static com.codepath.apps.restclienttemplate.models.Tweet.getRelativeTimeAgo;

/**
 * Created by robertvunabandi on 6/26/17.
 */

@Parcel
public class User {
    // list attributes
    public String name, screenName, profileImageUrl;
    public long uid;
    // variables for user details
    public String profileImageUrlHTTPS, profileBackgroundImageUrl, profileBackgroundImageUrlHTTPS, profileBackgroundColor, createdAt, description;
    public long followersCount, followingsCount;
    public boolean verifiedUser, followsYou;

    public User() {}

    // deserialize the JSON
    public static User fromJSON(JSONObject jsonObject) throws JSONException {
        User user = new User();

        // extract the values
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        user.profileImageUrl = jsonObject.getString("profile_image_url").replace("_normal","");
        user.profileImageUrlHTTPS = jsonObject.getString("profile_image_url_https").replace("_normal","");
        user.uid = jsonObject.getLong("id");
        // for user detail

        user.description = jsonObject.getString("description");
        user.profileBackgroundImageUrl = jsonObject.getString("profile_background_image_url").replace("_normal","");
        user.profileBackgroundColor = jsonObject.getString("profile_background_color");
        user.createdAt = getRelativeTimeAgo(jsonObject.getString("created_at")); // when the user was first on twitter
        user.followersCount = jsonObject.getLong("followers_count");
        user.followingsCount = jsonObject.getLong("friends_count"); // it could be listed_count as well
        user.verifiedUser = jsonObject.getBoolean("verified");
        user.followsYou = jsonObject.getBoolean("following");

        return user;
    }
}
