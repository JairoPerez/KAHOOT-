package org.example;

import java.io.*;
import java.net.*;

public class Jugadores extends Thread {
    private Socket socket;
    private Presentador presentador;
    private PrintWriter out;
    private BufferedReader in;
    private String nombreJugador;
    private boolean registrado = false;

    public Jugadores(Socket socket, Presentador presentador) throws IOException {
        this.socket = socket;
        this.presentador = presentador;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            out.println("Ingrese su nombre:");
            nombreJugador = in.readLine();
            presentador.registrarJugador(this);
            registrado = true;

            out.println("Bienvenido " + nombreJugador + "! Esperando a que inicie el juego...");
            out.println("Para empezar escribe S.");

            while (true) {
                String input = in.readLine();
                if (input == null) break;

                if (input.equalsIgnoreCase("s") && !presentador.KahootEnProcesoONo()) {
                    presentador.startQuiz();
                } else if (presentador.KahootEnProcesoONo()) {
                    try {
                        int respuesta = Integer.parseInt(input);
                        long responseTime = System.currentTimeMillis();
                        presentador.registrarPregunta(nombreJugador, respuesta, responseTime);
                    } catch (NumberFormatException e) {
                        sendMessage("Respuesta inválida. Ingrese un número.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + (registrado ? nombreJugador : "no registrado"));
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getNombreJugador() {
        return nombreJugador;
    }
}
