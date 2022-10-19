package org.commcare.dalvik.domain.model

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import java.lang.Exception

class AbhaDetailModel() {
    var healthIdNumber: String? = null
    var name: String? = null
    var gender: String? = null
    var yearOfBirth: String? = null
    var monthOfBirth: String? = null
    var firstName: String? = null
    var healthId: String? = null
    var lastName: String? = null
    var middleName: String? = null
    var stateCode: String? = null
    var districtCode: String? = null
    var stateName: String? = null
    var districtName: String? = null
    var email: String? = null
    var kycPhoto: String? = null
    var profilePhoto: String? = null
    var mobile: String? = null
    var authMethods: List<String>? = null
    var pincode: String? = null
    lateinit var data: JsonObject


    fun getAadhaarDataList(): List<KeyValueModel> {
        val filters = mutableListOf("kycPhoto", "profilePhoto", "new", "tags")
        val aadhaarDataList = mutableListOf<KeyValueModel>()
        data.keySet().filter {
            !filters.contains(it)
        }.forEach { key ->
            try {
                val value = data[key].run {
                    if(this is JsonNull) {
                        ""
                    }
                    else if(this is JsonArray){
                        var tempValue = ""
                        this.forEach {
                             tempValue +=it.asString +"\n"
                        }
                        tempValue
                    }else{
                        data[key].asString
                    }
                }
                aadhaarDataList.add(
                    KeyValueModel(
                        LanguageManager.getTranslatedValue(key),
                        value
                    )
                )
            } catch (e: Exception) {
                aadhaarDataList.add(KeyValueModel(LanguageManager.getTranslatedValue(key), ""))
            }
        }

        return aadhaarDataList
    }
}