package org.pjsip.pjsua2.app.call;


import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.StreamInfo;
import org.pjsip.pjsua2.app.MyAccount;
import org.pjsip.pjsua2.app.call.MyCall;
import org.pjsip.pjsua2.app.interfaces.ICall;
import org.pjsip.pjsua2.app.interfaces.SipServiceConstants;
import org.pjsip.pjsua2.app.service.PhoneSipService;
import org.pjsip.pjsua2.pjmedia_type;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;

import org.pjsip.pjsua2.CallVidSetStreamParam;

import static org.pjsip.pjsua2.pjmedia_type.PJMEDIA_TYPE_AUDIO;
import static org.pjsip.pjsua2.pjmedia_type.PJMEDIA_TYPE_VIDEO;
import static org.pjsip.pjsua2.pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE;
import static org.pjsip.pjsua2.pjsua_call_media_status.PJSUA_CALL_MEDIA_LOCAL_HOLD;

import org.pjsip.pjsua2.pjsua_call_vid_strm_op;

/**
 * Created by davinder on 4/5/17.
 */

public class SipCall extends MyCall implements Serializable, ICall {


    public static final String TAG = "SipCall";
    private long time;
    private SipServiceConstants.CALLTYPE callType;

    public enum VOIP_CALL_STATE {
        CONNECTING, CONNECTED, CALLING, RINGING, INCOMING_CALL,
        DISCONNECTED
    }

    private int callerId;
    private int callId;
    private int callerDID;
    private String linkedUUID;
    private String callerCname;
    private int callerContactId;
    private String callStatus;
    private boolean active;
    boolean hasVideo = true;
    VOIP_CALL_STATE state;
    boolean conferenceCall;
    boolean mute;
    boolean holdCall;
    boolean isDeleted;
    boolean isIncomingCall, isCallConnected;
    String label;
    private PhoneSipService phoneSipService;
    private String pjsipCallID;

    /* @property(nonatomic, strong) UIImage *image;
         @property(nonatomic, strong) Contacts *contact;
         @property(nonatomic, strong) NSData *imageData;
         @property(nonatomic, strong) NSString *phoneType;*/
    String imageUrl;
    String codecName;
    String callerName;
    boolean isDialedCall;
    String currentTimer;
    long seconds;
    Observable<String> stringObservable;
    /*@property(nonatomic, strong) NSUUID *callUUID;*/
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            seconds++;
            currentTimer = convertTimeInString(seconds);
        }
    };


    private boolean isCallOnMute;
    private boolean isCallOnSpeaker;


    public SipCall(MyAccount acc, int call_id, PhoneSipService sipService) {
        super(acc, call_id);
        this.phoneSipService = sipService;
    }

    public SipCall(MyAccount acc, int call_id) {
        super(acc, call_id);
    }


    public int getCallerId() {
        return callerId;
    }

    public void setCallerId(int callerId) {
        this.callerId = callerId;
    }

    public int getCallerDID() {
        return callerDID;
    }

    public void setCallerDID(int callerDID) {
        this.callerDID = callerDID;
    }

    @Override
    public String getCallerCname() {
        return callerCname;
    }

    @Override
    public void setCallerCname(String callerCname) {
        this.callerCname = callerCname;
    }

    public int getCallerContactId() {
        return callerContactId;
    }

    public void setCallerContactId(int callerContactId) {
        this.callerContactId = callerContactId;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public String getCallUid() {
        try {
            return getInfo().getCallIdString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setState(VOIP_CALL_STATE state) {
        if (state == VOIP_CALL_STATE.CONNECTED) {
            try {
                setCallConnected(true);
                timer.schedule(task, 0, 1000);
            } catch (Exception e) {
                //  e.printStackTrace();
            }
        }
        this.state = state;
    }

    public String getCallState() {
        String currentState;
        if (state == null)
            return "Connecting";
        switch (state) {
            case RINGING:
                currentState = "Ringing";
                break;
            case CALLING:
                currentState = "Dialing";
                break;
            case DISCONNECTED:
                currentState = "Disconnected";
                break;
            case CONNECTED:
                currentState = "Connected";
                break;
            case INCOMING_CALL:
                currentState = "Incoming";
                break;
            default:
                currentState = "";
        }
        return currentState;
    }

    public VOIP_CALL_STATE getState() {
        return state;
    }

    public boolean isConferenceCall() {
        return conferenceCall;
    }

    public void setConferenceCall(boolean conferenceCall) {
        this.conferenceCall = conferenceCall;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isHoldCall() {
        return holdCall;
    }

    @Override
    public boolean isCallIdPresent(int callId) {
        return this.callId == callId;
    }

    public void setHoldCall(boolean holdCall) {
        this.holdCall = holdCall;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getCallerName() {
        if (TextUtils.isEmpty(callerName))
            return "";
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public boolean isDialedCall() {
        return isDialedCall;
    }

    public void setDialedCall(boolean dialedCall) {
        isDialedCall = dialedCall;
    }


    private String convertTimeInString(long totalSecs) {
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public int getId() {
        return super.getId();
    }


    public String getCurrentTimer() {
        return currentTimer;
    }

    public AudioMedia getAudioMediaFor() {

        CallInfo ci;
        try {
            ci = getInfo();
        } catch (Exception e) {
            return null;
        }

        CallMediaInfoVector cmiv = ci.getMedia();

        for (int i = 0; i < cmiv.size(); i++) {
            CallMediaInfo cmi = cmiv.get(i);
            if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO) {
                // unfortunately, on Java too, the returned Media cannot be
                // downcasted to AudioMedia
                Media m = getMedia(i);
                if (m == null) {
                    continue;
                }
                return AudioMedia.typecastFromMedia(m);

            }
        }
        return null;
    }

    public void displayLog() {

        CallInfo ci;
        try {
            ci = getInfo();
        } catch (Exception e) {
            return;
        }

        CallMediaInfoVector cmiv = ci.getMedia();

        for (int i = 0; i < cmiv.size(); i++) {
            CallMediaInfo cmi = cmiv.get(i);
            if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO) {
                // unfortunately, on Java too, the returned Media cannot be
                // downcasted to AudioMedia

                try {
                    StreamInfo streamInfo = getStreamInfo(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;

            }
        }

    }

    @Override
    public synchronized void delete() {
        super.delete();
        if (timer != null)
            timer.cancel();
    }

    public long getSeconds() {
        return seconds;
    }

    @Override
    public String getCallName() {
        return callerName;
    }

    public int getCallId() {
        return callId;
    }

    public void setCallId() {
        this.callId = getId();
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setIncomingCall(boolean incomingCall) {
        isIncomingCall = incomingCall;
    }

    public void setCallConnected(boolean callConnected) {
        isCallConnected = callConnected;
    }

    public boolean isMissedCall() {
        return isIncomingCall && !isCallConnected;
    }

    public String getLinkedUUID() {
        return linkedUUID;
    }

    public void setLinkedUUID(String linkedUUID) {
        this.linkedUUID = linkedUUID;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setCallType(SipServiceConstants.CALLTYPE callType) {
        this.callType = callType;
    }

    public SipServiceConstants.CALLTYPE getCallType() {
        return callType;
    }

    public void notifyCallState() {
        try {
            CallInfo ci = getInfo();
            for (int i = 0; i < ci.getMedia().size(); i++) {
                if (ci.getMedia().get(i).getType() == PJMEDIA_TYPE_AUDIO &&
                        ci.getMedia().get(i).getStatus() == PJSUA_CALL_MEDIA_LOCAL_HOLD) {
                    setHoldCall(true);
                } else if (ci.getMedia().get(i).getType() == PJMEDIA_TYPE_AUDIO && ci.getMedia().get(i) != null) {
                    try {
                        setHoldCall(false);
                    } catch (Exception e) {
                        Log.e(TAG, "DK exception handler");
                    }
                } else if (ci.getMedia().get(i).getType() == PJMEDIA_TYPE_VIDEO &&
                        ci.getMedia().get(i).getStatus() == PJSUA_CALL_MEDIA_ACTIVE) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getTAG() {
        return TAG;
    }

    public boolean isCallOnMute() {
        return isCallOnMute;
    }

    public void setCallOnMute(boolean callOnMute) {
        isCallOnMute = callOnMute;
    }

    public boolean isCallOnSpeaker() {
        return isCallOnSpeaker;
    }

    public void setCallOnSpeaker(boolean callOnSpeaker) {
        isCallOnSpeaker = callOnSpeaker;
    }

    public void stopSendingKeyFrame() {
        phoneSipService.getmHandler().removeCallbacks(sendKeyFrameRunnable);
    }

    public void startSendingKeyFrame() {
        enqueueDelayedJob(sendKeyFrameRunnable, SipServiceConstants.DELAYED_JOB_DEFAULT_DELAY);
    }

    protected void enqueueDelayedJob(Runnable job, long delayMillis) {
        phoneSipService.getmHandler().postDelayed(job, delayMillis);
    }

    private Runnable sendKeyFrameRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                vidSetStream(pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_SEND_KEYFRAME, new CallVidSetStreamParam());
                startSendingKeyFrame();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    public String getPjsipCallID() {
        return pjsipCallID;
    }

    public void setPjsipCallID(String pjsipCallID) {
        this.pjsipCallID = pjsipCallID;
    }

}
