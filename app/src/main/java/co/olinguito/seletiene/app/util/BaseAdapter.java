package co.olinguito.seletiene.app.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseAdapter extends RecyclerView.Adapter {

    //TODO remove when server can serve pictures
    public static final int TYPE_PRODUCT = 0;
    public static final int TYPE_SERVICE = 1;
    protected final Context ctx;
    private final View mEmptyView;
    protected ClickListener mClickListener;

    protected JSONArray mData;

    public BaseAdapter(Context context, View v) {
        this.ctx = context;
        mClickListener = (ClickListener) context;
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

    protected void checkEmpty() {
        if (getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    public interface ClickListener {
        public void onItemClick(View v, JSONObject item);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                JSONObject item = mData.getJSONObject(getPosition());
                mClickListener.onItemClick(v, item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
