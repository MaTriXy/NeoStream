package com.github.invghost.neostream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    public WebView playerWebView, chatWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (savedInstanceState == null) {
            HomeFragment fragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_holder, fragment).commit();
        }

        if(playerWebView == null) {
            playerWebView = new WebView(this);
            playerWebView.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    view.loadUrl("javascript:(function(){" +
                            "document.getElementsByClassName('player-button--twitch')[0].style.visibility='collapse';" +
                            "document.getElementsByClassName('player-button--twitch')[0].style.display='none';" +
                            "document.getElementsByClassName('qa-fullscreen-button')[0].style.display='none';" +
                            "document.getElementsByClassName('player-buttons-right')[0].style = 'padding-right: 1.3em !important';" +
                            "})()");
                }
            });

            playerWebView.getSettings().setJavaScriptEnabled(true);
            playerWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        if(chatWebView == null) {
            chatWebView = new WebView(this);
            chatWebView.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    view.loadUrl("javascript:(function(){" +
                            "document.getElementsByClassName('textarea-contain')[0].style.visibility='collapse';" +
                            "document.getElementsByClassName('js-chat-messages')[0].style = 'height: 55px !important';" +
                            "})()");
                }
            });
            chatWebView.getSettings().setJavaScriptEnabled(true);
            //chatWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
        }

        PollReceiver.scheduleAlarms(this);
    }
}
