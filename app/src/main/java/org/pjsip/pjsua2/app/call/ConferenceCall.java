package org.pjsip.pjsua2.app.call;

import android.text.TextUtils;

import org.pjsip.pjsua2.app.interfaces.ICall;
import org.pjsip.pjsua2.app.interfaces.SipServiceConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by davinder on 15/6/17.
 */

public class ConferenceCall implements ICall,Serializable {
    List<SipCall> sipCalls = new ArrayList<>();
    boolean isHoldCall=false;

    private boolean isCallOnMute;
    private boolean isCallOnSpeaker;

    public ConferenceCall(List<SipCall> sipCalls) {
        this.sipCalls = sipCalls;
    }

    public ConferenceCall(){

    }

    public List<SipCall> getSipCalls() {
        return sipCalls;
    }

    @Override
    public String getCallName() {
        if(sipCalls.isEmpty())
            return "";
        StringBuilder builder = new StringBuilder();
        for (SipCall sipCall :
                sipCalls) {
            String callerName = sipCall.getCallerName();
            if(TextUtils.isEmpty(callerName)){
                callerName= sipCall.getCallerCname();
            }
            builder.append(callerName);
            if (sipCall!= sipCalls.get(sipCalls.size()-1)) {
                builder.append(" & ");
            }
        }
        return builder.toString();
    }

    @Override
    public String getCallerCname() {
        return "";
    }

    @Override
    public void setCallerCname(String callerCname) {

    }

    @Override
    public boolean isHoldCall() {
        for (SipCall sipCall :
                sipCalls) {
            if (!sipCall.isHoldCall()){
                return false;
            }
        }
        return true;
    }

    @Override
    public void setHoldCall(boolean isHoldCall) {

        for (SipCall sipCall :
                sipCalls) {
                sipCall.setHoldCall(isHoldCall);
        }
    }

    @Override
    public boolean isCallIdPresent(int callId) {
        for (SipCall sipCall :
                sipCalls) {
            if (sipCall.getCallId() == callId){
                return true;
            }
        }
        return false;
    }

    @Override
    public SipCall.VOIP_CALL_STATE getState() {
        return null;
    }

    @Override
    public void setState(SipCall.VOIP_CALL_STATE connected) {

    }

    @Override
    public void setActive(boolean active) {

    }

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public String getLinkedUUID() {
        return null;
    }

    @Override
    public void setLinkedUUID(String linkedUUID) {

    }

    @Override
    public void setTime(long time) {

    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public void setCallType(SipServiceConstants.CALLTYPE callType) {

    }

    public boolean isCallOnMute() {
        return false;
    }

    public void setCallOnMute(boolean callOnMute) {
       this.isCallOnMute = callOnMute;
    }

    public boolean isCallOnSpeaker() {
        return false;
    }

    public void setCallOnSpeaker(boolean callOnSpeaker) {
        this.isCallOnSpeaker = callOnSpeaker;
    }

    @Override
    public SipServiceConstants.CALLTYPE getCallType() {
        return null;
    }

    public SipCall getCallWithId(int callId) {
        for (SipCall sipCall :
                sipCalls) {
            if (sipCall.getCallId() == callId){
                return sipCall;
            }
        }
        return null;
    }

    public void removeCall(SipCall call) {
        sipCalls.remove(call);
    }

    public List<SipCall> removeCall(int callId) {
        List<SipCall> removedCalls= new ArrayList<>();
        Iterator<SipCall> iterator = sipCalls.iterator();
        while (iterator.hasNext()){
            SipCall sipCall = iterator.next();
            if(sipCall.isCallIdPresent(callId)){
                removedCalls.add(sipCall);
                iterator.remove();
                break;
            }
        }
        if(sipCalls.size()==1){
            removedCalls.add(sipCalls.remove(0));
        }
        return removedCalls;
    }
}
