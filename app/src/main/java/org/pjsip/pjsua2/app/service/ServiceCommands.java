package org.pjsip.pjsua2.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.core.app.NotificationCompat;


import org.pjsip.pjsua2.app.ErrorCodes;
import org.pjsip.pjsua2.app.application.SipApplication;
import org.pjsip.pjsua2.app.helper.AudioManagerHelper;
import org.pjsip.pjsua2.app.helper.BroadcastEventEmitter;
import org.pjsip.pjsua2.app.helper.ServiceExecutor;
import org.pjsip.pjsua2.app.interfaces.MyAppObserver;
import org.pjsip.pjsua2.app.interfaces.SipServiceConstants;

import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.PARAM_LINKED_UUID;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.SERVICE_NOTIFICATION_CHANNEL_ID;

/**
 * Service command class is used by client to talk to SDK i.e
 * {@link org.pjsip.pjsua2.app.service.PhoneSipService  PhoneSipService}
 *
 * @author rajantalwar
 * @since 12/9/2019
 * @version 1.0
 *
 */
public class ServiceCommands extends ServiceExecutor {

    /**
     *This method sets the file path in sdk for saving the VoIP logs.
     *If file path is a valid path then VoIP logging is enabled,
     *else not
     *
     * @param fileName  file path for saving the voip logs
     * @param context Activity context
     * @see org.pjsip.pjsua2.app.MyApp#init(MyAppObserver, String, boolean, boolean, Context)
     */
    public static void saveInformationForLogFiles(String fileName, Context context) {
        SipApplication.setLogFilesPathInformation(fileName, context);
    }


    /**
     *This method is called after user login but before initializing the sdk library for passing the
     * information needed for push registration.
     *
     * @param pushToken  firebase push token
     * @param versionName app version name
     * @param bundleID    application id
     * @param deviceInfo  device unique identifier
     * @param applicationID amazon server push notification id
     * @param deviceType device type like android or iOS
     * @param voipId user's VoIP id
     * @param voipPhoneID user's VoiP phone ID
     * @param context Android Context needed for shared perferences operations
     * @see org.pjsip.pjsua2.app.MyApp#getHeadersForPush(Context)
     */
    public static void saveInformationForPushRegisteration(String pushToken, String versionName,
                                                           String bundleID, String deviceInfo,
                                                           String applicationID, String deviceType,
                                                           String voipId, String voipPhoneID, Context context) {

        SipApplication.saveInformationForPush(pushToken, versionName, bundleID, deviceInfo,
                applicationID, deviceType, voipId, voipPhoneID,
                context);
    }

    /** This method is called by client for passing information to SDK which is needed for login
     * into SIP server.
     *
     * @param sipUsername  sipuserName credentials
     * @param sipPassword sipPassword  credentials
     * @param domainName  VoIP domain name
     * @param port VoIP port name
     * @param securePort optional needed in case of encyrpted commnunication
     * @param secureProtocolName optional needed in case of encyrpted commnunication
     * @param protocolName transport protocol to be used
     * @param context Android Context needed for shared perferences operations
     *
     * @see org.pjsip.pjsua2.app.service.PhoneSipService#login()
     */
    public static void saveInformationForSipLibraryInitialization(String sipUsername, String sipPassword,
                                                                  String domainName, int port,
                                                                  int securePort, String secureProtocolName,
                                                                  String protocolName, Context context) {

        SipApplication.saveInformationForSipLibraryInitialization(sipUsername,
                sipPassword,
                domainName,
                port,
                securePort,
                secureProtocolName,
                protocolName, context);
    }

    /**
     * This method is called by client for passing information to SDK which is needed for showing
     * foreground service notification {@link org.pjsip.pjsua2.app.service.PhoneSipService ForegroundServiceClass}.
     *
     * @param notificationTitle notification title
     * @param notificationSubtitle notification subtitle
     * @param notificationicon notification icon id
     * @param context Android Context needed for shared perferences operations
     */

    public static void saveInformationForForegroundServiceNotification(String notificationTitle,
                                                                       String notificationSubtitle,
                                                                       int notificationicon,
                                                                       Context context) {
        SipApplication.saveInformationForForegroundServiceNotification(notificationTitle,
                notificationSubtitle, notificationicon, context);

    }

    /**
     * Method to handle the push notifications
     *
     * @param status if canceled--> call is canceled by caller<br>
     *               answered --> call has been answered on another device<br>
     *               else it's an incoming call
     * @param from number of caller
     * @param server VoIP server
     * @param slot VoIP slot
     * @param linked_uuid unique id for identifying each call
     * @param callerName name of caller
     * @param context Android context needed for talking to SDK service
     */
    public static void handleCallPushNotification(String status, String from, String server,
                                                  String slot, String linked_uuid, String callerName,
                                                  Context context) {

        Intent intent = new Intent(context, PhoneSipService.class);
        if ("canceled".equalsIgnoreCase(status) ||
                "answered".equalsIgnoreCase(status)) {
            intent.setAction(SipServiceConstants.INCOMING_CALL_DISCONNECTED);
        } else {
            Log.d("navya15", "navya15" + "handleCallPushNotification:INCOMING_CALL_ACTION");
            intent.setAction(SipServiceConstants.INCOMING_CALL_ACTION);
        }
        intent.putExtra(SipServiceConstants.INCOMING_NUMBER, from);
        intent.putExtra(SipServiceConstants.INCOMING_SERVER, server);
        intent.putExtra(SipServiceConstants.INCOMING_SLOT, slot);
        intent.putExtra(SipServiceConstants.CALLER_CONTACT_NAME, callerName);
        intent.putExtra(PARAM_LINKED_UUID, linked_uuid);

        ServiceCommands.executeSipServiceAction(context, intent);

    }

    /**
     * Method for initiating an outbound call
     *
     * @param context Android context needed for talking to SDK service
     * @param destNumber called number
     * @param isVideo if true then it's a video call else normal audio call
     */
    public static void initiateCall(Context context, String destNumber, boolean isVideo) {
        Intent startCallingIntent = new Intent(context, PhoneSipService.class);
        startCallingIntent.setAction(SipServiceConstants.OUTGOING_CALL_ACTION);
        startCallingIntent.putExtra(SipServiceConstants.PARAM_PHONE_NUMBER, destNumber);
        startCallingIntent.putExtra(SipServiceConstants.PARAM_IS_VIDEO, isVideo);
        executeSipServiceAction(context, startCallingIntent);
    }

    /**
     * Method for hanging up an ongoing call
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void hangUpOnGoingCall(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_HANG_UP_CALL);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for rejecting an incoming call
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void rejectIncomingCall(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_DECLINE_INCOMING_CALL);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for rejecting an incoming call with specific error code
     *
     * @param context Android context needed for talking to SDK service
     * @param errorCode error code to be sent while rejecting the call
     */
    public static void rejectIncomingCallUserBusy(Context context, ErrorCodes errorCode) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.putExtra(SipServiceConstants.PARAM_ERROR_CODE_WHILE_REJECTING_INCOMING_CALL, errorCode.toString());
        intent.setAction(SipServiceConstants.ACTION_DECLINE_INCOMING_CALL_WITH_ERROR_CODE);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for placing the active call on Hold
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void holdOnGoingCall(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_SET_HOLD);
        intent.putExtra(SipServiceConstants.PARAM_HOLD_ALL_CALL, true);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for placing the active call on Unhold
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void unHoldOnGoingCall(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_SET_UNHOLD);
        intent.putExtra(SipServiceConstants.PARAM_UNHOLD_ALL_CALL, false);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for accepting an incoming call
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void acceptIncomingCall(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_ACCEPT_INCOMING_CALL);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for placing the active call on mute
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void mutePhone(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_SET_MUTE);
        intent.putExtra(SipServiceConstants.PARAM_MUTE, true);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for passing DTMF pressed keys to VOIP server
     *
     * @param context Android context needed for talking to SDK service
     * @param digit value of dtmf key pressed
     */
    public static void sendDTMF(Context context, String digit) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.DTMF_ACTION);
        intent.putExtra(SipServiceConstants.PARAM_DTMF_DIGIT, digit);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for placing the active call on unmute
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void unMutePhone(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_SET_MUTE);
        intent.putExtra(SipServiceConstants.PARAM_MUTE, false);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for turning on bluetooth
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void startPhoneEarPiece(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_BLUETOOTH_STATE);
        intent.putExtra(SipServiceConstants.PARAM_BLUETOOTH, false);
        executeSipServiceAction(context, intent);
        AudioManagerHelper.turnOffPhoneSpeaker(context);
    }

    /**
     * Method for turning on speaker
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void startLoudSpeaker(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_BLUETOOTH_STATE);
        intent.putExtra(SipServiceConstants.PARAM_BLUETOOTH, false);
        executeSipServiceAction(context, intent);
        AudioManagerHelper.turnOnPhoneSpeaker(context);
    }

    /**
     *  Method for turning on bluetooth
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void startBluetoothSpeaker(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_BLUETOOTH_STATE);
        intent.putExtra(SipServiceConstants.PARAM_BLUETOOTH, true);
        executeSipServiceAction(context, intent);
    }

    /**
     *Method for communicating to the SDK the app has been moved to background
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void moveToBackground(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.BACKGROUND);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for logging out the user from VOIP server
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void logout(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.LOGOUT_ACTION);
        executeSipServiceAction(context, intent);
    }

    /**
     *Method for loggin user into the VOIP server
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void login(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.LOGIN_ACTION);
        executeSipServiceAction(context, intent);
    }

    /**
     *Method for communicating to the SDK the app has been brought to foreground
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void onMoveToForeground(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.FOREGROUND);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for intitiating the incoming video feed
     *
     * @param context Android context needed for talking to SDK service
     * @param surface dedicated surface embedded inside view hierarchy for rendering the
     *                incoming video feed
     */
    public static void setupIncomingVideoFeed(Context context, Surface surface) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_SET_INCOMING_VIDEO);
        intent.putExtra(SipServiceConstants.PARAM_SURFACE, surface);
        executeSipServiceAction(context, intent);
    }

    /**
     *
     * @param context Android context needed for talking to SDK service
     * @param mute Boolean indicating whether to send video feed or not
     *             if true transmit video feed else not
     */
    public static void setVideoMute(Context context, boolean mute) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_SET_VIDEO_MUTE);
        intent.putExtra(SipServiceConstants.PARAM_VIDEO_MUTE, mute);
        executeSipServiceAction(context, intent);
    }

    /**
     * Methhod for start rendering outgoing video feed
     *
     * @param context Android context needed for talking to SDK service
     * @param surface dedicated surface embedded inside view hierarchy for rendering the
     *                 outgoing video feed
     */
    public static void startVideoPreview(Context context, Surface surface) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_START_VIDEO_PREVIEW);
        intent.putExtra(SipServiceConstants.PARAM_SURFACE, surface);
        executeSipServiceAction(context, intent);
    }

    /**
     * Method for creating an ongoing call notification
     *
     * @param notificationbody notification body
     * @param isCall boolean if true there is an ongoing active call else there is no active call
     * @param context Android context needed for talking to SDK service
     */
    public static Notification createForegroundServiceNotification(String notificationbody, boolean isCall,
                                                                   Context context) {
        if (notificationbody == null) {
            notificationbody = SipApplication.getNotificationBody(context);
        }
        Intent resultIntent = new Intent();
        BroadcastEventEmitter mBroadcastEmitter = new BroadcastEventEmitter(context);
        resultIntent.setAction(BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.NOTIFICATION_CLICK));
        resultIntent.putExtra(SipServiceConstants.INTENT_HANDLED, true);
        resultIntent.putExtra(SipServiceConstants.PARMA_IS_FROM_CALL, isCall);
        resultIntent = mBroadcastEmitter.getExplicitIntent(resultIntent);

        PendingIntent resultPendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder mBuilder;
        String channelId = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? SERVICE_NOTIFICATION_CHANNEL_ID : "";
        mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(SipApplication.getNotificationicon(context))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(SipApplication.getNotificationContentTitle(context))
                .setContentText(notificationbody);
        mBuilder.setContentIntent(resultPendingIntent);

        return mBuilder.build();
    }

  /**
   * Method for changing the video orientation
   *
   * @param context Android context needed for talking to SDK service
   * @param orientation Rotation constant
   *
   * {@link android.view.Surface#ROTATION_0 }  0 degree rotation (natural orientation).<br>
   *
   * {@link android.view.Surface#ROTATION_90}  90 degree rotation.<br>
   *
   * {@link android.view.Surface#ROTATION_180} 180 degree rotation.<br>
   *
   * {@link android.view.Surface#ROTATION_270} 270 degree rotation.<br>
   */
    public static void changeVideoOrientation(Context context, int orientation) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_SET_SELF_VIDEO_ORIENTATION);
        intent.putExtra(SipServiceConstants.PARAM_ORIENTATION, orientation);
        executeSipServiceAction(context, intent);
    }

    /**
     * This method is used to stop sending the video frames from the device
     * @param context Android context needed for talking to SDK service
     */

    public static void stopVideoPreview(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_STOP_VIDEO_PREVIEW);
        executeSipServiceAction(context, intent);
    }

    /**
     * This method is used for toggling between front camera and
     * back camera of the device
     *
     *  @param context Android context needed for talking to SDK service
     */
    public static void switchVideoCaptureDevice(Context context) {

        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.ACTION_SWITCH_VIDEO_CAPTURE_DEVICE);
        executeSipServiceAction(context, intent);
    }

    /**
     * This method
     * first unregisters the user from push service and
     * after that logs them out VoIP services.
     * Once above two operations are successful send an event to client
     * to indicate that they can now logout from the application
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void unregisterPushAndLogout(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.UNREGISTER_PUSH_LOGOUT);
        executeSipServiceAction(context, intent);
    }

    /**
     *This method is used to put current active call on Hold
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void pauseActiveCalls(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.PAUSE_CALL_ACTION);
        executeSipServiceAction(context, intent);
    }

    /**
     * This method is used to Unhold the current active call
     *
     * @param context Android context needed for talking to SDK service
     */
    public static void resumeActiveCalls(Context context) {
        Intent intent = new Intent(context, ServiceCommands.class);
        intent.setAction(SipServiceConstants.RESUME_CALL_ACTION);
        executeSipServiceAction(context, intent);
    }



}

