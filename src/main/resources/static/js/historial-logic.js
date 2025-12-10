document.addEventListener('DOMContentLoaded', () => {
    // Establecer fechas por defecto (Hoy inicio 00:00 - Hoy fin 23:59)
    const now = new Date();
    const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0);
    const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59);

    document.getElementById('fechaInicio').value = formatDateForInput(startOfDay);
    document.getElementById('fechaFin').value = formatDateForInput(endOfDay);

    // Cargar historial inicial
    cargarHistorial();
});

function formatDateForInput(date) {
    const tzOffset = date.getTimezoneOffset() * 60000;
    const localISOTime = (new Date(date - tzOffset)).toISOString().slice(0, 16);
    return localISOTime;
}

async function cargarHistorial() {
    const fechaInicio = document.getElementById('fechaInicio').value;
    const fechaFin = document.getElementById('fechaFin').value;
    const tbody = document.getElementById('tablaVentasBody');

    if (!fechaInicio || !fechaFin) {
        alert("Por favor selecciona un rango de fechas.");
        return;
    }

    try {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">Cargando...</td></tr>';

        const response = await fetch(`/api/historial/listar?fechaInicio=${fechaInicio}:00&fechaFin=${fechaFin}:59`);

        if (!response.ok) {
            if (response.status === 401) {
                alert("Sesión de caja no válida. Por favor vuelve a iniciar sesión en caja.");
                window.location.href = "/auth/seleccionar-caja";
                return;
            }
            throw new Error('Error al obtener datos');
        }

        const data = await response.json();

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No se encontraron ventas en este rango.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        data.forEach(venta => {
            const row = `
                <tr>
                    <td>${venta.nroVenta}</td>
                    <td>${new Date(venta.fecha).toLocaleString()}</td>
                    <td>${venta.clienteNombre}</td>
                    <td><span class="badge bg-info text-dark">${venta.tipoComprobante}</span></td>
                    <td>${venta.metodoPago}</td>
                    <td class="fw-bold">S/ ${venta.totalPagado.toFixed(2)}</td>
                    <td><span class="badge bg-success">Confirmada</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-secondary" title="Imprimir Ticket">
                            <i class="fas fa-print"></i>
                        </button>
                    </td>
                </tr>
            `;
            tbody.innerHTML += row;
        });

    } catch (error) {
        console.error(error);
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Error al cargar historial.</td></tr>';
    }
}

function exportarPdf() {
    const fechaInicio = document.getElementById('fechaInicio').value;
    const fechaFin = document.getElementById('fechaFin').value;

    if (!fechaInicio || !fechaFin) {
        alert("Por favor selecciona un rango de fechas.");
        return;
    }

    // Redireccionar directamente para descargar
    window.location.href = `/api/historial/exportar-pdf?fechaInicio=${fechaInicio}:00&fechaFin=${fechaFin}:59`;
}
