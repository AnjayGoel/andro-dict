package com.anjay.dictionary;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Anjay on 06-02-2016.
 */
public class InternetRelated {
    static String lang = "en";
    static String app_name = "Dictionary";
    static boolean auto_detect_language = false;
    static boolean translate_examples = true;
    static String lang_to_translate = "en";
    static ArrayList<String> def_array = new ArrayList<>();
    static ArrayList<String> example_array = new ArrayList<>();

    static void getLanguage(String s) {
        URL get;
        String data = null;
        try {
            get = new URL("https://translate.yandex.net/api/v1.5/tr.json/detect?key=trnsl.1.1.20160218T121452Z.dbac17e2ff4437ce.fb24bbac7655f251a33879a683554379042c41ad&text=" + s);
            InputStreamReader i_read = new InputStreamReader(get.openConnection().getInputStream());
            data = "";
            int char_read;
            while ((char_read = i_read.read()) != -1) {
                data += (char) char_read;
            }
            int start_index = data.lastIndexOf(":") + 2;
            int end_index = data.lastIndexOf("\"");
            lang = data.substring(start_index, end_index);
            if (lang.equals("")) lang = "en";

        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            Thread t = new Thread() {
                @Override
                public void run() {
                    if (lang.equals("en")){
                        DictService.lang_index = 29;
                        return;
                    }
                    for (int index = 1; index < DictService.codes.length; index++) {
                        if (DictService.codes[index].equalsIgnoreCase(InternetRelated.lang)) {
                            DictService.lang_index = index;
                            break;
                        }
                    }
                }
            };
            t.start();
            return;
        }
    }

    static boolean testInet(String site) {
        Socket sock = new Socket();
        InetSocketAddress addr = new InetSocketAddress(site, 80);
        try {
            sock.connect(addr, 3000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                sock.close();
            } catch (IOException ignored) {
            }
        }
    }

    static public String getDef(String word_string, boolean reTranslate) {
        Log.d(app_name, "got word to define: " + word_string);
        if (!testInet("google.com")) {
            Log.d(app_name, "no internet Found");
            fillWithNoExampleFoundDueToInternet();
            return fillWithNoInternetError();
        }

        String dict_data = "";
        try {
            word_string = URLEncoder.encode(word_string, "UTF-8");
            if (auto_detect_language && !reTranslate) getLanguage(word_string);

            URL get = new URL("https://glosbe.com/gapi/translate?from=" + lang + "&dest=" + lang_to_translate + "&format=json&phrase=" + word_string + "&tm=true");

            URLConnection connect = get.openConnection();

            HttpURLConnection con = (HttpURLConnection) connect;

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

            String current_line;

            while ((current_line = in.readLine()) != null) {

                dict_data += current_line;
            }

            con.disconnect();
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        dict_data = dict_data.replace("<strong class=\\\"keyword\\\">", "").replace("</strong>", "").replace("&#39;", "'").replace("&gt;", ">").replace("&lt;", "<").replace("\\\"", "'").replace("<b>", "").replace("</b>", "").replace("&quot;", "'");
        if (dict_data.contains(":[ ],")) {
            dict_data = fillWithNoWordFound();
            getExampleArray(dict_data);
        } else {
            String[] splited = dict_data.split("\"examples\":\\[");

            if (splited.length > 1) getExampleArray(splited[1]);
            else fillWithNoExampleFound();
            dict_data = getDefArray(splited[0]);

        }
        return dict_data;

    }

    protected static String fillWithNoInternetError() {
        def_array.clear();
        def_array.add("No Internet Connection Found");
        def_array.add("You Dont Have A Internet Connection");
        def_array.add("The Word Means Nothing");
        return def_array.get(0);
    }

    protected static String fillWithNoWordFound() {
        def_array.clear();
        def_array.add("No Meaning Found");
        def_array.add("I Said I Don't Know About It");
        def_array.add("Oh Leave Me");
        return def_array.get(0);
    }

    static public String getDefArray(String s) {
        def_array.clear();
        String[] arr = s.split("\\}\\,");
        for (String unextracted_string : arr) {

            if (!unextracted_string.contains("\"text\":")) continue;
            if (unextracted_string.contains("\"language\":\"" + lang + "\"") && (!lang.equals(lang_to_translate)))
                continue;
            int first_index = unextracted_string.indexOf("\"text\":") + 7;
            unextracted_string = unextracted_string.substring(first_index + 1);

            int last_index = unextracted_string.indexOf("\"");
            String sub = unextracted_string.substring(0, last_index);
            def_array.add(sub);

        }
        if (def_array.size() == 0) fillWithNoWordFound();

        return def_array.get(0);

    }

    public static void getExampleArray(String s) {
        example_array.clear();
        boolean same_lang;
        same_lang = lang.equals(lang_to_translate);
        while (s.contains("\"first\":")) {
            int first_index = s.indexOf("\"first\":") + 8;
            s = s.substring(first_index + 1);

            int last_index = s.indexOf("\"");
            String sub = s.substring(0, last_index);
            String sub2 ="";
            s = s.substring(last_index);

            if (!same_lang & translate_examples) {

                int first_index2 = s.indexOf("\"second\":") + 9;
                s = s.substring(first_index2 + 1);

                int last_index2 = s.indexOf("\"");
                sub2 = s.substring(0, last_index2);
                s = s.substring(last_index2);
            }
            if (!(sub.length() > 200)) example_array.add(sub+" \n "+sub2);


        }

        if (example_array.size() == 0) fillWithNoExampleFound();


    }

    protected static void fillWithNoExampleFound() {

        example_array.clear();
        example_array.add("I Cant Make Any Sentence Out Of It");
        example_array.add("I Said I Cant Do It");
        example_array.add("I Am Not Google");
    }

    protected static void fillWithNoExampleFoundDueToInternet() {
        example_array.clear();
        example_array.add("No Sentence Found Becauce I Cant Connect To Internet");
        example_array.add("You Expect Everything To Work Offline,Don't You");
        example_array.add("Connect To Internet Please");

    }


}
