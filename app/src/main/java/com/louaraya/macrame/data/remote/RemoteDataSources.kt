package com.louaraya.macrame.data.remote

import com.louaraya.macrame.data.FairEntity

/** Llama a Retrofit y convierte errores de red en Result.failure (la app sigue funcionando offline). */
class RemoteDataSources(private val provider: ApiProvider = ApiProvider()) {

    suspend fun catalogHealth(): Result<Map<String, String>> = safeCall { provider.catalogApi.health() }
    suspend fun requestHealth(): Result<Map<String, String>> = safeCall { provider.requestApi.health() }

    suspend fun getProducts(): Result<List<ProductDto>> = safeCall { provider.catalogApi.getProducts() }
    suspend fun getProduct(id: Long): Result<ProductDto> = safeCall { provider.catalogApi.getProduct(id) }
    suspend fun createProduct(product: ProductDto): Result<ProductDto> =
        safeCall { provider.catalogApi.createProduct(product) }
    suspend fun updateProduct(id: Long, product: ProductDto): Result<ProductDto> =
        safeCall { provider.catalogApi.updateProduct(id, product) }
    suspend fun deleteProduct(id: Long): Result<Map<String, Boolean>> =
        safeCall { provider.catalogApi.deleteProduct(id) }

    /** Crea o actualiza según exista en catalog-service; nunca lanza excepción. */
    suspend fun createOrUpdateProduct(product: ProductDto): Result<ProductDto> {
        if (product.id != 0L) {
            val found = getProduct(product.id)
            if (found.isSuccess) return updateProduct(product.id, product)
        }
        return createProduct(product.copy(id = 0))
    }

    suspend fun getFairs(): Result<List<FairDto>> = safeCall { provider.catalogApi.getFairs() }

    suspend fun getRequests(): Result<List<RequestDto>> = safeCall { provider.requestApi.getRequests() }
    suspend fun createRequest(request: RequestDto): Result<RequestDto> =
        safeCall { provider.requestApi.createRequest(request) }
    suspend fun updateRequest(id: Long, request: RequestDto): Result<RequestDto> =
        safeCall { provider.requestApi.updateRequest(id, request) }
    suspend fun updateRequestStatus(id: Long, status: String): Result<RequestDto> =
        safeCall { provider.requestApi.updateStatus(id, mapOf("status" to status)) }
    suspend fun deleteRequest(id: Long): Result<Map<String, Boolean>> =
        safeCall { provider.requestApi.deleteRequest(id) }

    suspend fun weather(lat: Double = ApiConfig.SANTIAGO_LAT, lon: Double = ApiConfig.SANTIAGO_LON): Result<WeatherResponse> =
        safeCall { provider.weatherApi.currentWeather(lat, lon) }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun FairDto.toEntity(): FairEntity = FairEntity(
    id = id, name = name.ifBlank { "Feria" }, type = type.ifBlank { "Feria" },
    place = place, dateText = dateText, description = description, lat = lat, lng = lng
)

fun FairEntity.toDto(): FairDto = FairDto(
    id = id, name = name, type = type, place = place,
    dateText = dateText, description = description, lat = lat, lng = lng
)
