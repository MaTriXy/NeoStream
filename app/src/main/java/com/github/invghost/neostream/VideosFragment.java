package com.github.invghost.neostream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

class LoadVideosTask extends AsyncTask<String, Void, ArrayList<TwitchVideo>> {
    private VideoAdapter adapter;
    private int offset;
    private String filter, sort;

    LoadVideosTask(VideoAdapter adapter, String filter, String sort, int offset) {
        this.adapter = adapter;
        this.filter = filter;
        this.sort = sort;
        this.offset = offset;
    }

    @Override
    protected ArrayList<TwitchVideo> doInBackground(String... params) {
        return TwitchAPI.GetVideos(params[0], filter, sort, offset);
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
    String currentFilter = null, currentSort = null;
    VideoAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        adapter = new VideoAdapter(getContext());

        Spinner filterSpinner = view.findViewById(R.id.videoFilterSpinner);
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(getContext(), R.array.video_types, android.R.layout.simple_spinner_item);

        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = null;
                switch(position) {
                    case 1:
                        filter = "upload";
                        break;
                    case 2:
                        filter = "archive";
                        break;
                    case 3:
                        filter = "highlight";
                        break;
                }

                showVideos(filter, currentSort);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showVideos(null, null);
            }
        });

        Spinner sortSpinner = view.findViewById(R.id.videoSortSpinner);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(getContext(), R.array.video_sorts, android.R.layout.simple_spinner_item);

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sort = null;
                switch(position) {
                    case 0:
                        sort = "time";
                        break;
                    case 1:
                        sort = "views";
                        break;
                }

                showVideos(currentFilter, sort);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showVideos(null, null);
            }
        });

        ListView videoList = view.findViewById(R.id.videoList);
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
                if(channel != null) {
                    currentOffset += 10;
                    new LoadVideosTask(adapter, currentFilter, currentSort, currentOffset).execute(channel.username);
                }
            }
        });

        showVideos(null, null);

        return view;
    }

    void showVideos(String filter, String sort) {
        TwitchChannel channel = getArguments().getParcelable("channel");
        if(channel == null || getView() == null)
            return;

        currentFilter = filter;
        currentSort = sort;

        adapter.data.clear();

        new LoadVideosTask(adapter, currentFilter, currentSort, 0).execute(channel.username);

        ListView videoList = getView().findViewById(R.id.videoList);
        videoList.setSelectionAfterHeaderView();
    }
}
