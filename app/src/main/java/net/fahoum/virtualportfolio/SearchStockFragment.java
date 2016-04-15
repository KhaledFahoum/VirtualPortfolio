package net.fahoum.virtualportfolio;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import static net.fahoum.virtualportfolio.Utility.*;

public class SearchStockFragment extends DialogFragment {
    private View view;
    private EditText inputBox;
    private String searchQuery;
    private String searchResult;
    private ArrayList<Stock> results;
    private SearchTask task;
    private OnDataPass dataPasser;
    private InputMethodManager inputManager;
    private StockPreviewAdapter listAdapter;

    public interface OnDataPass {
        public void onDataPass(Stock data);
    }

    static SearchStockFragment newInstance() {
        SearchStockFragment fragment = new SearchStockFragment();
        return fragment;
    }

    public SearchStockFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dataPasser = (OnDataPass) activity;
    }

    /* This one is a bitch. To hide the keyboard, you need the correct window token.
       The window token for the window where the keyboard is still present ("main activity window"),
       not the window where the keyboard first came up. ("search stock window") */
    @Override
    public void onStop() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ListView view = (ListView) getActivity().findViewById(R.id.stocks_view);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        Log.d("stop", "STOPPING");
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search_stock, container);
        inputBox = (EditText) view.findViewById(R.id.stock_search_input);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        inputBox.requestFocus();

        inputBox.addTextChangedListener(
                new TextWatcher() {
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    private Timer timer = new Timer();
                    private final long DELAY = 1000; // milliseconds

                    @Override
                    public void afterTextChanged(final Editable s) {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        inputManager.hideSoftInputFromWindow(inputBox.getWindowToken(), 0);
                                        searchQuery = inputBox.getText().toString();
                                        if(getActivity() == null) return;
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(inputBox == null) return;
                                                inputBox.getText().clear();
                                            }
                                        });
                                        if(searchQuery.length() < 1 || task != null) return;
                                        task = new SearchTask();
                                        startMySearchTask(task);
                                    }
                                },
                                DELAY
                        );
                    }
                }
        );
        results = new ArrayList<>();
        listAdapter = new StockPreviewAdapter(getActivity(), results, 1);
        ListView stocksView = (ListView) view.findViewById(R.id.search_list);
        stocksView.setAdapter(listAdapter);
        stocksView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dataPasser.onDataPass(results.get(position));
            }
        });
        return view;
    }

    public void parseJsonAndDisplayResults() {
        results.clear();
        String str = searchResult, symbol, name, exchange, exchangeDisp;
        String[] tokens = str.split("\"");
        int counter = 0, i = 11;
        Stock stock = null;
        while(i+24*counter < tokens.length) {
            symbol = tokens[i+24*counter];
            name = tokens[i+4+24*counter];
            exchange = tokens[i+8+24*counter];
            exchangeDisp = tokens[i+16+24*counter];
            counter++;
            stock = new Stock(symbol);
            stock.setValue(name, "n");
            stock.setValue(exchangeDisp, "x");
            results.add(stock);
        }
        listAdapter.notifyDataSetChanged();
    }

    public class SearchTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpGet request = new HttpGet("http://d.yimg.com/aq/autoc?query="+searchQuery+"&region=US&lang=en-US");
            try {
                HttpResponse response = httpclient.execute(request);
                HttpEntity resEntity = response.getEntity();
                searchResult = EntityUtils.toString(resEntity);
            } catch(Exception e) {
                e.printStackTrace();
            }
            httpclient.getConnectionManager().shutdown();
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void result) {
            parseJsonAndDisplayResults();
            task = null;
        }
    }
}