package co.olinguito.seletiene.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import co.olinguito.seletiene.app.util.UserManager;


public class ProfileActivity extends ActionBarActivity {

    protected UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        TextView nameView = (TextView) findViewById(R.id.profile_name);
        TextView emailView = (TextView) findViewById(R.id.profile_email);
        TextView phoneView = (TextView) findViewById(R.id.profile_phone);
        userManager = new UserManager(this);
        nameView.setText(userManager.getUser().getName());
        emailView.setText(userManager.getUser().getEmail());
        phoneView.setText(userManager.getUser().getPhone());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_edit_profile || super.onOptionsItemSelected(item);
    }

    public void logout(View view) {
        userManager.logout();
    }
}
