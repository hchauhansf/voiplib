/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class OnCallReplacedParam {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OnCallReplacedParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OnCallReplacedParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_OnCallReplacedParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setNewCallId(int value) {
    pjsua2JNI.OnCallReplacedParam_newCallId_set(swigCPtr, this, value);
  }

  public int getNewCallId() {
    return pjsua2JNI.OnCallReplacedParam_newCallId_get(swigCPtr, this);
  }

  public OnCallReplacedParam() {
    this(pjsua2JNI.new_OnCallReplacedParam(), true);
  }

}
