package com.github.invghost.neostream;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

class VideoAdapter extends BaseAdapter {
    Context context;
    ArrayList<TwitchVideo> data;
    static LayoutInflater inflater = null;

    VideoAdapter(Context context) {
        this.context = context;
        this.data = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void add(TwitchVideo video) {
        data.add(video);
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
            vi = inflater.inflate(R.layout.list_item_video, null);

        TextView channelText = (TextView)vi.findViewById(R.id.streamTitle);
        channelText.setText(data.get(position).title);

        TextView gameText = (TextView)vi.findViewById(R.id.gameTitle);
        if(data.get(position).game != null)
            gameText.setText(data.get(position).game);

        ImageView thumbnailView = (ImageView)vi.findViewById(R.id.streamThumbnail);
        Glide.with(context).load(data.get(position).thumbnailURL).crossFade().into(thumbnailView);
        thumbnailView.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);

        ImageView gameCoverView = (ImageView)vi.findViewById(R.id.streamGameCover);
        Glide.with(context).load("https://static-cdn.jtvnw.net/ttv-boxart/" + data.get(position).game.replaceAll(" ", "%20") + "-136x190.jpg").crossFade().into(gameCoverView);

        return vi;
    }
}
