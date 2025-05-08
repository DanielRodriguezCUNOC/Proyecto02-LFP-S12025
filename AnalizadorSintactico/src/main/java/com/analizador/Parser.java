package com.analizador;

import com.analizador.archivos.ArchivoPrint;

import java.io.*;
import java.util.*;


public class Parser {
    private Stack<String> pila = new Stack<>();
    private HashMap<String, Integer> tablaSimbolos = new HashMap<>();
    private Lexer.Token tokenActual;
    private Lexer lexer;
    private boolean debugMode = true;
    private boolean lastCondition;

    public Parser(Lexer lexer, String outputFile) throws IOException {
        this.lexer = lexer;
        ArchivoPrint.iniciar(outputFile);
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

            if(tope.equals("EOF")){
                if(!tokenActual.tipo.equals("EOF")){
                    throw new ErrorSintactico(tokenActual);
                }
                break;
            }

            if (esTerminal(tope)) {
                manejarTerminal(tope);
            } else {
                expandirNoTerminal(tope);
            }
        }
        ArchivoPrint.cerrar();
    }

    private void manejarTerminal(String terminal) throws Exception {
        if (terminal.equals(tokenActual.tipo)) {
            tokenActual = obtenerSiguienteToken();
        } else {
            throw new ErrorSintactico(tokenActual);
        }
    }

    private void expandirNoTerminal(String noTerminal) throws Exception {
        switch (noTerminal) {
            case "PROGRAM":
                pila.push("EOF");
                pila.push("MULTI_STRUCT");
                break;

            case "EXPR_END":
                pila.push("ε");
                break;

            case "STRUCT":
                if (tokenActual.lexema.equals("PRINT")) pila.push("PRINT_STRUCT");
                else if (tokenActual.lexema.equals("REPEAT")) pila.push("REPEAT_STRUCT");
                else if (tokenActual.lexema.equals("IF")) pila.push("CONDICIONAL_STRUCT");
                else if (tokenActual.tipo.equals("ID")) pila.push("ASIGNACION_STRUCT");
                else throw new ErrorSintactico(tokenActual);
                break;

            case "MULTI_STRUCT":
                if(tokenActual.tipo.equals("EOF")) {
                    pila.push("ε");
                }else{
                    pila.push("MULTI_STRUCT");
                    pila.push("STRUCT");
                }
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
                        ArchivoPrint.escribirLinea(tokenActual.lexema.replace("\"", ""), "LITERAL");
                        pila.push("LITERAL");
                        break;
                    case "ENTERO":
                        ArchivoPrint.escribirLinea(tokenActual.lexema, "NUMERO");
                        pila.push("ENTERO");
                        break;
                    case "ID":
                        String id = tokenActual.lexema.substring(1);
                        int valor = tablaSimbolos.get(id);
                        if(!tablaSimbolos.containsKey(id)){
                            throw new ErrorSintactico(tokenActual);
                        }
                        ArchivoPrint.escribirLinea(String.valueOf(valor), "ID");
                        pila.push("ID");
                        break;

                    case "LETRA":
                        ArchivoPrint.escribirLinea(tokenActual.lexema, "LITERAL");
                        tokenActual = obtenerSiguienteToken();
                        break;

                    default:
                        throw new ErrorSintactico(tokenActual);
                }
                break;

            // Estructura REPEAT
            case "REPEAT_STRUCT":
                pila.push("REPEAT_END");
                pila.push("BODY");
                pila.push("INIT");
                pila.push("REPEAT_COUNT");
                pila.push("REPEAT");
                break;

            case "BODY":
                if (tokenActual.lexema.equals("PRINT")) {
                    pila.push("BODY");
                    pila.push("PRINT_STRUCT");
                } else {
                    pila.push("ε");
                }
                break;

            case "REPEAT_COUNT":
                if (tokenActual.tipo.equals("ENTERO") || tokenActual.tipo.equals("ID")) {
                    //* Calculamos las repeticiones
                    int repeticiones = obtenerValor(tokenActual);
                    //* Consumimos el token
                    tokenActual = obtenerSiguienteToken();
                    //* Cerramos con ε
                    pila.push("ε");
                } else {
                    throw new ErrorSintactico(tokenActual);
                }
                break;

            case "REPEAT_END":
                pila.push("END");
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
                if(tokenActual.lexema.equals("TRUE") || tokenActual.lexema.equals("FALSE")){
                    //* Se guarda el flag y se consume el token
                    lastCondition = tokenActual.lexema.equals("TRUE");
                    tokenActual = obtenerSiguienteToken();
                    pila.push("ε");
                }else{
                    throw new ErrorSintactico(tokenActual);
                }
                break;

            case "CONDICIONAL_BODY":
                //* Solo hacemos push si la condicion es verdadera
                if (lastCondition && tokenActual.lexema.equals("PRINT")) {
                    pila.push("PRINT_STRUCT");
                }else{
                    pila.push("ε");
                }
                break;

            // Estructura ASIGNACION
            case "ASIGNACION_STRUCT":

                //* Consumimos el token ID
                String identificador = tokenActual.lexema.substring(1);
                tokenActual = obtenerSiguienteToken(); // Consumir el ID

                //* Consumir el token =
                if (!tokenActual.tipo.equals("IGUAL")) {
                    throw new ErrorSintactico(tokenActual);
                }
                tokenActual = obtenerSiguienteToken(); // Avanzar al siguiente token

                //* Reestructurar la pila para manejar END despues de EXPR
                //pila.push("END");
                //pila.push("EXPR");

                //* Evaluar la expresion y guardar en tabla de simbolos
                int valor = evaluarExpresion();
                //* Consumir el token END
                if (!tokenActual.tipo.equals("END")) {
                    throw new ErrorSintactico(tokenActual);
                }
                //* Actualizar la tabla de simbolos
                tablaSimbolos.put(identificador, valor);

                //* Configurar la pila para consumir END
                pila.push("END");
                break;

            // Manejo de expresiones
            case "EXPR":
                pila.push("EXPR_END");
                pila.push("EXPR'");
                pila.push("TERM");
                break;

            case "EXPR'":
                if (tokenActual.tipo.equals("SUMA") || tokenActual.tipo.equals("RESTA")) {
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
                pila.push("TERM'");
                pila.push("FACTOR");
                break;
            case "TERM'":
                if(tokenActual.tipo.equals("MULT") || tokenActual.tipo.equals("DIV") || tokenActual.tipo.equals("POT")){
                    pila.push("TERM'");
                    pila.push("FACTOR");
                    pila.push("OP_MUL");
                }else{
                    pila.push("ε");
                }
                break;

            case "TERM_END":
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

            case "PROGRAM_END":
                pila.push("ε");
                break;

            case "OP_ADD":
                if (tokenActual.tipo.equals("SUMA") || tokenActual.tipo.equals("RESTA")) {
                    //* Empujamos el terminal
                    pila.push(tokenActual.tipo);
                } else {
                    throw new ErrorSintactico(tokenActual);
                }
                break;

            case "OP_MUL":
                if (tokenActual.tipo.equals("MULT") || tokenActual.tipo.equals("DIV") ||
                        tokenActual.lexema.equals("POT")) {
                    //* Empujamos el terminal
                    pila.push(tokenActual.tipo);
                } else {
                    throw new ErrorSintactico(tokenActual);
                }
                break;

            default:
                throw new Error("No terminal no reconocido: " + noTerminal);
        }
    }

    private int evaluarExpresion() throws Exception {
        Stack<Integer> valores = new Stack<>();
        Stack<String> operadores = new Stack<>();

        // Consumir tokens hasta encontrar "END"
        while (!tokenActual.tipo.equals("END")) {
            switch (tokenActual.tipo) {
                case "ENTERO":
                    valores.push(Integer.parseInt(tokenActual.lexema));
                    tokenActual = obtenerSiguienteToken();
                    break;
                case "ID":
                    String id = tokenActual.lexema.substring(1);
                    if (!tablaSimbolos.containsKey(id)) {
                        throw new Error("Identificador no definido: " + id);
                    }
                    valores.push(tablaSimbolos.get(id));
                    tokenActual = obtenerSiguienteToken();
                    break;
                case "PAR_IZQ":
                    operadores.push("(");
                    tokenActual = obtenerSiguienteToken();
                    break;
                case "PAR_DER":
                    while (!operadores.peek().equals("(")) {
                        aplicarOperador(operadores.pop(), valores);
                    }
                    operadores.pop(); // Eliminar "("
                    tokenActual = obtenerSiguienteToken();
                    break;
                case "SUMA":
                case "RESTA":
                case "MULT":
                case "DIV":
                case "POT":
                    while (!operadores.isEmpty() && precedencia(operadores.peek()) >= precedencia(tokenActual.tipo)) {
                        aplicarOperador(operadores.pop(), valores);
                    }
                    operadores.push(tokenActual.tipo);
                    tokenActual = obtenerSiguienteToken();
                    break;
                default:
                    throw new ErrorSintactico(tokenActual);
            }
        }

        while (!operadores.isEmpty()) {
            aplicarOperador(operadores.pop(), valores);
        }

        return valores.pop();
    }

    private int precedencia(String op) {
        switch (op) {
            case "POT": return 3;
            case "MULT":
            case "DIV": return 2;
            case "SUMA":
            case "RESTA": return 1;
            default: return 0;
        }
    }

    private void aplicarOperador(String op, Stack<Integer> valores) {
        int b = valores.pop();
        int a = valores.pop();
        switch (op) {
            case "SUMA": valores.push(a + b); break;
            case "RESTA": valores.push(a - b); break;
            case "MULT": valores.push(a * b); break;
            case "DIV": valores.push(a / b); break;
            case "POT": valores.push((int) Math.pow(a, b)); break;
        }
    }

    private int obtenerValor(Lexer.Token token) {
        if (token.tipo.equals("ID")) {
            return tablaSimbolos.get(token.lexema.substring(1));
        }
        return Integer.parseInt(token.lexema);
    }

    private Lexer.Token obtenerSiguienteToken() throws Exception {
        Lexer.Token token = lexer.yylex();
        if (token == null) token = new Lexer.Token("EOF", "EOF", -1, -1);
        return token;
    }

    private boolean esTerminal(String simbolo) {
        return simbolo.equals("PRINT") || simbolo.equals("END") ||
               simbolo.equals("REPEAT") || simbolo.equals("INIT") ||
               simbolo.equals("IF") || simbolo.equals("THEN") ||
               simbolo.equals("ID") || simbolo.equals("ENTERO") ||
               simbolo.equals("LITERAL") || simbolo.equals("IGUAL") ||
                simbolo.equals("PAR_IZQ") || simbolo.equals("PAR_DER") ||
                simbolo.equals("SUMA") || simbolo.equals("RESTA") || simbolo.equals("MULT") ||
                simbolo.equals("DIV") || simbolo.equals("POT") ||
                simbolo.equals("TRUE") || simbolo.equals("FALSE");
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

}