package com.github.invghost.neostream;

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

import java.util.Set;

class CheckOnlineTask extends AsyncTask<String, Void, TwitchChannel> {
    private ChannelAdapter adapter;

    CheckOnlineTask(ChannelAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected TwitchChannel doInBackground(String... params) {
        TwitchChannel channel = TwitchAPI.GetChannel(params[0]);
        channel.stream = TwitchAPI.GetStream(params[0]); //TODO: this is bad, why why why are we doing this!

        return channel;
    }

    @Override
    protected void onPostExecute(TwitchChannel channel) {
        super.onPostExecute(channel);

        adapter.add(channel);
        adapter.notifyDataSetChanged();
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

        getActivity().setTitle(R.string.app_name);

        final ListView followedChannelsListView = (ListView)view.findViewById(R.id.following_status_list);
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
        FollowingChannelAdapter adapter = new FollowingChannelAdapter(getContext());

        ListView followedChannelsListView = (ListView)getView().findViewById(R.id.following_status_list);
        followedChannelsListView.setAdapter(adapter);

        SharedPreferences settings = getContext().getSharedPreferences("MyPrefs", 0);
        Set<String> followedStreamers = settings.getStringSet("followed", null);
        if(followedStreamers != null) {
            for (String streamer : followedStreamers) {
                new CheckOnlineTask(adapter).execute(streamer);
            }
        }
    }
}
