package com.louaraya.macrame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.louaraya.macrame.data.remote.ProductDto
import com.louaraya.macrame.data.remote.RemoteDataSources
import com.louaraya.macrame.data.remote.RequestDto
import com.louaraya.macrame.data.remote.WeatherResponse
import com.louaraya.macrame.data.remote.display
import com.louaraya.macrame.domain.RequestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApiProductFormState(
    val name: String = "",
    val price: String = "",
    val category: String = "Interiores",
    val stock: String = "5",
    val description: String = ""
)

data class ApiRequestFormState(
    val clientName: String = "Cliente API",
    val eventType: String = "Matrimonio",
    val widthCm: String = "150",
    val heightCm: String = "120",
    val material: String = "Algodón natural",
    val colors: String = "Crudo,Salvia"
)

data class BackendUiState(
    val catalogOnline: Boolean = false,
    val requestsOnline: Boolean = false,
    val weather: String = "Consultando clima…",
    val products: List<ProductDto> = emptyList(),
    val requests: List<RequestDto> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val productForm: ApiProductFormState = ApiProductFormState(),
    val requestForm: ApiRequestFormState = ApiRequestFormState()
)

/** Estado de la pantalla que demuestra la integración con los microservicios y la API externa. */
class BackendViewModel(private val remote: RemoteDataSources) : ViewModel() {
    private val _state = MutableStateFlow(BackendUiState())
    val state: StateFlow<BackendUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { refreshSuspend() }
    }

    private suspend fun refreshSuspend() {
        _state.update { it.copy(loading = true, error = null, message = null) }

        val catalogOk = remote.catalogHealth().isSuccess
        val requestsOk = remote.requestHealth().isSuccess
        val products = remote.getProducts().getOrDefault(emptyList())
        val requests = remote.getRequests().getOrDefault(emptyList())
        val weather = remote.weather().getOrNull()?.display() ?: "Clima no disponible (sin conexión)"

        _state.update {
            it.copy(
                catalogOnline = catalogOk, requestsOnline = requestsOk,
                products = products, requests = requests, weather = weather,
                loading = false,
                error = if (!catalogOk || !requestsOk)
                    "Los microservicios no responden. Revisa que estén encendidos en 8081 y 8082."
                else null
            )
        }
    }

    private fun nextProductCode(existing: List<ProductDto>): String {
        val used = existing.map { it.code }.toSet()
        var number = (existing.mapNotNull { it.code.removePrefix("LAM-").toIntOrNull() }.maxOrNull() ?: 0) + 1
        while ("LAM-%04d".format(number) in used) number++
        return "LAM-%04d".format(number)
    }

    /** CREATE desde la interfaz (botón rápido). */
    fun createDemoProduct() {
        action("Producto de prueba creado en catalog-service") {
            val code = nextProductCode(_state.value.products)
            val product = ProductDto(
                code = code,
                name = "Pieza API ${code.removePrefix("LAM-")}",
                description = "Producto creado desde la app con Retrofit.",
                category = "Interiores", widthCm = 80, heightCm = 60,
                material = "Algodón natural", priceClp = 45000, stock = 5, active = true
            )
            remote.createProduct(product)
        }
    }

    /** CREATE desde el formulario de la pestaña API. */
    fun createProductFromForm() {
        val form = _state.value.productForm
        val name = form.name.trim()
        val price = form.price.trim().toIntOrNull()
        val stock = form.stock.trim().toIntOrNull() ?: 0

        if (name.isBlank()) return showError("Escribe el nombre del producto.")
        if (price == null || price <= 0) return showError("Escribe un precio válido (número mayor a 0).")

        val code = nextProductCode(_state.value.products)
        val product = ProductDto(
            code = code,
            name = name,
            description = form.description.trim().ifBlank { "Producto creado desde la pestaña API." },
            category = form.category.trim().ifBlank { "Interiores" },
            widthCm = 80,
            heightCm = 60,
            material = "Algodón natural",
            priceClp = price,
            stock = stock,
            active = true
        )
        action("Producto «$name» creado en catalog-service ($code)", onSuccess = {
            _state.update { it.copy(productForm = ApiProductFormState()) }
        }) {
            remote.createProduct(product)
        }
    }

    fun setProductName(value: String) =
        _state.update { it.copy(productForm = it.productForm.copy(name = value)) }

    fun setProductPrice(value: String) =
        _state.update { it.copy(productForm = it.productForm.copy(price = value)) }

    fun setProductCategory(value: String) =
        _state.update { it.copy(productForm = it.productForm.copy(category = value)) }

    fun setProductStock(value: String) =
        _state.update { it.copy(productForm = it.productForm.copy(stock = value)) }

    fun setProductDescription(value: String) =
        _state.update { it.copy(productForm = it.productForm.copy(description = value)) }

    /** UPDATE desde la interfaz (sube fijo 1000). */
    fun bumpPrice(product: ProductDto) {
        action("Precio actualizado en catalog-service") {
            remote.updateProduct(product.id, product.copy(priceClp = product.priceClp + 1000))
        }
    }

    /** EDITAR PRECIO MANUALMENTE desde la interfaz. */
    fun updateProductPrice(product: ProductDto, newPrice: Int) {
        action("Precio actualizado a $newPrice CLP") {
            remote.updateProduct(product.id, product.copy(priceClp = newPrice))
        }
    }

    /** DELETE desde la interfaz. */
    fun deleteProduct(id: Long) {
        action("Producto eliminado en catalog-service") { remote.deleteProduct(id) }
    }

    /** UPDATE de estado desde la interfaz (PATCH). */
    fun advanceStatus(request: RequestDto) {
        val next = when (request.status) {
            RequestStatus.PENDIENTE -> RequestStatus.CONFIRMADO
            RequestStatus.CONFIRMADO -> RequestStatus.ENTREGADO
            else -> RequestStatus.PENDIENTE
        }
        action("Solicitud actualizada en request-service") {
            remote.updateRequestStatus(request.id, next)
        }
    }

    /** CREATE de solicitud desde la interfaz (botón rápido). */
    fun createDemoRequest() {
        action("Solicitud de prueba creada en request-service") {
            remote.createRequest(
                RequestDto(
                    userId = 2L, clientName = "Cliente API", eventType = "Matrimonio",
                    widthCm = 150, heightCm = 120, material = "Algodón natural",
                    colors = "Crudo,Salvia", requiredDate = System.currentTimeMillis(),
                    notes = "Solicitud creada desde la app con Retrofit.",
                    status = RequestStatus.PENDIENTE
                )
            )
        }
    }

    /** CREATE de solicitud desde el formulario de la pestaña API. */
    fun createRequestFromForm() {
        val form = _state.value.requestForm
        val width = form.widthCm.trim().toIntOrNull()
        val height = form.heightCm.trim().toIntOrNull()
        if (width == null || width <= 0) return showError("Escribe un ancho válido en cm.")
        if (height == null || height <= 0) return showError("Escribe un alto válido en cm.")

        val request = RequestDto(
            userId = 2L,
            clientName = form.clientName.trim().ifBlank { "Cliente API" },
            eventType = form.eventType.trim().ifBlank { "Matrimonio" },
            widthCm = width,
            heightCm = height,
            material = form.material.trim().ifBlank { "Algodón natural" },
            colors = form.colors.trim().ifBlank { "Crudo" },
            requiredDate = System.currentTimeMillis(),
            notes = "Solicitud creada desde la pestaña API.",
            status = RequestStatus.PENDIENTE
        )
        action("Solicitud creada en request-service", onSuccess = {
            _state.update { it.copy(requestForm = ApiRequestFormState()) }
        }) {
            remote.createRequest(request)
        }
    }

    fun setRequestClient(value: String) =
        _state.update { it.copy(requestForm = it.requestForm.copy(clientName = value)) }

    fun setRequestEvent(value: String) =
        _state.update { it.copy(requestForm = it.requestForm.copy(eventType = value)) }

    fun setRequestWidth(value: String) =
        _state.update { it.copy(requestForm = it.requestForm.copy(widthCm = value)) }

    fun setRequestHeight(value: String) =
        _state.update { it.copy(requestForm = it.requestForm.copy(heightCm = value)) }

    fun setRequestMaterial(value: String) =
        _state.update { it.copy(requestForm = it.requestForm.copy(material = value)) }

    fun setRequestColors(value: String) =
        _state.update { it.copy(requestForm = it.requestForm.copy(colors = value)) }

    private fun showError(message: String) {
        _state.update { it.copy(error = message, message = null, loading = false) }
    }

    private fun action(
        successMessage: String,
        onSuccess: () -> Unit = {},
        block: suspend () -> Result<*>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null, error = null) }
            val result = runCatching { block() }
            val remoteResult = result.getOrNull()
            when {
                remoteResult is Result<*> && remoteResult.isFailure -> _state.update {
                    it.copy(
                        loading = false,
                        error = remoteResult.exceptionOrNull()?.message ?: "No se pudo completar la operación"
                    )
                }
                result.isFailure -> _state.update {
                    it.copy(loading = false, error = result.exceptionOrNull()?.message ?: "Error inesperado")
                }
                else -> {
                    refreshSuspend()
                    onSuccess()
                    _state.update { it.copy(message = successMessage) }
                }
            }
        }
    }
}

/** Expuesto para pruebas unitarias del mapeo de la API externa. */
fun weatherText(response: WeatherResponse): String = response.display()
