package com.github.invghost.neostream;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

class CheckChannelStatusTask extends AsyncTask<String, Void, TwitchChannel> {
    private Context context;
    private ChannelAdapter adapter, hostingAdapter, offlineAdapter;
    private TextView statusText;

    private static int onlineCount = 0, hostingCount = 0, offlineCount = 0;

    public static void resetCounters() {
        onlineCount = 0;
        hostingCount = 0;
        offlineCount = 0;
    }

    CheckChannelStatusTask(Context context, ChannelAdapter adapter, ChannelAdapter hostingAdapter, ChannelAdapter offlineAdapter, TextView statusText) {
        this.context = context;
        this.adapter = adapter;
        this.hostingAdapter = hostingAdapter;
        this.offlineAdapter = offlineAdapter;
        this.statusText = statusText;
    }

    @Override
    protected TwitchChannel doInBackground(String... params) {
        TwitchChannel channel = TwitchAPI.GetChannel(params[0]);
        channel.stream = TwitchAPI.GetStream(params[0]); //TODO: this is bad, why why why are we doing this!

        if(channel.hosting != null)
            channel.hosting.stream = TwitchAPI.GetStream(channel.hosting.username);

        return channel;
    }

    @Override
    protected void onPostExecute(TwitchChannel channel) {
        super.onPostExecute(channel);

        //online
        if(channel.stream != null && channel.hosting == null) {
            adapter.add(channel);
            adapter.notifyDataSetChanged();

            onlineCount++;
        } else {
            //hosting
            if (channel.stream == null && channel.hosting != null) {
                hostingAdapter.add(channel);
                hostingAdapter.notifyDataSetChanged();

                hostingCount++;
            } else {
                //offline
                offlineAdapter.add(channel);
                offlineAdapter.notifyDataSetChanged();

                offlineCount++;
            }
        }

        statusText.setText(context.getString(R.string.following_statuses, onlineCount, hostingCount, offlineCount));
    }
}

public class HomeFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().setTitle(R.string.home);

        final ListView followedChannelsListView = (ListView)view.findViewById(R.id.online_channels);
        followedChannelsListView.setClickable(true);
        followedChannelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                TwitchChannel channel = (TwitchChannel)arg0.getItemAtPosition(position);

                ChannelFragment fragment = new ChannelFragment();

                Bundle bundle = new Bundle();
                bundle.putString("channel", channel.username);

                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).addToBackStack(null).commit();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RefreshOnline();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.app_bar_search) {
            SearchFragment fragment = new SearchFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).addToBackStack(null).commit();

            return true;
        }
        if(id == R.id.refresh_button) {
            RefreshOnline();

            return true;
        }
        if(id == R.id.action_settings) {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void RefreshOnline() {
        CheckChannelStatusTask.resetCounters();

        FollowingChannelAdapter adapter = new FollowingChannelAdapter(getContext());
        FollowingChannelAdapter hostingAdapter = new FollowingChannelAdapter(getContext());
        FollowingChannelAdapter offlineAdapter = new FollowingChannelAdapter(getContext());

        ListView followedChannelsListView = (ListView)getView().findViewById(R.id.online_channels);
        followedChannelsListView.setAdapter(adapter);

        ListView hostingChannelsListView = (ListView)getView().findViewById(R.id.hosting_channels);
        hostingChannelsListView.setAdapter(hostingAdapter);

        ListView offlineChannelsListView = (ListView)getView().findViewById(R.id.offline_channels);
        offlineChannelsListView.setAdapter(offlineAdapter);

        TextView followingStatuses = (TextView)getView().findViewById(R.id.channelStatuses);

        SharedPreferences settings = getContext().getSharedPreferences("MyPrefs", 0);
        Set<String> followedStreamers = settings.getStringSet("followed", null);
        if(followedStreamers != null) {
            for (String streamer : followedStreamers) {
                new CheckChannelStatusTask(getContext(), adapter, hostingAdapter, offlineAdapter, followingStatuses).execute(streamer);
            }
        }
    }
}
