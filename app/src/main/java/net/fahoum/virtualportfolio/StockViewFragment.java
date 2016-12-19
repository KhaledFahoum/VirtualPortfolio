package net.fahoum.virtualportfolio;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static net.fahoum.virtualportfolio.Utility.filterInputCharacters;
import static net.fahoum.virtualportfolio.Utility.startMySearchTask;

public class StockViewFragment extends DialogFragment {
    private View stockView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        stockView = inflater.inflate(R.layout.fragment_stock_view, container);
        Stock displayStock = App.getDisplayStock();
        TextView stockTickerText = (TextView) stockView.findViewById(R.id.stock_ticker);
        stockTickerText.setText(displayStock.getSymbol());
        return stockView;
    }

    static StockViewFragment newInstance() {
        StockViewFragment fragment = new StockViewFragment();
        return fragment;
    }

    public StockViewFragment() {
        // Empty constructor required for DialogFragment
    }
}
