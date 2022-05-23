package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.domain.driving.model.DrivingTripDetails
import com.sygic.driving.testapp.domain.driving.repository.DrivingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetServerTripDetails @Inject constructor(
    private val drivingRepository: DrivingRepository
) {
    operator fun invoke(id: String): Flow<Resource<DrivingTripDetails>> {
        return drivingRepository.getServerTripDetails(id)
    }
}