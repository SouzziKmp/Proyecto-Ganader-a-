package ganaderia.dao;

import ganaderia.db.ConexionADB;
import java.sql.*;

public class SaludDAO {

    public String registrarEvento(int idAnimal, int idEmpleado,
            String tipoEvento, String descripcion) {
        String resultado = "";
        String sql = "{ CALL PKG_SALUD.SP_REGISTRAR_EVENTO(?,?,?,?,?) }";

        try (Connection con = ConexionADB.getConexion(); CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idAnimal);
            cs.setInt(2, idEmpleado);
            cs.setString(3, tipoEvento);
            cs.setString(4, descripcion);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(5);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    public String aplicarTratamiento(int idTratamiento, int idMedicamento,
            String dosis, double cantidadAplicada,
            String observaciones) {
        String resultado = "";
        String sql = "{ CALL PKG_SALUD.SP_APLICAR_TRATAMIENTO(?,?,?,?,?,?) }";

        try (Connection con = ConexionADB.getConexion(); CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idTratamiento);
            cs.setInt(2, idMedicamento);
            cs.setString(3, dosis);
            cs.setDouble(4, cantidadAplicada);
            cs.setString(5, observaciones);
            cs.registerOutParameter(6, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(6);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    public String verificarStock(int idMedicamento, double cantidad) {
        String resultado = "";
        String sql = "{ ? = CALL PKG_SALUD.FN_VERIFICAR_STOCK(?,?) }";

        try (Connection con = ConexionADB.getConexion(); CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setInt(2, idMedicamento);
            cs.setDouble(3, cantidad);

            cs.execute();
            resultado = cs.getString(1);

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }
}
