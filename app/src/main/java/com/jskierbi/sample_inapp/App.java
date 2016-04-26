package com.jskierbi.sample_inapp;

import android.app.Application;
import com.jskierbi.sample_inapp.dagger2.AppComponent;
import com.jskierbi.sample_inapp.dagger2.AppModule;
import com.jskierbi.sample_inapp.dagger2.DaggerAppComponent;

/**
 * Created by jakub on 26/04/16.
 */
public class App extends Application {

  static App app;

  AppComponent component;

  @Override
  public void onCreate() {
    app = this;
    super.onCreate();
  }

  public static AppComponent component() {
    if (app.component == null) {
      app.component = DaggerAppComponent.builder()
          .appModule(new AppModule(app))
          .build();
    }
    return app.component;
  }
}
