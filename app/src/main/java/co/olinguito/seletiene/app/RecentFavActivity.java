package co.olinguito.seletiene.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import co.olinguito.seletiene.app.util.*;
import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RecentFavActivity extends ChildActivity {

    public static final String MODE_TAG = "mode_tag";
    public static final int FAV_MODE = 1;
    public static final int RECENT_MODE = 2;
    private ListView mList;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_fav);
        final int mode = getIntent().getIntExtra(MODE_TAG, 0);
        mList = (ListView) findViewById(R.id.rec_fav_list);
        mList.setEmptyView(findViewById(R.id.empty_view));
        if (mode == FAV_MODE) {
            setTitle(R.string.profile_favorites);
            mProgress = ProgressDialog.show(this, "", getString(R.string.loading_fav), true);
            loadFavorites();
        } else if (mode == RECENT_MODE) {
            setTitle(R.string.profile_recents);
            mProgress = ProgressDialog.show(this, "", getString(R.string.loading_rec), true);
            loadRecents();
        }

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject item = (JSONObject) parent.getItemAtPosition(position);
                try {
                    if (mode == FAV_MODE) item.put("Favorite", true);
                } catch (JSONException ignored) {
                }
                Intent intent = new Intent(getBaseContext(), ItemDetailActivity.class);
                intent.putExtra(ItemDetailFragment.JSON_OBJECT_STRING, item.toString());
                startActivity(intent);
            }
        });
    }

    protected void loadFavorites() {
        Api.getUserFavorites(new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mList.setAdapter(new FavoritesAdapter(response));
                if (mProgress.isShowing()) mProgress.dismiss();
            }
        }, new DefaultApiErrorHandler(mProgress));
    }

    protected void loadRecents() {
        mList.setAdapter(new RecentAdapter(userManager.getRecent()));
        if (mProgress.isShowing()) mProgress.dismiss();
    }

    public class RecentAdapter extends BaseAdapter {

        private final RecentPnS items;

        public RecentAdapter(RecentPnS items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return this.items.size();
        }

        @Override
        public Object getItem(int position) {
            // access the queue in reverse order
            int invertedPosition = -position + getCount() - 1;
            return this.items.get(invertedPosition);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    public class FavoritesAdapter extends BaseAdapter {

        private final JSONArray items;

        public FavoritesAdapter(JSONArray data) {
            this.items = data;
        }

        @Override
        public int getCount() {
            return items.length();
        }

        @Override
        public Object getItem(int position) {
            JSONObject item = null;
            try {
                item = items.getJSONObject(position);
            } catch (JSONException ignored) {
            }
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private abstract class BaseAdapter extends android.widget.BaseAdapter {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            LayoutInflater inflater = getLayoutInflater();
            if (convertView == null)
                vi = inflater.inflate(R.layout.list_item, null);
            TextView title = (TextView) vi.findViewById(R.id.l_title);
            TextView name = (TextView) vi.findViewById(R.id.l_name);
            TextView description = (TextView) vi.findViewById(R.id.l_desc);
            RatingBar rating = (RatingBar) vi.findViewById(R.id.l_rating);
            NetworkImageView image = (NetworkImageView) vi.findViewById(R.id.l_picture);
            // set data
            JSONObject item = (JSONObject) getItem(position);
            try {
                title.setText(item.getString("Title"));
                name.setText(item.getString("OwnerName"));
                description.setText(item.getString("Description"));
                rating.setRating(((Double) item.get("Rating")).floatValue());
                // image
                int type = item.getInt("Type");
                if (type == Api.TYPE_PRODUCT)
                    image.setDefaultImageResId(R.drawable.product_img);
                else if (type == Api.TYPE_SERVICE)
                    image.setDefaultImageResId(R.drawable.service_img);
                if (!item.isNull("ImageFile"))
                    image.setImageUrl(Api.BASE_URL + item.getString("ImageFile"), RequestSingleton.getInstance(RecentFavActivity.this).getImageLoader());
            } catch (JSONException e) {
                Log.e("JSON_ERROR>", e.getMessage());
            }
            return vi;
        }
    }
}
