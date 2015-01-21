package co.olinguito.seletiene.app.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import co.olinguito.seletiene.app.R;
import com.android.volley.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

import static com.android.volley.Request.Method.*;

public class Api {
    public static final String BASE_URL = "http://200.119.110.136:81/seletienea";
    public static final int TYPE_PRODUCT = 0;
    public static final int TYPE_SERVICE = 1;
    private static final String LOGIN_PARAM_EMAIL = "username";
    private static final String LOGIN_PARAM_PWD = "password";
    private static final int REQUEST_TIMEOUT = 2000;
    private static final int REQUEST_RETRY_COUNT = 2;
    private static final float REQUEST_BACKOFF_MULT = 1.4f;
    private static final Map<String, String> enpoints;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("items", "/api/ProductServices");
        map.put("token", "/token");
        map.put("me", "/api/Account");
        map.put("favorites", "/api/Account/Favorites");
        map.put("favoriteUpdate", "/api/ProductServices/Favorite?productServiceId=");
        map.put("prodPhoto", "/api/ProductServices/Image?ProductoServicioId=");
        enpoints = Collections.unmodifiableMap(map);
    }

    private static RequestSingleton sRequestSingleton = RequestSingleton.getInstance(App.getContext());
    public static RequestQueue requestQueue = sRequestSingleton.getRequestQueue();
    private static SharedPreferences tokenPreference;

    static {
        tokenPreference = App.getContext().getSharedPreferences("TOKEN", Context.MODE_PRIVATE);
    }

    public static String authToken;

    static {
        authToken = tokenPreference.getString("token", "");
    }

    public static void register(JSONObject data, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(POST, url("me"), data, listener, errorListener);
        request.setRetryPolicy(new DefaultRetryPolicy(5000, 1, 1));
        requestQueue.add(request);
    }

    public static void login(String email, String password, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) throws JSONException {
        final ArrayList<NameValuePair> data = new ArrayList<>();
        data.add(new BasicNameValuePair(LOGIN_PARAM_EMAIL, email));
        data.add(new BasicNameValuePair(LOGIN_PARAM_PWD, password));
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

    public static void getProductsAndServices(HashMap<String, String> paramsMap, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        // TODO: remove when users can be validated
        paramsMap.put("ignoreDPSValidation", "true");

        ArrayList<NameValuePair> parameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        String searchUrl = url("items") + "?" + URLEncodedUtils.format(parameters, "utf-8");

        JsonArrayRequest itemsRequest = new JsonArrayRequest(searchUrl, listener, errorListener);
        requestQueue.add(itemsRequest);
    }

    public static void getProductOrService(int id, Response.Listener listener) {
        String url = url("items") + "/" + id;
        requestQueue.add(new JsonObjectRequest(GET, url, null, listener, new DefaultApiErrorHandler(App.getContext())));
    }

    public static void createProductOrService(JSONObject data, Response.Listener<JSONObject> listener, ProgressDialog progress) {
        requestQueue.add(new JsonObjectRequest(POST, url("items"), data, listener, new DefaultApiErrorHandler(progress)));
    }

    public static void uploadProductOrServicePhoto(int id, File photo, Response.Listener<Object> listener, ProgressDialog progess) {
        String PHOTO_URL = url("prodPhoto") + id;
        requestQueue.add(new PhotoMultipartRequest<>(PHOTO_URL, photo, listener, new DefaultApiErrorHandler(progess)));
    }

    public static void getUserProfile(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        requestQueue.add(new JsonObjectRequest(GET, url("me"), null, listener, errorListener));
    }

    public static com.android.volley.Request getUserFavorites(Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        return requestQueue.add(new JsonArrayRequest(url("favorites"), listener, errorListener));
    }

    public static com.android.volley.Request addToFavorites(int id, Response.Listener<JSONObject> listener) {
        String url = url("favoriteUpdate") + id;
        return requestQueue.add(new JsonObjectRequest(PUT, url, new JSONObject(), listener, new DefaultApiErrorHandler(App.getContext())));
    }

    public static com.android.volley.Request deleteFromFavorites(int id, Response.Listener<JSONObject> listener) {
        String url = url("favoriteUpdate") + id;
        return requestQueue.add(new JsonObjectRequest(DELETE, url, new JSONObject(), listener, new DefaultApiErrorHandler(App.getContext())));
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
     */
    public static void handleResponseError(Context context, VolleyError error) {
        int errorString;
        if (error instanceof NetworkError)
            errorString = R.string.error_network;
        else if (error instanceof TimeoutError)
            errorString = R.string.error_server_timeout;
        else if (error instanceof AuthFailureError)
            errorString = R.string.error_auth;
        else if (error.networkResponse != null && error.networkResponse.statusCode == 400)
            errorString = R.string.error_client;
        else if (error instanceof ServerError)
            errorString = R.string.error_server;
        else
            errorString = R.string.error_generic;
        Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
    }
}
