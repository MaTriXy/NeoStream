package com.github.invghost.neostream;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

class FollowingChannelAdapter extends ChannelAdapter {
    FollowingChannelAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi;

        boolean isOnline = data.get(position).stream != null;
        boolean isHosting = data.get(position).hosting != null;

        //TODO: this is a LOT of duplicate code :/
        if (isOnline) {
            vi = inflater.inflate(R.layout.list_item_stream, null);

            TextView titleText = (TextView)vi.findViewById(R.id.lblTitle);
            titleText.setText(data.get(position).status);

            TextView gameText = (TextView)vi.findViewById(R.id.lblGame);
            gameText.setText(data.get(position).game);

            TextView channelText = (TextView)vi.findViewById(R.id.lblChannel);
            channelText.setText(data.get(position).displayName);

            TextView viewsText = (TextView)vi.findViewById(R.id.lblViewers);
            viewsText.setText(Utility.formatNumber(data.get(position).stream.viewers));

            ImageView imageView = (ImageView)vi.findViewById(R.id.imgThumbnail);

            Glide.with(context).load(data.get(position).stream.thumbnailURL).into(imageView);
        } else if(isHosting) {
            vi = inflater.inflate(R.layout.list_item_stream, null);

            TextView titleText = (TextView)vi.findViewById(R.id.lblTitle);
            titleText.setText(data.get(position).hosting.status);

            TextView gameText = (TextView)vi.findViewById(R.id.lblGame);
            gameText.setText(data.get(position).hosting.game);

            TextView channelText = (TextView)vi.findViewById(R.id.lblChannel);
            channelText.setText(data.get(position).displayName + " hosting " + data.get(position).hosting.displayName);

            TextView viewsText = (TextView)vi.findViewById(R.id.lblViewers);
            viewsText.setText(Utility.formatNumber(data.get(position).hosting.stream.viewers));

            ImageView imageView = (ImageView)vi.findViewById(R.id.imgThumbnail);

            Glide.with(context).load(data.get(position).hosting.stream.thumbnailURL).into(imageView);
        } else {
            vi = inflater.inflate(R.layout.list_item_channel, null);

            TextView channelText = (TextView)vi.findViewById(R.id.lblChannel);
            channelText.setText(data.get(position).displayName);

            ImageView imageView = (ImageView)vi.findViewById(R.id.imgIcon);

            String url = data.get(position).logoURL;
            if(url == null)
                url = data.get(position).offlineBannerURL;

            Glide.with(context).load(url).crossFade().into(imageView);
        }

        return vi;
    }
}
