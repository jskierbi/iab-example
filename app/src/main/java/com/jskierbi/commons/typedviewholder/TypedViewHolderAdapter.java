package com.jskierbi.commons.typedviewholder;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic adapter for RecyclerView
 * Uses factory pattern and registration mechanism to register typed ViewHolder for each available item type
 *
 * @param <T> base data type for each item
 */
public class TypedViewHolderAdapter<T> extends RecyclerView.Adapter<TypedViewHolder<? extends T>> {

  private List<T> mList;
  private Map<Class, TypedViewHolderFactory<? extends T>> mViewModelToHolderFactoryMap = new HashMap<>();
  private List<Class> mViewModelTypeList;

  private TypedViewHolderAdapter(Map<Class, TypedViewHolderFactory<? extends T>> viewHolderFactoryMap) {
    this.mViewModelToHolderFactoryMap = viewHolderFactoryMap;
    this.mViewModelTypeList = new ArrayList<>(viewHolderFactoryMap.keySet());
  }

  public static class Builder<T> {

    private Map<Class, TypedViewHolderFactory<? extends T>> mViewHolderFactoryMap = new HashMap<>();

    public Builder<T> addFactory(TypedViewHolderFactory<? extends T> typedViewHolderFactory) {
      final Class viewHolderDataType = typedViewHolderFactory.getViewHolderType();
      if (mViewHolderFactoryMap.containsKey(viewHolderDataType)) {
        throw new IllegalStateException("Factory already registered for type: " + viewHolderDataType);
      }
      mViewHolderFactoryMap.put(typedViewHolderFactory.getViewHolderType(), typedViewHolderFactory);
      return this;
    }

    public TypedViewHolderAdapter<T> build() {
      return new TypedViewHolderAdapter<>(mViewHolderFactoryMap);
    }
  }

  public void setData(List<T> list) {
    mList = list;
    notifyDataSetChanged();
  }

  public List<T> getData() {
    return mList;
  }

  @Override
  public TypedViewHolder<? extends T> onCreateViewHolder(ViewGroup parent, int viewType) {
    Class dataType = mViewModelTypeList.get(viewType);
    TypedViewHolderFactory<? extends T> factory = mViewModelToHolderFactoryMap.get(dataType);
    if (factory == null) {
      throw new IllegalStateException("No factory regisetered for type: " + dataType);
    }
    return factory.build(parent);
  }

  @Override
  public void onBindViewHolder(TypedViewHolder<? extends T> holder, int position) {
    TypedViewHolder holderErasedGeneric = (TypedViewHolder) holder;
    //noinspection unchecked
    holderErasedGeneric.bind(mList.get(position));
  }

  @Override
  public int getItemCount() {
    return mList == null ? 0 : mList.size();
  }

  @Override
  public int getItemViewType(int position) {
    Class dataType = mList.get(position).getClass();
    for (int i = 0; i < mViewModelTypeList.size(); ++i) {
      if (dataType.equals(mViewModelTypeList.get(i))) {
        return i;
      }
    }
    throw new IllegalStateException("Factory for data type not registered: " + dataType);
  }
}
