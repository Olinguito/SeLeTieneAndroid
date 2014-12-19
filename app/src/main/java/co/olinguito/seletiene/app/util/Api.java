package co.olinguito.seletiene.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import co.olinguito.seletiene.app.R;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.okhttp.OkUrlFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;

public class Api {
    private static RequestSingleton sRequestSingleton = RequestSingleton.getInstance(App.getContext());
    public static RequestQueue requestQueue = sRequestSingleton.getRequestQueue();
    public static final String BASE_URL = "http://seletiene.cloudapp.net";
    private static final String LOGIN_PARAM_EMAIL = "username";
    private static final String LOGIN_PARAM_PWD = "password";

    private static final int REQUEST_TIMEOUT = 6000;
    private static final int REQUEST_RETRY_COUNT = 2;
    private static final float REQUEST_BACKOFF_MULT = 1.5f;

    private static SharedPreferences tokenPreference;

    static {
        tokenPreference = App.getContext().getSharedPreferences("TOKEN", Context.MODE_PRIVATE);
    }

    private static String authToken;

    static {
        authToken = tokenPreference.getString("token", "");
    }

    private static final Map<String, String> enpoints;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("items", "/api/ProductServices?ignoreDPSValidation=true");
        map.put("token", "/token");
        map.put("me", "/api/Account");
//        map.put("favorites", "/usuario/me/favorito");
        enpoints = Collections.unmodifiableMap(map);
    }

    public static void register(JSONObject data, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(POST, url("me"), data, listener, errorListener) {
            // ignore response body because it's empty sometimes
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1));
        requestQueue.add(request);
    }

    public static void login(String email, String password, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) throws JSONException {
        final ArrayList<NameValuePair> data = new ArrayList<>();
        data.add(new BasicNameValuePair(LOGIN_PARAM_EMAIL, (String) email));
        data.add(new BasicNameValuePair(LOGIN_PARAM_PWD, (String) password));
        // needed for oauth2
        data.add(new BasicNameValuePair("grant_type", "password"));
        // Get authorization token then get the user profile
        JsonObjectRequest request = new JsonObjectRequest(POST, url("token"), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    setCredentials((String) response.get("access_token"));
                } catch (JSONException error) {
                    clearCredentials();
                } finally {
                    Api.getUserProfile(listener, errorListener);
                }
            }
        }, errorListener) {
            // TODO: remove when backend accepts JSON body
            @Override
            public byte[] getBody() {
                return URLEncodedUtils.format(data, getParamsEncoding()).getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT, REQUEST_RETRY_COUNT, REQUEST_BACKOFF_MULT));
        requestQueue.add(request);
    }

    public static void getProductsAndServices(HashMap params, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String uri = String.format("?type=%1$s&city=%2$s&rating=%3$s",
                params.get("type"),
                params.get("city"),
                params.get("rating")
        );
        ArrayRequest itemsRequest = new ArrayRequest(url("items"), listener, errorListener);

        requestQueue.add(itemsRequest);
    }

    public static void getUserProfile(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        requestQueue.add(new Request(GET, url("me"), null, listener, errorListener));
    }

    public static com.android.volley.Request getUserFavorites(Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        return requestQueue.add(new ArrayRequest(url("favorites"), listener, errorListener));
    }

    private static String url(String endpoint) {
        return BASE_URL + enpoints.get(endpoint);
    }

    public static boolean hasCredentials() {
        return !authToken.isEmpty();
    }

    public static void setCredentials(String token) {
        // save token in shared preferences
        tokenPreference.edit().putString("token", token).apply();
        authToken = token;
    }

    public static void clearCredentials() {
        tokenPreference.edit().clear().apply();
        authToken = "";
    }

    /**
     * Generic way to handle network errors
     *
     * @param error
     */
    public static void handleResponseError(Context context, VolleyError error) {
        int errorString;
        if (error instanceof NetworkError)
            errorString = R.string.error_network;
        else if (error instanceof TimeoutError)
            errorString = R.string.error_server_timeout;
        else if (error instanceof AuthFailureError)
            errorString = R.string.error_auth;
        else if (error.networkResponse.statusCode == 400)
            errorString = R.string.error_client;
        else if (error instanceof ServerError)
            errorString = R.string.error_server;
        else
            errorString = R.string.error_generic;
        Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
    }

    // custom json requests with auth headers

    static class Request extends JsonObjectRequest {

        public Request(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
            setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT, REQUEST_RETRY_COUNT, REQUEST_BACKOFF_MULT));
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>();
            if (hasCredentials())
                headers.put("Authorization", "Bearer " + authToken);
            return headers;
        }
    }

    static class ArrayRequest extends JsonArrayRequest {
        public ArrayRequest(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            super(url, listener, errorListener);
            setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT, REQUEST_RETRY_COUNT, REQUEST_BACKOFF_MULT));
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>();
            if (hasCredentials())
                headers.put("Authorization", "Bearer " + authToken);
            return headers;
        }
    }
}
