package ganaderia.db;

import java.sql.*;
import java.util.Properties;


public class ConexionADB {

   
    private static final String WALLET_PATH =
            "C:\Oracle\Wallet_DBORACLECLOUD";
            // ↑ Cambia esta ruta a donde tengas el wallet en tu PC

    // Contraseña del wallet
    private static final String WALLET_PASSWORD = "PWusrnewexp24$";
            // ↑ Cambia por la contraseña que usaste al descargar el wallet

    // Usuario y contraseña del esquema del proyecto
    private static final String DB_USER     = "PROYECTO";
    private static final String DB_PASSWORD = "PWusrnewexp24$";

   

    // Alias de tu tnsnames.ora — se usa _high para mayor prioridad
    private static final String TNS_ALIAS = "dboraclecloud_high";

    private static Connection conexion = null;

    /**
     * Retorna la conexion activa (Singleton).
     * La crea si no existe o si fue cerrada.
     */
    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = crearConexion();
        }
        return conexion;
    }

    private static Connection crearConexion() throws SQLException {
        // Metodo 1 — Wallet cwallet.sso (sin password)
        
        Properties props = new Properties();
        props.setProperty("user",     DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("oracle.net.wallet_location",
                "(SOURCE=(METHOD=FILE)(METHOD_DATA=(DIRECTORY=" + WALLET_PATH + ")))");

        // URL usando el alias exacto de tu tnsnames.ora
        // TNS_ADMIN apunta a la carpeta del wallet para leer tnsnames.ora y sqlnet.ora
        String url = "jdbc:oracle:thin:@" + TNS_ALIAS
                   + "?TNS_ADMIN=" + WALLET_PATH;

        System.out.println("[DB] Conectando a DBORACLECLOUD (" + TNS_ALIAS + ")...");
        Connection conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
        System.out.println("[DB] ¡Conexion exitosa a Oracle ADB!");
        return conn;
    }

    /**
     * Alternativa con JKS (si cwallet.sso falla).
     
     */
    @SuppressWarnings("unused")
    private static Connection crearConexionJKS() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user",     DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("javax.net.ssl.trustStore",
                WALLET_PATH + "/truststore.jks");
        props.setProperty("javax.net.ssl.trustStorePassword", WALLET_PASSWORD);
        props.setProperty("javax.net.ssl.keyStore",
                WALLET_PATH + "/keystore.jks");
        props.setProperty("javax.net.ssl.keyStorePassword",   WALLET_PASSWORD);
        props.setProperty("oracle.net.ssl_server_dn_match",   "true");

        String url = "jdbc:oracle:thin:@" + TNS_ALIAS
                   + "?TNS_ADMIN=" + WALLET_PATH;

        return DriverManager.getConnection(url, props);
    }

    /**
     
     */
    public static void main(String[] args) {
        System.out.println("=== TEST DE CONEXION ORACLE ADB ===");
        try {
            Connection con = getConexion();

            // Verificar que las tablas del proyecto existen
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("Driver: " + meta.getDriverName() + " " + meta.getDriverVersion());
            System.out.println("Base de datos: " + meta.getDatabaseProductName()
                             + " " + meta.getDatabaseProductVersion());

            // Contar tablas del esquema
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) AS total FROM USER_TABLES")) {
                if (rs.next()) {
                    System.out.println("Tablas en el esquema: " + rs.getInt("total"));
                }
            }

            // Contar objetos PL/SQL
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT object_type, COUNT(*) AS total " +
                     "FROM USER_OBJECTS " +
                     "WHERE object_type IN ('PACKAGE','PROCEDURE','FUNCTION','TRIGGER') " +
                     "GROUP BY object_type ORDER BY object_type")) {
                System.out.println("Objetos PL/SQL:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("object_type")
                                     + ": " + rs.getInt("total"));
                }
            }

            System.out.println("\n✓ Todo listo. La conexion funciona correctamente.");
            cerrarConexion();

        } catch (SQLException e) {
            System.err.println("ERROR de conexion: " + e.getMessage());
            System.err.println("Codigo Oracle: ORA-" + e.getErrorCode());
            System.err.println("\nVerifica:");
            System.err.println("  1. WALLET_PATH apunta a la carpeta correcta");
            System.err.println("  2. DB_USER y DB_PASSWORD son correctos");
            System.err.println("  3. Los JARs de Oracle estan en el classpath");
        }
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("[DB] Conexion cerrada.");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Error al cerrar: " + e.getMessage());
            } finally {
                conexion = null;
            }
        }
    }
}
