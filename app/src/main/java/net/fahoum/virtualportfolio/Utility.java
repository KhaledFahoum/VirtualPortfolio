package net.fahoum.virtualportfolio;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import java.util.ArrayList;
import java.util.Calendar;

public final class Utility {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static void startMySearchTask(AsyncTask<Void, Void, Void> asyncTask) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask.execute();
    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static void startMyRefreshTask(AsyncTask<String, Void, String> asyncTask) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask.execute();
    }

    /* Example result URL: http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT&f=nab */
    static String buildYahooFinanceURL(ArrayList<String> symbols, String flags) {
        String URL = "http://finance.yahoo.com/d/quotes.csv?s=";
        if(symbols.isEmpty()) {
            return "";
        }
        URL = URL + symbols.get(0);
        for(int i = 1; i < symbols.size(); i++) {
            URL = URL + "+" + symbols.get(i);
        }
        URL = URL + "&f=" + flags;
        return URL;
    }

    static String getDateAsString() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        return ""+day+"/"+month+"/"+year;
    }

    static String trimQuotes(String str) {
        return str.substring(1, str.length()-1);
    }

    static String getInitials(String val) {
        String result = "";
        String[] tokens = val.split(" ");
        for(int i = 0; i < tokens.length; i++) {
            result = result + tokens[i].charAt(0);
        }
        return result;
    }

    static String getNDecimals(String num, int n) {
        char[] vals = num.toCharArray();
        int index = -1;
        for(int i = 0; i < vals.length; i++) {
            if(index == -1) {       //haven't found decimal point
                if(vals[i] == '.')
                    index = 0;
            } else {                //already found decimal point
                index++;
                if(index == n) {    //finished, we want up to 'i' substring
                    index = i;
                    break;
                }
            }
        }
        return new String(vals, 0, index);
    }
}
