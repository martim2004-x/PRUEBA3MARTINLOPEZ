package com.louaraya.macrame

import android.os.SystemClock
import com.louaraya.macrame.data.CampaignEntity
import com.louaraya.macrame.data.CampaignProductRef
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.NotificationEntity
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.data.SessionManager
import com.louaraya.macrame.data.UserEntity
import com.louaraya.macrame.domain.NotificationType
import com.louaraya.macrame.domain.RequestStatus
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.ui.viewmodel.BackendViewModel
import com.louaraya.macrame.ui.viewmodel.CampaignsViewModel
import com.louaraya.macrame.ui.viewmodel.FavoritesViewModel
import com.louaraya.macrame.ui.viewmodel.HomeViewModel
import com.louaraya.macrame.ui.viewmodel.NotificationGroup
import com.louaraya.macrame.ui.viewmodel.NotificationsViewModel
import com.louaraya.macrame.ui.viewmodel.ProductDetailViewModel
import com.louaraya.macrame.ui.viewmodel.ScannerViewModel
import com.louaraya.macrame.ui.viewmodel.ScanOutcome
import com.louaraya.macrame.data.remote.ProductDto
import com.louaraya.macrame.data.remote.RemoteDataSources
import com.louaraya.macrame.data.remote.RequestDto
import com.louaraya.macrame.util.Dates
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Cubre los ViewModels restantes y las acciones de integración de BackendViewModel. */
class RemainingViewModelsTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private fun <T> StateFlow<T>.observeForTest() =
        onEach { }.launchIn(CoroutineScope(mainDispatcher.dispatcher))

    private fun user() = UserEntity(
        id = 1L, fullName = "Valentina Rojas", alias = "vale.rojas", email = "valentina@demo.cl",
        phone = "912345678", passwordHash = "hash", role = Role.CLIENT
    )

    private fun product(id: Long = 1L, category: String = "Matrimonios") = ProductEntity(
        id = id, code = "LAM-%04d".format(id), name = "Arco Ceremonial", description = "Pieza",
        category = category, widthCm = 200, heightCm = 180, material = "Algodón",
        priceClp = 100000, stock = 3
    )

    private fun activeCampaign(id: Long = 1L) = CampaignEntity(
        id = id, name = "Bodas", description = "Colección", theme = "Matrimonios",
        startDate = Dates.todayUtcMillis() - 86_400_000L,
        endDate = Dates.todayUtcMillis() + 86_400_000L, published = true
    )

    private fun commonMocks(
        repository: MacrameRepository,
        session: SessionManager,
        currentUser: UserEntity? = user()
    ) {
        every { session.currentUser } returns MutableStateFlow(currentUser)
        every { repository.observeUnreadCount(any()) } returns MutableStateFlow(2)
        every { repository.observeFavoriteIds(any()) } returns MutableStateFlow(listOf(1L, 2L))
        every { repository.observeFavoriteProducts(any()) } returns MutableStateFlow(
            listOf(product(1), product(2, "Interiores"))
        )
        every { repository.observeActiveProducts() } returns MutableStateFlow(listOf(product(1), product(2, "Interiores")))
        every { repository.observeAllProducts() } returns MutableStateFlow(listOf(product(1)))
        every { repository.observeNotifications(any()) } returns MutableStateFlow(
            listOf(
                NotificationEntity(1, 1L, "Campaña", "Texto", NotificationType.CAMPAIGN),
                NotificationEntity(2, 1L, "Solicitud", "Texto", NotificationType.REQUEST, read = true)
            )
        )
        every { repository.observeCampaigns() } returns MutableStateFlow(listOf(activeCampaign()))
        every { repository.observeCampaignRefs() } returns MutableStateFlow(listOf(CampaignProductRef(1L, 1L)))
    }

    @Test
    fun inicio_expone_usuario_contadores_coleccion_destacada_y_estadisticas() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        commonMocks(repository, session)
        every { repository.observeAllRequests() } returns MutableStateFlow(
            listOf(
                com.louaraya.macrame.data.RequestEntity(
                    1, 1L, "Cliente", 1L, "Arco", "Matrimonio", 100, 100, "Lino", "Crudo",
                    1L, "", RequestStatus.PENDIENTE
                )
            )
        )
        val vm = HomeViewModel(repository, session)
        vm.user.observeForTest()
        vm.unread.observeForTest()
        vm.favoritesCount.observeForTest()
        vm.products.observeForTest()
        vm.featured.observeForTest()
        vm.adminStats.observeForTest()

        assertEquals(1L, vm.user.value?.id)
        assertEquals(2, vm.unread.value)
        assertEquals(2, vm.favoritesCount.value)
        assertEquals(2, vm.products.value.size)
        assertEquals("Bodas", vm.featured.value?.campaign?.name)
        assertEquals(1, vm.featured.value?.products?.size)
        assertEquals(1, vm.adminStats.value.pending)
        assertEquals(1, vm.adminStats.value.activeProducts)
        assertEquals(1, vm.adminStats.value.activeCampaigns)
        assertEquals(1, vm.adminStats.value.pendingRequests.size)
    }

    @Test
    fun inicio_sin_sesion_devuelve_contadores_en_cero() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        commonMocks(repository, session, null)
        every { repository.observeAllRequests() } returns MutableStateFlow(emptyList())
        every { repository.observeCampaigns() } returns MutableStateFlow(emptyList())
        val vm = HomeViewModel(repository, session)
        vm.unread.observeForTest()
        vm.favoritesCount.observeForTest()
        vm.featured.observeForTest()

        assertNull(vm.user.value)
        assertEquals(0, vm.unread.value)
        assertEquals(0, vm.favoritesCount.value)
        assertNull(vm.featured.value)
    }

    @Test
    fun detalle_producto_muestra_favorito_y_puede_alternarlo() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        every { session.currentUser } returns MutableStateFlow(user())
        every { repository.observeProduct(1L) } returns MutableStateFlow(product())
        every { repository.observeFavoriteIds(any()) } returns MutableStateFlow(listOf(1L))
        val vm = ProductDetailViewModel(1L, repository, session)
        vm.state.observeForTest()

        assertEquals("Arco Ceremonial", vm.state.value.product?.name)
        assertTrue(vm.state.value.isFavorite)
        vm.toggleFavorite()
        coVerify { repository.toggleFavorite(1L, 1L) }
    }

    @Test
    fun favoritos_filtran_por_categoria_y_se_pueden_quitar() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        commonMocks(repository, session)
        val vm = FavoritesViewModel(repository, session)
        vm.state.observeForTest()

        assertEquals(2, vm.state.value.total)
        vm.onCategory("Interiores")
        assertEquals(1, vm.state.value.products.size)
        assertEquals("Interiores", vm.state.value.category)
        vm.remove(2L)
        coVerify { repository.toggleFavorite(1L, 2L) }
        vm.onCategory("Interiores") // volver a tocar la misma categoría la desactiva
        assertNull(vm.state.value.category)
    }

    @Test
    fun notificaciones_se_agrupan_se_filtran_y_se_marcan_como_leidas() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        commonMocks(repository, session)
        val vm = NotificationsViewModel(repository, session)
        vm.state.observeForTest()

        assertEquals(2, vm.state.value.items.size)
        assertEquals(1, vm.state.value.unread)
        vm.onGroup(NotificationGroup.REQUESTS)
        assertEquals(1, vm.state.value.items.size)
        vm.markAllRead()
        vm.markRead(2L)
        coVerify { repository.markAllNotificationsRead(1L) }
        coVerify { repository.markNotificationRead(2L) }
        vm.onGroup(NotificationGroup.REQUESTS)
        assertNull(vm.state.value.group)
    }

    @Test
    fun campanas_listan_filas_y_cambian_estado_de_publicacion() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        every { repository.observeCampaigns() } returns MutableStateFlow(listOf(activeCampaign()))
        every { repository.observeCampaignRefs() } returns MutableStateFlow(
            listOf(CampaignProductRef(1L, 1L), CampaignProductRef(1L, 2L))
        )
        val vm = CampaignsViewModel(repository)
        vm.rows.observeForTest()

        assertEquals(1, vm.rows.value.size)
        assertEquals(2, vm.rows.value.first().productCount)
        vm.setPublished(1L, false)
        coVerify { repository.setCampaignPublished(1L, false) }
    }

    @Test
    fun escanner_ignora_lecturas_vacias_y_procesa_codigo_valido() {
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } returns 10_000L
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        every { session.currentUser } returns MutableStateFlow(user())
        coEvery { repository.findProductByCode("LAM-0001") } returns product()
        val vm = ScannerViewModel(repository, session)

        vm.onCode("   ")
        assertNull(vm.outcome.value)
        vm.onCode("https://louaraya.cl/p/LAM-0001")
        assertNotNull(vm.outcome.value)
        assertTrue(vm.outcome.value is ScanOutcome.Found)
        coVerify { repository.logScan(1L, any(), true) }

        vm.consume()
        assertNull(vm.outcome.value)
        vm.onCode("LAM-9999")
        assertTrue(vm.outcome.value is ScanOutcome.NotFound)
        coVerify { repository.logScan(1L, any(), false) }
    }

    @Test
    fun backend_realiza_crud_de_productos_y_solicitudes() {
        val remote = mockk<RemoteDataSources>(relaxed = true)
        coEvery { remote.catalogHealth() } returns Result.success(mapOf("status" to "UP"))
        coEvery { remote.requestHealth() } returns Result.success(mapOf("status" to "UP"))
        coEvery { remote.getProducts() } returns Result.success(listOf(ProductDto(id = 1, priceClp = 1000)))
        coEvery { remote.getRequests() } returns Result.success(
            listOf(RequestDto(id = 2, status = RequestStatus.PENDIENTE))
        )
        coEvery { remote.createProduct(any()) } returns Result.success(ProductDto())
        coEvery { remote.updateProduct(any(), any()) } returns Result.success(ProductDto())
        coEvery { remote.deleteProduct(any()) } returns Result.success(mapOf("deleted" to true))
        coEvery { remote.createRequest(any()) } returns Result.success(RequestDto())
        coEvery { remote.updateRequestStatus(any(), any()) } returns Result.success(RequestDto())
        coEvery { remote.weather() } returns Result.success(
            com.louaraya.macrame.data.remote.WeatherResponse(
                current = com.louaraya.macrame.data.remote.CurrentWeather(18.0, 0),
                units = com.louaraya.macrame.data.remote.WeatherUnits("°C")
            )
        )
        val vm = BackendViewModel(remote)

        vm.createDemoProduct()
        coVerify { remote.createProduct(match { it.code.startsWith("LAM-") }) }
        vm.bumpPrice(ProductDto(id = 1, priceClp = 1000))
        coVerify { remote.updateProduct(1L, match { it.priceClp == 2000 }) }
        vm.deleteProduct(1L)
        coVerify { remote.deleteProduct(1L) }
        vm.createDemoRequest()
        coVerify { remote.createRequest(match { it.clientName == "Cliente API" }) }
        vm.advanceStatus(RequestDto(id = 2, status = RequestStatus.PENDIENTE))
        coVerify { remote.updateRequestStatus(2L, RequestStatus.CONFIRMADO) }
        assertEquals(1, vm.state.value.products.size)
    }
}
