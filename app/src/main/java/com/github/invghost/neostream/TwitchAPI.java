package com.github.invghost.neostream;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TwitchStream {
    TwitchChannel channel;

    int viewers;
    String thumbnailURL;
}

class TwitchChannel implements Parcelable {
    TwitchChannel() {}

    private TwitchChannel(Parcel in) {
        id = in.readInt();
        username = in.readString();
        displayName = in.readString();
        status = in.readString();
        game = in.readString();
        offlineBannerURL = in.readString();
        logoURL = in.readString();
        bannerURL = in.readString();
        description = in.readString();
    }

    int id;
    String username, displayName, status, game;
    String offlineBannerURL, logoURL;
    String bannerURL;
    String description;

    //channel that its hosting, if any
    TwitchChannel hosting;

    //DO NOT USE
    //ONLY USED BY FOLLOWED CHANNELS
    TwitchStream stream = null;

    public static final Creator<TwitchChannel> CREATOR = new Creator<TwitchChannel>() {
        @Override
        public TwitchChannel createFromParcel(Parcel in) {
            return new TwitchChannel(in);
        }

        @Override
        public TwitchChannel[] newArray(int size) {
            return new TwitchChannel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(username);
        dest.writeString(displayName);
        dest.writeString(status);
        dest.writeString(game);
        dest.writeString(offlineBannerURL);
        dest.writeString(logoURL);
        dest.writeString(bannerURL);
        dest.writeString(description);
    }
}

class TwitchVideo {
    String id;
    String title, game, username;
    String thumbnailURL;
}

class TwitchAPI {
    static TwitchChannel GetChannel(String username) {
        try {
            URL url = new URL("https://api.twitch.tv/kraken/channels/" + URLEncoder.encode(username, "UTF-8"));
            JSONObject json = GetJSON(url);

            if(json != null) {
                return GetChannel(json);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static TwitchChannel GetChannel(JSONObject channelObject) {
        TwitchChannel channel = new TwitchChannel();

        try {
            channel.username = channelObject.getString("name");
            channel.displayName = channelObject.getString("display_name");
            channel.status = channelObject.getString("status");
            channel.game = channelObject.getString("game");

            if(channelObject.has("channel_id"))
                channel.id = channelObject.getInt("channel_id");
            else if(channelObject.has("id"))
                channel.id = Integer.parseInt(channelObject.getString("id"));
            else if(channelObject.has("_id"))
                channel.id = channelObject.getInt("_id");

            if(!channelObject.isNull("logo"))
                channel.logoURL = channelObject.getString("logo");

            if(!channelObject.isNull("video_banner"))
                channel.offlineBannerURL = channelObject.getString("video_banner");

            if(!channelObject.isNull("profile_banner"))
                channel.bannerURL = channelObject.getString("profile_banner");

            String hostingUsername = CheckIfHosting(channel);
            if(hostingUsername != null)
                channel.hosting = GetChannel(hostingUsername);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return channel;
    }

    static TwitchStream GetStream(String username) {
        try {
            URL url = new URL("https://api.twitch.tv/kraken/streams/" + URLEncoder.encode(username, "UTF-8"));
            JSONObject json = GetJSON(url);

            if(json != null) {
                if(json.isNull("stream"))
                    return null;

                return GetStream(json.getJSONObject("stream"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static TwitchStream GetStream(JSONObject streamObject) {
        TwitchStream stream = new TwitchStream();

        try {
            stream.channel = GetChannel(streamObject.getJSONObject("channel"));
            stream.thumbnailURL = streamObject.getJSONObject("preview").getString("medium");
            stream.viewers = streamObject.getInt("viewers");
        } catch(Exception e) {
            e.printStackTrace();
        }

        return stream;
    }

    //these old functions below are going to be deprecated in favour of the structures above
    static boolean IsOnline(String username) {
        try {
            URL url = new URL("https://api.twitch.tv/kraken/streams/" + URLEncoder.encode(username, "UTF-8"));
            JSONObject json = GetJSON(url);

            if(json != null) {
                return !json.isNull("stream");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
     See if the channel is actually streaming something new,
     currently only used by live notifications.
     */
    static boolean IsStreamUnique(Context context, String username) {
        SharedPreferences statuses = context.getSharedPreferences("CachedStatuses", 0);
        SharedPreferences games = context.getSharedPreferences("CachedGames", 0);

        String oldGame = statuses.getString(username, "undefined");
        String oldStatus = games.getString(username, "undefined");

        String newGame = GetGame(username);
        String newStatus = GetStatus(username);

        boolean isUnique = (oldGame.equals(newGame) || oldStatus.equals(newStatus));

        //put in new stauses/games/etc
        if(isUnique) {
            SharedPreferences.Editor statusesEditor = statuses.edit();
            SharedPreferences.Editor gamesEditor = games.edit();

            statusesEditor.putString(username, newStatus);
            gamesEditor.putString(username, newGame);

            statusesEditor.apply();
            gamesEditor.apply();
        }

        return isUnique;
    }

    static String GetStatus(String username) {
        try {
            URL url = new URL("https://api.twitch.tv/api/channels/" + URLEncoder.encode(username, "UTF-8"));
            JSONObject json = GetJSON(url);

            if(json != null) {
                return json.getString("status");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "undefined";
    }

    private static String GetGame(String username) {
        try {
            URL url = new URL("https://api.twitch.tv/api/channels/" + URLEncoder.encode(username, "UTF-8"));
            JSONObject json = GetJSON(url);

            if(json != null) {
                return json.getString("game");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "undefined";
    }

    private static String GetAccessToken(String username) {
        try {
            URL url = new URL("https://api.twitch.tv/api/channels/" + URLEncoder.encode(username, "UTF-8") + "/access_token");
            JSONObject json = GetJSON(url);

            if(json != null) {
                return json.getString("token");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "undefined";
    }

    private static String GetSignature(String username) {
        try {
            URL url = new URL("https://api.twitch.tv/api/channels/" + URLEncoder.encode(username, "UTF-8") + "/access_token");
            JSONObject json = GetJSON(url);

            if(json != null) {
                return json.getString("sig");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "undefined";
    }

    static ArrayList<String> GetQualities(String username) {
        ArrayList<String> qualities = new ArrayList<>();

        try {
            //FIXME: HTTP is bad!
            URL url = new URL("http://usher.twitch.tv/api/channel/hls/" + URLEncoder.encode(username, "UTF-8") + ".m3u8?player=twitchweb&token=" +
                    URLEncoder.encode(GetAccessToken(username), "UTF-8") + "&sig=" + URLEncoder.encode(GetSignature(username), "UTF-8") + "&allow_audio=only=true" +
                    "&allow_source=true&type=any");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Client-ID", "zdol60x6sxbx0phk60ci6mki79qhrf");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:53.0) Gecko/20100101 Firefox/53.0");
            urlConnection.setUseCaches(false);

            InputStream inputStream;
            int status = urlConnection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK)
                inputStream = urlConnection.getErrorStream();
            else
                inputStream = urlConnection.getInputStream();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                String line = br.readLine();
                while(line != null)
                {
                    final Pattern pattern = Pattern.compile("^#EXT-X-STREAM-INF:.*VIDEO=\"(?<value>[^\"]*)");

                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String quality = matcher.group(1);

                        qualities.add(quality);
                    }

                    line = br.readLine();
                }

                br.close();
            } finally {
                urlConnection.disconnect();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return qualities;
    }

    static String GetStreamURI(String username, String quality) {
        try {
            //FIXME: HTTP is bad!
            URL url = new URL("http://usher.twitch.tv/api/channel/hls/" + URLEncoder.encode(username, "UTF-8") + ".m3u8?player=twitchweb&token=" +
                    URLEncoder.encode(GetAccessToken(username), "UTF-8") + "&sig=" + URLEncoder.encode(GetSignature(username), "UTF-8") + "&allow_source=true&allow_audio_only=true");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Client-ID", "zdol60x6sxbx0phk60ci6mki79qhrf");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
            urlConnection.setUseCaches(false);

            InputStream inputStream;
            int status = urlConnection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK)
                inputStream = urlConnection.getErrorStream();
            else
                inputStream = urlConnection.getInputStream();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                boolean nextURL = false;

                String line = br.readLine();
                while(line != null)
                {
                    if(nextURL) {
                        return line;
                    } else {
                        final Pattern pattern = Pattern.compile("^#EXT-X-STREAM-INF:.*VIDEO=\"(?<value>[^\"]*)");

                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String streamQuality = matcher.group(1);

                            //this is it!!
                            if (streamQuality.equals(quality)) {
                                nextURL = true;
                            }
                        }
                    }

                    line = br.readLine();
                }

                br.close();
            } finally {
                urlConnection.disconnect();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "undefined";
    }

    static ArrayList<TwitchChannel> SearchChannels(String query) {
        ArrayList<TwitchChannel> results = new ArrayList<>();

        try {
            URL url = new URL("https://api.twitch.tv/kraken/search/channels?query=" + URLEncoder.encode(query, "UTF-8"));
            JSONObject json = GetJSON(url);

            if(json != null) {
                JSONArray resultArray = json.getJSONArray("channels");

                for (int i = 0; i < resultArray.length(); i++) {
                    results.add(GetChannel(resultArray.getJSONObject(i)));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    static ArrayList<TwitchStream> SearchStreams(String query) {
        ArrayList<TwitchStream> results = new ArrayList<>();

        try {
            URL url = new URL("https://api.twitch.tv/kraken/search/streams?query=" + URLEncoder.encode(query, "UTF-8"));
            JSONObject json = GetJSON(url);

            if(json != null) {
                JSONArray resultArray = json.getJSONArray("streams");

                for (int i = 0; i < resultArray.length(); i++) {
                    results.add(GetStream(resultArray.getJSONObject(i)));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private static String CheckIfHosting(TwitchChannel channel) {
        try {
            URL url = new URL("http://tmi.twitch.tv/hosts?include_logins=1&host=" + Integer.toString(channel.id));
            JSONObject json = GetJSON(url);

            if(json != null) {
                JSONArray hostsArray = json.getJSONArray("hosts");

                if(hostsArray.length() > 0 && hostsArray.getJSONObject(0).length() > 0) {
                    if(hostsArray.getJSONObject(0).has("target_display_name"))
                        return hostsArray.getJSONObject(0).getString("target_display_name");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    static ArrayList<TwitchVideo> GetVideos(String username, String filter, String sort, int offset) {
        ArrayList<TwitchVideo> videos = new ArrayList<>();

        try {
            String urlString = "https://api.twitch.tv/kraken/channels/" + URLEncoder.encode(username, "UTF-8") + "/videos?offset=" + Integer.toString(offset);
            if(filter != null)
                urlString += "&broadcast_type=" + filter;

            if(sort != null)
                urlString += "&sort=" + sort;

            URL url = new URL(urlString);
            JSONObject json = GetJSON(url);

            if(json != null) {
                for (int i = 0; i < json.getJSONArray("videos").length(); i++) {
                    TwitchVideo video = new TwitchVideo();
                    video.id = json.getJSONArray("videos").getJSONObject(i).getString("_id");
                    video.title = json.getJSONArray("videos").getJSONObject(i).getString("title");
                    video.game = json.getJSONArray("videos").getJSONObject(i).getString("game");
                    video.thumbnailURL = json.getJSONArray("videos").getJSONObject(i).getString("preview");
                    video.username = username;

                    videos.add(video);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return videos;
    }

    private static JSONObject GetJSON(URL url) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Client-ID", "zdol60x6sxbx0phk60ci6mki79qhrf");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:53.0) Gecko/20100101 Firefox/53.0");
            urlConnection.setUseCaches(false);

            InputStream inputStream;
            int status = urlConnection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK)
                inputStream = urlConnection.getErrorStream();
            else
                inputStream = urlConnection.getInputStream();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine = br.readLine();

                br.close();

                return new JSONObject(inputLine);
            } finally {
                urlConnection.disconnect();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
