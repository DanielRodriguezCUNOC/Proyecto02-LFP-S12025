
%%
%class Lexer
%unicode
%line
%column
%type Token

%{
    private SymbolTable symbolTable = new SymbolTable();

    public Token token(String name, String lexeme) {
        return new Token(name, lexeme, yyline+1, yycolumn+1);
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]

/* Literales */
Literal = \"([^\"]|\\.)*\"

/* Números */
Integer = [+-]?[0-9]+

/* Identificadores */
Identifier = \$[a-zA-Z0-9_\-]+

/* Palabras reservadas */
Reserved = PRINT | END | REPEAT | INIT | IF | TRUE | FALSE | THEN

/* Operadores */
Operator = "+" | "-" | "*" | "/" | "^" | "=" | "(" | ")"

/* Comentarios */
LineComment = #.*
BlockComment = \/\*([^*]|\*+[^*/])*\*+\/

%%

{Literal}        { return token("LITERAL", yytext()); }
{Integer}        { return token("INTEGER", yytext()); }
{Identifier}     { return token("IDENTIFIER", yytext()); }
{Reserved}       { return token(yytext().toUpperCase(), yytext()); }
{Operator}       { return token("OPERATOR", yytext()); }
{LineComment}    { /* Ignorar comentarios de línea */ }
{BlockComment}   { /* Ignorar comentarios de bloque */ }
{WhiteSpace}     { /* Ignorar espacios en blanco */ }

[^]              { throw new Error("Carácter ilegal: " + yytext()); }