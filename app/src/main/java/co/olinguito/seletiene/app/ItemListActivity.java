package co.olinguito.seletiene.app;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import co.olinguito.seletiene.app.util.BaseAdapter;
import co.olinguito.seletiene.app.util.RecentPnS;
import co.olinguito.seletiene.app.util.UserManager;
import org.json.JSONObject;

public class ItemListActivity extends ActionBarActivity
        implements BaseAdapter.ClickListener {

    private final int DETAIL_ACTIVITY_CODE = 69;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private ItemListFragment mListfragment;
    private boolean mDidSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        mListfragment = (ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list);
        handleIntent(getIntent());

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            // TODO: set touched item active
/*
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
*/
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).trim();
            mListfragment.setSearchQuery(query);
            mListfragment.requestItems();
            mDidSearch = true;
        } else {
            mDidSearch = false;
        }
    }

    @Override
    public void onItemClick(View v, JSONObject item) {
        UserManager userManager = new UserManager(this);
        // save viewed item to recent list
        RecentPnS recent = userManager.getRecent();
        recent.add(item);
        userManager.saveRecent(recent);
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.JSON_OBJECT_STRING, item.toString());
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.JSON_OBJECT_STRING, item.toString());
            startActivityForResult(detailIntent, DETAIL_ACTIVITY_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // request items when returning from detail activity to refresh favorites
        if (requestCode == DETAIL_ACTIVITY_CODE) {
            mListfragment.requestItems();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // do 'clean' search on search close
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                if (mDidSearch) {
                    mListfragment.setSearchQuery("");
                    mListfragment.requestItems();
                    mDidSearch = false;
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_offer:
                startActivity(new Intent(this, OfferActivity.class));
                break;
            case R.id.action_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
