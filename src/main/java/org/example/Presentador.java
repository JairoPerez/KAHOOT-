package org.example;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.*;
import java.io.IOException;

public class Presentador {
    boolean esperandoReinicio, kahootActivo= false;
    private List<Preguntas> arraypreguntas;
    private HashMap<String, Integer> puntos;
    private List<Jugadores> jugadores;
    private int preguntaActual = 0;
    private Set<String> respuestasRecibidas; //Set no permite valores duplicados, evita que un jugador envíe varias respuestas a la misma pregunta.

    public Presentador(){
        this.arraypreguntas = cargarPreguntas("src/main/resources/preguntas.json");
        Collections.shuffle(arraypreguntas);
        arraypreguntas.subList(0, Math.min(10, arraypreguntas.size()));
        this.puntos = new HashMap<>();
        this.jugadores = Collections.synchronizedList(new ArrayList<>());
        this.respuestasRecibidas = Collections.synchronizedSet(new HashSet<>());
    }

    private List<Preguntas> cargarPreguntas(String archivo) {
        try (FileReader reader = new FileReader(archivo)) {
            return new Gson().fromJson(reader, new TypeToken<List<Preguntas>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized void empezarKahoot() {
        if (kahootActivo || jugadores.isEmpty()) return;
        kahootActivo = true;
        preguntaActual = 0;

        mensajeGlobal("\n¡El juego comienza ahora!\n");
        new Thread(this::iniciarPreguntas).start();
    }

    private void iniciarPreguntas() {
        int numeroPreguntas = 10;
        mensajeGlobal("Iniciando juego con " + numeroPreguntas + " preguntas.");

        for (preguntaActual = 0; preguntaActual<numeroPreguntas; preguntaActual++) {
            Preguntas pregunta = arraypreguntas.get(preguntaActual);
            respuestasRecibidas.clear();

            mensajeGlobal("\n Pregunta " + (preguntaActual + 1) + ":\n" + pregunta.toString());
            while (respuestasRecibidas.size()<jugadores.size()){
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            mensajeGlobal("\n Todos los jugadores han respondido.\n");
        }
        finalizarKahoot();
    }

    public synchronized void registrarPregunta(String jugador, int respuesta) {
        if (respuestasRecibidas.contains(jugador)) return;
        respuestasRecibidas.add(jugador);

        Preguntas pregunta = arraypreguntas.get(preguntaActual);
        if (respuesta == pregunta.getRespuesta() + 1) {
            int puntosGanados = 1000;
            if (puntos.containsKey(jugador)){
                puntos.put(jugador, puntos.get(jugador)+puntosGanados);
            }else{
                puntos.put(jugador, puntosGanados);
            }
            enviarCliente(jugador, " Correcto! +" + puntosGanados + " puntos");
        } else {
            enviarCliente(jugador, " Incorrecto! La respuesta era: " + (pregunta.getRespuesta() + 1));
        }
        if (respuestasRecibidas.size() < jugadores.size()){
            enviarCliente(jugador, "Esperando a que los demas jugadores respondan...");
        }
    }

    private void mostrarResultados() {
        StringBuilder resultados = new StringBuilder("\nResultados finales:\n");

        puntos.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) //Muestra los resultados de mayor a menor
                .forEach(entry -> resultados.append(entry.getKey()).append(": ").append(entry.getValue()).append(" puntos\n"));

        mensajeGlobal(resultados.toString());
    }

    private void finalizarKahoot() {
        kahootActivo = false;
        mostrarResultados();
        reiniciarKahoot();

        System.out.println("\n ¿Jugar otra vez? (s/n)");
        esperandoReinicio=true;
    }
    private void reiniciarKahoot() {
        Collections.shuffle(arraypreguntas);
        this.arraypreguntas = arraypreguntas.subList(0, Math.min(10, arraypreguntas.size()));
        puntos.clear();
    }

    public synchronized void registrarJugador(Jugadores jugador) {
        if (kahootActivo) {
            jugador.enviarMensaje("No puedes unirte, el juego ya ha empezado.");
            try {
                jugador.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (jugadores.size() >= 5) {
            jugador.enviarMensaje("Lo siento, el juego ya tiene 5 jugadores. No hay espacio disponible.");
            try {
                jugador.getSocket().close();
                System.out.println("Conexión cerrada para " + jugador.getNombreJugador() + " (no hay espacio).");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        jugadores.add(jugador);
        puntos.put(jugador.getNombreJugador(), 0);
        System.out.println(jugador.getNombreJugador()+" se ha unido a la partida.");
    }
    private void mensajeGlobal(String message) {
        jugadores.forEach(jugador -> jugador.enviarMensaje(message));
    }

    private void enviarCliente(String nombreCliente, String msg) {
        jugadores.stream()
                .filter(c -> c.getNombreJugador().equals(nombreCliente)) // Filtra jugadores cuyo nombre coincide con nombreCliente pasas como argumento.
                .forEach(c -> c.enviarMensaje(msg));
    }

    public boolean kahootEjecutandose() {
        return kahootActivo;
    }
    public boolean isEsperandoReinicio(){
        return esperandoReinicio;
    }
}
