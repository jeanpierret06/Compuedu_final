package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.SoporteDocumento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoporteDocumentoRepository extends MongoRepository<SoporteDocumento, String> {
}