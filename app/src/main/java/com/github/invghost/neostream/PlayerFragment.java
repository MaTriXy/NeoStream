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
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            WebView playerWebView = ((MainActivity) getActivity()).playerWebView;

            if(getArguments().getBoolean("video"))
                playerWebView.loadUrl("https://player.twitch.tv/?video=" + getArguments().getString("videoid") + "&allowfullscreen=false");
            else
                playerWebView.loadUrl("https://player.twitch.tv/?channel=" + getArguments().getString("channel") + "&allowfullscreen=false");

            if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enable_chat", true)) {
                WebView chatWebView = ((MainActivity) getActivity()).chatWebView;

                String chatHTML = "<html><body style=\"margin: 0px;\"><iframe frameborder=\"0\"\n" +
                        "        scrolling=\"yes\"\n" +
                        "        src=\"https://www.twitch.tv/" + getArguments().getString("channel") + "/chat?popout=\"\n" +
                        "        style=\"width: 100%; height: 100%;\"\n" +
                        "></body></html>\n";

                chatWebView.loadData(chatHTML, "text/html", "UTF-8");
            }

            getActivity().setTitle(getArguments().getString("channel"));
        }

        setHasOptionsMenu(false);
        ((MainActivity)getActivity()).getSupportActionBar().hide();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        WebView playerWebView = ((MainActivity)getActivity()).playerWebView;

        LinearLayout playerContainer = (LinearLayout)view.findViewById(R.id.player_container);

        if(playerWebView.getParent() != null)
            ((ViewGroup)playerWebView.getParent()).removeView(playerWebView);

        playerWebView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        playerContainer.addView(playerWebView);

        LinearLayout chatContainer = (LinearLayout)view.findViewById(R.id.chat_container);
        if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enable_chat", true) && !getArguments().getBoolean("video")) {
            WebView chatWebView = ((MainActivity)getActivity()).chatWebView;

            if(chatWebView.getParent() != null)
                ((ViewGroup)chatWebView.getParent()).removeView(chatWebView);

            chatWebView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            chatContainer.addView(chatWebView);
        } else {
            View infoView = inflater.inflate(R.layout.fragment_stream_info, container, false);

            infoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            chatContainer.addView(infoView);

            TextView titleText = (TextView)chatContainer.findViewById(R.id.infoStreamTitle);
            titleText.setText(getArguments().getString("title"));
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_player, menu);
    }

    static ViewGroup.LayoutParams oldPlayerLayoutParams = null;
    static ViewGroup.LayoutParams oldChatLayoutParams = null;

    static int oldVisibility = 0;

    private void showSystemUi() {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(oldVisibility);}

    private void hideSystemUi() {
        oldVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
        int visibility =
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getActivity().getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(getView() == null)
            return;

        LinearLayout playerContainer = (LinearLayout)getView().findViewById(R.id.player_container);
        LinearLayout chatContainer = (LinearLayout)getView().findViewById(R.id.chat_container);

        // Checks the orientation of the screen
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            oldPlayerLayoutParams = playerContainer.getLayoutParams();
            oldChatLayoutParams = chatContainer.getLayoutParams();

            chatContainer.setVisibility(View.GONE);

            playerContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            hideSystemUi();
        } else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showSystemUi();

            chatContainer.setVisibility(View.VISIBLE);

            playerContainer.setLayoutParams(oldPlayerLayoutParams);
            chatContainer.setLayoutParams(oldChatLayoutParams);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((MainActivity)getActivity()).playerWebView.loadUrl("");
        ((MainActivity)getActivity()).chatWebView.loadUrl("");

        ((MainActivity)getActivity()).getSupportActionBar().show();
    }
}
