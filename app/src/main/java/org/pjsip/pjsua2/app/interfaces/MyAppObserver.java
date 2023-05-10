package org.pjsip.pjsua2.app.interfaces;

import org.pjsip.pjsua2.OnCallMediaEventParam;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.app.MyBuddy;
import org.pjsip.pjsua2.app.call.MyCall;
import org.pjsip.pjsua2.app.call.SipCall;
/**
 * Interface to separate UI & engine a bit better
 * Gives VOIP library information to the implementing class.
 * The events are  related to various stages of a call(like ringing, dialing, connected, hangup etc) and call media as well as
 * account(registered, unregistered, trying).
 *
 * @author rajantalwar
 * @since 12/9/2019
 * @version 1.0
 *
 */


public interface MyAppObserver
{
    /**
     * Method for notifying about the account registration status
     * @see org.pjsip.pjsua2.app.MyAccount#onRegState(OnRegStateParam)
     * @see org.pjsip.pjsua2.app.service.PhoneSipService#notifyRegState(OnRegStateParam)
     *
     * @param prm contains information about the registration status
     */
    abstract void notifyRegState(OnRegStateParam prm);


    abstract void notifyIncomingCall(SipCall call, OnIncomingCallParam prm);

    /**
     * Method for notifying about the current state of the call
     * @see org.pjsip.pjsua2.app.service.PhoneSipService#notifyCallState(MyCall, OnCallStateParam) 
     * @see MyCall#onCallState(OnCallStateParam) 
     * 
     * @param call active call object
     * @param prm call state parameters
     *            
     */
    abstract void notifyCallState(MyCall call, OnCallStateParam prm);

    /** Method for notifying aboout the current call media state like whether an audio or videoo call
     * @see MyCall#onCallMediaState(OnCallMediaStateParam) 
     *
     * @param call active call object
     */
    abstract void notifyCallMediaState(MyCall call);

    abstract void notifyBuddyState(MyBuddy buddy);

    /**
     * This method is called for notifying about
     * {@link org.pjsip.pjsua2.pjmedia_event_type call media events like}
     * @see MyCall#onCallMediaEvent(OnCallMediaEventParam) 
     * 
     * @param prm CallMediaEventParam
     * @param mVideoWindow VideoWindow object 
     */
    abstract void onCallMediaEvent(OnCallMediaEventParam prm,  VideoWindow mVideoWindow);

    abstract void notifyChangeNetwork();

    /**
     * Method called by VOIP sdk to update about an incoming call
     * @see org.pjsip.pjsua2.app.MyAccount#onIncomingCall(OnIncomingCallParam)
     * @see org.pjsip.pjsua2.app.service.PhoneSipService#notifyIncomingCall(OnIncomingCallParam)
     *
     * @param prm parameter containing information about the incoming call
     */
    abstract void notifyIncomingCall(OnIncomingCallParam prm);
}
