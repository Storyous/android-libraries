package com.storyous.bills.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BillsApi {

    @GET("/bills/{merchantId}-{placeId}/{billId}")
    suspend fun loadBillDetail(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Path("billId") billId: String
    ): BillWithItems

    @Suppress("LongParameterList")
    @GET("/bills/{merchantId}-{placeId}")
    suspend fun loadBills(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Query("from") fromDate: String? = null,
        @Query("till") tillDate: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("lastBillId") lastBillId: String? = null,
        @Query("modifiedSince") modifiedSinceDate: String? = null,
        @Query("refunded") refunded: Boolean = true
    ): BillsResponse
    
    @POST("/bills/{merchantId}-{placeId}/{billId}/refund")
    suspend fun refund(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Path("billId") billId: String,
        @Body body: RefundBillRequest
    ): BillWithItems

    @PATCH("/bills/{merchantId}-{placeId}/{billId}")
    suspend fun updateBillData(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Path("billId") billId: String,
        @Body body: UpdateBillRequest
    )
}
