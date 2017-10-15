package com.github.invghost.neostream;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class RetrieveChannelTask extends AsyncTask<String, Void, TwitchChannel> {
    private ChannelFragment fragment;

    RetrieveChannelTask(ChannelFragment fragment) {
        this.fragment = fragment;
    }

    protected TwitchChannel doInBackground(String... urls) {
        return TwitchAPI.GetChannel(urls[0]);
    }

    protected void onPostExecute(TwitchChannel channel) {
        ImageView imageView = (ImageView)fragment.getView().findViewById(R.id.channelBanner);
        if(channel.bannerURL != null)
            Glide.with(fragment.getContext()).load(channel.bannerURL).crossFade().into(imageView);

        ImageView logoImageView = (ImageView)fragment.getView().findViewById(R.id.channelLogo);
        if(channel.logoURL != null)
            Glide.with(fragment.getContext()).load(channel.logoURL).crossFade().into(logoImageView);

        if(channel.description != null)
            ((TextView)fragment.getView().findViewById(R.id.channelDescription)).setText(channel.description);

        fragment.getActivity().setTitle(channel.displayName);
    }
}

class RetrieveQualitiesTask extends AsyncTask<String, Void, ArrayList<String>> {
    private ChannelFragment fragment;

    RetrieveQualitiesTask(ChannelFragment fragment) {
        this.fragment = fragment;
    }

    protected ArrayList<String> doInBackground(String... urls) {
        return TwitchAPI.GetQualities(urls[0]);
    }

    protected void onPostExecute(ArrayList<String> qualities) {
        if(fragment.getView() == null)
            return;

        Spinner spinner = (Spinner)fragment.getView().findViewById(R.id.qualitySpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.getContext(), android.R.layout.simple_spinner_item, qualities);
        spinner.setAdapter(adapter);
    }
}

class PlayStreamTask extends AsyncTask<String, Void, String> {
    private FragmentActivity fragment;

    PlayStreamTask(FragmentActivity fragment) {
        this.fragment = fragment;
    }

    protected String doInBackground(String... urls) {
        String URI = TwitchAPI.GetStreamURI(urls[0], urls[1]);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(URI), "video/flv");
        fragment.startActivity(intent);

        return "dummy";
    }
}

public class ChannelFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new RetrieveChannelTask(this).execute(getArguments().getString("channel"));
        new RetrieveQualitiesTask(this).execute(getArguments().getString("channel"));

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_channel, container, false);

        Button playButton = (Button)view.findViewById(R.id.playStream);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("use_external_player", true)) {
                    String quality = ((Spinner) view.findViewById(R.id.qualitySpinner)).getSelectedItem().toString();

                    new PlayStreamTask(getActivity()).execute(getArguments().getString("channel"), quality);
                } else {
                    PlayerFragment fragment = new PlayerFragment();

                    fragment.setArguments(getArguments());
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).addToBackStack(null).commit();
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_channel, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.channelFollow)
        {
            SharedPreferences settings = getActivity().getSharedPreferences("MyPrefs", 0);
            Set<String> followedStreamers = settings.getStringSet("followed", null);
            if(followedStreamers == null)
                followedStreamers = new HashSet<>();

            followedStreamers.add(getArguments().getString("channel"));

            SharedPreferences.Editor editor = settings.edit();
            editor.putStringSet("followed", followedStreamers);
            editor.apply();

            Context context = getContext();
            CharSequence text = "Followed " + getArguments().getString("channel");
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
