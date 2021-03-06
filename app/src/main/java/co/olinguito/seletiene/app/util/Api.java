package co.olinguito.seletiene.app.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import co.olinguito.seletiene.app.R;
import com.android.volley.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.*;

public class Api {
    //    public static final String BASE_URL = "http://seletiene.cloudapp.net";
    public static final String BASE_URL = "http://201.245.123.114:8089/seletiene";
    public static final int TYPE_PRODUCT = 0;
    public static final int TYPE_SERVICE = 1;
    private static final int REQUEST_TIMEOUT = 2000;
    private static final int REQUEST_RETRY_COUNT = 2;
    private static final float REQUEST_BACKOFF_MULT = 1.4f;
    private static final Map<String, String> enpoints;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("items", "/api/ProductServices");
        map.put("token", "/token");
        map.put("me", "/api/Account");
        map.put("fb", "/api/Account/FacebookLogin");
        map.put("favorites", "/api/Account/Favorites");
        map.put("favoriteUpdate", "/api/ProductServices/Favorite?productServiceId=");
        map.put("prodPhoto", "/api/ProductServices/Image?ProductoServicioId=");
        map.put("citiesByDepartment", "/api/Departments/");
        map.put("passReset", "/api/Account/RecoverPasswordEmail?email=");
        map.put("rate", "/api/ProductServices/Rate?");
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

    private static final int REGISTER_TIMEOUT = 22000; // it should be lower

    public static void register(JSONObject data, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(POST, url("me"), data, listener, errorListener);
        request.setRetryPolicy(new DefaultRetryPolicy(REGISTER_TIMEOUT, 1, 1));
        requestQueue.add(request);
    }

    public static void login(final String email, final String password, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) throws JSONException {
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
            @Override
            public byte[] getBody() {
                String authData = null;
                try {
                    authData = "username=" + URLEncoder.encode(email, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8") +
                            "&grant_type=password";
                } catch (UnsupportedEncodingException ignored) {
                }
                return authData != null ? authData.getBytes() : new byte[0];
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT, REQUEST_RETRY_COUNT, REQUEST_BACKOFF_MULT));
        requestQueue.add(request);
    }

    public static void loginFB(JSONObject data, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        Log.d("FB>>", data.toString());
        requestQueue.add(new JsonObjectRequest(POST, url("fb"), data, new Response.Listener<JSONObject>() {
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
        }, errorListener));
    }

    public static void getProductsAndServices(HashMap<String, String> paramsMap, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String searchUrl = url("items") + "?";
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            searchUrl += URLEncoder.encode(entry.getKey()) + "=" +  URLEncoder.encode(entry.getValue()) + "&";
        }
        searchUrl = searchUrl.substring(0, searchUrl.length() - 1);

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

    public static com.android.volley.Request getCitiesByDepartmentId(int id, final Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = url("citiesByDepartment") + id;
        return requestQueue.add(new JsonObjectRequest(GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    listener.onResponse(response.getJSONArray("Cities"));
                } catch (JSONException e) {
                    Log.e("JSON_ERROR>", e.getMessage());
                }
            }
        }, errorListener));
    }

    public static void ratePS(int id, int rating, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = url("rate") + "productServiceId=" + id + "&newRating=" + rating;
        requestQueue.add(new JsonObjectRequest(PUT, url, new JSONObject(), listener, errorListener));
    }

    public static void resetPassword(String email, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = url("passReset") + email;
        requestQueue.add(new JsonObjectRequest(POST, url, new JSONObject(), listener, errorListener));
    }

    public static void editUserField(String fieldName, String value, Response.Listener<JSONObject> listener) {
        String url = url("me");
        JSONObject data = new JSONObject();
        try {
            data.put(fieldName, value);
        } catch (JSONException ignored) {
        }
        requestQueue.add(new JsonObjectRequest(PUT, url, data, listener, new DefaultApiErrorHandler(App.getContext())));
    }

    //

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
