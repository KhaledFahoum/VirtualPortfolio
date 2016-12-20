package net.fahoum.virtualportfolio;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.TextView;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static net.fahoum.virtualportfolio.Utility.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchStockFragment.OnDataPass {

    private Account currentAccount = null;
    private ArrayList<Stock> ownedFeed = null;
    private ArrayList<Stock> watchFeed = null;
    private ArrayList<ArrayList<Stock>> allFeeds = null;
    private StockPreviewAdapter watchFeedAdapter = null;
    private StockPreviewAdapter ownedFeedAdapter = null;
    private ArrayList<StockPreviewAdapter> allAdapters = null;
    private FeedRefreshTask task = null;
    private enum feedId { WATCH_FEED, OWNED_FEED };
    private feedId currentFeed;
    private com.baoyz.swipemenulistview.SwipeMenuListView stocksView;
    private String currentDate = "";
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar;
        ActionBarDrawerToggle toggle;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stocksView = (com.baoyz.swipemenulistview.SwipeMenuListView) findViewById(R.id.stocks_view);
        setTitle(R.string.watch_title);
        accountManager = new AccountManager();
        currentAccount = accountManager.getLastLoggedAccount();
        if(currentAccount == null) {    // new user
            currentAccount = accountManager.logInNewAccount();
        }
        currentFeed = feedId.WATCH_FEED;
        currentDate = getDateAsString();
        initializeFeedsAndAdapters();
        bindAdapterToCurrentFeed();
        importCurrentAccountFeed();
        refreshFeed(currentFeed);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(120, 0, 0)));
                deleteItem.setWidth(width-100);
                deleteItem.setIcon(R.drawable.ic_delete_forever_black_24dp);
                menu.addMenuItem(deleteItem);
            }
        };
        stocksView.setMenuCreator(creator);
        stocksView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
    //    stocksView.setCloseInterpolator(new BounceInterpolator());
    //    stocksView.setOpenInterpolator(new BounceInterpolator());
        stocksView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        stocksView.smoothCloseMenu();
                        watchFeed.remove(position);
                        currentAccount.getWatchedStocks().remove(position);
                        watchFeedAdapter.notifyDataSetChanged();
                        break;
                  /*  case 1:
                        // camera
                        break;*/
                }
                // false: close the menu; true: don't close the menu.
                return false;
            }
        });
        stocksView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
            }

            @Override
            public void onSwipeEnd(int position) {
                stocksView.smoothOpenMenu(position);
            }
        });
        // Initialize toolbar, navigation drawer, floating action button.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setRippleColor(Color.rgb(153, 31, 0));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                DialogFragment newFragment = SearchStockFragment.newInstance();
                newFragment.show(transaction, "fragment_search_stock");
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View View) {
                super.onDrawerOpened(View);
                TextView view = (TextView) findViewById(R.id.portfolio_name);
                view.setText(currentAccount.getName());
                view = (TextView) findViewById(R.id.portfolio_creation_date);
                view.setText(currentAccount.getCreationDate());
                view = (TextView) findViewById(R.id.portfolio_balance_value);
                view.setText(String.valueOf(currentAccount.getBalance()));
                invalidateOptionsMenu();
            }
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                bindAdapterToCurrentFeed();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void bindAdapterToCurrentFeed() {
        StockPreviewAdapter adapter = allAdapters.get(currentFeed.ordinal());
        stocksView.setAdapter(adapter);
        //adapter.notifyDataSetChanged();
        stocksView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App.setDisplayStock(allFeeds.get(currentFeed.ordinal()).get(position));
                android.support.v4.app.FragmentTransaction transaction =
                                        getSupportFragmentManager().beginTransaction();
                DialogFragment newFragment = StockViewFragment.newInstance();
                newFragment.show(transaction, "fragment_stock_view");
            }
        });
    }

    public class FeedRefreshTask extends AsyncTask<String, Void, String> {
        private URL url;
        private feedId id;
        private File temporaryCacheFile = null;

        public FeedRefreshTask(String url, feedId id) throws MalformedURLException {
            this.url = new URL(url);
            this.id = id;
        }

        @Override
        protected String doInBackground(String... params) {
            int count;
            try {
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream input = new BufferedInputStream(url.openStream(), 10000);
                temporaryCacheFile = new File(getCacheDir(), "tempfile"+id.ordinal()+".csv");
                OutputStream output = new FileOutputStream(temporaryCacheFile.getPath());
                byte data[] = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String result) {
            BufferedReader reader;
            String record;  // Record contains data on one stock, starting with its symbol.
            String[] values;
            int index;
            try {
                reader = new BufferedReader(new FileReader(temporaryCacheFile));
                while ((record = reader.readLine()) != null) {
                    values = record.split(",");
                    index = findStockIndexBySymbol(values[0].substring(
                                1, values[0].length() - 1), id);
                    if(index == -1) {
                        continue;
                    }
                    Stock stock = allFeeds.get(id.ordinal()).get(index);
                    stock.updateStock(values);
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            allAdapters.get(id.ordinal()).notifyDataSetChanged();
            task = null;
        }
    }

    public void refreshFeed(feedId id) {
        if(task != null)
            return;
        String url = "";
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Stock> feed = allFeeds.get(id.ordinal());
        if(feed.size() < 1) {
            allAdapters.get(id.ordinal()).notifyDataSetChanged();
            return;
        }
        for(Stock stock : feed) {
            list.add(stock.getSymbol());
        }
        url = buildYahooFinanceURL(list, App.getQueryFlagsString());
        try {
            task = new FeedRefreshTask(url, id);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        startRefreshTask(task);          // Task will update UI on post-execute.
    }

    public int findStockIndexBySymbol(String searchSymbol, feedId currentFeed) {
        int i = 0;
        for(Stock stock : allFeeds.get(currentFeed.ordinal())) {
            if(stock.getSymbol().equals(searchSymbol)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void initializeFeedsAndAdapters() {
        watchFeed = App.watchFeed;
        ownedFeed = App.ownedFeed;
        allFeeds = new ArrayList<ArrayList<Stock>>();
        allFeeds.add(watchFeed);                //index=0
        allFeeds.add(ownedFeed);                //index=1
        watchFeedAdapter = new StockPreviewAdapter(this, watchFeed, 0);
        ownedFeedAdapter = new StockPreviewAdapter(this, ownedFeed, 0);
        allAdapters = new ArrayList<StockPreviewAdapter>();
        allAdapters.add(watchFeedAdapter);      //index=0
        allAdapters.add(ownedFeedAdapter);      //index=1
    }

    public void importCurrentAccountFeed() {
        if(currentAccount == null || watchFeed == null || ownedFeed == null) {
            return;
        }
        ArrayList<Stock> watchedFeedList = allFeeds.get(0);
        for(Stock stock : currentAccount.getWatchedStocks()) {
            watchedFeedList.add(stock);
        }
        ArrayList<Stock> ownedFeedList = allFeeds.get(1);
        for(PurchasedStock stock : currentAccount.getOwnedStocks()) {
            ownedFeedList.add(stock);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.force_refresh) {
            refreshFeed(currentFeed);
            return true;
        } else if(item.getItemId() == R.id.clear_all) {
            currentAccount.getWatchedStocks().clear();
            allFeeds.get(currentFeed.ordinal()).clear();
            refreshFeed(currentFeed);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.watched_tickers) {
            currentFeed = feedId.WATCH_FEED;
            setTitle(R.string.watch_title);
        } else if(id == R.id.owned_shares) {
            currentFeed = feedId.OWNED_FEED;
            setTitle(R.string.owned_title);
        } else if(id == R.id.summary_report) {

        } else if(id == R.id.switch_account) {

        } else if(id == R.id.share_portfolio) {

        } else if(id == R.id.settings) {
            Intent myIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(myIntent, 0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            accountManager.saveAccountStructure();
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDataPass(Stock result) {
        int index = findStockIndexBySymbol(result.getSymbol(), currentFeed);
        if(index != -1) {
            return;
        }
        currentAccount.watchNewStock(result);
        watchFeed.add(result);
        refreshFeed(feedId.WATCH_FEED);
    }

    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }
}
