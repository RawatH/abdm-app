package org.commcare.dalvik.domain.model

import com.google.gson.annotations.SerializedName

data class PatientConsentModel(
    val id: Int,

    @SerializedName("consent_request_id")
    val consentRequestId: String,

    @SerializedName("date_created")
    val creationDate: String?,

    @SerializedName("last_modified")
    val lastModified: String?,

    @SerializedName("health_info_types")
    val healthInfoType: List<String>?,

    @SerializedName("expiry_date")
    val expiryDate: String?,

    @SerializedName("health_info_from_date")
    val healthInfoFromDate: String?,

    @SerializedName("health_info_to_date")
    val healthInfoToDate: String?,

    @SerializedName("date_granted")
    val grantedDate: String?,

    @SerializedName("date_revoked")
    val revokedDate: String?,

    @SerializedName("date_denied")
    val deniedDate: String?,

    val user: String?,
    val error: Error?,
    val status: String?,
    val details: PatientConsentDetailModel

)