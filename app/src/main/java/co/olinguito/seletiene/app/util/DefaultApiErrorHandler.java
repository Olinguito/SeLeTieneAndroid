package co.olinguito.seletiene.app.util;

import android.content.Context;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class DefaultApiErrorHandler implements Response.ErrorListener {

    private Context mContext;

    public DefaultApiErrorHandler(Context context) {
        mContext = context;
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        Api.handleResponseError(mContext, error);
    }
}
