package net.fahoum.virtualportfolio;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import static net.fahoum.virtualportfolio.Utility.*;

public class StockPreviewAdapter extends ArrayAdapter<Stock> {
    private int previewType; // 0 = 'stock_view', 1 = 'search_list'
    private int MAX_EXCHANGE_LENGTH = 8;
    private int MAX_NAME_LENGTH = 40;
    private int MAX_SYMBOL_LENGTH = 12;

    public StockPreviewAdapter(Context context, ArrayList<Stock> array, int type) {
        super(context, 0, array);
        previewType = type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Stock stock = (Stock)getItem(position);
        String changeStr, changePercentStr, exchangeStr, nameStr, symbolStr,
                askPriceStr, bidPriceStr;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.stock_preview, parent, false);
        }
        convertView.setMinimumHeight(160);          //unit = pixels
        // Lookup views for data population
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
            symbolStr = symbolStr.substring(0, MAX_NAME_LENGTH-1)+"...";
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

        if(previewType == 0) {
            if(!askPriceStr.equals("") && !bidPriceStr.equals("")) {
                bidPrice.setText(bidPriceStr);
                askPrice.setText(askPriceStr);
            }
            if(!changeStr.equals("N/A") && !changePercentStr.equals("N/A")
                    && !changeStr.equals("") && !changePercentStr.equals("")) {
                changePercent.setText("("+changePercentStr+")");
                change.setText(changeStr);
                if(changeStr.charAt(0) == '+') {
                    changePercent.setTextColor(Color.rgb(50, 153, 0));
                    change.setTextColor(Color.rgb(50, 153, 0));
                } else if(changeStr.charAt(0) == '-') {
                    changePercent.setTextColor(Color.rgb(204, 0, 0));
                    change.setTextColor(Color.rgb(204, 0, 0));
                }
            }
        } else if(previewType == 1) {
            askPrice.setVisibility(View.INVISIBLE);
            bidPrice.setVisibility(View.INVISIBLE);
            askPrice = (TextView) convertView.findViewById(R.id.stock_prev_ask_price_text);
            bidPrice = (TextView) convertView.findViewById(R.id.stock_prev_bid_price_text);
            askPrice.setVisibility(View.INVISIBLE);
            bidPrice.setVisibility(View.INVISIBLE);
            int num = position % 2;
            if(num == 0) {
                symbol.setTextColor(Color.rgb(112, 128, 144));
                name.setTextColor(Color.rgb(112, 128, 144));
                exchange.setTextColor(Color.rgb(112, 128, 144));

            } else {
                symbol.setTextColor(Color.rgb(184, 134, 11));
                name.setTextColor(Color.rgb(184, 134, 11));
                exchange.setTextColor(Color.rgb(184, 134, 11));
            }
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
