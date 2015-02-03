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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.ChildActivity;
import co.olinguito.seletiene.app.util.UserManager;
import com.android.volley.VolleyError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.Listener;

public class RegisterActivity extends ChildActivity {

    private EditText mNameView;
    private EditText mEmailView;
    private EditText mPwdView;
    private EditText mConfirmPwdView;
    private Session.StatusCallback onFBLogin = new LoginCallback();
    private Button mSumbmitButton;
    private int MIN_LENGHT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mNameView = (EditText) findViewById(R.id.reg_field_name);
        mEmailView = (EditText) findViewById(R.id.reg_field_email);
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
        userData.put("name", mNameView.getText().toString());
        userData.put("email", mEmailView.getText().toString());
        userData.put("password", mPwdView.getText().toString());
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
                                        response.get("email").toString(),
                                        response.get("name").toString(),
                                        response.get("phoneNumber").toString(),
                                        response.get("mobileNumber").toString()
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
//                Log.e("REG", String.valueOf(error.networkResponse.statusCode));
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
