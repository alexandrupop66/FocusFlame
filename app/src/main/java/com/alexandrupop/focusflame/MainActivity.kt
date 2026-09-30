package com.alexandrupop.focusflame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexandrupop.focusflame.ui.FocusFlameApp
import com.alexandrupop.focusflame.ui.FocusFlameTheme

class MainActivity : ComponentActivity() {
    private val viewModel: FocusViewModel by viewModels {
        val app = application as FocusFlameApplication
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FocusViewModel(app) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(viewModel)
        setContent {
            FocusFlameTheme {
                FocusFlameApp(
                    viewModel = viewModel,
                    activity = this
                )
            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(viewModel)
        super.onDestroy()
    }
}
