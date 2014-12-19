package co.olinguito.seletiene.app.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import org.json.JSONArray;

public abstract class BaseAdapter extends RecyclerView.Adapter{

    //TODO remove when server can serve pictures
    public static final String RANDOM_IMAGE_GRID = "http://lorempixel.com/320/160/food";
    public static final String RANDOM_IMAGE_LIST = "http://lorempixel.com/200/200/food";
    protected final Context ctx;
    private final View mEmptyView;

    protected JSONArray mData;

    public BaseAdapter(Context context, View v) {
        this.ctx = context;
        mEmptyView = v;
        mData = new JSONArray();
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }
        });
        checkEmpty();
    }

    public void setData(JSONArray data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.length();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("CLICK", "Item clicked");
        }
    }

    protected void checkEmpty() {
        if (getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }
}
