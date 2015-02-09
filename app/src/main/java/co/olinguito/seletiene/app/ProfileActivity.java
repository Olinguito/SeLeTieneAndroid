package co.olinguito.seletiene.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.UserManager;
import com.android.volley.Response;
import org.json.JSONObject;


public class ProfileActivity extends ActionBarActivity implements View.OnLongClickListener {

    protected UserManager mUserManager;
    private static String NULL_FIELD = "null";
    private static String INPUT_HINT = "---";
    private Button logoutBtn;
    private EditText phoneEditView;
    private EditText mobileEditView;
    private EditText mEditView;
    private Button editBtn;
    private UserManager.User mUser;
    private InputMethodManager mIm;
    private String mPrevValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        TextView nameView = (TextView) findViewById(R.id.profile_name);
        TextView emailView = (TextView) findViewById(R.id.profile_email);
        // edit fields
        phoneEditView = (EditText) findViewById(R.id.profile_phone_edit);
        phoneEditView.setEnabled(true);
        phoneEditView.setOnLongClickListener(this);
        mobileEditView = (EditText) findViewById(R.id.profile_mobile_edit);
        mobileEditView.setEnabled(true);
        mobileEditView.setOnLongClickListener(this);
        //
        logoutBtn = (Button) findViewById(R.id.profile_logout_btn);
        editBtn = (Button) findViewById(R.id.profile_edit_btn);
        //
        mUserManager = new UserManager(this);
        mUser = mUserManager.getUser();
        nameView.setText(mUser.getName());
        String email = mUser.getEmail();
        String phone = mUser.getPhone();
        String mobile = mUser.getMobile();
        emailView.setText(email);
        if (!(phone.equals(NULL_FIELD) || phone.isEmpty() || phone.equals(JSONObject.NULL)))
            phoneEditView.setText(phone);
        if (!(mobile.equals(NULL_FIELD) || mobile.isEmpty() || mobile.equals(JSONObject.NULL)))
            mobileEditView.setText(mobile);
        //
        mIm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    public void logout(View view) {
        mUserManager.logout();
    }

    public void openFavorites(View view) {
        Intent intent = new Intent(getBaseContext(), RecentFavActivity.class);
        intent.putExtra(RecentFavActivity.MODE_TAG, RecentFavActivity.FAV_MODE);
        startActivity(intent);
    }

    public void openRecents(View view) {
        Intent intent = new Intent(getBaseContext(), RecentFavActivity.class);
        intent.putExtra(RecentFavActivity.MODE_TAG, RecentFavActivity.RECENT_MODE);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        // disable previous edit
        disableEditField(mEditView);
        // enable input
        v.setFocusableInTouchMode(true);
        // open keyboard
        v.requestFocus();
        mIm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);

        // set editView to currently selected view
        int id = v.getId();
        if (id == R.id.profile_mobile_edit)
            mEditView = (EditText) v;
        else if (id == R.id.profile_phone_edit)
            mEditView = (EditText) v;
        // clear dashes
        mEditView.setHint("");
        // save value before edit to compare later
        mPrevValue = mEditView.getText().toString().trim();
        // switch logout btn with edit btn
        editBtn.setVisibility(View.VISIBLE);
        logoutBtn.setVisibility(View.GONE);
        return true;
    }

    public void editFields(View view) {
        disableEditField(mEditView);
        // hide keyboard
        mIm.hideSoftInputFromWindow(mEditView.getWindowToken(), 0);
        // hide button
        editBtn.setVisibility(View.GONE);
        logoutBtn.setVisibility(View.VISIBLE);
        // send data to server
        final boolean editingMobile = mEditView.getId() == R.id.profile_mobile_edit;
        final String fieldValue = mEditView.getText().toString().trim();
        if (mPrevValue.equals(fieldValue)) return;
        String field = editingMobile ? "mobileNumber" : "phoneNumber";
        Api.editUserField(field, fieldValue, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(ProfileActivity.this, R.string.profile_edition_successful, Toast.LENGTH_LONG).show();
                if (editingMobile)
                    mUser.setMobile(fieldValue);
                else
                    mUser.setPhone(fieldValue);
                mUserManager.saveUser(mUser);
            }
        });
    }

    private void disableEditField(EditText editView) {
        if (editView != null) {
            editView.setFocusableInTouchMode(false);
            editView.clearFocus();
            //
            editView.setHint(INPUT_HINT);
        }
    }
}
