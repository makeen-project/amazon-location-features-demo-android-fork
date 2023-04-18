package com.aws.amazonlocation

import org.junit.After
import org.junit.Before
import org.mockito.MockitoAnnotations

abstract class BaseTest {

    private lateinit var closeable: AutoCloseable

    @Before
    open fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
    }

    @After
    fun teardown() {
        try {
            closeable.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
