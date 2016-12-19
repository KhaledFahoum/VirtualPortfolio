package net.fahoum.virtualportfolio;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import static net.fahoum.virtualportfolio.Utility.getDateAsString;

public class Account {
    private Boolean loggedInFlag;
    private String name = "";
    private String creationDate = null;
    private float balance = 0;
    private ArrayList<Transaction> purchases = null;
    private ArrayList<Transaction> sales = null;
    private ArrayList<Stock> watchedStocks = null;
    private ArrayList<Stock> ownedStocks = null;
    private ArrayList<Integer> ownedStocksAmounts = null;


    // Debug constructor
    public Account(String name) {
        this.name = name;
        this.balance = 100000;         //Generous
        purchases = new ArrayList<>();
        sales = new ArrayList<>();
        creationDate = getDateAsString();
        watchedStocks = new ArrayList<>();
        ownedStocks = new ArrayList<>();
        ownedStocksAmounts = new ArrayList<>();
        loggedInFlag = true;
    }

    public Account(String name, String creationDate, String balance,
                   boolean loggedInFlag, ArrayList<Stock> watchedList,
                   ArrayList<Stock> ownedList, ArrayList<Integer> ownedAmountsList) {
        this.name = name;
        this.creationDate = creationDate;
        this.balance = Float.valueOf(balance);
        this.loggedInFlag = loggedInFlag;
        purchases = new ArrayList<>();
        sales = new ArrayList<>();
        watchedStocks = watchedList;
        ownedStocks = ownedList;
        ownedStocksAmounts = ownedAmountsList;
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

    public Boolean isLoggedIn() {
        return loggedInFlag;
    }

    public void setLoggedIn(Boolean val) {
        loggedInFlag = val;
    }

    public void watchNewStock(Stock stock) {
        watchedStocks.add(stock);
    }

    public void printToFile(File file) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter((new FileWriter((file))));
            writer.write("account line\n");
            if(loggedInFlag) {
                writer.write("true\n");
            } else {
                writer.write("false\n");
            }
            writer.write(name + "\n");
            writer.write(creationDate+"\n");
            writer.write(Float.toString(balance)+"\n");
            writer.write("watched stocks line\n");
            for(int i = 0; i < watchedStocks.size(); i++) {
                writer.write(watchedStocks.get(i).getName()+"\n");
                writer.write(watchedStocks.get(i).getSymbol()+"\n");
                writer.write(watchedStocks.get(i).getExchange()+"\n");
            }
            writer.write("owned stocks line");
            for(int j = 0; j < ownedStocks.size(); j++) {
                writer.write(ownedStocks.get(j).getName()+"\n");
                writer.write(ownedStocks.get(j).getSymbol()+"\n");
                writer.write(ownedStocks.get(j).getExchange()+"\n");
                writer.write(Integer.toString(ownedStocksAmounts.get(j))+"\n");
            }
            //TODO: backup transaction history
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
