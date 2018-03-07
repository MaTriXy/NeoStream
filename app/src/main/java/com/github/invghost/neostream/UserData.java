package com.github.invghost.neostream;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class UserData {
    private final static String followingKeyName = "following";

    static boolean isFollowing(Context context, String username) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> followedStreamers = settings.getStringSet(followingKeyName, new HashSet<String>());

        return followedStreamers.contains(username);
    }

    static ArrayList<String> getFollowing(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> followedStreamers = settings.getStringSet(followingKeyName, new HashSet<String>());

        ArrayList<String> followingList = new ArrayList<>();
        followingList.addAll(followedStreamers);

        return followingList;
    }

    static void addFollow(Context context, String username) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> followedStreamers = new HashSet<>(settings.getStringSet(followingKeyName, new HashSet<String>()));
        followedStreamers.add(username);

        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(followingKeyName, followedStreamers);
        editor.apply();
    }

    static void removeFollow(Context context, String username) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> followedStreamers = new HashSet<>(settings.getStringSet(followingKeyName, new HashSet<String>()));
        followedStreamers.remove(username);

        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(followingKeyName, followedStreamers);
        editor.apply();
    }
}
