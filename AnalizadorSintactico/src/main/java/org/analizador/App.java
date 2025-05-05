package org.analizador;

import org.analizador.partelexica.AnalizadorLexico;
import org.analizador.partelexica.Token;
import org.analizador.partesintactica.AnalizadorSintactico;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        // Casos de prueba
        String[] pruebas = {
                "PRINT $var END",
                "$var = 10 + 5 END PRINT $var END",
                "REPEAT 2 INIT PRINT \"Iteración\" END END",
                "IF TRUE THEN $x = 5 END IF FALSE THEN PRINT \"No visible\" END END"
        };

        for (String prueba : pruebas) {
            System.out.println("\n=== Probando: " + prueba + " ===");
            probarAnalizador(prueba, "salida.txt");
        }
    }

    public static void probarAnalizador(String codigo, String archivoSalida) {
        try {
            // 1. Análisis léxico
            StringReader reader = new StringReader(codigo);
            AnalizadorLexico lexico = new AnalizadorLexico(reader);

            List<Token> tokens = new ArrayList<>();
            Token token;
            while ((token = lexico.yylex()) != null && !token.tipo.equals("EOF")) {
                System.out.println("Token: " + token);
                tokens.add(token);
            }

            // 2. Análisis sintáctico
            AnalizadorSintactico sintactico = new AnalizadorSintactico(tokens, archivoSalida);
            sintactico.analizar();

            System.out.println("Análisis completado correctamente");
        } catch (Exception e) {
            System.err.println("Error durante el análisis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
