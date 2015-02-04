package co.olinguito.seletiene.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import co.olinguito.seletiene.app.util.ChildActivity;


public class InfoActivity extends ChildActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }

    public void openTos(View view) {
        startActivity(new Intent(this, TosActivity.class));
    }
}
