package org.pjsip.pjsua2.app.interfaces;

import org.pjsip.pjsua2.app.call.SipCall;

import java.io.Serializable;

/**
 * Created by davinder on 15/6/17.
 */

public interface ICall extends Serializable{

    /**
     * Method for returning the CallerName, uses {@link #getCallerCname()}, it performs operation on
     * {@link #getCallerCname()} to return name based on formatting and custom logic in implementatio
     * class.
     *
     * @return the callerName
     */
    String getCallName();

    /**
     * Method for returning the CallerName
     * @see #getCallName()
     * @return the caller name
     */
    String getCallerCname();

    /**
     * Method for setting the caller name
     * @param callerCname
     */
    void setCallerCname(String callerCname);

    /**
     * Method for checking the hold unhold status of current call
     *
     * @return boolean indicating whether the current call is on hold
     * if true the call is on hold
     * else not
     */
    boolean isHoldCall();

    /**
     * Method for setting the hold/ unhold status if current call
     * @param isHoldCall boolean true-> call is on hold
     *                   false -> call in not on hold
     */
    void setHoldCall(boolean isHoldCall);

    /**
     * Method for checking whether the current call is same as other one by comparing their call id
     *
     * @param callId callID to match
     * @return boolean true if both the calls match else false
     */
    boolean isCallIdPresent(int callId );

    /**
     * Method to get the state of call
     * @see SipCall.VOIP_CALL_STATE
     *
     * @return the current state of call
     */
    SipCall.VOIP_CALL_STATE getState();

    /**
     * Method for setting the state of call
     * @see SipCall.VOIP_CALL_STATE
     *
     * @param connected indicates the state of call
     */
    void setState(SipCall.VOIP_CALL_STATE connected);

    /**
     * Method to set the active status of call
     * @param active if true call is active else not
     */
    void setActive(boolean active);

    /**
    @deprecated
     */
    String getImageUrl();

    /**
     *
     * @return
     */
    String getLinkedUUID();

    void setLinkedUUID(String linkedUUID);

    /**
     * Method for setting the time of call
     *
     * @param time long time of call
     */
    void setTime(long time);

    /**
     * Method for getting the time of call
     *
     * @return the time of call in long
     */
    long getTime();

    /**
     * Method to set the type of call
     *
     * @param callType  the type of call
     * @see SipServiceConstants.CALLTYPE
     */
    void setCallType(SipServiceConstants.CALLTYPE callType);

    /**
     * Method to retrieve the type of call
     *
     * @return the type of call
     * @see SipServiceConstants.CALLTYPE
     */
    SipServiceConstants.CALLTYPE getCallType();
}
