package net.fahoum.virtualportfolio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static net.fahoum.virtualportfolio.Utility.*;

public class Stock {
    private String symbol = "";
    private String exchange = "";
    private String name = "";
    private String askPrice = "";
    private String bidPrice = "";
    private String dividendPayDate = "";
    private String dividendPerShare = "0";
    private String earningsPerShare = "0";
    private String change = "";
    private String daysHigh = "";
    private String daysLow = "";
    private String changePercent = "";
    private String volume = "";
    private String marketCap = "";

    public Stock(String symbol) {
        this.symbol = symbol;
    }

    public Stock() {

    }

    public Stock(Stock stock) {
        this.symbol = stock.symbol;
        this.exchange = stock.exchange;
        this.name = stock.name;
        this.askPrice = stock.askPrice;
        this.bidPrice = stock.bidPrice;
        this.dividendPayDate = stock.dividendPayDate;
        this.dividendPerShare = stock.dividendPerShare;
        this.earningsPerShare = stock.earningsPerShare;
        this.change = stock.change;
        this.daysHigh = stock.daysHigh;
        this.daysLow = stock.daysLow;
        this.changePercent = stock.changePercent;
        this.volume = stock.volume;
        this.marketCap = stock.marketCap;
    }

    public void refreshStock() {
        ArrayList<String> symbolContainer = new ArrayList<>();
        symbolContainer.add(symbol);
        try {
            URL url = new URL(buildYahooFinanceURL(symbolContainer, App.getQueryFlagsString()));
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream input = new BufferedInputStream(url.openStream(), 10000);
            File temporaryCacheFile = new File(App.getAppContext().getCacheDir(), "single_stock_data.dat");
            OutputStream output = new FileOutputStream(temporaryCacheFile.getPath());
            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            BufferedReader reader = new BufferedReader(new FileReader(temporaryCacheFile));
            String record;
            String[] values;
            record = reader.readLine();
            values = record.split(",");
            updateStock(values);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateStock(String[] values) {
        int i = 0;
        for(String flag : App.getQueryFlagsList()) {
            if(flag.equals("x")) {
                i++;
                continue;
            }
            setValue(values[i], flag);
            i++;
        }
    }

    public void setValue(String value, String flag) {
        if(value.equals("N/A")) {
            return;
        }
        switch(flag) {
            case "s":
                symbol = trimQuotes(value);
                break;
            case "x":
                exchange = value;
                break;
            case "p2":
                changePercent = getNDecimals(trimQuotes(value), 2)+"%";
                break;
            case "c1":
                change = getNDecimals(value, 2);
                break;
            case "a":
                askPrice = value;
                break;
            case "b":
                bidPrice = value;
                break;
            case "r1":
                dividendPayDate = convertDateToDMY(trimQuotes(value));
                break;
            case "d":
                dividendPerShare = value;
                break;
            case "v":
                volume = value;
                break;
            case "j1":
                marketCap = value;
                break;
            case "g":
                daysLow = value;
                break;
            case "h":
                daysHigh = value;
                break;
            case "e":
                earningsPerShare = value;
                break;
            case "n":
                name = value;
                break;
            default:
                return;
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public String getName() {
        return name;
    }

    public String getAskPrice() {
        return askPrice;
    }

    public String getBidPrice() {
        return bidPrice;
    }

    public String getDividendPayDate() {
        return dividendPayDate;
    }

    public String getDividendPerShare() {
        return dividendPerShare;
    }

    public String getChange() {
        return change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public String getVolume() {
        return volume;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public String getDaysHigh() {
        return daysHigh;
    }

    public String getDaysLow() {
        return daysLow;
    }

    public String getEarningsPerShare() {
        return earningsPerShare;
    }
}
