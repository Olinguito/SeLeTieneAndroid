package co.olinguito.seletiene.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import co.olinguito.seletiene.app.util.BaseAdapter;
import co.olinguito.seletiene.app.util.RequestSingleton;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONException;
import org.json.JSONObject;

public class ItemGridAdapter extends BaseAdapter {

    public ItemGridAdapter(Context context, View v) {
        super(context, v);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        GridViewHolder vh = (GridViewHolder) viewHolder;
        try {
            JSONObject item = mData.getJSONObject(i);
            String title = (String) item.get("title");
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
            vh.title.setText(title);
            vh.name.setText((String) item.get("ownerName"));
            vh.rating.setRating(((Double) item.get("rating")).floatValue());
            vh.image.setImageUrl(RANDOM_IMAGE_GRID, RequestSingleton.getInstance(this.ctx).getImageLoader());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class GridViewHolder extends ViewHolder {
        public TextView title;
        public TextView name;
        public RatingBar rating;
        public NetworkImageView image;

        public GridViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.g_title);
            name = (TextView) view.findViewById(R.id.g_name);
            rating = (RatingBar) view.findViewById(R.id.g_rating);
            image = (NetworkImageView) view.findViewById(R.id.g_picture);
        }
    }
}