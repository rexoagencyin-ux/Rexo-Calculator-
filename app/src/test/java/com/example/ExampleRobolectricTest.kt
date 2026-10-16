package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CalculatorTab
import com.example.model.GstMode
import com.example.viewmodel.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Rexo Calculator", appName)
  }

  @Test
  fun `verify calculator viewModel initial state`() {
    val viewModel = CalculatorViewModel()
    assertEquals(CalculatorTab.PERCENTAGE, viewModel.activeTab.value)
    assertEquals("INR", viewModel.activeCurrency.value.code)
    assertEquals("₹", viewModel.activeCurrency.value.symbol)
    assertEquals(0, viewModel.history.value.size)
  }

  @Test
  fun `verify number formatting`() {
    assertEquals("1,250.00", CalculatorViewModel.formatAmount(1250.0))
    assertEquals("12.50", CalculatorViewModel.formatAmount(12.5))
    assertEquals("0.00", CalculatorViewModel.formatAmount(0.0))
  }

  @Test
  fun `verify history recording`() {
    val viewModel = CalculatorViewModel()
    viewModel.addHistoryItem("GST", "₹100 + 18%", "₹118")
    assertEquals(1, viewModel.history.value.size)
    assertEquals("GST", viewModel.history.value[0].category)
    assertEquals("₹118", viewModel.history.value[0].result)

    viewModel.clearHistory()
    assertEquals(0, viewModel.history.value.size)
  }
}
