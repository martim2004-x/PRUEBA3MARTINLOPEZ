package com.louaraya.macrame.data.remote

import com.louaraya.macrame.data.ProductEntity
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** DTO del microservicio catalog-service (puerto 8081). */
data class ProductDto(
    val id: Long = 0,
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val widthCm: Int = 0,
    val heightCm: Int = 0,
    val material: String = "",
    val priceClp: Int = 0,
    val stock: Int = 0,
    val madeToOrder: Boolean = false,
    val active: Boolean = true,
    val imageUri: String? = null,
    val createdAt: Long = 0
)

data class FairDto(
    val id: Long = 0,
    val name: String = "",
    val type: String = "",
    val place: String = "",
    val dateText: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

interface CatalogApi {
    @GET("api/health")
    suspend fun health(): Map<String, String>

    @GET("api/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: Long): ProductDto

    @POST("api/products")
    suspend fun createProduct(@Body product: ProductDto): ProductDto

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: ProductDto): ProductDto

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Map<String, Boolean>

    @GET("api/fairs")
    suspend fun getFairs(): List<FairDto>
}

fun ProductEntity.toDto(): ProductDto = ProductDto(
    id = id, code = code, name = name, description = description, category = category,
    widthCm = widthCm, heightCm = heightCm, material = material, priceClp = priceClp,
    stock = stock, madeToOrder = madeToOrder, active = active,
    imageUri = imageUri, createdAt = createdAt
)

fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    id = id,
    code = code.ifBlank { "LAM-%04d".format(id) },
    name = name.ifBlank { "Producto remoto" },
    description = description,
    category = category.ifBlank { "Interiores" },
    widthCm = widthCm,
    heightCm = heightCm,
    material = material.ifBlank { "Algodón natural" },
    priceClp = priceClp,
    stock = stock,
    madeToOrder = madeToOrder,
    active = active,
    imageUri = imageUri,
    createdAt = createdAt
)
