package org.analizador.partelexica;

public class Token {
    public final String tipo;
    public final String lexema;
    public final int linea;
    public final int columna;

    public Token(String type, String lexeme, int line, int column) {
        this.tipo = type;
        this.lexema = lexeme;
        this.linea = line;
        this.columna = column;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) [%d:%d]", tipo, lexema, linea, columna);
    }
}
