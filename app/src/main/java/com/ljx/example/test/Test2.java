package com.ljx.example.test;

import com.jc.instrument.JCTools;

/**
 * User: ljx
 * Date: 2022/3/22
 * Time: 17:15
 */
public class Test2 {


    public Test2() {
        if (JCTools.performance()) {
            nuiiiiiii(1, 2);
        }
    }

    public boolean nuiiiiiii(int i, int i2) {

        int i3 = Math.abs(i2);

        return i * i2 / i3 == 0;
    }


}
