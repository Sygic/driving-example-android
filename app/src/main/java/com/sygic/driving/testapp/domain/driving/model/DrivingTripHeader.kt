package com.sygic.driving.testapp.domain.driving.model

import com.sygic.driving.trips.*
import java.util.*

enum class DrivingTripStorage {
    Local, Server
}

enum class DrivingTripHeaderStatus {
    None, Success, Error
}

data class DrivingTripHeader(
    val id: String,
    val startTime: Date,
    val endTime: Date,
    val status: DrivingTripHeaderStatus,
    val storage: DrivingTripStorage
)
//
//data class LocalTripHeader(
//    val tripRecord: TripRecord,
//    val storage: TripStorage,
//    val startTime: Date,
//    val endTime: Date,
//    val startReason: TripStartReason?,
//    val endReason: TripEndReason?,
//    val uploadStatus: TripUploadStatus
//)


//
//fun TripHeader.toLocalTripHeader(): LocalTripHeader {
//    return LocalTripHeader(
//        tripRecord = tripRecord,
//        storage = TripStorage.Local,
//        startTime = startTime,
//        endTime = endTime,
//        startReason = startReason,
//        endReason = endReason,
//        uploadStatus = uploadStatus
//    )
//}