package com.github.invghost.neostream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class ChannelTabsAdapter extends FragmentPagerAdapter {
    private TwitchChannel channel;

    ChannelTabsAdapter(TwitchChannel channel, FragmentManager fm) {
        super(fm);
        this.channel = channel;
    }

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;

        switch (index) {
            case 0:
                fragment = new StreamFragment();
                break;
            case 1:
                fragment = new VideosFragment();
                break;
        }

        if(fragment != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("channel", channel);
            fragment.setArguments(bundle);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "Stream";
            case 1:
                return "Videos";
        }

        return super.getPageTitle(position);
    }
}