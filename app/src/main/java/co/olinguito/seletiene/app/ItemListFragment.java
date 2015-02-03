package co.olinguito.seletiene.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.DefaultApiErrorHandler;
import co.olinguito.seletiene.app.util.FilterDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemListFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, FilterDialog.FilterListener {

    public static final int GRID_MODE = 0;
    public static final int LIST_MODE = 1;
    public static final float DEFAULT_STARS = 3.0f;
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private ArrayList<Integer> mFavorites = new ArrayList<>();
    private int mCurrentMode = LIST_MODE;

    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mList;
    private ItemListAdapter mListAdapter;
    private ItemGridAdapter mGridAdapter;
    private GridLayoutManager mLayoutManager;
    private HashMap<String, String> mSearchParams = new HashMap<String, String>() {{
        put("q", "");
        put("minStars", String.valueOf(DEFAULT_STARS));
        put("order", "");
    }};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // order spinner
        final String[] orderValues = getResources().getStringArray(R.array.order_choices_vals);
        Spinner orderSpinner = (Spinner) view.findViewById(R.id.order_spinner);
        final ArrayAdapter<CharSequence> orderAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.order_choices, R.layout.order_spinner_text);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(orderAdapter);
        // order change
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSearchParams.put("order", orderValues[position]);
                requestItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("list>", "nada");
            }
        });
        // filter button
        Button mFilterBtn = (Button) view.findViewById(R.id.filter_button);
        mFilterBtn.setOnClickListener(this);
        // refresh view
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_list);
        mSwipeLayout.setColorSchemeResources(R.color.red1, R.color.red2, R.color.red3, R.color.red4);
        mSwipeLayout.setOnRefreshListener(this);
        // switch radio buttons
        RadioButton switchList = (RadioButton) view.findViewById(R.id.switch_list);
        RadioButton switchGrid = (RadioButton) view.findViewById(R.id.switch_grid);
        switchGrid.setOnClickListener(this);
        switchList.setOnClickListener(this);
        // listview and gridview
        View emptyView = view.findViewById(R.id.empty_view);
        mListAdapter = new ItemListAdapter(getActivity(), emptyView);
        mGridAdapter = new ItemGridAdapter(getActivity(), emptyView);
        mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mList = (RecyclerView) view.findViewById(R.id.items);
        // fix on scroll up firing refresh
        mList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeLayout.setEnabled(topRowVerticalPosition >= 0);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        mList.setLayoutManager(mLayoutManager);
        switchModeTo(mCurrentMode);
        if (mCurrentMode == LIST_MODE) {
            switchList.setChecked(true);
        } else {
            switchGrid.setChecked(true);
        }
        return view;
    }

    @Override
    public void onRefresh() {
        requestItems();
    }

    public void requestItems() {
        mSwipeLayout.setRefreshing(true);
        Api.getUserFavorites(new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray favorites) {
                mFavorites = getFavoritesArray(favorites);
                Api.getProductsAndServices(mSearchParams, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mSwipeLayout.setRefreshing(false);
                        JSONArray resultsWithFavorites = getResultsWithFavorites(response);
                        mGridAdapter.setData(resultsWithFavorites);
                        mListAdapter.setData(resultsWithFavorites);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeLayout.setRefreshing(false);
                        Api.handleResponseError(getActivity(), error);
                    }
                });
            }
        }, new DefaultApiErrorHandler(getActivity())); // TODO stop refreshing on error
    }

    @Override
    public void onClick(View view) {
        boolean checked;
        switch (view.getId()) {
            case R.id.switch_grid:
                checked = ((RadioButton) view).isChecked();
                if (checked)
                    switchModeTo(GRID_MODE);
                break;
            case R.id.switch_list:
                checked = ((RadioButton) view).isChecked();
                if (checked)
                    switchModeTo(LIST_MODE);
                break;
            case R.id.filter_button:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) ft.remove(prev);
                ft.addToBackStack(null);
                // Create and show the dialog.
                FilterDialog filterDialog = FilterDialog.newInstance(mSearchParams);
                filterDialog.setFilterChangeListener(this);
                filterDialog.show(ft, "dialog");
                break;
        }
    }

    public void switchModeTo(int mode) {
        mCurrentMode = mode;
        if (mode == LIST_MODE) {
            mLayoutManager.setSpanCount(1);
            mList.setAdapter(mListAdapter);
        } else {
            mLayoutManager.setSpanCount(getResources().getInteger(R.integer.column_count));
            mList.setAdapter(mGridAdapter);
        }
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
//            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onFilterChange(HashMap<String, String> searchParams) {
        mSearchParams = searchParams;
        requestItems();
    }

    private ArrayList<Integer> getFavoritesArray(JSONArray results) {
        ArrayList<Integer> favorites = new ArrayList<>();
        try {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                favorites.add(item.getInt("id"));
            }
        } catch (JSONException ignored) {
        }
        return favorites;
    }

    private JSONArray getResultsWithFavorites(JSONArray results) {
        try {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                item.put("favorite", isInFavorites(item.getInt("id")));
                results.put(i, item);
            }
        } catch (JSONException ignored) {
        }
        return results;
    }

    private boolean isInFavorites(int id) {
        for (int itemId : mFavorites) {
            if (itemId == id) return true;
        }
        return false;
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    /*public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }*/

    /*private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }*/
}
