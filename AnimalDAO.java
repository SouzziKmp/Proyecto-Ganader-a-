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

    
    // REGISTRAR un animal nuevo  →  PKG_ANIMALES.SP_REGISTRAR_ANIMAL
    
    public String registrarAnimal(Animal a) {
        String resultado = "";
        String sql = "{ CALL PKG_ANIMALES.SP_REGISTRAR_ANIMAL(?,?,?,?,?,?,?) }";

        try (Connection con = ConexionADB.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            // Parametros IN
            cs.setInt    (1, a.getIdPotrero());
            cs.setString (2, a.getCodigoArete());
            cs.setString (3, a.getRaza());
            cs.setString (4, a.getSexo());
            cs.setDate   (5, new java.sql.Date(a.getFechaNacimiento().getTime()));
            cs.setDouble (6, a.getPesoKg());

            // Parametro OUT
            cs.registerOutParameter(7, Types.VARCHAR);

            cs.execute();
            resultado = cs.getString(7);
            con.commit();

        } catch (SQLException e) {
            resultado = "ERROR: " + e.getMessage();
        }

        return resultado;
    }

    
    // TRASLADAR un animal a otro potrero  →  PKG_ANIMALES.SP_TRASLADAR_ANIMAL
    
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

    
    // DAR DE BAJA un animal  →  PKG_ANIMALES.SP_BAJA_ANIMAL
    
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

    
    // OBTENER EDAD EN MESES  →  PKG_ANIMALES.FN_EDAD_MESES
   
   
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

    
    // LISTAR animales activos (cursor / ResultSet via bloque PL/SQL)
  
    public List<Animal> listarAnimalesActivos(int idFinca) {
        List<Animal> lista = new ArrayList<>();

        // Bloque anonimo PL/SQL que abre un cursor y lo retorna
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

            cs.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            cs.setInt(2, idFinca);
            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(1);
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
            rs.close();

        } catch (SQLException e) {
            System.err.println("Error listarAnimalesActivos: " + e.getMessage());
        }

        return lista;
    }

    
    // VALIDAR FORMATO DE ARETE  →  FN_VALIDAR_ARETE
    
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
}
