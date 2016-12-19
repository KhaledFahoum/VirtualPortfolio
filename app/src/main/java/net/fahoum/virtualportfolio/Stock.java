package net.fahoum.virtualportfolio;

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
