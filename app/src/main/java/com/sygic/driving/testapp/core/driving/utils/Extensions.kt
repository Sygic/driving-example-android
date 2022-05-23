package com.sygic.driving.testapp.core.driving.utils

import com.sygic.driving.testapp.domain.driving.model.DrivingTripHeader
import com.sygic.driving.testapp.domain.driving.model.DrivingTripHeaderStatus
import com.sygic.driving.testapp.domain.driving.model.DrivingTripStorage
import com.sygic.driving.trips.TripHeader
import com.sygic.driving.trips.TripUploadStatus

fun TripHeader.toDrivingTripHeader(): DrivingTripHeader {
    return DrivingTripHeader(
        id = tripRecord.fileName,
        startTime = startTime,
        endTime = endTime,
        status = uploadStatus.toDrivingTripHeaderStatus(),
        storage = DrivingTripStorage.Local
    )
}

fun TripUploadStatus.toDrivingTripHeaderStatus(): DrivingTripHeaderStatus {
    return when(this) {
        is TripUploadStatus.Uploaded -> DrivingTripHeaderStatus.Success
        is TripUploadStatus.UploadFailed -> DrivingTripHeaderStatus.Error
        else -> DrivingTripHeaderStatus.None
    }
}