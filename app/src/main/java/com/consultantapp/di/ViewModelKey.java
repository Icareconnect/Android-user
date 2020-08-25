package com.consultantapp.di;

import androidx.lifecycle.ViewModel;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.MapKey;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@MapKey
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface ViewModelKey {
    Class<? extends ViewModel> value();
}