package org.analizador.partesintactica;

import org.analizador.partelexica.Token;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalizadorSintactico {
    // Tabla de símbolos para almacenar identificadores y sus valores
    private Map<String, Integer> tablaSimbolos = new HashMap<>();
    private List<Token> tokens;
    private int posicionActual = 0;
    private PrintWriter salida;

    public AnalizadorSintactico(List<Token> tokens, String archivoSalida) throws IOException {
        this.tokens = tokens;
        this.salida = new PrintWriter(new FileWriter(archivoSalida));
    }

    //* Método principal para iniciar el análisis
    public void analizar() {
        while (posicionActual < tokens.size()) {
            Token tokenActual = tokens.get(posicionActual);
            try {
                if (tokenActual.tipo.equals("PRINT")) {
                    estructuraPRINT();
                } else if (tokenActual.tipo.equals("REPEAT")) {
                    estructuraREPEAT();
                } else if (tokenActual.tipo.equals("IF")) {
                    estructuraCONDICIONAL();
                } else if (tokenActual.tipo.equals("ID") &&
                        posicionActual + 1 < tokens.size() &&
                        tokens.get(posicionActual + 1).tipo.equals("ASIGN")) {
                    estructuraASIGNACION();
                } else if (!tokenActual.tipo.equals("EOF")) {
                    errorSintactico("Estructura no reconocida", tokenActual);
                }
            } catch (Exception e) {
                errorSintactico("Error en la estructura: " + e.getMessage(), tokenActual);
                posicionActual++;
            }
            posicionActual++;
        }

        salida.close();
    }

    // Estructura PRINT: PRINT (literal|entero|id) END
    private void estructuraPRINT() {
        Token tokenPrint = tokens.get(posicionActual);
        posicionActual++; // Avanzar sobre PRINT

        if (posicionActual >= tokens.size()) {
            errorSintactico("Falta valor después de PRINT", tokenPrint);
            return;
        }

        Token valor = tokens.get(posicionActual);
        String resultado = "";

        if (valor.tipo.equals("LITERAL")) {
            // Quitar las comillas del literal
            resultado = valor.lexema.substring(1, valor.lexema.length() - 1);
        } else if (valor.tipo.equals("ENTERO")) {
            resultado = valor.lexema;
        } else if (valor.tipo.equals("ID")) {
            // Buscar en la tabla de símbolos
            if (tablaSimbolos.containsKey(valor.lexema)) {
                resultado = tablaSimbolos.get(valor.lexema).toString();
            } else {
                errorSintactico("Identificador no definido: " + valor.lexema, valor);
                return;
            }
        } else {
            errorSintactico("Valor inválido después de PRINT", valor);
            return;
        }

        posicionActual++; // Avanzar sobre el valor

        // Verificar END
        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("END")) {
            errorSintactico("Falta END después de PRINT", tokenPrint);
            return;
        }

        // Escribir en el archivo de salida
        salida.println(resultado);
    }

    // Estructura REPEAT: REPEAT (entero|id) INIT [PRINT...] END
    private void estructuraREPEAT() {
        Token tokenRepeat = tokens.get(posicionActual);
        posicionActual++; // Avanzar sobre REPEAT

        if (posicionActual >= tokens.size()) {
            errorSintactico("Falta valor después de REPEAT", tokenRepeat);
            return;
        }

        Token repeticiones = tokens.get(posicionActual);
        int veces = 0;

        if (repeticiones.tipo.equals("ENTERO")) {
            veces = Integer.parseInt(repeticiones.lexema);
        } else if (repeticiones.tipo.equals("ID")) {
            if (tablaSimbolos.containsKey(repeticiones.lexema)) {
                veces = tablaSimbolos.get(repeticiones.lexema);
            } else {
                errorSintactico("Identificador no definido: " + repeticiones.lexema, repeticiones);
                return;
            }
        } else {
            errorSintactico("Valor inválido después de REPEAT", repeticiones);
            return;
        }

        posicionActual++; // Avanzar sobre el valor

        // Verificar INIT
        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("INIT")) {
            errorSintactico("Falta INIT después de REPEAT", tokenRepeat);
            return;
        }

        posicionActual++; // Avanzar sobre INIT

        // Procesar las estructuras PRINT repetidamente
        for (int i = 0; i < veces; i++) {
            int posicionInicial = posicionActual;

            // Procesar todas las estructuras PRINT hasta encontrar END
            while (posicionActual < tokens.size() &&
                    !tokens.get(posicionActual).tipo.equals("END")) {
                if (tokens.get(posicionActual).tipo.equals("PRINT")) {
                    estructuraPRINT();
                } else {
                    posicionActual++;
                }
            }

            // Reiniciar para la próxima repetición
            posicionActual = posicionInicial;
        }

        // Avanzar hasta el END final
        while (posicionActual < tokens.size() &&
                !tokens.get(posicionActual).tipo.equals("END")) {
            posicionActual++;
        }

        if (posicionActual >= tokens.size()) {
            errorSintactico("Falta END después de REPEAT", tokenRepeat);
        }
    }

    // Estructura CONDICIONAL: IF (TRUE|FALSE) THEN [PRINT] END
    private void estructuraCONDICIONAL() {
        Token tokenIf = tokens.get(posicionActual);
        posicionActual++; // Avanzar sobre IF

        if (posicionActual >= tokens.size()) {
            errorSintactico("Falta condición después de IF", tokenIf);
            return;
        }

        Token condicion = tokens.get(posicionActual);
        boolean condicionValida = false;

        if (condicion.tipo.equals("TRUE")) {
            condicionValida = true;
        } else if (condicion.tipo.equals("FALSE")) {
            condicionValida = false;
        } else {
            errorSintactico("Condición inválida después de IF", condicion);
            return;
        }

        posicionActual++; // Avanzar sobre la condición

        // Verificar THEN
        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("THEN")) {
            errorSintactico("Falta THEN después de IF", tokenIf);
            return;
        }

        posicionActual++; // Avanzar sobre THEN

        // Procesar PRINT solo si la condición es TRUE
        if (condicionValida && posicionActual < tokens.size() &&
                tokens.get(posicionActual).tipo.equals("PRINT")) {
            estructuraPRINT();
        } else {
            // Saltar hasta END
            while (posicionActual < tokens.size() &&
                    !tokens.get(posicionActual).tipo.equals("END")) {
                posicionActual++;
            }
        }

        if (posicionActual >= tokens.size()) {
            errorSintactico("Falta END después de IF", tokenIf);
        }
    }

    // Estructura ASIGNACION: id = EXPRESION END
    private void estructuraASIGNACION() {
        Token id = tokens.get(posicionActual);
        posicionActual++; // Avanzar sobre ID

        Token asign = tokens.get(posicionActual);
        posicionActual++; // Avanzar sobre =

        // Evaluar la expresión
        int valor = evaluarExpresion();

        // Verificar END
        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("END")) {
            errorSintactico("Falta END después de asignación", id);
            return;
        }

        // Guardar en tabla de símbolos
        tablaSimbolos.put(id.lexema, valor);
        System.out.println("Asignación: " + id.lexema + " = " + valor);
    }

    // Evaluar expresiones aritméticas (recursivo para manejar paréntesis)
    private int evaluarExpresion() {
        return evaluarSumaResta();
    }

    private int evaluarSumaResta() {
        int resultado = evaluarMultDiv();

        while (posicionActual < tokens.size()) {
            Token operador = tokens.get(posicionActual);

            if (operador.tipo.equals("SUMA")) {
                posicionActual++;
                resultado += evaluarMultDiv();
            } else if (operador.tipo.equals("RESTA")) {
                posicionActual++;
                resultado -= evaluarMultDiv();
            } else {
                break;
            }
        }

        return resultado;
    }

    private int evaluarMultDiv() {
        int resultado = evaluarPotencia();

        while (posicionActual < tokens.size()) {
            Token operador = tokens.get(posicionActual);

            if (operador.tipo.equals("MULT")) {
                posicionActual++;
                resultado *= evaluarPotencia();
            } else if (operador.tipo.equals("DIV")) {
                posicionActual++;
                int divisor = evaluarPotencia();
                if (divisor == 0) {
                    errorSintactico("División por cero", operador);
                    return 0;
                }
                resultado /= divisor;
            } else {
                break;
            }
        }

        return resultado;
    }

    private int evaluarPotencia() {
        int resultado = evaluarTermino();

        while (posicionActual < tokens.size()) {
            Token operador = tokens.get(posicionActual);

            if (operador.tipo.equals("POT")) {
                posicionActual++;
                resultado = (int) Math.pow(resultado, evaluarTermino());
            } else {
                break;
            }
        }

        return resultado;
    }

    private int evaluarTermino() {
        Token actual = tokens.get(posicionActual);

        if (actual.tipo.equals("PAR_IZQ")) {
            posicionActual++; // Avanzar sobre (
            int resultado = evaluarExpresion();

            if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("PAR_DER")) {
                errorSintactico("Falta paréntesis de cierre", actual);
                return 0;
            }

            posicionActual++; // Avanzar sobre )
            return resultado;
        } else if (actual.tipo.equals("ENTERO")) {
            posicionActual++;
            return Integer.parseInt(actual.lexema);
        } else if (actual.tipo.equals("ID")) {
            posicionActual++;
            if (tablaSimbolos.containsKey(actual.lexema)) {
                return tablaSimbolos.get(actual.lexema);
            } else {
                errorSintactico("Identificador no definido: " + actual.lexema, actual);
                return 0;
            }
        } else {
            errorSintactico("Término inválido en expresión", actual);
            return 0;
        }
    }

    private void errorSintactico(String mensaje, Token token) {
        System.err.printf("Error sintáctico en línea %d, columna %d: %s (Token: %s)%n",
                token.linea, token.columna, mensaje, token.lexema);
    }
}
