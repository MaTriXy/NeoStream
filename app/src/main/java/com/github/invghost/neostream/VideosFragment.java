package com.github.invghost.neostream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

class LoadVideosTask extends AsyncTask<String, Void, ArrayList<TwitchVideo>> {
    private VideoAdapter adapter;
    private int offset;

    LoadVideosTask(VideoAdapter adapter, int offset) {
        this.adapter = adapter;
        this.offset = offset;
    }

    @Override
    protected ArrayList<TwitchVideo> doInBackground(String... params) {
        return TwitchAPI.GetVideos(params[0], offset);
    }

    @Override
    protected void onPostExecute(ArrayList<TwitchVideo> videos) {
        super.onPostExecute(videos);

        adapter.data.addAll(videos);
        adapter.notifyDataSetChanged();
    }
}

public class VideosFragment extends Fragment {
    int currentOffset = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        final VideoAdapter adapter = new VideoAdapter(getContext());

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

        Button loadButton = new Button(getContext());
        loadButton.setText(getString(R.string.load_more));

        videoList.addFooterView(loadButton);

        final TwitchChannel channel = getArguments().getParcelable("channel");

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                currentOffset += 10;
                new LoadVideosTask(adapter, currentOffset).execute(channel.username);
            }
        });

        if(channel != null)
            new LoadVideosTask(adapter, 0).execute(channel.username);

        return view;
    }
}
