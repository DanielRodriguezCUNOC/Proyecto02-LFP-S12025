package com.analizador;

import java.io.StringReader;

public class Main {
    public static void main(String[] args) {
        /*String codigoEjemplo =
                "$x = 5 END\n" +
                        "PRINT $x END\n" +
                        "REPEAT 3 INIT\n" +
                        "   PRINT \"Hola\" END\n" +
                        "END";*/
        String codigoEjemplo = "$x = (5 + 3) * 2 END";
        try {
            Lexer lexer = new Lexer(new StringReader(codigoEjemplo)); // Usar StringReader
            Parser parser = new Parser(lexer, "salida.txt");
            parser.analizar();
            System.out.println("Análisis exitoso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
