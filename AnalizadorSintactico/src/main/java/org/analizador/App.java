package org.analizador;

import org.analizador.partelexica.AnalizadorLexico;
import org.analizador.partelexica.Token;
import org.analizador.partesintactica.AnalizadorSintactico;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        probar("PRINT $var END", "Variable no definida (error esperado)");
        probar("$var = 3 END", "Asignacion de variable");
        probar("PRINT $var END", "Impresion de variable");
        probar("REPEAT 2 INIT PRINT \"Iteración\" END END", "REPEAT básico");
        probar("IF TRUE THEN PRINT \"Holi\" END IF FALSE THEN PRINT $var END END", "IF anidado");
    }

    static void probar(String codigo, String descripcion) {
        System.out.println("\n=== " + descripcion + " ===\nCódigo: " + codigo);

        try {
            // Análisis léxico
            AnalizadorLexico lexico = new AnalizadorLexico(new StringReader(codigo));
            List<Token> tokens = new ArrayList<>();
            Token token;

            while ((token = lexico.yylex()) != null && !token.tipo.equals("EOF")) {
                System.out.println("Token: " + token);
                tokens.add(token);
            }

            // Análisis sintáctico
            AnalizadorSintactico sintactico = new AnalizadorSintactico(tokens, "salida.txt");
            sintactico.analizar();

            // Mostrar tabla de símbolos
            System.out.println("\nTabla de símbolos final:");
            sintactico.tablaSimbolos.forEach((k, v) -> System.out.println(k + " = " + v));

            // Mostrar contenido del archivo de salida
            System.out.println("\nContenido de salida.txt:");
            Files.lines(Paths.get("salida.txt")).forEach(System.out::println);

        } catch (Exception e) {
            System.err.println("Error durante la prueba: " + e.getMessage());
        }
    }
}
