package co.olinguito.seletiene.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.ChildActivity;
import co.olinguito.seletiene.app.util.DefaultApiErrorHandler;
import co.olinguito.seletiene.app.util.UserManager;
import com.android.volley.VolleyError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public class RegisterActivity extends ChildActivity implements AdapterView.OnItemSelectedListener {

    private EditText mNameView;
    private EditText mEmailView;
    private EditText mPwdView;
    private EditText mConfirmPwdView;
    private EditText mIdView;
    private EditText mPhoneView;
    private Session.StatusCallback onFBLogin = new LoginCallback();
    private Button mSumbmitButton;
    private int MIN_LENGHT = 6;
    private Spinner mDepartment;
    private Spinner mCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mNameView = (EditText) findViewById(R.id.reg_field_name);
        mEmailView = (EditText) findViewById(R.id.reg_field_email);
        mIdView = (EditText) findViewById(R.id.reg_field_id);
        mPhoneView = (EditText) findViewById(R.id.reg_field_phone);
        mDepartment = (Spinner) findViewById(R.id.deparment);
        mDepartment.setFocusable(true);
        mDepartment.setFocusableInTouchMode(true);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.deparments, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDepartment.setAdapter(adapter);
        mDepartment.setOnItemSelectedListener(this);
        mCity = (Spinner) findViewById(R.id.city);
        mCity.setFocusable(true);
        mCity.setFocusableInTouchMode(true);
        mCity.setEnabled(false);
        mCity.setClickable(false);
        mPwdView = (EditText) findViewById(R.id.reg_field_pwd);
        mConfirmPwdView = (EditText) findViewById(R.id.reg_field_pwd_conf);
        mSumbmitButton = (Button) findViewById(R.id.reg_submitbutton);
        // on soft keyboard submit
        mConfirmPwdView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    mSumbmitButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Creates a new user, sends user data to the server after validation
     *
     * @throws JSONException It should never throw this(avoids try/catch)
     */
    public void createUser(View v) throws JSONException {
        if (!dataValid()) return;
        final ProgressDialog progress = ProgressDialog.show(this, "", getString(R.string.rl_progress), true);
        // Form data
        final JSONObject userData = new JSONObject();
        userData.put("name", mNameView.getText().toString().trim());
        userData.put("email", mEmailView.getText().toString().trim());
        userData.put("userId", mIdView.getText().toString().trim());
        userData.put("mobileNumber", mPhoneView.getText().toString().trim());
        userData.put("password", mPwdView.getText().toString().trim());

        Object city = mCity.getSelectedItem();
        if (city != null && !city.toString().isEmpty())
            userData.put("city", city);
        // Call endpoint to register user
        Api.register(userData, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progress.setMessage(getString(R.string.rl_loging));
                Toast.makeText(RegisterActivity.this, R.string.rl_user_created, Toast.LENGTH_LONG).show();
                // Login after successfully creating the user
                try {
                    Api.login((String) userData.get("email"), (String) userData.get("password"), new Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (progress.isShowing()) progress.dismiss();
                            try {
                                // save user profile in shared preferences
                                userManager.saveUser(new UserManager.User(
                                        response.getString("userId"),
                                        response.getString("email"),
                                        response.getString("name"),
                                        response.getString("phoneNumber"),
                                        response.getString("mobileNumber")
                                ));
                                Intent intent = new Intent(RegisterActivity.this, ItemListActivity.class);
                                intent.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } catch (JSONException ignored) {
                            }
                        }
                    }, new ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (progress.isShowing()) progress.dismiss();
                            Api.handleResponseError(RegisterActivity.this, error);
                        }
                    });
                } catch (JSONException ignored) {
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progress.isShowing()) progress.dismiss();
                Log.e("REG", error.toString());
                // when email already exist
                if (error.networkResponse != null && error.networkResponse.statusCode == 409)
                    Toast.makeText(RegisterActivity.this, R.string.error_user_exists, Toast.LENGTH_LONG).show();
                else
                    Api.handleResponseError(RegisterActivity.this, error);
            }
        });
    }

    /**
     * Form validation
     *
     * @return true if form valid
     */
    private boolean dataValid() {
        View focusView = null;
        // validate password match
        if (!mPwdView.getText().toString().equals(mConfirmPwdView.getText().toString())) {
            mConfirmPwdView.setError(getString(R.string.error_incorrect_password));
            focusView = mConfirmPwdView;
        }
        // validate password
        String pass = mPwdView.getText().toString();
        if (TextUtils.isEmpty(pass)) {
            mPwdView.setError(getString(R.string.error_field_required));
            focusView = mPwdView;
        } else if (pass.length() < MIN_LENGHT) {
            mPwdView.setError(getString(R.string.error_invalid_password));
            focusView = mPwdView;
        }

        // validate phone
        if (TextUtils.isEmpty(mPhoneView.getText())) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
        } else if (!Patterns.PHONE.matcher(mPhoneView.getText()).matches()) {
            mPhoneView.setError(getString(R.string.error_incorrect_phone));
            focusView = mPhoneView;
        }

        // validate id
        if (TextUtils.isEmpty(mIdView.getText())) {
            mIdView.setError(getString(R.string.error_field_required));
            focusView = mIdView;
        }

        // validate email
        if (TextUtils.isEmpty(mEmailView.getText())) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmailView.getText()).matches()) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
        }
        // validate name
        if (TextUtils.isEmpty(mNameView.getText())) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
        }
        if (focusView != null) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }

    /**
     * Try to do fb login on action button click and take user data
     *
     * @param item The action bar button
     */
    public void fillFormWithFacebook(MenuItem item) {
        Session session = new Session(this);
        Session.setActiveSession(session);
        session.openForRead(new Session.OpenRequest(this).setPermissions(Arrays.asList("email")).setCallback(onFBLogin));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (Session.getActiveSession() != null)
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0)
            Api.getCitiesByDepartmentId(position, new Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    ArrayAdapter<String> citiesAdapter;
                    ArrayList<String> cities = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject city = response.getJSONObject(i);
                            cities.add(city.getString("name"));
                        } catch (JSONException ignored) {
                        }
                    }
                    citiesAdapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_spinner_dropdown_item, cities);
                    mCity.setAdapter(citiesAdapter);
                    mCity.setEnabled(true);
                    mCity.setClickable(true);
                }
            }, new DefaultApiErrorHandler(this));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void openTos(View view) {
        startActivity(new Intent(this, TosActivity.class));
    }


    private class LoginCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened())
                Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            mNameView.setText(user.getName());
                            mEmailView.setText(user.getProperty("email").toString());
                        }
                    }
                }).executeAsync();
        }
    }
}
