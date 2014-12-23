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

    public static final int PRODUCT = 0;
    public static final String JSON_OBJECT_STRING = "json_string";
    private static final String TMP_IMAGE_URL = "http://lorempixel.com/400/300/food";
    private JSONObject mItem;
    private Request mFavRequest;

    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(JSON_OBJECT_STRING)) {
            try {
                mItem = new JSONObject(getArguments().getString(JSON_OBJECT_STRING));
                Log.d("DETAIL>>>", mItem.toString());
                int titleRes;
                if (mItem.getInt("type") == PRODUCT) titleRes = R.string.product;
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
                ((TextView) rootView.findViewById(R.id.detail_title)).setText(mItem.getString("title"));
                ((TextView) rootView.findViewById(R.id.detail_owner)).setText(mItem.getString("ownerName"));
                ((TextView) rootView.findViewById(R.id.detail_description)).setText(mItem.getString("description"));
                float elevation = getResources().getDimension(R.dimen.button_elevation);
                ViewCompat.setElevation(rootView.findViewById(R.id.detail_image_container), elevation);
                NetworkImageView image = (NetworkImageView) rootView.findViewById(R.id.detail_picture);
                image.setImageUrl(TMP_IMAGE_URL, RequestSingleton.getInstance(getActivity()).getImageLoader());
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
                if (mFavRequest == null){
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
