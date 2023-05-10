package org.pjsip.pjsua2.app.interfaces;

/**
 * Created by rajan talwar on 2019-12-24.
 */
public interface SipServiceConstants {

    /**
     *Incoming call bundle constants
     */
    String INCOMING_NUMBER = "incoming_number";
    String INCOMING_SERVER = "incoming_server";
    String INCOMING_SLOT = "incoming_slot";
    String CALLER_CONTACT_NAME = "caller_contact_name";

    /*
     * Intent Actions for Sip Service
     */
    String END_SERVICE_ACTION = "end_service";
    String INCOMING_CALL_ACTION = "incoming_action";
    String PAUSE_CALL_ACTION = "pause_calls_action";
    String RESUME_CALL_ACTION = "resume_calls_action";
    String OUTGOING_CALL_ACTION = "outgoingcall";
    String INCOMING_CALL_DISCONNECTED = "incomingdisconnected";
    String FOREGROUND = "foreground";
    String BACKGROUND = "background";
    String LOGOUT_ACTION = "logout_action";
    String LOGIN_ACTION = "login_action";
    String UNREGISTER_PUSH_LOGOUT = "unregisterpush_and_logout";
    String RESTART_ACTION = "restart_action";
    String ACTION_SET_HOLD = "callSetHold";
    String ACTION_SET_UNHOLD = "callSetUNHold";
    String ACTION_SET_MUTE = "callSetMute";
    String ACTION_BLUETOOTH_STATE = "bluetoothstate";
    String ACTION_ACCEPT_INCOMING_CALL = "acceptIncomingCall";
    String ACTION_DECLINE_INCOMING_CALL = "declineIncomingCall";
    String ACTION_DECLINE_INCOMING_CALL_WITH_ERROR_CODE = "declineIncomingCallWithErrorCode";
    String ACTION_HANG_UP_CALL = "hangUpCall";
    String ACTION_SET_INCOMING_VIDEO = "setIncomingVideo";
    String ACTION_SET_SELF_VIDEO_ORIENTATION = "setSelfVideoOrientation";
    String ACTION_SET_VIDEO_MUTE = "setVideoMute";
    String ACTION_START_VIDEO_PREVIEW = "startVideoPreview";
    String ACTION_STOP_VIDEO_PREVIEW = "stopVideoPreview";
    String ACTION_SWITCH_VIDEO_CAPTURE_DEVICE = "switchVideoCaptureDevice";
    String DTMF_ACTION = "dtmf_action";

    /**
     * phone speaker states
     */
    enum SpeakerState {
        LOUDSPEAKER,
        BLUETOOTH,
        EARPIECE
    }


    /**
     * call states
     */
    enum CallScreenState {
        RINGING,
        RECEIVING,
        CALL_INITIATED,
        DIALING,
        ONGOING_CALL,
        DISCONNECTED,       // Remote disconnected would be handled here only
        INCOMING_SINGLE_CALL,
        CONNECTING,
        VIDEO_INITIATED,
        PLAY_RINGTONE
    }

    /**
     * call types
     */
    enum CALLTYPE {
        INCOMING,
        OUTGOING,
        MISSED
    }


    /**
     * Specific Parameters passed in the broadcast intents.
     */
    String PARAM_PHONE_NUMBER = "phone_number";
    String PARMA_IS_FROM_CALL = "callnotification";
    String PARAM_LINKED_UUID = "linkeduuid";
    String PARAM_CALLER_NAME = "caller_name";
    String PARAM_NO_ACTIVE_CALL = "noactivecall";
    String PARAM_TIME = "time";
    String PARAM_SECONDS = "seconds";
    String PARAM_CALLER_C_NAME = "caller_C_name";
    String PARAM_CALL_TYPE = "call_type";
    String PARAM_IS_INCOMINGCALL = "isIncomingCall";
    String PARAM_INCOMING_SERVER = "incoming_server";
    String PARAM_SLOT = "incoming_slot";
    String PARAM_MUTE = "mute";
    String PARAM_BLUETOOTH = "bluetooth";
    String PARAM_HOLD_ALL_CALL = "holdAllCalls";
    String PARAM_UNHOLD_ALL_CALL = "unholdAllCalls";
    String PARAM_CALL_STATE = "callState";
    String PARAM_DTMF_DIGIT = "dtmfdigit";
    String PARAM_ERROR_CODE_WHILE_REJECTING_INCOMING_CALL = "errorCodeWhileRejectingIncomingCall";
    String PARAM_CALL_NAME = "callName";

    String PARAM_SURFACE = "surface";
    String PARAM_ORIENTATION = "orientation";
    String PARAM_LOCAL_VIDEO_MUTE = "localVideoMute";
    String PARAM_VIDEO_MUTE = "videoMute";
    String PARAM_IS_VIDEO = "videoMute";
    String PARAM_INCOMING_VIDEO_WIDTH = "incomingVideoWidth";
    String PARAM_INCOMING_VIDEO_HEIGHT = "incomingVideoHeight";
    String PARAM_IS_LOGOUT = "islogout";


    String MISS_CALL_NOTIFICATION_CHANNEL = "notificationid";
    String SERVICE_NOTIFICATION_CHANNEL_ID = "callnotificationnew";
    String INCOMING_CALL_NOTIFICATION_CHANNEL_ID = "incomingcall";

    String PARAM_CALL_MEDIA_EVENT_TYPE = "mediaEventType";
    String INTENT_HANDLED = "intenthandled";


    /**
     * notification constants
     */
    int MISSED_NOTIFICATION_ID = 1674;
    int SERVICE_FOREGROUND_NOTIFICATION_ID = 121;
    int HANGUP_BROADCAST_ACTION_ID = 2;
    int ACCEPT_CALL_BROADCAST_ACTION_ID = 1;



    int FRONT_CAMERA_CAPTURE_DEVICE = 1;    // Front Camera idx
    int BACK_CAMERA_CAPTURE_DEVICE = 2;     // Back Camera idx
    int DEFAULT_RENDER_DEVICE = 0;          // OpenGL Render


    /**
     * Generic Constants
     */
    int DELAYED_JOB_DEFAULT_DELAY = 5000;
    int HOLD_DELAY = 500;


    /**
     * Video Configuration Params
     */

    String H264_CODEC_ID = "H264/97";
    int H264_DEF_WIDTH = 640;
    int H264_DEF_HEIGHT = 360;

    /**
     * Janus Bridge call specific parameters.
     */
    String PROFILE_LEVEL_ID_HEADER = "profile-level-id";
    String PROFILE_LEVEL_ID_LOCAL = "42e01e";
    String PROFILE_LEVEL_ID_JANUS_BRIDGE = "42e01f";


}
