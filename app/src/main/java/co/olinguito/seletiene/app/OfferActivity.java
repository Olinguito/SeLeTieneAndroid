package co.olinguito.seletiene.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import co.olinguito.seletiene.app.util.Api;
import com.android.volley.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static co.olinguito.seletiene.app.util.Api.TYPE_PRODUCT;
import static co.olinguito.seletiene.app.util.Api.TYPE_SERVICE;


public class OfferActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final int IMG_HEIGHT = 720;
    public static final int IMG_WIDTH = 1280;
    public static final String TMP_IMAGE = "pp.jpg";

    private ViewPager mViewPager;
    private int REQUEST_IMAGE_CAPTURE = 1;
    // product fields
    private EditText mProductTitle;
    private ImageView mPhotoView;
    private EditText mProductDesc;
    // service fields
    private EditText mServiceTitle;
    private EditText mServiceComment;
    private EditText mServiceTraining;
    private File mPhotoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

        mPhotoFile = new File(getExternalCacheDir(), TMP_IMAGE);

        // product fields
        mProductTitle = (EditText) findViewById(R.id.new_product_title);
        mProductDesc = (EditText) findViewById(R.id.new_product_description);
        // service fields
        mServiceTitle = (EditText) findViewById(R.id.new_service_title);
        mServiceComment = (EditText) findViewById(R.id.new_service_comments);
        mServiceTraining = (EditText) findViewById(R.id.new_service_training);

        mPhotoView = (ImageView) findViewById(R.id.new_product_photo);
        if (mPhotoFile.exists())
            mPhotoView.setImageBitmap(BitmapFactory.decodeFile(mPhotoFile.getAbsolutePath()));
        // use tabs in action bar - TODO: since its deprecated I should use something else
        final ActionBar actionbar = getSupportActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new SectionsPagerAdapter());

        actionbar.addTab(actionbar.newTab()
                .setText(R.string.product).setTabListener(this));
        actionbar.addTab(actionbar.newTab()
                .setText(R.string.service).setTabListener(this));
        actionbar.setSelectedNavigationItem(TYPE_PRODUCT);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionbar.setSelectedNavigationItem(position);
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // set photo height proportionally
        int height = (int) (mPhotoView.getWidth() * 0.565f); // 16:9 ratio
        mPhotoView.setLayoutParams(new RelativeLayout.LayoutParams(mPhotoView.getWidth(), height));
    }

    public void takePhoto(View view) {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (photoIntent.resolveActivity(getPackageManager()) != null) {
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
            startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // camera capture succeed
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            FileOutputStream outputStream = null;
            Bitmap scaledPhoto;
            try {
                // take original photo
                String path = mPhotoFile.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                // scale it down and save it to the same file
                scaledPhoto = scaleCropToFit(bitmap, IMG_WIDTH, IMG_HEIGHT);
                outputStream = new FileOutputStream(mPhotoFile);
                scaledPhoto.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                mPhotoView.setImageBitmap(scaledPhoto);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            mPhotoFile.delete();
        }
    }

    public void offerService(View view) {
        if (validService()) sendData(TYPE_SERVICE);
    }

    public boolean validService() {
        View focusView = null;
        if (TextUtils.isEmpty(mServiceTraining.getText())) {
            mServiceTraining.setError(getString(R.string.error_field_required));
            focusView = mServiceTraining;
        }
        if (TextUtils.isEmpty(mServiceComment.getText())) {
            mServiceComment.setError(getString(R.string.error_field_required));
            focusView = mServiceComment;
        }
        if (TextUtils.isEmpty(mServiceTitle.getText())) {
            mServiceTitle.setError(getString(R.string.error_field_required));
            focusView = mServiceTitle;
        }
        if (focusView != null) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    public void offerProduct(View view) {
        if (validProduct()) sendData(TYPE_PRODUCT);
    }

    public boolean validProduct() {
        View focusView = null;
        if (TextUtils.isEmpty(mProductDesc.getText())) {
            mProductDesc.setError(getString(R.string.error_field_required));
            focusView = mProductDesc;
        }
        if (TextUtils.isEmpty(mProductTitle.getText())) {
            mProductTitle.setError(getString(R.string.error_field_required));
            focusView = mProductTitle;
        }
        if (focusView != null) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public void sendData(final int type) {
        // set data to send
        JSONObject data = new JSONObject();
        int creatingMsg = 0;
        try {
            data.put("type", type);
            if (type == TYPE_PRODUCT) {
                data.put("title", mProductTitle.getText().toString().trim());
                data.put("description", mProductDesc.getText().toString().trim());
                creatingMsg = R.string.offer_creating_product;
            } else if (type == TYPE_SERVICE) {
                data.put("title", mServiceTitle.getText().toString().trim());
                data.put("description",
                        mServiceComment.getText().toString().trim()
                        + "\n" +
                        mServiceTraining.getText().toString().trim());
                creatingMsg = R.string.offer_created_service;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final ProgressDialog progress = ProgressDialog.show(this, "", getString(creatingMsg), true);
        // make API call
        Api.createProductOrService(data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                final int message = type == TYPE_PRODUCT ? R.string.offer_created_product : R.string.offer_created_service;
                // upload photo if type is product
                if (type == TYPE_PRODUCT && mPhotoFile.exists()) {
                    progress.setMessage(getString(R.string.offer_uploading_pic));
                    try {
                        Api.uploadProductOrServicePhoto(response.getInt("id"), mPhotoFile, new Response.Listener<Object>() {
                            @Override
                            public void onResponse(Object response) {
                                if (progress.isShowing()) progress.dismiss();
                                Toast.makeText(OfferActivity.this, message, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }, progress);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // finish activity and show success message
                    if (progress.isShowing()) progress.dismiss();
                    Toast.makeText(OfferActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }, progress);
    }

    // TODO: refactor out in its own file or utility library
    public static Bitmap scaleCropToFit(Bitmap original, int targetWidth, int targetHeight) {
        //Need to scale the image, keeping the aspect ration first
        int width = original.getWidth();
        int height = original.getHeight();

        float widthScale = (float) targetWidth / (float) width;
        float heightScale = (float) targetHeight / (float) height;
        float scaledWidth;
        float scaledHeight;

        int startY = 0;
        int startX = 0;

        if (widthScale > heightScale) {
            scaledWidth = targetWidth;
            scaledHeight = height * widthScale;
            //crop height by...
            startY = (int) ((scaledHeight - targetHeight) / 2);
        } else {
            scaledHeight = targetHeight;
            scaledWidth = width * heightScale;
            //crop width by..
            startX = (int) ((scaledWidth - targetWidth) / 2);
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(original, (int) scaledWidth, (int) scaledHeight, true);

        return Bitmap.createBitmap(scaledBitmap, startX, startY, targetWidth, targetHeight);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    public class SectionsPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return mViewPager.getChildAt(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }
    }
}
