package com.jskierbi.commons.typedviewholder;

import android.view.ViewGroup;

/**
 * Provide instances of TypedViewHolder
 * <p/>
 * Created by jakub on 17/07/15.
 */
public abstract class TypedViewHolderFactory<T> {

  private Class<T> mDataType;

  public TypedViewHolderFactory(Class<T> dataType) {
    this.mDataType = dataType;
  }

  public Class<T> getViewHolderType() {
    return mDataType;
  }

  public abstract TypedViewHolder<T> build(ViewGroup parent);
}
