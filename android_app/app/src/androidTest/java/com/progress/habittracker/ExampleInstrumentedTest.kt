package com.progress.habittrackerpackage com.example.android_app



import androidx.test.platform.app.InstrumentationRegistryimport androidx.test.platform.app.InstrumentationRegistry

import androidx.test.ext.junit.runners.AndroidJUnit4import androidx.test.ext.junit.runners.AndroidJUnit4



import org.junit.Testimport org.junit.Test

import org.junit.runner.RunWithimport org.junit.runner.RunWith



import org.junit.Assert.*import org.junit.Assert.*



/**/**

 * Instrumented teszt - Ez Android eszközön vagy emulatoron fut * Instrumented test, which will execute on an Android device.

 *  *

 * További információ: [testing documentation](http://d.android.com/tools/testing). * See [testing documentation](http://d.android.com/tools/testing).

 */ */

@RunWith(AndroidJUnit4::class)@RunWith(AndroidJUnit4::class)

class ExampleInstrumentedTest {class ExampleInstrumentedTest {

    /**    @Test

     * Teszt az alkalmazás kontextusához    fun useAppContext() {

     * Ellenőrzi, hogy a package név helyes-e        // Context of the app under test.

     */        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test        assertEquals("com.example.android_app", appContext.packageName)

    fun useAppContext() {    }

        // Az alkalmazás kontextusa amit tesztelünk}
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.progress.habittracker", appContext.packageName)
    }
}
