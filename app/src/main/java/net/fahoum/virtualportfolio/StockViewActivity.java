package net.fahoum.virtualportfolio;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by khaled on 06-Apr-16.
 */
public class StockViewActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_view);
        Stock displayStock = ((App)getApplicationContext()).getDisplayStock();
        TextView stockTickerText = (TextView) findViewById(R.id.stock_ticker);
        stockTickerText.setText(displayStock.getSymbol());
    }

}
