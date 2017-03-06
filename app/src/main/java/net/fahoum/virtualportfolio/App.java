package net.fahoum.virtualportfolio;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

public class App extends Application {
    private static Stock currentlyDisplayedStock = null;
    private static Context appContext;
    private static Account currentAccount = null;
    private static String queryFlagsString = null;
    private static ArrayList<String> queryFlagsList = null;
    public static ArrayList<Stock> watchFeed = null, ownedFeed = null;
    public static int PRECISION = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        initializeQueryFlags();
        watchFeed = new ArrayList<Stock>();
        ownedFeed = new ArrayList<Stock>();
    }

    public static void setDisplayStock(Stock stock) {
        currentlyDisplayedStock = stock;
    }

    public static Stock getDisplayStock() {
        return currentlyDisplayedStock;
    }

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static int getPrecision() {
        return PRECISION;
    }

    public static String getQueryFlagsString() {
        return queryFlagsString;
    }

    public static ArrayList<String> getQueryFlagsList() {
        return queryFlagsList;
    }

    private void initializeQueryFlags() {
        queryFlagsList = new ArrayList<>();
        queryFlagsList.add("s");
        queryFlagsList.add("a");
        queryFlagsList.add("b");
        queryFlagsList.add("c1");
        queryFlagsList.add("p2");
        queryFlagsList.add("v");
        queryFlagsList.add("x");
        queryFlagsList.add("j1");
        queryFlagsList.add("g");
        queryFlagsList.add("h");
        queryFlagsList.add("e");
        queryFlagsList.add("r1");
        queryFlagsList.add("d");
        StringBuilder builder = new StringBuilder();
        for(String flag : queryFlagsList) {
            builder.append(flag);
        }
        queryFlagsString = builder.toString();
    }
}
