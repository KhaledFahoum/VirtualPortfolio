package net.fahoum.virtualportfolio;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

import static net.fahoum.virtualportfolio.MainActivity.currentFeed;
import static net.fahoum.virtualportfolio.Utility.*;

public class StockPreviewAdapter extends ArrayAdapter<Stock> {
    private int previewType; // 0 = 'fragment_stock_view', 1 = 'search_list'
    private int MAX_EXCHANGE_LENGTH = 8;
    private int MAX_NAME_LENGTH = 35;
    private int MAX_SYMBOL_LENGTH = 12;

    public StockPreviewAdapter(Context context, ArrayList<Stock> array, int type) {
        super(context, 0, array);
        previewType = type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Stock stock = (Stock)getItem(position);
        String changeStr, changePercentStr, exchangeStr, nameStr, symbolStr,
                askPriceStr, bidPriceStr;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.stock_preview, parent, false);
        }
        convertView.setMinimumHeight(160);          //unit = pixels
        TextView symbol = (TextView) convertView.findViewById(R.id.stock_prev_symbol);
        TextView name = (TextView) convertView.findViewById(R.id.stock_prev_name);
        TextView askPrice = (TextView) convertView.findViewById(R.id.stock_prev_ask_price);
        TextView bidPrice = (TextView) convertView.findViewById(R.id.stock_prev_bid_price);
        TextView exchange = (TextView) convertView.findViewById(R.id.stock_prev_exchange);
        TextView change = (TextView) convertView.findViewById(R.id.stock_prev_change);
        TextView changePercent = (TextView) convertView.findViewById(R.id.stock_prev_change_percent);
        symbolStr = stock.getSymbol();
        nameStr = stock.getName();
        exchangeStr = stock.getExchange();
        bidPriceStr = stock.getBidPrice();
        askPriceStr = stock.getAskPrice();
        changeStr = stock.getChange();
        changePercentStr = stock.getChangePercent();
        if(symbolStr.length() > MAX_SYMBOL_LENGTH) {
            symbolStr = symbolStr.substring(0, MAX_SYMBOL_LENGTH-1)+"...";
        }
        if(nameStr.length() > MAX_NAME_LENGTH) {
            nameStr = nameStr.substring(0, MAX_NAME_LENGTH-1)+"...";
        }
        if(exchangeStr.length() > MAX_EXCHANGE_LENGTH) {
            exchangeStr = getInitials(exchangeStr);
        }
        exchange.setText(exchangeStr);
        symbol.setText(symbolStr);
        name.setText(nameStr);

        if(previewType == 0) {      // main feed preview
            LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.stock_prev_left_layout);
            layout.setPadding(0, DPtoPixel(10, getContext()), 0,  DPtoPixel(10, getContext()));
            layout = (LinearLayout) convertView.findViewById(R.id.stock_prev_middle_layout);
            layout.setPadding(0, DPtoPixel(5, getContext()), 0,  0);
            layout = (LinearLayout) convertView.findViewById(R.id.stock_prev_right_layout);
            layout.setPadding(0, DPtoPixel(5, getContext()), 0,  0);

            if(!askPriceStr.equals(DATA_NOT_AVAILABLE) && !bidPriceStr.equals(DATA_NOT_AVAILABLE)) {
                bidPrice.setText("$"+bidPriceStr);
                askPrice.setText("$"+askPriceStr);
            } else {
                bidPrice.setText(DATA_NOT_AVAILABLE);
                askPrice.setText(DATA_NOT_AVAILABLE);
            }
            changePercent.setText("("+changePercentStr+")");
            change.setText(changeStr);
            if(!changeStr.equals(DATA_NOT_AVAILABLE) &&
                    !changePercentStr.equals(DATA_NOT_AVAILABLE)) {
                if(changeStr.charAt(0) == '+') {
                    changePercent.setTextColor(GREEN_COLOR);
                    change.setTextColor(GREEN_COLOR);
                } else if(changeStr.charAt(0) == '-') {
                    changePercent.setTextColor(RED_COLOR);
                    change.setTextColor(RED_COLOR);
                }
            }
            TextView view = (TextView)convertView.findViewById(R.id.stock_prev_amount);
            if(currentFeed == MainActivity.feedId.OWNED_FEED) {
                view.setText(String.valueOf(stock.getAmount()));
            } else {
                view.setVisibility(View.GONE);
                view = (TextView)convertView.findViewById(R.id.stock_prev_amount_title);
                view.setVisibility(View.GONE);
            }
        } else if(previewType == 1) {           // Search preview
            TextView view = (TextView)convertView.findViewById(R.id.stock_prev_amount);
            view.setVisibility(View.GONE);
            view = (TextView)convertView.findViewById(R.id.stock_prev_amount_title);
            view.setVisibility(View.GONE);
            askPrice.setVisibility(View.GONE);
            bidPrice.setVisibility(View.GONE);
            askPrice = (TextView) convertView.findViewById(R.id.stock_prev_ask_price_text);
            bidPrice = (TextView) convertView.findViewById(R.id.stock_prev_bid_price_text);
            askPrice.setVisibility(View.GONE);
            bidPrice.setVisibility(View.GONE);
            int num = position % 2;
            if(num == 0) {
                symbol.setTextColor(GRAY_COLOR);
                name.setTextColor(GRAY_COLOR);
                exchange.setTextColor(GRAY_COLOR);

            } else {
                symbol.setTextColor(COPPER_COLOR);
                name.setTextColor(COPPER_COLOR);
                exchange.setTextColor(COPPER_COLOR);
            }
        }
        setFontAndSize(name, "fonts/ostrich-inline.ttf", 20);
        setFontAndSize(symbol, "fonts/ostrich-black.ttf", 18);
        setFontAndSize(exchange, "fonts/ostrich-black.ttf", 16);
        return convertView;
    }

    private void setFontAndSize(TextView view, String typefaceAsset, int size) {
        view.setTypeface(Typeface.createFromAsset(getContext().getAssets(), typefaceAsset));
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

}
