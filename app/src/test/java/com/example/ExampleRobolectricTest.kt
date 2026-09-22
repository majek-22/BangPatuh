package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BangPatuh", appName)
  }

  @Test
  fun `read auth placeholder strings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val userPlaceholder = context.getString(R.string.auth_username_placeholder)
    val passPlaceholder = context.getString(R.string.auth_password_placeholder)
    assertEquals("Enter officer username", userPlaceholder)
    assertEquals("Enter security password", passPlaceholder)
  }
}
