package pe.edu.promart.sistemaventas.service;

import pe.edu.promart.sistemaventas.dto.ClienteBusquedaDto;
import java.util.List;

public interface ClienteService {
    /**
     * Busca clientes por coincidencia parcial en DNI, Nombres o Apellidos.
     * 
     * @param termino El texto de búsqueda.
     * @return Lista de ClienteBusquedaDto.
     */
    List<ClienteBusquedaDto> buscarClientes(String termino);

}
