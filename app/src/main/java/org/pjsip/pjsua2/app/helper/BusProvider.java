package org.pjsip.pjsua2.app.helper;


import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * BusProvider class is used for publishing some events and performing some actions when received
 *
 * @author rajantalwar
 * @version 1.0
 */
public class BusProvider {


    public static BusProvider mInstance;

    private final PublishSubject<Object> mBus=PublishSubject.create();

    /**
     * Method for providing singleton instance of BusProvider
     *
     * @return singleton instance of BusProvider
     */
    public static BusProvider getInstance() {
        if (mInstance == null) {
            mInstance = new BusProvider();
        }


        return mInstance;
    }


    /**
     * Method for publishing events
     *
     * @param object event which is to be published
     */
    public void send(Object object) {
        mBus.onNext(object);
    }

    /**
     * Method for listening to events and performing actions when received
     *
     * @return the event which is to be observed
     */
    public Observable<Object> toObserverable() {
        return mBus;
    }

}
