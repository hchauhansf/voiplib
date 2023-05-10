package org.pjsip.pjsua2.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.app.MyApp;

/**
 * Service with a background worker thread.
 *
 */
class BackgroundService extends Service {

    public HandlerThread mWorkerThread;


    private Handler mHandler;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null)
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startWorkerThread();
    }

    public synchronized void startWorkerThread() {
        mWorkerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_FOREGROUND);
        mWorkerThread.setPriority(Thread.MAX_PRIORITY);
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());
        checkThread(mWorkerThread);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWorkerThread.quitSafely();
        } else {
            mWorkerThread.quit();
        }
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }

    }

    public void enqueueJob(Runnable job) {
        if (mWorkerThread.isAlive()) {
            mHandler.post(job);
        } else {
            startWorkerThread();
            enqueueJob(job);
        }

    }

    protected void enqueueDelayedJob(Runnable job) {
        mHandler.postDelayed(job, 5000);
    }

    public synchronized void checkThread(Thread thread) {
        try {
            Endpoint mEndpoint = MyApp.ep;
            if (mEndpoint != null && !mEndpoint.libIsThreadRegistered())
                mEndpoint.libRegisterThread(thread.getName());

        } catch (Exception e) {
            Log.w("SIP", "Threading: libRegisterThread failed: " + e.getMessage());
        }
    }

    public void dequeueJob(Runnable job) {
        mHandler.removeCallbacks(job);
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }


}
