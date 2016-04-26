package com.jskierbi.sample_inapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import ext.inappbilling.util.IabException;
import ext.inappbilling.util.IabResult;
import ext.inappbilling.util.Purchase;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

import javax.inject.Inject;

public class MainActivity extends IabBaseActivity {

  @Inject IabManager mIabManager;

  Subscription mDialogSubscription = Subscriptions.empty();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    App.component().inject(this);
  }

  @Override
  protected void onDestroy() {
    mDialogSubscription.unsubscribe();
    super.onDestroy();
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
