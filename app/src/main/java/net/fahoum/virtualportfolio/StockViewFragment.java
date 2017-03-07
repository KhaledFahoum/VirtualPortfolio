package net.fahoum.virtualportfolio;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import static net.fahoum.virtualportfolio.Transaction.TransactionType;
import static net.fahoum.virtualportfolio.Utility.*;

public class StockViewFragment extends DialogFragment {
    private View stockView;
    private Stock displayStock;
    private Account currentAccount = null;
    private int SYMBOL_SIZE = 24, EXCHANGE_SIZE = 24, NAME_SIZE = 32, VOLUME_SIZE = 20,
                ASK_SIZE = 25, BID_SIZE = 23, CHANGE_SIZE = 20, CHANGE_PERCENT_SIZE = 19,
                DAYS_LOW_SIZE = 19, DAYS_HIGH_SIZE = 19, DIVS_PER_SHARE_SIZE = 19,
                DIVS_PAY_DATE_SIZE = 19, EARNINGS_SIZE = 19, MARKET_CAP_SIZE = 19,
                AMOUNT_SIZE = 21;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView view;
        Button button;
        stockView = inflater.inflate(R.layout.fragment_stock_view, container);
        displayStock = App.getDisplayStock();
        currentAccount = App.getCurrentAccount();
        view = (TextView) stockView.findViewById(R.id.stock_symbol);
        view.setText(displayStock.getSymbol());
        setFontAndSize(view, "fonts/ostrich-black.ttf", SYMBOL_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_exchange);
        view.setText(displayStock.getExchange());
        setFontAndSize(view, "fonts/ostrich-black.ttf", EXCHANGE_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_name);
        view.setText(displayStock.getName());
        setFontAndSize(view, "fonts/ostrich-inline.ttf", NAME_SIZE);
        String temp;
        view = (TextView) stockView.findViewById(R.id.stock_volume);
        view.setText(displayStock.getVolume());
        setFontAndSize(view, "fonts/ostrich-black.ttf", VOLUME_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_ask_price);
        temp = displayStock.getAskPrice();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        setFontAndSize(view, "fonts/ostrich-black.ttf", ASK_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_change);
        temp = displayStock.getChange();
        view.setText(temp);
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            if(temp.charAt(0) == '+') {
                view.setTextColor(GREEN_COLOR);
            } else if(temp.charAt(0) == '-') {
                view.setTextColor(RED_COLOR);
            }
        }
        setFontAndSize(view, "fonts/ostrich-black.ttf", CHANGE_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_change_percent);
        temp = displayStock.getChangePercent();
        view.setText("("+temp+")");
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            if(temp.charAt(0) == '+') {
                view.setTextColor(GREEN_COLOR);
            } else if(temp.charAt(0) == '-') {
                view.setTextColor(RED_COLOR);
            }
        }
        setFontAndSize(view, "fonts/ostrich-black.ttf", CHANGE_PERCENT_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_bid_price);
        temp = displayStock.getBidPrice();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        setFontAndSize(view, "fonts/ostrich-black.ttf", BID_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_low_price);
        temp = displayStock.getDaysLow();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        setFontAndSize(view, "fonts/ostrich-black.ttf", DAYS_LOW_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_high_price);
        temp = displayStock.getDaysHigh();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        setFontAndSize(view, "fonts/ostrich-black.ttf", DAYS_HIGH_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_dividend_per_share);
        temp = displayStock.getDividendPerShare();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        setFontAndSize(view, "fonts/ostrich-black.ttf", DIVS_PER_SHARE_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_dividend_pay_date);
        view.setText(displayStock.getDividendPayDate());
        setFontAndSize(view, "fonts/ostrich-black.ttf", DIVS_PAY_DATE_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_earnings_per_share);
        temp = displayStock.getEarningsPerShare();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        setFontAndSize(view, "fonts/ostrich-black.ttf", EARNINGS_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_market_capital);
        temp = displayStock.getMarketCap();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        setFontAndSize(view, "fonts/ostrich-black.ttf", MARKET_CAP_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_amount);
        view.setText(String.valueOf(displayStock.getAmount()));
        setFontAndSize(view, "fonts/ostrich-black.ttf", AMOUNT_SIZE);

        button = (Button) stockView.findViewById(R.id.sell_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //displayStock.refreshStock();

                // For debugging only
                currentAccount.performTransaction(displayStock, TransactionType.SELL_OP,
                        30, currentAccount.getBalance());
            }
        });

        button = (Button) stockView.findViewById(R.id.buy_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //displayStock.refreshStock();

                // For debugging only
                currentAccount.performTransaction(displayStock, TransactionType.BUY_OP,
                        30, currentAccount.getBalance());
            }
        });

        button = (Button) stockView.findViewById(R.id.google_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newsURL = "https://www.google.com/finance/company_news?q=" +
                        displayStock.getExchange() + "%3A" + displayStock.getSymbol();
                openURL(newsURL);
            }
        });

        button = (Button) stockView.findViewById(R.id.yahoo_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newsURL = "https://finance.yahoo.com/quote/" +
                                                displayStock.getSymbol();
                openURL(newsURL);
            }
        });

        button = (Button) stockView.findViewById(R.id.reddit_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String redditURL = "https://www.reddit.com/r/investing/search?q=" +
                        displayStock.getSymbol() + "+" + displayStock.getName() +
                        "&sort=new&restrict_sr=on&t=all";
                openURL(redditURL);
            }
        });
        return stockView;
    }

    static StockViewFragment newInstance() {
        StockViewFragment fragment = new StockViewFragment();
        return fragment;
    }

    public StockViewFragment() {
        // Empty constructor required for DialogFragment
    }

    private void setFontAndSize(TextView view, String typefaceAsset, int size) {
        view.setTypeface(Typeface.createFromAsset(getContext().getAssets(), typefaceAsset));
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    private void openURL(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
