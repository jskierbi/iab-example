package com.jskierbi.sample_inapp;

import android.support.annotation.VisibleForTesting;
import com.jskierbi.commons.rx.SafeOnSubscribe;
import com.jskierbi.sample_inapp.dagger2.ApplicationScope;
import ext.inappbilling.util.*;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

import static com.jskierbi.commons.ObjectUtils.isEmpty;

/**
 * Implementation of in app billing features.
 * <p/>
 * Please note, that actual purchase flow is deeply integrated into Activity
 * lifecycle, thus requires usage of base {@link IabBaseActivity} class to
 * access performPurchase action.
 * <p/>
 * Created by marcinbak on 30/09/14.
 */
@ApplicationScope
public class IabManager {

  private Provider<IabHelper>        mIabHelperProvider;
  private BehaviorSubject<IabHelper> mHelperInitSubject;

  @Inject
  public IabManager(final Provider<IabHelper> helperProvider) {
    mIabHelperProvider = helperProvider;
  }

  public Observable<Map<String, String>> getPrice(final List<String> skuList) {
    return initializeHelper()
        .first()
        .flatMap(new Func1<IabHelper, Observable<Map<String, String>>>() {
          @Override
          public Observable<Map<String, String>> call(final IabHelper helper) {
            return Observable.create(new SafeOnSubscribe<Map<String, String>>() {
              @Override
              public void safeCall(final Subscriber<? super Map<String, String>> subscriber) throws Throwable {
                Inventory inventory = helper.queryInventory(true, skuList);
                Map<String, String> priceMap = new HashMap<>(skuList.size());
                for (String sku : skuList) {
                  SkuDetails skuDetails = inventory.getSkuDetails(sku);
                  if (skuDetails == null) {
                    throw new IllegalStateException("Failed to find in inventory item for SKU= " + sku + " The inventory returned= " + inventory);
                  }
                  priceMap.put(sku, skuDetails.getPrice());
                }
                if (!priceMap.isEmpty()) {
                  subscriber.onNext(priceMap);
                }
                subscriber.onCompleted();
              }
            });
          }
        });
  }

  public Observable<Set<Purchase>> getPurchases() {
    return initializeHelper()
        .first()
        .flatMap(new Func1<IabHelper, Observable<Set<Purchase>>>() {
          @Override
          public Observable<Set<Purchase>> call(final IabHelper helper) {
            return Observable.create(new SafeOnSubscribe<Set<Purchase>>() {
              @Override
              public void safeCall(final Subscriber<? super Set<Purchase>> subscriber) throws Throwable {
                Inventory inventory = helper.queryInventory(true, null);
                final List<String> ownedSkuList = inventory.getAllOwnedSkus();
                if (isEmpty(ownedSkuList)) {
                  subscriber.onCompleted();
                  return;
                }
                Set<Purchase> purchasedProducts = new HashSet<>();
                for (String sku : ownedSkuList) {
                  Purchase purchase = inventory.getPurchase(sku);
                  if (purchase == null) {
                    throw new IllegalStateException("Failed to find in purchase for SKU= " + sku + " The inventory returned= " + inventory);
                  }
                  purchasedProducts.add(purchase);
                }
                subscriber.onNext(purchasedProducts);
                subscriber.onCompleted();
              }
            });
          }
        });
  }

  @VisibleForTesting
  synchronized Observable<IabHelper> initializeHelper() {
    if (mHelperInitSubject == null || mHelperInitSubject.hasThrowable()) {
      mHelperInitSubject = BehaviorSubject.create();
      final IabHelper iabHelper = mIabHelperProvider.get();
      iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
        @Override
        public void onIabSetupFinished(IabResult result) {
          if (result.isSuccess()) {
            mHelperInitSubject.onNext(iabHelper);
          } else {
            mHelperInitSubject.onError(new IabException(result));
          }
        }
      });
    }
    return mHelperInitSubject;
  }
}
