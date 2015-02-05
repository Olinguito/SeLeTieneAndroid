package co.olinguito.seletiene.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
    private View mProviderDetails;
    private TextView mDetailText;
    private static String NULL_FIELD = "null";


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
        final View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        mProviderDetails = rootView.findViewById(R.id.provider_details);

        if (mItem != null) {
            try {
                float elevation = getResources().getDimension(R.dimen.button_elevation);
                int type = mItem.getInt("type");
                // provider info
                Api.getProductOrService(mItem.getInt("id"), new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        try {
                            JSONObject itemData = (JSONObject) response;
                            JSONObject owner = itemData.getJSONObject("owner");
                            String email = owner.getString("email");
                            String phone = owner.getString("phoneNumber");
                            String mobile = owner.getString("mobileNumber");
                            ((TextView) rootView.findViewById(R.id.profile_email)).setText(email);
                            if (!(phone.equals(NULL_FIELD) || phone.isEmpty()) || phone.equals(JSONObject.NULL))
                                ((TextView) rootView.findViewById(R.id.profile_phone)).setText(phone);
                            if (!(mobile.equals(NULL_FIELD) || mobile.isEmpty() || mobile.equals(JSONObject.NULL)))
                                ((TextView) rootView.findViewById(R.id.profile_mobile)).setText(mobile);

                        } catch (JSONException ignored) {
                        }

                    }
                });

                rootView.findViewById(R.id.detail_contact).setOnClickListener(this);
                ((TextView) rootView.findViewById(R.id.detail_title)).setText(mItem.getString("title"));
                ((TextView) rootView.findViewById(R.id.detail_owner)).setText(mItem.getString("ownerName"));
                mDetailText = (TextView) rootView.findViewById(R.id.detail_description);
                mDetailText.setText(mItem.getString("description"));
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
                if (mItem.getBoolean("favorite"))
                    fav.setChecked(true);
                fav.setOnClickListener(this);
                RatingBar rating = (RatingBar) rootView.findViewById(R.id.detail_rating);
                rating.setRating(mItem.getInt("rating"));
            } catch (JSONException ignored) {
            }
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int itemId = 0;
        try {
            itemId = mItem.getInt("id");
        } catch (JSONException ignored) {
        }
        switch (v.getId()) {
            case R.id.detail_favorite:
                CheckBox checkBox = (CheckBox) v;
                boolean checked = checkBox.isChecked();
                // waits until request finish to allow another request
                // TODO: will fail on error, mFavRequest isn't set back to null
                if (mFavRequest == null) {
                    if (checked) {
                        mFavRequest = Api.addToFavorites(itemId, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                mFavRequest = null;
                                Toast.makeText(getActivity(), R.string.detail_message_fav_added, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        mFavRequest = Api.deleteFromFavorites(itemId, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                mFavRequest = null;
                                Toast.makeText(getActivity(), R.string.detail_message_fav_deleted, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    if (checked) checkBox.setChecked(false);
                    else checkBox.setChecked(true);
                }
                break;
            case R.id.detail_contact:
                Button button = (Button) v;
                mProviderDetails.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
                mDetailText.setVisibility(View.GONE);
                break;
        }
    }
}
