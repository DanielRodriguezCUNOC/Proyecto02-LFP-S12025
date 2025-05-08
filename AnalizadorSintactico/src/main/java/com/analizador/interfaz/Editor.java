package com.analizador.interfaz;

import com.analizador.Lexer;
import com.analizador.Parser;
import com.analizador.reportes.TokenTable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Editor extends JFrame {
    private final String PATH = "/home/luluwalilith/Documents/salida.txt";
    private JTextArea textArea;
    private JButton analyzeButton;
    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openItem;
    private JMenu saveMenu;
    private JMenuItem saveItem;
    private JMenuItem saveAsItem;
    private JMenu newMenu;
    private JMenuItem newItem;
    private JMenu editMenu;
    private JMenuItem copyItem;
    private JMenuItem pasteItem;
    private JMenuItem undoItem;
    private JMenuItem redoItem;
    private JMenuItem aboutItem;
    private boolean hasUnsavedChanges = false;
    private Font customFont = new Font("Monospaced", Font.BOLD, 18);
    private File currentFile = null;

    public Editor() {
        setTitle("IDE - Analizador Sintáctico");
        setSize(800, 600);
        setLocationRelativeTo(null);
        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (hasUnsavedChanges) {
                    int option = JOptionPane.showConfirmDialog(
                            Editor.this,
                            "¿Desea guardar los cambios antes de salir?",
                            "Confirmar salida",
                            JOptionPane.YES_NO_CANCEL_OPTION
                    );

                    if (option == JOptionPane.YES_OPTION) saveFile();
                    else if (option == JOptionPane.CANCEL_OPTION) return;
                }
                dispose();
            }
        });

        mainPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea();
        textArea.setFont(customFont);
        analyzeButton = new JButton("Analizar");
        analyzeButton.setFont(customFont);

        scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(analyzeButton, BorderLayout.SOUTH);

        add(mainPanel);
        createMenuBar();
        setupTextAreaListener();
        analyzeButton.addActionListener(e -> onAnalyze());
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("Archivo");
        fileMenu.setFont(customFont);
        openItem = new JMenuItem("Abrir");
        openItem.setFont(customFont);
        openItem.addActionListener(e -> openFile());

        saveMenu = new JMenu("Guardar");
        saveMenu.setFont(customFont);
        saveItem = new JMenuItem("Guardar");
        saveItem.setFont(customFont);
        saveAsItem = new JMenuItem("Guardar Como");
        saveAsItem.setFont(customFont);

        newMenu = new JMenu("Nuevo");
        newMenu.setFont(customFont);
        newItem = new JMenuItem("Nuevo Archivo");
        newItem.setFont(customFont);

        editMenu = new JMenu("Editar");
        editMenu.setFont(customFont);
        copyItem = new JMenuItem("Copiar");
        copyItem.setFont(customFont);
        pasteItem = new JMenuItem("Pegar");
        pasteItem.setFont(customFont);
        undoItem = new JMenuItem("Deshacer");
        undoItem.setFont(customFont);
        redoItem = new JMenuItem("Rehacer");
        redoItem.setFont(customFont);

        aboutItem = new JMenuItem("Acerca de");
        aboutItem.setFont(customFont);


        saveItem.addActionListener(e -> saveFile());
        saveAsItem.addActionListener(e -> saveAsFile());
        newItem.addActionListener(e -> newFile());
        aboutItem.addActionListener(e -> showAboutDialog());

        saveMenu.add(saveItem);
        saveMenu.add(saveAsItem);
        newMenu.add(newItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        fileMenu.add(openItem);


        menuBar.add(fileMenu);
        menuBar.add(saveMenu);
        menuBar.add(newMenu);
        menuBar.add(editMenu);
        menuBar.add(aboutItem);
        setJMenuBar(menuBar);
    }

    private void openFile() {
        // Verificar cambios no guardados antes de abrir un nuevo archivo
        if (hasUnsavedChanges) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea guardar los cambios antes de abrir otro archivo?",
                    "Advertencia",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                saveFile(); // Guardar cambios actuales
            } else if (option == JOptionPane.CANCEL_OPTION) {
                return; // Cancelar la operación
            }
        }

        // Mostrar diálogo para seleccionar archivo
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();

                // Leer contenido del archivo
                String content = Files.readString(selectedFile.toPath());

                // Actualizar interfaz y estado
                textArea.setText(content);
                currentFile = selectedFile; // <-- Guardar referencia al archivo abierto
                hasUnsavedChanges = false;   // <-- Reiniciar bandera

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error al leer el archivo:\n" + ex.getMessage(),
                        "Error de lectura",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void setupTextAreaListener() {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                hasUnsavedChanges = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                hasUnsavedChanges = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // No aplica para texto plano
            }
        });
    }

    private void saveFile() {
        if (currentFile == null) {
            saveAsFile();
        } else {
            try {
                Files.write(currentFile.toPath(), textArea.getText().getBytes());
                hasUnsavedChanges = false;
                JOptionPane.showMessageDialog(this, "Archivo guardado exitosamente");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }
        }
    }

    private void saveAsFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveFile();
        }
    }

    private void newFile() {
        if (hasUnsavedChanges) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea guardar los cambios?",
                    "Cambios sin guardar",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            if (option == JOptionPane.YES_OPTION) saveFile();
            else if (option == JOptionPane.CANCEL_OPTION) return;
        }

        textArea.setText("");
        currentFile = null;
        hasUnsavedChanges = false;
    }

        copyItem.addActionListener(e ->textArea.copy());
    pasteItem.addActionListener(e ->textArea.paste());
}

private void setupUndoRedo() {
    UndoManager undoManager = new UndoManager();
    textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

    undoItem.addActionListener(e -> {
        if (undoManager.canUndo()) undoManager.undo();
    });

    redoItem.addActionListener(e -> {
        if (undoManager.canRedo()) undoManager.redo();
    });
}

private void showAboutDialog() {
    String info = "Desarrollado por:\n"
            + "Pablo Daniel Alvarado Rodríguez\n"
            + "Carné: 202130534\n"
            + "Curso: Lenguajes Formales y de Programación";

    JOptionPane.showMessageDialog(
            this,
            info,
            "Acerca de",
            JOptionPane.INFORMATION_MESSAGE
    );
}

private void onAnalyze() {
    String input = textArea.getText();
    if (input.isEmpty()) {
        JOptionPane.showMessageDialog(this, "El área de texto está vacía.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        Lexer lexer = new Lexer(new StringReader(input));
        List<Lexer.Token> tokens = new ArrayList<>();
        Lexer.Token token;

        while ((token = lexer.yylex()) != null) {
            tokens.add(token);
        }
        Lexer lexerAnalyze = new Lexer(new StringReader(input));
        Parser parser = new Parser(lexerAnalyze, PATH);
        parser.analizar();

        JOptionPane.showMessageDialog(this, "Análisis exitoso.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

        TokenTable tokenTable = new TokenTable(tokens);
        tokenTable.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error durante el análisis: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
}
