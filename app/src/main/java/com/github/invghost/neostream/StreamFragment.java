package com.github.invghost.neostream;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

class PlayStreamTask extends AsyncTask<String, Void, String> {
    private FragmentActivity fragment;

    PlayStreamTask(FragmentActivity fragment) {
        this.fragment = fragment;
    }

    protected String doInBackground(String... urls) {
        String URI = TwitchAPI.GetStreamURI(urls[0], urls[1]);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(URI), "video/flv");
        fragment.startActivity(intent);

        return "dummy";
    }
}

public class StreamFragment extends Fragment {
    private TwitchChannel channel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.channel = getArguments().getParcelable("channel");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stream, container, false);

        if(channel.hosting == null)
            view.findViewById(R.id.host_alert).setVisibility(View.INVISIBLE);
        else
            ((TextView)view.findViewById(R.id.host_alert_message)).setText(getContext().getString(R.string.host_alert, channel.displayName, channel.hosting.displayName));

        TextView statusText = (TextView)view.findViewById(R.id.streamStatus);
        if(channel.stream == null && channel.hosting == null)
            statusText.setText(getString(R.string.channel_offline));

        TextView titleText = (TextView)view.findViewById(R.id.streamTitle);
        if(channel.hosting == null)
            titleText.setText(channel.status);
        else
            titleText.setText(channel.hosting.status);

        TextView gameText = (TextView)view.findViewById(R.id.gameTitle);
        if(channel.hosting == null)
            gameText.setText(channel.game);
        else
            gameText.setText(channel.hosting.game);

        String username = channel.username;
        if(channel.hosting != null)
            username = channel.hosting.username;

        if(channel.hosting == null && channel.stream == null) {
            ImageView playButton = (ImageView)view.findViewById(R.id.streamPlayButton);
            playButton.setBackground(null);
        }

        ImageView imageView = (ImageView)view.findViewById(R.id.streamThumbnail);
        Glide.with(getContext()).load("https://static-cdn.jtvnw.net/previews-ttv/live_user_" + username + "-1280x720.jpg").crossFade().into(imageView);

        imageView.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);

        if(channel.hosting != null || channel.stream != null) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("use_external_player", true)) {
                        //String quality = ((Spinner) view.findViewById(R.id.qualitySpinner)).getSelectedItem().toString();

                        new PlayStreamTask(getActivity()).execute(channel.username, "chunked");
                    } else {
                        PlayerFragment fragment = new PlayerFragment();

                        Bundle bundle = new Bundle();
                        if (channel.hosting != null) {
                            bundle.putString("channel", channel.hosting.username);
                            bundle.putString("title", channel.hosting.status);
                        } else {
                            bundle.putString("channel", channel.username);
                            bundle.putString("title", channel.status);
                        }

                        bundle.putBoolean("video", false);

                        fragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).addToBackStack(null).commit();
                    }
                }
            });
        }

        String game = channel.game;
        if(channel.hosting != null)
            game = channel.hosting.game;

        ImageView gameCoverView = (ImageView)view.findViewById(R.id.streamGameCover);
        Glide.with(getContext()).load("https://static-cdn.jtvnw.net/ttv-boxart/" + game.replaceAll(" ", "%20") + "-136x190.jpg").crossFade().into(gameCoverView);

        Button shareButton = (Button)view.findViewById(R.id.streamShareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = channel.username;
                String displayName = channel.displayName;

                if(channel.hosting != null) {
                    username = channel.hosting.username;
                    displayName = channel.hosting.displayName;
                }

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "https://www.twitch.tv/" + username);
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, displayName + " Twitch stream");

                startActivity(Intent.createChooser(intent, "Share"));
            }
        });

        return view;
    }
}
