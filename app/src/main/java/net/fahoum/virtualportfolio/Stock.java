package net.fahoum.virtualportfolio;

import java.util.ArrayList;

import static net.fahoum.virtualportfolio.Utility.*;

public class Stock {
    private String symbol = DATA_NOT_AVAILABLE;
    private String exchange = DATA_NOT_AVAILABLE;
    private String name = DATA_NOT_AVAILABLE;
    private String askPrice = DATA_NOT_AVAILABLE;
    private String bidPrice = DATA_NOT_AVAILABLE;
    private String dividendPayDate = DATA_NOT_AVAILABLE;
    private String dividendPerShare = DATA_NOT_AVAILABLE;
    private String earningsPerShare = DATA_NOT_AVAILABLE;
    private String change = DATA_NOT_AVAILABLE;
    private String changePercent = DATA_NOT_AVAILABLE;
    private String daysHigh = DATA_NOT_AVAILABLE;
    private String daysLow = DATA_NOT_AVAILABLE;
    private String volume = DATA_NOT_AVAILABLE;
    private String marketCap = DATA_NOT_AVAILABLE;
    private ArrayList<Transaction> transactions = null;
    private int amount = 0;
    private float investment = 0;
    /* Each stock begins as a Watched stock, and if bought once,
     * it becomes Owned forever. (to track transaction history)
     * A Watched stock can be unwatched even if it's Owned too. */
    public boolean watched = true, owned = false;

    public Stock(String symbol) {
        this.symbol = symbol;
        this.amount = 0;
        this.investment = 0;
        this.transactions = new ArrayList<>();
        this.watched = true;
        this.owned = false;
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
        this.amount = 0;
        this.investment = 0;
        this.transactions = new ArrayList<>();
    }

    /* Returns the change in account balance, or 0 in case of failure. */
    public float performTransaction(Transaction.TransactionType type, int amount, float currentBalance) {
        if(amount < 1 || this.getBidPrice().equals(DATA_NOT_AVAILABLE) ||
                        this.getAskPrice().equals(DATA_NOT_AVAILABLE)) {
            return 0;
        }
        if(type == Transaction.TransactionType.BUY_OP) {
            float askPrice = Float.parseFloat(this.getAskPrice());
            float totalPurchasePrice = askPrice * amount;
            if(totalPurchasePrice > currentBalance) {
                return 0;   // Account lacks funds to buy.
            } else {
                this.amount += amount;
                this.investment += totalPurchasePrice;
                this.transactions.add(new Transaction(type, amount, askPrice, getDateAsString(), this.getSymbol()));
                owned = true;
                return -1 * totalPurchasePrice;
            }
        }
        if(type == Transaction.TransactionType.SELL_OP) {
            float bidPrice = Float.parseFloat(this.getBidPrice());
            float totalSellPrice = bidPrice * amount;
            if(this.amount < amount) {
                return 0;   // Account lacks shares to sell.
            } else {
                this.amount -= amount;
                this.investment -= totalSellPrice;
                this.transactions.add(new Transaction(type, amount, bidPrice, getDateAsString(), this.getSymbol()));
                return totalSellPrice;
            }
        }
        return 0;   // Just in case.
    }

    /* Calls 'setValue()' for each Stock member field. */
    public void updateStock(ArrayList<String> values) {
        int i = 0;
        for(String flag : App.getQueryFlagsList()) {
            if(flag.equals("x")) {
                i++;
                continue;
            }
            setValue(values.get(i), flag);
            i++;
        }
    }

    public void setValue(String value, String flag) {
        if(value.equals(DATA_NOT_AVAILABLE)) {
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
                changePercent = getNDecimals(trimQuotes(value), PRECISION)+"%";
                break;
            case "c1":
                change = getNDecimals(value, PRECISION);
                break;
            case "a":
                askPrice = getNDecimals(value, PRECISION);
                break;
            case "b":
                bidPrice = getNDecimals(value, PRECISION);;
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

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public float getInvestment() {
        return investment;
    }

    public void setInvestment(float investment) {
        this.investment = investment;
    }
}
