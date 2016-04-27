package com.jskierbi.commons.typedviewholder;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Base class for all displayable view holders
 * <p/>
 * Created by jakub on 17/07/15.
 */
public abstract class TypedViewHolder<T> extends RecyclerView.ViewHolder {

  private Context mContext;

  public TypedViewHolder(@LayoutRes int layoutRes, ViewGroup parent) {
    super(LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false));
    mContext = parent.getContext();
  }

  public TypedViewHolder(View view) {
    super(view);
    mContext = view.getContext();
  }

  protected Context getContext() {
    return mContext;
  }

  public abstract void bind(T dataItem);
}
