package com.louaraya.macrame

import com.louaraya.macrame.data.FairEntity
import com.louaraya.macrame.data.ProductEntity
import com.louaraya.macrame.data.RequestEntity
import com.louaraya.macrame.data.remote.CurrentWeather
import com.louaraya.macrame.data.remote.FairDto
import com.louaraya.macrame.data.remote.ProductDto
import com.louaraya.macrame.data.remote.RequestDto
import com.louaraya.macrame.data.remote.WeatherResponse
import com.louaraya.macrame.data.remote.WeatherUnits
import com.louaraya.macrame.data.remote.display
import com.louaraya.macrame.data.remote.toDto
import com.louaraya.macrame.data.remote.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de los mapeadores entre DTO (Retrofit) y entidades (Room). */
class RemoteMappingTest {

    @Test
    fun producto_roundtrip_entity_dto() {
        val entity = ProductEntity(
            id = 3L, code = "LAM-0003", name = "Colgante Quillay", description = "Plantas",
            category = "Interiores", widthCm = 40, heightCm = 120, material = "Yute",
            priceClp = 34500, stock = 8, active = true
        )
        val dto = entity.toDto()
        val back = dto.toEntity()

        assertEquals(entity, back)
    }

    @Test
    fun producto_dto_incompleto_usa_valores_por_defecto() {
        val back = ProductDto(id = 9).toEntity()

        assertEquals("LAM-0009", back.code)
        assertEquals("Producto remoto", back.name)
        assertEquals("Interiores", back.category)
    }

    @Test
    fun solicitud_roundtrip_entity_dto() {
        val entity = RequestEntity(
            id = 5L, userId = 2L, clientName = "Cliente", productId = 1L,
            productName = "Arco", eventType = "Matrimonio", widthCm = 200, heightCm = 180,
            material = "Lino", colors = "Crudo,Salvia", requiredDate = 123L,
            notes = "Nota", status = "Pendiente"
        )

        assertEquals(entity, entity.toDto().toEntity())
    }

    @Test
    fun feria_roundtrip() {
        val fair = FairEntity(2, "Feria", "Feria", "Parque", "Hoy", "Texto", -33.4, -70.6)
        assertEquals(fair, fair.toDto().toEntity())
        assertEquals("Feria", FairDto(id = 1).toEntity().name)
    }

    @Test
    fun clima_de_api_externa_se_muestra_como_texto() {
        val response = WeatherResponse(
            current = CurrentWeather(temperature = 21.7, code = 0),
            units = WeatherUnits(temperature = "°C")
        )
        val text = response.display()

        assertTrue(text.contains("Despejado"))
        assertTrue(text.contains("21°"))
        assertEquals("Clima no disponible", WeatherResponse().display())
    }
}
