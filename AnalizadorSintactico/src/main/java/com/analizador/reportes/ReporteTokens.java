package com.analizador.reportes;

import java.util.ArrayList;
import java.util.List;

public class ReporteTokens {

    private static List<TokenInfo> tokens = new ArrayList<>();

    public static void agregarToken(String nombre, String lexema, int linea, int columna) {
        tokens.add(new TokenInfo(nombre, lexema, linea, columna));
    }

    public static void generarReporte() {
        System.out.println("Token                          Lexema                Línea      Columna");
        for (TokenInfo token : tokens) {
            System.out.printf("%-30s %-20s %-10d %-10d%n", token.nombre, token.lexema, token.linea, token.columna);
        }
    }

    public static void limpiar() {
        tokens.clear();
    }


    private static class TokenInfo {
        String nombre;
        String lexema;
        int linea;
        int columna;

        TokenInfo(String nombre, String lexema, int linea, int columna) {
            this.nombre = nombre;
            this.lexema = lexema;
            this.linea = linea;
            this.columna = columna;
        }
    }
}
