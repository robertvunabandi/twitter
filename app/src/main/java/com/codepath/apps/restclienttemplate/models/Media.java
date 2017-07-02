package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

/**
 * Created by robertvunabandi on 7/1/17.
 */

@Parcel
public class Media {
    public String url, urlHTTPS;
    public long uid;

    public Media() {}

    public static Media fromJSON(JSONArray jsonObject) throws JSONException {
        Media media = new Media();
        JSONObject MED = jsonObject.getJSONObject(0);
        // extract the values
        media.url = MED.getString("media_url");
        media.urlHTTPS = MED.getString("media_url_https");
        media.uid = MED.getLong("id");

        return media;
    }
}
