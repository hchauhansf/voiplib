package org.pjsip.pjsua2.app.helper;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.pjsip.pjsua2.app.service.PhoneSipService;

import java.util.Iterator;
import java.util.List;

/**
 * ServiceExecutor class is the helper class for communicating with the {@link PhoneSipService}
 * This class is used by {@link org.pjsip.pjsua2.app.service.ServiceCommands} for passing information
 * to perform operation by service.
 *
 * @author rajantalwar
 * @version 1.0

 */
public class ServiceExecutor {

    /**
     * Method for passing information about the action to be performed by {@link PhoneSipService}<br>
     * It checks by calling {@link #isServiceRunningInForeground(Context, Class)} to check if
     * service is in foreground or not.<br>
     * If service is in foreground it calls {@link android.content.Context#startService(Intent)}
     * If service is not in foreground then it starts the service by calling
     * {@link ContextCompat#startForegroundService(Context, Intent)}
     *
     * @param context Android context for communicating with service
     * @param intent bundle containing infomation for the type of operation to be performed
     *               bundle is passed to the service and based on that service perform the needed action.
     *               This bundle also conatins the information required for performing the operation
     */
    public static synchronized void executeSipServiceAction(Context context, Intent intent) {
        intent.setComponent(new ComponentName(context, PhoneSipService.class));
        Log.d("navya15", "navya15 isServiceRunningInForeground " +
                isServiceRunningInForeground(context, PhoneSipService.class));
        if(isServiceRunningInForeground(context, PhoneSipService.class)) {
            context.startService(intent);
        } else {
            ContextCompat.startForegroundService(context, intent);
        }
    }

    /**
     * Method for checking whether the service is running in foreground or not.
     * This method is used by {@link #executeSipServiceAction(Context, Intent)}
     *
     * @param context  Android context
     * @param serviceClass Service class name
     * @return boolean true if the service has asked to run as a foreground process  else false
     *
     */
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }
}
