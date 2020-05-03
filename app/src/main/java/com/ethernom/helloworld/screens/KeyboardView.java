package com.ethernom.helloworld.screens;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.*;
import com.ethernom.helloworld.R;

public class KeyboardView extends LinearLayout implements View.OnClickListener {

    private SparseArray<String> keyValues = new SparseArray<>();
    private InputConnection inputConnection;
    public KeyboardView(Context context) {
        this(context, null, 0);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
        init(context, attrs);

    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context , AttributeSet attrs) {
        inflate( context, R.layout.keyboard, this);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.t9_key_0).setOnClickListener(this);
        findViewById(R.id.t9_key_1).setOnClickListener(this);
        findViewById(R.id.t9_key_2).setOnClickListener(this);
        findViewById(R.id.t9_key_3).setOnClickListener(this);
        findViewById(R.id.t9_key_4).setOnClickListener(this);
        findViewById(R.id.t9_key_5).setOnClickListener(this);
        findViewById(R.id.t9_key_6).setOnClickListener(this);
        findViewById(R.id.t9_key_7).setOnClickListener(this);
        findViewById(R.id.t9_key_8).setOnClickListener(this);
        findViewById(R.id.t9_key_9).setOnClickListener(this);
        findViewById(R.id.t9_key_clear).setOnClickListener(this);
        findViewById(R.id.t9_key_enter).setOnClickListener(this);

        keyValues.put(R.id.t9_key_0, "0");
        keyValues.put(R.id.t9_key_1, "1");
        keyValues.put(R.id.t9_key_2, "2");
        keyValues.put(R.id.t9_key_3, "3");
        keyValues.put(R.id.t9_key_4, "4");
        keyValues.put(R.id.t9_key_5, "5");
        keyValues.put(R.id.t9_key_6, "6");
        keyValues.put(R.id.t9_key_7, "7");
        keyValues.put(R.id.t9_key_8, "8");
        keyValues.put(R.id.t9_key_9, "9");
        keyValues.put(R.id.t9_key_enter, "\n");


    }

    @Override
    public void onClick(View v) {
        if(inputConnection == null ) {
            return;
        }

        if(v.getId() == R.id.t9_key_clear) {
            CharSequence selextedText = inputConnection.getSelectedText(0);
            if(TextUtils.isEmpty(selextedText)) {
                inputConnection.deleteSurroundingText(1,0);
            } else {
                inputConnection.commitText("", 1);
            }
        } else {
            String value = keyValues.get(v.getId());
            inputConnection.commitText(value, 1);
        }
    }

    public void setInputConnection(InputConnection inputConnection) {
        this.inputConnection = inputConnection;
    }

}
