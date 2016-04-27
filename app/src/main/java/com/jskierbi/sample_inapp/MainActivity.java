package com.jskierbi.sample_inapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.jskierbi.commons.LoadingLayout;
import com.jskierbi.commons.rx.BaseObserver;
import com.jskierbi.commons.typedviewholder.TypedViewHolder;
import com.jskierbi.commons.typedviewholder.TypedViewHolderAdapter;
import com.jskierbi.commons.typedviewholder.TypedViewHolderFactory;
import ext.inappbilling.util.*;
import lombok.Data;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.jskierbi.commons.ObjectUtils.defaultIfNull;
import static java.util.Arrays.asList;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class MainActivity extends IabBaseActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  @Bind(R.id.loading_layout) LoadingLayout mLoadingLayout;
  @Bind(R.id.recyclerview)   RecyclerView  mRecyclerView;

  @Inject IabManager mIabManager;

  List<String> mIabItemSkus = asList("product.001", "product.002", "product.003");

  Subscription mDialogSubscription = Subscriptions.empty();
  Subscription mIabSubscription    = Subscriptions.empty();

  TypedViewHolderAdapter<SkuItem> mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    App.component().inject(this);
    ButterKnife.bind(this);

    // setup recyclerview
    mAdapter = new TypedViewHolderAdapter.Builder<SkuItem>()
        .addFactory(SkuDetailsHolder.factory(new SkuDetailsHolder.ItemPurchaseListener() {
          @Override
          void onItemPurchaseClick(SkuItem item) {
            performPurchase("", item.details.getSku());
          }
        }))
        .build();
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.setAdapter(mAdapter);

    // Subscribe products
    subscribeProducts();
  }

  @Override
  protected void onDestroy() {
    mDialogSubscription.unsubscribe();
    mIabSubscription.unsubscribe();
    super.onDestroy();
  }

  void subscribeProducts() {
    mLoadingLayout.setLoadingVisible(true);
    mIabSubscription.unsubscribe();
    mIabSubscription = mIabManager
        .getItemDetails(mIabItemSkus)
        .subscribeOn(io())
        .observeOn(mainThread())
        .doOnUnsubscribe(new Action0() {
          @Override
          public void call() {
            mLoadingLayout.setLoadingVisible(false);
          }
        })
        .subscribe(new BaseObserver<Inventory>() {
          @Override
          public void onNext(Inventory inventory) {
            List<SkuItem> detailList = new ArrayList<>();
            for (String sku : mIabItemSkus) {
              SkuItem item = new SkuItem(inventory.getSkuDetails(sku), inventory.hasPurchase(sku));
              detailList.add(item);
              inventory.hasPurchase(sku);
            }
            mAdapter.setData(detailList);
          }

          @Override
          public void onError(Throwable e) {
            final Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.iab_query_products_failed)
                .setMessage(e.getMessage())
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    subscribeProducts();
                  }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
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
    subscribeProducts();
    if (result.isSuccess()) {
      final Dialog dialog = new AlertDialog.Builder(this)
          .setMessage(R.string.iab_purchase_succeeded)
          .setPositiveButton(R.string.ok, null)
          .show();
      mDialogSubscription.unsubscribe();
      mDialogSubscription = Subscriptions.create(new Action0() {
        @Override
        public void call() {
          dialog.dismiss();
        }
      });
    } else {
      Toast.makeText(this, "Purchase not completed. Reason: " + result.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  @Data
  static class SkuItem {
    final SkuDetails details;
    final boolean    isPurchased;
  }

  static class SkuDetailsHolder extends TypedViewHolder<SkuItem> {

    public static TypedViewHolderFactory<SkuItem> factory(final ItemPurchaseListener listener) {
      return new TypedViewHolderFactory<SkuItem>(SkuItem.class) {
        @Override
        public TypedViewHolder<SkuItem> build(ViewGroup parent) {
          return new SkuDetailsHolder(parent, listener);
        }
      };
    }

    @Bind(R.id.item_name) TextView itemName;
    @Bind(R.id.item_desc) TextView itemDesc;
    @Bind(R.id.btn_price) Button   itemPrice;
    @Bind(R.id.item_sku)  TextView itemSku;
    @Bind(R.id.separator) View     separator;

    SkuItem              item;
    ItemPurchaseListener listener;

    public SkuDetailsHolder(ViewGroup parent, ItemPurchaseListener listener) {
      super(R.layout.li_sku_details, parent);
      ButterKnife.bind(this, itemView);
      this.listener = defaultIfNull(listener, new ItemPurchaseListener());
    }

    @Override
    public void bind(SkuItem dataItem) {
      this.item = dataItem;

      itemName.setText(dataItem.details.getTitle());
      itemDesc.setText(dataItem.details.getDescription());
      itemSku.setText(dataItem.details.getSku());

      // Price / purchased
      itemPrice.setText(dataItem.isPurchased ? getContext().getString(R.string.purchased) : dataItem.details.getPrice());
      itemPrice.setEnabled(!dataItem.isPurchased);

      // Separator
      separator.setVisibility(getAdapterPosition() == 0 ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.btn_price)
    void onPurchaseClick() {
      listener.onItemPurchaseClick(item);
    }

    public static class ItemPurchaseListener {
      void onItemPurchaseClick(SkuItem item) {}
    }
  }
}
