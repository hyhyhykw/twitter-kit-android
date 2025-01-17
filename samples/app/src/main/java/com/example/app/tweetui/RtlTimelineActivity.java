package com.example.app.tweetui;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.app.R;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

public class RtlTimelineActivity extends TweetUiActivity {
    final Locale deviceLocale = Locale.getDefault();

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.rtl_timeline);
        }

        findViewById(android.R.id.content).setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    @Override
    int getLayout() {
        return R.layout.activity_frame;
    }

    @Override
    Fragment createFragment() {
        return RtlTimelineFragment.newInstance();
    }

    @Override
    public void onResume() {
        super.onResume();

        final Locale locale = new Locale("ar");
        Locale.setDefault(locale);
        final Configuration config = getResources().getConfiguration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    public void onPause() {
        super.onPause();

        Locale.setDefault(deviceLocale);
        final Configuration config = getResources().getConfiguration();
        config.locale = deviceLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    /**
     * Fragment showing a Timeline with a list of Rtl Tweets.
     */
    public static class RtlTimelineFragment extends ListFragment {

        public static RtlTimelineFragment newInstance() {
            return new RtlTimelineFragment();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final UserTimeline userTimeline = new UserTimeline.Builder()
                    .screenName("DubaiAirportsAr")
                    .build();
            final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(getActivity())
                    .setTimeline(userTimeline)
                    .build();

            setListAdapter(adapter);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tweetui_timeline, container, false);
        }
    }
}
