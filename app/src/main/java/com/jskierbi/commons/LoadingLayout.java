package com.jskierbi.commons;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.jskierbi.sample_inapp.R;


/**
 * This code is interpretation of library:
 * http://repo1.maven.org/maven2/com/jskierbi/loadinglayout/
 * <p/>
 * This layout extends FrameLayout and has 1 built-in view: loading
 * Loading can be shown using code:
 * * {@link #setLoadingVisible(boolean)}
 * <p/>
 * Custom attributes ({@link R.styleable#LoadingLayout}):
 * - {@link R.styleable#LoadingLayout_loading_layout}
 * - {@link R.styleable#LoadingLayout_loading_layer_alpha}
 * - {@link R.styleable#LoadingLayout_loading_initially_visible}
 * <p/>
 * Loading layout can contain inner, animated view, which will be faded in with slight delay.
 * This way, when waiting e.g. for disk cache (small amount of time) animation is not shown before
 * loading layer is faded-out (=better user exp)
 * <p/>
 * Inner view should have id set to: @+id/loading_view
 * Also possible for inner view to contain a TextView with id: @+id/loading_label
 */
public class LoadingLayout extends FrameLayout {

  @LayoutRes
  private static final int DEFAULT_LOADING_LAYOUT = R.layout.loadinglayout_loading;

  private static final float DEFAULT_LOADING_LAYER_ALPHA = 0.7f;

  private InstanceState mState = new InstanceState();
  private View     mLoadingLayout;
  private View     mLoadingLayoutAnimView;
  private TextView mLoadingLayoutLable;

  private float mLoadingLayerAlpha;

  /**
   * Instance state for internal use. Public visibility because of Parceler.
   */
  public static class InstanceState implements Parcelable {
    boolean flgLoadingVisible = false;
    boolean flgErrorVisible   = false;
    Parcelable superInstanceState;

    public InstanceState() {
    }

    public InstanceState(Parcel in) {
      boolean[] stateFlags = new boolean[2];
      in.readBooleanArray(stateFlags);
      flgLoadingVisible = stateFlags[0];
      flgErrorVisible = stateFlags[1];
      superInstanceState = in.readParcelable(null);
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      boolean[] stateFlags = new boolean[2];
      stateFlags[0] = flgLoadingVisible;
      stateFlags[1] = flgErrorVisible;
      dest.writeBooleanArray(stateFlags);
      dest.writeParcelable(superInstanceState, flags);
    }

    public static final Creator CREATOR = new Creator() {
      @Override
      public Object createFromParcel(Parcel source) {
        return new InstanceState(source);
      }

      @Override
      public Object[] newArray(int size) {
        return new InstanceState[size];
      }
    };
  }

  public LoadingLayout(Context context) {
    super(context);
    inflateLayout(context, DEFAULT_LOADING_LAYOUT);
    mLoadingLayerAlpha = DEFAULT_LOADING_LAYER_ALPHA;
  }

  public LoadingLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    loadAttrs(context, attrs, 0);
  }

  public LoadingLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    loadAttrs(context, attrs, defStyle);
  }

  /**
   * Show or hide loading layer.
   * Show: loading layer fades in, loading anim inside (@+id/loading_view) is faded in with slight delay
   * Hide: loading layer fades out.
   *
   * @param flgVisible whether view should be visible or not
   * @throws IllegalStateException
   */
  @SuppressLint("NewApi")
  public void setLoadingVisible(boolean flgVisible) {
    final boolean isAlreadyAdded = this == mLoadingLayout.getParent();
    if (flgVisible) {
      if (!isAlreadyAdded) {
        addView(mLoadingLayout);
        mLoadingLayout.setAlpha(0f);
      }
      bringChildToFront(mLoadingLayout);
      if (!mState.flgLoadingVisible) {
        mLoadingLayout.animate().setListener(null);
        mLoadingLayout.animate()
            .alpha(mLoadingLayerAlpha)
            .setDuration(500l)
            .start();

        if (mLoadingLayoutAnimView != null) {
          mLoadingLayoutAnimView.setAlpha(0.f);
          mLoadingLayoutAnimView.animate().cancel();
          mLoadingLayoutAnimView.animate()
              .alpha(1f)
              .setDuration(500l)
              .setStartDelay(200l)
              .start();
        }
      }
    } else if (isAlreadyAdded) {
      // Animate - fade out
      if (mState.flgLoadingVisible) {
        if (mLoadingLayoutAnimView != null) mLoadingLayoutAnimView.animate().cancel();
        mLoadingLayout.animate().cancel(); // Cancel current animation
        mLoadingLayout.animate()
            .alpha(0.f)
            .setDuration(350l)
            .setListener(new Animator.AnimatorListener() {
              @Override
              public void onAnimationStart(Animator animation) {
              }

              @Override
              public void onAnimationCancel(Animator animation) {
              }

              @Override
              public void onAnimationRepeat(Animator animation) {
              }

              @Override
              public void onAnimationEnd(Animator animation) {
                try {
                  mLoadingLayout.animate().setListener(null);
                  removeView(mLoadingLayout);
                } catch (Exception ignore) {
                }
              }
            });
      }
    }
    mState.flgLoadingVisible = flgVisible;
  }

  /**
   * Checks if loading layer is visible to user
   */
  public boolean isLoadingVisible() {
    return mState.flgLoadingVisible;
  }

  /**
   * Checks if error view is visible to user
   */
  public boolean isErrorVisible() {
    return mState.flgErrorVisible;
  }

  /**
   * Returns loading view root
   */
  public View getLoadingView() {
    return mLoadingLayout;
  }

  /**
   * Load attributes from XML
   */
  private void loadAttrs(Context context, AttributeSet attrs, int defStyle) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingLayout, defStyle, 0);
    mLoadingLayerAlpha = a.getFloat(R.styleable.LoadingLayout_loading_layer_alpha, DEFAULT_LOADING_LAYER_ALPHA);
    int color = a.getColor(R.styleable.LoadingLayout_loader_color, Color.TRANSPARENT);
    String label = a.getString(R.styleable.LoadingLayout_label);
    inflateLayout(
        context,
        a.getResourceId(R.styleable.LoadingLayout_loading_layout, DEFAULT_LOADING_LAYOUT));
    boolean mFlgLoadingInitiallyVisible = a.getBoolean(R.styleable.LoadingLayout_loading_initially_visible, false);
    a.recycle();

    if (color != Color.TRANSPARENT && mLoadingLayoutAnimView instanceof ProgressBar) {
      ((ProgressBar) mLoadingLayoutAnimView).getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    if (mLoadingLayoutLable != null && !TextUtils.isEmpty(label)) {
      mLoadingLayoutLable.setText(label);
    }

    if (mFlgLoadingInitiallyVisible) {
      setLoadingVisible(true);
      mLoadingLayout.animate().setListener(null);
      mLoadingLayout.animate().cancel();
      mLoadingLayout.setAlpha(mLoadingLayerAlpha);
    }
  }

  /**
   * Inflate layouts
   */
  private void inflateLayout(Context context, @LayoutRes int loadingLayout) {
    mLoadingLayout = LayoutInflater.from(context).inflate(loadingLayout, this, false);
    mLoadingLayoutAnimView = mLoadingLayout.findViewById(R.id.loading_view);
    mLoadingLayoutLable = (TextView) mLoadingLayout.findViewById(R.id.loading_label);
  }
}
