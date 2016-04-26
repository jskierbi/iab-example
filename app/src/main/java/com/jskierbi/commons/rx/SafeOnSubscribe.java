package com.jskierbi.commons.rx;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by jakub on 16/12/15.
 */
public abstract class SafeOnSubscribe<T> implements Observable.OnSubscribe<T> {

  public abstract void safeCall(Subscriber<? super T> subscriber) throws Throwable;

  @Override
  public final void call(Subscriber<? super T> subscriber) {
    try {
      safeCall(subscriber);
    } catch (Throwable e) {
      subscriber.onError(e);
    }
  }
}
