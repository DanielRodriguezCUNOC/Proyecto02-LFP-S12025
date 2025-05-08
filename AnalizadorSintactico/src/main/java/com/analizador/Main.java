package com.analizador;

import com.analizador.interfaz.Editor;

import javax.swing.*;
import java.io.StringReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Crear y mostrar la interfaz gráfica
            Editor editor = new Editor();
            editor.setVisible(true);
        });

    }
}
