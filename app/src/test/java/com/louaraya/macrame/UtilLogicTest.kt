package com.louaraya.macrame

import com.louaraya.macrame.domain.CampaignPhase
import com.louaraya.macrame.domain.Validators
import com.louaraya.macrame.domain.phase
import com.louaraya.macrame.ui.viewmodel.ScannerViewModel
import com.louaraya.macrame.util.Dates
import com.louaraya.macrame.util.Formatters
import com.louaraya.macrame.util.PasswordHasher
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas adicionales de lógica pura (validadores, formateadores y hash). */
class UtilLogicTest {

    @Test
    fun validadores_cubren_casos_de_borde() {
        assertNull(Validators.required("  dato  ", "error"))
        assertNotNull(Validators.required("   ", "error"))
        assertEquals("Máximo 20 caracteres", Validators.alias("abcdefghijklmnopqrstu"))
        assertEquals("Solo letras, números, punto y guion bajo", Validators.alias("alias-con-guion"))
        assertEquals("Incluye una minúscula", Validators.password("DEMO1234"))
        assertEquals("Celular chileno, ej: +56 9 1234 5678", Validators.phone("123456789"))
        assertEquals("Escribe una dirección más completa", Validators.optionalAddress("abc"))
        assertEquals("Solo números enteros", Validators.dimension("abc", "el ancho"))
        assertEquals("Entre 0 y 999 unidades", Validators.stock("1000"))
        assertEquals("Máximo 300 caracteres", Validators.notes("x".repeat(301)))
        assertNull(Validators.notes("x".repeat(300)))
    }

    @Test
    fun fecha_requerida_no_puede_ser_anterior_a_hoy() {
        assertNull(Validators.requiredDate(Dates.todayUtcMillis()))
        assertNotNull(Validators.requiredDate(Dates.todayUtcMillis() - 86_400_000L))
    }

    @Test
    fun fase_de_campana_segun_fechas() {
        val today = LocalDate.of(2026, 11, 19)
        fun utc(d: LocalDate) = d.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val base = com.louaraya.macrame.data.CampaignEntity(
            name = "Campaña", description = "Descripción larga", theme = "Tema",
            startDate = utc(today.minusDays(1)), endDate = utc(today.plusDays(1)), published = true
        )
        assertEquals(CampaignPhase.ACTIVA, base.phase(today))
        assertEquals(CampaignPhase.BORRADOR, base.copy(published = false).phase(today))
        assertEquals(CampaignPhase.PROGRAMADA, base.copy(startDate = utc(today.plusDays(2))).phase(today))
        assertEquals(CampaignPhase.FINALIZADA, base.copy(endDate = utc(today.minusDays(2))).phase(today))
    }

    @Test
    fun formateadores_producen_codigos_y_textos_estables() {
        assertEquals("SOL-0007", Formatters.requestCode(7))
        assertEquals("LAM-0012", Formatters.productCode(12))
        assertEquals("200 × 150 cm", Formatters.dimensions(200, 150))
        assertEquals("Ahora", Formatters.relative(1000L, 2000L))
        assertTrue(Formatters.price(15000).contains("15"))
    }

    @Test
    fun hash_de_contrasena_es_sal_y_verificable() {
        val hash = PasswordHasher.hash("Demo1234")
        assertTrue(hash.contains(":"))
        assertTrue(PasswordHasher.verify("Demo1234", hash))
        assertFalse(PasswordHasher.verify("Otra1234", hash))
        assertFalse(PasswordHasher.verify("Demo1234", "no-es-un-hash"))
    }

    @Test
    fun escanner_extrae_codigo_de_texto_y_url() {
        assertEquals("LAM-0001", ScannerViewModel.extractProductCode("lam-0001"))
        assertEquals("LAM-0007", ScannerViewModel.extractProductCode("https://louaraya.cl/p/LAM-0007"))
        assertNull(ScannerViewModel.extractProductCode("sin codigo"))
    }
}
