package com.jskierbi.sample_inapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.jskierbi.commons.rx.BaseObserver;
import ext.inappbilling.util.IabException;
import ext.inappbilling.util.IabResult;
import ext.inappbilling.util.Inventory;
import ext.inappbilling.util.Purchase;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

import javax.inject.Inject;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class MainActivity extends IabBaseActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  @Inject IabManager mIabManager;

  Subscription mDialogSubscription = Subscriptions.empty();
  Subscription mIabSubscription    = Subscriptions.empty();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    App.component().inject(this);
    ButterKnife.bind(this);
  }

  @Override
  protected void onDestroy() {
    mDialogSubscription.unsubscribe();
    mIabSubscription.unsubscribe();
    super.onDestroy();
  }

  @OnClick(R.id.btn_query_products)
  void queryProductsClick() {
    mIabSubscription.unsubscribe();
    mIabSubscription = mIabManager
        .getAllProducts()
        .subscribeOn(io())
        .observeOn(mainThread())
        .subscribe(new BaseObserver<Inventory>() {
          @Override
          public void onNext(Inventory inventory) {
            Log.d(TAG, "=========================");
            for (String sku : inventory.getAllOwnedSkus()) {
              Log.d(TAG, "Owned: " + sku);
            }
            Log.d(TAG, "=========================");
          }

          @Override
          public void onError(Throwable e) {
            Log.e(TAG, "Error retrieving inventory", e);
          }
        });
  }

  @Override
  protected void onIabInitFailed(IabException exception) {
    final Dialog dialog = new AlertDialog.Builder(this)
        .setMessage(R.string.iab_init_failed)
        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            finish();
          }
        })
        .setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            finish();
          }
        })
        .show();
    mDialogSubscription.unsubscribe();
    mDialogSubscription = Subscriptions.create(new Action0() {
      @Override
      public void call() {
        dialog.dismiss();
      }
    });
  }

  @Override
  public void onIabPurchaseFinished(IabResult result, Purchase info) {
    final Dialog dialog = new AlertDialog.Builder(this)
        .setMessage(R.string.iab_purchase_succeeded)
        .setNegativeButton(R.string.ok, null)
        .show();
    mDialogSubscription.unsubscribe();
    mDialogSubscription = Subscriptions.create(new Action0() {
      @Override
      public void call() {
        dialog.dismiss();
      }
    });
  }
}
