package com.storyous.bills.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BillsApi {

    @GET("/bills/{merchant}-{placeId}/{billId}")
    suspend fun loadMenu(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Query("billId") billId: String
    ): BillWithItems

    @Suppress("LongParameterList")
    @GET("/bills/{merchant}-{placeId}")
    suspend fun loadTimedMenu(
        @Path("merchantId") merchantId: String,
        @Path("placeId") placeId: String,
        @Query("from") fromDate: String? = null,
        @Query("till") tillDate: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("lastBillId") lastBillId: String? = null,
        @Query("modifiedSince") modifiedSinceDate: String? = null,
        @Query("refunded") refunded: Boolean = true
    ): BillsResponse
}
