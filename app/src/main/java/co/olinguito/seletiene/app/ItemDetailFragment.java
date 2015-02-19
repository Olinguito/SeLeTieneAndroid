package co.olinguito.seletiene.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.RequestSingleton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private RatingBar mRating;
    private String mTitle;


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

                rootView.findViewById(R.id.detail_contact).setOnClickListener(this);
                mTitle = mItem.getString("title");
                mTitle = mTitle.substring(0, 1).toUpperCase() + mTitle.substring(1);
                ((TextView) rootView.findViewById(R.id.detail_title)).setText(mTitle);
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
                mRating = (RatingBar) rootView.findViewById(R.id.detail_rating);
                mRating.setRating((float) mItem.getDouble("rating"));
                // contact info
                final TextView phoneView = (TextView) rootView.findViewById(R.id.profile_phone_edit);
                final TextView mobileView = (TextView) rootView.findViewById(R.id.profile_mobile_edit);
                final TextView emailView = (TextView) rootView.findViewById(R.id.profile_email);
                phoneView.setEnabled(true);
                mobileView.setEnabled(true);
                phoneView.setOnClickListener(this);
                mobileView.setOnClickListener(this);
                emailView.setOnClickListener(this);

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
                            emailView.setText(email);
                            if (!(phone.equals(NULL_FIELD) || phone.isEmpty()) || phone.equals(JSONObject.NULL))
                                phoneView.setText(phone);
                            if (!(mobile.equals(NULL_FIELD) || mobile.isEmpty() || mobile.equals(JSONObject.NULL)))
                                mobileView.setText(mobile);

                        } catch (JSONException ignored) {
                        }

                    }
                });

                // share
                rootView.findViewById(R.id.detail_share).setOnClickListener(this);
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
                // alert to remind to rate user
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.detail_alert_rate_title))
                        .setMessage(getString(R.string.detail_alert_rate))
                        .setPositiveButton(R.string.detail_alert_rate_ok, null)
                        .show();
                // enable rating bar
                mRating.setIsIndicator(false);
                // on rate change
                mRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        mRating.setIsIndicator(true);
                        int roundedRating = (int) Math.ceil(rating);
                        try {
                            Api.ratePS(mItem.getInt("id"), roundedRating, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Toast.makeText(getActivity(), R.string.detail_message_rated, Toast.LENGTH_LONG).show();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    mRating.setIsIndicator(false);
                                    Api.handleResponseError(getActivity(), error);
                                }
                            });
                        } catch (JSONException ignored) {
                        }
                    }
                });
                break;
            case R.id.detail_share:
                List<String> packageNames = new ArrayList<>();
                packageNames.add("com.facebook.katana");
                packageNames.add("com.twitter.android");
                List<Intent> intentList = new ArrayList<>();
                String textToShare = mTitle + ": \n" + mDetailText.getText().toString();

                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(shareIntent, 0);
                if (!resInfo.isEmpty()){
                    for (ResolveInfo info : resInfo) {
                        Intent targetedShare = new Intent(android.content.Intent.ACTION_SEND);
                        targetedShare.setType("text/plain"); // put here your mime type
                        if (packageNames.contains(info.activityInfo.packageName.toLowerCase())) {
                            targetedShare.putExtra(Intent.EXTRA_TEXT, textToShare);
                            targetedShare.setPackage(info.activityInfo.packageName.toLowerCase());
                            intentList.add(targetedShare);
                        }
                    }
                    if (!intentList.isEmpty()) {
                        Intent chooserIntent = Intent.createChooser(intentList.remove(0), getResources().getString(R.string.detail_share));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
                        startActivity(chooserIntent);
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.detail_share)
                                .setMessage(R.string.detail_share_error)
                                .setPositiveButton(android.R.string.yes, null)
                                .show();
                    }
                }
                break;
            case R.id.profile_email:
                TextView emailView = (TextView) v;
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", emailView.getText().toString().trim(), null));
                startActivity(intent);
                break;
            case R.id.profile_phone_edit:
            case R.id.profile_mobile_edit:
                String number = ((TextView) v).getText().toString().trim();
                if (!TextUtils.isEmpty(number)) {
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                    phoneIntent.setData(Uri.parse("tel:" + number));
                    startActivity(phoneIntent);
                }
                break;
        }
    }
}
