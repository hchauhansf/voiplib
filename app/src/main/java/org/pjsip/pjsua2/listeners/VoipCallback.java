package org.pjsip.pjsua2.listeners;

import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnIncomingCallParam;

import org.pjsip.pjsua2.OnRegStateParam;

import org.pjsip.pjsua2.app.interfaces.MyAppObserver;
import org.pjsip.pjsua2.app.MyBuddy;
import org.pjsip.pjsua2.app.call.MyCall;
import org.pjsip.pjsua2.app.call.SipCall;

/**
 * Created by dpsingh on 9/30/15.
 */
public  abstract class VoipCallback implements MyAppObserver {


    @Override
    public void notifyRegState(OnRegStateParam prm) {

    }

    @Override
    public void notifyIncomingCall(SipCall call, OnIncomingCallParam prm) {

    }

    @Override
    public void notifyCallState(MyCall call,OnCallStateParam prm) {

    }

    @Override
    public void notifyCallMediaState(MyCall call) {

    }

    @Override
    public void notifyBuddyState(MyBuddy buddy) {

    }

    @Override
    public void notifyChangeNetwork() {

    }
}
