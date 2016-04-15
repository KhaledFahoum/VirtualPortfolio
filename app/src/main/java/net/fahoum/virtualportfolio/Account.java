package net.fahoum.virtualportfolio;

import java.util.ArrayList;
import static net.fahoum.virtualportfolio.Utility.getDateAsString;

public class Account {
    private Boolean lastLogIn;
    private String name = "";
    private String creationDate = null;
    private float balance = 0;
    private ArrayList<Transaction> purchases = null;
    private ArrayList<Transaction> sales = null;
    private ArrayList<Stock> watchedStocks = null;
    private ArrayList<Stock> ownedStocks = null;
    private ArrayList<Integer> ownedStocksAmounts = null;

    public Account(String name) {
        this.name = name;
        this.balance = 100000;         //Generous
        purchases = new ArrayList<>();
        sales = new ArrayList<>();
        creationDate = getDateAsString();
        watchedStocks = new ArrayList<>();
        ownedStocks = new ArrayList<>();
        ownedStocksAmounts = new ArrayList<>();
        lastLogIn = true;
    }

    public String getName() {
        return name;
    }

    public float getBalance() {
        return balance;
    }

    public ArrayList<Transaction> getPurchases() {
        return purchases;
    }

    public ArrayList<Transaction> getSales() {
        return sales;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public ArrayList<Integer> getOwnedStocksAmounts() {
        return ownedStocksAmounts;
    }

    public ArrayList<Stock> getOwnedStocks() {
        return ownedStocks;
    }

    public ArrayList<Stock> getWatchedStocks() {
        return watchedStocks;
    }

    public Boolean isLastLoggedUser() {
        return lastLogIn;
    }

    public void setLastLoggedUser(Boolean val) {
        lastLogIn = val;
    }

    public void watchNewStock(Stock stock) {
        watchedStocks.add(stock);
    }

    /*
    public void buyShares(String symbol, int amount, float price) {
        if(amount*price > balance) return;
        Transaction trans = new Transaction(amount, price, getDateAsString(), symbol);
        purchases.add(trans);
        if(!ownedStocks.contains(symbol)) {
            ownedStocks.add(symbol);
            ownedStocksAmounts.add(amount);
        } else {
            Integer prevAmount = ownedStocksAmounts.get(ownedStocks.indexOf(symbol));
            prevAmount += amount;
        }
        balance -= amount*price;
    }

    public void sellShares(String symbol, int amount, float price) {
        if(!ownedStocks.contains(symbol)) return;
        int index = ownedStocks.indexOf(symbol);
        Integer prevAmount = ownedStocksAmounts.get(index);
        if(prevAmount < amount) return;
        Transaction trans = new Transaction(amount, price, getDateAsString(), symbol);
        sales.add(trans);
        if(prevAmount == amount) {
            ownedStocks.remove(index);
            ownedStocksAmounts.remove(index);
        } else {
            prevAmount -= amount;
        }
        balance += amount*price;
    }
*/

    public class Transaction {
        private int amount;
        private float price;
        private String date;
        private String symbol;

        public Transaction(int count, float price, String date, String symbol) {
            this.amount = count;
            this.price = price;
            this.date = date;
            this.symbol = symbol;
        }

        public int getAmount() {
            return amount;
        }

        public float getPrice() {
            return price;
        }

        public String getDate() {
            return date;
        }

        public String getSymbol() {
            return symbol;
        }
    }
}
