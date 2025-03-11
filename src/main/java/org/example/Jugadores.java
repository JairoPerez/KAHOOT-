package org.example;

import java.io.*;
import java.net.*;

public class Jugadores extends Thread {
    private Socket socket;
    private Presentador presentador;
    private PrintWriter salida;
    private BufferedReader entrada;
    private String nombreJugador;

    public Jugadores(Socket socket, Presentador presentador) throws IOException {
        this.socket = socket;
        this.presentador = presentador;
        this.salida = new PrintWriter(socket.getOutputStream(), true);
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            salida.println("Ingrese su nombre:");
            nombreJugador = entrada.readLine();
            presentador.registrarJugador(this);  // referencia a la instancia actual de la clase

            salida.println("Bienvenido " + nombreJugador + "! Esperando a que inicie el juego...");

            while (true) {
                String input = entrada.readLine();
                if (input == null || input.equals("Cerrando el juego... ¡Gracias por jugar!")) {
                    break;
                }

                if (presentador.kahootEjecutandose()) {
                    try {
                        int respuesta = Integer.parseInt(input);
                        if (respuesta >= 1  && respuesta <= 4) {
                            presentador.registrarPregunta(nombreJugador, respuesta);
                        }else {
                            enviarMensaje("Respuesta inválida. Ingrese un número del 1 al 4.");
                        }
                    } catch (NumberFormatException e) {
                        enviarMensaje("Respuesta inválida. Ingrese un número.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + nombreJugador + " no registrado");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void enviarMensaje(String message) {
        salida.println(message);
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public Socket getSocket() {
        return socket;
    }
}
