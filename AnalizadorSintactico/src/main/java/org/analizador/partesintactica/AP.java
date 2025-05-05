package org.analizador.partesintactica;

import org.analizador.partelexica.Token;

import java.util.List;
import java.util.Stack;

public class AP {

    private Stack<String> stack = new Stack<>();
    private TablaSimbolos tablaSimbolos;
    private StringBuilder output = new StringBuilder();
    private boolean currentCondition = false;

    public AP() {
        this.tablaSimbolos = new TablaSimbolos();
        //Inicializar pila
        stack.push("S");
    }

    public String parse(List<Token> tokens) throws SyntaxError {
        int index = 0;
        currentCondition = false;

        while (!stack.isEmpty() && index < tokens.size()) {
            String top = stack.peek();
            Token current = tokens.get(index);


            if (top.equals(current.tipo)) {
                // Coincidencia, avanzar
                stack.pop();
                index++;

                if (current.tipo.equals("TRUE")){
                    currentCondition = true;
                }else if (current.tipo.equals("FALSE")){
                    currentCondition = false;
                }else if (current.tipo.equals("IDENTIFIER") && stack.peek().equals("=")) {
                    // Estamos en una asignación
                    handleAssignment(current);
                }
            } else if (isNonTerminal(top)) {
                // Expandir según la gramática
                expandProduction(top, current);
            } else {
                throw new SyntaxError(current.linea, current.columna,
                        "Se esperaba " + top + " pero se encontró " + current.tipo);
            }
        }

        if (!stack.isEmpty()) {
            throw new SyntaxError("Fin de entrada inesperado");
        }

        return output.toString();
    }


    private void expandProduction(String nonTerminal, Token lookahead) throws SyntaxError {

        //Sacamos el no terminal
        stack.pop();

        //Estructura PRINT
        if(nonTerminal.equals("S")){
            if (lookahead.tipo.equals("PRINT")) {
                stack.push("END");
                stack.push("EXPR");
                stack.push("PRINT");
            }
            else if (lookahead.tipo.equals("IF")) {
                stack.push("CONDICIONAL");
            } else if (lookahead.tipo.equals("IDENTIFICADOR")) {
                stack.push("ASIGNACION");
            }
        } else if (nonTerminal.equals("CONDICIONAL")) {
            stack.push("END");
            stack.push("OPTIONAL_PRINT");
            stack.push("THEN");
            stack.push("BOOL_VALUE");
            stack.push("IF");
        } else if (nonTerminal.equals("BOOL_VALUE")) {
            if (lookahead.tipo.equals("TRUE") || lookahead.tipo.equals("FALSE")) {
                stack.push(lookahead.tipo);
            } else {
                throw new SyntaxError(lookahead.linea, lookahead.columna,
                        "Se esperaba un valor booleano (TRUE o FALSE) pero se encontró " + lookahead.tipo);
            }
        } else if (nonTerminal.equals("OPTIONAL_PRINT")) {
            if (lookahead.tipo.equals("PRINT")){
                stack.push("PRINT_STRUCT");
            }
        }

    }

    private void handleAssignment(Token idToken) {
        // Aquí se maneja la asignación
        String id = idToken.lexema;
        int value = Integer.parseInt(stack.peek());
        tablaSimbolos.put(id, value);
    }

    private boolean isNonTerminal(String symbol) {
        // Aquí se define qué es un no terminal
        return symbol.equals("S") || symbol.equals("EXPR") || symbol.equals("PRINT_STRUCT");
    }

}