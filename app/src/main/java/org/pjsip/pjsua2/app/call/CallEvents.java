package org.pjsip.pjsua2.app.call;


import android.os.Parcel;
import android.os.Parcelable;

import org.pjsip.pjsua2.app.interfaces.ICall;
import org.pjsip.pjsua2.app.interfaces.SipServiceConstants;

/**
 * Call Event class is container for CallStates like
 * {@link SipServiceConstants.CallScreenState OngoingCallState},
 * @see SipServiceConstants.CallScreenState <br>
 * {@link IncomingCallUpdate IncomingCallState}
 * @see SipCall.VOIP_CALL_STATE <br>
 * {@link UpdatedConferenceCall ConferenceCalls}
 *
 * implements {@link Parcelable} so that it's instances can be written to
 * and restored from a {@link Parcel}
 *
 * @author rajantalwar
 * @version 1.0
 */
public class CallEvents {

    /**
     * ScreenUpdate class for OngoingCallScreen State
     */
    public static class ScreenUpdate implements Parcelable {
        public final boolean forceUpdate;
        public static SipServiceConstants.CallScreenState callScreenState;

        /**
         *
         * @param callScreenState current state of call for more info refer to
         * {@link SipServiceConstants.CallScreenState}
         * @param forceUpdate boolean @deprecated
         */
        public ScreenUpdate(SipServiceConstants.CallScreenState callScreenState, boolean forceUpdate) {
            this.callScreenState = callScreenState;
            this.forceUpdate = forceUpdate;
        }

        protected ScreenUpdate(Parcel in) {
            forceUpdate = in.readByte() != 0;
        }

        public static final Creator<ScreenUpdate> CREATOR = new Creator<ScreenUpdate>() {
            @Override
            public ScreenUpdate createFromParcel(Parcel in) {
                return new ScreenUpdate(in);
            }

            @Override
            public ScreenUpdate[] newArray(int size) {
                return new ScreenUpdate[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte((byte) (forceUpdate ? 1 : 0));
        }
    }

    /**
     * class containing incoming call state for more info refer to {@link SipCall.VOIP_CALL_STATE}
     */
    public static class IncomingCallUpdate {
        public SipCall.VOIP_CALL_STATE state;

        public IncomingCallUpdate() {
            this.state = state;
        }
    }

    public static class UpdatedConferenceCall {
        public ICall conferenceCall;

        public UpdatedConferenceCall(ICall conferenceCall) {
            this.conferenceCall = conferenceCall;
        }
    }
}
