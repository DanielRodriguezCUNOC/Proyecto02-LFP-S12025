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
    public Map<String, Integer> tablaSimbolos = new HashMap<>();
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
            Token actual = tokens.get(posicionActual);
            System.out.println("Token actual: " + actual.lexema + " (" + actual.tipo + ")");

            try {
                switch (actual.tipo) {
                    case "PRINT":
                        estructuraPRINT();
                        break;
                    case "ID":
                        if (esAsignacion()) {
                            estructuraASIGNACION();
                        } else {
                            errorSintactico("Identificador no seguido de asignación", actual);
                            posicionActual++;
                        }
                        break;
                    case "REPEAT":
                        estructuraREPEAT();
                        break;
                    case "IF":
                        estructuraCONDICIONAL();
                        break;
                    case "END":
                        // Solo se consume en las estructuras que lo usan
                        errorSintactico("END fuera de contexto", actual);
                        posicionActual++;
                        break;
                    case "INIT":
                    case "THEN":
                        // Solo válidos dentro de REPEAT e IF respectivamente
                        errorSintactico(actual.tipo + " fuera de contexto", actual);
                        posicionActual++;
                        break;
                    default:
                        errorSintactico("Estructura no reconocida: " + actual.tipo, actual);
                        posicionActual++;
                }
            } catch (Exception e) {
                errorSintactico("Error al procesar: " + e.getMessage(), actual);
                posicionActual++; // Recuperación: saltar token problemático
            }
        }
        salida.close();
    }

    private boolean esAsignacion() {
        return posicionActual + 1 < tokens.size() &&
                tokens.get(posicionActual + 1).tipo.equals("ASIGN");
    }

    // Estructura PRINT: PRINT (literal|entero|id) END
    private void estructuraPRINT() {
        Token printToken = tokens.get(posicionActual++); //* Consumir PRINT

        if (posicionActual >= tokens.size()) {
            errorSintactico("Falta valor después de PRINT", printToken);
            return;
        }

        Token valor = tokens.get(posicionActual);
        String output = "";

        switch (valor.tipo) {
            case "LITERAL":
                output = valor.lexema.substring(1, valor.lexema.length() - 1); // Quitar comillas
                posicionActual++;
                break;

            case "ENTERO":
                output = valor.lexema;
                posicionActual++;
                break;
            case "ID":
                // Usar evaluarExpresion() para permitir operaciones y acceder a variables
                if (tablaSimbolos.containsKey(valor.lexema)) {
                    output = String.valueOf(tablaSimbolos.get(valor.lexema));
                    posicionActual++;
                } else {
                    errorSintactico("Identificador no definido: " + valor.lexema, valor);
                    return;
                }
                posicionActual++;
                break;

            default:
                errorSintactico("Valor inválido para PRINT", valor);
                return;
        }

        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("END")) {
            errorSintactico("Falta END después de PRINT", printToken);
            return;
        }
        posicionActual++; // Avanzar sobre END
        salida.println(output);
    }

    private Token consumirToken(String tipoEsperado) {
        if (posicionActual >= tokens.size()) {
            errorSintactico("Se esperaba un token de tipo " + tipoEsperado, null);
            return null;
        }
        Token token = tokens.get(posicionActual);
        if (!token.tipo.equals(tipoEsperado)) {
            errorSintactico("Se esperaba un token de tipo " + tipoEsperado + ", pero se encontró: " + token.tipo, token);
            return null;
        }
        posicionActual++;
        return token;
    }

    private boolean verificarToken(String tipoEsperado, String mensajeError) {
        if (posicionActual >= tokens.size() || tokens.get(posicionActual).tipo.equals(tipoEsperado)) {
            errorSintactico(mensajeError, tokens.get(posicionActual));
            return false;
        }
        posicionActual++;
        return true;
    }

    // Estructura REPEAT: REPEAT (entero|id) INIT [PRINT...] END
    private void estructuraREPEAT() {
        Token tokenRepeat = tokens.get(posicionActual++);

        if (posicionActual >= tokens.size()) {
            errorSintactico("Falta valor después de REPEAT", tokenRepeat);
            return;
        }

        Token repeticiones = tokens.get(posicionActual);
        int veces = 0;

        if (repeticiones.tipo.equals("ENTERO")) {
            veces = Integer.parseInt(repeticiones.lexema);
            posicionActual++;
        } else if (repeticiones.tipo.equals("ID")) {
            if (tablaSimbolos.containsKey(repeticiones.lexema)) {
                veces = tablaSimbolos.get(repeticiones.lexema);
                posicionActual++;
            } else {
                errorSintactico("Identificador no definido: " + repeticiones.lexema, repeticiones);
                return;
            }
        } else {
            errorSintactico("Valor inválido después de REPEAT", repeticiones);
            return;
        }
        posicionActual++; // Avanzar sobre INIT

        for (int i = 0; i < veces; i++) {
            int posicionBloque = posicionActual;
            while (posicionActual < tokens.size() && !tokens.get(posicionActual).tipo.equals("END")) {

                Token actual = tokens.get(posicionActual);
                switch (actual.tipo) {
                    case "PRINT":
                        estructuraPRINT();
                        break;

                    default:
                        errorSintactico("Instruccion no valida dentro del REPEAT", actual);
                        posicionActual++;
                }
            }

            if (posicionActual >= tokens.size()) {
                errorSintactico("Falta END después de REPEAT", tokenRepeat);
            }

            //Reiniciar la posicion
            if (i < veces - 1) {
                posicionActual = posicionBloque;
            } else {
                posicionActual++; // Avanzar sobre END
            }
        }

    }

    // Estructura CONDICIONAL: IF (TRUE|FALSE) THEN [PRINT] END
    private void estructuraCONDICIONAL() {
        Token tokenIf = tokens.get(posicionActual++);

        if (posicionActual >= tokens.size()) {
            errorSintactico("Falta condición después de IF", tokenIf);
            return;
        }

        Token condicion = tokens.get(posicionActual);
        boolean condicionValida = false;

        if (condicion.tipo.equals("TRUE")) {
            condicionValida = true;
            posicionActual++;
        } else if (condicion.tipo.equals("FALSE")) {
            condicionValida = false;
            posicionActual++;
        } else {
            errorSintactico("Condición inválida después de IF", condicion);
            return;
        }

        // Verificar THEN
        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("THEN")) {
            errorSintactico("Falta THEN después de IF", tokenIf);
            return;
        }

        posicionActual++; // Avanzar sobre THEN

        // Procesar PRINT solo si la condición es TRUE
        if (condicionValida) {
            while (posicionActual < tokens.size() && !tokens.get(posicionActual).tipo.equals("END")) {
                Token actual = tokens.get(posicionActual);
                switch (actual.tipo) {
                    case "PRINT":
                        estructuraPRINT();
                        break;
                    case "IF":
                        estructuraCONDICIONAL();
                        break;

                    default:
                        errorSintactico("Instrucción no válida dentro del IF", actual);
                        posicionActual++;
                }
            }
        } else {
            // Si la condición es FALSE, saltar el bloque de PRINT
            while (posicionActual < tokens.size() && !tokens.get(posicionActual).tipo.equals("END")) {
                posicionActual++;
            }
        }

        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("END")) {
            errorSintactico("Falta END después de IF", tokenIf);
            return;
        }
        posicionActual++; // Avanzar sobre END
    }

    // Estructura ASIGNACION: id = EXPRESION END
    private void estructuraASIGNACION() {
        Token id = tokens.get(posicionActual++);

        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("ASIGN")) {
            errorSintactico("Falta asignación después de ID", id);
            return;
        }
        posicionActual++; //Avanzar sobre ASIGN

        int valor = evaluarExpresion();

        if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("END")) {
            errorSintactico("Falta END después de asignación", id);
            return;
        }

        tablaSimbolos.put(id.lexema, valor);
        posicionActual++; // Avanzar sobre END
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
        if (posicionActual >= tokens.size()) {
            errorSintactico("Se esperaba un término, pero no hay más tokens", null);
            return 0;
        }

        //* Obtener el término actual
        Token actual = tokens.get(posicionActual);

        switch (actual.tipo) {
            case "PAR_IZQ":
                posicionActual++; // Avanzar sobre (
                int resultado = evaluarExpresion();

                if (posicionActual >= tokens.size() || !tokens.get(posicionActual).tipo.equals("PAR_DER")) {
                    errorSintactico("Falta paréntesis de cierre", actual);
                    return 0;
                }

                posicionActual++; // Avanzar sobre )
                return resultado;

            case "ENTERO":
                posicionActual++;
                return Integer.parseInt(actual.lexema);

            case "ID":
                posicionActual++;
                if (tablaSimbolos.containsKey(actual.lexema)) {
                    return tablaSimbolos.get(actual.lexema);
                } else {
                    errorSintactico("Identificador no definido: " + actual.lexema, actual);
                    return 0;
                }

            default:
                errorSintactico("Término inválido en expresión", actual);
                return 0;
        }
    }


    private void errorSintactico(String mensaje, Token token) {
        System.err.printf("Error sintáctico en línea %d, columna %d: %s (Token: %s)%n",
                token.linea, token.columna, mensaje, token.lexema);
    }
}
