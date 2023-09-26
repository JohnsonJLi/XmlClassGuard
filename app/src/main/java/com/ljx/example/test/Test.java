package com.ljx.example.test;

import android.util.Log;

/**
 * User: ljx
 * Date: 2022/3/22
 * Time: 17:15
 */
public class Test {

//    public boolean nuyahnu13(String str, int i) {
//        long currentTimeMillis = System.currentTimeMillis();
//        Log.i("FULL_STACK", "nuyahnu13( arg1 : " + str + " , arg2 : " + i + " )");
//        if (str == null || str.length() == 0 || i <= 0 || str.length() < i) {
//            return false;
//        }
//        nuiiiiiii(200,234);
//        Log.i("FULL_STACK", "nuyahnu13 executionTime : [" + (System.currentTimeMillis() - currentTimeMillis));
//        return true;
//    }

//    public String uanyuu5(String str, String str2) {
//        long currentTimeMillis = System.currentTimeMillis();
//        Log.i("FULL_STACK", "uanyuu5( arg1 : " + str + " , arg2 : " + str2 + " )");
//        if (str == null || str.length() == 0 || str2 == null || str2.length() == 0) {
//            return null;
//        }
//        String str3 = str + str2;
//        Log.i("FULL_STACK", "uanyuu5 executionTime : [" + (System.currentTimeMillis() - currentTimeMillis));
//        return str3;
//    }

    public void ststste7(String str, int i) {
        long currentTimeMillis = System.currentTimeMillis();
        Log.i("FULL_STACK", "ststste7( arg1 : " + str + " , arg2 : " + i + " )");
        if (str == null || str.length() == 0) {
            throw new NullPointerException("Parameter NullPointerException");
        }
        if (i <= 0) {
            throw new RuntimeException("Parameter Exceptions");
        }
        if (str.length() * i == 0) {
            throw new RuntimeException("Parameter Exceptions");
        }
        Log.i("FULL_STACK", "ststste7 executionTime : " + (System.currentTimeMillis() - currentTimeMillis) + " ms");
    }

    public void ststste(String str, int i) {
        long currentTimeMillis = System.currentTimeMillis();
        Log.i("FULL_STACK", "ststste7( arg1 : " + str + " , arg2 : " + i + " )");
        if (str == null || str.length() == 0) {
            throw new NullPointerException("Parameter NullPointerException");
        }
        if (i <= 0) {
            throw new RuntimeException("Parameter Exceptions");
        }
        if (str.length() < i) {
            throw new RuntimeException("Parameter Exceptions");
        }
        Log.i("FULL_STACK", "ststste7 executionTime : " + (System.currentTimeMillis() - currentTimeMillis) + " ms");
    }


    public boolean nuiiiiiii(int i, int i2) {
        return i * i2 / 2 == 0;
    }

    public boolean nuzzzzzz(boolean i, boolean i2) {
        return  i && i2;
    }


    public boolean nuzzz() {
        return  false;
    }
    public void vvvv() {
    }

//    public void uhayauu8(String str, int i) {
//        long currentTimeMillis = System.currentTimeMillis();
//        Log.i("FULL_STACK", "uhayauu8( arg1 : " + str + " , arg2 : " + i + " )");
//        if (str == null || str.length() == 0) {
//            throw new NullPointerException("Parameter NullPointerException");
//        }
//        if (i <= 0) {
//            throw new RuntimeException("Parameter Exceptions");
//        }
//        if (str.length() < i) {
//            throw new RuntimeException("Parameter Exceptions");
//        }
//        Log.i("FULL_STACK", "uhayauu8 executionTime : " + (System.currentTimeMillis() - currentTimeMillis));
//    }


}
