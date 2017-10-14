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

class StreamAdapter extends BaseAdapter {
    private Context context;
    ArrayList<TwitchStream> data;
    private static LayoutInflater inflater = null;

    StreamAdapter(Context context) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if (vi == null)
            vi = inflater.inflate(R.layout.list_item_stream, null);

        TextView titleText = (TextView)vi.findViewById(R.id.lblTitle);
        titleText.setText(data.get(position).channel.status);

        TextView gameText = (TextView)vi.findViewById(R.id.lblGame);
        gameText.setText(data.get(position).channel.game);

        TextView channelText = (TextView)vi.findViewById(R.id.lblChannel);
        channelText.setText(data.get(position).channel.displayName);

        TextView viewsText = (TextView)vi.findViewById(R.id.lblViewers);
        viewsText.setText(String.valueOf(data.get(position).viewers));

        ImageView imageView = (ImageView)vi.findViewById(R.id.imgThumbnail);

        Glide.with(context).load(data.get(position).thumbnailURL).into(imageView);

        return vi;
    }
}
