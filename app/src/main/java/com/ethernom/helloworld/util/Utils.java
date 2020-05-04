package com.ethernom.helloworld.util;

import android.view.View;

public class Utils {


    public static void preventDoubleClick(final View view){
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
    }
}
