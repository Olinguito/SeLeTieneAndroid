package co.olinguito.seletiene.app.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class DefaultApiErrorHandler implements Response.ErrorListener {

    private Context mContext;
    ProgressDialog mProgress;

    public DefaultApiErrorHandler(Context context) {
        this(context, null);
    }

    public DefaultApiErrorHandler(Context context, ProgressDialog dialog) {
        mContext = context;
        mProgress = dialog;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("ReqError", error.toString());
        Api.handleResponseError(mContext, error);
        if (mProgress != null)
            if (mProgress.isShowing()) mProgress.dismiss();
    }
}
