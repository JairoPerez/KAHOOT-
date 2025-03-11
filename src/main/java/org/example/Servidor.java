package org.example;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Servidor {
    public static final int port = 1234;

    public static void main(String[] args) throws Exception {
        ServerSocket socketServidor = new ServerSocket(port);
        Presentador gestor = new Presentador();

        System.out.println("\n!BIENVENIDO AL SERVIDOR KAHOOT!");
        System.out.println("\nEscriba `start` para comenzar o `exit` para salir.\n");

        new Thread(() -> {
            while (true) {
                try {
                    Socket socketCliente = socketServidor.accept();
                    new Jugadores(socketCliente, gestor).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String input = sc.nextLine();
                if (gestor.isEsperandoReinicio()) {
                    if (input.equalsIgnoreCase("s")) {
                        gestor.esperandoReinicio = false;
                        gestor.empezarKahoot();
                    } else if (input.equalsIgnoreCase("n")) {
                        System.out.println("Juego finalizado. Cerrando el servidor...");
                        System.exit(0);
                    } else {
                        System.out.println("Respuesta no correcta. Ingrese `s` o `n`.");
                    }
                } else {
                    if (input.equalsIgnoreCase("start")){
                        gestor.empezarKahoot();
                    }else if (input.equalsIgnoreCase("exit")){
                       System.exit(0);
                    }else{
                        System.out.println("Comando no reconocido. Escribe `start` para comenzar.");
                    }

                }
            }
        }).start();
    }
}