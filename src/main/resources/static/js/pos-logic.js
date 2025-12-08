// =================================================================
// Archivo: pos-logic.js
// Funcionalidad: Lógica de Carrito, Búsqueda de Productos y Procesamiento de Venta.
// Dependencias: Bootstrap 5 (para modales), jQuery (opcional, pero no usado aquí), Fetch API.
// =================================================================

const IGV_RATE = 0.18; // Tasa de IGV (18% para Perú)
let carrito = {}; // Almacena productos {id: {id, codigo, nombre, precio, stock, cantidad}}
let timerBusqueda; // Para el debounce en la búsqueda de productos

// =================================================================
// 1. INICIALIZACIÓN
// =================================================================

document.addEventListener('DOMContentLoaded', () => {
    // Mostrar fecha y hora actual
    const fechaElement = document.getElementById('fecha-actual');
    if (fechaElement) {
        fechaElement.textContent = new Date().toLocaleString();
    }

    // Configurar evento de búsqueda con 'debounce'
    const inputBuscar = document.getElementById('inputBuscarProducto');
    if (inputBuscar) {
        inputBuscar.addEventListener('keyup', (e) => {
            clearTimeout(timerBusqueda);
            timerBusqueda = setTimeout(() => {
                const termino = e.target.value.trim();
                buscarProductos(termino);
            }, 300);
        });

        // Cargar el catálogo completo al iniciar
        buscarProductos('');
    }

    // Evento para cálculo de cambio y totales
    document.getElementById('inputMontoRecibido').oninput = calcularTotales;

    // Evento para habilitar el botón de venta
    const checkRegistrar = document.getElementById('checkRegistrarVenta');
    const btnProcesar = document.getElementById('btnProcesarVenta');
    if (checkRegistrar && btnProcesar) {
        checkRegistrar.addEventListener('change', calcularTotales);
    }

    calcularTotales();
});


// =================================================================
// 2. BÚSQUEDA Y CATÁLOGO
// =================================================================

/**
 * Llama al API de Spring Boot para buscar productos.
 */
function buscarProductos(termino) {
    const resultadosDiv = document.getElementById('resultadosBusqueda');
    resultadosDiv.innerHTML = '<p class="text-center text-info"><i class="fas fa-spinner fa-spin"></i> Buscando productos...</p>';

    // Llama al ProductoRestController: GET /api/productos/buscar?term=query
    fetch(`/api/productos/buscar?term=${termino}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al conectar con la API de productos.');
            }
            return response.json();
        })
        .then(productos => {
            renderizarCatalogo(productos);
        })
        .catch(error => {
            console.error('Error de búsqueda:', error);
            resultadosDiv.innerHTML = `<p class="text-center text-danger">Error: ${error.message}</p>`;
        });
}

/**
 * Renderiza el catálogo visual de productos como tarjetas clickeables.
 */
function renderizarCatalogo(productos) {
    const resultadosDiv = document.getElementById('resultadosBusqueda');
    resultadosDiv.innerHTML = '';

    if (productos.length === 0) {
        resultadosDiv.innerHTML = '<p class="text-center text-muted">No se encontraron productos.</p>';
        return;
    }

    const row = document.createElement('div');
    row.className = 'row g-3 mt-1';

    productos.forEach(p => {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';

        const card = document.createElement('div');
        card.className = `card h-100 product-card cursor-pointer shadow-sm ${p.stock <= 0 ? 'bg-light text-muted' : ''}`;

        // Almacenar todos los datos importantes para pasarlos al carrito
        card.setAttribute('onclick', `agregarProductoACarrito(${p.id}, '${p.codigo}', ${p.precioVenta}, '${p.nombre}', ${p.stock})`);

        card.innerHTML = `
            <div class="card-body">
                <h6 class="card-title text-primary">${p.nombre}</h6>
                <p class="card-text small mb-1">Cód: <strong>${p.codigo}</strong></p>
                <h5 class="text-danger fw-bold">S/ ${p.precioVenta.toFixed(2)}</h5>
                <p class="card-text small text-${p.stock > 10 ? 'success' : p.stock > 0 ? 'warning' : 'danger'}">
                    Stock: <strong>${p.stock}</strong>
                </p>
                ${p.stock > 0 ? '' : '<span class="badge bg-danger">SIN STOCK</span>'}
            </div>
        `;

        col.appendChild(card);
        row.appendChild(col);
    });

    resultadosDiv.appendChild(row);
}

// =================================================================
// 3. LÓGICA DEL CARRITO Y CÁLCULOS
// =================================================================

/**
 * Añade o incrementa un producto en el carrito.
 */
function agregarProductoACarrito(id, codigo, precioVenta, nombre, stock) {
    const itemKey = id; // Usamos el ID de la DB como clave

    if (stock <= 0) {
        alert(`El producto ${nombre} no tiene stock disponible.`);
        return;
    }

    if (carrito[itemKey]) {
        if (carrito[itemKey].cantidad < stock) {
            carrito[itemKey].cantidad++;
        } else {
            alert(`No hay suficiente stock. Stock disponible: ${stock}`);
        }
    } else {
        carrito[itemKey] = {
            id: id,
            codigo: codigo,
            nombre: nombre,
            precio: precioVenta,
            stock: stock,
            cantidad: 1
        };
    }

    actualizarTablaCarrito();
    calcularTotales();
}

/**
 * Actualiza la tabla del carrito.
 */
function actualizarTablaCarrito() {
    const tbody = document.getElementById('tabla-carrito');
    tbody.innerHTML = '';

    const productosEnCarrito = Object.values(carrito);
    const contadorProductos = document.getElementById('contador-productos');

    if (productosEnCarrito.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted p-4">El carrito está vacío. Empieza a agregar productos.</td>
            </tr>
        `;
        contadorProductos.textContent = 0;
        return;
    }

    let totalItems = 0;

    productosEnCarrito.forEach(item => {
        const subtotal = item.cantidad * item.precio;
        totalItems += item.cantidad;

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${item.codigo}</td>
            <td>${item.nombre}</td>
            <td>
                <div class="input-group input-group-sm">
                    <button class="btn btn-outline-danger" type="button" onclick="cambiarCantidad(${item.id}, -1)">-</button>
                    <input type="text" class="form-control text-center" value="${item.cantidad}" readonly style="width: 50px;">
                    <button class="btn btn-outline-success" type="button" onclick="cambiarCantidad(${item.id}, 1)">+</button>
                </div>
            </td>
            <td class="text-end">S/ ${item.precio.toFixed(2)}</td>
            <td class="text-end">S/ ${subtotal.toFixed(2)}</td>
            <td>
                <button class="btn btn-sm btn-danger" onclick="eliminarProducto(${item.id})">
                    <i class="fas fa-times"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });

    contadorProductos.textContent = totalItems;
}

/**
 * Cambia la cantidad de un producto en el carrito.
 */
function cambiarCantidad(id, operacion) {
    if (!carrito[id]) return;

    const item = carrito[id];

    if (operacion === 1) {
        if (item.cantidad < item.stock) {
            item.cantidad++;
        } else {
            alert(`No hay suficiente stock. Límite: ${item.stock}`);
        }
    } else if (operacion === -1) {
        item.cantidad--;
    }

    if (item.cantidad <= 0) {
        delete carrito[id];
    }

    actualizarTablaCarrito();
    calcularTotales();
}

/**
 * Elimina un producto del carrito.
 */
function eliminarProducto(id) {
    if (confirm('¿Estás seguro de que quieres eliminar este producto del carrito?')) {
        delete carrito[id];
        actualizarTablaCarrito();
        calcularTotales();
    }
}


/**
 * Realiza todos los cálculos financieros (Subtotal, IGV, Total) y actualiza la UI.
 */
function calcularTotales() {
    let totalBruto = 0; // Suma de precios finales de los productos
    const descuento = 0; // Asumimos 0 por simplicidad
    const inputMontoRecibido = document.getElementById('inputMontoRecibido');
    const btnProcesar = document.getElementById('btnProcesarVenta');
    const checkRegistrar = document.getElementById('checkRegistrarVenta');

    // 1. Calcular el total bruto (suma de precios de lista)
    Object.values(carrito).forEach(item => {
        totalBruto += item.cantidad * item.precio;
    });

    // 2. Desglosar IGV (asumiendo que totalBruto ya incluye IGV)
    const subtotalNeto = totalBruto / (1 + IGV_RATE);
    const igv = totalBruto - subtotalNeto;
    const totalPagar = totalBruto - descuento;

    // 3. Cálculo del Cambio
    const montoRecibido = parseFloat(inputMontoRecibido.value) || 0;
    const cambio = montoRecibido - totalPagar;

    // 4. Actualizar Resumen de Pago (Columna Derecha)
    document.querySelector('#resumen-pago p:nth-child(1)').innerHTML = `Subtotal: <span class="fw-bold">S/ ${subtotalNeto.toFixed(2)}</span>`;
    document.querySelector('#resumen-pago p:nth-child(2)').innerHTML = `IGV (${(IGV_RATE * 100).toFixed(0)}%): <span class="fw-bold">S/ ${igv.toFixed(2)}</span>`;
    document.querySelector('#resumen-pago p:nth-child(3)').innerHTML = `Descuento: <span class="fw-bold text-danger">S/ ${descuento.toFixed(2)}</span>`;
    document.getElementById('total-final').textContent = `S/ ${totalPagar.toFixed(2)}`;
    document.getElementById('cambioCalculado').textContent = `S/ ${cambio.toFixed(2)}`;

    // 5. Control del Botón Procesar
    const carritoNoVacio = Object.keys(carrito).length > 0;
    const montoSuficiente = montoRecibido >= totalPagar;
    const checkMarcado = checkRegistrar.checked;

    btnProcesar.disabled = !(carritoNoVacio && montoSuficiente && checkMarcado);
}

// =================================================================
// 4. PROCESAMIENTO DE VENTA (COMUNICACIÓN CON EL BACKEND)
// =================================================================

/**
 * Maneja la acción de procesar y registrar la venta llamando al VentaRestController.
 */
async function procesarVenta() {
    calcularTotales();

    const totalFinalText = document.getElementById('total-final').textContent.replace('S/ ', '').replace(',', '');
    const totalPagar = parseFloat(totalFinalText);
    const montoRecibido = parseFloat(document.getElementById('inputMontoRecibido').value) || 0;

    const productosEnCarrito = Object.values(carrito);
    const cambioFinal = montoRecibido - totalPagar;

    // 1. CONSTRUCCIÓN DEL DTO DE DETALLES (DetalleVentaDto)
    const detallesVenta = productosEnCarrito.map(item => ({
        idProducto: item.id,
        cantidad: item.cantidad,
        precioUnitario: item.precio,
        subtotal: item.cantidad * item.precio,
        descuento: 0.00 // Asumimos 0.00 en línea
    }));

    // 2. CONSTRUCCIÓN DEL DTO DE CABECERA (VentaRequestDto)
    const ventaRequest = {
        idCliente: parseInt(document.getElementById('idClienteSeleccionado').value),
        tipoComprobante: 'BOLETA',
        metodoPago: document.getElementById('selectMetodoPago').value,
        totalVenta: totalPagar,
        descuentos: 0.00,
        montoRecibido: montoRecibido,
        cambio: cambioFinal,
        observaciones: document.getElementById('observaciones').value,
        detalles: detallesVenta
    };

    // 3. LLAMADA FETCH POST
    try {
        const response = await fetch('/api/ventas/procesar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(ventaRequest)
        });

        if (response.status === 401) {
            throw new Error("Error de sesión: No hay caja asignada. Intente reloguear.");
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Fallo en el registro (Error ${response.status}). Posiblemente stock insuficiente o datos inválidos.`);
        }

        // Venta exitosa: recibe el VentaResponseDto
        const ventaResponse = await response.json();

        // 4. MOSTRAR CONFIRMACIÓN Y LIMPIAR
        mostrarConfirmacion(ventaResponse);
        limpiarCarrito();

    } catch (error) {
        console.error('Error al procesar la venta:', error);
        alert('Fallo al registrar la venta: ' + error.message);
    }
}

/**
 * Muestra el modal de confirmación con el resumen de la venta.
 */
function mostrarConfirmacion(resumen) {
    const modalBody = document.getElementById('resumen-venta-modal-body');

    // Preparar detalle de productos
    let detalleTablaHtml = resumen.detalleResumen.map(item => `
        <tr>
            <td>${item.productoCodigo}</td>
            <td>${item.nombreProducto}</td>
            <td>${item.cantidad}</td>
            <td class="text-end">S/ ${item.precioUnitario.toFixed(2)}</td>
            <td class="text-end">S/ ${item.subtotal.toFixed(2)}</td>
        </tr>
    `).join('');

    // Renderizar el HTML completo del resumen
    modalBody.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h6>INFORMACIÓN GENERAL</h6>
                <hr>
                <p>N° Venta: <strong>${resumen.nroVenta}</strong></p>
                <p>Tipo Comprobante: <strong>${resumen.tipoComprobante}</strong></p>
                <p>Fecha: <strong>${resumen.fecha}</strong></p>
                <p>Cliente: <strong>${resumen.clienteNombre}</strong></p>
            </div>
            <div class="col-md-6">
                <h6>DATOS DE PAGO</h6>
                <hr>
                <p>Método: <strong>${resumen.metodoPago}</strong></p>
                <p>Monto Recibido: <strong class="text-success">S/ ${resumen.montoRecibido.toFixed(2)}</strong></p>
                <p>Cambio: <strong class="text-primary">S/ ${resumen.cambio.toFixed(2)}</strong></p>
            </div>
        </div>

        <h6 class="mt-4">DETALLE DE PRODUCTOS VENDIDOS</h6>
        <table class="table table-bordered table-sm">
            <thead>
                <tr><th>Código</th><th>Producto</th><th>Cant.</th><th class="text-end">Precio U.</th><th class="text-end">Subtotal</th></tr>
            </thead>
            <tbody>
                ${detalleTablaHtml}
            </tbody>
        </table>

        <h6 class="mt-4 text-end">RESUMEN FINAL</h6>
        <div class="text-end border p-3 bg-light">
            <h4 class="text-success mt-2">TOTAL PAGADO: S/ ${resumen.totalPagado.toFixed(2)}</h4>
        </div>
    `;

    document.getElementById('btnImprimirTicket').onclick = function () {
        imprimirTicket(resumen);
    };

    const modal = new bootstrap.Modal(document.getElementById('modalConfirmacionVenta'));
    modal.show();

}

/**
 * Genera un ticket/boleta simple en PDF usando jsPDF.
 * @param {Object} resumen - Objeto VentaResponseDto con todos los datos.
 */
function imprimirTicket(resumen) {
    // Necesario para usar jsPDF en el navegador
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    let y = 10; // Posición vertical inicial
    const margin = 10;
    const lineSpacing = 6;
    const pageWidth = doc.internal.pageSize.getWidth();

    // --- Cabecera y Título ---
    doc.setFontSize(14);
    doc.text("PROMART POS - COMPROBANTE DE VENTA", pageWidth / 2, y, { align: "center" });
    y += lineSpacing;
    doc.line(margin, y, pageWidth - margin, y); // Línea divisoria
    y += lineSpacing;

    // --- Información General ---
    doc.setFontSize(10);
    doc.text(`N° Venta: ${resumen.nroVenta}`, margin, y);
    doc.text(`Fecha: ${new Date(resumen.fecha).toLocaleString()}`, pageWidth - margin, y, { align: "right" });
    y += lineSpacing;
    doc.text(`Comprobante: ${resumen.tipoComprobante}`, margin, y);
    y += lineSpacing;
    doc.text(`Cliente: ${resumen.clienteNombre}`, margin, y);
    y += lineSpacing * 1.5;

    // --- Detalles de Productos (Tabla simple) ---
    doc.setFontSize(10);
    doc.text("--------------------------------------------------------------------------------", margin, y);
    y += lineSpacing;
    doc.text("CÓDIGO", margin, y);
    doc.text("PRODUCTO", 35, y);
    doc.text("CANT.", pageWidth - 45, y, { align: "right" });
    doc.text("TOTAL", pageWidth - margin, y, { align: "right" });
    y += lineSpacing;
    doc.text("--------------------------------------------------------------------------------", margin, y);
    y += lineSpacing;

    // Iterar sobre los detalles
    resumen.detalleResumen.forEach(item => {
        doc.text(item.productoCodigo, margin, y);
        // Recortar nombre largo para que quepa
        doc.text(item.nombreProducto.substring(0, 20), 35, y);
        doc.text(String(item.cantidad), pageWidth - 45, y, { align: "right" });
        doc.text(`S/ ${item.subtotal.toFixed(2)}`, pageWidth - margin, y, { align: "right" });
        y += lineSpacing;
    });

    // --- Resumen Financiero ---
    y += lineSpacing * 0.5;
    doc.line(margin, y, pageWidth - margin, y);
    y += lineSpacing;

    doc.setFontSize(10);
    // CÁLCULOS: Obtenemos el subtotal y el IGV del total pagado
    const subtotal = resumen.totalPagado / (1 + 0.18);
    const igv = resumen.totalPagado - subtotal;

    doc.text(`SUBTOTAL: S/ ${subtotal.toFixed(2)}`, pageWidth - margin, y, { align: "right" });
    y += lineSpacing;
    doc.text(`IGV (18%): S/ ${igv.toFixed(2)}`, pageWidth - margin, y, { align: "right" });
    y += lineSpacing;

    doc.setFontSize(12);
    doc.text(`TOTAL PAGADO: S/ ${resumen.totalPagado.toFixed(2)}`, pageWidth - margin, y, { align: "right" });
    y += lineSpacing;

    doc.text(`Monto Recibido: S/ ${resumen.montoRecibido.toFixed(2)}`, pageWidth - margin, y, { align: "right" });
    y += lineSpacing;
    doc.text(`Cambio: S/ ${resumen.cambio.toFixed(2)}`, pageWidth - margin, y, { align: "right" });
    y += lineSpacing * 2;

    doc.setFontSize(9);
    doc.text("¡GRACIAS POR SU COMPRA!", pageWidth / 2, y, { align: "center" });

    // Abre el PDF en una nueva pestaña (mejor para imprimir)
    doc.output('dataurlnewwindow');
}

// =================================================================
// 5. LÓGICA DE BÚSQUEDA Y SELECCIÓN DE CLIENTES
// =================================================================

let timerBusquedaCliente; // Timer para debounce

/**
 * Llama al API de clientes con debounce para autocompletado.
 * @param {string} termino - Texto ingresado por el usuario.
 */
function buscarClientesFront(termino) {
    clearTimeout(timerBusquedaCliente);

    // Si el término es corto, resetea la selección, pero no busques
    if (termino.length < 3) {
        // Si el campo está vacío, vuelve a Público General
        if (termino === "") {
            seleccionarCliente(1, "Público General", "00000000");
        }
        return;
    }

    timerBusquedaCliente = setTimeout(() => {
        // Llama al ClienteRestController: GET /api/clientes/buscar?term=query
        fetch(`/api/clientes/buscar?term=${termino}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al buscar clientes.');
                }
                return response.json();
            })
            .then(clientes => {
                renderizarOpcionesClientes(clientes);
            })
            .catch(error => {
                console.error('Fallo en búsqueda de cliente:', error);
                // Si falla, limpiar opciones, pero mantener lo que escribió el usuario
            });
    }, 500); // Espera 500ms para buscar
}

/**
 * Renderiza las opciones en el datalist para el autocompletado.
 * @param {Array<Object>} clientes - Lista de ClienteBusquedaDto.
 */
function renderizarOpcionesClientes(clientes) {
    const datalist = document.getElementById('listaClientesResultados');
    datalist.innerHTML = ''; // Limpia resultados anteriores

    // Añadir la opción 'Público General' siempre
    datalist.innerHTML = `<option value="Público General" data-id="1">DNI 00000000</option>`;

    if (clientes.length === 0) {
        return;
    }

    clientes.forEach(c => {
        const option = document.createElement('option');
        // El 'value' es lo que aparece en la caja de texto al seleccionar
        option.value = `${c.nombreCompleto} (${c.documentoIdentidad})`;
        // Usamos un atributo data-id para guardar el ID real
        option.setAttribute('data-id', c.idCliente);

        datalist.appendChild(option);
    });
}


/**
 * Función que se ejecuta al seleccionar un cliente del datalist o al limpiar.
 */
document.getElementById('inputBuscarCliente').addEventListener('change', (event) => {
    const input = event.target;
    const opcionSeleccionada = document.querySelector(`#listaClientesResultados option[value="${input.value}"]`);

    if (opcionSeleccionada) {
        const id = opcionSeleccionada.getAttribute('data-id');
        seleccionarCliente(parseInt(id), input.value, opcionSeleccionada.value.match(/\(([^)]+)\)/)[1]);
    } else if (input.value === 'Público General') {
        seleccionarCliente(1, "Público General", "00000000");
    } else {
        // Si el usuario escribe algo que no existe, forzamos a Público General
        seleccionarCliente(1, "Público General", "00000000");
        input.value = "Público General";
    }
});


/**
 * Actualiza el ID oculto y el label visible del cliente seleccionado.
 * @param {number} id - ID del cliente para el DTO.
 * @param {string} nombre - Nombre del cliente.
 * @param {string} documento - DNI o RUC.
 */
function seleccionarCliente(id, nombre, documento) {
    document.getElementById('idClienteSeleccionado').value = id;
    document.getElementById('nombreClienteActual').textContent = `${nombre} (${documento})`;
}


// Sobrescribir la función limpiarCarrito para resetear el cliente también
function limpiarCarrito() {
    carrito = {};
    document.getElementById('inputMontoRecibido').value = 0.00;
    document.getElementById('observaciones').value = '';
    document.getElementById('checkRegistrarVenta').checked = false;
    document.getElementById('btnProcesarVenta').disabled = true;

    // Resetear cliente a Público General
    seleccionarCliente(1, "Público General", "00000000");
    document.getElementById('inputBuscarCliente').value = "Público General";

    actualizarTablaCarrito();
    calcularTotales();
    buscarProductos('');

}
/**
 * Función para el botón "Anular".
 */
function anularTransaccion() {
    limpiarCarrito();
}