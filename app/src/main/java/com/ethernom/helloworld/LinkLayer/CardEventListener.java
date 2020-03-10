package com.ethernom.helloworld.LinkLayer;

abstract public class CardEventListener {
    public abstract void onCardOpenSuccess(int resultCode);
    public abstract void onCardOpenFail(int resultCode, int hwSpecificError);

    public abstract void onCardClosedSucess(int resultCode);
    public abstract void onCardClosedFail(int resultCode,int hwSpecificError);
    public abstract void onCardClosedByCard(int resultCode);
    public abstract void onCardConnectionDropped(int resultCode);

    public abstract void onWriteToCardSuccess(int resultCode);
    public abstract void onWriteToCardFail(int resultCode, int hwSpecificError);

    public abstract void onReadFromCardSuccess(int resultCode, byte[] buffer);
    public abstract void onReadFromCardFail(int resultCode, int hwSpecificError);

}
