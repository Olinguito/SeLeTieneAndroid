package co.olinguito.seletiene.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.BaseAdapter;
import co.olinguito.seletiene.app.util.RequestSingleton;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONException;
import org.json.JSONObject;

public class ItemListAdapter extends BaseAdapter {

    public ItemListAdapter(Context context, View v) {
        super(context, v);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        ListViewHolder vh = (ListViewHolder) viewHolder;
        try {
            JSONObject item = mData.getJSONObject(i);
            String title = item.getString("Title");
            int type = item.getInt("Type");

            title = title.substring(0, 1).toUpperCase() + title.substring(1);
            vh.title.setText(title);
            vh.description.setText(item.getString("Description"));
            vh.name.setText(item.getString("OwnerName"));
            vh.rating.setRating(((Double) item.getDouble("Rating")).floatValue());
            if (type == Api.TYPE_PRODUCT)
                vh.image.setDefaultImageResId(R.drawable.product_img);
            else if (type == Api.TYPE_SERVICE)
                vh.image.setDefaultImageResId(R.drawable.service_img);
            if (!item.isNull("ImageFile"))
                vh.image.setImageUrl(Api.BASE_URL + item.getString("ImageFile"), RequestSingleton.getInstance(this.ctx).getImageLoader());
        } catch (JSONException e) {
            Log.e("JSON_ERROR>", e.getMessage());
        }
    }

    public class ListViewHolder extends ViewHolder {
        public TextView title;
        public TextView name;
        public TextView description;
        public RatingBar rating;
        public NetworkImageView image;

        public ListViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.l_title);
            name = (TextView) view.findViewById(R.id.l_name);
            description = (TextView) view.findViewById(R.id.l_desc);
            rating = (RatingBar) view.findViewById(R.id.l_rating);
            image = (NetworkImageView) view.findViewById(R.id.l_picture);
        }
    }
}