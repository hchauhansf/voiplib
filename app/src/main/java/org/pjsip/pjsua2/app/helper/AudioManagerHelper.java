package org.pjsip.pjsua2.app.helper;

import android.content.Context;
import android.media.AudioManager;

import org.pjsip.pjsua2.app.application.SipApplication;

/**
 * AudioMangagerHelper class is the helper class for controlling the speaker during call
 *
 * @author rajantalwar
 * @version 1.0
 */
public class AudioManagerHelper {

    /**
     *This method should only be used by applications that replace the platform-wide
     * management of audio settings or the main telephony application.
     * This method turns on speakerphone
     *
     * @param context Android context to getthe handle to a system-level service by name
     */
    public static void turnOnPhoneSpeaker(Context context) {
        ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(true);
    }

    /**
     *This method should only be used by applications that replace the platform-wide
     * management of audio settings or the main telephony application.
     *This method turns off speakerphone
     * @param context Android context to getthe handle to a system-level service by name
     */
    public static void turnOffPhoneSpeaker(Context context) {
        ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(false);
    }
}
