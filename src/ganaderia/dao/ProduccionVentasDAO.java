package ganaderia.dao;

import ganaderia.db.ConexionADB;

import java.sql.*;
import java.util.Date;

/**
 * DAO para Produccion de Leche y Ventas.
 */
public class ProduccionVentasDAO {

    public String registrarProduccion(int idAnimal, int idEmpleado,
            Date fecha, String turno, double litros) {
        String resultado = "";
        String sql = "{ CALL PKG_PRODUCCION.SP_REGISTRAR_PRODUCCION(?,?,?,?,?,?) }";

        try (Connection con = ConexionADB.getConexion(); CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idAnimal);
            cs.setInt(2, idEmpleado);
            cs.setDate(3, new java.sql.Date(fecha.getTime()));
            cs.setString(4, turno);
            cs.setDouble(5, litros);
            cs.registerOutParameter(6, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(6);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    public double totalLitrosAnimal(int idAnimal, Date inicio, Date fin) {
        double total = 0;
        String sql = "{ ? = CALL PKG_PRODUCCION.FN_TOTAL_LITROS_ANIMAL(?,?,?) }";

        try (Connection con = ConexionADB.getConexion(); CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setInt(2, idAnimal);
            cs.setDate(3, new java.sql.Date(inicio.getTime()));
            cs.setDate(4, new java.sql.Date(fin.getTime()));
            cs.execute();
            total = cs.getDouble(1);

        } catch (SQLException e) {
            System.err.println("Error FN_TOTAL_LITROS_ANIMAL: " + e.getMessage());
        }

        return total;
    }

    public void reporteProduccionDiaria(int idFinca, Date fecha) {
        String sql = "{ CALL PKG_PRODUCCION.SP_REPORTE_PRODUCCION_DIARIA(?,?) }";

        try (Connection con = ConexionADB.getConexion(); CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idFinca);
            cs.setDate(2, new java.sql.Date(fecha.getTime()));
            cs.execute();
            System.out.println("Reporte generado en DBMS_OUTPUT del servidor.");

        } catch (SQLException e) {
            System.err.println("Error reporte produccion: " + e.getMessage());
        }
    }

    public String registrarVenta(int idAnimal, double monto,
            String comprador, String tipoVenta) {
        String resultado = "";
        String sql = "{ CALL PKG_VENTAS.SP_REGISTRAR_VENTA(?,?,?,?,?) }";

        try (Connection con = ConexionADB.getConexion(); CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idAnimal);
            cs.setDouble(2, monto);
            cs.setString(3, comprador);
            cs.setString(4, tipoVenta);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(5);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    public double totalVentasTipo(String tipoVenta, int anio) {
        double total = 0;
        String sql = "{ ? = CALL PKG_VENTAS.FN_TOTAL_VENTAS_TIPO(?,?) }";

        try (Connection con = ConexionADB.getConexion(); CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setString(2, tipoVenta);
            cs.setInt(3, anio);
            cs.execute();
            total = cs.getDouble(1);

        } catch (SQLException e) {
            System.err.println("Error FN_TOTAL_VENTAS_TIPO: " + e.getMessage());
        }

        return total;
    }

    public String reporteVentasPorFinca(int idFinca) {
        StringBuilder resultado = new StringBuilder();

        String sql
                = "SELECT a.codigo_arete || ' | ' || v.tipo_venta || ' | $' || v.monto AS linea "
                + "FROM VENTA v "
                + "JOIN ANIMAL a ON v.id_animal = a.id_animal "
                + "JOIN POTRERO p ON a.id_potrero = p.id_potrero "
                + "WHERE p.id_finca = ? "
                + "ORDER BY v.fecha_venta DESC";

        try (Connection con = ConexionADB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idFinca);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.append(rs.getString("linea")).append("\n");
                }
            }

        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }

        return resultado.length() == 0 ? "Sin datos." : resultado.toString();
    }
}
