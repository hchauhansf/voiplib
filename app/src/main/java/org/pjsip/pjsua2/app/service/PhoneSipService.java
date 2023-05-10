package org.pjsip.pjsua2.app.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AccountInfo;
import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.CallVidSetStreamParam;
import org.pjsip.pjsua2.Extra.SharedPreferenceData;
import org.pjsip.pjsua2.OnCallMediaEventParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.SipEvent;
import org.pjsip.pjsua2.SipHeader;
import org.pjsip.pjsua2.SipHeaderVector;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.app.MyAccount;
import org.pjsip.pjsua2.app.MyApp;
import org.pjsip.pjsua2.app.MyBuddy;
import org.pjsip.pjsua2.app.application.SipApplication;
import org.pjsip.pjsua2.app.call.CallEvents;
import org.pjsip.pjsua2.app.call.ConferenceCall;
import org.pjsip.pjsua2.app.call.IncomingCall;
import org.pjsip.pjsua2.app.call.MyCall;
import org.pjsip.pjsua2.app.call.SipCall;
import org.pjsip.pjsua2.app.helper.BroadcastEventEmitter;
import org.pjsip.pjsua2.app.helper.BusProvider;
import org.pjsip.pjsua2.app.helper.SipUtility;
import org.pjsip.pjsua2.app.interfaces.ICall;
import org.pjsip.pjsua2.app.interfaces.MyAppObserver;
import org.pjsip.pjsua2.app.interfaces.SipServiceConstants;
import org.pjsip.pjsua2.listeners.VoipCallback;
import org.pjsip.pjsua2.pjmedia_orient;
import org.pjsip.pjsua2.pjmedia_srtp_use;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua_call_vid_strm_op;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import io.reactivex.annotations.NonNull;

import static org.pjsip.pjsua2.app.call.SipCall.VOIP_CALL_STATE.INCOMING_CALL;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.HOLD_DELAY;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.PARAM_IS_VIDEO;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.PARAM_LINKED_UUID;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.PARAM_PHONE_NUMBER;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.SERVICE_FOREGROUND_NOTIFICATION_ID;

import static org.pjsip.pjsua2.pjsip_event_id_e.PJSIP_EVENT_RX_MSG;
import static org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_OK;
import static org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_RINGING;

/**
 * Created by rajan on 12/29/2019.
 */
public class PhoneSipService extends BackgroundService implements MyAppObserver {

    private static final String TAG = "Phone SipHelper";

    private static boolean isAccountRegistered = false;
    public MyApp app = null;
    public static MyAccount account = null;
    public AccountConfig accCfg = null;

    private Handler handler;
    private Context mContext;
    private VoipCallback mCallbacks;
    private final IBinder mBinder = new LocalBinder();
    static Stack<ICall> activeCalls = new Stack<>();
    static ICall activeIncomingCall;
    private AudioManager audioManager;

    boolean isServiceInForeground;
    boolean isUserLoggedIn = false;
    boolean isLogoutAndLoginUserFlow = false;
    private BroadcastReceiver mPowerKeyReceiver = null;
    private boolean isPendingCallToAccept = false;
    private boolean isPendingCallToHandleWithErrorCodes = false;
    private boolean isToHandleRejectIncomingCall = false;
    private String errorCodes;
    private pjsip_status_code currentStatusCode;

    final Object mutex = new Object();
    static boolean isCallInitiated;
    private String incomingCallUUID = "";
    private AudioFocusRequest audioFocusRequest;

    private boolean isSipRegistrationOngoing = false;
    private boolean isAppinForeground = true;
    private boolean isLoginInitiated = false;

    private BroadcastEventEmitter mBroadcastEmitter;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("navya15", "navya15 service onCreate");
        handler = new Handler();
        mContext = getApplicationContext();
        mBroadcastEmitter = new BroadcastEventEmitter(PhoneSipService.this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.e("navya", "navya inside null intent");
            loginWithCallCheck();
            return Service.START_STICKY;
        }

        Log.d("navya15", "navya15 service action" + intent.getAction() + this);

        if (!SipServiceConstants.INCOMING_CALL_ACTION.equalsIgnoreCase(intent.getAction())) {
            if (isNoActiveCallPresent() || SipServiceConstants.ACTION_HANG_UP_CALL.equals(intent.getAction()) ||
                    SipServiceConstants.ACTION_DECLINE_INCOMING_CALL.equals(intent.getAction())) {
                startForeground(ServiceCommands.createForegroundServiceNotification(SipApplication.getNotificationBody(this),
                        false, this));

                stopCallForegroundService();
            }

        }


        switch (intent.getAction()) {
            case SipServiceConstants.END_SERVICE_ACTION:
                endAllCalls();
                break;
            case SipServiceConstants.INCOMING_CALL_ACTION:
                startIncomingCall(intent);
                break;
            case SipServiceConstants.INCOMING_CALL_DISCONNECTED:
                disconnectIncomingCall(intent);
                break;
            case SipServiceConstants.PAUSE_CALL_ACTION:
                holdAllCalls();
                break;
            case SipServiceConstants.RESUME_CALL_ACTION:
                resumeCalls();
                break;
            case SipServiceConstants.OUTGOING_CALL_ACTION:
                handleOutgoingCall(intent);
                break;
            case SipServiceConstants.LOGOUT_ACTION:
                logoutWithCallCheck();
                break;
            case SipServiceConstants.BACKGROUND:
                backGroundHandling();
                break;
            case SipServiceConstants.FOREGROUND:
                foregroundHandling();
                resumeCalls();
                break;
            case SipServiceConstants.ACTION_SET_MUTE:
                disconnectCallFromLocalMedia(intent.getBooleanExtra(SipServiceConstants.PARAM_MUTE, false));
                break;
            case SipServiceConstants.ACTION_BLUETOOTH_STATE:
                handleBluetooth(intent.getBooleanExtra(SipServiceConstants.PARAM_BLUETOOTH, false));
                break;
            case SipServiceConstants.ACTION_SET_HOLD:
                handleHoldCall(intent.getBooleanExtra(SipServiceConstants.PARAM_HOLD_ALL_CALL, false));
                break;
            case SipServiceConstants.ACTION_SET_UNHOLD:
                handleUnHoldCall(intent.getBooleanExtra(SipServiceConstants.PARAM_UNHOLD_ALL_CALL, false));
                break;
            case SipServiceConstants.ACTION_ACCEPT_INCOMING_CALL:
                acceptCall();
                break;
            case SipServiceConstants.ACTION_DECLINE_INCOMING_CALL:
                handleRejectIncomingCall();
                break;
            case SipServiceConstants.ACTION_HANG_UP_CALL:
                hangUpCall();
                break;
            case SipServiceConstants.LOGIN_ACTION:
                handleLogoutAndLogin(false);
                break;

            case SipServiceConstants.ACTION_SET_INCOMING_VIDEO:
                handleSetIncomingVideoFeed(intent);
                break;
            case SipServiceConstants.ACTION_SET_SELF_VIDEO_ORIENTATION:
                handleSetSelfVideoOrientation(intent);
                break;
            case SipServiceConstants.ACTION_SET_VIDEO_MUTE:
                handleSetVideoMute(intent);
                break;
            case SipServiceConstants.ACTION_START_VIDEO_PREVIEW:
                handleStartVideoPreview(intent);
                break;
            case SipServiceConstants.ACTION_STOP_VIDEO_PREVIEW:
                handleStopVideoPreview(intent);
                break;
            case SipServiceConstants.ACTION_SWITCH_VIDEO_CAPTURE_DEVICE:
                handleSwitchVideoCaptureDevice(intent);
                break;
            case SipServiceConstants.DTMF_ACTION:
                handleDTMFClick(intent);
                break;
            case SipServiceConstants.UNREGISTER_PUSH_LOGOUT:
                unregisterPushAnddLogout();
                break;
            case SipServiceConstants.ACTION_DECLINE_INCOMING_CALL_WITH_ERROR_CODE:
                handleRejectIncomingCallWhenUserisBusy(intent, true);
                break;
            default:
                loginWithCallCheck();
                break;
        }

        return Service.START_STICKY;
    }


    public void foregroundHandling() {
        Log.d("navya", "navya foreground handling account" + account);
        if (account != null) {
            Log.d("navya", "navya foreground handling account valid" + account.isValid() + account + "accList" + MyApp.accList.size());
        }
        if (!mWorkerThread.isAlive()) {
            Log.d("navya", "navya account is valid" + (account == null ? "account is null" : account.isValid()));
            startWorkerThread();
        }
        try {
            if (account != null) {
                Log.d("navya", "navya foreground handling accountid" + account.getId());
            }

            isAppinForeground = true;
            if (account == null) {
                Log.d("navya", "navya foreground login");
                handleLogoutAndLogin(false);
            } else if (account != null && !account.isValid()) {
                Log.e("navya", "navya foreground login for invalid account + account.isVAlid" + account.isValid());
                if (activeCalls == null || activeCalls.isEmpty()) {
                    //handleInactiveAccount();
                    account.setRegistration(true);
                }

            }

        } catch (Exception e) {
        }
    }

    public void backGroundHandling() {
        Log.d("navya", "navya backgroud handling");
        isAppinForeground = false;
        handler.postDelayed(() -> {
            if (!isAppinForeground) {
                Log.d("navya", "navya logout account" + account);
                logoutWithCallCheck();
            }
        }, 5000);
    }

    public void handleHoldCall(boolean isToHoldAllCalls) {
        if (activeCalls.isEmpty())
            return;
        if (isToHoldAllCalls) {
            List<ICall> calls = activeCalls;
            for (ICall call :
                    calls) {
                holdCall(call);
            }
        } else {
            holdCall(activeCalls.peek());
        }
    }

    public void handleUnHoldCall(boolean isToUnHoldAllCalls) {
        if (activeCalls == null || activeCalls.isEmpty())
            return;
        if (isToUnHoldAllCalls) {

        } else {
            ICall call = activeCalls.peek();
            if (call.isHoldCall()) {
                unHoldCall(call);
            }
        }
    }

    public void logoutWithCallCheck() {
        if (isNoActiveCallPresent()) {
            logoutUser();
        }
    }

    private void handleInactiveAccount() {
        Log.e("samarth15", "samarth15 handle inactive account isSipRegistrationOngoing" +
                isSipRegistrationOngoing + "isLoginInitiated" + isLoginInitiated);
        if (!isSipRegistrationOngoing && !isLoginInitiated) {
            logoutAndLoginUser();
        }
    }

    private synchronized void handleLogoutAndLogin(boolean handleLogout) {
        if(handleLogout){
            handleLogout();
        } else {
            if(mWorkerThread == null || !mWorkerThread.isAlive()) {
                startWorkerThread();
            }
            login();
        }

    }

    private synchronized void login() {
        Log.d("navya", "navya getRegIsActive" + "login is called");
        if (!isSipRegistrationOngoing && !isLoginInitiated) {
            Log.d("navya15", "navya15 calling login");
            String username = SipApplication.getSipUsername(this);
            String password = SipApplication.getSipPassword(this);
            String domainName = SipApplication.getSipPassword(this);
            log("Login started");
            if (!username.isEmpty() && !password.isEmpty()) {
                enqueueJob(() -> {
                    isLoginInitiated = true;
                    //foregroundWithNoCall();
                    initializeVoipAccount(username, password, domainName);
                });
            }
        }
    }

    public void initializeVoipAccount(String username, String password, String domainName) {
        enqueueJob(() -> {
            initializeVoipSync(username, password, domainName);
        });
    }

    private synchronized void initializeVoipSync(String username, String password, String domainName) {
        isSipRegistrationOngoing = true;
        String randomCallId = UUID.randomUUID().toString();
        Log.e("navya15", "navya15 inside initializeVoipSync");
        if (app == null || MyApp.ep == null) {
            app = new MyApp();
            // Wait for GDB to init, for native debugging only
            if (false &&
                    (mContext.getApplicationInfo().flags &
                            ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }

            app.init(this, mContext.getFilesDir().getAbsolutePath(), false
                    , SharedPreferenceData.isSecureProtocol(this), this);
            isUserLoggedIn = true;
        }

        if (app.accList.size() > 0) {
            if (account != null)
                account.delete();
        }

        accCfg = new AccountConfig();
        accCfg.setIdUri(SipUtility.getSipUserUri(username, this));
        accCfg.getRegConfig().setRegistrarUri(SipUtility.getDomainUri(this));
        log(SipUtility.getDomainUri(this));

        StringVector proxies = accCfg.getSipConfig().getProxies();
        proxies.clear();
        proxies.add(SipUtility.getDomainUri(this));
        if (MyApp.isToAddHeadersForPushNotification(this)) {
            Log.d("navya", "navya" + "sending app headers");
            accCfg.getRegConfig().setHeaders(MyApp.getHeadersForPush(this));
        }
        accCfg.getNatConfig().setContactRewriteUse(1);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        accCfg.getVideoConfig().setDefaultCaptureDevice(SipServiceConstants.FRONT_CAMERA_CAPTURE_DEVICE);
        accCfg.getVideoConfig().setDefaultRenderDevice(SipServiceConstants.DEFAULT_RENDER_DEVICE);

        AuthCredInfoVector creds = accCfg.getSipConfig().getAuthCreds();
        creds.clear();
        creds.add(new AuthCredInfo("Digest", "*", username, 0,
                password));
        accCfg.getSipConfig().setAuthCreds(creds);
        accCfg.getRegConfig().setCallID(randomCallId);
        Log.d("navya", "navya setting linkeduuid:" + randomCallId);


        log("initializeVoipAccount: befor adding account" + accCfg);
        if (SharedPreferenceData.isSecureProtocol(this))
            accCfg.getMediaConfig().setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_MANDATORY);
        Log.e("navya15", "navya15 initializeVoipSync  before login account:" + account);
        account = app.addAcc(accCfg);
        Log.e("navya15", "navya15 account after login account:" + account);
        isSipRegistrationOngoing = false;
        isLoginInitiated = false;
    }

    public void disconnectIncomingCall(Intent intent) {
        String linkedUUID = intent.getStringExtra(PARAM_LINKED_UUID);
        if (activeIncomingCall != null && activeIncomingCall.getLinkedUUID().equalsIgnoreCase(linkedUUID) &&
                activeIncomingCall.getState().equals(SipCall.VOIP_CALL_STATE.INCOMING_CALL)) {
            // disconnect call if active
            stopCallForegroundService();
            String number = intent.getStringExtra(SipServiceConstants.INCOMING_NUMBER);
            IncomingCall incomingCallObject = createIncomingCallObject(intent);
            incomingCallObject.setCallType(SipServiceConstants.CALLTYPE.MISSED);
            handleMissedCall(incomingCallObject, number);

            isPendingCallToAccept = false;
            isPendingCallToHandleWithErrorCodes= false;
            isToHandleRejectIncomingCall = false;
            errorCodes=null;
            setIncomingCallToNull();
            Log.d("navya15", "laxman" + "disconnectIncomingCall(Intent intent)");
            mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.DISCONNECTED, true));
            audioManager.setSpeakerphoneOn(false);
            ServiceCommands.createForegroundServiceNotification("PhoneSip Service", false, mContext);


            startForeground(ServiceCommands.createForegroundServiceNotification(SipApplication.getNotificationBody(this),
                    false, this));

            stopCallForegroundService();


            resumeMusicPlayer();
        }
    }



    public void disconnectCallFromLocalMedia(boolean forMute) {
        enqueueJob(() -> {
            if (activeCalls.isEmpty())
                return;
            ICall currentCall = activeCalls.peek();
            List<SipCall> calls = new ArrayList<>();
            if (currentCall instanceof SipCall) {
                ((SipCall) currentCall).setCallOnMute(forMute);
                calls.add((SipCall) currentCall);
            } else {
                ConferenceCall confCall = (ConferenceCall) currentCall;
                ((ConferenceCall) currentCall).setCallOnMute(forMute);
                calls.addAll(confCall.getSipCalls());
            }
            for (SipCall sipCall :
                    calls) {
                int medIdx = -1;
                CallInfo ci = null;
                try {

                    AudioMedia audMed = sipCall.getAudioMediaFor();
                    if (audMed != null) {
                        float muteInt = forMute ? 0.001f : 1;
                        unMuteOnCleanUp(muteInt);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleBluetooth(boolean isON) {
    }


    private void unMuteOnCleanUp(float muteInt) {
        try {
            AudDevManager mgr = MyApp.ep.audDevManager();
            mgr.getCaptureDevMedia().adjustTxLevel(muteInt);
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d("navya15", "navya15 inside stop service");
        logoutUser();
        return super.stopService(name);
    }

    @Override
    public void notifyBuddyState(MyBuddy buddy) {
    }

    @Override
    public void notifyChangeNetwork() {
    }


    public class LocalBinder extends Binder {
        public PhoneSipService getService() {
            return PhoneSipService.this;
        }
    }

    private void startForeground(Notification notification) {
        startForeground(SERVICE_FOREGROUND_NOTIFICATION_ID, notification);
    }

    private void handleMissedCall(ICall call, String number) {
        long seconds = 0;
        if (call instanceof SipCall) {
            seconds = ((SipCall) call).getSeconds();
        }
        mBroadcastEmitter.handleMissedCall(call instanceof SipCall ? false : true,
                    number, call.getLinkedUUID(), call.getCallName(), call.getTime(),
                    seconds, call.getCallerCname(), call.getCallType());
    }

    @Override
    public void notifyRegState(OnRegStateParam prm) {
        isLoginInitiated = false;

        AccountInfo accountInfo = null;
        try {
            if (account != null) {
                accountInfo = account.getInfo();
                if (accountInfo != null && !accountInfo.getRegIsActive()) {
                    Log.d("navya15", "navya15 notifyregstate accountInfo.getRegIsActive()" + accountInfo.getRegIsActive());
                    handleLogoutAndLogin(true);
                    return;
                }
            }
            } catch(Exception e){
                e.printStackTrace();
            }

        if (prm.getExpiration() == 0) {
            stopSelf();
        }

        currentStatusCode = prm.getCode();
        if (mCallbacks != null) {
            mCallbacks.notifyRegState(prm);
        }
        Log.d("navya", "navya notifyRegState : " + prm.getCode());
        if (PJSIP_SC_OK == prm.getCode() && prm.getStatus() == 0) {
            Log.d("navya", "navya account is registered accont active" + (account == null ? "account is null" : account.isValid()));
            if (account == null || !account.isValid()) {
                isAccountRegistered = false;
            } else {
                isAccountRegistered = true;
            }
        }
        if (isPendingCallToHandleWithErrorCodes) {
            if (PJSIP_SC_OK == prm.getCode() && prm.getStatus() == 0) {
                if (activeIncomingCall != null &&  errorCodes != null) {
                    rejectIncomingCallUserBusy(errorCodes, false);
                }

            }
        }
        if(isToHandleRejectIncomingCall){
            if (PJSIP_SC_OK == prm.getCode()) {
                if (activeIncomingCall != null) {
                    rejectIncomingCall();
                }

            }
        }
        Log.e("navya15", "navya15 notifyRegState isPendingCallToaccept:" + isPendingCallToAccept +
                "activeIncomingCall :" + activeIncomingCall);
        if (isPendingCallToAccept) {
            if (PJSIP_SC_OK == prm.getCode() && prm.getStatus() == 0) {
                if (activeIncomingCall != null) {
                    initiateIncomingCall(activeIncomingCall, false);
                    log("registration completed and accepting call");
                }

            } else {
                Log.d("navya", "navya account is not registered");
                isAccountRegistered = false;
                activeIncomingCall = null;
                Log.d("navya15", "laxman" + "notifyRegState(OnRegStateParam prm)" + "   isPendingCallToAccept--> else");
                mBroadcastEmitter.callState(new CallEvents.ScreenUpdate
                        (SipServiceConstants.CallScreenState.DISCONNECTED, true));
                log("notifyregstate call is disconnected");
            }
        }
    }

    private void addIncomingCallToActiveCalls() {
    }

    @Override
    public void notifyCallMediaState(MyCall call) {
        SipCall sipCall = (SipCall) call;
        sipCall.notifyCallState();

        // todo handle incoming video for outgoing call
     /*   mBroadcastEmitter.callState(new CallEvents.
                ScreenUpdate(SipServiceConstants.CallScreenState.VIDEO_INITIATED, true));*/
        synchronized (mutex) {
            mutex.notify();
        }
    }

    public void startIncomingCall(Intent intent) {
        stopMusicPlayer();
        enqueueJob(() -> {
            try {
                if (account != null && MyApp.ep != null && account.isValid())
                    currentStatusCode = account.getInfo().getRegStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ICall call = createIncomingCallObject(intent);
            startForeground(ServiceCommands.createForegroundServiceNotification(SipApplication.getNotificationBody(this),
                    false, this));

            notifyIncomingCall(intent, call);

            if (account != null)
                Log.d("navya", "navya startIncomingCall account.isValid() " + account.isValid());
            if (account == null || MyApp.ep == null) {
                Log.d("navya15", "navya15 startIncomingCall logoutAndLoginUser currentStatusCode" + currentStatusCode);
                log("Login was required....");
                handleLogoutAndLogin(false);
            } else if (account != null && (!account.isValid() || !PJSIP_SC_OK.equals(currentStatusCode))) {
                isSipRegistrationOngoing = false;
               logoutAndLoginUser();
            }
        });
    }

    public void notifyIncomingCall(Intent intent, ICall call) {
        boolean isVideo = intent.getBooleanExtra(PARAM_IS_VIDEO, false);
        IncomingCall incomingCall = (IncomingCall) call;
        incomingCall.setVideoCall(isVideo);
        startForeground(createIncomingNotification(call, true));
        String callerName = incomingCall.getCallerName();
        if (callerName == null) {
            callerName = getCallName(call);
        }
        updateScreenForIncomingCall(incomingCall.getNumber(),
                incomingCall.getServer(), incomingCall.getSlot(),
                incomingCall.getLinkedUUID(), callerName, incomingCall.isVideoCall());
        // check in the loop if there is already an active call present
        if (!isActiveIncomingCallAlreadyPresent(incomingCall.getLinkedUUID())) {
            activeIncomingCall = incomingCall;
        }
    }

    public boolean isActiveIncomingCallAlreadyPresent(String linkedUUID) {
        if (linkedUUID != null && activeCalls != null && activeCalls.size() > 0) {
            ListIterator<ICall> itr = activeCalls.listIterator();
            while (itr.hasNext()) {
                ICall call = itr.next();
                if (call.getLinkedUUID() != null && call.getLinkedUUID().equals(linkedUUID)) {
                    return true;
                }

            }
        }
        return false;
    }

    private Notification createIncomingNotification(ICall call, boolean isCall) {
        return ServiceCommands.createForegroundServiceNotification(getCallName(call), isCall, this);

    }

    private String getCallName(ICall call) {
        String callName;
        Notification notification = null;
        if (call == null)
            return null;
        callName = call.getCallName();
        if (TextUtils.isEmpty(callName)) {
            callName = PhoneNumberUtils.formatNumber(call.getCallerCname(), "US");
        }
        if (callName == null)
            callName = "";
        return callName;
    }


    public void handleOutgoingCall(Intent intent) {
        String number = intent.getStringExtra(PARAM_PHONE_NUMBER);
        boolean isVideoCall = intent.getBooleanExtra(PARAM_IS_VIDEO, false);
        if (account == null) {
            log("calling but account is null; app will login first");
            handleLogoutAndLogin(false);//(username, password);
            delayThread(2000);
        }
        holdAllCalls();
        startCalling(number, isVideoCall);
    }

    public void loginWithCallCheck() {
        if (isNoActiveCallPresent()) {
            if (account == null) {
                handleLogoutAndLogin(false);
            }
            if (!mWorkerThread.isAlive()) {
                startWorkerThread();
            }
        }
    }

    public boolean isNoActiveCallPresent() {
        boolean isACtiveCall = !isCallInitiated && activeCalls.isEmpty() && activeIncomingCall == null;
        Log.d("navya", "navya getRegIsActive" +  isACtiveCall);
        return !isCallInitiated && activeCalls.isEmpty() && activeIncomingCall == null;
    }

    public void setCallbacks(VoipCallback mCallbacks) {
        this.mCallbacks = mCallbacks;
    }

    public Stack<ICall> getActiveCalls() {
        return activeCalls;
    }

    public ICall getIncomingCall() {
        return activeIncomingCall;
    }

    public void setIncomingCall(ICall incomingCall) {
        Log.d("navya", "navya set incoming call activeIncomingCall" + activeIncomingCall);
        this.activeIncomingCall = incomingCall;
    }

    @Override
    public void notifyIncomingCall(SipCall call, OnIncomingCallParam prm) {
        log("incoming call was there");
        enqueueJob(() -> {
            if (call == null)
                return;
            try {
                if (call.getInfo().getState() != pjsip_inv_state.PJSIP_INV_STATE_INCOMING) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            activeIncomingCall = call;
        });
    }

    @Override
    public void notifyIncomingCall(OnIncomingCallParam prm) {
        String msg = prm.getRdata().getWholeMsg();
        String name = SipUtility.getNameFromHeader(msg);
        String linkedUUID = SipUtility.getUUIDFromHeader(msg);
        Log.e("navya15", "navya15 inside native call name:" + name + "linkedUUID:" + linkedUUID);

        SipCall sipCall = new SipCall(account, prm.getCallId(), this);

        CallInfo callInfo = null;
        try {
            callInfo = sipCall.getInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sipCall == null)
            return;

        if (activeIncomingCall == null) {
            startForeground(ServiceCommands.createForegroundServiceNotification(SipApplication.getNotificationBody(this),
                    true, this));
        }

        sipCall.setPjsipCallID(callInfo.getCallIdString());
        sipCall.setLinked_uuid(linkedUUID);


        sipCall.setCallerName(name);
        sipCall.setCallerCname(name);
        // check for notification in case of push first


        activeCalls.push(sipCall);
        activeIncomingCall = sipCall;

        try {
            CallOpParam prm1 = new CallOpParam(true);
            prm1.setStatusCode(PJSIP_SC_RINGING);
            sipCall.answer(prm1);

        } catch (Exception e) {
            Log.e("navya", "navya exception e" + e.getLocalizedMessage());
        }


    }

    @NonNull
    private IncomingCall createIncomingCallObject(Intent intent) {
        // assign callername at this point to avoid multiple Utility calls
        String number = intent.getStringExtra(SipServiceConstants.INCOMING_NUMBER);
        String server = intent.getStringExtra(SipServiceConstants.INCOMING_SERVER);
        String slot = intent.getStringExtra(SipServiceConstants.INCOMING_SLOT);
        String linked_uid = intent.getStringExtra(PARAM_LINKED_UUID);

        String callerName = number;

        if (intent.hasExtra(SipServiceConstants.CALLER_CONTACT_NAME)) {
            callerName = intent.getStringExtra(SipServiceConstants.CALLER_CONTACT_NAME);
        }
        log("incoming call was there " + linked_uid);
        // create a call over here
        IncomingCall incomingCall = new IncomingCall();
        incomingCall.setLinkedUUID(linked_uid);
        incomingCall.setServer(server);
        incomingCall.setSlot(slot);
        incomingCall.setNumber(number);

        incomingCall.setCallerCname(callerName);
        incomingCall.setCallerName(callerName);
        incomingCall.setCallType(SipServiceConstants.CALLTYPE.INCOMING);
        incomingCall.setTime(SipUtility.timeInSeconds());
        incomingCall.setState(INCOMING_CALL);
        return incomingCall;
    }

    private void updateScreenForIncomingCall(String number,
                                             String server,
                                             String slot,
                                             String linked_uid,
                                             String callerName,
                                             boolean isVideoCall) {
        mBroadcastEmitter.handleIncomingCall(number, server, slot, linked_uid,
                isNoActiveCallPresent(), isVideoCall, callerName);
    }

    @Override
    public void onDestroy() {
        Log.d("navya15", "navya15 service onDestroy");
        setCallbacks(null);
        super.onDestroy();
    }

    @Nullable
    private Notification createNotification(ICall call, boolean isIncomingCallNotification) {
        String callName;
        Notification notification = null;
        if (call == null)
            return null;
        callName = call.getCallName();
        if (TextUtils.isEmpty(callName)) {
            callName = PhoneNumberUtils.formatNumber(call.getCallerCname(), "US");
        }
        if (callName == null)
            callName = "";
        if (isIncomingCallNotification) {
            notification = ServiceCommands.createForegroundServiceNotification(SipApplication.getNotificationBody(this),
                    true, this);
        } else {
            notification = ServiceCommands.createForegroundServiceNotification(SipApplication.getNotificationBody(this),
                    true, this);
        }
        return notification;

    }


    @Override
    public void notifyCallState(final MyCall call1, OnCallStateParam prm) {
        SipCall call = (SipCall) call1;
        log("notifyCallState: " + call.getLinkedUUID());
        Log.d("navya15", "laxman" + "notifyCallState(final MyCall call1, OnCallStateParam prm)" + "  " + call.getCallId() + "   " + prm.getE().getType().toString());
        try {
            updateCallStatus(call.getCallId(), prm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(call);
    }

    private void delayThread(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void delayThread() {
        delayThread(1000);
    }

    private void handleCallHangUp(SipCall call, ConferenceCall conferenceCall,
                                  ListIterator<ICall> itr, int i) {
        unregisterReceiver();
        saveRecentCall(call, null);

        if (!call.isConferenceCall()) {
            enqueueJob(call::delete);
            itr.remove();
        } else {
            if (conferenceCall != null) {
                enqueueJob(call::delete);
                conferenceCall.removeCall(call);
                BusProvider.getInstance().send(new CallEvents.UpdatedConferenceCall(conferenceCall));
                if (conferenceCall.getSipCalls().isEmpty() || conferenceCall.getSipCalls().size() == 1) {
                    itr.remove();
                    if (conferenceCall.getSipCalls().size() == 1) {
                        SipCall barrenCall = conferenceCall.getSipCalls().get(0);
                        activeCalls.add(i, barrenCall);
                        BusProvider.getInstance().send(new CallEvents.UpdatedConferenceCall(null));
                    }
                }
            }
        }
    }

    private synchronized void updateCallStatus(int callId, OnCallStateParam prm) throws Exception {
        Log.e("navya", "navya updateCallStatus");
        log("update call status is called");
        if (getIncomingCall() != null && getIncomingCall().getState() == INCOMING_CALL) {
            addIncomingCallToActiveCalls();
        }
        int oldCallCounts = getSize();
        int i = 0;

        SipServiceConstants.CallScreenState callScreenState = null;//SipServiceConstants.CallScreenState.ONGOING_CALL;
        boolean notInStack = true;

        ListIterator<ICall> itr = activeCalls.listIterator();

        while (itr.hasNext()) {
            ICall iCall = itr.next();
            if (iCall == null)
                continue;


            if (iCall.isCallIdPresent(callId)) {
                SipCall call;
                notInStack = false;
                ConferenceCall conferenceCall = null;
                if (iCall instanceof ConferenceCall) {
                    conferenceCall = (ConferenceCall) iCall;
                    call = conferenceCall.getCallWithId(callId);
                    if (call == null) {
                        continue;
                    }
                    call.setConferenceCall(true);
                } else {
                    call = (SipCall) iCall;
                }
                if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                    log("Call Disconnect event");
                    if (call != null) {
                        call.stopSendingKeyFrame();
                        call.stopVideoFeeds();

                        handleCallHangUp(call, conferenceCall, itr, i);
                    }
                    callScreenState = SipServiceConstants.CallScreenState.DISCONNECTED;
                    Log.d("navya15", "laxman" + "updateCallStatus(int callId, OnCallStateParam prm)" + "   call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED");


                } else if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                    Log.d("navya", "navya" + "call connected");


                    if (call.isHasVideo()) {
                        //call.startSendingKeyFrame();
                        // call.setVideoMute(false);
                        Log.d("navya", "navya" + "call connected sending broadcast for UI");
                        Log.d("navya15", "laxman" + "updateCallStatus(int callId, OnCallStateParam prm)" + "   call.isHasVideo()");
                        mBroadcastEmitter.callState(new CallEvents.
                                ScreenUpdate(SipServiceConstants.CallScreenState.VIDEO_INITIATED, true));
                    }

                    call.setState(SipCall.VOIP_CALL_STATE.CONNECTED);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    callScreenState = SipServiceConstants.CallScreenState.ONGOING_CALL;
                } else if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
                    log("Call Dialing");
                    call.setState(SipCall.VOIP_CALL_STATE.CALLING);
                    callScreenState = SipServiceConstants.CallScreenState.DIALING;//activeCalls.size()==1? CallScreenState.DIALING:ONGOING_CALL;
                } else if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
                    int code;
                    String msg;
                    SipEvent e = prm.getE();

                    if (e.getBody().getTsxState().getType() == PJSIP_EVENT_RX_MSG) {
                        msg = e.getBody().getTsxState().getSrc().getRdata().getWholeMsg();
                    } else {
                        msg = e.getBody().getTsxState().getSrc().getRdata().getWholeMsg();
                    }
                    String uuidFromHeader = SipUtility.getUUIDFromHeader(msg);
                    log("outgoing call started " + uuidFromHeader);
                    call.setLinkedUUID(uuidFromHeader);
                    call.setState(SipCall.VOIP_CALL_STATE.RINGING);
                    call.setTime(SipUtility.timeInSeconds());
                    call.setCallType(SipServiceConstants.CALLTYPE.OUTGOING);
                    if (TextUtils.isEmpty(incomingCallUUID)) {
                        Log.d("issuess", "Phonesipservice...RINGING");
                        callScreenState = SipServiceConstants.CallScreenState.RINGING;
                        if (call.getInfo().getLastStatusCode().swigValue() == 180) {
                            callScreenState = SipServiceConstants.CallScreenState.PLAY_RINGTONE;
                        }
                    } else {
                        callScreenState = SipServiceConstants.CallScreenState.ONGOING_CALL;
                        incomingCallUUID = "";
                    }

                } else if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_INCOMING) {
                    call.setState(INCOMING_CALL);
                } else if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_NULL) {
                    call.setState(SipCall.VOIP_CALL_STATE.DISCONNECTED);
                    Log.d("navya15", "laxman" + "updateCallStatus(int callId, OnCallStateParam prm)" + "   call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_NULL");
                    callScreenState = SipServiceConstants.CallScreenState.DISCONNECTED;
                    if (!call.isConferenceCall()) {
                        enqueueJob(call::delete);
                        itr.remove();
                    } else {
                        if (conferenceCall != null) {
                            enqueueJob(call::delete);
                            conferenceCall.removeCall(call);
                            BusProvider.getInstance().send(new CallEvents.UpdatedConferenceCall(conferenceCall));
                            if (conferenceCall.getSipCalls().isEmpty() || conferenceCall.getSipCalls().size() == 1) {
                                itr.remove();
                                if (conferenceCall.getSipCalls().size() == 1) {
                                    SipCall barrenCall = conferenceCall.getSipCalls().get(0);
                                    activeCalls.add(i, barrenCall);
                                }
                            }
                        }
                    }
                    if (getIncomingCall() != null && getIncomingCall().getState() == INCOMING_CALL) {
                        setIncomingCallToNull();
                    }
                    Runtime.getRuntime().gc();
                }
            }
            i++;
        }
        Runtime.getRuntime().gc();
        boolean forceUpdate = oldCallCounts != getSize();
        Log.d("issuess", "" + forceUpdate);
        if (callScreenState != null) {
            Log.d("navya15", "laxman" + "updateCallStatus(int callId, OnCallStateParam prm)" + "   callScreenState != null" + callScreenState.toString());
            mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(callScreenState, forceUpdate));
        }
        if (isNoActiveCallPresent()) {
            handler.postDelayed(this::cleanUpCall, 1000);

        }
    }

    private void cleanUpCall() {
        audioManager.setSpeakerphoneOn(false);
        unMuteOnCleanUp(1);
        resumeMusicPlayer();
        try {
            audioManager.setMode(AudioManager.MODE_NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBroadcastEmitter.handleCleanUpCall();
        stopCallForegroundService();
    }

    private void saveRecentCall(ICall call, String number) {
        long seconds = 0;
        if (call instanceof SipCall) {
            seconds = ((SipCall) call).getSeconds();
        }
        mBroadcastEmitter.handleSaveCallAfterHangup(call instanceof SipCall ? false : true,
                number, call.getLinkedUUID(), call.getCallName(), call.getTime(), seconds, call.getCallerCname(), call.getCallType());
    }

    private void setIncomingCallToNull() {
        Log.e("navya", "navya calling set incoming call to null" + activeIncomingCall);
        if (activeIncomingCall == null)
            return;
        setIncomingCall(null);//getIncomingCall() = null;
        BusProvider.getInstance().send(new CallEvents.IncomingCallUpdate());
    }

    public void logoutUser() {
        Log.d("navya15", "navya15 inside logoutuser");
        logoutUserSync();
       // stopSelf();
    }

    public synchronized void logoutAndLoginUser() {
        Log.d("navya15", "navya15 inside logoutAndLoginUser account" + account);

        Log.d("navya", "navya logout user sync account " + account);
        if (account != null) {
            Log.d("navya", "navya logout user sync account valid" + account.isValid());
            isUserLoggedIn = false;
            Log.d("navya15", "navya15 calling enque logout job app" + app);
            try {
                account.setRegistration(false);
                isLogoutAndLoginUserFlow = true;
            }catch (Exception e){

            }
        }
    }

    private void logoutUserSync() {
        Log.d("navya", "navya logout user sync account " + account);
        if (account != null) {
            Log.d("navya", "navya logout user sync account valid" + account.isValid());
            isUserLoggedIn = false;
            foregroundWithNoCall();
            enqueueJob(() -> {
                try {
                    account.setRegistration(false);
                }catch (Exception e){

                }
            });
        }
    }

    private void handleLogout() {
        if (app != null) {
            Log.d("navya", "navya logout deleting account");

           enqueueJob(() -> {
                app.delAcc(account);
            account = null;
            app.deinit();
            accCfg = null;

            MyApp.ep.delete();
            MyApp.ep = null;
            if(!isLogoutAndLoginUserFlow || !isLoginInitiated) {
                        mWorkerThread.quitSafely();
            }

            });
        }

        currentStatusCode = null;


        if(isLogoutAndLoginUserFlow) {
            if(mWorkerThread == null || !mWorkerThread.isAlive()) {
                startWorkerThread();
            }
            handleLogoutAndLogin(false);
        } else if(isNoActiveCallPresent()) {
            System.gc();
        }
        Log.d("navya", "navya getRegIsActive" + "handleLogout complete");
        isLogoutAndLoginUserFlow = false;
    }

    public synchronized void startCallForegroundService() {
        log("start foreground is also called");
        enqueueJob(() -> {
            ICall iCall = null;
            if (!activeCalls.isEmpty())
                iCall = activeCalls.peek();
            if (iCall == null && getIncomingCall() != null)
                iCall = getIncomingCall();
            Notification notification = null;
            if (iCall != null) {
                notification = createNotification(iCall, false);
            } else {
                notification = ServiceCommands.
                        createForegroundServiceNotification(SipApplication.getNotificationBody(this), false, this);
            }

            startForeground(notification);
        });
        isServiceInForeground = true;
    }


    public void endAllCalls() {
        if (activeIncomingCall != null) {
            rejectIncomingCall();
        }
        enqueueJob(() -> {
            for (ICall icall :
                    activeCalls) {
                endCall(icall);
            }
            stopCallForegroundService();
        });
    }

    private void unregisterReceiver() {
        int apiLevel = Build.VERSION.SDK_INT;

        if (apiLevel >= 7) {
            try {
                getApplicationContext().unregisterReceiver(mPowerKeyReceiver);
            } catch (IllegalArgumentException e) {
                mPowerKeyReceiver = null;
            }
        } else {
            getApplicationContext().unregisterReceiver(mPowerKeyReceiver);
            mPowerKeyReceiver = null;
        }
    }

    public synchronized void stopCallForegroundService() {
        if (!isNoActiveCallPresent())
            return;
        stopForeground(true);
        isServiceInForeground = false;
    }

    private void foregroundWithNoCall() {
        if (isNoActiveCallPresent()) {
            Log.e("navya15", "navya15 calling foreground with no call");
            stopForeground(true);
        }

    }

    private int getSize() {
        int size = 0;
        for (ICall icall :
                activeCalls) {
            if (icall instanceof ConferenceCall) {
                size += ((ConferenceCall) icall).getSipCalls().size();
            } else {
                size += 1;
            }
        }
        return size;
    }

    public void hangUpCall() {
        enqueueJob(() -> {
            ListIterator<ICall> itr = activeCalls.listIterator();


            ICall currentActiveCall;
            log("userTappedOnEndCall: " + activeCalls.size());
            if (activeCalls.empty()) {
                SipServiceConstants.CallScreenState callScreenState = SipServiceConstants.CallScreenState.DISCONNECTED;
                Log.d("navya15", "laxman" + "hangUpCall()" + "   (activeCalls.empty()");
                mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(callScreenState, true));
                return;
            }
            try {
                currentActiveCall = activeCalls.peek();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            saveRecentCall(currentActiveCall, null);
            if (currentActiveCall != null && currentActiveCall instanceof MyCall) {
                ((MyCall) currentActiveCall).stopVideoFeeds();
            }
            endCall(currentActiveCall);
            if (currentActiveCall instanceof SipCall) {
                if (!activeCalls.empty()) {
                    activeCalls.pop();
                }
                SipServiceConstants.CallScreenState callScreenState = SipServiceConstants.CallScreenState.DISCONNECTED;
                Log.d("navya15", "laxman" + "hangUpCall()" + "   currentActiveCall instanceof SipCall");
                mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(callScreenState, true));
                enqueueJob(((SipCall) currentActiveCall)::delete);
            }
            log("active calls after pop" + activeCalls.size());
           // Runtime.getRuntime().gc();
        });
    }

    public void endCall(ICall call) {
        List<SipCall> calls = new ArrayList<>();
        if (call instanceof ConferenceCall) {
            calls.addAll(((ConferenceCall) call).getSipCalls());
        } else {
            calls.add((SipCall) call);
        }
        for (SipCall sipCall :
                calls) {
            if (sipCall != null && sipCall.isActive()) {
                CallOpParam prm = new CallOpParam();
                prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
                try {
                    if (sipCall.isActive() && !sipCall.isDeleted()) {
                        sipCall.hangup(prm);
                        sipCall.setDeleted(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void unHoldCall(ICall iCall) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                unHoldCallSync(iCall);
            }
        };
        enqueueJob(runnable);
    }

    private void unHoldCallSync(ICall iCall) {
        if (iCall instanceof ConferenceCall) {
            ConferenceCall conferenceCall = (ConferenceCall) iCall;
            List<SipCall> calls = conferenceCall.getSipCalls();
            mergeCallSync(calls);
        } else {
            log("run: " + iCall.getCallName());
            SipCall call = (SipCall) iCall;
            if (call.isDeleted())
                return;
            CallOpParam prm = new CallOpParam(true);
            prm.getOpt().setFlag(1);
            prm.getOpt().setAudioCount(1);
            prm.getOpt().setVideoCount(1);
            try {
                call.reinvite(prm);
                call.setActive(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            log("run: was resumed? " + call.isActive());
        }
        Log.d("navya15", "laxman" + "unHoldCallSync(ICall iCall)" + "   else part");
        mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.ONGOING_CALL, true));
    }


    public synchronized void startCalling(String number, boolean isVideoCall) {
        if (account == null) {
            SipServiceConstants.CallScreenState callScreenState = SipServiceConstants.CallScreenState.DISCONNECTED;
            Log.d("navya15", "laxman" + "startCalling(String number, boolean isVideoCall)" + "   account == null");
            mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(callScreenState, true));
            return;
        }
        long time = System.currentTimeMillis();
        if (!mWorkerThread.isAlive()) {
            logoutUser();
            startWorkerThread();
            handleLogoutAndLogin(false);
            delayThread();
            startCalling(number, isVideoCall);
        } else
            enqueueJob(() -> {
                isCallInitiated = true;
                SipCall call = null;
                call = new SipCall(account, -1, this);
                startCallForegroundService();
                CallOpParam prm = new CallOpParam(true);
                prm.getOpt().setAudioCount(1);
                if (isVideoCall) {
                    prm.getOpt().setVideoCount(1);
                } else {
                    prm.getOpt().setVideoCount(0);
                }
                try {
                    call.setHasVideo(true);
                    Thread.sleep(2000);
                    call.makeCall(SipUtility.getSipUserUri(number, this), prm);
                    call.setVideoMute(false);
                    call.setActive(true);
                    call.setCallId();
                    call.setCallerCname(number);
                    call.setCallType(SipServiceConstants.CALLTYPE.OUTGOING);
                    activeCalls.push(call);
                    stopMusicPlayer();
                    log("startCalling: time is " + (System.currentTimeMillis() - time));
                    // Assign a name for easy access;
                    String callerName = number;//Utility.getCallerName(call);
                    call.setState(SipCall.VOIP_CALL_STATE.CALLING);
                    mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.DIALING, true));
                    Log.d("navya15", "laxman" + "startCalling(String number, boolean isVideoCall)" + "  try-> !mWorkerThread.isAlive()->else");
                } catch (Exception e) {
                    stopCallForegroundService();
                    call.setDeleted(true);
                    Toast.makeText(getApplicationContext(), "Unable to connect call. Please check your network connection and try again.", Toast.LENGTH_LONG).show();
                    stopForeground(true);
                    Log.d("navya15", "laxman" + "startCalling(String number, boolean isVideoCall)" + "  catch-> !mWorkerThread.isAlive()->else");
                    mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.DISCONNECTED, true));
                    stopSelf();
                }
                isCallInitiated = false;
            });
    }

    public synchronized void initiateIncomingCall(ICall incomingCall, boolean value) {
        if (activeIncomingCall instanceof SipCall) {
            acceptRegularCall((SipCall) activeIncomingCall, true);
        } else {
            IncomingCall incomingcallObj = (IncomingCall) incomingCall;
            if (incomingCall == null) {
                Log.e("navya", "navya incoimg call is null" + this);

            }
            String number = incomingcallObj.getCallerCname();
            String server = incomingcallObj.getServer();
            String slot = incomingcallObj.getSlot();
            String linked_uid = incomingcallObj.getLinkedUUID();
            String msg = "incoming call initiated";
            log(msg);
            long time = System.currentTimeMillis();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pjsip_status_code code = currentStatusCode;
            if (account != null) {
                try {
                    Log.e("navya15", "navya15 account valid" + account.isValid() + account);
                    if (account.isValid()) {
                        code = account.getInfo().getRegStatus();
                        Log.d("navya", "navya account valid" + account.isValid() + account + "code" + code + this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //logoutAndLoginUser();
                    Log.d("navya15", "laxman" + "initiateIncomingCall(ICall incomingCall)" + "  account != null--> catch()");
                    mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.DISCONNECTED, true));
                    Log.e("navya", "navya crash exception" + e.getCause() + e.getLocalizedMessage());
                    handleInactiveAccount();
                    isPendingCallToAccept = true;
                    activeIncomingCall = incomingCall;
                    Log.d("navya15", "laxman" + "initiateIncomingCall(ICall incomingCall)" + "  account != null--> catch()-- Connecting");
                    mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.CONNECTING, true));
                    return;
                }
            }
            if (code == null) {
                Log.d("navya15", "navya15");
            }
            if (code != null && code.equals(pjsip_status_code.PJSIP_SC_TRYING)) {
                Log.d("navya", "navya PJSIP_SC_TRYING");
                handleInactiveAccount();
                isPendingCallToAccept = true;
                activeIncomingCall = incomingCall;
                mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.CONNECTING, true));
                return;
            }
            Log.d("navya", "navya" + "initiateIncomingCall account :" + account + "isAccountRegistered" + isAccountRegistered + "is valid");
            if(account == null) {
                handleLogoutAndLogin(false);
                isPendingCallToAccept = true;
                activeIncomingCall = incomingCall;
                mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.CONNECTING, true));
                return;
            }

            if (!account.isValid() || value) {
                Log.d("navya", "navya navya navya navya navay");
                handleInactiveAccount();
                isPendingCallToAccept = true;
                activeIncomingCall = incomingCall;
                Log.d("navya15", "laxman" + "initiateIncomingCall(ICall incomingCall)" + "  account == null || !isAccountRegistered || !account.isValid()");
                mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.CONNECTING, true));
                return;
            } else {
                isPendingCallToAccept = false;
            }

            enqueueJob(() -> {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
                SipCall call = null;
                call = new SipCall(account, -1, this);
                call.setHasVideo(true);

                CallOpParam prm = new CallOpParam(true);

                SipHeader hSlot = new SipHeader();
                hSlot.setHName("X-Slot");
                hSlot.setHValue(slot);

                SipHeader hServer = new SipHeader();
                hServer.setHName("X-Server");
                hServer.setHValue(server);


                SipHeaderVector headerVector = new SipHeaderVector();
                headerVector.add(hSlot);
                headerVector.add(hServer);
                prm.getTxOption().setHeaders(headerVector);

                try {
                    String tempNumber = number;
                    log(SipUtility.getSipUserUri(tempNumber, this));
                    prm.getOpt().setVideoCount(1);
                    Log.d("navya", "navya making call");
                    call.makeCall(SipUtility.getSipUserUri(tempNumber, this), prm);
                    activeCalls.push(call);
                    setIncomingCallToNull();
                    call.setActive(true);
                    call.setCallId();
                    call.setCallerCname(tempNumber);
                    call.setLinkedUUID(linked_uid);
                    if (SharedPreferenceData.isSecureProtocol(this)) {
                        accCfg.getMediaConfig().setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_MANDATORY);
                    }
                    log("initiate incoming call " + linked_uid);
                    // Assign a name for easy access;
                    String callerName = tempNumber;
                    call.setState(SipCall.VOIP_CALL_STATE.CONNECTED);
                    call.setCallType(SipServiceConstants.CALLTYPE.INCOMING);
                    incomingCallUUID = call.getLinkedUUID();
                    Log.d("navya15", "laxman" + "initiateIncomingCall(ICall incomingCall)" + "   enqueueJob(()");
                    mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.ONGOING_CALL, true));
                } catch (Exception e) {
                    call.setDeleted(true);
                    Log.d("navya15", "laxman" + "initiateIncomingCall(ICall incomingCall)" + "   enqueueJob(() --> catch" + e.getLocalizedMessage());
                    mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.ONGOING_CALL, true));
                }
            });
        }


    }

    private void acceptRegularCall(SipCall sipRegularCall, boolean isVideoCall) {
        try {
            CallOpParam prm = new CallOpParam(true);
            prm.getOpt().setAudioCount(1);
            if (isVideoCall) {
                prm.getOpt().setVideoCount(1);
            } else {
                prm.getOpt().setVideoCount(0);
            }
            prm.setStatusCode(PJSIP_SC_OK);
            sipRegularCall.answer(prm);
            setIncomingCallToNull();
            mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.ONGOING_CALL,
                    true));
        } catch (Exception e) {
            Log.e("navya", "navya exception e" + e.getLocalizedMessage());
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    public void mergeCall(List<SipCall> calls) {
        enqueueJob(() -> {
            mergeCallSync(calls);
        });
    }

    public void mergeCallSync(List<SipCall> calls) {
        List<AudioMedia> audioMedias = new ArrayList<>();
        for (SipCall sipCall :
                calls) {
            if (sipCall.isDeleted())
                continue;

            if (sipCall.isHoldCall()) {
                unHoldCallSync(sipCall);

                synchronized (mutex) {
                    try {
                        mutex.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            AudioMedia am = sipCall.getAudioMediaFor();
            if (am != null) {
                audioMedias.add(am);
            }
        }
        try {
            for (int i = 1; i < audioMedias.size(); i++) {
                AudioMedia audioMedia = audioMedias.get(i);
                for (int j = 0; j <= i - 1; j++) {
                    AudioMedia prevMedia = audioMedias.get(j);
                    prevMedia.startTransmit(audioMedia);
                    audioMedia.startTransmit(prevMedia);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void transferCall(SipCall call, String phoneNumber) {
        enqueueJob(() -> {
            String destinationNumber = SipUtility.getSipUserUri(phoneNumber, this);
            CallOpParam param = new CallOpParam();
            try {
                call.xfer(destinationNumber, param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void acceptCall() {
        ICall incomingCall = activeIncomingCall;
        Log.d("navya15", "navya15 acceptCall activeIncomingCall" + activeIncomingCall);
        if (incomingCall == null) {
            Log.d("navya15", "laxman" + "acceptCall()" + "   ");
            mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.DISCONNECTED, true));
        } else {
            initiateIncomingCall(incomingCall, false);
        }
    }

    public void holdCall(ICall call) {
        enqueueJob(() -> {
            List<SipCall> calls = new ArrayList<>();
            if (call instanceof ConferenceCall) {
                calls.addAll(((ConferenceCall) call).getSipCalls());
            } else {
                calls.add((SipCall) call);
            }
            for (SipCall sipCall :
                    calls) {
                if (sipCall.isDeleted())
                    continue;
                CallOpParam prm = new CallOpParam(true);

                try {
                    sipCall.setHold(prm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void handleRejectIncomingCall() {
        if (activeIncomingCall != null) {

            rejectCall(activeIncomingCall);
        }
    }

    public synchronized void rejectCall(ICall call) {
        enqueueJob(() -> {
            List<SipCall> calls = new ArrayList<>();
            if (call instanceof ConferenceCall) {
                calls.addAll(((ConferenceCall) call).getSipCalls());
            } else if (call instanceof IncomingCall) {
                rejectIncomingCall();
            } else {
                calls.add((SipCall) call);
            }
            for (SipCall sipCall :
                    calls) {
                if (sipCall != null && sipCall.isActive()) {
                    if (sipCall.isDeleted())
                        continue;
                    CallOpParam prm = new CallOpParam();
                    prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
                    try {
                        if (sipCall.isActive() && !sipCall.isDeleted()) {
                            sipCall.hangup(prm);
                            sipCall.setDeleted(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void rejectIncomingCall() {
        isPendingCallToAccept = false;
        IncomingCall incomingcallObj = (IncomingCall) activeIncomingCall;
        String number = incomingcallObj.getCallerCname();
        String server = incomingcallObj.getServer();
        String slot = incomingcallObj.getSlot();



       // number,  call.getLinkedUUID(),  call.getCallName(),  call.getTime(),   seconds,  call.getCallerCname(),  call.getCallType()


        CallOpParam prm = new CallOpParam(true);
        SipHeader hSlot = new SipHeader();
        hSlot.setHName("X-Slot");
        hSlot.setHValue(slot);

        SipHeader hServer = new SipHeader();
        hServer.setHName("X-Server");
        hServer.setHValue(server);

        SipHeader hDisconnect = new SipHeader();
        hDisconnect.setHName("X-Disconnect");
        hDisconnect.setHValue("true");

        SipHeaderVector headerVector = new SipHeaderVector();
        headerVector.add(hSlot);
        headerVector.add(hServer);
        headerVector.add(hDisconnect);
        prm.getTxOption().setHeaders(headerVector);

        try {
            if (account != null && MyApp.ep != null && account.isValid())
                currentStatusCode = account.getInfo().getRegStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pjsip_status_code code = currentStatusCode;
        if (account != null) {
            try {
                if (account.isValid()) {
                    code = account.getInfo().getRegStatus();
                }
            } catch (Exception e) {
                e.printStackTrace();
                handleInactiveAccount();
                isToHandleRejectIncomingCall = true;
                activeIncomingCall = incomingcallObj;
                return;
            }
        }
        if (code != null && code.equals(pjsip_status_code.PJSIP_SC_TRYING)) {
            Log.d("navya", "navya PJSIP_SC_TRYING");
            handleInactiveAccount();
            isToHandleRejectIncomingCall = true;
            activeIncomingCall = incomingcallObj;
            return;
        }

        if(account == null) {
            handleLogoutAndLogin(false);
            isToHandleRejectIncomingCall = true;
            activeIncomingCall = incomingcallObj;
            return;
        }

        if (!account.isValid()) {
            handleInactiveAccount();
            isToHandleRejectIncomingCall = true;
            activeIncomingCall = incomingcallObj;
            return;
        } else {
            isToHandleRejectIncomingCall = false;
        }


        SipCall call = null;
        call = new SipCall(account, -1, this);
        call.setHasVideo(true);


        try {
            call.makeCall(SipUtility.getSipUserUri(number, this), prm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setIncomingCallToNull();
        Log.d("navya15", "laxman" + "rejectIncomingCall()" + "   ");
        mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.DISCONNECTED, true));
        audioManager.setSpeakerphoneOn(false);
        mBroadcastEmitter.handleExitApplications();
    }

    private void handleRejectIncomingCallWhenUserisBusy(Intent intent, boolean value) {
        String errorCode = intent.getStringExtra(SipServiceConstants.
                PARAM_ERROR_CODE_WHILE_REJECTING_INCOMING_CALL);
        rejectIncomingCallUserBusy(errorCode, value);
    }

    private void rejectIncomingCallUserBusy(String errorCode, boolean value) {
        isPendingCallToAccept = false;
        if (activeIncomingCall == null) {
            Log.e(TAG, "rejectIncomingCallUserBusy requested without an active incoming call present");
            return;
        }
        IncomingCall incomingcallObj = (IncomingCall) activeIncomingCall;
        String number = incomingcallObj.getCallerCname();
        String server = incomingcallObj.getServer();
        String slot = incomingcallObj.getSlot();


        // number,  call.getLinkedUUID(),  call.getCallName(),  call.getTime(),   seconds,  call.getCallerCname(),  call.getCallType()


        CallOpParam prm = new CallOpParam(true);
        SipHeader hSlot = new SipHeader();
        hSlot.setHName("X-Slot");
        hSlot.setHValue(slot);

        SipHeader hServer = new SipHeader();
        hServer.setHName("X-Server");
        hServer.setHValue(server);

        SipHeader hDisconnect = new SipHeader();
        hDisconnect.setHName("X-Disconnect");
        hDisconnect.setHValue(errorCode);

        SipHeaderVector headerVector = new SipHeaderVector();
        headerVector.add(hSlot);
        headerVector.add(hServer);
        headerVector.add(hDisconnect);
        prm.getTxOption().setHeaders(headerVector);
        try {
            if (account != null && MyApp.ep != null && account.isValid())
                currentStatusCode = account.getInfo().getRegStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pjsip_status_code code = currentStatusCode;
        if (account != null) {
            try {
                if (account.isValid()) {
                    code = account.getInfo().getRegStatus();
                }
            } catch (Exception e) {
                e.printStackTrace();
                handleInactiveAccount();
                isPendingCallToHandleWithErrorCodes = true;
                errorCodes = errorCode;
                activeIncomingCall = incomingcallObj;
                return;
            }
        }
        if (code != null && code.equals(pjsip_status_code.PJSIP_SC_TRYING)) {
            Log.d("navya", "navya PJSIP_SC_TRYING");
            handleInactiveAccount();
            isPendingCallToHandleWithErrorCodes = true;
            errorCodes = errorCode;
            activeIncomingCall = incomingcallObj;
            return;
        }

        if (account == null) {
            handleLogoutAndLogin(false);
            isPendingCallToHandleWithErrorCodes = true;
            errorCodes = errorCode;
            activeIncomingCall = incomingcallObj;
            return;
        }

        if (!account.isValid() || value) {
            handleInactiveAccount();
            isPendingCallToHandleWithErrorCodes = true;
            errorCodes = errorCode;
            activeIncomingCall = incomingcallObj;
            return;
        } else {
            isPendingCallToHandleWithErrorCodes = false;
        }


        SipCall call = null;
        if (account != null) {
            try {
                call = new SipCall(account, -1, this);
                call.setHasVideo(true);
                call.makeCall(SipUtility.getSipUserUri(number, this), prm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        setIncomingCallToNull();
        Log.d("navya15", "laxman" + "rejectIncomingCall()" + "   ");
        mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.DISCONNECTED, true));
        audioManager.setSpeakerphoneOn(false);
        mBroadcastEmitter.handleExitApplications();
    }






    public void stopMusicPlayer() {
        if (Build.VERSION.SDK_INT >= 26) {
            AudioAttributes mPlaybackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int i) {

                        }
                    }, handler)
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    public void resumeMusicPlayer() {
        if (Build.VERSION.SDK_INT >= 26) {
            if (audioFocusRequest != null)
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(null);
        }
    }

    public void holdAllCalls() {
        if (activeCalls.isEmpty())
            return;
        ICall activeCall = activeCalls.peek();
        holdCall(activeCall);
        try {
            Thread.sleep(HOLD_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("navya15", "laxman" + "holdAllCalls()" + "   ");
        mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.ONGOING_CALL, true));
    }

    public void resumeCalls() {
        if (activeCalls.isEmpty())
            return;
        ICall activeCall = activeCalls.peek();
        unHoldCall(activeCall);
        handler.postDelayed(() -> {
            Log.d("navya15", "laxman" + "resumeCalls()" + "   ");
            mBroadcastEmitter.callState(new CallEvents.ScreenUpdate(SipServiceConstants.CallScreenState.ONGOING_CALL, true));
        }, 500);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        setIncomingCallToNull();
    }


    private void handleSetSelfVideoOrientation(Intent intent) {
        int orientation = intent.getIntExtra(SipServiceConstants.PARAM_ORIENTATION, -1);
        SipCall sipCall = (SipCall) activeCalls.peek();
        if (sipCall == null) {
            return;
        }

        setSelfVideoOrientation(sipCall, orientation);
    }

    void setSelfVideoOrientation(SipCall sipCall, int orientation) {
        try {
            pjmedia_orient pjmediaOrientation;

            switch (orientation) {
                case Surface.ROTATION_0:   // Portrait
                    pjmediaOrientation = pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG;
                    break;
                case Surface.ROTATION_90:  // Landscape, home button on the right
                    pjmediaOrientation = pjmedia_orient.PJMEDIA_ORIENT_NATURAL;
                    break;
                case Surface.ROTATION_180:
                    pjmediaOrientation = pjmedia_orient.PJMEDIA_ORIENT_ROTATE_90DEG;
                    break;
                case Surface.ROTATION_270: // Landscape, home button on the left
                    pjmediaOrientation = pjmedia_orient.PJMEDIA_ORIENT_ROTATE_180DEG;
                    break;
                default:
                    pjmediaOrientation = pjmedia_orient.PJMEDIA_ORIENT_UNKNOWN;
            }

            if (pjmediaOrientation != pjmedia_orient.PJMEDIA_ORIENT_UNKNOWN)
                // set orientation to the correct current device
                MyApp.getVidDevManager().setCaptureOrient(
                        sipCall.isFrontCamera()
                                ? SipServiceConstants.FRONT_CAMERA_CAPTURE_DEVICE
                                : SipServiceConstants.BACK_CAMERA_CAPTURE_DEVICE,
                        pjmediaOrientation, true);

        } catch (Exception iex) {

        }
    }

    private void handleSetVideoMute(Intent intent) {
        SipCall sipCall = (SipCall) activeCalls.peek();

        if (sipCall == null) {

            return;
        }
        boolean mute = intent.getBooleanExtra(SipServiceConstants.PARAM_VIDEO_MUTE, false);
        sipCall.setVideoMute(mute);
    }

    private void handleStartVideoPreview(Intent intent) {
        SipCall sipCall = (SipCall) activeCalls.peek();

        if (sipCall == null) {

            return;
        }
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Surface surface = intent.getExtras().getParcelable(SipServiceConstants.PARAM_SURFACE);
            sipCall.startPreviewVideoFeed(surface);
        }
    }

    private void handleStopVideoPreview(Intent intent) {
        SipCall sipCall = (SipCall) activeCalls.peek();

        if (sipCall == null) {
            return;
        }

        sipCall.stopPreviewVideoFeed();
    }

    // Switch Camera
    private void handleSwitchVideoCaptureDevice(Intent intent) {
        final SipCall sipCall = (SipCall) activeCalls.peek();
        if (sipCall == null) {
            return;
        }

        try {
            CallVidSetStreamParam callVidSetStreamParam = new CallVidSetStreamParam();
            callVidSetStreamParam.setCapDev(sipCall.isFrontCamera()
                    ? SipServiceConstants.BACK_CAMERA_CAPTURE_DEVICE
                    : SipServiceConstants.FRONT_CAMERA_CAPTURE_DEVICE);
            sipCall.setFrontCamera(!sipCall.isFrontCamera());
            sipCall.vidSetStream(pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_CHANGE_CAP_DEV, callVidSetStreamParam);
        } catch (Exception ex) {

        }
    }

    private void handleDTMFClick(Intent intent) {
        if (activeCalls != null) {
            final SipCall sipCall = (SipCall) activeCalls.peek();
            if (sipCall == null) {
                return;
            }
            String digit = intent.getStringExtra(SipServiceConstants.PARAM_DTMF_DIGIT);
            sendDtmfDigits(sipCall, digit);
        }
    }

    public void sendDtmfDigits(SipCall sipCall, String digit) {
        enqueueJob(() -> {
            try {
                sipCall.dialDtmf(digit);
                SipUtility.playSound(digit + ".wav", getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleSetIncomingVideoFeed(Intent intent) {
        if (activeCalls != null && !activeCalls.empty()) {
            MyCall myCall = (MyCall) activeCalls.peek();
            if (myCall == null) {
                return;
            }
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Surface surface = bundle.getParcelable(SipServiceConstants.PARAM_SURFACE);
                myCall.setIncomingVideoFeed(surface);
            }
        }
    }

    @Override
    public void onCallMediaEvent(OnCallMediaEventParam prm, VideoWindow mVideoWindow) {
        Log.d("samarth", "samarth inside onCallMediaEvent");
        try {
            mBroadcastEmitter.videoSize(
                    (int) mVideoWindow.getInfo().getSize().getW(),
                    (int) mVideoWindow.getInfo().getSize().getH());
            mBroadcastEmitter.sendCallMediaEvent(prm.getEv().getType().toString());
            Log.d("samarthmedia", prm.getEv().getType().toString());
        } catch (Exception ex) {

        }
    }

    private void unregisterPushAnddLogout() {
        if (isNoActiveCallPresent() && accCfg != null) {
            SipHeaderVector headerVector = accCfg.getRegConfig().getHeaders();
            headerVector.add(MyApp.getHeadersForUnregisterPush(this));
            accCfg.getRegConfig().
                    setHeaders(headerVector);
        }

        if (account != null) {
            try {
                account.modify(accCfg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // unregisterHeaders
        logoutUser();
        updateLogout();
        SharedPreferenceData.clearAllSharedPreferences(this);
        Log.d("navya", "navya unregisteration complete");
    }


    private void updateLogout() {
        mBroadcastEmitter.handleLogout(true);
    }
}
