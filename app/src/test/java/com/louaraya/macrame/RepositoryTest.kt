package com.louaraya.macrame

import androidx.room.withTransaction
import com.louaraya.macrame.data.AppDatabase
import com.louaraya.macrame.data.AuthResult
import com.louaraya.macrame.data.CampaignDao
import com.louaraya.macrame.data.CampaignEntity
import com.louaraya.macrame.data.FavoriteDao
import com.louaraya.macrame.data.FairDao
import com.louaraya.macrame.data.FairEntity
import com.louaraya.macrame.data.MacrameRepository
import com.louaraya.macrame.data.NotificationDao
import com.louaraya.macrame.data.ProductDao
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.data.RegisterInput
import com.louaraya.macrame.data.RequestDao
import com.louaraya.macrame.data.RequestEntity
import com.louaraya.macrame.data.RequestInput
import com.louaraya.macrame.data.ScanLogDao
import com.louaraya.macrame.data.UserDao
import com.louaraya.macrame.data.UserEntity
import com.louaraya.macrame.data.remote.ProductDto
import com.louaraya.macrame.data.remote.RemoteDataSources
import com.louaraya.macrame.data.remote.RequestDto
import com.louaraya.macrame.domain.RequestStatus
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.util.PasswordHasher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas unitarias de MacrameRepository con DAOs simulados (sin emulador ni base de datos real). */
class RepositoryTest {

    private class Fixture(private val remote: RemoteDataSources? = null) {
        val db = mockk<AppDatabase>(relaxed = true)
        val users = mockk<UserDao>(relaxed = true)
        val products = mockk<ProductDao>(relaxed = true)
        val favorites = mockk<FavoriteDao>(relaxed = true)
        val requests = mockk<RequestDao>(relaxed = true)
        val notifications = mockk<NotificationDao>(relaxed = true)
        val fairs = mockk<FairDao>(relaxed = true)
        val campaigns = mockk<CampaignDao>(relaxed = true)
        val scans = mockk<ScanLogDao>(relaxed = true)

        init {
            mockkStatic("androidx.room.RoomDatabaseKt")
            coEvery { db.withTransaction<Any>(any<suspend () -> Any>()) } coAnswers {
                // En una extensión simulada, args[0] es el receptor (db) y args[1] es el bloque.
                arg<suspend () -> Any>(1).invoke()
            }
            every { db.userDao() } returns users
            every { db.productDao() } returns products
            every { db.favoriteDao() } returns favorites
            every { db.requestDao() } returns requests
            every { db.notificationDao() } returns notifications
            every { db.fairDao() } returns fairs
            every { db.campaignDao() } returns campaigns
            every { db.scanLogDao() } returns scans
        }

        fun repository() = MacrameRepository(db, remote)
    }

    private fun user(
        id: Long = 1L,
        role: String = Role.CLIENT,
        password: String = "Demo1234",
        alias: String = "vale.rojas"
    ) = UserEntity(
        id = id, fullName = "Valentina Rojas", alias = alias, email = "valentina@demo.cl",
        phone = "912345678", passwordHash = PasswordHasher.hash(password), role = role
    )

    private fun product(id: Long = 1L) = ProductEntity(
        id = id, code = "LAM-%04d".format(id), name = "Arco Ceremonial", description = "Pieza de macramé",
        category = "Matrimonios", widthCm = 200, heightCm = 180, material = "Algodón natural",
        priceClp = 100000, stock = 3
    )

    private fun request(id: Long = 1L, status: String = RequestStatus.PENDIENTE) = RequestEntity(
        id = id, userId = 1L, clientName = "Valentina Rojas", productId = 1L,
        productName = "Arco Ceremonial", eventType = "Matrimonio", widthCm = 200, heightCm = 180,
        material = "Algodón natural", colors = "Crudo", requiredDate = 1000L, notes = "",
        status = status
    )

    private fun requestInput() = RequestInput(
        product = product(), eventType = "Matrimonio", widthCm = 200, heightCm = 180,
        material = "Algodón natural", colors = listOf("Crudo", "Salvia"),
        requiredDate = 1000L, notes = "  Ceremonia en jardín  "
    )

    private fun campaign(id: Long = 1L, published: Boolean = true) = CampaignEntity(
        id = id, name = "Bodas de ensueño", description = "Colección de matrimonios",
        theme = "Matrimonios", startDate = 100L, endDate = 200L, published = published
    )

    // ─── Usuarios ───

    @Test
    fun login_valida_correo_contrasena_y_rol() = runTest {
        val f = Fixture()
        val demo = user()
        coEvery { f.users.findByEmail("valentina@demo.cl") } returns demo
        coEvery { f.users.findByEmail("otro@demo.cl") } returns null
        val repo = f.repository()

        assertEquals(AuthResult.Success(demo), repo.login(" valentina@demo.cl ", "Demo1234", Role.CLIENT))
        assertTrue(repo.login("otro@demo.cl", "Demo1234", Role.CLIENT) is AuthResult.Error)
        assertTrue(repo.login("valentina@demo.cl", "Mala1234", Role.CLIENT) is AuthResult.Error)
        assertTrue(repo.login("valentina@demo.cl", "Demo1234", Role.ADMIN) is AuthResult.Error)
    }

    @Test
    fun registro_rechaza_duplicados_y_crea_usuario_con_notificacion() = runTest {
        val f = Fixture()
        val input = RegisterInput(
            "Valentina Rojas", "vale.rojas", "valentina@demo.cl", "912345678",
            "Demo1234", "Los Leones 1234", 0
        )
        val demo = user()
        coEvery { f.users.findByEmail(any()) } returns null
        coEvery { f.users.findByAlias(any()) } returns null
        coEvery { f.users.insert(any()) } returns 1L
        coEvery { f.users.findById(1L) } returns demo
        val repo = f.repository()

        assertEquals(AuthResult.Success(demo), repo.register(input))
        coVerify { f.notifications.insert(match { it.userId == 1L && it.title.contains("Bienvenida") }) }

        coEvery { f.users.findByEmail(any()) } returns demo
        assertTrue(repo.register(input) is AuthResult.Error)
        coEvery { f.users.findByEmail(any()) } returns null
        coEvery { f.users.findByAlias(any()) } returns demo
        assertTrue(repo.register(input) is AuthResult.Error)
    }

    @Test
    fun perfil_actualiza_datos_y_rechaza_alias_ajeno() = runTest {
        val f = Fixture()
        val demo = user()
        coEvery { f.users.findById(1L) } returns demo
        coEvery { f.users.findByAlias("nuevo.alias") } returns null
        coEvery { f.users.findByAlias("vale.rojas") } returns demo.copy(id = 2L)
        val repo = f.repository()

        val ok = repo.updateProfile(1L, "  Valentina Rojas ", "nuevo.alias", "912345678", "Dirección 1", 2)
        assertTrue(ok is AuthResult.Success)
        assertEquals("Valentina Rojas", (ok as AuthResult.Success).user.fullName)
        coVerify { f.users.update(any()) }

        val duplicated = repo.updateProfile(1L, "Valentina Rojas", "vale.rojas", "912345678", "Dirección 1", 2)
        assertEquals("alias", (duplicated as AuthResult.Error).field)
        coEvery { f.users.findById(1L) } returns null
        assertTrue(repo.updateProfile(1L, "x", "y", "z", "", 0) is AuthResult.Error)
    }

    @Test
    fun observadores_y_busquedas_delegan_en_los_daos() = runTest {
        val f = Fixture()
        val demo = user()
        val demoProduct = product()
        val demoCampaign = campaign()
        coEvery { f.users.findById(1L) } returns demo
        coEvery { f.users.findByEmail(any()) } returns demo
        coEvery { f.products.find(1L) } returns demoProduct
        coEvery { f.products.findByCode("LAM-0001") } returns demoProduct
        coEvery { f.campaigns.find(1L) } returns demoCampaign
        coEvery { f.campaigns.productIds(1L) } returns listOf(1L, 2L)
        every { f.users.observe(1L) } returns MutableStateFlow(demo)
        every { f.products.observeAll() } returns MutableStateFlow(listOf(demoProduct))
        every { f.products.observeActive() } returns MutableStateFlow(listOf(demoProduct))
        every { f.products.observe(1L) } returns MutableStateFlow(demoProduct)
        every { f.favorites.observeIds(1L) } returns MutableStateFlow(listOf(1L))
        every { f.favorites.observeProducts(1L) } returns MutableStateFlow(listOf(demoProduct))
        every { f.requests.observeByUser(1L) } returns MutableStateFlow(listOf(request()))
        every { f.requests.observeAll() } returns MutableStateFlow(listOf(request()))
        every { f.requests.observePendingCount() } returns MutableStateFlow(1)
        every { f.notifications.observeByUser(1L) } returns MutableStateFlow(emptyList())
        every { f.notifications.observeUnreadCount(1L) } returns MutableStateFlow(0)
        every { f.fairs.observeAll() } returns MutableStateFlow(
            listOf(FairEntity(1, "Feria", "Feria", "Parque", "Hoy", "Texto", -33.4, -70.6))
        )
        every { f.campaigns.observeAll() } returns MutableStateFlow(listOf(campaign()))
        every { f.campaigns.observeRefs() } returns MutableStateFlow(emptyList())
        val repo = f.repository()

        assertEquals(demo, repo.findUser(1L))
        assertEquals(demoProduct, repo.findProduct(1L))
        assertEquals(demoProduct, repo.findProductByCode("LAM-0001"))
        assertEquals(demoCampaign, repo.findCampaign(1L))
        assertEquals(listOf(1L, 2L), repo.campaignProductIds(1L))
        assertEquals(1L, repo.observeUser(1L).first()?.id)
        assertEquals(1, repo.observeAllProducts().first().size)
        assertEquals(1, repo.observeActiveProducts().first().size)
        assertEquals(1L, repo.observeProduct(1L).first()?.id)
        assertEquals(listOf(1L), repo.observeFavoriteIds(1L).first())
        assertEquals(1, repo.observeFavoriteProducts(1L).first().size)
        assertEquals(1, repo.observeRequestsByUser(1L).first().size)
        assertEquals(1, repo.observeAllRequests().first().size)
        assertEquals(1, repo.observePendingCount().first())
        assertEquals(0, repo.observeNotifications(1L).first().size)
        assertEquals(0, repo.observeUnreadCount(1L).first())
        assertEquals(1, repo.observeFairs().first().size)
        assertEquals(1, repo.observeCampaigns().first().size)
        assertTrue(repo.observeCampaignRefs().first().isEmpty())
    }

    // ─── Catálogo ───

    @Test
    fun guardar_producto_nuevo_asigna_codigo_y_notifica() = runTest {
        val f = Fixture()
        coEvery { f.products.maxId() } returns 4L
        coEvery { f.products.insert(any()) } returns 5L
        coEvery { f.products.find(5L) } returns product(5L)
        coEvery { f.users.notifiableClients() } returns listOf(user(2L))
        val repo = f.repository()

        val id = repo.saveProduct(product(0L).copy(code = ""))

        assertEquals(5L, id)
        coVerify { f.products.insert(match { it.code == "LAM-0005" }) }
        coVerify { f.notifications.insertAll(match { it.size == 1 && it[0].type == "CATALOG" }) }
    }

    @Test
    fun guardar_producto_existente_actualiza_y_activa_o_desactiva() = runTest {
        val f = Fixture()
        coEvery { f.products.find(1L) } returns product()
        val repo = f.repository()

        assertEquals(1L, repo.saveProduct(product()))
        coVerify { f.products.update(any()) }
        repo.setProductActive(1L, false)
        coVerify { f.products.setActive(1L, false) }
    }

    @Test
    fun favoritos_agregan_y_quitan_segun_existencia() = runTest {
        val f = Fixture()
        val repo = f.repository()

        coEvery { f.favorites.exists(1L, 1L) } returns 0
        repo.toggleFavorite(1L, 1L)
        coVerify { f.favorites.add(match { it.userId == 1L && it.productId == 1L }) }

        coEvery { f.favorites.exists(1L, 1L) } returns 1
        repo.toggleFavorite(1L, 1L)
        coVerify { f.favorites.remove(1L, 1L) }
    }

    // ─── Solicitudes ───

    @Test
    fun crear_solicitud_guarda_localmente_y_notifica_al_administrador() = runTest {
        val f = Fixture()
        coEvery { f.requests.insert(any()) } returns 42L
        coEvery { f.requests.find(42L) } returns request(42L)
        coEvery { f.users.firstAdmin() } returns user(9L, Role.ADMIN, alias = "lou.admin")
        val repo = f.repository()

        assertEquals(42L, repo.createRequest(user(), requestInput()))

        coVerify {
            f.requests.insert(match {
                it.clientName == "Valentina Rojas" && it.colors == "Crudo,Salvia" && it.notes == "Ceremonia en jardín"
            })
        }
        coVerify { f.notifications.insert(match { it.userId == 9L && it.body.contains("SOL-0042") }) }
    }

    @Test
    fun cambiar_estado_crea_notificacion_y_ignora_estado_igual() = runTest {
        val f = Fixture()
        coEvery { f.requests.find(1L) } returns request(1L)
        val repo = f.repository()

        repo.updateRequestStatus(1L, RequestStatus.CONFIRMADO)
        coVerify { f.requests.updateStatus(1L, RequestStatus.CONFIRMADO) }
        coVerify { f.notifications.insert(match { it.title.contains("cambió de estado") }) }

        coEvery { f.requests.find(1L) } returns request(1L, RequestStatus.CONFIRMADO)
        repo.updateRequestStatus(1L, RequestStatus.CONFIRMADO)
        coVerify(exactly = 1) { f.requests.updateStatus(any(), any()) }

        coEvery { f.requests.find(1L) } returns null
        repo.updateRequestStatus(1L, RequestStatus.ENTREGADO)
        coVerify(exactly = 1) { f.requests.updateStatus(any(), any()) }
    }

    @Test
    fun notificaciones_se_marcan_como_leidas() = runTest {
        val f = Fixture()
        val repo = f.repository()
        repo.markAllNotificationsRead(1L)
        repo.markNotificationRead(3L)
        coVerify { f.notifications.markAllRead(1L) }
        coVerify { f.notifications.markRead(3L) }
    }

    // ─── Campañas y escáner ───

    @Test
    fun guardar_campana_nueva_crea_referencias_y_notifica_si_se_publica() = runTest {
        val f = Fixture()
        coEvery { f.campaigns.insert(any()) } returns 7L
        coEvery { f.users.notifiableClients() } returns listOf(user(2L))
        val repo = f.repository()

        assertEquals(7L, repo.saveCampaign(campaign(0L), listOf(1L, 3L)))

        coVerify { f.campaigns.insert(any()) }
        coVerify { f.campaigns.clearProducts(7L) }
        coVerify { f.campaigns.insertRefs(match { it.size == 2 }) }
        coVerify { f.notifications.insertAll(match { it[0].type == "CAMPAIGN" }) }
    }

    @Test
    fun guardar_campana_existente_actualiza_y_publica_solo_cuando_cambia() = runTest {
        val f = Fixture()
        coEvery { f.campaigns.find(1L) } returns campaign(1L, published = false)
        coEvery { f.users.notifiableClients() } returns listOf(user(2L))
        val repo = f.repository()

        assertEquals(1L, repo.saveCampaign(campaign(1L), listOf(2L)))
        coVerify { f.campaigns.update(any()) }
        coVerify(exactly = 1) { f.notifications.insertAll(any()) }

        repo.setCampaignPublished(1L, true) // find sigue devolviendo published=false: notifica
        coVerify(exactly = 2) { f.notifications.insertAll(any()) }
        coEvery { f.campaigns.find(1L) } returns campaign(1L, published = true)
        repo.setCampaignPublished(1L, true)
        coVerify(exactly = 2) { f.notifications.insertAll(any()) }
        coEvery { f.campaigns.find(1L) } returns null
        repo.setCampaignPublished(1L, true)
    }

    @Test
    fun escanner_ignora_lecturas_repetidas_en_10_segundos() = runTest {
        val f = Fixture()
        coEvery { f.scans.countRecent(any(), any(), any()) } returns 1
        val repo = f.repository()

        repo.logScan(1L, "LAM-0001", true)
        coVerify(exactly = 0) { f.scans.insert(any()) }

        coEvery { f.scans.countRecent(any(), any(), any()) } returns 0
        repo.logScan(1L, "LAM-0001", false)
        coVerify { f.scans.insert(match { it.code == "LAM-0001" && !it.found }) }
    }

    // ─── Integración con microservicios ───

    @Test
    fun sincronizacion_remota_guarda_datos_y_reporta_errores() = runTest {
        val remote = mockk<RemoteDataSources>(relaxed = true)
        coEvery { remote.getProducts() } returns Result.success(listOf(ProductDto(id = 1, name = "Arco")))
        coEvery { remote.getFairs() } returns Result.success(emptyList())
        coEvery { remote.getRequests() } returns Result.failure(IllegalStateException("sin conexión"))
        val f = Fixture(remote)
        val repo = f.repository()

        val result = repo.syncFromRemote()

        assertEquals(1, result.products)
        assertEquals(0, result.fairs)
        assertEquals(0, result.requests)
        assertEquals(1, result.errors.size)
        coVerify { f.products.insertAll(match { it.size == 1 && it[0].name == "Arco" }) }
    }

    @Test
    fun sincronizacion_sin_cliente_remoto_no_hace_nada() = runTest {
        val repo = Fixture().repository()
        val result = repo.syncFromRemote()
        assertEquals(0, result.products + result.requests + result.fairs)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun guardar_producto_y_solicitud_llaman_al_servicio_remoto() = runTest {
        val remote = mockk<RemoteDataSources>(relaxed = true)
        coEvery { remote.createProduct(any()) } returns Result.success(ProductDto())
        coEvery { remote.createOrUpdateProduct(any()) } returns Result.success(ProductDto())
        coEvery { remote.createRequest(any()) } returns Result.success(RequestDto())
        val f = Fixture(remote)
        coEvery { f.products.maxId() } returns 0L
        coEvery { f.products.insert(any()) } returns 1L
        coEvery { f.products.find(1L) } returns product()
        coEvery { f.requests.insert(any()) } returns 2L
        coEvery { f.requests.find(2L) } returns request(2L)
        coEvery { f.users.firstAdmin() } returns null
        val repo = f.repository()

        repo.saveProduct(product(0L).copy(code = ""))
        repo.createRequest(user(), requestInput())

        coVerify { remote.createOrUpdateProduct(match { it.code == "LAM-0001" }) }
        coVerify { remote.createRequest(match { it.clientName == "Valentina Rojas" }) }
    }

    @Test
    fun datos_iniciales_se_saltan_si_ya_hay_usuarios() = runTest {
        val f = Fixture()
        coEvery { f.users.count() } returns 1
        val repo = f.repository()

        repo.seedIfNeeded()

        coVerify(exactly = 0) { f.products.insertAll(any()) }
    }
}
