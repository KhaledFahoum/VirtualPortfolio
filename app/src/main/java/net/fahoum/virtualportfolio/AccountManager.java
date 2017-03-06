package net.fahoum.virtualportfolio;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AccountManager {
    private String storageDataFilename = "virtual_portfolio_data.dat";
    private static Context appContext;
    private ArrayList<Account> accountsList = null;
    private Account lastLoggedAccount = null;

    public AccountManager() {
        appContext = App.getAppContext();
        lastLoggedAccount = loadAccountStructure();
        App.setCurrentAccount(lastLoggedAccount);
    }

    public void saveAccountStructure() {
        File file = new File(appContext.getFilesDir(), storageDataFilename);
        for(Account account : accountsList) {
            account.printToFile(file);
        }
    }

    public Account logInNewAccount() {
        Account newAccount = new Account("My New Portfolio");
        accountsList.add(newAccount);
        lastLoggedAccount = newAccount;
        App.setCurrentAccount(newAccount);
        return newAccount;
    }

    public Account getLastLoggedAccount() {
        return lastLoggedAccount;
    }

    /* Rebuilds the Accounts list and returns the last logged-in account, else returns null. */
    private Account loadAccountStructure() {
        accountsList = new ArrayList<Account>();
        Account recoveredAccount, lastLoggedAccount = null;
        ArrayList<Stock> watchList;
        ArrayList<Stock> ownedList;
        BufferedReader reader;
        String record, lastLogIn, name, creationDate, balance, stockName, symbol, amount, exchange;
        Stock stock;
        PurchasedStock purchasedStock;
        File file = new File(appContext.getFilesDir(), storageDataFilename);
        if(file == null) {
            return null;
        }
        try {
            reader = new BufferedReader(new FileReader(file));
            record = reader.readLine();
            if(record == null) {        // new user
                return null;
            }
            // else ->  record == "account line"
            while ((record = reader.readLine()) != null) {
                watchList = new ArrayList<Stock>();
                ownedList = new ArrayList<Stock>();
                lastLogIn = record.replace("\n", "");
                record = reader.readLine();
                name = record.replace("\n", "");
                record = reader.readLine();
                creationDate = record.replace("\n", "");
                record = reader.readLine();
                balance = record.replace("\n", "");
                record = reader.readLine();         //intentional, to skip "watched stocks line"
                record = reader.readLine();
                record = record.replace("\n", "");
                while(!record.equals("owned stocks line")) {   // there's a watched stock
                    stockName = record;
                    record = reader.readLine();
                    symbol = record.replace("\n", "");
                    record = reader.readLine();
                    exchange = record.replace("\n", "");
                    stock = new Stock(symbol);
                    stock.setValue(stockName, "n");
                    stock.setValue(exchange, "x");
                    watchList.add(stock);
                    record = reader.readLine();
                    record = record.replace("\n", "");
                }
                record = reader.readLine();
                if(record != null) {
                    record = record.replace("\n", "");
                }
                while(record != null && !record.equals("account line")) { // there's an owned stock
                    stockName = record;
                    record = reader.readLine();
                    symbol = record.replace("\n", "");
                    record = reader.readLine();
                    exchange = record.replace("\n", "");
                    record = reader.readLine();
                    amount = record.replace("\n", "");
                    stock = new Stock(symbol);
                    stock.setValue(stockName, "n");
                    stock.setValue(exchange, "x");
                    stock.setAmount(Integer.parseInt(amount));
                    ownedList.add(stock);
                    record = reader.readLine();
                    if(record != null) {
                        record = record.replace("\n", "");
                    }
                }
                if(lastLogIn.equals("true")) {
                    recoveredAccount = new Account(name, creationDate, balance, true, watchList, ownedList);
                    lastLoggedAccount = recoveredAccount;
                } else {
                    recoveredAccount = new Account(name, creationDate, balance, false, watchList, ownedList);
                }
                accountsList.add(recoveredAccount);
                if(record == null) {        // Done.
                    break;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastLoggedAccount;
    }
}
