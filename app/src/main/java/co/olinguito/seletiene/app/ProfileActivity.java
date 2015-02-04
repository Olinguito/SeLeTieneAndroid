package co.olinguito.seletiene.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import co.olinguito.seletiene.app.util.UserManager;


public class ProfileActivity extends ActionBarActivity {

    protected UserManager userManager;
    private static String NULL_FIELD = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        TextView nameView = (TextView) findViewById(R.id.profile_name);
        TextView emailView = (TextView) findViewById(R.id.profile_email);
        TextView phoneView = (TextView) findViewById(R.id.profile_phone);
        userManager = new UserManager(this);
        nameView.setText(userManager.getUser().getName());
        String email = userManager.getUser().getEmail();
        String phone = userManager.getUser().getPhone();
        String mobile = userManager.getUser().getMobile();
        emailView.setText(email);
        if (!(phone == NULL_FIELD || phone.isEmpty()))
            phoneView.setText(phone);
        if (!(mobile == NULL_FIELD || mobile.isEmpty()))
            phoneView.setText(mobile);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.profile, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        return id == R.id.action_edit_profile || super.onOptionsItemSelected(item);
//    }

    public void logout(View view) {
        userManager.logout();
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
}
