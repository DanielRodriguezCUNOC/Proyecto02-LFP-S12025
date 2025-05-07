package com.analizador;

import java.io.*;
import java.util.*;


public class Parser {
    private Stack<String> pila = new Stack<>();
    private HashMap<String, Integer> tablaSimbolos = new HashMap<>();
    private Lexer.Token tokenActual;
    private Lexer lexer;
    private BufferedWriter outputWriter;
    private boolean debugMode = true;

    public Parser(Lexer lexer, String outputFile) throws IOException {
        this.lexer = lexer;
        this.outputWriter = new BufferedWriter(new FileWriter(outputFile));
        inicializarPila();
    }

    private void inicializarPila() {
        pila.push("$");
        pila.push("PROGRAM");
    }

    public void analizar() throws Exception {
        tokenActual = obtenerSiguienteToken();
        while (!pila.isEmpty()) {
            String tope = pila.pop();
            logDebug("Pila: " + pila + " | Token actual: " + tokenActual.lexema);

            //* Manejamos cadena vacia
            if (tope.equals("ε")) {
                continue;
            }

            //* Si el tope es un símbolo de fin de cadena, verificamos si el token actual es EOF
            if (tope.equals("$")) {
                if (!tokenActual.tipo.equals("EOF")) {
                    throw new Error("Error: Entrada no consumida completamente");
                }
                break;
            }

            if (esTerminal(tope)) {
                manejarTerminal(tope);
            } else {
                expandirNoTerminal(tope);
            }
        }
        outputWriter.close();
    }

    private void manejarTerminal(String terminal) throws Exception {
        if (terminal.equals(tokenActual.tipo) || (terminal.equals("PALABRA") && terminal.equals(tokenActual.tipo))) {
            tokenActual = obtenerSiguienteToken();
        } else {
            throw new ErrorSintactico(tokenActual);
        }
    }

    private void expandirNoTerminal(String noTerminal) throws Exception {
        switch (noTerminal) {
            case "PROGRAM":
                pila.push("PROGRAM_END");
                pila.push("STRUCT");
                break;
                
            case "STRUCT":
                if (tokenActual.lexema.equals("PRINT")) pila.push("PRINT_STRUCT");
                else if (tokenActual.lexema.equals("REPEAT")) pila.push("REPEAT_STRUCT");
                else if (tokenActual.lexema.equals("IF")) pila.push("CONDICIONAL_STRUCT");
                else if (tokenActual.tipo.equals("ID")) pila.push("ASIGNACION_STRUCT");
                else throw new ErrorSintactico(tokenActual);
                break;

            // Estructura PRINT
            case "PRINT_STRUCT":
                pila.push("END");
                pila.push("PRINT_VALUE");
                pila.push("PRINT");
                break;

            case "PRINT_VALUE":
                switch (tokenActual.tipo) {
                    case "LITERAL":
                        escribirSalida(tokenActual.lexema.replace("\"", ""));
                        pila.push("LITERAL");
                        break;
                    case "ENTERO":
                        escribirSalida(tokenActual.lexema);
                        pila.push("ENTERO");
                        break;
                    case "ID":
                        String id = tokenActual.lexema.substring(1);
                        escribirSalida(tablaSimbolos.get(id).toString());
                        pila.push("ID");
                        break;
                    default:
                        throw new ErrorSintactico(tokenActual);
                }
                break;

            // Estructura REPEAT
            case "REPEAT_STRUCT":
                pila.push("REPEAT_END");
                pila.push("INIT");
                pila.push("REPEAT_COUNT");
                pila.push("REPEAT");
                break;

            case "REPEAT_COUNT":
                if (tokenActual.tipo.equals("ENTERO") || tokenActual.tipo.equals("ID")) {
                    int repeticiones = obtenerValor(tokenActual);
                    pila.push(tokenActual.tipo);
                    for (int i = 0; i < repeticiones; i++) {
                        pila.push("PRINT_STRUCT");
                    }
                } else {
                    throw new ErrorSintactico(tokenActual);
                }
                break;

            // Estructura CONDICIONAL
            case "CONDICIONAL_STRUCT":
                pila.push("END");
                pila.push("CONDICIONAL_BODY");
                pila.push("THEN");
                pila.push("BOOL_VALUE");
                pila.push("IF");
                break;

            case "BOOL_VALUE":
                if (tokenActual.lexema.equals("TRUE") || tokenActual.lexema.equals("FALSE")) {
                    boolean activar = tokenActual.lexema.equals("TRUE");
                    pila.push(activar ? "PRINT_STRUCT" : "ε");
                    pila.push("PALABRA");
                } else {
                    throw new ErrorSintactico(tokenActual);
                }
                break;

            // Estructura ASIGNACION
            case "ASIGNACION_STRUCT":
                pila.push("END");
                pila.push("EXPR");
                pila.push("IGUAL");
                String identificador = tokenActual.lexema.substring(1);
                pila.push("ID");
                tablaSimbolos.put(identificador, evaluarExpresion());
                break;

            // Manejo de expresiones
            case "EXPR":
                pila.push("EXPR_END");
                pila.push("TERM");
                pila.push("EXPR'");
                break;

            case "EXPR'":
                if (esOperadorAditivo()) {
                    pila.push("EXPR'");
                    pila.push("TERM");
                    pila.push("OP_ADD");
                } else {
                    pila.push("ε");
                }
                break;

                //* Reglas para la expresión

            case "TERM":
                pila.push("TERM_END");
                pila.push("FACTOR");
                pila.push("TERM'");
                break;
            case "TERM'":
                if(esOperadorMultiplicativo()){
                    pila.push("TERM'");
                    pila.push("FACTOR");
                    pila.push("OP_MULT");
                }else{
                    pila.push("ε");
                }
                break;

            case "TERM_END":
                pila.push("ε");
                break;

            case "EXPR_END":
                pila.push("ε");
                break;

                case "FACTOR":
                    switch (tokenActual.tipo){
                        case "ENTERO":
                        case "ID":
                            pila.push(tokenActual.tipo);
                            break;
                        case"PAR_IZQ":
                            pila.push("PAR_DER");
                            pila.push("EXPR");
                            pila.push("PAR_IZQ");
                            break;

                        default:
                            throw new ErrorSintactico(tokenActual);
                    }
                    break;

                    case"OP_ADD":
                        if(tokenActual.lexema.equals("+") || tokenActual.lexema.equals("-")){
                            pila.push("OPERADOR");
                        }else{
                            throw new ErrorSintactico(tokenActual);
                        }
                        break;

            case "OP_MUL":
                if(tokenActual.lexema.equals("*") || tokenActual.lexema.equals("/") || tokenActual.lexema.equals("^")){
                    pila.push("OPERADOR");
                }else{
                    throw new ErrorSintactico(tokenActual);
                }
                break;

            default:
                throw new Error("No terminal no reconocido: " + noTerminal);
        }
    }

    private int evaluarExpresion() throws Exception {
        // Implementar evaluación completa de expresiones
        // (Usar algoritmo Shunting-yard o similar)
        return 0; // Temporal para compilación
    }

    private int obtenerValor(Lexer.Token token) {
        if (token.tipo.equals("ID")) {
            return tablaSimbolos.get(token.lexema.substring(1));
        }
        return Integer.parseInt(token.lexema);
    }

    private void escribirSalida(String contenido) throws IOException {
        outputWriter.write(contenido);
        outputWriter.newLine();
    }

    private Lexer.Token obtenerSiguienteToken() throws Exception {
        Lexer.Token token = lexer.yylex();
        if (token == null) token = new Lexer.Token("EOF", "", -1, -1);
        return token;
    }

    private boolean esTerminal(String simbolo) {
        return simbolo.equals("PRINT") || simbolo.equals("END") || 
               simbolo.equals("REPEAT") || simbolo.equals("INIT") || 
               simbolo.equals("IF") || simbolo.equals("THEN") || 
               simbolo.equals("ID") || simbolo.equals("ENTERO") || 
               simbolo.equals("LITERAL") || simbolo.equals("IGUAL") ||
                simbolo.equals("PAR_IZQ") ||
                simbolo.equals("PAR_DER") ||
                simbolo.equals("OPERADOR");
    }

    private void logDebug(String mensaje) {
        if (debugMode) System.out.println("[DEBUG] " + mensaje);
    }

    private static class ErrorSintactico extends Error {
        public ErrorSintactico(Lexer.Token token) {
            super(String.format("Error sintáctico en línea %d, columna %d: Token inesperado '%s'",
                    token.fila, token.columna, token.lexema));
        }
    }

    private boolean esOperadorAditivo() {
        return tokenActual.lexema.equals("+") || tokenActual.lexema.equals("-");
    }

    private boolean esOperadorMultiplicativo() {
        return tokenActual.lexema.equals("*") || tokenActual.lexema.equals("/");
    }

   /* public static void main(String[] args) {
        try {
            Lexer lexer = new Lexer(new FileReader("entrada.txt"));
            Parser parser = new Parser(lexer, "salida.txt");
            parser.analizar();
            System.out.println("Análisis completado exitosamente!");
        } catch (ErrorSintactico e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}