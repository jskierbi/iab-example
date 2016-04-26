package com.jskierbi.commons.rx;

import android.util.Log;
import rx.Observer;

import static com.jskierbi.commons.ObjectUtils.defaultIfEmpty;


/**
 * Created by jakub on 20/11/15.
 */
public class BaseObserver<T> implements Observer<T> {

  private static final String TAG = BaseObserver.class.getSimpleName();

  private static final String DEFAULT_MESSAGE = "Error in [%s]";

  private final String errorMessage;

  public BaseObserver() {
    errorMessage = String.format(DEFAULT_MESSAGE, getClass());
  }

  public BaseObserver(String errorMessage) {
    this.errorMessage = String.format(DEFAULT_MESSAGE, getClass()) + ": " + defaultIfEmpty(errorMessage, "");
  }

  @Override
  public void onCompleted() {
  }

  @Override
  public void onError(Throwable e) {
    Log.e(TAG, errorMessage, new Exception(e));
  }

  @Override
  public void onNext(T t) {
  }
}
