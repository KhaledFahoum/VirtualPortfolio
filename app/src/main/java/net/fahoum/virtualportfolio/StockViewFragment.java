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

public class StockViewFragment extends DialogFragment {
    private View stockView;
    private Stock displayStock;
    private Account currentAccount = null;
    private int SYMBOL_SIZE = 24, EXCHANGE_SIZE = 24, NAME_SIZE = 32;
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

        button = (Button) stockView.findViewById(R.id.news_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newsURL = "https://www.google.com/finance/company_news?q=" +
                        displayStock.getExchange() + "%3A" + displayStock.getSymbol();
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
