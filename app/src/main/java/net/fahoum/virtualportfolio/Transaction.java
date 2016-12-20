package net.fahoum.virtualportfolio;

public class Transaction {
    public static enum TransactionType { BUY_OP, SELL_OP };

    private TransactionType type;
    private int amount;
    private float price;
    private String date;
    private String symbol;

    public Transaction(TransactionType type, int amount, float price, String date, String symbol) {
        this.type = type;
        this.amount = amount;
        this.price = price;
        this.date = date;
        this.symbol = symbol;
    }

    public TransactionType getType() {
        return type;
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