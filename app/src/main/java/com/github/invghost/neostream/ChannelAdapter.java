package com.github.invghost.neostream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

class ChannelAdapter extends BaseAdapter {
    Context context;
    ArrayList<TwitchChannel> data;
    static LayoutInflater inflater = null;

    ChannelAdapter(Context context) {
        this.context = context;
        this.data = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void add(TwitchChannel channel) {
        data.add(channel);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if (vi == null)
            vi = inflater.inflate(R.layout.list_item_channel, null);

        TextView channelText = vi.findViewById(R.id.lblChannel);
        channelText.setText(data.get(position).displayName);

        String url = data.get(position).logoURL;
        if(url == null)
            url = data.get(position).offlineBannerURL;

        ImageView imageView = vi.findViewById(R.id.imgIcon);
        Glide.with(context).load(url).crossFade().into(imageView);

        return vi;
    }
}
