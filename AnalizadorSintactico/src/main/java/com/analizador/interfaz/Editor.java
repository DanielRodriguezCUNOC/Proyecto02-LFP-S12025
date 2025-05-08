package com.analizador.interfaz;

import com.analizador.Lexer;
import com.analizador.Parser;
import com.analizador.reportes.TokenTable;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class Editor extends JFrame {
    private final String PATH = "/home/luluwalilith/Documents/salida.txt";
    private EditorConNumerosDeLinea editor;
    private JButton analyzeButton;
    private JPanel mainPanel;
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
    private EditorConNumerosDeLinea editorConNumerosDeLinea;

    public Editor() {
        setTitle("IDE - Analizador Sintáctico");
        setSize(1000, 900);
        setLocationRelativeTo(null);
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
                System.exit(0);
            }
        });

        mainPanel = new JPanel(new BorderLayout());
        editor = new EditorConNumerosDeLinea();
        analyzeButton = new JButton("Analizar");
        analyzeButton.setFont(customFont);

        mainPanel.add(editor, BorderLayout.CENTER);
        mainPanel.add(analyzeButton, BorderLayout.SOUTH);
        add(mainPanel);

        createMenuBar();
        setupUndoRedo();

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
        saveItem.addActionListener(e -> saveFile());

        saveAsItem = new JMenuItem("Guardar Como");
        saveAsItem.setFont(customFont);
        saveAsItem.addActionListener(e -> saveAsFile());

        newMenu = new JMenu("Nuevo");
        newMenu.setFont(customFont);
        newItem = new JMenuItem("Nuevo Archivo");
        newItem.setFont(customFont);
        newItem.addActionListener(e -> newFile());

        editMenu = new JMenu("Editar");
        editMenu.setFont(customFont);
        copyItem = new JMenuItem("Copiar");
        copyItem.setFont(customFont);
        copyItem.addActionListener(e -> editor.copy());

        pasteItem = new JMenuItem("Pegar");
        pasteItem.setFont(customFont);
        pasteItem.addActionListener(e -> editor.paste());

        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        fileMenu.add(openItem);
        saveMenu.add(saveItem);
        saveMenu.add(saveAsItem);
        newMenu.add(newItem);

        menuBar.add(fileMenu);
        menuBar.add(saveMenu);
        menuBar.add(newMenu);
        menuBar.add(editMenu);
        setJMenuBar(menuBar);
    }


    private void onAnalyze() {
        String input = editor.getText();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El área de texto está vacía.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Crear el Lexer y Parser
            Lexer lexer = new Lexer(new StringReader(input));
            Parser parser = new Parser(lexer, "salida.txt");
            parser.analizar(); // Realiza el análisis sintáctico

            // Obtener los tokens recolectados
            List<Lexer.Token> tokens = parser.getTokens();

            // Mostrar la tabla de tokens
            TokenTable tokenTable = new TokenTable(tokens);
            tokenTable.setVisible(true);

            JOptionPane.showMessageDialog(
                    this,
                    "Análisis realizado correctamente.\nTokens encontrados: " + tokens.size(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            // Manejar errores mostrando los tokens recolectados hasta el error
            JOptionPane.showMessageDialog(
                    this,
                    "Error durante el análisis: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String content = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
                editorConNumerosDeLinea.setText(content);
                currentFile = selectedFile;
                hasUnsavedChanges = false;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error al abrir el archivo: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveAsFile();
        } else {
            try {
                Files.writeString(currentFile.toPath(), editor.getText(), StandardCharsets.UTF_8);
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
        editor.setText("");
        currentFile = null;
        hasUnsavedChanges = false;
    }

    private void setupUndoRedo() {
        UndoManager undoManager = new UndoManager();
        editor.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
    }
}
