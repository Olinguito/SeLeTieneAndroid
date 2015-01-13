package co.olinguito.seletiene.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.RequestSingleton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements View.OnClickListener {

    public static final int TYPE_PRODUCT = 0;
    public static final int TYPE_SERVICE = 1;
    public static final String JSON_OBJECT_STRING = "json_string";
    private JSONObject mItem;
    private int mType;
    private Request mFavRequest;

    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(JSON_OBJECT_STRING)) {
            try {
                int titleRes;
                mItem = new JSONObject(getArguments().getString(JSON_OBJECT_STRING));
                mType = mItem.getInt("type");
                if (mType == TYPE_PRODUCT) titleRes = R.string.product;
                else titleRes = R.string.service;
                getActivity().setTitle(titleRes);
            } catch (JSONException ignored) {
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        if (mItem != null) {
            try {
                float elevation = getResources().getDimension(R.dimen.button_elevation);
                int type = mItem.getInt("type");

                ((TextView) rootView.findViewById(R.id.detail_title)).setText(mItem.getString("title"));
                ((TextView) rootView.findViewById(R.id.detail_owner)).setText(mItem.getString("ownerName"));
                ((TextView) rootView.findViewById(R.id.detail_description)).setText(mItem.getString("description"));
                ViewCompat.setElevation(rootView.findViewById(R.id.detail_image_container), elevation);
                NetworkImageView image = (NetworkImageView) rootView.findViewById(R.id.detail_picture);
                // set default image
                if (type == TYPE_PRODUCT)
                    image.setDefaultImageResId(R.drawable.product_img);
                else if (type == TYPE_SERVICE)
                    image.setDefaultImageResId(R.drawable.service_img);
                // download image if available
                if (!mItem.isNull("imageFile"))
                    image.setImageUrl(Api.BASE_URL + mItem.getString("imageFile"), RequestSingleton.getInstance(getActivity()).getImageLoader());
                CheckBox fav = (CheckBox) rootView.findViewById(R.id.detail_favorite);
                //TODO: check if already favorite
                fav.setOnClickListener(this);
                RatingBar rating = (RatingBar) rootView.findViewById(R.id.detail_rating);
            } catch (JSONException ignored) {
            }
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        CheckBox checkBox = (CheckBox) v;
        boolean checked = checkBox.isChecked();
        int itemId = 0;
        try {
            itemId = mItem.getInt("id");
        } catch (JSONException ignored) {
        }
        switch (v.getId()) {
            case R.id.detail_favorite:
                // waits until request finish to allow another request
                // TODO: will fail on error, mFavRequest isn't set back to null
                if (mFavRequest == null) {
                    if (checked) {
                        mFavRequest = Api.addToFavorites(itemId, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("DETAIL>", response.toString());
                                mFavRequest = null;
                            }
                        });
                    } else {
                        mFavRequest = Api.deleteFromFavorites(itemId, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("DETAIL>", response.toString());
                                mFavRequest = null;
                            }
                        });
                    }
                } else {
                    if (checked) checkBox.setChecked(false);
                    else checkBox.setChecked(true);
                }
                break;
        }
    }
}
