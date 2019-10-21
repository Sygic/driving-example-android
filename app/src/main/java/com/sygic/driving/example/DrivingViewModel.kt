package com.sygic.driving.example

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DrivingViewModel: ViewModel() {
    val isInTrip = MutableLiveData<Boolean>(false)
}