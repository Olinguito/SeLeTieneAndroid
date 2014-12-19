package co.olinguito.seletiene.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


public class OfferActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final int PRODUCT = 0;
    public static final int SERVICE = 1;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private int REQUEST_IMAGE_CAPTURE = 1;
    private Button mTakePhotoBtn;
    // product fields
    private EditText mProductType;
    private ImageView mPhotoView;
    private EditText mProductDesc;
    // service fields
    private EditText mServiceType;
    private EditText mServiceComment;
    private EditText mServiceTraining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);
        // use tabs in action bar - TODO: since its deprecated I should use something else
        final ActionBar actionbar = getSupportActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new SectionsPagerAdapter());

        actionbar.addTab(actionbar.newTab()
                .setText(R.string.product).setTabListener(this));
        actionbar.addTab(actionbar.newTab()
                .setText(R.string.service).setTabListener(this));
        actionbar.setSelectedNavigationItem(PRODUCT);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionbar.setSelectedNavigationItem(position);
            }
        });

        mPhotoView = (ImageView) findViewById(R.id.new_product_photo);
        mTakePhotoBtn = (Button) findViewById(R.id.take_photo);
        // product fields
        mProductType = (EditText) findViewById(R.id.new_product_type);
        mProductDesc = (EditText) findViewById(R.id.new_product_description);
        // service fields
        mServiceType = (EditText) findViewById(R.id.new_service_type);
        mServiceComment = (EditText) findViewById(R.id.new_service_comments);
        mServiceTraining = (EditText) findViewById(R.id.new_service_training);
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
            startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            mPhotoView.setImageBitmap(image);
            mTakePhotoBtn.setVisibility(View.GONE);
        }
    }

    public void offerService(View view) {
        if (validService()) {
            Toast.makeText(this, R.string.offer_created_service, Toast.LENGTH_SHORT).show();
            finish();
        }
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
        if (TextUtils.isEmpty(mServiceType.getText())) {
            mServiceType.setError(getString(R.string.error_field_required));
            focusView = mServiceType;
        }
        if (focusView != null) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    public void offerProduct(View view) {
        if (validProduct()) {
            Toast.makeText(this, R.string.offer_created_product, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public boolean validProduct() {
        View focusView = null;
        if (TextUtils.isEmpty(mProductDesc.getText())) {
            mProductDesc.setError(getString(R.string.error_field_required));
            focusView = mProductDesc;
        }
        if (TextUtils.isEmpty(mProductType.getText())) {
            mProductType.setError(getString(R.string.error_field_required));
            focusView = mProductType;
        }
        if (focusView != null) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
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
            return (View) mViewPager.getChildAt(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == ((View) o);
        }
    }
}
