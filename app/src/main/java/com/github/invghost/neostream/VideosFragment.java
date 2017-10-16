package com.github.invghost.neostream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

class LoadVideosTask extends AsyncTask<String, Void, ArrayList<TwitchVideo>> {
    private VideoAdapter adapter;

    LoadVideosTask(VideoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected ArrayList<TwitchVideo> doInBackground(String... params) {
        return TwitchAPI.GetVideos(params[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<TwitchVideo> videos) {
        super.onPostExecute(videos);

        adapter.data = videos;
        adapter.notifyDataSetChanged();
    }
}

public class VideosFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        VideoAdapter adapter = new VideoAdapter(getContext());

        ListView videoList = (ListView)view.findViewById(R.id.videoList);
        videoList.setAdapter(adapter);

        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TwitchVideo video = (TwitchVideo) parent.getItemAtPosition(position);

                PlayerFragment fragment = new PlayerFragment();

                Bundle bundle = new Bundle();
                bundle.putString("channel", video.username);
                bundle.putString("title", video.title);
                bundle.putString("videoid", video.id);
                bundle.putBoolean("video", true);

                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).addToBackStack(null).commit();
            }
        });

        TwitchChannel channel = getArguments().getParcelable("channel");
        new LoadVideosTask(adapter).execute(channel.username);

        return view;
    }
}
