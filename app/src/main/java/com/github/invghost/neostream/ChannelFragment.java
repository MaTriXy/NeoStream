package com.github.invghost.neostream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import static com.github.invghost.neostream.Utility.getColor;
import static com.github.invghost.neostream.Utility.getDrawable;

class BlurTransformation extends BitmapTransformation {
    private RenderScript rs;

    BlurTransformation(Context context) {
        super( context );

        rs = RenderScript.create( context );
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap blurredBitmap = toTransform.copy(Bitmap.Config.ARGB_8888, true);

        Allocation input = Allocation.createFromBitmap(
                rs,
                blurredBitmap,
                Allocation.MipmapControl.MIPMAP_FULL,
                Allocation.USAGE_SHARED
        );
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);
        script.setRadius(15);
        script.forEach(output);

        output.copyTo(blurredBitmap);

        toTransform.recycle();

        return blurredBitmap;
    }

    @Override
    public String getId() {
        return "blur";
    }
}

class RetrieveChannelTask extends AsyncTask<String, Void, TwitchChannel> {
    private ChannelFragment fragment;

    RetrieveChannelTask(ChannelFragment fragment) {
        this.fragment = fragment;
    }

    protected TwitchChannel doInBackground(String... urls) {
        TwitchChannel channel = TwitchAPI.GetChannel(urls[0]);
        if(channel != null)
            channel.stream = TwitchAPI.GetStream(urls[0]);

        return channel;
    }

    protected void onPostExecute(TwitchChannel channel) {
        if(fragment == null || fragment.getView() == null || channel == null)
            return;

        ImageView imageView = fragment.getView().findViewById(R.id.channelBanner);

        if(channel.bannerURL != null)
            Glide.with(fragment.getContext()).load(channel.bannerURL).transform(new BlurTransformation(fragment.getContext())).crossFade().into(imageView);

        imageView.setColorFilter(Color.rgb(100, 100, 100), android.graphics.PorterDuff.Mode.MULTIPLY);

        ImageView logoImageView = fragment.getView().findViewById(R.id.channelLogo);
        if(channel.logoURL != null)
            Glide.with(fragment.getContext()).load(channel.logoURL).crossFade().into(logoImageView);

        if(channel.description != null)
            ((TextView)fragment.getView().findViewById(R.id.channelDescription)).setText(channel.description);
        else
            ((TextView)fragment.getView().findViewById(R.id.channelDescription)).setText(fragment.getString(R.string.no_description));

        ((TextView)fragment.getView().findViewById(R.id.channelTitle)).setText(channel.displayName);

        if(channel.stream != null)
            ((TextView)fragment.getView().findViewById(R.id.channelStatus)).setText(fragment.getString(R.string.channel_online, channel.game, channel.stream.viewers));
        else if(channel.hosting != null)
            ((TextView)fragment.getView().findViewById(R.id.channelStatus)).setText(fragment.getString(R.string.channel_hosting, channel.hosting.displayName));
        else
            ((TextView)fragment.getView().findViewById(R.id.channelStatus)).setText(fragment.getString(R.string.channel_offline));

        fragment.getActivity().setTitle(channel.displayName);
        fragment.displayName = channel.displayName;

        ViewPager viewPager = fragment.getView().findViewById(R.id.pager);
        viewPager.setAdapter(new ChannelTabsAdapter(channel, fragment.getChildFragmentManager()));
    }
}

public class ChannelFragment extends Fragment {
    boolean following = false;
    String displayName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new RetrieveChannelTask(this).execute(getArguments().getString("channel"));

        following = UserData.isFollowing(getContext(), getArguments().getString("channel"));
    }

    @Override
    public void onResume() {
        super.onResume();

        new RetrieveChannelTask(this).execute(getArguments().getString("channel"));

        getActivity().setTitle(displayName);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);

        TabLayout tabStrip = view.findViewById(R.id.channelTabLayout);
        tabStrip.setSelectedTabIndicatorColor(getColor(getContext(), R.color.tabText));
        tabStrip.setTabTextColors(getColor(getContext(), R.color.tabText), getColor(getContext(), R.color.tabText));
        tabStrip.setupWithViewPager((ViewPager)view.findViewById(R.id.pager));

        final Button followButton = view.findViewById(R.id.followButton);
        if(following)
            followButton.setBackground(getDrawable(getContext(), R.drawable.ic_favorite_black_24dp));

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(following) {
                    unfollow();
                    followButton.setBackground(getDrawable(getContext(), R.drawable.ic_favorite_border_black_24dp));
                } else {
                    follow();
                    followButton.setBackground(getDrawable(getContext(), R.drawable.ic_favorite_black_24dp));
                }
            }
        });

        return view;
    }

    void follow() {
        UserData.addFollow(getContext(), getArguments().getString("channel"));

        Toast toast = Toast.makeText(getContext(), getString(R.string.followed_toast, getArguments().getString("channel")), Toast.LENGTH_SHORT);
        toast.show();

        following = true;
    }

    void unfollow() {
        UserData.removeFollow(getContext(), getArguments().getString("channel"));

        Toast toast = Toast.makeText(getContext(), getString(R.string.unfollowed_toast, getArguments().getString("channel")), Toast.LENGTH_SHORT);
        toast.show();

        following = false;
    }
}
