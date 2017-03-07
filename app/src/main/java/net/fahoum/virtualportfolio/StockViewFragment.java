package net.fahoum.virtualportfolio;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.apptik.widget.MultiSlider;
import me.grantland.widget.AutofitHelper;

import static net.fahoum.virtualportfolio.Transaction.TransactionType;
import static net.fahoum.virtualportfolio.Utility.*;


public class StockViewFragment extends DialogFragment {
    private View stockView;
    private Stock displayStock;
    private MultiSlider transactionSlider;
    private EditText transactionBox;
    private Button transactionButton;
    private boolean transactionsEnabled = false;
    private Account currentAccount = null;
    private int SYMBOL_SIZE = 24, EXCHANGE_SIZE = 24, NAME_SIZE = 32, VOLUME_SIZE = 20,
                ASK_SIZE = 25, BID_SIZE = 23, CHANGE_SIZE = 20, CHANGE_PERCENT_SIZE = 19,
                DAYS_LOW_SIZE = 19, DAYS_HIGH_SIZE = 19, DIVS_PER_SHARE_SIZE = 19,
                DIVS_PAY_DATE_SIZE = 19, EARNINGS_SIZE = 19, MARKET_CAP_SIZE = 19,
                AMOUNT_SIZE = 21, STOCK_VIEW_REFRESH_DELAY_INTERVAL =  1000*1;
    private Handler refreshHandler;
    private boolean refreshing = false;
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if(refreshing == true) {
                return;
            }
            refreshing = true;
            try {
                populateViewWithStockData();
                updateTransactionSliderRange();
            } finally {
                refreshHandler.postDelayed(refreshRunnable, STOCK_VIEW_REFRESH_DELAY_INTERVAL);
            }
            refreshing = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Button button;
        stockView = inflater.inflate(R.layout.fragment_stock_view, container);
        displayStock = App.getDisplayStock();
        currentAccount = App.getCurrentAccount();
        initializeFonts();
        populateViewWithStockData();

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

        transactionButton = (Button) stockView.findViewById(R.id.transaction_button);
        transactionButton.setBackgroundColor(getResources().getColor(R.color.colorNeutral));
        transactionButton.setText("");
        transactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int input = transactionSlider.getThumb(0).getValue();
                if(input > 0) {
                    currentAccount.performTransaction(displayStock, TransactionType.BUY_OP, input);
                } else if(input < 0) {
                    currentAccount.performTransaction(displayStock, TransactionType.SELL_OP, Math.abs(input));
                } else {
                    return;
                }
            }
        });

        transactionBox = (EditText) stockView.findViewById(R.id.transaction_amount);
        transactionSlider = (MultiSlider)stockView.findViewById(R.id.transaction_slider);
        if((displayStock.getAskPrice().equals(DATA_NOT_AVAILABLE)) ||
                (displayStock.getBidPrice().equals(DATA_NOT_AVAILABLE))) { //disable transactions
            transactionsEnabled = false;
            transactionBox.setVisibility(View.GONE);
            transactionSlider.setVisibility(View.GONE);
            transactionButton.setVisibility(View.GONE);
            ((TextView) stockView.findViewById(R.id.shares_text)).setVisibility(View.GONE);
        } else {
            transactionsEnabled = true;
            transactionSlider.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
                @Override
                public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value) {
                    int input = transactionSlider.getThumb(0).getValue();
                    transactionBox.setText(""+Math.abs(input));
                    transactionBox.setSelection(transactionBox.getText().length());
                    if(input > 0) {
                        transactionButton.setBackgroundColor(getResources().getColor(R.color.colorBuy));
                        Float totalPrice = Float.valueOf(displayStock.getAskPrice())*Math.abs(input);

                        transactionButton.setText("BUY\n(-$"+String.format(java.util.Locale.US,"%.2f", totalPrice)+")");
                    } else if(input < 0) {
                        transactionButton.setBackgroundColor(getResources().getColor(R.color.colorSell));
                        Float totalPrice = Float.valueOf(displayStock.getBidPrice())*Math.abs(input);
                        transactionButton.setText("SELL\n($"+String.format(java.util.Locale.US,"%.2f", totalPrice)+")");
                    } else {
                        transactionButton.setBackgroundColor(getResources().getColor(R.color.colorNeutral));
                        transactionButton.setText("");
                    }
                }
            });
            updateTransactionSliderRange();
            transactionBox.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    int input = 0;
                    if(!s.toString().equals("")) {
                        input = Integer.valueOf(s.toString());
                    }
                    int sliderValue = transactionSlider.getThumb(0).getValue();
                    if(Math.abs(sliderValue) == input) {
                        return;
                    }
                    if(sliderValue >= 0) {  //Buying
                        if(input > transactionSlider.getMax()) {
                            transactionSlider.getThumb(0)
                                    .setValue(transactionSlider.getMax());
                        } else {
                            transactionSlider.getThumb(0).setValue(input);
                        }
                    } else if(sliderValue < 0) { //Selling
                        if(-1*input < transactionSlider.getMin()) {
                            transactionSlider.getThumb(0)
                                    .setValue(transactionSlider.getMin());
                        } else {
                            transactionSlider.getThumb(0)
                                    .setValue(-1*input);
                        }
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    //..
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    //..
                }
            });
        }
        // Setting up auto-refresh handler.
        refreshHandler = new Handler();
        refreshRunnable.run();
        return stockView;
    }

    static StockViewFragment newInstance() {
        StockViewFragment fragment = new StockViewFragment();
        return fragment;
    }

    public StockViewFragment() {
        // Empty constructor required for DialogFragment
    }

    private void updateTransactionSliderRange() {
        if(!transactionsEnabled) {
            return;
        }
        transactionSlider.setMax((int)(currentAccount.getBalance()/Float.valueOf(displayStock.getAskPrice())));
        transactionSlider.setMin(-displayStock.getAmount());
    }

    private void populateViewWithStockData() {
        TextView view;
        view = (TextView) stockView.findViewById(R.id.stock_symbol);
        view.setText(displayStock.getSymbol());
        view = (TextView) stockView.findViewById(R.id.stock_exchange);
        view.setText(displayStock.getExchange());
        view = (TextView) stockView.findViewById(R.id.stock_name);
        AutofitHelper.create(view);

        view.setText(displayStock.getName());
        String temp;
        view = (TextView) stockView.findViewById(R.id.stock_volume);
        view.setText(displayStock.getVolume());
        view = (TextView) stockView.findViewById(R.id.stock_ask_price);
        temp = displayStock.getAskPrice();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+getNDecimals(temp, PRECISION);
        }
        view.setText(temp);
        view = (TextView) stockView.findViewById(R.id.stock_change);
        temp = displayStock.getChange();
        view.setText(getNDecimals(temp, PRECISION));
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            if(temp.charAt(0) == '+') {
                view.setTextColor(GREEN_COLOR);
            } else if(temp.charAt(0) == '-') {
                view.setTextColor(RED_COLOR);
            }
        }
        view = (TextView) stockView.findViewById(R.id.stock_change_percent);
        temp = displayStock.getChangePercent();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            view.setText("("+getNDecimals(temp, PRECISION)+"%)");
            if(temp.charAt(0) == '+') {
                view.setTextColor(GREEN_COLOR);
            } else if(temp.charAt(0) == '-') {
                view.setTextColor(RED_COLOR);
            }
        } else {
            view.setText("("+getNDecimals(temp, PRECISION)+")");
        }
        view = (TextView) stockView.findViewById(R.id.stock_bid_price);
        temp = displayStock.getBidPrice();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+getNDecimals(temp, PRECISION);
        }
        view.setText(temp);
        view = (TextView) stockView.findViewById(R.id.stock_low_price);
        temp = displayStock.getDaysLow();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+getNDecimals(temp, PRECISION);
        }
        view.setText(temp);
        view = (TextView) stockView.findViewById(R.id.stock_high_price);
        temp = displayStock.getDaysHigh();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+getNDecimals(temp, PRECISION);
        }
        view.setText(temp);
        view = (TextView) stockView.findViewById(R.id.stock_dividend_per_share);
        temp = displayStock.getDividendPerShare();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        view = (TextView) stockView.findViewById(R.id.stock_dividend_pay_date);
        view.setText(displayStock.getDividendPayDate());
        view = (TextView) stockView.findViewById(R.id.stock_earnings_per_share);
        temp = displayStock.getEarningsPerShare();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        view = (TextView) stockView.findViewById(R.id.stock_market_capital);
        temp = displayStock.getMarketCap();
        if(!temp.equals(DATA_NOT_AVAILABLE)) {
            temp = "$"+temp;
        }
        view.setText(temp);
        view = (TextView) stockView.findViewById(R.id.stock_amount);
        view.setText(String.valueOf(displayStock.getAmount()));
    }

    private void initializeFonts() {
        TextView view;
        view = (TextView) stockView.findViewById(R.id.stock_symbol);
        setFontAndSize(view, DEFAULT_FONT, SYMBOL_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_exchange);
        setFontAndSize(view, DEFAULT_FONT, EXCHANGE_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_name);
        setFontAndSize(view, NAME_FONT, NAME_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_volume);
        setFontAndSize(view, DEFAULT_FONT, VOLUME_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_ask_price);
        setFontAndSize(view, DEFAULT_FONT, ASK_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_change);
        setFontAndSize(view, DEFAULT_FONT, CHANGE_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_change_percent);
        setFontAndSize(view, DEFAULT_FONT, CHANGE_PERCENT_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_bid_price);
        setFontAndSize(view, DEFAULT_FONT, BID_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_low_price);
        setFontAndSize(view, DEFAULT_FONT, DAYS_LOW_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_high_price);
        setFontAndSize(view, DEFAULT_FONT, DAYS_HIGH_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_dividend_per_share);
        setFontAndSize(view, DEFAULT_FONT, DIVS_PER_SHARE_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_dividend_pay_date);
        setFontAndSize(view, DEFAULT_FONT, DIVS_PAY_DATE_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_earnings_per_share);
        setFontAndSize(view, DEFAULT_FONT, EARNINGS_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_market_capital);
        setFontAndSize(view, DEFAULT_FONT, MARKET_CAP_SIZE);
        view = (TextView) stockView.findViewById(R.id.stock_amount);
        setFontAndSize(view, DEFAULT_FONT, AMOUNT_SIZE);
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
