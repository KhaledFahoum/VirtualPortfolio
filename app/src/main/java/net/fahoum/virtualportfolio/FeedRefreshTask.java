package net.fahoum.virtualportfolio;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static net.fahoum.virtualportfolio.MainActivity.*;
import static net.fahoum.virtualportfolio.Utility.*;

/* Receives a feedID and performs network operations to retrieve real-time values,
 * then updates the stocks datastructures and notifies the UI. */
public class FeedRefreshTask extends AsyncTask<String, Void, String> {
    private URL targetURL;
    private MainActivity.feedId id;

    public FeedRefreshTask(MainActivity.feedId id) throws MalformedURLException {
        this.targetURL = null;
        this.id = id;
    }

    @Override
    protected void onPreExecute() {
        String targetURL = "";
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Stock> feed = allFeeds.get(id.ordinal());
        if(feed.size() < 1) {
            allAdapters.get(id.ordinal()).notifyDataSetChanged();
            return;
        }
        for(Stock stock : feed) {
            list.add(stock.getSymbol());
        }
        targetURL = buildYahooFinanceURL(list, App.getQueryFlagsString());
        try {
            this.targetURL = new URL(targetURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        int index;
        Stock stock;
        ArrayList<String> result;

        try {
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(targetURL.openStream()));
            String line;
            while((line = inputReader.readLine()) != null) {
                result = new ArrayList<String>(Arrays.asList(line.split(",")));
                index = findStockIndexBySymbol(trimQuotes(result.get(0)), id);
                if(index == -1) {
                    continue;
                }
                stock = allFeeds.get(id.ordinal()).get(index);
                stock.updateStock(result);
            }
        } catch (Exception e) {
            //..
        }
        return null;
    }

    @Override
    protected void onPostExecute(String res) {
        allAdapters.get(id.ordinal()).notifyDataSetChanged();
        refreshTask = null;
    }
}