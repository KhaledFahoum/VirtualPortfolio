package net.fahoum.virtualportfolio;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import static net.fahoum.virtualportfolio.Utility.getDateAsString;
import static net.fahoum.virtualportfolio.Transaction.TransactionType;

public class Account {
    private Boolean loggedInFlag;
    private String name = "";
    private String creationDate = null;
    private float balance = 0;
    private ArrayList<Stock> watchedStocks = null;
    private ArrayList<Stock> ownedStocks = null;


    // Debug constructor
    public Account(String name) {
        this.name = name;
        this.balance = 100000;         //Generous
        creationDate = getDateAsString();
        watchedStocks = new ArrayList<>();
        ownedStocks = new ArrayList<>();
        loggedInFlag = true;
    }

    public Account(String name, String creationDate, String balance,
                   boolean loggedInFlag, ArrayList<Stock> watchedList,
                   ArrayList<Stock> ownedList) {
        this.name = name;
        this.creationDate = creationDate;
        this.balance = Float.valueOf(balance);
        this.loggedInFlag = loggedInFlag;
        watchedStocks = watchedList;
        ownedStocks = ownedList;
    }

    public String getName() {
        return name;
    }

    public float getBalance() {
        return balance;
    }

    public String getCreationDate() {
        return creationDate;
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
            writer.write("stocks line\n");
            for(Stock stock : watchedStocks) {
                writer.write(stock.getName()+"\n");
                writer.write(stock.getSymbol()+"\n");
                writer.write(stock.getExchange()+"\n");
                writer.write(String.valueOf(stock.getAmount())+"\n");
                writer.write(String.valueOf(stock.getInvestment())+"\n");
                writer.write(String.valueOf(stock.watched)+"\n");
                writer.write(String.valueOf(stock.owned)+"\n");
                //TODO: backup transaction history
            }
            for(Stock stock : ownedStocks) {
                if(stock.watched == true) {
                    continue; //already backed up
                }
                writer.write(stock.getName()+"\n");
                writer.write(stock.getSymbol()+"\n");
                writer.write(stock.getExchange()+"\n");
                writer.write(String.valueOf(stock.getAmount())+"\n");
                writer.write(String.valueOf(stock.getInvestment())+"\n");
                writer.write(String.valueOf(stock.watched)+"\n");
                writer.write(String.valueOf(stock.owned)+"\n");
                //TODO: backup transaction history
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void performTransaction(Stock stock, TransactionType type, int amount) {
        Stock targetStock = null;
        boolean newStockFlag = true;
        for(Stock ownedStock : ownedStocks) {
            if(ownedStock.getSymbol().equals(stock.getSymbol())) {  // Already traded this stock.
                targetStock = ownedStock;
                newStockFlag = false;
                break;
            }
        }
        if(newStockFlag == true) {
            targetStock = stock;
        }
        float result = targetStock.performTransaction(type, amount, this.balance);
        if(result == 0) {   // Transaction failed.
;           return;
        } else {            // Transaction succeeded.
            balance += result;
            if(newStockFlag == true) {
                ownedStocks.add(targetStock);
                App.ownedFeed.add(targetStock);
            }
        }
    }
}
