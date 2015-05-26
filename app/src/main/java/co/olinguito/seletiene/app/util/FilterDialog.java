package co.olinguito.seletiene.app.util;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import co.olinguito.seletiene.app.R;

import java.util.HashMap;

import static co.olinguito.seletiene.app.util.Api.TYPE_PRODUCT;
import static co.olinguito.seletiene.app.util.Api.TYPE_SERVICE;

public class FilterDialog extends DialogFragment implements View.OnClickListener {

    private RadioButton mAllCheck;
    private RadioButton mProductCheck;
    private RadioButton mServiceCheck;
    private RatingBar mRatingBar;
    private EditText mQuery;
    private Spinner mDepartment;

    public interface FilterListener {
        public void onFilterChange(HashMap<String, String> searchParams);
    }

    HashMap<String, String> mParams;
    FilterListener mFilterListener;

    public static FilterDialog newInstance(HashMap<String, String> params) {
        FilterDialog dialog = new FilterDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable("params", params);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        Bundle arguments = getArguments();
        if (arguments != null)
            mParams = (HashMap<String, String>) arguments.getSerializable("params");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.filter_view, container, false);
//        mDepartment = (Spinner) view.findViewById(R.id.deparment);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.deparments, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mDepartment.setAdapter(adapter);
        mAllCheck = (RadioButton) view.findViewById(R.id.filter_all);
        mServiceCheck = (RadioButton) view.findViewById(R.id.filter_service);
        mProductCheck = (RadioButton) view.findViewById(R.id.filter_product);
        mRatingBar = (RatingBar) view.findViewById(R.id.filter_rating);
        mQuery = (EditText) view.findViewById(R.id.filter_query);
        Button button = (Button) view.findViewById(R.id.filter_apply_btn);
        button.setOnClickListener(this);
        //
        setValues();
        return view;
    }

    private void setValues() {
        int type;
        if (mParams.containsKey("type"))
            type = Integer.parseInt(mParams.get("type"));
        else
            type = -1;

        if (type == TYPE_PRODUCT)
            mProductCheck.setChecked(true);
        else if (type == Api.TYPE_SERVICE)
            mServiceCheck.setChecked(true);
        else
            mAllCheck.setChecked(true);
        mRatingBar.setRating(Float.parseFloat(mParams.get("minStars")));
        mQuery.setText(mParams.get("q"));
        //

//        int department;
//        if (mParams.containsKey("departmentId"))
//            department = Integer.parseInt(mParams.get("departmentId"));
//        else department = 0;
//        mDepartment.setSelection(department);
    }

    public void setFilterChangeListener(FilterListener listener) {
        mFilterListener = listener;
    }

    @Override
    public void onClick(View v) {
        mParams.put("minStars", String.valueOf((int) Math.ceil(mRatingBar.getRating())));
        if (mProductCheck.isChecked())
            mParams.put("type", String.valueOf(TYPE_PRODUCT));
        else if (mServiceCheck.isChecked())
            mParams.put("type", String.valueOf(TYPE_SERVICE));
        else
            mParams.remove("type");
        mParams.put("q", mQuery.getText().toString());
        //
        //int departmentId = mDepartment.getSelectedItemPosition();
        //if (departmentId == 0)
        //    mParams.remove("departmentId");
        //else
        //    mParams.put("departmentId", String.valueOf(departmentId));

        if (mFilterListener != null)
            mFilterListener.onFilterChange(mParams);
        dismiss();
    }

    private int findDeparmentId(String department) {
        Log.d("FILTER>>>", String.valueOf(department));
        SpinnerAdapter adapter = mDepartment.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String selection = (String) adapter.getItem(i);
            Log.d("FILTER>", selection);
            if (department.equals(selection)) return i;
        }
        return -1;
    }
}