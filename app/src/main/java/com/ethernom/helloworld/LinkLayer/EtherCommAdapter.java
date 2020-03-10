package com.ethernom.helloworld.LinkLayer;
import android.content.Context;


import java.util.ArrayList;


abstract public class EtherCommAdapter {
    // adapter error codes

    public CardEventListener CardEventListener = null;

    abstract public void Init(Context context);

    abstract public String GetAdapterAddress();
    abstract public void SetAdapterAddress(String address);

    abstract public void CardOpen(CardInfo cardInfo);
    abstract public void CardClose();

    public void SetCardEventListener(CardEventListener listener){
        CardEventListener = listener;
    }

    // a way to write byte buffers
    abstract public void WriteToCard(byte [] buffer);

}

