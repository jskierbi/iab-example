package com.jskierbi.sample_inapp.dagger2;

import com.jskierbi.sample_inapp.MainActivity;
import dagger.Component;
import ext.inappbilling.util.IabHelper;

/**
 * Created by jakub on 26/04/16.
 */
@ApplicationScope
@Component(modules = {
    AppModule.class
})
public interface AppComponent {

  void inject(MainActivity mainActivity);

  IabHelper getIabHelper();
}
