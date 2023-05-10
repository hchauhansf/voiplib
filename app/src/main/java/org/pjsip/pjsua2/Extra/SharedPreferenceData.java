package org.pjsip.pjsua2.Extra;

import android.content.Context;
import android.content.SharedPreferences;

import org.pjsip.pjsua2.app.application.SipApplication;


/**
 * SharedPreferenceData class is used to save and retreive shared pref data.
 *
 * @author rajantalwar
 * @version 1.0
 * @since 2019-12-24.
 */

public class SharedPreferenceData {

    public static final String TOKEN = "token";

    public static final String PROTOCOL = "protocol";

    // notification constants
    public static final String NOTIFICATION_BODY = "notificationBody";
    public static final String NOTIFICATION_CONTENT_TITLE = "notificationContentTitle";
    public static final String NOTIFICATION_ICON = "notificationicon";

    // sip account constants
    public static final String SIP_USER_NAME = "sipUsername";
    public static final String SIP_PASSWORD = "sipPassword";
    public static final String DOMAIN_NAME = "domainName";
    public static final String PORT = "port";
    public static final String SECURE_PORT = "securePort";
    public static final String SECURE_PROTOCOL_NAME = "secureProtocolName";
    public static final String PROTOCOL_NAME = "protocolName";


    // push registration constants
    public static final String DEVICE_TYPE = "hdeviceType";
    public static final String VOIP_ID = "hVoipID";
    public static final String VOIP_PHONE_ID = "hVoipPhoneID";

    public static final String PUSH_TOKEN = "hpushToken";
    public static final String VERSION_NAME = "hversionName";
    public static final String BUNDLE_ID = "finalhbundleID";
    public static final String DEVICE_INFO = "hdeviceInfo";
    public static final String APPLICATION_ID = "happlicationID";
    public static final String LOGS_FOLDER_PATH = "logs_folder_path";
    public static final String LOGS_FILE_NAME = "logs_file_name";



    /**
     * This method is used to save string value in shared prefrences.
     *
     * @param key     Shared pref key
     * @param context Android context needed
     *
     */
    public static void putInSharedPreference(String key, String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.phone.app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }



    /**
     * This method is used to save integer value in shared prefrences.
     *
     * @param key     Shared pref key
     * @param context Android context needed
     *
     */
    public static void putInSharedPreference(String key, int value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.phone.app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }



    /**
     * This method is used to save boolean in shared prefrences.
     *
     * @param key     Shared pref key
     * @param context Android context needed
     *
     */
    public static void putInSharedPreference(String key, boolean value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.phone.app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    /**
     * This method is used to get shared pref string values on the basis of key.
     *
     * @param key     Shared pref key
     * @param context Android context needed
     *
     * @return string value from shared pref
     */
    public static String getInSharedPreference(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.phone.app", Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }


    /**
     * This method is used to get shared pref integer values on the basis of key.
     *
     * @param key     Shared pref key
     * @param context Android context needed
     * @return integer value from shared pref
     */
    public static int getIntSharedPreference(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.phone.app", Context.MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }


    /**
     * This method is used to get shared pref string values on the basis of key.
     *
     * @param key          Shared pref key
     * @param defaultvalue default value if value doesn't exist
     * @param context      Android context needed
     * @return string value from shared pref
     */
    public static String getInSharedPreference(String key, String defaultvalue, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.phone.app", Context.MODE_PRIVATE);
        return prefs.getString(key, defaultvalue);
    }


    /**
     * This method is used to get shared pref boolean on the basis of key.
     *
     * @param key          Shared pref key
     * @param defaultvalue default value if value doesn't exist
     * @param context      Android context needed
     * @return boolean value from shared pref
     */
    public static boolean getBooleanPreference(String key, boolean defaultvalue, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.phone.app", Context.MODE_PRIVATE);
        return prefs.getBoolean(key, defaultvalue);
    }


    /**
     * This method is used to clear all shared prefs data.
     *
     * @param context Android context needed
     */
    public static void clearAllSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.phone.app", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }


    /**
     * This method is used to get secure protocol from shared prefs.
     *
     * @param context Android context needed
     */
    public static boolean isSecureProtocol(Context context) {
        return SharedPreferenceData.getBooleanPreference(PROTOCOL, false, context);
    }

}
