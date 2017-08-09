package com.zte.autodial;

import android.app.Application;
import android.content.Context;

/**
 * 重写application，该类主要用来声明一些全局变量，方便在activity和service中使用
 */

public class AutoDialApp extends Application
{

    private boolean state;//当前呼叫状态，分为 空闲和正在呼叫...
    private String toalNumbers;//待呼叫号码总量
    private String dealNumbers;//已经呼叫的号码数量
    private String remainNumbers;//剩余号码数量
    private String currentNumber;//当前呼叫的号码

    public String getToalNumbers()
    {
        return toalNumbers;
    }

    public void setToalNumbers(String toalNumbers)
    {
        this.toalNumbers = toalNumbers;
    }

    public String getDealNumbers()
    {
        return dealNumbers;
    }

    public void setDealNumbers(String dealNumbers)
    {
        this.dealNumbers = dealNumbers;
    }

    public String getRemainNumbers()
    {
        return remainNumbers;
    }

    public void setRemainNumbers(String remainNumbers)
    {
        this.remainNumbers = remainNumbers;
    }

    public String getCurrentNumber()
    {
        return currentNumber;
    }

    public void setCurrentNumber(String currentNumber)
    {
        this.currentNumber = currentNumber;
    }

    public boolean getState()
    {
        return state;
    }

    public void setState(boolean s)
    {
        this.state = s;
    }

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        //初始化全局变量
        setState(false);
        setCurrentNumber("无");
        setDealNumbers("0");
        setRemainNumbers("0");
        setToalNumbers("0");
    }
}
