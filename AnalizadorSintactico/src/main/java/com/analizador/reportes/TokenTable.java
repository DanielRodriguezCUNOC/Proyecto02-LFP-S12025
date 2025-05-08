package com.analizador.reportes;

import com.analizador.Lexer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TokenTable extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public TokenTable(List<Lexer.Token> tokens) {
        setTitle("Reporte de Tokens");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columnNames = {"Token", "Lexema", "Línea", "Columna"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("SansSerif", Font.BOLD, 18));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 18));
        for (Lexer.Token token : tokens) {
            String nombreToken = token.tipo;
            if (token.tipo.matches("PRINT|END|REPEAT|INIT|IF|TRUE|FALSE|THEN")) {
                nombreToken = "PALABRA RESERVADA";
            }

            Object[] rowData = {
                    nombreToken,
                    token.lexema,
                    token.fila,
                    token.columna
            };
            tableModel.addRow(rowData);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        if (tokens.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se encontraron tokens",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
