package it.uniroma2.dicii.claupiscu.model.dao;

import it.uniroma2.dicii.claupiscu.exception.DAOException;

import java.sql.SQLException;

public interface GenericProcedureDAO<P> {

    P execute(Object... params) throws DAOException, SQLException;

}
