package com.jskierbi.sample_inapp.dagger2;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jakub on 27/04/16.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface IabItemSkus {
}
