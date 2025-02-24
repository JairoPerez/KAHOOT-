package org.example;
import java.io.*;
import java.net.*;

public class Servidor {
    public static final int Puerto = 4444;

    public static void main(String[] args) throws IOException {
        ServerSocket socketServidor = new ServerSocket(Puerto);
        Presentador gestor = new Presentador();

        System.out.println("Servidor iniciado en puerto " + Puerto);

        while (true) {
            Socket socketCliente = socketServidor.accept();
            new Jugadores(socketCliente, gestor).start();
        }
    }
}