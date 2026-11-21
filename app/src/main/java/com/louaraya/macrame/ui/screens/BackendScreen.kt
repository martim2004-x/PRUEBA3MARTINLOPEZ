package com.louaraya.macrame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.louaraya.macrame.ui.components.AppCard
import com.louaraya.macrame.ui.components.Pill
import com.louaraya.macrame.ui.components.PrimaryButton
import com.louaraya.macrame.ui.components.RoundIconButton
import com.louaraya.macrame.ui.components.SectionTitle
import com.louaraya.macrame.ui.components.SoftButton
import com.louaraya.macrame.ui.components.StatusPill
import com.louaraya.macrame.ui.theme.MacrameColors
import com.louaraya.macrame.ui.viewmodel.BackendUiState
import com.louaraya.macrame.ui.viewmodel.BackendViewModel
import com.louaraya.macrame.ui.viewmodel.macrameViewModel
import com.louaraya.macrame.util.Formatters

/** Pantalla de evidencia visual de la integración microservicios y api */
@Composable
fun BackendScreen(onBack: () -> Unit) {
    val vm = macrameViewModel { BackendViewModel(it.remote) }
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        com.louaraya.macrame.ui.components.ScreenHeader(
            title = "API",
            subtitle = "Clima / Agregar producto",
            onBack = onBack,
            actions = {
                RoundIconButton(Icons.Rounded.Refresh, "Actualizar", vm::refresh)
            }
        )

        if (state.loading) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center
            ) { CircularProgressIndicator(color = MacrameColors.Forest) }
        }

        state.error?.let { ErrorBanner(it) }
        state.message?.let { MessageBanner(it) }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { StatusRow(state.catalogOnline, state.requestsOnline) }

            item {
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Public, null, tint = MacrameColors.Forest)
                            Text("Clima", style = MaterialTheme.typography.titleMedium, color = MacrameColors.Ink)
                        }
                        Text("Open-Meteo · Santiago, Chile", style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted)
                        Text(state.weather, style = MaterialTheme.typography.titleLarge, color = MacrameColors.Forest)
                    }
                }
            }

            item { SectionTitle("Agregar producto · catalog-service") }
            item { ProductFormCard(state, vm) }

            item {
                SoftButton(
                    text = "Crear producto rápido (relleno automático)",
                    onClick = vm::createDemoProduct,
                    icon = Icons.Rounded.Add,
                    enabled = !state.loading
                )
            }

            item { SectionTitle("Productos · catalog-service (CRUD)") }

            items(state.products, key = { it.code }) { product ->
                ProductRow(product, vm)
            }

            item { SectionTitle("Solicitudes · request-service (CRUD)") }

            item { RequestFormCard(state, vm) }

            item {
                SoftButton(
                    text = "Crear solicitud de prueba",
                    onClick = vm::createDemoRequest,
                    icon = Icons.Rounded.Add,
                    enabled = !state.loading
                )
            }

            items(state.requests, key = { it.id.toString() }) { request ->
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "SOL-%04d · %s".format(request.id, request.clientName),
                                style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(request.status)
                        }
                        Text(
                            "${request.eventType} · ${Formatters.dimensions(request.widthCm, request.heightCm)}",
                            style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
                        )
                        TextButton(onClick = { vm.advanceStatus(request) }) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.padding(end = 4.dp))
                            Text("Avanzar estado", color = MacrameColors.Forest)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductFormCard(state: BackendUiState, vm: BackendViewModel) {
    AppCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Nuevo producto", style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink)

            TextField(
                value = state.productForm.name,
                onValueChange = vm::setProductName,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextField(
                value = state.productForm.price,
                onValueChange = vm::setProductPrice,
                label = { Text("Precio CLP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = state.productForm.category,
                    onValueChange = vm::setProductCategory,
                    label = { Text("Categoría") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                TextField(
                    value = state.productForm.stock,
                    onValueChange = vm::setProductStock,
                    label = { Text("Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            TextField(
                value = state.productForm.description,
                onValueChange = vm::setProductDescription,
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            PrimaryButton(
                text = "Crear producto",
                onClick = vm::createProductFromForm,
                icon = Icons.Rounded.Add,
                enabled = !state.loading
            )

            state.error?.let {
                Text("Error: $it", style = MaterialTheme.typography.bodySmall, color = MacrameColors.Error)
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: com.louaraya.macrame.data.remote.ProductDto,
    vm: BackendViewModel
) {
    AppCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${product.code} · ${product.name}",
                    style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Pill(if (product.active) "Activo" else "Inactivo", MacrameColors.SageTint, MacrameColors.Forest)
            }
            Text(
                "${Formatters.price(product.priceClp)} · ${product.category} · Stock ${product.stock}",
                style = MaterialTheme.typography.bodySmall, color = MacrameColors.Muted
            )

            var priceEdit by remember(product.priceClp) { mutableStateOf(product.priceClp.toString()) }
            TextField(
                value = priceEdit,
                onValueChange = { priceEdit = it },
                label = { Text("Nuevo precio CLP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { vm.bumpPrice(product) }) {
                    Icon(Icons.Rounded.Edit, null, modifier = Modifier.padding(end = 4.dp))
                    Text("+1000", color = MacrameColors.Forest)
                }
                TextButton(onClick = {
                    try {
                        val newPrice = priceEdit.toInt()
                        vm.updateProductPrice(product, newPrice)
                    } catch (e: NumberFormatException) {
                        vm.updateProductPrice(product, 0)
                    }
                }) {
                    Icon(Icons.Rounded.Edit, null, modifier = Modifier.padding(end = 4.dp))
                    Text("Guardar", color = MacrameColors.Forest)
                }
                TextButton(onClick = { vm.deleteProduct(product.id) }) {
                    Icon(Icons.Rounded.Delete, null, modifier = Modifier.padding(end = 4.dp))
                    Text("Eliminar", color = MacrameColors.Error)
                }
            }
        }
    }
}

@Composable
private fun RequestFormCard(state: BackendUiState, vm: BackendViewModel) {
    AppCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Nueva solicitud", style = MaterialTheme.typography.titleSmall, color = MacrameColors.Ink)

            TextField(
                value = state.requestForm.clientName,
                onValueChange = vm::setRequestClient,
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextField(
                value = state.requestForm.eventType,
                onValueChange = vm::setRequestEvent,
                label = { Text("Tipo de evento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = state.requestForm.widthCm,
                    onValueChange = vm::setRequestWidth,
                    label = { Text("Ancho cm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                TextField(
                    value = state.requestForm.heightCm,
                    onValueChange = vm::setRequestHeight,
                    label = { Text("Alto cm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            TextField(
                value = state.requestForm.material,
                onValueChange = vm::setRequestMaterial,
                label = { Text("Material") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextField(
                value = state.requestForm.colors,
                onValueChange = vm::setRequestColors,
                label = { Text("Colores") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            SoftButton(
                text = "Crear solicitud",
                onClick = vm::createRequestFromForm,
                icon = Icons.Rounded.Add,
                enabled = !state.loading
            )

            state.error?.let {
                Text("Error: $it", style = MaterialTheme.typography.bodySmall, color = MacrameColors.Error)
            }
        }
    }
}

@Composable
private fun StatusRow(catalogOnline: Boolean, requestsOnline: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pill(
            if (catalogOnline) "Catálogo ● en línea" else "Catálogo ● sin conexión",
            if (catalogOnline) MacrameColors.SuccessTint else MacrameColors.ErrorTint,
            if (catalogOnline) MacrameColors.Success else MacrameColors.Error,
            Modifier.weight(1f)
        )
        Pill(
            if (requestsOnline) "Solicitudes ● en línea" else "Solicitudes ● sin conexión",
            if (requestsOnline) MacrameColors.SuccessTint else MacrameColors.ErrorTint,
            if (requestsOnline) MacrameColors.Success else MacrameColors.Error,
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun ErrorBanner(text: String) {
    AppCard(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.Cloud, null, tint = MacrameColors.Error)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Error)
        }
    }
}

@Composable
private fun MessageBanner(text: String) {
    AppCard(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MacrameColors.Success, modifier = Modifier.padding(12.dp))
    }
}
