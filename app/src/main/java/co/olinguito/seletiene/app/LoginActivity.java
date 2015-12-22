package co.olinguito.seletiene.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.ChildActivity;
import co.olinguito.seletiene.app.util.DefaultApiErrorHandler;
import co.olinguito.seletiene.app.util.UserManager;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A login screen that offers login via email/password. (based on login template)
 */
public class LoginActivity extends ChildActivity {

    private EditText mEmailView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // TODO: return/dont login if login in process

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // focus on first field with errors
            focusView.requestFocus();
        } else {
            // Call Api to login (get access_token and profile)
            final ProgressDialog progress = ProgressDialog.show(this, "", getString(R.string.rl_loging), true);
            try {
                Api.login(mEmailView.getText().toString(), mPasswordView.getText().toString(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (progress.isShowing()) progress.dismiss();
                        // save user profile data in shared preferences
                        try {
                            userManager.saveUser(new UserManager.User(
                                    response.getString("UserId"),
                                    response.getString("Email"),
                                    response.getString("Name"),
                                    response.getString("PhoneNumber"),
                                    response.getString("MobileNumber")
                            ));
                            Intent intent = new Intent(LoginActivity.this, ItemListActivity.class);
                            intent.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR>", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (progress.isShowing()) progress.dismiss();
                        Log.e("LOGIN", error.toString());
                        Api.handleResponseError(LoginActivity.this, error);
                    }
                });
            } catch (JSONException e) {
                Log.e("JSON_ERROR>", e.getMessage());
            }
        }
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void resetPassword(View view) {
        if (!TextUtils.isEmpty(mEmailView.getText()))
            Api.resetPassword(mEmailView.getText().toString(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(LoginActivity.this, R.string.rl_pass_reset_message, Toast.LENGTH_LONG).show();
                }
            }, new DefaultApiErrorHandler(this));
        else
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_LONG).show();
    }
}



