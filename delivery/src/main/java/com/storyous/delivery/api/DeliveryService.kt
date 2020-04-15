package com.storyous.storyouspay.api

import com.storyous.delivery.common.api.model.BaseDataResponse
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.api.model.RequestDeclineBody
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

interface DeliveryService {

    @GET("/deliveryInternal/{merchantId}-{placeId}/orders")
    @Throws(HttpException::class, IOException::class)
    suspend fun getDeliveryOrdersAsync(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Query("modifiedSince") modifiedSince: String?
    ): BaseDataResponse<List<DeliveryOrder>>

    @POST("/deliveryInternal/{merchantId}-{placeId}/orders/{orderId}/confirm")
    @Throws(HttpException::class, IOException::class)
    suspend fun confirmDeliveryOrderAsync(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Path("orderId") orderId: String
    ): DeliveryOrder

    @POST("/deliveryInternal/{merchantId}-{placeId}/orders/{orderId}/decline")
    @Throws(HttpException::class, IOException::class)
    suspend fun declineDeliveryOrderAsync(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Path("orderId") orderId: String,
        @Body body: RequestDeclineBody
    ): DeliveryOrder

    @POST("/deliveryInternal/{merchantId}-{placeId}/orders/{orderId}/dispatch")
    @Throws(HttpException::class, IOException::class)
    suspend fun notifyOrderDispatched(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Path("orderId") orderId: String
    ): DeliveryOrder
}
