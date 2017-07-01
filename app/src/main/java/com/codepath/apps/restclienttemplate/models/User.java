package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

/**
 * Created by robertvunabandi on 6/26/17.
 */

@Parcel
public class User {
    // list attributes
    public String name, screenName, profileImageUrl;
    public long uid;

    public User() {
    }

    // deserialize the JSON
    public static User fromJSON(JSONObject jsonObject) throws JSONException {
        User user = new User();

        // extract the values
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        user.profileImageUrl = jsonObject.getString("profile_image_url").replace("_normal","");
        user.uid = jsonObject.getLong("id");

        return user;
    }
}
