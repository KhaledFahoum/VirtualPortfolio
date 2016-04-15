package net.fahoum.virtualportfolio;

import android.app.Application;

public class App extends Application {
    private Stock currentlyDisplayedStock = null;
    private int precision;

    @Override
    public void onCreate() {
        super.onCreate();
        precision = 2;
    }

    public void setDisplayStock(Stock stock) {
        currentlyDisplayedStock = stock;
    }

    public Stock getDisplayStock() {
        return currentlyDisplayedStock;
    }

    public int getPrecision() {
        return precision;
    }
}
