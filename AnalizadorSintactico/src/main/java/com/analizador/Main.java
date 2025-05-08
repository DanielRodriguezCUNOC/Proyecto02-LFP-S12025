package com.analizador;

import java.io.StringReader;

public class Main {
    public static void main(String[] args) {
        //String codigoEjemplo = "$dato_2025 = (5 + 3) * 2 END PRINT $dato_2025 END";
        String input1 = "PRINT \"¡Hola Mundo!\" END";  // Literal
        String input2 = "PRINT $miVariable END";       // Identificador
        String input3 = "PRINT -15 END";               // Entero con signo
        String input4 = "$x = 42 END";                  // Asignación simple
        String input5 = "$total = (10 - 5) * 3 END";    // Expresión con paréntesis
        String input6 = "IF TRUE THEN PRINT \"OK\" END END";
        String input7 = "IF FALSE THEN END";  // Sin cuerpo
        String input8 = "REPEAT 3 INIT PRINT \"Loop\" END END";  // Número fijo
        String input9 = "REPEAT $veces INIT PRINT $contador END END";  // Con identificador
        String input10 = """
    $a = 5 END
    $b = 10 END
    PRINT $a + $b END
    IF TRUE THEN
        REPEAT 2 INIT PRINT \"Anidado\" END END
    END
""";
        String input11 = "$resultado = (2 ^ 4) * (10 / (5 - 3)) END";  // Potencia y división
        String input12 = "$valor = $x * ($y + 3) - 100 / 2 ^ 2 END";   // Variables y precedencia
        String input13 = """
    REPEAT 2 INIT
        IF TRUE THEN
            PRINT \"Nivel 1\" END
            REPEAT 3 INIT
                PRINT \"Nivel 2\" END
            END
        END
    END
""";
        String input14 = "PRINT \"<div class='test'>#&%</div>\" END";  // HTML con símbolos
        String input15 = "PRINT \"Valor: \" + $x END";  // Concatenación implícita (depende de tu implementación)
        String input16 = """
    # Asignar valores
    $base = 2 END  # Esto es un comentario
    /* Comentario en bloque:
       Asignar exponente */
    $exp = 10 END
    PRINT $base ^ $exp END  # Resultado: 1024
""";
        String input17 = "PRINT 0 END";                  // Cero
        String input18 = "PRINT +2147483647 END";        // Máximo entero positivo (Java)
        String input19 = "PRINT -2147483648 END";        // Mínimo entero negativo (Java)
        String input20 = "$x = 5 + 3 * 2 ^ (10 / 5) END";  // 5 + 3*2^2 = 5 + 12 = 17
        String inputCompleto = """
    # Configuración inicial
    $iteraciones = 3 END
    $mensaje = \"Ejecutando iteración\" END
    
    REPEAT $iteraciones INIT
        PRINT $mensaje + \" \" + $contador END  # Asume que $contador existe
        IF TRUE THEN
            $contador = $contador + 1 END  # Incrementar
        END
    END
    
    /* Resultado final */
    PRINT \"Valor final: \" END
    PRINT $contador END
""";
        try {
            Lexer lexer = new Lexer(new StringReader(input6)); // Usar StringReader
            Parser parser = new Parser(lexer, "salida.txt");
            parser.analizar();
            System.out.println("Análisis exitoso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
