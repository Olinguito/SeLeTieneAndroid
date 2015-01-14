package co.olinguito.seletiene.app.util;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseRequest<T> extends JsonRequest<T> {

    public BaseRequest(int method, String url, String requestBody, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        if (Api.hasCredentials())
            headers.put("Authorization", "Bearer " + Api.authToken);
        headers.put("Accept", "application/json");
        return headers;
    }
}
