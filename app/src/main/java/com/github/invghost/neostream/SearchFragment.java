package com.github.invghost.neostream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

//FIXME: wtf is this duplication
class SearchChannelTask extends AsyncTask<String, Void, ArrayList<TwitchChannel>> {
    private ChannelAdapter adapter;

    SearchChannelTask(ChannelAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected ArrayList<TwitchChannel> doInBackground(String... params) {
        return TwitchAPI.SearchChannels(params[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<TwitchChannel> channels) {
        super.onPostExecute(channels);

        adapter.data = channels;
        adapter.notifyDataSetChanged();
    }
}

class SearchStreamTask extends AsyncTask<String, Void, ArrayList<TwitchStream>> {
    private StreamAdapter adapter;

    SearchStreamTask(StreamAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected ArrayList<TwitchStream> doInBackground(String... params) {
        return TwitchAPI.SearchStreams(params[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<TwitchStream> streams) {
        super.onPostExecute(streams);

        adapter.data = streams;
        adapter.notifyDataSetChanged();
    }
}

enum Filter {
    STREAM,
    CHANNEL
}

public class SearchFragment extends Fragment {
    private ChannelAdapter channelAdapter;
    private StreamAdapter streamAdapter;

    Filter filter;
    int filterItemCheckedId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.channelAdapter = new ChannelAdapter(getContext());
        this.streamAdapter = new StreamAdapter(getContext());

        filter = Filter.CHANNEL;

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle("Search");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);

        ((EditText)view.findViewById(R.id.searchEditText)).setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || (event != null && (
                                event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {

                                search();
                                return true;
                        }
                        return false;
                    }
                });

        changeFilter(null, Filter.CHANNEL, view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        restoreFilterChecked(menu, filterItemCheckedId);
    }

    void search() {
        if(getView() == null)
            return;

        String query = ((EditText)getView().findViewById(R.id.searchEditText)).getText().toString();
        if(query.length() <= 1)
            return;

        switch (filter) {
            case STREAM:
                new SearchStreamTask(streamAdapter).execute(query);
                break;
            case CHANNEL:
                new SearchChannelTask(channelAdapter).execute(query);
                break;
        }
    }

    void changeFilter(MenuItem item, Filter filter, View view) {
        this.filter = filter;

        if(item != null) {
            this.filterItemCheckedId = item.getItemId();
            item.setChecked(true);
        }

        ListView searchResultList = view.findViewById(R.id.searchResultList);

        switch(filter) {
            case CHANNEL:
                searchResultList.setAdapter(channelAdapter);

                ((ListView)view.findViewById(R.id.searchResultList)).setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TwitchChannel channel = (TwitchChannel)parent.getItemAtPosition(position);

                        ChannelFragment fragment = new ChannelFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("channel", channel.username);
                        fragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).addToBackStack(null).commit();
                    }
                });

                break;
            case STREAM:
                searchResultList.setAdapter(streamAdapter);

                ((ListView)view.findViewById(R.id.searchResultList)).setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TwitchStream stream = (TwitchStream)parent.getItemAtPosition(position);

                        ChannelFragment fragment = new ChannelFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("channel", stream.channel.username);
                        fragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).addToBackStack(null).commit();
                    }
                });

                break;
        }
    }

    private void restoreFilterChecked(Menu menu, int itemId) {
        if (itemId != -1) {
            MenuItem item = menu.findItem(itemId);
            if (item == null) return;

            item.setChecked(true);
            switch (itemId) {
                case R.id.menu_filter_stream:
                    filter = Filter.STREAM;
                    break;
                case R.id.menu_filter_channel:
                    filter = Filter.CHANNEL;
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter_stream:
                changeFilter(item, Filter.STREAM, getView());
                search();
                return true;
            case R.id.menu_filter_channel:
                changeFilter(item, Filter.CHANNEL, getView());
                search();
                return true;
            default:
                return false;
        }
    }

}
