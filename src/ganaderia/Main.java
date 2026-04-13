package ganaderia;

import ganaderia.dao.AnimalDAO;
import ganaderia.dao.ProduccionVentasDAO;
import ganaderia.dao.UsuarioDAO;
import ganaderia.modelo.Animal;
import ganaderia.dao.SaludDAO;
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
    private static final SaludDAO saludDAO = new SaludDAO();

    private static JFrame frame;
    private static JPanel cards;
    private static JTabbedPane tabs;
    private static String currentUserRole = "";
    private static final String LOGIN_PANEL = "login";
    private static final String MAIN_PANEL = "main";
    private static JLabel statusLabel;
    private static JLabel welcomeLabel;
    private static final String ADMIN_PANEL_TITLE = "Usuarios";
    private static final String TAB_ANIMALES = "Animales";
    private static final String TAB_PRODUCCION = "Produccion";
    private static final String TAB_VENTAS = "Ventas";
    private static final String TAB_TRASLADO = "Traslado";
    private static final String TAB_SALUD = "Salud";
    private static final String TAB_REPORTES = "Reportes";
    private static final String[] USER_ROLES = {"ADMIN", "VETERINARIO", "OPERARIO", "GERENTE"};
    private static JPanel animalesPanel;
    private static JPanel produccionPanel;
    private static JPanel ventasPanel;
    private static JPanel trasladoPanel;
    private static JPanel saludPanel;
    private static JPanel reportesPanel;
    private static JPanel usuariosPanel;

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

        tabs = new JTabbedPane();

        animalesPanel = createAnimalsPanel();
        produccionPanel = createProductionPanel();
        ventasPanel = createSalesPanel();
        trasladoPanel = createTransferPanel();
        saludPanel = createHealthPanel();
        reportesPanel = createReportsPanel();
        usuariosPanel = createUserAdminPanel();

        manageTabsByRole();

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

        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(6, 6, 6, 6);
        gbcForm.anchor = GridBagConstraints.WEST;

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

        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        form.add(potreroLabel, gbcForm);
        gbcForm.gridx = 1;
        form.add(potreroField, gbcForm);
        gbcForm.gridx = 2;
        form.add(areteLabel, gbcForm);
        gbcForm.gridx = 3;
        form.add(areteField, gbcForm);

        gbcForm.gridx = 0;
        gbcForm.gridy = 1;
        form.add(razaLabel, gbcForm);
        gbcForm.gridx = 1;
        form.add(razaField, gbcForm);
        gbcForm.gridx = 2;
        form.add(sexoLabel, gbcForm);
        gbcForm.gridx = 3;
        form.add(sexoField, gbcForm);

        gbcForm.gridx = 0;
        gbcForm.gridy = 2;
        form.add(pesoLabel, gbcForm);
        gbcForm.gridx = 1;
        form.add(pesoField, gbcForm);
        gbcForm.gridx = 3;
        form.add(registrarButton, gbcForm);

        JPanel bajaPanel = new JPanel(new GridBagLayout());
        bajaPanel.setBorder(BorderFactory.createTitledBorder("Dar de baja animal"));

        GridBagConstraints gbcBaja = new GridBagConstraints();
        gbcBaja.insets = new Insets(6, 6, 6, 6);
        gbcBaja.anchor = GridBagConstraints.WEST;

        JLabel bajaIdLabel = new JLabel("ID animal:");
        JLabel motivoLabel = new JLabel("Motivo:");
        JTextField bajaIdField = new JTextField(8);
        JTextField motivoField = new JTextField(20);
        JButton bajaButton = new JButton("Dar de baja");

        gbcBaja.gridx = 0;
        gbcBaja.gridy = 0;
        bajaPanel.add(bajaIdLabel, gbcBaja);
        gbcBaja.gridx = 1;
        bajaPanel.add(bajaIdField, gbcBaja);

        gbcBaja.gridx = 0;
        gbcBaja.gridy = 1;
        bajaPanel.add(motivoLabel, gbcBaja);
        gbcBaja.gridx = 1;
        bajaPanel.add(motivoField, gbcBaja);

        gbcBaja.gridx = 1;
        gbcBaja.gridy = 2;
        gbcBaja.anchor = GridBagConstraints.CENTER;
        bajaPanel.add(bajaButton, gbcBaja);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 10, 10));
        bottom.add(form);
        bottom.add(bajaPanel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollLista, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

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

        bajaButton.addActionListener(e -> {
            try {
                int idAnimal = Integer.parseInt(bajaIdField.getText().trim());
                String motivo = motivoField.getText().trim();

                if (motivo.isEmpty()) {
                    mostrarError("Debe indicar un motivo de baja.");
                    return;
                }

                String resultado = animalDAO.bajaAnimal(idAnimal, motivo);
                JOptionPane.showMessageDialog(frame, resultado, "Baja animal", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                mostrarError("ID animal debe ser numerico.");
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

        JLabel fincaLabel = new JLabel("ID finca:");
        JTextField fincaField = new JTextField(8);
        JButton fincaButton = new JButton("Reporte ventas finca");
        JTextArea reporteArea = new JTextArea(8, 40);
        reporteArea.setEditable(false);
        JScrollPane reporteScroll = new JScrollPane(reporteArea);

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

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(fincaLabel, gbc);
        gbc.gridx = 1;
        panel.add(fincaField, gbc);
        gbc.gridx = 2;
        panel.add(fincaButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(reporteScroll, gbc);

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

        fincaButton.addActionListener(e -> {
            try {
                int idFinca = Integer.parseInt(fincaField.getText().trim());
                String reporte = pvDAO.reporteVentasPorFinca(idFinca);
                reporteArea.setText(reporte);
            } catch (NumberFormatException ex) {
                mostrarError("ID finca debe ser numerico.");
            }
        });

        return panel;
    }

    private static void login(String username, String password) {
        String[] sesion = usuarioDAO.login(username, hashSimple(password));
        if (sesion != null && sesion.length == 3 && sesion[2].startsWith("OK")) {
            currentUserRole = sesion[1] != null ? sesion[1].trim().toUpperCase() : "";
            welcomeLabel.setText(String.format("Bienvenido, %s - Rol: %s", username, currentUserRole));
            statusLabel.setText(" ");
            manageTabsByRole();
            showCard(MAIN_PANEL);
        } else {
            statusLabel.setText("Acceso denegado: " + (sesion != null ? sesion[2] : "Error de autenticacion"));
        }
    }

    private static void logout() {
        currentUserRole = "";
        manageTabsByRole();
        showCard(LOGIN_PANEL);
        statusLabel.setText("Sesion cerrada. Ingrese nuevamente.");
    }

    private static void showCard(String name) {
        CardLayout cl = (CardLayout) cards.getLayout();
        cl.show(cards, name);
    }

    private static void manageTabsByRole() {
        tabs.removeAll();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUserRole);
        boolean isVeterinario = "VETERINARIO".equalsIgnoreCase(currentUserRole);
        boolean isOperario = "OPERARIO".equalsIgnoreCase(currentUserRole);
        boolean isGerente = "GERENTE".equalsIgnoreCase(currentUserRole);

        if (isAdmin || isGerente || isVeterinario) {
            tabs.addTab(TAB_ANIMALES, animalesPanel);
        }
        if (isAdmin || isGerente || isOperario) {
            tabs.addTab(TAB_PRODUCCION, produccionPanel);
        }
        if (isAdmin || isGerente) {
            tabs.addTab(TAB_VENTAS, ventasPanel);
        }
        if (isAdmin || isGerente || isOperario || isVeterinario) {
            tabs.addTab(TAB_TRASLADO, trasladoPanel);
        }
        if (isAdmin || isGerente || isVeterinario) {
            tabs.addTab(TAB_SALUD, saludPanel);
        }
        if (isAdmin || isGerente) {
            tabs.addTab(TAB_REPORTES, reportesPanel);
        }
        if (isAdmin) {
            tabs.addTab(ADMIN_PANEL_TITLE, usuariosPanel);
        }
    }

    private static JPanel createUserAdminPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Creación de usuarios - Solo ADMIN"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nombreLabel = new JLabel("Nombre completo:");
        JLabel usuarioLabel = new JLabel("Usuario:");
        JLabel claveLabel = new JLabel("Password:");
        JLabel confirmarLabel = new JLabel("Confirmar password:");
        JLabel rolLabel = new JLabel("Rol:");

        JTextField nombreField = new JTextField(20);
        JTextField usuarioField = new JTextField(20);
        JPasswordField claveField = new JPasswordField(20);
        JPasswordField confirmarField = new JPasswordField(20);
        JComboBox<String> rolCombo = new JComboBox<>(USER_ROLES);
        JButton crearButton = new JButton("Crear usuario");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nombreLabel, gbc);
        gbc.gridx = 1;
        panel.add(nombreField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(usuarioLabel, gbc);
        gbc.gridx = 1;
        panel.add(usuarioField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(claveLabel, gbc);
        gbc.gridx = 1;
        panel.add(claveField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(confirmarLabel, gbc);
        gbc.gridx = 1;
        panel.add(confirmarField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(rolLabel, gbc);
        gbc.gridx = 1;
        panel.add(rolCombo, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(crearButton, gbc);

        crearButton.addActionListener(e -> {
            String nombre = nombreField.getText().trim();
            String usuario = usuarioField.getText().trim();
            String clave = new String(claveField.getPassword()).trim();
            String confirmar = new String(confirmarField.getPassword()).trim();
            String rol = rolCombo.getSelectedItem().toString();

            if (nombre.isEmpty() || usuario.isEmpty() || clave.isEmpty() || confirmar.isEmpty()) {
                mostrarError("Complete todos los campos para crear el usuario.");
                return;
            }
            if (!clave.equals(confirmar)) {
                mostrarError("Las contraseñas no coinciden.");
                return;
            }

            String resultado = usuarioDAO.ingresarUsuario(nombre, usuario, hashSimple(clave), rol);
            JOptionPane.showMessageDialog(frame, resultado, "Crear usuario", JOptionPane.INFORMATION_MESSAGE);

            if (resultado != null && resultado.toUpperCase().contains("OK")) {
                nombreField.setText("");
                usuarioField.setText("");
                claveField.setText("");
                confirmarField.setText("");
                rolCombo.setSelectedIndex(0);
            }
        });

        return panel;
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

    private static JPanel createHealthPanel() {
        JPanel eventoPanel = new JPanel(new GridBagLayout());
        eventoPanel.setBorder(BorderFactory.createTitledBorder("Registrar evento de salud"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel animalLabel = new JLabel("ID animal:");
        JLabel empleadoLabel = new JLabel("ID empleado:");
        JLabel tipoLabel = new JLabel("Tipo evento:");
        JLabel descripcionLabel = new JLabel("Descripcion:");

        JTextField animalField = new JTextField(10);
        JTextField empleadoField = new JTextField(10);
        JComboBox<String> tipoCombo = new JComboBox<>(
                new String[]{"VACUNA", "ENFERMEDAD", "REVISION"}
        );
        JTextField descripcionField = new JTextField(20);
        JButton registrarButton = new JButton("Registrar evento");

        gbc.gridx = 0;
        gbc.gridy = 0;
        eventoPanel.add(animalLabel, gbc);
        gbc.gridx = 1;
        eventoPanel.add(animalField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        eventoPanel.add(empleadoLabel, gbc);
        gbc.gridx = 1;
        eventoPanel.add(empleadoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        eventoPanel.add(tipoLabel, gbc);
        gbc.gridx = 1;
        eventoPanel.add(tipoCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        eventoPanel.add(descripcionLabel, gbc);
        gbc.gridx = 1;
        eventoPanel.add(descripcionField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        eventoPanel.add(registrarButton, gbc);

        JPanel tratamientoPanel = new JPanel(new GridBagLayout());
        tratamientoPanel.setBorder(BorderFactory.createTitledBorder("Aplicar medicamento a tratamiento"));

        GridBagConstraints gbcT = new GridBagConstraints();
        gbcT.insets = new Insets(8, 8, 8, 8);
        gbcT.anchor = GridBagConstraints.WEST;

        JLabel tratamientoLabel = new JLabel("ID tratamiento:");
        JLabel medicamentoLabel = new JLabel("ID medicamento:");
        JLabel dosisLabel = new JLabel("Dosis:");
        JLabel cantidadLabel = new JLabel("Cantidad:");
        JLabel obsLabel = new JLabel("Observaciones:");

        JTextField tratamientoField = new JTextField(10);
        JTextField medicamentoField = new JTextField(10);
        JTextField dosisField = new JTextField(10);
        JTextField cantidadField = new JTextField(10);
        JTextField obsField = new JTextField(20);

        JButton verificarButton = new JButton("Verificar stock");
        JButton aplicarButton = new JButton("Aplicar tratamiento");

        gbcT.gridx = 0;
        gbcT.gridy = 0;
        tratamientoPanel.add(tratamientoLabel, gbcT);
        gbcT.gridx = 1;
        tratamientoPanel.add(tratamientoField, gbcT);

        gbcT.gridx = 0;
        gbcT.gridy = 1;
        tratamientoPanel.add(medicamentoLabel, gbcT);
        gbcT.gridx = 1;
        tratamientoPanel.add(medicamentoField, gbcT);

        gbcT.gridx = 0;
        gbcT.gridy = 2;
        tratamientoPanel.add(dosisLabel, gbcT);
        gbcT.gridx = 1;
        tratamientoPanel.add(dosisField, gbcT);

        gbcT.gridx = 0;
        gbcT.gridy = 3;
        tratamientoPanel.add(cantidadLabel, gbcT);
        gbcT.gridx = 1;
        tratamientoPanel.add(cantidadField, gbcT);

        gbcT.gridx = 0;
        gbcT.gridy = 4;
        tratamientoPanel.add(obsLabel, gbcT);
        gbcT.gridx = 1;
        tratamientoPanel.add(obsField, gbcT);

        gbcT.gridx = 0;
        gbcT.gridy = 5;
        tratamientoPanel.add(verificarButton, gbcT);
        gbcT.gridx = 1;
        tratamientoPanel.add(aplicarButton, gbcT);

        registrarButton.addActionListener(e -> {
            try {
                int idAnimal = Integer.parseInt(animalField.getText().trim());
                int idEmpleado = Integer.parseInt(empleadoField.getText().trim());
                String tipoEvento = tipoCombo.getSelectedItem().toString();
                String descripcion = descripcionField.getText().trim();

                if (descripcion.isEmpty()) {
                    mostrarError("Debe ingresar una descripcion.");
                    return;
                }

                String resultado = saludDAO.registrarEvento(
                        idAnimal, idEmpleado, tipoEvento, descripcion
                );

                JOptionPane.showMessageDialog(
                        frame,
                        resultado,
                        "Registrar evento de salud",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (NumberFormatException ex) {
                mostrarError("ID animal e ID empleado deben ser numericos.");
            }
        });

        verificarButton.addActionListener(e -> {
            try {
                int idMedicamento = Integer.parseInt(medicamentoField.getText().trim());
                double cantidad = Double.parseDouble(cantidadField.getText().trim());

                String resultado = saludDAO.verificarStock(idMedicamento, cantidad);

                JOptionPane.showMessageDialog(
                        frame,
                        "Resultado stock: " + resultado,
                        "Verificar stock",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (NumberFormatException ex) {
                mostrarError("ID medicamento y cantidad deben ser numericos.");
            }
        });

        aplicarButton.addActionListener(e -> {
            try {
                int idTratamiento = Integer.parseInt(tratamientoField.getText().trim());
                int idMedicamento = Integer.parseInt(medicamentoField.getText().trim());
                String dosis = dosisField.getText().trim();
                double cantidad = Double.parseDouble(cantidadField.getText().trim());
                String observaciones = obsField.getText().trim();

                if (dosis.isEmpty()) {
                    mostrarError("Debe ingresar la dosis.");
                    return;
                }

                String resultado = saludDAO.aplicarTratamiento(
                        idTratamiento, idMedicamento, dosis, cantidad, observaciones
                );

                JOptionPane.showMessageDialog(
                        frame,
                        resultado,
                        "Aplicar tratamiento",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (NumberFormatException ex) {
                mostrarError("ID tratamiento, ID medicamento y cantidad deben ser numericos.");
            }
        });

        JPanel contenedor = new JPanel(new GridLayout(2, 1, 10, 10));
        contenedor.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contenedor.add(eventoPanel);
        contenedor.add(tratamientoPanel);

        return contenedor;
    }

}
