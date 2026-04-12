package ganaderia;

import ganaderia.dao.AnimalDAO;
import ganaderia.dao.ProduccionVentasDAO;
import ganaderia.dao.UsuarioDAO;
import ganaderia.modelo.Animal;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * Interfaz grafica para el sistema de ganaderia.
 */
public class Main {

    private static final AnimalDAO animalDAO = new AnimalDAO();
    private static final ProduccionVentasDAO pvDAO = new ProduccionVentasDAO();
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private static JFrame frame;
    private static JPanel cards;
    private static final String LOGIN_PANEL = "login";
    private static final String MAIN_PANEL = "main";
    private static JLabel statusLabel;
    private static JLabel welcomeLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Sistema de Gestion Ganadera");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(920, 700);
        frame.setLocationRelativeTo(null);

        cards = new JPanel(new CardLayout());
        cards.add(createLoginPanel(), LOGIN_PANEL);
        cards.add(createMainPanel(), MAIN_PANEL);

        frame.setContentPane(cards);
        frame.setVisible(true);
    }

    private static JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Ingreso al sistema de ganaderia", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel userLabel = new JLabel("Usuario:");
        JLabel passLabel = new JLabel("Password:");
        JTextField userField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JButton loginButton = new JButton("Ingresar");

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(userLabel, gbc);
        gbc.gridx = 1;
        form.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(passLabel, gbc);
        gbc.gridx = 1;
        form.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(loginButton, gbc);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);

        panel.add(form, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Ingrese usuario y password.");
                return;
            }
            login(username, password);
        });

        return panel;
    }

    private static JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        JButton logoutButton = new JButton("Cerrar Sesion");
        logoutButton.addActionListener(e -> logout());

        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Animales", createAnimalsPanel());
        tabs.addTab("Produccion", createProductionPanel());
        tabs.addTab("Ventas", createSalesPanel());
        tabs.addTab("Traslado", createTransferPanel());
        tabs.addTab("Reportes", createReportsPanel());

        panel.add(header, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);

        return panel;
    }

    private static JPanel createAnimalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel fincaLabel = new JLabel("ID de finca:");
        JTextField fincaField = new JTextField(10);
        JButton listarButton = new JButton("Listar animales activos");
        JTextArea listaArea = new JTextArea(12, 64);
        listaArea.setEditable(false);
        listaArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollLista = new JScrollPane(listaArea);
        scrollLista.setBorder(BorderFactory.createTitledBorder("Animales activos"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        top.add(fincaLabel, gbc);
        gbc.gridx = 1;
        top.add(fincaField, gbc);
        gbc.gridx = 2;
        top.add(listarButton, gbc);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Registrar animal"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;

        JLabel potreroLabel = new JLabel("ID potrero:");
        JLabel areteLabel = new JLabel("Codigo arete:");
        JLabel razaLabel = new JLabel("Raza:");
        JLabel sexoLabel = new JLabel("Sexo (M/H):");
        JLabel pesoLabel = new JLabel("Peso kg:");

        JTextField potreroField = new JTextField(8);
        JTextField areteField = new JTextField(12);
        JTextField razaField = new JTextField(12);
        JTextField sexoField = new JTextField(3);
        JTextField pesoField = new JTextField(8);
        JButton registrarButton = new JButton("Registrar animal");

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(potreroLabel, gbc);
        gbc.gridx = 1;
        form.add(potreroField, gbc);
        gbc.gridx = 2;
        form.add(areteLabel, gbc);
        gbc.gridx = 3;
        form.add(areteField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(razaLabel, gbc);
        gbc.gridx = 1;
        form.add(razaField, gbc);
        gbc.gridx = 2;
        form.add(sexoLabel, gbc);
        gbc.gridx = 3;
        form.add(sexoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(pesoLabel, gbc);
        gbc.gridx = 1;
        form.add(pesoField, gbc);
        gbc.gridx = 3;
        form.add(registrarButton, gbc);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollLista, BorderLayout.CENTER);
        panel.add(form, BorderLayout.SOUTH);

        listarButton.addActionListener(e -> {
            try {
                int idFinca = Integer.parseInt(fincaField.getText().trim());
                List<Animal> lista = animalDAO.listarAnimalesActivos(idFinca);
                if (lista.isEmpty()) {
                    listaArea.setText("No hay animales activos en esta finca.");
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Animal a : lista) {
                        sb.append(a).append("\n");
                    }
                    sb.append("\nTotal: ").append(lista.size()).append(" animales.");
                    listaArea.setText(sb.toString());
                }
            } catch (NumberFormatException ex) {
                mostrarError("ID de finca debe ser un numero.");
            }
        });

        registrarButton.addActionListener(e -> {
            try {
                int potrero = Integer.parseInt(potreroField.getText().trim());
                String arete = areteField.getText().trim().toUpperCase();
                String raza = razaField.getText().trim();
                String sexo = sexoField.getText().trim().toUpperCase();
                double peso = Double.parseDouble(pesoField.getText().trim());

                String validacion = animalDAO.validarArete(arete);
                if (!"VALIDO".equals(validacion)) {
                    mostrarError("Formato de arete invalido. Debe ser XX-NNNNNN.");
                    return;
                }

                Animal a = new Animal();
                a.setIdPotrero(potrero);
                a.setCodigoArete(arete);
                a.setRaza(raza);
                a.setSexo(sexo);
                a.setFechaNacimiento(new Date());
                a.setPesoKg(peso);

                String resultado = animalDAO.registrarAnimal(a);
                JOptionPane.showMessageDialog(frame, resultado, "Registrar animal", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                mostrarError("ID de potrero y peso deben ser numericos.");
            }
        });

        return panel;
    }

    private static JPanel createProductionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel animalLabel = new JLabel("ID animal:");
        JLabel empleadoLabel = new JLabel("ID empleado:");
        JLabel turnoLabel = new JLabel("Turno:");
        JLabel litrosLabel = new JLabel("Litros:");

        JTextField animalField = new JTextField(10);
        JTextField empleadoField = new JTextField(10);
        JTextField turnoField = new JTextField(10);
        JTextField litrosField = new JTextField(10);
        JButton registrarButton = new JButton("Registrar produccion");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(animalLabel, gbc);
        gbc.gridx = 1;
        panel.add(animalField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(empleadoLabel, gbc);
        gbc.gridx = 1;
        panel.add(empleadoField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(turnoLabel, gbc);
        gbc.gridx = 1;
        panel.add(turnoField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(litrosLabel, gbc);
        gbc.gridx = 1;
        panel.add(litrosField, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registrarButton, gbc);

        registrarButton.addActionListener(e -> {
            try {
                int idAnimal = Integer.parseInt(animalField.getText().trim());
                int idEmpleado = Integer.parseInt(empleadoField.getText().trim());
                String turno = turnoField.getText().trim().toUpperCase();
                double litros = Double.parseDouble(litrosField.getText().trim());

                String resultado = pvDAO.registrarProduccion(idAnimal, idEmpleado, new Date(), turno, litros);
                JOptionPane.showMessageDialog(frame, resultado, "Registrar produccion", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                mostrarError("ID de animal, ID de empleado y litros deben ser numericos.");
            }
        });

        return panel;
    }

    private static JPanel createSalesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel animalLabel = new JLabel("ID animal:");
        JLabel montoLabel = new JLabel("Monto ($):");
        JLabel compradorLabel = new JLabel("Comprador:");
        JLabel tipoLabel = new JLabel("Tipo venta:");

        JTextField animalField = new JTextField(10);
        JTextField montoField = new JTextField(10);
        JTextField compradorField = new JTextField(15);
        JTextField tipoField = new JTextField(12);
        JButton registrarButton = new JButton("Registrar venta");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(animalLabel, gbc);
        gbc.gridx = 1;
        panel.add(animalField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(montoLabel, gbc);
        gbc.gridx = 1;
        panel.add(montoField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(compradorLabel, gbc);
        gbc.gridx = 1;
        panel.add(compradorField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(tipoLabel, gbc);
        gbc.gridx = 1;
        panel.add(tipoField, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registrarButton, gbc);

        registrarButton.addActionListener(e -> {
            try {
                int idAnimal = Integer.parseInt(animalField.getText().trim());
                double monto = Double.parseDouble(montoField.getText().trim());
                String comprador = compradorField.getText().trim();
                String tipo = tipoField.getText().trim().toUpperCase();
                String resultado = pvDAO.registrarVenta(idAnimal, monto, comprador, tipo);
                JOptionPane.showMessageDialog(frame, resultado, "Registrar venta", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                mostrarError("ID animal y monto deben ser numericos.");
            }
        });

        return panel;
    }

    private static JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel animalLabel = new JLabel("ID animal:");
        JLabel potreroLabel = new JLabel("ID potrero destino:");

        JTextField animalField = new JTextField(10);
        JTextField potreroField = new JTextField(10);
        JButton transferirButton = new JButton("Trasladar animal");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(animalLabel, gbc);
        gbc.gridx = 1;
        panel.add(animalField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(potreroLabel, gbc);
        gbc.gridx = 1;
        panel.add(potreroField, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(transferirButton, gbc);

        transferirButton.addActionListener(e -> {
            try {
                int idAnimal = Integer.parseInt(animalField.getText().trim());
                int idPotrero = Integer.parseInt(potreroField.getText().trim());
                String resultado = animalDAO.trasladarAnimal(idAnimal, idPotrero);
                JOptionPane.showMessageDialog(frame, resultado, "Trasladar animal", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                mostrarError("ID animal y ID potrero destino deben ser numericos.");
            }
        });

        return panel;
    }

    private static JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel edadLabel = new JLabel("ID animal:");
        JTextField edadField = new JTextField(10);
        JButton edadButton = new JButton("Consultar edad");
        JLabel edadResult = new JLabel(" ");

        JLabel tipoLabel = new JLabel("Tipo venta:");
        JLabel anioLabel = new JLabel("Año:");
        JTextField tipoField = new JTextField(10);
        JTextField anioField = new JTextField(6);
        JButton totalButton = new JButton("Total ventas");
        JLabel totalResult = new JLabel(" ");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(edadLabel, gbc);
        gbc.gridx = 1;
        panel.add(edadField, gbc);
        gbc.gridx = 2;
        panel.add(edadButton, gbc);
        gbc.gridx = 3;
        panel.add(edadResult, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(tipoLabel, gbc);
        gbc.gridx = 1;
        panel.add(tipoField, gbc);
        gbc.gridx = 2;
        panel.add(anioLabel, gbc);
        gbc.gridx = 3;
        panel.add(anioField, gbc);
        gbc.gridx = 4;
        panel.add(totalButton, gbc);
        gbc.gridx = 5;
        panel.add(totalResult, gbc);

        edadButton.addActionListener(e -> {
            try {
                int idAnimal = Integer.parseInt(edadField.getText().trim());
                double meses = animalDAO.obtenerEdadMeses(idAnimal);
                if (meses < 0) {
                    edadResult.setText("No encontrado");
                } else {
                    edadResult.setText(String.format("%.1f meses (%.1f años)", meses, meses / 12.0));
                }
            } catch (NumberFormatException ex) {
                mostrarError("ID animal debe ser numerico.");
            }
        });

        totalButton.addActionListener(e -> {
            try {
                String tipo = tipoField.getText().trim().toUpperCase();
                int anio = Integer.parseInt(anioField.getText().trim());
                double total = pvDAO.totalVentasTipo(tipo, anio);
                totalResult.setText(String.format("$%,.2f", total));
            } catch (NumberFormatException ex) {
                mostrarError("Año debe ser numerico.");
            }
        });

        return panel;
    }

    private static void login(String username, String password) {
        String[] sesion = usuarioDAO.login(username, hashSimple(password));
        if (sesion != null && sesion.length == 3 && sesion[2].startsWith("OK")) {
            welcomeLabel.setText(String.format("Bienvenido, %s - Rol: %s", username, sesion[1]));
            statusLabel.setText(" ");
            showCard(MAIN_PANEL);
        } else {
            statusLabel.setText("Acceso denegado: " + (sesion != null ? sesion[2] : "Error de autenticacion"));
        }
    }

    private static void logout() {
        showCard(LOGIN_PANEL);
        statusLabel.setText("Sesion cerrada. Ingrese nuevamente.");
    }

    private static void showCard(String name) {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, name);
    }

    private static void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(frame, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static String hashSimple(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(texto.getBytes());
            return String.format("%032x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
