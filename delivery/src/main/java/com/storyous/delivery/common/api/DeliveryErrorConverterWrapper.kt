package com.storyous.delivery.common.api

import com.storyous.delivery.common.BuildConfig
import com.storyous.delivery.common.api.model.DeliveryErrorResponse
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import timber.log.Timber

class DeliveryErrorConverterWrapper(
    private val converter: Converter<ResponseBody, DeliveryErrorResponse>
) {
    companion object {
        var INSTANCE: DeliveryErrorConverterWrapper? = null
    }

    /**
     * Convert [Throwable] to response containing all response data
     */
    fun convertPosError(e: Throwable): DeliveryErrorResponse {
        return if (e is HttpException) {
            convertPosError(e.response()?.errorBody())
                ?: DeliveryErrorResponse(e.message(), e.message(), e.code(), e.code())
        } else {
            // todo implement other types like timeout etc.
            Timber.e(e, "DeliveryErrorConverterWrapper: non-http exception")
            // printStackTrace for easier test debugging
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            DeliveryErrorResponse.UNKNOWN_ERROR
        }
    }

    /**
     * Convert error [ResponseBody] to response containing all response data
     */
    fun convertPosError(body: ResponseBody?): DeliveryErrorResponse? {
        return runCatching { body?.let { converter.convert(body) } }
            .getOrElse {
                Timber.e(it, "DeliveryErrorConverterWrapper: Malformed JSON: ${body?.string()}")
                null
            }
    }
}
