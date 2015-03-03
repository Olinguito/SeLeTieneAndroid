package co.olinguito.seletiene.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.Button;
import co.olinguito.seletiene.app.util.Api;
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

public class StartActivity extends FragmentActivity {

    protected UserManager userManager;
    private Session.StatusCallback onFBLogin = new LoginCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Button registerBtn = (Button) findViewById(R.id.register_btn);
        Button loginBtn = (Button) findViewById(R.id.login_btn);
        float elevation = getResources().getDimension(R.dimen.button_elevation);
        ViewCompat.setElevation(registerBtn, elevation);
        ViewCompat.setElevation(loginBtn, elevation);
        // hidden views
        View btnInfo = findViewById(R.id.start_btn_info);
        View btnsContainer = findViewById(R.id.start_btns_container);
        View btnOffer = findViewById(R.id.start_btn_offer);
        // loading view
        View loadingView = findViewById(R.id.start_loading);
        // check if user exsist
        userManager = new UserManager(this);
        if (userManager.getUser() != null && Api.hasCredentials()) {
            Intent intent = new Intent(this, ItemListActivity.class);
            intent.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            loadingView.setVisibility(View.GONE);
            btnInfo.setVisibility(View.VISIBLE);
            btnsContainer.setVisibility(View.VISIBLE);
            btnOffer.setVisibility(View.VISIBLE);
        }
    }

    public void info(View view) {
        startActivity(new Intent(this, InfoActivity.class));
    }

    public void register(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void login(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    public void loginFB(View view) {
        Session session = new Session(this);
        Session.setActiveSession(session);
        session.openForRead(new Session.OpenRequest(this).setPermissions(Arrays.asList("email")).setCallback(onFBLogin));
    }

    private class LoginCallback implements Session.StatusCallback {
        @Override
        public void call(final Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                final ProgressDialog progress = ProgressDialog.show(StartActivity.this, "", getString(R.string.rl_fb_conecting), true);
                Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            progress.setMessage(getString(R.string.rl_loging));
                            JSONObject data = new JSONObject();
                            try {
                                data.put("token", session.getAccessToken());
                                data.put("name", user.getName());
                                data.put("email", user.getProperty("email").toString());
                            } catch (JSONException ignored) {
                            }
                            //
                            Api.loginFB(data, new Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if (progress.isShowing()) progress.dismiss();
                                    try {
                                        userManager.saveUser(new UserManager.User(
                                                response.getString("userId"),
                                                response.getString("email"),
                                                response.getString("name"),
                                                response.getString("phoneNumber"),
                                                response.getString("mobileNumber")
                                        ));
                                        Intent intent = new Intent(StartActivity.this, ItemListActivity.class);
                                        intent.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } catch (JSONException ignored) {
                                    }
                                }
                            }, new ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (progress.isShowing()) progress.dismiss();
                                    Api.handleResponseError(StartActivity.this, error);
                                }
                            });
                        }
                    }
                }).executeAsync();

            }
        }
    }
}
