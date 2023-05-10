/* $Id: MyApp.java 5361 2016-06-28 14:32:08Z nanang $ */
/*
 * Copyright (C) 2013 Teluu Inc. (http://www.teluu.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pjsip.pjsua2.app;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.pjsip.pjsua2.*;
import org.pjsip.pjsua2.app.application.SipApplication;
import org.pjsip.pjsua2.app.interfaces.MyAppObserver;
import org.pjsip.pjsua2.app.interfaces.SipServiceConstants;

import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.H264_CODEC_ID;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.H264_DEF_HEIGHT;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.H264_DEF_WIDTH;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.PROFILE_LEVEL_ID_HEADER;
import static org.pjsip.pjsua2.app.interfaces.SipServiceConstants.PROFILE_LEVEL_ID_JANUS_BRIDGE;


/* Interface to separate UI & engine a bit better */


class MyLogWriter extends LogWriter {
    @Override
    public void write(LogEntry entry) {
        System.out.println(entry.getMsg());
    }
}


class MyAccountConfig {
    public AccountConfig accCfg = new AccountConfig();
    public ArrayList<BuddyConfig> buddyCfgs = new ArrayList<BuddyConfig>();

    public void readObject(ContainerNode node) {
        try {
            ContainerNode acc_node = node.readContainer("Account");
            accCfg.readObject(acc_node);
            ContainerNode buddies_node = acc_node.readArray("buddies");
            buddyCfgs.clear();
            while (buddies_node.hasUnread()) {
                BuddyConfig bud_cfg = new BuddyConfig();
                bud_cfg.readObject(buddies_node);
                buddyCfgs.add(bud_cfg);
            }
        } catch (Exception e) {
        }
    }

    public void writeObject(ContainerNode node) {
        try {
            ContainerNode acc_node = node.writeNewContainer("Account");
            accCfg.writeObject(acc_node);
            ContainerNode buddies_node = acc_node.writeNewArray("buddies");
            for (int j = 0; j < buddyCfgs.size(); j++) {
                buddyCfgs.get(j).writeObject(buddies_node);
            }
        } catch (Exception e) {
        }
    }
}


public class MyApp {
    static {
        try {
            System.loadLibrary("openh264");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("UnsatisfiedLinkError: " + e.getMessage());
            System.out.println("This could be safely ignored if you " +
                    "don't need video.");
        }

        try {
            System.loadLibrary("pjsua2");
        } catch (UnsatisfiedLinkError error) {
            //throw new RuntimeException(error);
        }
    }

    public static int PRIORITY_MAX = 254;
    public static int PRIORITY_MIN = 1;
    public static int PRIORITY_DISABLED = 0;
    public static Endpoint ep = new Endpoint();
    public static MyAppObserver observer;
    public static ArrayList<MyAccount> accList = new ArrayList<MyAccount>();

    private ArrayList<MyAccountConfig> accCfgs =
            new ArrayList<MyAccountConfig>();
    private EpConfig epConfig = new EpConfig();
    private TransportConfig sipTpConfig = new TransportConfig();
    private String appDir;

    /* Maintain reference to log writer to avoid premature cleanup by GC */
    private MyLogWriter logWriter;

    private final String configName = "pjsua2.json";
    private final int SIP_PORT = 6060;
    private final int LOG_LEVEL = 5;

    public void init(MyAppObserver obs, String app_dir, Context context) {
        init(obs, app_dir, false, false, context);
    }

    public void init(MyAppObserver obs, String app_dir,
                     boolean own_worker_thread, boolean isTLS, Context context) {
        Log.e("navya15", "Directory   -  ");
        observer = obs;
        appDir = app_dir;

        /* Create endpoint */
        try {
            if (ep == null)
                ep = new Endpoint();
            ep.libCreate();
        } catch (Exception e) {
            return;
        }

        /* Load config */
        String configPath = appDir + "/" + configName;
        File f = new File(configPath);
        if (f.exists()) {
            //loadConfig(configPath);
        } else {
            /* Set 'default' values */
            sipTpConfig.setPort(SIP_PORT);
        }

        /* Override log level setting */
        //epConfig.getLogConfig().setLevel(LOG_LEVEL);
        epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);

        /* Set log config. */
        LogConfig log_cfg = epConfig.getLogConfig();


        if (!SipApplication.getLogFilePath(context).isEmpty()) {
            epConfig.getLogConfig().setMsgLogging(1);
            log_cfg.setFilename(SipApplication.getLogFilePath(context));
        }


        logWriter = new MyLogWriter();
        log_cfg.setWriter(logWriter);
        log_cfg.setDecor(log_cfg.getDecor() &
                ~(pj_log_decoration.PJ_LOG_HAS_CR.swigValue() |
                        pj_log_decoration.PJ_LOG_HAS_NEWLINE.swigValue()));
        /* Media config    */
        epConfig.getMedConfig().setHasIoqueue(true);
        epConfig.getMedConfig().setClockRate(16000);
        epConfig.getMedConfig().setQuality(10);
        epConfig.getMedConfig().setEcOptions(1);
        epConfig.getMedConfig().setEcTailLen(200);

        /* Set ua config. */
        UaConfig ua_cfg = epConfig.getUaConfig();
        ua_cfg.setUserAgent("MobileOffice");
        StringVector stun_servers = new StringVector();
        ua_cfg.setStunServer(stun_servers);
        if (own_worker_thread) {
            ua_cfg.setThreadCnt(0);
            ua_cfg.setMainThreadOnly(true);
        }

        /* Init endpoint */
        try {
            ep.libInit(epConfig);
        } catch (Exception e) {
            return;
        }


        if (!isTLS) {
            try {
                ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
                        sipTpConfig);
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            try {
                ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS,
                        sipTpConfig);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        /* Set SI
        P port back to default for JSON saved config */
        //sipTpConfig.setPort(SIP_PORT);

        /* Create accounts. */
        for (int i = 0; i < accCfgs.size(); i++) {
            MyAccountConfig my_cfg = accCfgs.get(i);
            if (isToAddHeadersForPushNotification(context)) {
                my_cfg.accCfg.getRegConfig().setHeaders(getHeadersForPush(context));
            }
            /* Customize account config */
            my_cfg.accCfg.getNatConfig().setIceEnabled(true);
            my_cfg.accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
            my_cfg.accCfg.getVideoConfig().setAutoShowIncoming(true);

           /* MyAccount acc = addAcc(my_cfg.accCfg);
            if (acc == null)
                continue;
            *//* Add Buddies *//*
            for (int j = 0; j < my_cfg.buddyCfgs.size(); j++) {
                BuddyConfig bud_cfg = my_cfg.buddyCfgs.get(j);
                acc.addBuddy(bud_cfg);
            }*/
        }

        /* Start. */
        try {
            ep.libStart();
            ep.codecSetPriority("opus/48000/2", (short) (PRIORITY_MAX));
            ep.codecSetPriority("PCMA/8000", (short) (PRIORITY_MAX - 1));
            ep.codecSetPriority("PCMU/8000", (short) (PRIORITY_MAX - 2));
            ep.codecSetPriority("G729/8000", (short) PRIORITY_DISABLED);
            ep.codecSetPriority("speex/8000", (short) PRIORITY_DISABLED);
            ep.codecSetPriority("speex/16000", (short) PRIORITY_DISABLED);
            ep.codecSetPriority("speex/32000", (short) PRIORITY_DISABLED);
            ep.codecSetPriority("GSM/8000", (short) PRIORITY_DISABLED);
            ep.codecSetPriority("G722/16000", (short) PRIORITY_DISABLED);
            ep.codecSetPriority("G7221/16000", (short) PRIORITY_DISABLED);
            ep.codecSetPriority("G7221/32000", (short) PRIORITY_DISABLED);
            ep.codecSetPriority("ilbc/8000", (short) PRIORITY_DISABLED);


            // Set H264 Parameters
            VidCodecParam vidCodecParam = ep.getVideoCodecParam(H264_CODEC_ID);
            CodecFmtpVector codecFmtpVector = vidCodecParam.getDecFmtp();
            MediaFormatVideo mediaFormatVideo = vidCodecParam.getEncFmt();
            mediaFormatVideo.setWidth(H264_DEF_WIDTH);
            mediaFormatVideo.setHeight(H264_DEF_HEIGHT);
            vidCodecParam.setEncFmt(mediaFormatVideo);
            for (int i = 0; i < codecFmtpVector.size(); i++) {
                if (PROFILE_LEVEL_ID_HEADER.equals(codecFmtpVector.get(i).getName())) {
                    codecFmtpVector.get(i).setVal(PROFILE_LEVEL_ID_JANUS_BRIDGE);
                    break;
                }
            }
            vidCodecParam.setDecFmtp(codecFmtpVector);
            ep.setVideoCodecParam(H264_CODEC_ID, vidCodecParam);


        } catch (Exception e) {
            return;
        }
    }

    public MyAccount addAcc(AccountConfig cfg) {
        MyAccount acc = new MyAccount(cfg);
        try {
            acc.create(cfg);
        } catch (Exception e) {
            acc = null;
            return null;
        }
        Log.d("navya", "navya account list before addition" + accList.size());

        accList.add(acc);
        return acc;
    }

    public void delAcc(MyAccount acc) {
        Log.d("navya", "navya account list before removal" + accList.size());
        accList.remove(acc);
        accList.clear();
        Log.d("navya", "navya account list after removal" + accList.size());
    }

    private void loadConfig(String filename) {
        JsonDocument json = new JsonDocument();

        try {
            /* Load file */
            json.loadFile(filename);
            ContainerNode root = json.getRootContainer();

            /* Read endpoint config */
            epConfig.readObject(root);

            /* Read transport config */
            ContainerNode tp_node = root.readContainer("SipTransport");
            sipTpConfig.readObject(tp_node);

            /* Read account configs */
            accCfgs.clear();
            ContainerNode accs_node = root.readArray("accounts");
            while (accs_node.hasUnread()) {
                MyAccountConfig acc_cfg = new MyAccountConfig();
                acc_cfg.readObject(accs_node);
                accCfgs.add(acc_cfg);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        /* Force delete json now, as I found that Java somehow destroys it
         * after lib has been destroyed and from non-registered thread.
         */
        json.delete();
    }

    private void buildAccConfigs() {

    }

    private void saveConfig(String filename) {
        JsonDocument json = new JsonDocument();

        try {
            /* Write endpoint config */
            json.writeObject(epConfig);

            /* Write transport config */
            ContainerNode tp_node = json.writeNewContainer("SipTransport");
            sipTpConfig.writeObject(tp_node);

            /* Write account configs */
            buildAccConfigs();
            ContainerNode accs_node = json.writeNewArray("accounts");
            for (int i = 0; i < accCfgs.size(); i++) {
                accCfgs.get(i).writeObject(accs_node);
            }

            /* Save file */
            json.saveFile(filename);
        } catch (Exception e) {
        }

        /* Force delete json now, as I found that Java somehow destroys it
         * after lib has been destroyed and from non-registered thread.
         */
        json.delete();
    }


    public void deinit() {
        String configPath = appDir + "/" + configName;
        // saveConfig(configPath);

        /* Try force GC to avoid late destroy of PJ objects as they should be
         * deleted before lib is destroyed.
         */
        Runtime.getRuntime().gc();

        /* Shutdown pjsua. Note that Endpoint destructor will also invoke
         * libDestroy(), so this will be a test of double libDestroy().
         */
        try {
            ep.libDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Force delete Endpoint here, to avoid deletion from a non-
         * registered thread (by GC?).
         */
        /*ep.delete();
        ep = null;*/
        Runtime.getRuntime().gc();
    }

    public static SipHeaderVector getHeadersForPush(Context context) {
        SipHeader hdeviceToken = new SipHeader();
        hdeviceToken.setHName("X-Device-Token");
        hdeviceToken.setHValue(SipApplication.getPushToken(context));

        SipHeader hAppVersion = new SipHeader();
        hAppVersion.setHName("X-App-Version");
        hAppVersion.setHValue(SipApplication.getVersionName(context));

        SipHeader hbundleID = new SipHeader();
        hbundleID.setHName("X-Bundle-ID");
        hbundleID.setHValue(SipApplication.getBundleID(context));

        SipHeader hdeviceInfo = new SipHeader();
        hdeviceInfo.setHName("X-Device-Info");
        hdeviceInfo.setHValue(SipApplication.getDeviceInfo(context));

        SipHeader happlicationID = new SipHeader();
        happlicationID.setHName("X-Application-ID");
        happlicationID.setHValue(SipApplication.getApplicationID(context));

        SipHeader hDeviceType = new SipHeader();
        hDeviceType.setHName("X-Device-Type");
        hDeviceType.setHValue(SipApplication.getHdeviceType(context));

        SipHeader hVoIPID = new SipHeader();
        hVoIPID.setHName("X-VoIP-ID");
        hVoIPID.setHValue(SipApplication.gethVoipID(context));

        SipHeader hvoipPhoneID = new SipHeader();
        hvoipPhoneID.setHName("X-VoIP-Phone-ID");
        hvoipPhoneID.setHValue(SipApplication.gethVoipPhoneID(context));


        SipHeader hDebug = new SipHeader();
        hDebug.setHName("X-Debug-Mode");
        hDebug.setHValue("0");


        SipHeaderVector headerVector = new SipHeaderVector();
        headerVector.add(hdeviceToken);
        headerVector.add(hDeviceType);
        headerVector.add(hAppVersion);
        headerVector.add(hbundleID);
        headerVector.add(hdeviceInfo);
        headerVector.add(happlicationID);
        headerVector.add(hvoipPhoneID);
        headerVector.add(hVoIPID);
        headerVector.add(hDebug);

        return headerVector;
    }

    public static SipHeader getHeadersForUnregisterPush(Context context) {
        SipHeader hlogoutHeader = new SipHeader();
        hlogoutHeader.setHName("X-Action");
        hlogoutHeader.setHValue("Logout");
        SipHeaderVector headerVector = new SipHeaderVector();

        return hlogoutHeader;
    }


    public static boolean isToAddHeadersForPushNotification(Context context) {
        if (validateString(SipApplication.getPushToken(context)) && validateString(SipApplication.getVersionName(context))
                && validateString(SipApplication.getBundleID(context)) && validateString(SipApplication.getDeviceInfo(context))
                && validateString(SipApplication.getApplicationID(context))) {
            Log.d("navya", "navya" + "headers are present");
            return true;
        }
        Log.d("navya", "navya" + "headers are missing");
        return false;
    }

    public static boolean validateString(String value) {
        if (value != null && !value.trim().isEmpty())
            return true;
        else
            return false;
    }


    public static synchronized VidDevManager getVidDevManager() {
        return ep.vidDevManager();
    }

}