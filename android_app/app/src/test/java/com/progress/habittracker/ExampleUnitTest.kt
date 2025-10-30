package com.progress.habittrackerpackage com.example.android_app



import org.junit.Testimport org.junit.Test



import org.junit.Assert.*import org.junit.Assert.*



/**/**

 * Példa Unit teszt - Ez a fejlesztői gépen fut (host) * Example local unit test, which will execute on the development machine (host).

 *  *

 * További információ: [testing documentation](http://d.android.com/tools/testing). * See [testing documentation](http://d.android.com/tools/testing).

 */ */

class ExampleUnitTest {class ExampleUnitTest {

    /**    @Test

     * Egyszerű teszt az összeadás működésének ellenőrzésére    fun addition_isCorrect() {

     */        assertEquals(4, 2 + 2)

    @Test    }

    fun addition_isCorrect() {}
        assertEquals(4, 2 + 2)
    }
}
