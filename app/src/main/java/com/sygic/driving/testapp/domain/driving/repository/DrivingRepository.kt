package com.sygic.driving.testapp.domain.driving.repository

import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.domain.driving.model.DrivingTripDetails
import com.sygic.driving.testapp.domain.driving.model.DrivingTripHeader
import kotlinx.coroutines.flow.Flow

interface DrivingRepository {

    fun getServerTripHeaders(): Flow<Resource<List<DrivingTripHeader>>>

    fun getServerTripDetails(id: String): Flow<Resource<DrivingTripDetails>>
}