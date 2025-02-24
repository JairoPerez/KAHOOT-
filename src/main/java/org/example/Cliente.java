package org.example;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVIDOR = "localhost"; // Cambia a la IP del servidor si es necesario
    private static final int PUERTO = 4444;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVIDOR, PUERTO);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor Kahoot!");

            // Escuchar mensajes del servidor en un hilo separado
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        System.out.println(mensaje);
                    }
                } catch (IOException e) {
                    System.out.println("Desconectado del servidor.");
                }
            }).start();

            // Leer del teclado y enviar al servidor
            String input;
            while ((input = teclado.readLine()) != null) {
                out.println(input);
            }

        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor: " + e.getMessage());
        }
    }
}

