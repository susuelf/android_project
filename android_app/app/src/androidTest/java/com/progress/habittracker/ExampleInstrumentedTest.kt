package com.progress.habittracker

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test, amely Android eszközön fut.
 *
 * Dokumentáció: [testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    
    /**
     * Ellenőrzi, hogy az alkalmazás context megfelelő package névvel rendelkezik.
     */
    @Test
    fun useAppContext() {
        // Az alkalmazás context
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.progress.habittracker", appContext.packageName)
    }
}