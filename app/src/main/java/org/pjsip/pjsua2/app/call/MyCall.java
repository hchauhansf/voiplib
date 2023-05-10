package org.pjsip.pjsua2.app.call;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.CallVidSetStreamParam;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaEventParam;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoPreviewOpParam;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowHandle;
import org.pjsip.pjsua2.app.MyAccount;
import org.pjsip.pjsua2.app.MyApp;
import org.pjsip.pjsua2.pjmedia_event_type;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_media_status;
import org.pjsip.pjsua2.pjsua_call_vid_strm_op;

import java.io.Serializable;

/**
 * MyCall represents a normal SIP call, this class is parent class of {@link SipCall}
 * Implements callbacks for CallStateObserver {@link #onCallMediaState(OnCallMediaStateParam)},
 * CallMediaState {@link #onCallMediaState(OnCallMediaStateParam)},
 * CallMediaEvents {@link #onCallMediaEvent(OnCallMediaEventParam)}
 *
 * @author rajantalwar
 * @version 1.0
 */
public class MyCall extends Call implements Serializable {
    public VideoWindow mVideoWindow;
    public VideoPreview mVideoPreview;
    private boolean localVideoMute = false;
    private boolean frontCamera = true;
    private String linked_uuid;

    /**
     * Constructor for MyCall class
     * @param acc sip accountobject
     * @param call_id callId which is a unique identifier for sip calll
     */
    public MyCall(MyAccount acc, int call_id) {
        super(acc, call_id);
        mVideoWindow = null;
    }

    /**
     * This method is called by SDK when there are changes to the call state
     * @see org.pjsip.pjsua2.app.service.PhoneSipService#notifyCallState(MyCall, OnCallStateParam)
     *
     * @param prm call state param
     */
    public synchronized void onCallState(OnCallStateParam prm) {
        MyApp.observer.notifyCallState(this, prm);
    }

    /**
     * This method is listener for call media state
     * it helps in identifying when the call has been put on hold/unhold at the other end
     * @see SipCall#notifyCallState
     *
     * @param prm CallMediaEventParam
     */
    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        CallInfo ci;
        try {
            ci = getInfo();
        } catch (Exception e) {
            return;
        }

        CallMediaInfoVector cmiv = ci.getMedia();

        for (int i = 0; i < cmiv.size(); i++) {
            CallMediaInfo cmi = cmiv.get(i);
            if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                    (cmi.getStatus() ==
                            pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                            cmi.getStatus() ==
                                    pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)) {
                // unfortunately, on Java too, the returned Media cannot be
                // downcasted to AudioMedia
                Media m = getMedia(i);
                AudioMedia am = AudioMedia.typecastFromMedia(m);

                // connect ports
                try {
                    MyApp.ep.audDevManager().getCaptureDevMedia().
                            startTransmit(am);
                    am.startTransmit(MyApp.ep.audDevManager().
                            getPlaybackDevMedia());
                } catch (Exception e) {
                    continue;
                }
            } else if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                    cmi.getStatus() ==
                            pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
                    cmi.getVideoIncomingWindowId() != pjsua2.INVALID_ID) {
                if (mVideoWindow != null) {
                    mVideoWindow.delete();
                }
                if (mVideoPreview != null) {
                    mVideoPreview.delete();
                }
                mVideoWindow = new VideoWindow(cmi.getVideoIncomingWindowId());
                mVideoPreview = new VideoPreview(cmi.getVideoCapDev());

                // send a broadcast to the activity
                MyApp.observer.notifyCallMediaState(this);

            }
        }

        MyApp.observer.notifyCallMediaState(this);
    }

    /**
     * This method is used for listenting to different type of mediaEvents.
     * It notifies the listeners about the received events by calling {@link MyApp#observer}
     *
     * @param prm  containing infomation about mediaEvent
     * {@link org.pjsip.pjsua2.pjmedia_event_type}
     */
    @Override
    public void onCallMediaEvent(OnCallMediaEventParam prm) {
       // MyApp.observer.notifyCallMediaState(prm);
        Log.d("samarthmediamycall",prm.getEv().getType().toString());
        if (prm.getEv().getType() == pjmedia_event_type.PJMEDIA_EVENT_FMT_CHANGED) {
            // Sending new video size
            MyApp.observer.onCallMediaEvent(prm, mVideoWindow);

        }
        super.onCallMediaEvent(prm);
    }

    /**
     *
     * @param surface dedicated surface embedded inside view hierarchy for rendering the
     *                incoming video feed
     */
    public void setIncomingVideoFeed(Surface surface) {
        if (mVideoWindow != null) {
            VideoWindowHandle videoWindowHandle = new VideoWindowHandle();
            videoWindowHandle.getHandle().setWindow(surface);
            try {
                mVideoWindow.setWindow(videoWindowHandle);
                setVideoMute(false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void stopIncomingVideoFeed() {
        VideoWindow videoWindow = getVideoWindow();
        if (videoWindow != null) {
            try {
                videoWindow.delete();
            } catch (Exception ex) {
            }
        }
    }

    /**
     *
     * @param surface  dedicated surface embedded inside view hierarchy for rendering the
     *                incoming video feed
     */
    public void startPreviewVideoFeed(Surface surface) {
        if (mVideoPreview != null) {
            VideoWindowHandle videoWindowHandle = new VideoWindowHandle();
            videoWindowHandle.getHandle().setWindow(surface);
            VideoPreviewOpParam videoPreviewOpParam = new VideoPreviewOpParam();
            videoPreviewOpParam.setWindow(videoWindowHandle);
            try {
                mVideoPreview.start(videoPreviewOpParam);
            } catch (Exception ex) {

            }
        }
    }


    public VideoWindow getVideoWindow() {
        return mVideoWindow;
    }

    public void setVideoWindow(VideoWindow mVideoWindow) {
        this.mVideoWindow = mVideoWindow;
    }

    public VideoPreview getVideoPreview() {
        return mVideoPreview;
    }

    public void setVideoPreview(VideoPreview mVideoPreview) {
        this.mVideoPreview = mVideoPreview;
    }


    public void stopVideoFeeds() {
        stopIncomingVideoFeed();
        stopPreviewVideoFeed();
    }

    public void stopPreviewVideoFeed() {
        VideoPreview videoPreview = getVideoPreview();
        if (videoPreview != null) {
            try {
                videoPreview.stop();
            } catch (Exception ex) {

            }
        }
    }

    public void setVideoMute(boolean videoMute) {
        try {
            vidSetStream(videoMute
                            ? pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_STOP_TRANSMIT
                            : pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_START_TRANSMIT,
                    new CallVidSetStreamParam());
            localVideoMute = videoMute;
        } catch (Exception ex) {

        }
    }

    public void setFrontCamera(boolean frontCamera) {
        this.frontCamera = frontCamera;
    }

    public boolean isFrontCamera() {
        return frontCamera;
    }


    public void startSendingVidStream() {

    }

    /**
     * Method to get linkedUUID
     * @return the linkeduuid  which is unique identifier for the call
     */
    public String getLinked_uuid() {
        return linked_uuid;
    }

    /**
     * Method for setting the linkedUUID
     *
     * @param linked_uuid linkeduuid  which is unique identifier for the call
     */
    public void setLinked_uuid(String linked_uuid) {
        this.linked_uuid = linked_uuid;
    }


}