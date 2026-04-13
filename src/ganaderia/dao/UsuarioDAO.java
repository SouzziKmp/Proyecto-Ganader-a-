package ganaderia.dao;

import ganaderia.db.ConexionADB;

import java.sql.*;

/**
 * DAO para mantenimiento y autenticacion de usuarios.
 */
public class UsuarioDAO {

    
    // CREAR USUARIO  →  SP_INGRESA_USUARIO
    
    public String ingresarUsuario(String nombre, String username,
                                  String passwordHash, String rol) {
        String resultado = "";
        String sql = "{ CALL SP_INGRESA_USUARIO(?,?,?,?,?) }";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, nombre);
            cs.setString(2, username);
            cs.setString(3, passwordHash);
            cs.setString(4, rol);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(5);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    
    // LOGIN  →  SP_LOGIN_USUARIO
    
    public String[] login(String username, String passwordHash) {
        String[] respuesta = {"0", "", "ERROR"};
        String sql = "{ CALL SP_LOGIN_USUARIO(?,?,?,?,?) }";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, username);
            cs.setString(2, passwordHash);
            cs.registerOutParameter(3, Types.NUMERIC);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();
            respuesta[0] = String.valueOf(cs.getInt(3));
            respuesta[1] = cs.getString(4);
            respuesta[2] = cs.getString(5);
            con.commit();

        } catch (SQLException e) {
            respuesta[2] = "ERROR: " + e.getMessage();
        }

        return respuesta;
    }
}
