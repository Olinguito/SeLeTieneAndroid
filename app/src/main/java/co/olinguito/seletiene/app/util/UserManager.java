package co.olinguito.seletiene.app.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.IntentCompat;
import co.olinguito.seletiene.app.StartActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class UserManager {
    private static final String PREF_NAME = "USER";

    Context mContext;
    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;

    public UserManager(Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public User getUser() {
        boolean login = mPreferences.getBoolean("login", false);
        if (login)
            return new User(
                    mPreferences.getString("id", ""),
                    mPreferences.getString("email", ""),
                    mPreferences.getString("name", ""),
                    mPreferences.getString("phone", ""),
                    mPreferences.getString("mobile", "")
            );
        else return null;
    }

    public void saveUser(User user) {
        mEditor.putString("id", user.getId());
        mEditor.putString("email", user.getEmail());
        mEditor.putString("name", user.getName());
        mEditor.putString("phone", user.getPhone());
        mEditor.putString("mobile", user.getMobile());
        mEditor.putBoolean("login", true);
        mEditor.apply();
    }

    public void saveRecent(RecentPnS recent) {
        Gson gson = new Gson();
        mEditor.putString("recent", gson.toJson(recent))
                .commit();
    }

    public RecentPnS getRecent() {
        Gson gson = new Gson();
        String json = mPreferences.getString("recent", "");
        RecentPnS recent;
        if (json.isEmpty()) {
            recent = new RecentPnS();
        } else {
            Type type = new TypeToken<RecentPnS>() {
            }.getType();
            recent = gson.fromJson(json, type);
        }
        return recent;
    }

    public void logout() {
        mEditor.clear().apply();
        Api.clearCredentials();
        Intent i = new Intent(mContext, StartActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(i);
    }

    public static class User {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String mobile;

        public User(String id, String email, String name, String phone, String mobile) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.mobile = mobile;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getId() { return id; }

        public void setId(String id) { this.id = id; }

        @Override
        public String toString() {
            return String.format("User:{id: %s, name: %s, email: %s, phone: %s, mobile: %s}", id, name, email, phone, mobile);
        }
    }
}
