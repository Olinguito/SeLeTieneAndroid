package co.olinguito.seletiene.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.Button;
import co.olinguito.seletiene.app.util.Api;
import co.olinguito.seletiene.app.util.UserManager;

public class StartActivity extends FragmentActivity {

    protected UserManager userManager;

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

    public void offer(View view) {
        startActivity(new Intent(this, OfferActivity.class));
    }
}
