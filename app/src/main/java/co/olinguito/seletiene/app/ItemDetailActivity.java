package co.olinguito.seletiene.app;

import android.os.Bundle;
import co.olinguito.seletiene.app.util.ChildActivity;


public class ItemDetailActivity extends ChildActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.JSON_OBJECT_STRING,
                    getIntent().getStringExtra(ItemDetailFragment.JSON_OBJECT_STRING));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }
}
