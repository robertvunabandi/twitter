package com.codepath.apps.restclienttemplate.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by robertvunabandi on 7/3/17.
 */

public class TweetsPagerAdapter extends FragmentPagerAdapter {

    // TODO - Add a search fragment
    private String[] tabTitles = new String[]{"Home", "Mentions"};
    private Context context;

    private HomeTimelineFragment timelineFragment;
    private MentionsTimelineFragment mentionsFragment;

    public TweetsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        timelineFragment = new HomeTimelineFragment();
        mentionsFragment = new MentionsTimelineFragment();
        this.context = context;
    }

    // return the total number of fragments there are (similar to getCount from RecyclerView
    @Override
    public int getCount() {
        return 2;
    }

    // return the fragment to user depending on the position
    @Override
    public Fragment getItem(int position) {
        // if (position == 0) return new HomeTimelineFragment();
        // else if (position == 1) return new MentionsTimelineFragment();

        if (position == 0) return timelineFragment;
        else if (position == 1) return mentionsFragment;
        else return null;
    }

    // return the fragment title
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
