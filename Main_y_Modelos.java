// ============================================================
// ARCHIVO: UsuarioDAO.java
// ============================================================
package ganaderia.dao;

import ganaderia.db.ConexionADB;

import java.sql.*;

/**
 * DAO para mantenimiento y autenticacion de usuarios.
 */
public class UsuarioDAO {

    // ----------------------------------------------------------
    // CREAR USUARIO  →  SP_INGRESA_USUARIO
    // ----------------------------------------------------------
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

    // ----------------------------------------------------------
    // LOGIN  →  SP_LOGIN_USUARIO
    // Retorna un arreglo: [0]=idUsuario, [1]=rol, [2]=resultado
    // ----------------------------------------------------------
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


// ============================================================
// ARCHIVO: Animal.java  — Modelo de datos
// ============================================================
package ganaderia.modelo;

import java.util.Date;

public class Animal {
    private int    idAnimal;
    private int    idPotrero;
    private String codigoArete;
    private String raza;
    private String sexo;
    private Date   fechaNacimiento;
    private double pesoKg;
    private String estado;
    private String potrero;   // nombre del potrero (para reportes)

    // --- Getters y Setters ---
    public int    getIdAnimal()       { return idAnimal; }
    public void   setIdAnimal(int v)  { this.idAnimal = v; }

    public int    getIdPotrero()      { return idPotrero; }
    public void   setIdPotrero(int v) { this.idPotrero = v; }

    public String getCodigoArete()         { return codigoArete; }
    public void   setCodigoArete(String v) { this.codigoArete = v; }

    public String getRaza()         { return raza; }
    public void   setRaza(String v) { this.raza = v; }

    public String getSexo()         { return sexo; }
    public void   setSexo(String v) { this.sexo = v; }

    public Date   getFechaNacimiento()      { return fechaNacimiento; }
    public void   setFechaNacimiento(Date v){ this.fechaNacimiento = v; }

    public double getPesoKg()         { return pesoKg; }
    public void   setPesoKg(double v) { this.pesoKg = v; }

    public String getEstado()         { return estado; }
    public void   setEstado(String v) { this.estado = v; }

    public String getPotrero()         { return potrero; }
    public void   setPotrero(String v) { this.potrero = v; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %.1f kg | %s | Potrero: %s",
                codigoArete, raza, sexo.equals("H") ? "Hembra" : "Macho",
                estado, pesoKg, fechaNacimiento, potrero);
    }
}


// ============================================================
// ARCHIVO: Main.java  — Programa principal de demostración
// ============================================================
package ganaderia;

import ganaderia.dao.AnimalDAO;
import ganaderia.dao.ProduccionVentasDAO;
import ganaderia.dao.UsuarioDAO;
import ganaderia.db.ConexionADB;
import ganaderia.modelo.Animal;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Punto de entrada del sistema de ganaderia.
 * La interfaz SOLO llama procedimientos — ningun SQL directo.
 */
public class Main {

    private static AnimalDAO          animalDAO  = new AnimalDAO();
    private static ProduccionVentasDAO pvDAO      = new ProduccionVentasDAO();
    private static UsuarioDAO          usuarioDAO = new UsuarioDAO();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("================================");
        System.out.println("  SISTEMA DE GESTION GANADERA  ");
        System.out.println("================================");

        // --- Login ---
        System.out.print("Usuario: ");
        String user = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        // En produccion usa SHA-256; aqui se simplifica para demo
        String[] sesion = usuarioDAO.login(user, hashSimple(pass));
        if (!sesion[2].startsWith("OK")) {
            System.out.println("Acceso denegado: " + sesion[2]);
            return;
        }

        System.out.println("Bienvenido. Rol: " + sesion[1]);
        menuPrincipal(sc, sesion);

        ConexionADB.cerrarConexion();
        System.out.println("Sesion cerrada.");
    }

    // ----------------------------------------------------------
    private static void menuPrincipal(Scanner sc, String[] sesion) {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\n--- MENU PRINCIPAL ---");
            System.out.println("1. Listar animales activos");
            System.out.println("2. Registrar animal");
            System.out.println("3. Registrar produccion de leche");
            System.out.println("4. Registrar venta de animal");
            System.out.println("5. Trasladar animal de potrero");
            System.out.println("6. Consultar edad de animal");
            System.out.println("7. Total ventas por tipo");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");

            String opcion = sc.nextLine().trim();
            switch (opcion) {
                case "1": opListarAnimales(sc);      break;
                case "2": opRegistrarAnimal(sc);     break;
                case "3": opRegistrarProduccion(sc); break;
                case "4": opRegistrarVenta(sc);      break;
                case "5": opTrasladarAnimal(sc);     break;
                case "6": opEdadAnimal(sc);           break;
                case "7": opTotalVentas(sc);          break;
                case "0": continuar = false;           break;
                default:  System.out.println("Opcion invalida.");
            }
        }
    }

    // ----------------------------------------------------------
    private static void opListarAnimales(Scanner sc) {
        System.out.print("ID de finca: ");
        int idFinca = Integer.parseInt(sc.nextLine().trim());

        List<Animal> lista = animalDAO.listarAnimalesActivos(idFinca);
        if (lista.isEmpty()) {
            System.out.println("No hay animales activos en esta finca.");
            return;
        }
        System.out.println("\n--- ANIMALES ACTIVOS ---");
        for (Animal a : lista) {
            System.out.println(a);
        }
        System.out.println("Total: " + lista.size() + " animales.");
    }

    // ----------------------------------------------------------
    private static void opRegistrarAnimal(Scanner sc) {
        System.out.print("ID potrero: ");
        int potrero = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Codigo arete (ej: CR-001234): ");
        String arete = sc.nextLine().trim().toUpperCase();

        // Validar formato con la funcion PL/SQL
        String validacion = animalDAO.validarArete(arete);
        if (!validacion.equals("VALIDO")) {
            System.out.println("Formato de arete invalido. Debe ser XX-NNNNNN (ej: CR-001234)");
            return;
        }

        System.out.print("Raza: ");
        String raza = sc.nextLine().trim();

        System.out.print("Sexo (M/H): ");
        String sexo = sc.nextLine().trim().toUpperCase();

        System.out.print("Peso en kg: ");
        double peso = Double.parseDouble(sc.nextLine().trim());

        Animal a = new Animal();
        a.setIdPotrero(potrero);
        a.setCodigoArete(arete);
        a.setRaza(raza);
        a.setSexo(sexo);
        a.setFechaNacimiento(new Date());  // simplificado; en produccion pedir fecha
        a.setPesoKg(peso);

        String resultado = animalDAO.registrarAnimal(a);
        System.out.println("Resultado: " + resultado);
    }

    // ----------------------------------------------------------
    private static void opRegistrarProduccion(Scanner sc) {
        System.out.print("ID animal: ");
        int idAnimal = Integer.parseInt(sc.nextLine().trim());
        System.out.print("ID empleado: ");
        int idEmpleado = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Turno (MANANA/TARDE/NOCHE): ");
        String turno = sc.nextLine().trim().toUpperCase();
        System.out.print("Litros: ");
        double litros = Double.parseDouble(sc.nextLine().trim());

        String resultado = pvDAO.registrarProduccion(idAnimal, idEmpleado,
                new Date(), turno, litros);
        System.out.println("Resultado: " + resultado);
    }

    // ----------------------------------------------------------
    private static void opRegistrarVenta(Scanner sc) {
        System.out.print("ID animal: ");
        int idAnimal = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Monto ($): ");
        double monto = Double.parseDouble(sc.nextLine().trim());
        System.out.print("Nombre comprador: ");
        String comprador = sc.nextLine().trim();
        System.out.print("Tipo venta (PIE/CANAL/LECHE/REPRODUCTORA): ");
        String tipo = sc.nextLine().trim().toUpperCase();

        String resultado = pvDAO.registrarVenta(idAnimal, monto, comprador, tipo);
        System.out.println("Resultado: " + resultado);
    }

    // ----------------------------------------------------------
    private static void opTrasladarAnimal(Scanner sc) {
        System.out.print("ID animal: ");
        int idAnimal = Integer.parseInt(sc.nextLine().trim());
        System.out.print("ID potrero destino: ");
        int idPotrero = Integer.parseInt(sc.nextLine().trim());

        String resultado = animalDAO.trasladarAnimal(idAnimal, idPotrero);
        System.out.println("Resultado: " + resultado);
    }

    // ----------------------------------------------------------
    private static void opEdadAnimal(Scanner sc) {
        System.out.print("ID animal: ");
        int idAnimal = Integer.parseInt(sc.nextLine().trim());

        double meses = animalDAO.obtenerEdadMeses(idAnimal);
        if (meses < 0) {
            System.out.println("Animal no encontrado.");
        } else {
            System.out.printf("Edad: %.1f meses (%.1f años)%n", meses, meses / 12.0);
        }
    }

    // ----------------------------------------------------------
    private static void opTotalVentas(Scanner sc) {
        System.out.print("Tipo venta (PIE/CANAL/LECHE/REPRODUCTORA): ");
        String tipo = sc.nextLine().trim().toUpperCase();
        System.out.print("Año: ");
        int anio = Integer.parseInt(sc.nextLine().trim());

        double total = pvDAO.totalVentasTipo(tipo, anio);
        System.out.printf("Total ventas tipo %s en %d: $%,.2f%n", tipo, anio, total);
    }

    // ----------------------------------------------------------
    // Hash simple para demo (en produccion usar SHA-256 + salt)
    private static String hashSimple(String texto) {
        return String.valueOf(texto.hashCode());
    }
}
