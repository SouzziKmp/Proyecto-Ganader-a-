package ganaderia.dao;

import ganaderia.db.ConexionADB;
import java.sql.*;

public class SaludDao {

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

    public String reporteMedicamentosCriticos() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT m.id_medicamento, m.nombre, m.stock_actual, m.stock_minimo "
                + "FROM medicamento m "
                + "WHERE m.stock_actual <= m.stock_minimo "
                + "ORDER BY m.stock_actual ASC, m.id_medicamento";

        try (Connection con = ConexionADB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            sb.append(String.format("%-6s %-35s %12s %12s%n",
                    "ID", "NOMBRE", "STOCK_ACT", "STOCK_MIN"));
            sb.append("-".repeat(70)).append("\n");
            boolean hay = false;
            while (rs.next()) {
                hay = true;
                sb.append(String.format("%-6d %-35s %12.2f %12.2f%n",
                        rs.getInt("id_medicamento"),
                        trunc(rs.getString("nombre"), 35),
                        rs.getDouble("stock_actual"),
                        rs.getDouble("stock_minimo")));
            }
            if (!hay) {
                return "Sin medicamentos en estado critico (stock por encima del minimo o sin registros).";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
        return sb.toString();
    }

    public String reporteEventosSalud() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM evento_salud ORDER BY 1 DESC FETCH FIRST 500 ROWS ONLY";

        try (Connection con = ConexionADB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            for (int c = 1; c <= n; c++) {
                sb.append(md.getColumnLabel(c));
                if (c < n) {
                    sb.append(" | ");
                }
            }
            sb.append("\n");
            sb.append("-".repeat(Math.min(sb.length(), 120))).append("\n");

            boolean hay = false;
            while (rs.next()) {
                hay = true;
                for (int c = 1; c <= n; c++) {
                    String val = rs.getString(c);
                    sb.append(val != null ? val : "");
                    if (c < n) {
                        sb.append(" | ");
                    }
                }
                sb.append("\n");
            }
            if (!hay) {
                return "Sin registros en EVENTO_SALUD.";
            }
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
        return sb.toString();
    }

    private static String trunc(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }
}
