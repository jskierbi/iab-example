package com.jskierbi.sample_inapp.dagger2;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jakub on 26/04/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface ForApplication {
}
