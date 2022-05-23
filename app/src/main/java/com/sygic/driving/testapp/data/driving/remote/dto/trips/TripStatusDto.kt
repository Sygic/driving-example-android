package com.sygic.driving.testapp.data.driving.remote.dto.trips

import com.google.gson.annotations.SerializedName

enum class TripStatusDto {
    @SerializedName("waitingForEvaluation") WaitingForEvaluation,
    @SerializedName("evaluated") Evaluated,
    @SerializedName("invalid") Invalid,
    @SerializedName("error") Error
}