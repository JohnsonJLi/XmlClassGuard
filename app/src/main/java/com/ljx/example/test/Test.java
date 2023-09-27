package com.ljx.example.test;

import android.util.Log;

/**
 * User: ljx
 * Date: 2022/3/22
 * Time: 17:15
 */
public class Test extends Test2 {

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

    private String mJuCoFrom = "def_from";

    public Test(String from,boolean bf,int i) {
        this.mJuCoFrom = from;
        if (bf) this.mJuCoFrom = getClass().getSimpleName();
        try {
            ttttst0(609);
            ttttst2(mJuCoFrom, 123);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Test(String from,boolean bf) {
        this.mJuCoFrom = from;
        if (bf) this.mJuCoFrom = getClass().getSimpleName();
        try {
            ttttst0(609);
            ttttst2(mJuCoFrom, 123);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean ttttst0(double i) {
        double y = Math.sqrt(i);
        System.out.println("Square root of PI is: " + y);
        ttttst2(mJuCoFrom, 123);
        return true;
    }

    public boolean ttttst2(String str, int i) {
        long currentTimeMillis = System.currentTimeMillis();
        Log.i("FULL_STACK", "ttttst0( arg1 : " + str + " , arg2 : " + i + " )");
        if (str == null || str.length() == 0 || i <= 0 || str.length() < i) {
            return false;
        }
        int i2 = 202;
        ttttst0(i2);
        Log.i("FULL_STACK", "ttttst0 executionTime : [" + (System.currentTimeMillis() - currentTimeMillis) + " ] ms");

        return true;
    }


    public boolean nuiiiiiii(int i, int i2) {
        return i * i2 / 2 == 0;
    }

    public boolean nuzzzzzz(boolean i, boolean i2) {
        return i && i2;
    }


    public boolean nuzzz() {
        return false;
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
