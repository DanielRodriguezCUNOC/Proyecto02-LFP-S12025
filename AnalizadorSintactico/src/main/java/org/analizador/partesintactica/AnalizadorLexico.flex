%%
%class AnalizadorLexico
%unicode
%line
%column
%type Token
%public

%{
    // Clase Token que usaremos para representar los tokens encontrados
    public static class Token {
        public final String lexema;
        public final String tipo;
        public final int linea;
        public final int columna;

        public Token(String lexema, String tipo, int linea, int columna) {
            this.lexema = lexema;
            this.tipo = tipo;
            this.linea = linea;
            this.columna = columna;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) [%d:%d]", lexema, tipo, linea, columna);
        }
    }
%}

// Definición de patrones
DIGITO = [0-9]
LETRA = [a-zA-Z]
SIGNO = [+-]
ESPACIO = [ \t\r\n\f]
ID = \$({LETRA}|{DIGITO}|[-_])+
ENTERO = {SIGNO}?[1-9]{DIGITO}*|0
LITERAL = \"[^\"]*\"
COMENTARIO_LINEA = \#.*
COMENTARIO_BLOQUE = "/\*"([^*]*\*+)*([^*/][^*]*\*+)*"/"

%%
// Reglas léxicas

{ESPACIO}       { /* Ignorar espacios en blanco */ }

/* Palabras reservadas */
"PRINT"         { return new Token(yytext(), "PRINT", yyline+1, yycolumn+1); }
"END"           { return new Token(yytext(), "END", yyline+1, yycolumn+1); }
"REPEAT"        { return new Token(yytext(), "REPEAT", yyline+1, yycolumn+1); }
"INIT"          { return new Token(yytext(), "INIT", yyline+1, yycolumn+1); }
"IF"            { return new Token(yytext(), "IF", yyline+1, yycolumn+1); }
"TRUE"          { return new Token(yytext(), "TRUE", yyline+1, yycolumn+1); }
"FALSE"         { return new Token(yytext(), "FALSE", yyline+1, yycolumn+1); }
"THEN"          { return new Token(yytext(), "THEN", yyline+1, yycolumn+1); }

/* Operadores y símbolos */
"+"             { return new Token(yytext(), "SUMA", yyline+1, yycolumn+1); }
"-"             { return new Token(yytext(), "RESTA", yyline+1, yycolumn+1); }
"*"             { return new Token(yytext(), "MULT", yyline+1, yycolumn+1); }
"/"             { return new Token(yytext(), "DIV", yyline+1, yycolumn+1); }
"^"             { return new Token(yytext(), "POT", yyline+1, yycolumn+1); }
"("             { return new Token(yytext(), "PAR_IZQ", yyline+1, yycolumn+1); }
")"             { return new Token(yytext(), "PAR_DER", yyline+1, yycolumn+1); }
"="             { return new Token(yytext(), "ASIGN", yyline+1, yycolumn+1); }

/* Literales y otros tokens */
{ENTERO}        { return new Token(yytext(), "ENTERO", yyline+1, yycolumn+1); }
{ID}            { return new Token(yytext(), "ID", yyline+1, yycolumn+1); }
{LITERAL}       { return new Token(yytext(), "LITERAL", yyline+1, yycolumn+1); }
{COMENTARIO_LINEA}  { /* Ignorar comentarios de línea */ }
{COMENTARIO_BLOQUE} { /* Ignorar comentarios de bloque */ }

/* Cualquier otro carácter no reconocido */
.               { return new Token(yytext(), "ERROR", yyline+1, yycolumn+1); }

<<EOF>>         { return new Token("EOF", "EOF", yyline+1, yycolumn+1); }
