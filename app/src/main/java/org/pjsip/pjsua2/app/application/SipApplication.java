package org.pjsip.pjsua2.app.application;

import android.app.Application;
import android.content.Context;

import org.pjsip.pjsua2.Extra.SharedPreferenceData;

import static org.pjsip.pjsua2.Extra.SharedPreferenceData.APPLICATION_ID;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.BUNDLE_ID;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.DEVICE_INFO;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.DEVICE_TYPE;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.DOMAIN_NAME;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.LOGS_FILE_NAME;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.LOGS_FOLDER_PATH;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.NOTIFICATION_BODY;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.NOTIFICATION_CONTENT_TITLE;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.NOTIFICATION_ICON;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.PORT;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.PROTOCOL_NAME;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.PUSH_TOKEN;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.SECURE_PORT;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.SECURE_PROTOCOL_NAME;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.SIP_PASSWORD;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.SIP_USER_NAME;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.VERSION_NAME;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.VOIP_ID;
import static org.pjsip.pjsua2.Extra.SharedPreferenceData.VOIP_PHONE_ID;

/**
 * SipApplication class is used to save information to initialize library.
 *
 * @author rajantalwar
 * @version 1.0
 * @since 2019-12-24.
 */
public class SipApplication {


    /**
     * This method is used to save information for push notification.
     *
     * @param pushToken     pushToken
     * @param versionName   versionName
     * @param bundleID      bundleID
     * @param deviceInfo    deviceInfo
     * @param applicationID applicationID
     * @param deviceType    deviceType
     * @param voipId        voipId
     * @param voipPhoneID   voipPhoneID
     * @param context       Android context needed
     */
    public static void saveInformationForPush(String pushToken, String versionName, String bundleID,
                                              String deviceInfo, String applicationID, String deviceType,
                                              String voipId, String voipPhoneID, Context context) {
        setPushToken(pushToken, context);
        setVersionName(versionName, context);
        setBundleID(bundleID, context);
        setDeviceInfo(deviceInfo, context);
        setApplicationID(applicationID, context);
        setHdeviceType(deviceType, context);
        sethVoipID(voipId, context);
        sethVoipPhoneID(voipPhoneID, context);
    }


    /**
     * This method is used to save information for library initialization.
     *
     * @param sipUsername        sipUsername
     * @param sipPassword        sipPassword
     * @param domainName         domainName
     * @param port               port
     * @param securePort         securePort
     * @param secureProtocolName secureProtocolName
     * @param protocolName       protocolName
     * @param context            Android context needed
     */
    public static void saveInformationForSipLibraryInitialization(String sipUsername, String sipPassword,
                                                                  String domainName, int port,
                                                                  int securePort, String secureProtocolName,
                                                                  String protocolName, Context context) {
        setSipUsername(sipUsername, context);
        setSipPassword(sipPassword, context);
        setDomainName(domainName, context);
        setPort(port, context);
        setSecurePort(securePort, context);
        setSecureProtocolName(secureProtocolName, context);
        setProtocolName(protocolName, context);
    }


    /**
     * This method is used to save foreground notification information.
     *
     * @param notificationTitle    notification Title
     * @param notificationSubtitle notification Subtitle
     * @param notificationicon     notification icon
     * @param context              Android context needed
     */
    public static void saveInformationForForegroundServiceNotification(String notificationTitle,
                                                                       String notificationSubtitle,
                                                                       int notificationicon,
                                                                       Context context) {
        setNotificationBody(notificationTitle, context);
        setNotificationContentTitle(notificationSubtitle, context);
        setNotificationicon(notificationicon, context);
    }


    /**
     * This method is used to get sipUsername.
     *
     * @param context Android context needed
     * @return sipUsername
     */
    public static String getSipUsername(Context context) {
        return SharedPreferenceData.getInSharedPreference(SIP_USER_NAME, context);
    }


    /**
     * This method is used to set sipUsername.
     *
     * @param sipUsername sipUsername
     * @param context     Android context needed
     */
    public static void setSipUsername(String sipUsername, Context context) {
        SharedPreferenceData.putInSharedPreference(SIP_USER_NAME, sipUsername, context);
    }


    /**
     * This method is used to get sip password.
     *
     * @param context Android context needed
     * @return sip password
     */
    public static String getSipPassword(Context context) {
        return SharedPreferenceData.getInSharedPreference(SIP_PASSWORD, context);
    }


    /**
     * This method is used to set sip password.
     *
     * @param sipPassword sip password
     * @param context     Android context needed
     */
    public static void setSipPassword(String sipPassword, Context context) {
        SharedPreferenceData.putInSharedPreference(SIP_PASSWORD, sipPassword, context);
    }


    /**
     * This method is used to get domain name.
     *
     * @param context Android context needed
     * @return domain name
     */
    public static String getDomainName(Context context) {
        return SharedPreferenceData.getInSharedPreference(DOMAIN_NAME, context);
    }


    /**
     * This method is used to set notification domain name.
     *
     * @param domainName domain name
     * @param context    Android context needed
     */
    public static void setDomainName(String domainName, Context context) {
        SharedPreferenceData.putInSharedPreference(DOMAIN_NAME, domainName, context);
    }


    /**
     * This method is used to get port.
     *
     * @param context Android context needed
     * @return port
     */
    public static int getPort(Context context) {
        return SharedPreferenceData.getIntSharedPreference(PORT, context);
    }


    /**
     * This method is used to set port.
     *
     * @param port    port
     * @param context Android context needed
     */
    public static void setPort(int port, Context context) {
        SharedPreferenceData.putInSharedPreference(PORT, port, context);
    }


    /**
     * This method is used to get securePort.
     *
     * @param context Android context needed
     * @return securePort
     */
    public static int getSecurePort(Context context) {
        return SharedPreferenceData.getIntSharedPreference(SECURE_PORT, context);
    }


    /**
     * This method is used to set securePort.
     *
     * @param securePort securePort
     * @param context    Android context needed
     */
    public static void setSecurePort(int securePort, Context context) {
        SharedPreferenceData.putInSharedPreference(SECURE_PORT, securePort, context);
    }


    /**
     * This method is used to get secureProtocolName.
     *
     * @param context Android context needed
     * @return secureProtocolName
     */
    public static String getSecureProtocolName(Context context) {
        return SharedPreferenceData.getInSharedPreference(SECURE_PROTOCOL_NAME, context);
    }


    /**
     * This method is used to set secureProtocolName.
     *
     * @param secureProtocolName secureProtocolName
     * @param context            Android context needed
     */
    public static void setSecureProtocolName(String secureProtocolName, Context context) {
        SharedPreferenceData.putInSharedPreference(SECURE_PROTOCOL_NAME, secureProtocolName, context);
    }


    /**
     * This method is used to get protocolName.
     *
     * @param context Android context needed
     * @return protocolName
     */
    public static String getProtocolName(Context context) {
        return SharedPreferenceData.getInSharedPreference(PROTOCOL_NAME, context);
    }


    /**
     * This method is used to set protocolName.
     *
     * @param protocolName protocolName
     * @param context      Android context needed
     */
    public static void setProtocolName(String protocolName, Context context) {
        SharedPreferenceData.putInSharedPreference(PROTOCOL_NAME, protocolName, context);
    }


    /**
     * This method is used to get pushToken.
     *
     * @param context Android context needed
     * @return pushToken
     */
    public static String getPushToken(Context context) {
        return SharedPreferenceData.getInSharedPreference(PUSH_TOKEN, context);
    }


    /**
     * This method is used to set pushToken.
     *
     * @param pushToken pushToken
     * @param context   Android context needed
     */
    public static void setPushToken(String pushToken, Context context) {
        SharedPreferenceData.putInSharedPreference(PUSH_TOKEN, pushToken, context);
    }


    /**
     * This method is used to get versionName.
     *
     * @param context Android context needed
     * @return versionName
     */
    public static String getVersionName(Context context) {
        return SharedPreferenceData.getInSharedPreference(VERSION_NAME, context);
    }


    /**
     * This method is used to set versionName.
     *
     * @param versionName versionName
     * @param context     Android context needed
     */
    private static void setVersionName(String versionName, Context context) {
        SharedPreferenceData.putInSharedPreference(VERSION_NAME, versionName, context);
    }


    /**
     * This method is used to get bundle id.
     *
     * @param context Android context needed
     * @return bundle id
     */
    public static String getBundleID(Context context) {
        return SharedPreferenceData.getInSharedPreference(BUNDLE_ID, context);
    }


    /**
     * This method is used to set bundle id.
     *
     * @param bundleID bundle id
     * @param context  Android context needed
     */
    private static void setBundleID(String bundleID, Context context) {
        SharedPreferenceData.putInSharedPreference(BUNDLE_ID, bundleID, context);
    }


    /**
     * This method is used to get device info.
     *
     * @param context Android context needed
     * @return device info
     */
    public static String getDeviceInfo(Context context) {
        return SharedPreferenceData.getInSharedPreference(DEVICE_INFO, context);
    }


    /**
     * This method is used to set device info.
     *
     * @param deviceInfo device info
     * @param context    Android context needed
     */
    private static void setDeviceInfo(String deviceInfo, Context context) {
        SharedPreferenceData.putInSharedPreference(DEVICE_INFO, deviceInfo, context);
    }


    /**
     * This method is used to get application id.
     *
     * @param context Android context needed
     * @return application id.
     */
    public static String getApplicationID(Context context) {
        return SharedPreferenceData.getInSharedPreference(APPLICATION_ID, context);
    }


    /**
     * This method is used to set application id.
     *
     * @param applicationID application id
     * @param context       Android context needed
     */
    private static void setApplicationID(String applicationID, Context context) {
        SharedPreferenceData.putInSharedPreference(APPLICATION_ID, applicationID, context);
    }


    /**
     * This method is used to get device type.
     *
     * @param context Android context needed
     * @return device type
     */
    public static String getHdeviceType(Context context) {
        return SharedPreferenceData.getInSharedPreference(DEVICE_TYPE, context);
    }


    /**
     * This method is used to set device type.
     *
     * @param hdeviceType device type
     * @param context     Android context needed
     */
    public static void setHdeviceType(String hdeviceType, Context context) {
        SharedPreferenceData.putInSharedPreference(DEVICE_TYPE, hdeviceType, context);
    }


    /**
     * This method is used to get voip id
     *
     * @param context Android context needed
     * @return voip id
     */
    public static String gethVoipID(Context context) {
        return SharedPreferenceData.getInSharedPreference(VOIP_ID, context);
    }


    /**
     * This method is used to set voip id
     *
     * @param hVoipID voip id
     * @param context Android context needed
     */
    public static void sethVoipID(String hVoipID, Context context) {
        SharedPreferenceData.putInSharedPreference(VOIP_ID, hVoipID, context);
    }


    /**
     * This method is used to get voip phone id.
     *
     * @param context Android context needed
     * @return voip phone id
     */
    public static String gethVoipPhoneID(Context context) {
        return SharedPreferenceData.getInSharedPreference(VOIP_PHONE_ID, context);
    }

    /**
     * This method is used to set voip phone id.
     *
     * @param hVoipPhoneID voip phone id
     * @param context      Android context needed
     */
    public static void sethVoipPhoneID(String hVoipPhoneID, Context context) {
        SharedPreferenceData.putInSharedPreference(VOIP_PHONE_ID, hVoipPhoneID, context);
    }


    /**
     * This method is used to get notification message.
     *
     * @param context Android context needed
     * @return notification message
     */
    public static String getNotificationBody(Context context) {
        return SharedPreferenceData.getInSharedPreference(NOTIFICATION_BODY, context);
    }


    /**
     * This method is used to set notification message.
     *
     * @param mnotificationTitle message
     * @param context            Android context needed
     */
    public static void setNotificationBody(String mnotificationTitle, Context context) {
        SharedPreferenceData.putInSharedPreference(NOTIFICATION_BODY, mnotificationTitle, context);
    }


    /**
     * This method is used to get notification title.
     *
     * @param context Android context needed
     * @return notification title
     */
    public static String getNotificationContentTitle(Context context) {
        return SharedPreferenceData.getInSharedPreference(NOTIFICATION_CONTENT_TITLE, context);
    }


    /**
     * This method is used to set notification title.
     *
     * @param mnotificationSubtitle title
     * @param context               Android context needed
     */
    public static void setNotificationContentTitle(String mnotificationSubtitle, Context context) {
        SharedPreferenceData.putInSharedPreference(NOTIFICATION_CONTENT_TITLE, mnotificationSubtitle, context);
    }


    /**
     * This method is used to get notification icon.
     *
     * @param context Android context needed
     * @return notification icon
     */
    public static int getNotificationicon(Context context) {
        return SharedPreferenceData.getIntSharedPreference(NOTIFICATION_ICON, context);
    }


    /**
     * This method is used to set notification icon.
     *
     * @param mnotificationicon icon
     * @param context           Android context needed
     */
    public static void setNotificationicon(int mnotificationicon, Context context) {
        SharedPreferenceData.putInSharedPreference(NOTIFICATION_ICON, mnotificationicon, context);
    }


    /**
     * This method is used to set log file name and path to sdk
     *
     * @param fileName filename for saving logs
     * @param context  Android context needed
     */
    public static void setLogFilesPathInformation(String fileName, Context context) {
        SharedPreferenceData.putInSharedPreference(LOGS_FILE_NAME, fileName, context);
    }


    /**
     * This method is used to retrieve log file directory.
     *
     * @param context Android context needed
     * @return path of log file directory
     */
    public static String getLogFilesFolderPath(Context context) {
        return SharedPreferenceData.getInSharedPreference(LOGS_FOLDER_PATH, context);
    }


    /**
     * This method is used to retrieve log file path.
     *
     * @param context Android context needed
     * @return path of log file
     */
    public static String getLogFilePath(Context context) {
        return SharedPreferenceData.getInSharedPreference(LOGS_FILE_NAME, context);
    }

}
