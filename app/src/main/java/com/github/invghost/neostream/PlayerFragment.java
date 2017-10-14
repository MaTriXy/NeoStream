package com.github.invghost.neostream;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class PlayerFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            WebView playerWebView = ((MainActivity) getActivity()).playerWebView;
            WebView chatWebView = ((MainActivity) getActivity()).chatWebView;

            playerWebView.loadUrl("https://player.twitch.tv/?channel=" + getArguments().getString("channel") + "&allowfullscreen=false");

            if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enable_chat", true)) {
                String chatHTML = "<html><body style=\"margin: 0px;\"><iframe frameborder=\"0\"\n" +
                        "        scrolling=\"yes\"\n" +
                        "        src=\"https://www.twitch.tv/" + getArguments().getString("channel") + "/chat?popout=\"\n" +
                        "        style=\"width: 100%; height: 100%;\"\n" +
                        "></body></html>\n";

                chatWebView.loadData(chatHTML, "text/html", "UTF-8");
            }

            getActivity().setTitle(getArguments().getString("channel"));
        }

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        WebView playerWebView = ((MainActivity)getActivity()).playerWebView;
        WebView chatWebView = ((MainActivity)getActivity()).chatWebView;

        FrameLayout playerContainer = (FrameLayout)view.findViewById(R.id.player_container);
        FrameLayout chatContainer = (FrameLayout)view.findViewById(R.id.chat_container);

        if(playerWebView.getParent() != null)
            ((ViewGroup)playerWebView.getParent()).removeView(playerWebView);

        if(chatWebView.getParent() != null)
            ((ViewGroup)chatWebView.getParent()).removeView(chatWebView);

        playerWebView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        playerContainer.addView(playerWebView);

        chatWebView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        chatContainer.addView(chatWebView);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_player, menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        WebView playerWebView = ((MainActivity)getActivity()).playerWebView;

        FrameLayout playerContainer = (FrameLayout)getView().findViewById(R.id.player_container);
        RelativeLayout mainLayout = (RelativeLayout)getView().findViewById(R.id.mainPlayerLayout);

        // Checks the orientation of the screen
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            playerContainer.removeView(playerWebView);

            ((MainActivity) getActivity()).getSupportActionBar().hide();

            getView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

            playerWebView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mainLayout.addView(playerWebView);
        } else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mainLayout.removeView(playerWebView);

            ((MainActivity) getActivity()).getSupportActionBar().show();

            getView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            playerWebView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            playerContainer.addView(playerWebView);
        }
    }
}
