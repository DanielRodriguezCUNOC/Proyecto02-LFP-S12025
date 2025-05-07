
%%
%class Lexer
%unicode
%line
%column
%type Token

%{
    public static class Token {
        public final String tipo;
        public final String lexema;
        public final int fila;
        public final int columna;

        public Token(String tipo, String lexema, int fila, int columna) {
            this.tipo = tipo;
            this.lexema = lexema;
            this.fila = fila;
            this.columna = columna;
        }
    }
%}

// Definiciones
DIGITO = [0-9]
LETRA = [a-zA-Z]
SIGNO = [+-]
ID = \$([a-zA-Z0-9_-]+)
LITERAL = \"([^\"]*)\"
COMENTARIO_LINEA = #.*
COMENTARIO_BLOQUE = \/\*([^*]|(\*+[^*/]))*\*+\/
PAR_IZQ = "("
PAR_DER = ")"

// Palabras reservadas
PALABRAS = "PRINT"|"END"|"REPEAT"|"INIT"|"IF"|"TRUE"|"FALSE"|"THEN"

%%
// Operadores individuales
"+"                   { return new Token("SUMA", yytext(), yyline+1, yycolumn+1); }
"-"                   { return new Token("RESTA", yytext(), yyline+1, yycolumn+1); }
"*"                   { return new Token("MULT", yytext(), yyline+1, yycolumn+1); }
"/"                   { return new Token("DIV", yytext(), yyline+1, yycolumn+1); }
"^"                   { return new Token("POT", yytext(), yyline+1, yycolumn+1); }

{SIGNO}?{DIGITO}+     {
    if (yytext().length() > 1 || (yytext().charAt(0) != '+' && yytext().charAt(0) != '-')) {
        return new Token("ENTERO", yytext(), yyline+1, yycolumn+1);
    } else {
        throw new Error("Carácter ilegal: " + yytext());
    }
}
{PAR_IZQ}             { return new Token("PAR_IZQ", yytext(), yyline+1, yycolumn+1); }
{PAR_DER}             { return new Token("PAR_DER", yytext(), yyline+1, yycolumn+1); }
//{PALABRAS}            { return new Token("PALABRA", yytext(), yyline+1, yycolumn+1); }
{PALABRAS}   {
     String w = yytext();
     return new Token(w, w, yyline+1, yycolumn+1);
}
{DIGITO}+             { return new Token("ENTERO", yytext(), yyline+1, yycolumn+1); }
{ID}                  { return new Token("ID", yytext(), yyline+1, yycolumn+1); }
{LITERAL}             { return new Token("LITERAL", yytext(), yyline+1, yycolumn+1); }
"="                   { return new Token("IGUAL", yytext(), yyline+1, yycolumn+1); }
{LETRA}+              { return new Token("LETRA", yytext(), yyline+1, yycolumn+1); }
{COMENTARIO_LINEA}    { /* Ignorar */ }
{COMENTARIO_BLOQUE}   { /* Ignorar */ }
[ \t\n\r]             { /* Ignorar espacios */ }
.                     { throw new Error("Carácter ilegal: " + yytext()); }