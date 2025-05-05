package org.analizador.partesintactica;

public class SyntaxError extends Exception {
    private final int line;
    private final int column;

    public SyntaxError(int line, int column, String message) {
        super(String.format("Error sintáctico en línea %d, columna %d: %s", line, column, message));
        this.line = line;
        this.column = column;
    }

    public SyntaxError(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
