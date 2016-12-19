package net.fahoum.virtualportfolio;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Stock currentlyDisplayedStock = null;
    private int PRECISION = 2;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public void setDisplayStock(Stock stock) {
        currentlyDisplayedStock = stock;
    }

    public static Stock getDisplayStock() {
        return currentlyDisplayedStock;
    }

    public static Context getAppContext() {
        return appContext;
    }
    public int getPrecision() {
        return PRECISION;
    }
}
