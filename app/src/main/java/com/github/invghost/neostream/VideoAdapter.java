package com.github.invghost.neostream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View vi = inflater.inflate(R.layout.list_item_video, null);

        TextView channelText = vi.findViewById(R.id.streamTitle);
        channelText.setText(data.get(position).title);

        TextView gameText = vi.findViewById(R.id.gameTitle);
        if(data.get(position).game != null)
            gameText.setText(data.get(position).game);

        ImageView thumbnailView = vi.findViewById(R.id.streamThumbnail);
        Glide.with(context).load(data.get(position).thumbnailURL).diskCacheStrategy(DiskCacheStrategy.NONE).crossFade().into(thumbnailView);
        thumbnailView.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);

        ImageView gameCoverView = vi.findViewById(R.id.streamGameCover);
        Glide.with(context).load("https://static-cdn.jtvnw.net/ttv-boxart/" + data.get(position).game.replaceAll(" ", "%20") + "-136x190.jpg").crossFade().into(gameCoverView);

        Button shareButton = vi.findViewById(R.id.streamShareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "https://www.twitch.tv/videos/" + data.get(position).id);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, data.get(position).title);

                vi.getContext().startActivity(Intent.createChooser(intent, "Share"));
            }
        });

        return vi;
    }
}
