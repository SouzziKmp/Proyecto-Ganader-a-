package ganaderia.dao;

import ganaderia.db.ConexionADB;
import ganaderia.modelo.Animal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para ANIMAL.
 */
public class AnimalDAO {

    public String registrarAnimal(Animal a) {
        String resultado = "";
        String sql = "{ CALL PKG_ANIMALES.SP_REGISTRAR_ANIMAL(?,?,?,?,?,?,?) }";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt    (1, a.getIdPotrero());
            cs.setString (2, a.getCodigoArete());
            cs.setString (3, a.getRaza());
            cs.setString (4, a.getSexo());
            cs.setDate   (5, new java.sql.Date(a.getFechaNacimiento().getTime()));
            cs.setDouble (6, a.getPesoKg());
            cs.registerOutParameter(7, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(7);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    public String trasladarAnimal(int idAnimal, int idPotreroDestino) {
        String resultado = "";
        String sql = "{ CALL PKG_ANIMALES.SP_TRASLADAR_ANIMAL(?,?,?) }";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idAnimal);
            cs.setInt(2, idPotreroDestino);
            cs.registerOutParameter(3, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(3);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    public String bajaAnimal(int idAnimal, String motivo) {
        String resultado = "";
        String sql = "{ CALL PKG_ANIMALES.SP_BAJA_ANIMAL(?,?,?) }";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt   (1, idAnimal);
            cs.setString(2, motivo);
            cs.registerOutParameter(3, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(3);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    public double obtenerEdadMeses(int idAnimal) {
        double meses = -1;
        String sql = "{ ? = CALL PKG_ANIMALES.FN_EDAD_MESES(?) }";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setInt(2, idAnimal);
            cs.execute();
            meses = cs.getDouble(1);

        } catch (SQLException e) {
            System.err.println("Error FN_EDAD_MESES: " + e.getMessage());
        }

        return meses;
    }

    public List<Animal> listarAnimalesActivos(int idFinca) {
        List<Animal> lista = new ArrayList<>();
        String sql = "BEGIN " +
                     "  OPEN ? FOR " +
                     "  SELECT a.id_animal, a.codigo_arete, a.raza, a.sexo, " +
                     "         a.fecha_nacimiento, a.peso_kg, a.estado, p.nombre AS potrero " +
                     "  FROM ANIMAL a " +
                     "  JOIN POTRERO p ON a.id_potrero = p.id_potrero " +
                     "  WHERE p.id_finca = ? AND a.estado = 'ACTIVO' " +
                     "  ORDER BY a.codigo_arete; " +
                     "END;";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.REF_CURSOR);
            cs.setInt(2, idFinca);
            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                while (rs.next()) {
                    Animal a = new Animal();
                    a.setIdAnimal      (rs.getInt   ("id_animal"));
                    a.setCodigoArete   (rs.getString("codigo_arete"));
                    a.setRaza          (rs.getString("raza"));
                    a.setSexo          (rs.getString("sexo"));
                    a.setFechaNacimiento(rs.getDate ("fecha_nacimiento"));
                    a.setPesoKg        (rs.getDouble("peso_kg"));
                    a.setEstado        (rs.getString("estado"));
                    a.setPotrero       (rs.getString("potrero"));
                    lista.add(a);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error listarAnimalesActivos: " + e.getMessage());
        }

        return lista;
    }

    public String validarArete(String arete) {
        String resultado = "ERROR";
        String sql = "{ ? = CALL FN_VALIDAR_ARETE(?) }";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, arete);
            cs.execute();
            resultado = cs.getString(1);

        } catch (SQLException e) {
            System.err.println("Error FN_VALIDAR_ARETE: " + e.getMessage());
        }

        return resultado;
    }

    public String estimarValorVentaPorPesoRaza(double pesoKg, String raza) {
        if (pesoKg <= 0) {
            return "ERROR: El peso debe ser mayor que cero.";
        }
        if (raza == null || raza.trim().isEmpty()) {
            return "ERROR: Indique la raza.";
        }
        String razaTrim = raza.trim();

        String sqlHist = "SELECT AVG(v.monto / NULLIF(a.peso_kg, 0)) AS precio_kg, COUNT(*) AS n "
                + "FROM venta v "
                + "JOIN animal a ON v.id_animal = a.id_animal "
                + "WHERE UPPER(TRIM(a.raza)) = UPPER(TRIM(?)) "
                + "  AND a.peso_kg > 0";

        try (Connection con = ConexionADB.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlHist)) {

            ps.setString(1, razaTrim);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int n = rs.getInt("n");
                    double precioKg = rs.getDouble("precio_kg");
                    if (n > 0 && !rs.wasNull() && precioKg > 0) {
                        double estimado = precioKg * pesoKg;
                        return String.format(
                                "Raza: %s | Peso: %.2f kg%n"
                                        + "Basado en %d venta(s) historicas (promedio $/kg sobre peso al vender).%n"
                                        + "Valor estimado: $%,.2f%n",
                                razaTrim, pesoKg, n, estimado);
                    }
                }
            }

            double refKg = precioReferenciaPorKg(razaTrim);
            double estimadoRef = refKg * pesoKg;
            return String.format(
                    "Raza: %s | Peso: %.2f kg%n"
                            + "Sin ventas historicas suficientes para esta raza; referencia interna $/kg.%n"
                            + "Valor estimado referencial: $%,.2f%n",
                    razaTrim, pesoKg, estimadoRef);

        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private static double precioReferenciaPorKg(String raza) {
        String r = raza.toUpperCase();
        if (r.contains("ANGUS")) {
            return 4.2;
        }
        if (r.contains("HEREFORD") || r.contains("BRAHMAN")) {
            return 3.6;
        }
        if (r.contains("HOLSTEIN") || r.contains("JERSEY")) {
            return 3.0;
        }
        if (r.contains("CEBU") || r.contains("BOS")) {
            return 3.2;
        }
        return 3.4;
    }
}
