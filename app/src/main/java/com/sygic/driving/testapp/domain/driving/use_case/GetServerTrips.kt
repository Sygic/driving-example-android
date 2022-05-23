package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.domain.driving.model.DrivingTripHeader
import com.sygic.driving.testapp.domain.driving.repository.DrivingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetServerTrips @Inject constructor(
    private val drivingRepository: DrivingRepository
) {
    operator fun invoke(): Flow<Resource<List<DrivingTripHeader>>> {
        return drivingRepository.getServerTripHeaders()
    }
}