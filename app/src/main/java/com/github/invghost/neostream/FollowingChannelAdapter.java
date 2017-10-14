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
        View vi = convertView;

        if (vi == null)
            vi = inflater.inflate(R.layout.list_item_channel, null);

        TextView channelText = (TextView)vi.findViewById(R.id.lblChannel);
        channelText.setText(data.get(position).displayName);

        ImageView imageView = (ImageView)vi.findViewById(R.id.imgThumbnail);

        if(data.get(position).stream != null) {
            ((ImageView)vi.findViewById(R.id.imgStatus)).setImageDrawable(context.getResources().getDrawable(R.drawable.icon_online));

            Glide.with(context).load(data.get(position).stream.thumbnailURL).crossFade().into(imageView);
        } else {
            ((ImageView)vi.findViewById(R.id.imgStatus)).setImageDrawable(context.getResources().getDrawable(R.drawable.icon_offline));

            String url = data.get(position).logoURL;
            if(url == null)
                url = data.get(position).offlineBannerURL;

            Glide.with(context).load(url).crossFade().into(imageView);
        }

        return vi;
    }
}
