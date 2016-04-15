package net.fahoum.virtualportfolio;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.util.Log;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import static net.fahoum.virtualportfolio.Utility.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchStockFragment.OnDataPass {

    private Account currentAccount = null;
    private ArrayList<Account> allAccounts = null;
    private ArrayList<Stock> ownedFeed = null;
    private ArrayList<Stock> watchFeed = null;
    private ArrayList<ArrayList<Stock>> allFeeds = null;
    private StockPreviewAdapter watchFeedAdapter = null;
    private StockPreviewAdapter ownedFeedAdapter = null;
    private ArrayList<StockPreviewAdapter> allAdapters = null;
    private File networkReplyFile = null;
    private FeedRefreshTask task = null;
    private String currentNetworkFlag = null;
    private ArrayList<String> currentNetworkFlagArray = null;
    private enum feedId { WATCH_FEED, OWNED_FEED };
    private feedId currentlyViewedFeed;
    private ActionBarDrawerToggle toggle;
    private com.baoyz.swipemenulistview.SwipeMenuListView stocksView;
    private String storageDataFilename = "virtual_portfolio_data.dat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stocksView = (com.baoyz.swipemenulistview.SwipeMenuListView) findViewById(R.id.stocks_view);
        setTitle(R.string.watch_title);
        restoreAccountsState();
        currentAccount = logInCurrentAccount();
        currentlyViewedFeed = feedId.WATCH_FEED;
        initializeNetworkFlags();
        initializeFeedsAndAdapters();;
        bindAdapterToCurrentFeed();
        importCurrentAccountFeed();
        refreshFeed(currentlyViewedFeed);

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
/*                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                openItem.setWidth(500);
                openItem.setTitle("Open");
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem); */
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
                // false : close the menu; true : not close the menu
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
        // Initialize toolbar, navigation drawer, floation action button.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        fab.setRippleColor(Color.RED);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchStockFragment newF = new SearchStockFragment();
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
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void onDestroy() {
        storeAccountsState();
        super.onDestroy();
    }

    public void storeAccountsState() {
        File file = new File(getApplicationContext().getFilesDir(), storageDataFilename);
        BufferedWriter writer;
        String record;
        try {
            writer = new BufferedWriter((new FileWriter((file))));
            writer.write("ABCD BITCH");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Account logInCurrentAccount() {
        return new Account("Khaled's Portfolio");
    }

    public void restoreAccountsState() {
        File file = new File(getApplicationContext().getFilesDir(), storageDataFilename);
        BufferedReader reader;
        String record;
        String[] tokens;
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((record = reader.readLine()) != null) {
                Log.d("acc", record);
                //tokens = record.split(",");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bindAdapterToCurrentFeed() {
        stocksView.setAdapter(allAdapters.get(currentlyViewedFeed.ordinal()));
        stocksView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((App) getApplicationContext()).setDisplayStock(allFeeds.
                        get(currentlyViewedFeed.ordinal()).get(position));
                Intent myIntent = new Intent(view.getContext(), StockViewActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }

    public class FeedRefreshTask extends AsyncTask<String, Void, String> {
        private URL url;
        private feedId id;

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
                networkReplyFile = new File(getCacheDir(), "tempfile"+id.ordinal()+".csv");
                OutputStream output = new FileOutputStream(networkReplyFile.getPath());
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
            updateFeedWithReplyFile(id);
            allAdapters.get(id.ordinal()).notifyDataSetChanged();
            task = null;
        }
    }

    public void refreshFeed(feedId id) {
        if(task != null) return;
        String url = "";
        Stock stock;
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Stock> feed = allFeeds.get(currentlyViewedFeed.ordinal());
        if(feed.size() < 1) return;
        for(int i = 0; i < feed.size(); i++) {
            list.add(feed.get(i).getSymbol());
        }
        url = buildYahooFinanceURL(list, currentNetworkFlag);
        Log.d("network", url);
        try {
            task = new FeedRefreshTask(url, id);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        startMyRefreshTask(task);          // Task will update UI on post-execute.
    }

    public void updateFeedWithReplyFile(feedId feedId) {
        BufferedReader reader;
        String record;
        String[] tokens;
        int index;
        try {
            reader = new BufferedReader(new FileReader(networkReplyFile));
            while ((record = reader.readLine()) != null) {
                tokens = record.split(",");
                index = findStockIndexBySymbol(tokens[0], feedId, true);
                if(index == -1) continue;
                updateStockWithReplyRecord(index, tokens, feedId);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateStockWithReplyRecord(int index, String[] tokens, feedId id){
        Stock stock = allFeeds.get(id.ordinal()).get(index);
        for(int i = 0; i < currentNetworkFlagArray.size(); i++) {
            if(currentNetworkFlagArray.get(i) == "x") continue;
            stock.setValue(tokens[i], currentNetworkFlagArray.get(i));
        }
    }

    public int findStockIndexBySymbol(String symbol, feedId feedId, boolean trimFlag) {
        String feedSymbol;
        String searchSymbol;
        if(trimFlag) {
            searchSymbol = symbol.substring(1, symbol.length() - 1);
        } else {
            searchSymbol = symbol;
        }
        ArrayList<Stock> feedList = allFeeds.get(feedId.ordinal());
        for(int i = 0; i < feedList.size(); i++) {
            feedSymbol = feedList.get(i).getSymbol();
            if(feedSymbol.equals(searchSymbol)) {
                return i;
            }
        }
        return -1;
    }

    public void initializeNetworkFlags() {
        currentNetworkFlagArray = new ArrayList<>();
        currentNetworkFlagArray.add("s");
        currentNetworkFlagArray.add("a");
        currentNetworkFlagArray.add("b");
        currentNetworkFlagArray.add("y");
        currentNetworkFlagArray.add("d");
        currentNetworkFlagArray.add("c1");
        currentNetworkFlagArray.add("p2");
        currentNetworkFlagArray.add("v");
        currentNetworkFlagArray.add("x");
        currentNetworkFlagArray.add("j1");
        currentNetworkFlagArray.add("g");
        currentNetworkFlagArray.add("h");
        currentNetworkFlagArray.add("e");
        String str = "";
        for(int i = 0; i < currentNetworkFlagArray.size(); i++) {
            str = str + currentNetworkFlagArray.get(i);
        }
        currentNetworkFlag = str;
    }

    public void initializeFeedsAndAdapters() {
        watchFeed = new ArrayList<Stock>();
        ownedFeed = new ArrayList<Stock>();
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
        if(currentAccount == null || watchFeed == null || ownedFeed == null) return;
        ArrayList<Stock> accountFeedList;
        ArrayList<Stock> displayFeedList;
        accountFeedList = currentAccount.getWatchedStocks();
        displayFeedList = allFeeds.get(0);
        for(int j = 0; j < accountFeedList.size(); j++) {
            displayFeedList.add(accountFeedList.get(j));
        }
        accountFeedList = currentAccount.getOwnedStocks();
        displayFeedList = allFeeds.get(1);
        for(int j = 0; j < accountFeedList.size(); j++) {
            displayFeedList.add(accountFeedList.get(j));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.force_refresh) {
            refreshFeed(currentlyViewedFeed);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.watched_tickers) {
            currentlyViewedFeed = feedId.WATCH_FEED;
            bindAdapterToCurrentFeed();
            setTitle(R.string.watch_title);
        } else if (id == R.id.owned_shares) {
            currentlyViewedFeed = feedId.OWNED_FEED;
            bindAdapterToCurrentFeed();
            setTitle(R.string.owned_title);
        } else if (id == R.id.summary_report) {

        } else if (id == R.id.switch_account) {

        } else if (id == R.id.share_portfolio) {

        } else if (id == R.id.settings) {

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
            super.onBackPressed();
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
        int index = findStockIndexBySymbol(result.getSymbol(), currentlyViewedFeed, false);
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

    public void initTest2() {
        currentAccount.getWatchedStocks().add(new Stock("MSFT"));
        currentAccount.getWatchedStocks().add(new Stock("TWTR"));
        currentAccount.getWatchedStocks().add(new Stock("INTC"));
/*        currentAccount.buyShares("TWTR", 500, 35);
        currentAccount.buyShares("TWTR", 500, 35);
        currentAccount.buyShares("MSFT", 300, 56);
        currentAccount.buyShares("NFLX", 600, 60);*/
    }

    public void initTest() {
        Stock temp;
        String str;
        char chr;
        int num;
        int range = 'Z' - 'A';
        Random gen = new Random();
        for(int i = 0; i < 50; i++) {
            str = "";
            for(int j = 0; j < 4; j++) {
                num = gen.nextInt(range) + 'A';
                chr = (char)num;
                str = str + chr;
            }
            temp = new Stock(str);
            watchFeed.add(temp);
        }
    }
}
