package com.analizador.archivos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ArchivoPrint {

    private static BufferedWriter writer;

    //* Inicializar el writer
    public static void iniciar(String rutaSalida) throws IOException {
        writer = new BufferedWriter(new FileWriter(rutaSalida));
    }

    //* Cerrar el writer
    public static void cerrar() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    //* Escribir en el archivo
    public static void escribirLinea(String texto, String tipo) throws IOException {
        String linea;
        switch (tipo){
            case "ID":
                linea = "Contenido Identificador: " + texto;
                break;

            case "LITERAL":
                linea = "Contenido Literal: " + texto;
                break;

            case "NUMERO":
                linea = "Contenido Numero: " + texto;
                break;

            default:
                linea = texto;
        }
        writer.write(linea);
        writer.newLine();
    }
}
