package com.louaraya.macrame

import com.louaraya.macrame.data.AuthResult
import com.louaraya.macrame.data.CampaignEntity
import com.louaraya.macrame.data.FairEntity
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.data.RegisterInput
import com.louaraya.macrame.data.RequestEntity
import com.louaraya.macrame.data.RequestInput
import com.louaraya.macrame.data.SessionManager
import com.louaraya.macrame.data.UserEntity
import com.louaraya.macrame.domain.RequestStatus
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.ui.viewmodel.AdminProductsViewModel
import com.louaraya.macrame.ui.viewmodel.BackendViewModel
import com.louaraya.macrame.ui.viewmodel.CampaignFormViewModel
import com.louaraya.macrame.ui.viewmodel.CatalogViewModel
import com.louaraya.macrame.ui.viewmodel.LoginViewModel
import com.louaraya.macrame.ui.viewmodel.ProfileViewModel
import com.louaraya.macrame.ui.viewmodel.MapViewModel
import com.louaraya.macrame.ui.viewmodel.ProductFormViewModel
import com.louaraya.macrame.ui.viewmodel.RegisterViewModel
import com.louaraya.macrame.ui.viewmodel.RequestFormViewModel
import com.louaraya.macrame.ui.viewmodel.RequestsViewModel
import com.louaraya.macrame.util.Dates
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Pruebas unitarias de la lógica de los ViewModel (JUnit + MockK), sin emulador. */
class ViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    /** Los StateFlow de la app usan WhileSubscribed: hay que suscribirse en la prueba. */
    private fun <T> StateFlow<T>.observeForTest() =
        onEach { }.launchIn(CoroutineScope(mainDispatcher.dispatcher))

    private fun user(role: String = Role.CLIENT) = UserEntity(
        id = 1L, fullName = "Valentina Rojas", alias = "vale.rojas",
        email = "valentina@demo.cl", phone = "912345678",
        passwordHash = "hash", role = role
    )

    private fun product(id: Long = 1L, name: String = "Arco Ceremonial") = ProductEntity(
        id = id, code = "LAM-%04d".format(id), name = name, description = "Pieza de macramé",
        category = "Matrimonios", widthCm = 200, heightCm = 180,
        material = "Algodón natural", priceClp = 100000, stock = 3
    )

    private fun request(id: Long = 1L, status: String = RequestStatus.PENDIENTE) = RequestEntity(
        id = id, userId = 1L, clientName = "Valentina Rojas", productId = 1L,
        productName = "Arco Ceremonial", eventType = "Matrimonio", widthCm = 200, heightCm = 180,
        material = "Algodón natural", colors = "Crudo", requiredDate = Dates.utcMillisFromNow(30),
        notes = "", status = status
    )

    // ─── Autenticación ───

    @Test
    fun login_valida_correo_antes_de_llamar_al_repositorio() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        val vm = LoginViewModel(repository, session)

        vm.onEmail("correo-invalido")
        vm.onPassword("Demo1234")
        vm.submit()

        assertNotNull(vm.state.value.email.error)
        coVerify(exactly = 0) { repository.login(any(), any(), any()) }
    }

    @Test
    fun login_exitoso_guarda_la_sesion() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        val demo = user()
        coEvery { repository.login("valentina@demo.cl", "Demo1234", Role.CLIENT) } returns AuthResult.Success(demo)
        val vm = LoginViewModel(repository, session)

        vm.onEmail("valentina@demo.cl")
        vm.onPassword("Demo1234")
        vm.submit()

        assertTrue(vm.state.value.loggedIn)
        verify { session.signIn(demo) }
    }

    @Test
    fun login_contrasena_incorrecta_muestra_error_en_el_campo() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        coEvery { repository.login(any(), any(), any()) } returns AuthResult.Error("Contraseña incorrecta", "password")
        val vm = LoginViewModel(repository, session)

        vm.onEmail("valentina@demo.cl")
        vm.onPassword("Mala1234")
        vm.submit()

        assertEquals("Contraseña incorrecta", vm.state.value.password.error)
        verify(exactly = 0) { session.signIn(any()) }
    }

    @Test
    fun registro_valida_y_luego_crea_la_cuenta() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        val demo = user()
        coEvery { repository.register(any()) } returns AuthResult.Success(demo)
        val vm = RegisterViewModel(repository, session)

        vm.onFullName("Valentina")
        vm.onAlias("vale.rojas")
        vm.onEmail("valentina@demo.cl")
        vm.onPhone("912345678")
        vm.onPassword("Demo1234")
        vm.onConfirm("Demo1234")
        vm.onAccepted(true)
        vm.submit()
        assertEquals("Incluye nombre y apellido", vm.state.value.fullName.error)
        coVerify(exactly = 0) { repository.register(any()) }

        vm.onFullName("Valentina Rojas")
        vm.submit()

        assertTrue(vm.state.value.registered)
        coVerify { repository.register(any()) }
        verify { session.signIn(demo) }
    }

    // ─── Perfil ───

    @Test
    fun perfil_carga_datos_y_guarda_cambios() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        every { session.currentUser } returns MutableStateFlow(user())
        coEvery { repository.updateProfile(any(), any(), any(), any(), any(), any()) } returns AuthResult.Success(user())
        val vm = ProfileViewModel(repository, session)

        vm.startEditing()
        assertEquals("Valentina Rojas", vm.state.value.fullName.value)
        vm.onPhone("912345678")
        vm.save()

        assertTrue(vm.state.value.savedCounter > 0)
        assertFalse(vm.state.value.editing)
        coVerify { repository.updateProfile(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun perfil_rechaza_alias_invalido() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        every { session.currentUser } returns MutableStateFlow(user())
        val vm = ProfileViewModel(repository, session)

        vm.startEditing()
        vm.onAlias("alias!")
        vm.save()

        assertNotNull(vm.state.value.alias.error)
        coVerify(exactly = 0) { repository.updateProfile(any(), any(), any(), any(), any(), any()) }
    }

    // ─── Solicitudes ───

    @Test
    fun formulario_solicitud_valida_y_envia_datos_correctos() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        every { session.currentUser } returns MutableStateFlow(user())
        coEvery { repository.createRequest(any(), any()) } returns 42L
        val vm = RequestFormViewModel(1L, repository, session)

        vm.submit()
        assertNotNull(vm.state.value.eventType.error)
        assertNotNull(vm.state.value.width.error)
        coVerify(exactly = 0) { repository.createRequest(any(), any()) }

        vm.onEventType("Matrimonio")
        vm.onWidth("220")
        vm.onHeight("180")
        vm.onMaterial("Algodón natural")
        vm.toggleColor("Crudo")
        vm.onDate(Dates.utcMillisFromNow(30))
        vm.onNotes("Ceremonia en jardín")
        vm.submit()

        assertEquals("SOL-0042", vm.state.value.createdCode)
        coVerify {
            repository.createRequest(
                any(),
                match<RequestInput> { it.widthCm == 220 && it.colors == listOf("Crudo") }
            )
        }
    }

    @Test
    fun solicitud_sin_sesion_muestra_error_general() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        every { session.currentUser } returns MutableStateFlow(null)
        val vm = RequestFormViewModel(null, repository, session)

        vm.onEventType("Matrimonio")
        vm.onWidth("100")
        vm.onHeight("100")
        vm.onMaterial("Lino")
        vm.toggleColor("Crudo")
        vm.onDate(Dates.utcMillisFromNow(10))
        vm.submit()

        assertNotNull(vm.state.value.generalError)
    }

    @Test
    fun lista_solicitudes_admin_filtra_y_cambia_estado() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        every { session.currentUser } returns MutableStateFlow(user(Role.ADMIN))
        every { repository.observeAllRequests() } returns MutableStateFlow(listOf(request(1), request(2, RequestStatus.CONFIRMADO)))
        val vm = RequestsViewModel(repository, session)
        vm.state.observeForTest()

        assertTrue(vm.state.value.isAdmin)
        assertEquals(2, vm.state.value.total)
        vm.onFilter(RequestStatus.PENDIENTE)
        assertEquals(1, vm.state.value.requests.size)
        vm.updateStatus(1L, RequestStatus.CONFIRMADO)

        coVerify { repository.updateRequestStatus(1L, RequestStatus.CONFIRMADO) }
    }

    // ─── Catálogo y administración ───

    @Test
    fun catalogo_filtra_por_texto_y_categoria() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val session = mockk<SessionManager>(relaxed = true)
        every { session.currentUser } returns MutableStateFlow(user())
        every { repository.observeActiveProducts() } returns MutableStateFlow(
            listOf(product(1, "Arco Ceremonial"), product(2, "Tapiz Mural").copy(category = "Interiores"))
        )
        every { repository.observeFavoriteIds(any()) } returns MutableStateFlow(emptyList())
        val vm = CatalogViewModel(repository, session)
        vm.state.observeForTest()

        vm.onQuery("tapiz")
        assertEquals(1, vm.state.value.products.size)
        assertEquals("Tapiz Mural", vm.state.value.products.first().name)

        vm.onQuery("")
        vm.onCategory("Matrimonios")
        assertEquals(1, vm.state.value.products.size)
        vm.toggleFavorite(1L)

        coVerify { repository.toggleFavorite(1L, 1L) }
    }

    @Test
    fun admin_productos_aplica_filtro_de_estado() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val active = product(1).copy(active = true)
        val inactive = product(2, "Pieza oculta").copy(active = false)
        every { repository.observeAllProducts() } returns MutableStateFlow(listOf(active, inactive))
        val vm = AdminProductsViewModel(repository)
        vm.state.observeForTest()

        vm.onFilter(com.louaraya.macrame.ui.viewmodel.ProductFilter.INACTIVE)
        assertEquals(1, vm.state.value.products.size)
        assertEquals("Pieza oculta", vm.state.value.products.first().name)
        vm.onQuery("LAM-0002")
        assertEquals(1, vm.state.value.products.size)
    }

    @Test
    fun formulario_producto_valida_y_guarda() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val vm = ProductFormViewModel(0L, repository)

        vm.save()
        assertNotNull(vm.state.value.name.error)

        vm.onName("Arco Ceremonial")
        vm.onCategory("Matrimonios")
        vm.onWidth("220")
        vm.onHeight("180")
        vm.onMaterial("Algodón natural")
        vm.onPrice("150000")
        vm.onStock("5")
        vm.onDescription("Arco para ceremonias al aire libre")
        vm.save()

        assertTrue(vm.state.value.saved)
        coVerify { repository.saveProduct(any()) }
    }

    @Test
    fun formulario_campania_valida_fechas_y_productos() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        every { repository.observeActiveProducts() } returns MutableStateFlow(listOf(product()))
        val vm = CampaignFormViewModel(0L, repository)

        vm.save()
        assertNotNull(vm.state.value.name.error)
        assertNotNull(vm.state.value.productsError)

        vm.onName("Bodas de ensueño")
        vm.onDescription("Colección para ceremonias de matrimonio")
        vm.onTheme("Bodas y matrimonios")
        vm.onStart(Dates.utcMillisFromNow(1))
        vm.onEnd(Dates.utcMillisFromNow(20))
        vm.toggleProduct(1L)
        vm.save()

        assertTrue(vm.state.value.saved)
        coVerify { repository.saveCampaign(any(), any()) }
    }

    @Test
    fun mapa_aplica_filtro_y_selecciona_feria() {
        val repository = mockk<MacrameRepository>(relaxed = true)
        val fairs = listOf(
            FairEntity(1, "Feria Norte", "Feria", "Parque", "Hoy", "Descripción", -33.4, -70.6),
            FairEntity(2, "Showroom Centro", "Showroom", "Providencia", "Sábado", "Descripción", -33.42, -70.61)
        )
        every { repository.observeFairs() } returns MutableStateFlow(fairs)
        val vm = MapViewModel(repository)
        vm.state.observeForTest()

        vm.onTypeFilter("Showroom")
        assertEquals(1, vm.state.value.fairs.size)
        vm.select(2L)
        assertEquals("Showroom Centro", vm.state.value.selected?.name)
    }

    // ─── Integración de APIs ───

    @Test
    fun backend_expone_estado_de_microservicios_y_api_externa() {
        val remote = mockk<com.louaraya.macrame.data.remote.RemoteDataSources>(relaxed = true)
        coEvery { remote.catalogHealth() } returns Result.success(mapOf("status" to "UP"))
        coEvery { remote.requestHealth() } returns Result.success(mapOf("status" to "UP"))
        coEvery { remote.getProducts() } returns Result.success(
            listOf(com.louaraya.macrame.data.remote.ProductDto(id = 1, name = "Arco", priceClp = 1000))
        )
        coEvery { remote.getRequests() } returns Result.success(emptyList())
        coEvery { remote.weather() } returns Result.success(
            com.louaraya.macrame.data.remote.WeatherResponse(
                current = com.louaraya.macrame.data.remote.CurrentWeather(temperature = 18.0, code = 0),
                units = com.louaraya.macrame.data.remote.WeatherUnits(temperature = "°C")
            )
        )

        val vm = BackendViewModel(remote)

        assertTrue(vm.state.value.catalogOnline)
        assertTrue(vm.state.value.requestsOnline)
        assertEquals(1, vm.state.value.products.size)
        assertTrue(vm.state.value.weather.contains("18°"))
    }
}
