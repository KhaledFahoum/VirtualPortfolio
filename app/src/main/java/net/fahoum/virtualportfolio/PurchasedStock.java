package net.fahoum.virtualportfolio;

import java.util.ArrayList;

import static net.fahoum.virtualportfolio.Utility.*;
import static net.fahoum.virtualportfolio.Transaction.TransactionType;

public class PurchasedStock extends Stock {
    private ArrayList<Transaction> transactions = null;
    private int amount = 0;

    public PurchasedStock(Stock stock) {
        super(stock);
        transactions = new ArrayList<>();
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

    /* Returns the change in account balance, or 0 in case of failure. */
    public float performTransaction(TransactionType type, int amount, float currentBalance) {
        if(amount < 1 || this.getBidPrice().equals("") || this.getAskPrice().equals("")) {
            return 0;
        }
        if(type == TransactionType.BUY_OP) {
            float bidPrice = Float.parseFloat(this.getBidPrice());
            float purchasePrice = bidPrice * amount;
            if(purchasePrice > currentBalance) {
                return 0;
            } else {
                this.amount += amount;
                transactions.add(new Transaction(type, amount, bidPrice, getDateAsString(), this.getSymbol()));
                return -1 * purchasePrice;
            }
        }
        if(type == TransactionType.SELL_OP) {
            float askPrice = Float.parseFloat(this.getAskPrice());
            float salePrice = askPrice * amount;
            if(this.amount < amount) {
                return 0;
            } else {
                this.amount -= amount;
                transactions.add(new Transaction(type, amount, askPrice, getDateAsString(), this.getSymbol()));
                return salePrice;
            }
        }
        return 0;
    }
}
