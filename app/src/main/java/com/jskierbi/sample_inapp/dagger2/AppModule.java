package com.jskierbi.sample_inapp.dagger2;

import android.app.Application;
import android.content.Context;
import dagger.Module;
import dagger.Provides;
import ext.inappbilling.util.IabHelper;

/**
 * Created by jakub on 26/04/16.
 */
@Module
public class AppModule {

  private final Application application;

  public AppModule(Application app) {
    this.application = app;
  }

  @Provides
  @ApplicationScope
  Application provideApplication() {
    return application;
  }

  @Provides
  @ApplicationScope
  @ForApplication
  Context provideAppContext() {
    return application.getApplicationContext();
  }

  @Provides
  IabHelper provideIabHelper(@ForApplication Context context) {
    return new IabHelper(context, "dummy_key");
  }
}
