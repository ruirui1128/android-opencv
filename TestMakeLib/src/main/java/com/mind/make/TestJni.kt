package com.mind.make

/**
 * create by Rui on 2023-10-17
 * desc:
 */
class TestJni {
    companion object {
        init {
            System.loadLibrary("testcmake")
        }

        val jni by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { TestJni() }

    }


   external fun getStringFromJni():String
}