package com.sygic.driving.testapp.data.repository

import com.sygic.driving.testapp.core.auth.utils.AuthBuildHeadersException
import com.sygic.driving.testapp.core.auth.utils.awaitHeaders
import com.sygic.driving.testapp.core.settings.AppSettings
import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.core.utils.addDays
import com.sygic.driving.testapp.data.driving.remote.DrbsApi
import com.sygic.driving.testapp.data.driving.remote.utils.toDrivingTripDetails
import com.sygic.driving.testapp.data.driving.remote.utils.toDrivingTripHeader
import com.sygic.driving.testapp.domain.driving.model.DrivingTripDetails
import com.sygic.driving.testapp.domain.driving.model.DrivingTripHeader
import com.sygic.driving.testapp.domain.driving.repository.DrivingRepository
import com.sygic.lib.auth.Auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.await
import java.util.*

private typealias ListTrips = List<DrivingTripHeader>

class DrivingRepositoryImpl(
    private val drbsApi: DrbsApi,
    private val auth: Auth,
    private val settings: AppSettings
) : DrivingRepository {

    override fun getServerTripHeaders(): Flow<Resource<ListTrips>> = flow {

        emit(Resource.Loading<ListTrips>())

        val now = Date()
        val start = now.addDays(-31)

        val authHeaders = try {
            auth.awaitHeaders()
        } catch (e: AuthBuildHeadersException) {
            e.printStackTrace()

            val message = e.message ?: e.toString()
            emit(Resource.Error<ListTrips>(message = "Auth: $message"))

            return@flow
        }

        try {
            val tripsPage = drbsApi.getTrips(
                authHeaders = authHeaders,
                userId = settings.userId.first(),
                fromDate = start,
                toDate = now
            ).await()

            val trips = tripsPage.trips.map { it.toDrivingTripHeader() }
            emit(Resource.Success(trips))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error<ListTrips>(message = e.message ?: e.toString()))
        }
    }

    override fun getServerTripDetails(id: String): Flow<Resource<DrivingTripDetails>> = flow {
        emit(Resource.Loading<DrivingTripDetails>())

        val authHeaders = try {
            auth.awaitHeaders()
        } catch (e: AuthBuildHeadersException) {
            e.printStackTrace()

            val message = e.message ?: e.toString()
            emit(Resource.Error<DrivingTripDetails>(message = "Auth: $message"))

            return@flow
        }

        try {
            val tripDetailsWrapper = drbsApi.getTripDetails(authHeaders, id).await()
            emit(
                Resource.Success(data = tripDetailsWrapper.tripDetails.toDrivingTripDetails())
            )
        }
        catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error<DrivingTripDetails>(message = e.message ?: e.toString()))
        }
    }
}