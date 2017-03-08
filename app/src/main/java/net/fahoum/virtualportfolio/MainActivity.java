package net.fahoum.virtualportfolio;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static net.fahoum.virtualportfolio.Utility.*;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchStockFragment.OnDataPass {

    public enum feedId { WATCH_FEED, OWNED_FEED };
    public static feedId currentFeed;
    public static ArrayList<ArrayList<Stock>> allFeeds = null;
    public static ArrayList<StockPreviewAdapter> allAdapters = null;

    private Account currentAccount = null;
    private ArrayList<Stock> ownedFeed = null;
    private ArrayList<Stock> watchFeed = null;
    private StockPreviewAdapter watchFeedAdapter = null;
    private StockPreviewAdapter ownedFeedAdapter = null;
    public static FeedRefreshTask refreshTask = null;
    private com.baoyz.swipemenulistview.SwipeMenuListView stocksView;
    private String currentDate = "";
    private AccountManager accountManager;
    // units: milliseconds
    private int FEED_REFRESH_INITIAL_DELAY = 1000*2, FEED_REFRESH_DELAY_INTERVAL = 1000*2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar;
        ActionBarDrawerToggle toggle;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.setMainActivity(this);
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
/*
        // Setting up auto-refresh timer
        Timer refreshTimer = new Timer("RefreshTimer", true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshFeed(currentFeed);
            }
        }, FEED_REFRESH_INITIAL_DELAY, FEED_REFRESH_DELAY_INTERVAL); */
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
                newFragment.show(transaction, "fragment_stock_view_tag");
            }
        });
    }

    public void refreshFeed(feedId id) {
        if(refreshTask != null) {
            return;
        }
        try {
            refreshTask = new FeedRefreshTask(id);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        startRefreshTask(refreshTask);          // Task will update UI on post-execute.
    }

    public static int findStockIndexBySymbol(String searchSymbol, feedId targetFeed) {
        int i = 0;
        for(Stock stock : allFeeds.get(targetFeed.ordinal())) {
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
        for(Stock stock : currentAccount.getOwnedStocks()) {
            ownedFeedList.add(stock);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.force_refresh) {
            printToast("Refreshing...", UI_TOAST);
            refreshFeed(currentFeed);
            return true;
        } else if(item.getItemId() == R.id.clear_all) {
            currentAccount.getWatchedStocks().clear();
            allFeeds.get(currentFeed.ordinal()).clear();
            refreshFeed(currentFeed);
            printToast("Cleared watched stocks", UI_TOAST);
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
        int index = findStockIndexBySymbol(result.getSymbol(), feedId.WATCH_FEED);
        if(index != -1) {
            return;
        }
        index = findStockIndexBySymbol(result.getSymbol(), feedId.OWNED_FEED);
        if(index != -1) {
            Stock stock = ownedFeed.get(index);
            currentAccount.watchNewStock(stock);
            watchFeed.add(stock);
        } else {
            currentAccount.watchNewStock(result);
            watchFeed.add(result);
        }
        refreshFeed(feedId.WATCH_FEED);
    }

    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }
}
