package com.jskierbi.sample_inapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.jskierbi.commons.rx.BaseObserver;
import ext.inappbilling.util.IabException;
import ext.inappbilling.util.IabHelper;
import ext.inappbilling.util.IabResult;
import ext.inappbilling.util.Purchase;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;

/**
 * Created by jakub on 26/04/16.
 */
public abstract class IabBaseActivity extends AppCompatActivity implements IabHelper.OnIabPurchaseFinishedListener {
  final static         int    PURCHASE_REQUEST_CODE = 699;
  private static final String TAG                   = IabBaseActivity.class.getSimpleName();

  IabHelper mIabHelper; // Not injected directly to prevent child class override
  BehaviorSubject<IabHelper> mIabHelperInitSubject = BehaviorSubject.create();

  Subscription mPurchaseSubscription       = Subscriptions.empty();
  Subscription mActivityResultSubscription = Subscriptions.empty();


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Setup IabHelper
    mIabHelper = App.component().getIabHelper();
    mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      @Override
      public void onIabSetupFinished(IabResult result) {
        if (result.isSuccess()) {
          mIabHelperInitSubject.onNext(mIabHelper);
        } else {
          IabException e = new IabException(result);
          onIabInitFailed(e);
          mIabHelperInitSubject.onError(new IabException(result));
        }
      }
    });
  }

  protected final void performPurchase(final String developerPayload, final String sku) {
    mPurchaseSubscription.unsubscribe();
    mPurchaseSubscription = mIabHelperInitSubject.subscribe(new BaseObserver<IabHelper>() {
      @Override
      public void onNext(IabHelper iabHelper) {
        iabHelper.launchPurchaseFlow(IabBaseActivity.this, sku, PURCHASE_REQUEST_CODE, IabBaseActivity.this, developerPayload);
      }
    });
  }

  protected final void performSubscriptionPurchase(final String developerPayload, final String sku) {
    mPurchaseSubscription.unsubscribe();
    mPurchaseSubscription = mIabHelperInitSubject.subscribe(new BaseObserver<IabHelper>() {
      @Override
      public void onNext(IabHelper iabHelper) {
        iabHelper.launchSubscriptionPurchaseFlow(IabBaseActivity.this, sku, PURCHASE_REQUEST_CODE, IabBaseActivity.this, developerPayload);
      }
    });
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mActivityResultSubscription.unsubscribe();
    mActivityResultSubscription = mIabHelperInitSubject.subscribe(
        new BaseObserver<IabHelper>() {
          @Override
          public void onNext(IabHelper iabHelper) {
            // Workaround for handling purchase after activity recreate (orientation change)
            // see http://stackoverflow.com/questions/18223130/in-app-billing-rapid-device-orientation-causes-crash-illegalstateexception
            iabHelper.resumePurchase(requestCode, IabBaseActivity.this);
            iabHelper.handleActivityResult(requestCode, resultCode, data);
          }
        }
    );
  }

  @Override
  protected void onDestroy() {
    mIabHelperInitSubject.onCompleted();
    mPurchaseSubscription.unsubscribe();
    mActivityResultSubscription.unsubscribe();
    try {
      mIabHelper.dispose();
    } catch (Throwable t) {
      Log.w(TAG, "Cannot dispose IabHelper!", t);
    }
    super.onDestroy();
  }

  protected abstract void onIabInitFailed(IabException exception);

  @Override
  public abstract void onIabPurchaseFinished(IabResult result, Purchase info);
}
