package net.fahoum.virtualportfolio;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

public final class Utility {

    public static int GREEN_COLOR = Color.rgb(50, 90, 0),
                        RED_COLOR = Color.rgb(204, 0, 0),
                        GRAY_COLOR = Color.rgb(112, 128, 144),
                        COPPER_COLOR = Color.rgb(184, 134, 11);
    public static String DATA_NOT_AVAILABLE = "N/A";
    public static int PRECISION = 2;
    public static String DEFAULT_FONT = "fonts/ostrich-black.ttf",
                            NAME_FONT = "fonts/ostrich-inline.ttf";

    static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);
            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static void startSearchTask(AsyncTask<Void, Void, Void> asyncTask) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask.execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static void startRefreshTask(AsyncTask<String, Void, String> asyncTask) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask.execute();
    }

    /* Example result URL: http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT&f=nab */
    static String buildYahooFinanceURL(ArrayList<String> symbols, String flags) {
        int i = 0;
        if(symbols.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("http://finance.yahoo.com/d/quotes.csv?s=");
        result.append(symbols.get(0));
        for(String str : symbols.subList(1, symbols.size())) {
            result.append("+");
            result.append(str);
        }
        result.append("&f=");
        result.append(flags);
        return result.toString();
    }

    static int DPtoPixel(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
       return (int)(dp * density);
    }

    /* Used to filter search input by omitting non-alphanumeric characters. */
    static String filterInputCharacters(String str) {
        char[] array = str.toCharArray();
        StringBuilder result = new StringBuilder();
        for(char ch : array) {
            if((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') ||
                    (ch >= '0' && ch <= '9')) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    static String convertDateToDMY(String date) {
        String[] tokens = date.split("/");
        return tokens[1]+"/"+tokens[0]+"/"+tokens[2];
    }

    static String getDateAsString() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        return ""+day+"/"+month+"/"+year;
    }

    static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    static String trimQuotes(String str) {
        return str.substring(1, str.length()-1);
    }

    static String getInitials(String val) {
        String result = "";
        String[] tokens = val.split(" ");
        for(String token : tokens) {
            result = result + token.charAt(0);
        }
        return result;
    }

    static String getNDecimals(String num, int n) {
        if(n == 0) {
            return num.substring(0, num.indexOf("."));
        } else {
            return num.substring(0, num.indexOf(".")+n+1); //+1 because endIndex is exclusive.
        }
    }
}
