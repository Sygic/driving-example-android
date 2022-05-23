package com.sygic.driving.testapp.core.utils

import androidx.navigation.NavDirections
import java.io.File

sealed class UiEvent {
    data class NavigateTo(val navDirections: NavDirections): UiEvent()
    data class ShowToast(val resId: Int): UiEvent()
    object PopBackStack: UiEvent()
    data class ShareFiles(val files: List<File>): UiEvent()
}