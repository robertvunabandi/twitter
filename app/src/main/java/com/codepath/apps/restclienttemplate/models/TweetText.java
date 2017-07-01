package com.codepath.apps.restclienttemplate.models;

import java.util.Arrays;
import java.util.List;

/**
 * Created by robertvunabandi on 6/29/17.
 */

public class TweetText {
    public String HTMLFONTOPEN = "<font color='#248ff1'>"; // color highlight
    public String HTMLFONTOPENHASHTAG = "<font color='#d4e9fc'>"; // color highlight
    public String HTMLFONTCLOSE = "</font>";
    public String finalText;

    public TweetText(String text) {
        setTweet(text);
    }

    public void setTweet(String tweet) {
        String[] words = tweet.split("\\s+");
        int l = words.length;
        for (int i = 0; i < l; i++) {
            String replacement = userIntoHTMLBlue(words[i]);
            words[i] = replacement;
        }
        List<String> list = Arrays.asList(words);
        finalText = join(list, " ");
    }

    public String userIntoHTMLBlue(String userText) {
        if (userText.charAt(0) == '@') {
            return HTMLFONTOPEN + userText + HTMLFONTCLOSE;
        } else if (userText.charAt(0) == '#') {
            return HTMLFONTOPENHASHTAG + userText + HTMLFONTCLOSE;
        } else {
            return userText;
        }
    }

    public static String join(List<String> list, String delimeter) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                first = false;
            } else {
                sb.append(delimeter);
            }
            sb.append(item);
        }
        return sb.toString();
    }
}
