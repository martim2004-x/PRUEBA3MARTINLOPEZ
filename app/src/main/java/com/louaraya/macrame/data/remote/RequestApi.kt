package com.louaraya.macrame.data.remote

import com.louaraya.macrame.data.RequestEntity
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** DTO del microservicio request-service (puerto 8082). */
data class RequestDto(
    val id: Long = 0,
    val userId: Long = 0,
    val clientName: String = "",
    val productId: Long? = null,
    val productName: String? = null,
    val eventType: String = "",
    val widthCm: Int = 0,
    val heightCm: Int = 0,
    val material: String = "",
    val colors: String = "",
    val requiredDate: Long = 0,
    val notes: String = "",
    val status: String = "Pendiente",
    val createdAt: Long = 0
)

interface RequestApi {
    @GET("api/health")
    suspend fun health(): Map<String, String>

    @GET("api/requests")
    suspend fun getRequests(): List<RequestDto>

    @GET("api/requests/{id}")
    suspend fun getRequest(@Path("id") id: Long): RequestDto

    @POST("api/requests")
    suspend fun createRequest(@Body request: RequestDto): RequestDto

    @PUT("api/requests/{id}")
    suspend fun updateRequest(@Path("id") id: Long, @Body request: RequestDto): RequestDto

    @PATCH("api/requests/{id}/status")
    suspend fun updateStatus(@Path("id") id: Long, @Body body: Map<String, String>): RequestDto

    @DELETE("api/requests/{id}")
    suspend fun deleteRequest(@Path("id") id: Long): Map<String, Boolean>
}

fun RequestEntity.toDto(): RequestDto = RequestDto(
    id = id, userId = userId, clientName = clientName, productId = productId,
    productName = productName, eventType = eventType, widthCm = widthCm, heightCm = heightCm,
    material = material, colors = colors, requiredDate = requiredDate, notes = notes,
    status = status, createdAt = createdAt
)

fun RequestDto.toEntity(): RequestEntity = RequestEntity(
    id = id, userId = userId, clientName = clientName.ifBlank { "Cliente" },
    productId = productId, productName = productName, eventType = eventType.ifBlank { "Evento" },
    widthCm = widthCm, heightCm = heightCm,
    material = material.ifBlank { "Algodón natural" }, colors = colors,
    requiredDate = requiredDate, notes = notes,
    status = status.ifBlank { "Pendiente" }, createdAt = createdAt
)
