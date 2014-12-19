package co.olinguito.seletiene.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import co.olinguito.seletiene.app.util.*;
import co.olinguito.seletiene.app.util.BaseAdapter;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONArray;

import java.util.HashMap;

public class ItemListFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    public static final int GRID_MODE = 0;
    public static final int LIST_MODE = 1;

    private RecyclerView mItemsView;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mList;
    private ItemListAdapter mListAdapter;
    private ItemGridAdapter mGridAdapter;
    private GridLayoutManager mLayoutManager;
    private Button mFilterBtn;
    private HashMap mSearchParams;

    public interface Callbacks {

        public void onItemSelected(String id);
    }

    /**
     * Implementation of the {@link Callbacks} interface that does nothing.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: replace with a real list adapter.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // order spinner
        Spinner orderSpinner = (Spinner) view.findViewById(R.id.order_spinner);
        ArrayAdapter<CharSequence> orderAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.order_choices, R.layout.order_spinner_text);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(orderAdapter);
        // filter button
        mFilterBtn = (Button) view.findViewById(R.id.filter_button);
        mFilterBtn.setOnClickListener(this);
        // refresh view
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_list);
        mSwipeLayout.setColorSchemeResources(R.color.red1, R.color.red2, R.color.red3, R.color.red4);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setEnabled(true);
        // switch radio buttons
        RadioButton switchList = (RadioButton) view.findViewById(R.id.switch_list);
        RadioButton switchGrid = (RadioButton) view.findViewById(R.id.switch_grid);
        switchGrid.setOnClickListener(this);
        switchList.setOnClickListener(this);
        switchList.setChecked(true);
        // listview and gridview
        View emptyView = view.findViewById(R.id.empty_view);
        mListAdapter = new ItemListAdapter(getActivity(), emptyView);
        mGridAdapter = new ItemGridAdapter(getActivity(), emptyView);
        mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mList = (RecyclerView) view.findViewById(R.id.items);
        mList.setLayoutManager(mLayoutManager);
        mList.setAdapter(mListAdapter);
        // search params
        mSearchParams = new HashMap();
        Log.d("1>>", "create view");
        return view;
    }

    @Override
    public void onRefresh() {
        requestItems();
    }

    public void requestItems() {
        Log.d("->", "requesting");
        mSwipeLayout.setRefreshing(true);
        Api.getProductsAndServices(mSearchParams, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mSwipeLayout.setRefreshing(false);
                mGridAdapter.setData(response);
                mListAdapter.setData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeLayout.setRefreshing(false);
                Api.handleResponseError(getActivity(), error);
            }
        });
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
                DialogFragment filterDialog = new FilterDialog();
                filterDialog.show(ft, "dialog");
                break;
        }
    }

    public void switchModeTo(int mode) {
//        DividerItemDecoration separator = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        if (mode == LIST_MODE) {
            mLayoutManager.setSpanCount(1);
//            mList.addItemDecoration(separator);
            mList.setAdapter(mListAdapter);
        } else {
            mLayoutManager.setSpanCount(2);
//            mList.removeItemDecoration(separator);
            mList.setAdapter(mGridAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        requestItems();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /*@Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
    }*/

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
