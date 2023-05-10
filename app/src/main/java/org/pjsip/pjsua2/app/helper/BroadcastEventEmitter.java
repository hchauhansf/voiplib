package org.pjsip.pjsua2.app.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.pjsip.pjsua2.app.call.CallEvents;
import org.pjsip.pjsua2.app.interfaces.SipServiceConstants;
import org.pjsip.pjsua2.app.service.ServiceCommands;

import java.util.List;

/**
 * BroadcastEventEmitter class is used to send calling events to client.
 * {@link org.pjsip.pjsua2.app.service.PhoneSipService  PhoneSipService}
 *
 * @author rajantalwar
 * @version 1.0
 * @since 2019-12-24.
 */
public class BroadcastEventEmitter implements SipServiceConstants {
    public static String NAMESPACE = "com.rajan";
    private Context mContext;

    /**
     * Enumeration of the broadcast actions
     */
    public enum BroadcastAction {
        REGISTRATION,
        INCOMING_CALL,
        CALL_STATE,
        OUTGOING_CALL,
        STACK_STATUS,
        CODEC_PRIORITIES,
        CODEC_PRIORITIES_SET_STATUS,
        MISSED_CALL,
        VIDEO_SIZE,
        CALL_STATS,
        EXIT_APPLICATION,
        SAVE_CALL_AFTER_HANGUP,
        CLEAN_UP_CALL,
        CALL_BACK_ACTION,
        NOTIFICATION_CLICK,
        END_SERVICE_ACTION,
        VIDEO_FEED,
        LOGOUT,
        ACCEPT_INCOMING_CALL_ACTION,
        CALL_MEDIA_EVENT
    }


    /**
     * Constructor of the class
     *
     * @param context Android context needed for talking to SDK service
     */
    public BroadcastEventEmitter(Context context) {
        mContext = context;
    }



    /**
     * This method is used to get unique broadcast action string
     *
     * @param action broadcast action type {@link BroadcastAction}
     *
     * @return unique broadcast action string
     */
    public static String getAction(BroadcastAction action) {
        return NAMESPACE + "." + action;
    }


    /**
     * This method is used to send application’s broadcast receiver that will
     * get triggered when the custom intent action defined, gets called.
     *
     * @param intent intent
     *
     * @return status of broadcast sent or not
     */
    private boolean sendExplicitBroadcast(Intent intent) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> matches = pm.queryBroadcastReceivers(intent, 0);
        boolean sent = false;

        for (ResolveInfo resolveInfo : matches) {
            ComponentName cn =
                    new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);

            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mContext.sendBroadcast(intent);
            sent = true;
        }
        return sent;
    }



    /**
     * This method is used to retrieve all receivers that can handle a broadcast of the given intent.
     *
     * @param intent intent
     *
     * @return the matched intent
     */
    public Intent getExplicitIntent(Intent intent) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> matches = pm.queryBroadcastReceivers(intent, 0);
        boolean sent = false;

        for (ResolveInfo resolveInfo : matches) {
            ComponentName cn =
                    new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);

            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            break;

        }
        return intent;
    }



    /**
     * This method is used to exit
     *
     */
    public void handleExitApplications() {
        final Intent intent = new Intent();
        intent.setAction(getAction(BroadcastAction.EXIT_APPLICATION));
        sendExplicitBroadcast(intent);
    }



    /**
     * This method is used to disconnect the call if no active call is present.
     *
     */
    public void handleCleanUpCall() {
        final Intent intent = new Intent();
        intent.setAction(getAction(BroadcastAction.CLEAN_UP_CALL));
        sendExplicitBroadcast(intent);
    }



    /**
     * This method is used for sending missed call information to client.
     *
     * @param isIncomingCall tells missed call is incoming
     * @param number number associated with missed call
     * @param linkedUUid linkedUUid of the missed call
     * @param callerName name associated with the missed call
     * @param time timestamp of the missed call in milliseconds
     * @param seconds duration of the missed call in seconds
     * @param callerCname callerCname associated with the missed call
     * @param callType type of call (incoming, outgoing, missed) {@link org.pjsip.pjsua2.app.interfaces.SipServiceConstants.CALLTYPE}
     */
    public void handleMissedCall(boolean isIncomingCall, String number, String linkedUUid,
                                 String callerName, long time, long seconds, String callerCname,
                                 CALLTYPE callType) {
        Intent intent;
        intent = new Intent();
        intent.putExtra(SipServiceConstants.PARAM_PHONE_NUMBER, number);
        intent.putExtra(PARAM_IS_INCOMINGCALL, isIncomingCall);
        intent.putExtra(PARAM_PHONE_NUMBER, number);
        intent.putExtra(PARAM_LINKED_UUID, linkedUUid);
        intent.putExtra(PARAM_CALLER_NAME, callerName);
        intent.putExtra(PARAM_TIME, time);
        intent.putExtra(PARAM_SECONDS, seconds);
        intent.putExtra(PARAM_CALLER_C_NAME, callerCname);
        intent.putExtra(PARAM_CALL_TYPE, callType);
        intent.setAction(getAction(BroadcastAction.MISSED_CALL));
        sendExplicitBroadcast(intent);
    }


    /**
     * This method is used for sending current call information after hangup to client.
     *
     * @param isIncomingCall tells current call is incoming or outgoing
     * @param number number associated with current call
     * @param linkedUUid linkedUUid of the current call
     * @param callerName name associated with the current call
     * @param time timestamp of the current call in milliseconds
     * @param seconds duration of the current call in seconds
     * @param callerCname callerCname associated with the current call
     * @param callType type of call (incoming, outgoing, missed) {@link org.pjsip.pjsua2.app.interfaces.SipServiceConstants.CALLTYPE}
     */
    public void handleSaveCallAfterHangup(boolean isIncomingCall, String number, String linkedUUid,
                                          String callerName, long time, long seconds, String callerCname,
                                          CALLTYPE callType) {
        final Intent intent = new Intent();
        intent.putExtra(PARAM_IS_INCOMINGCALL, isIncomingCall);
        intent.putExtra(PARAM_PHONE_NUMBER, number);
        intent.putExtra(PARAM_LINKED_UUID, linkedUUid);
        intent.putExtra(PARAM_CALLER_NAME, callerName);
        intent.putExtra(PARAM_TIME, time);
        intent.putExtra(PARAM_SECONDS, seconds);
        intent.putExtra(PARAM_CALLER_C_NAME, callerCname);
        intent.putExtra(PARAM_CALL_TYPE, callType);
        intent.setAction(getAction(BroadcastAction.SAVE_CALL_AFTER_HANGUP));
        sendExplicitBroadcast(intent);
    }



    /**
     * This method is used for sending incoming call information to client.
     *
     * @param number incoming call number
     * @param incomingServer server info of incoming call
     * @param slot slot info of incoming call
     * @param linkeduid linkedUid of the incoming call
     * @param isActiveCallPresent status of activeCallPresent
     * @param isVideo incoming call is video call or not
     * @param callName name associated with the incoming call
     */
    public void handleIncomingCall(String number, String incomingServer,
                                   String slot, String linkeduid, boolean isActiveCallPresent,
                                   boolean isVideo, String callName) {
        final Intent intent = new Intent();
        intent.putExtra(PARAM_INCOMING_SERVER, incomingServer);
        intent.putExtra(PARAM_SLOT, slot);
        intent.putExtra(PARAM_LINKED_UUID, linkeduid);
        intent.putExtra(PARAM_PHONE_NUMBER, number);
        intent.putExtra(PARAM_NO_ACTIVE_CALL, isActiveCallPresent);
        intent.putExtra(PARAM_IS_VIDEO, isVideo);
        intent.putExtra(CALLER_CONTACT_NAME, callName);
        intent.setAction(getAction(BroadcastAction.INCOMING_CALL));
        sendExplicitBroadcast(intent);
    }



    /**
     * This method is used for sending different calling events to update calling screen to client.
     * {@link org.pjsip.pjsua2.app.interfaces.SipServiceConstants.CallScreenState}
     *
     * @param screenUpdate CallEvents.ScreenUpdate
     */
    public synchronized void callState(CallEvents.ScreenUpdate screenUpdate) {
        final Intent intent = new Intent();
        intent.putExtra(PARAM_CALL_STATE, screenUpdate);
        intent.setAction(getAction(BroadcastAction.CALL_STATE));
        sendExplicitBroadcast(intent);
    }


    /**
     * This method is used for sending video width and height to client.
     *
     * @param width  video width
     * @param height video height
     */
    public synchronized void videoSize(int width, int height) {
        final Intent intent = new Intent();

        intent.setAction(getAction(BroadcastAction.VIDEO_SIZE));
        intent.putExtra(PARAM_INCOMING_VIDEO_WIDTH, width);
        intent.putExtra(PARAM_INCOMING_VIDEO_HEIGHT, height);

        mContext.sendBroadcast(intent);
    }


    /**
     * This method is used for sending logout event to client.
     *
     * @param isLogout logout status
     */
    public void handleLogout(boolean isLogout) {
        final Intent intent = new Intent();
        intent.putExtra(PARAM_IS_LOGOUT, isLogout);
        intent.setAction(getAction(BroadcastAction.LOGOUT));
        sendExplicitBroadcast(intent);
    }


    /**
     * This method is used for sending different type of mediaEvents to client.
     *
     * @param mediaEventType Type of mediaEvent  {@link org.pjsip.pjsua2.pjmedia_event_type}
     */
    public void sendCallMediaEvent(String mediaEventType) {
        final Intent intent = new Intent();
        intent.putExtra(PARAM_CALL_MEDIA_EVENT_TYPE, mediaEventType);
        intent.setAction(getAction(BroadcastAction.CALL_MEDIA_EVENT));
        sendExplicitBroadcast(intent);
    }
}
