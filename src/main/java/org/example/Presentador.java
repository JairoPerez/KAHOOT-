package org.example;

import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

public class Presentador {
    private List<Preguntas> arraypreguntas;
    private ConcurrentHashMap<String, Integer> puntos;
    private List<Jugadores> jugadores;
    private ScheduledExecutorService cronometro;
    private boolean kahootEnProgreso = false;
    private int preguntaActual = 0;
    private Set<String> respuestasRecibidas;

    public Presentador() throws IOException {

        this.arraypreguntas = Lector.cargarPreguntas("/preguntas.json");
        this.puntos = new ConcurrentHashMap<>();
        this.jugadores = Collections.synchronizedList(new ArrayList<>());
        this.cronometro = Executors.newScheduledThreadPool(1);
        this.respuestasRecibidas = Collections.synchronizedSet(new HashSet<>());
    }

    public synchronized void startQuiz() {
        if (kahootEnProgreso || jugadores.isEmpty()) return;
        kahootEnProgreso = true;
        preguntaActual = 0;

        broadcastMessage("\n📢 ¡El juego comienza en 5 segundos!\n");
        sleep(5000);

        new Thread(this::iniciarPreguntas).start();
    }

    private void iniciarPreguntas() {
        for (; preguntaActual < arraypreguntas.size(); preguntaActual++) {
            Preguntas pregunta = arraypreguntas.get(preguntaActual);
            respuestasRecibidas.clear();
            broadcastMessage("\n🔹 Pregunta " + (preguntaActual + 1) + ":\n" + pregunta.formatear());
            empezarCuentaAtras(pregunta.getTiempo());
            sleep(pregunta.getTiempo() * 1000L);
        }
        finalizarKahoot();
    }

    private void empezarCuentaAtras(int segundos) {
        // Programar para que la cuenta atrás de 5 segundos inicie cuando queden 5 segundos
        cronometro.schedule(() -> {
            // Mostrar la cuenta regresiva desde 5 hasta 1 segundos
            for (int i = 5; i > 0; i--) {
                broadcastMessage("⏱ " + i + "...");
                sleep(1000); // Esperar 1 segundo entre cada mensaje
            }
            // Mostrar cuando el tiempo se termine
            broadcastMessage("\n⏳ ¡Tiempo terminado para esta pregunta!\n");
        }, segundos - 5, TimeUnit.SECONDS); // Comienza la cuenta atrás 5 segundos antes de que se acabe el tiempo
    }

    public synchronized void registrarPregunta(String clientName, int answer, long responseTime) {
        if (respuestasRecibidas.contains(clientName)) return;
        respuestasRecibidas.add(clientName);

        Preguntas pregunta = arraypreguntas.get(preguntaActual);
        if (answer == pregunta.getRespuesta() + 1) {
            // Tiempo máximo por pregunta (en milisegundos)
            long tiempoMaximo = pregunta.getTiempo() * 1000L;

            // Tiempo que tardó el jugador en responder (en milisegundos)
            long tiempoRespuesta = System.currentTimeMillis() - responseTime;

            // Calcula los puntos (0 a 1000)
            int puntosGanados = calcularPuntos(tiempoRespuesta, tiempoMaximo);

            puntos.merge(clientName, puntosGanados, Integer::sum);
            enviarACliente(clientName, "✅ Correcto! +" + puntosGanados + " puntos");
        } else {
            enviarACliente(clientName, "❌ Incorrecto! La respuesta era: " + (pregunta.getRespuesta() + 1));
        }
    }
    private int calcularPuntos(long tiempoRespuesta, long tiempoMaximo) {
        // Asegura que el tiempo de respuesta no sea negativo
        if (tiempoRespuesta < 0) tiempoRespuesta = 0;

        // Si el jugador se pasa del tiempo máximo, obtiene 0 puntos
        if (tiempoRespuesta > tiempoMaximo) return 0;

        // Calcula los puntos en función del tiempo (más rápido = más puntos)
        double porcentaje = 1.0 - ((double) tiempoRespuesta / tiempoMaximo);
        return (int) (porcentaje * 1000); // Puntos entre 0 y 1000
    }

    private void finalizarKahoot() {
        kahootEnProgreso = false;
        mostrarResultados();
        reiniciarKahoot();
    }

    private void mostrarResultados() {
        StringBuilder ranking = new StringBuilder("\n🏆 RANKING FINAL 🏆\n");
        puntos.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> ranking.append(e.getKey()).append(": ").append(e.getValue()).append(" puntos\n"));
        broadcastMessage(ranking.toString());
    }

    private void reiniciarKahoot() {
        puntos.clear();
        broadcastMessage("\n¿Jugar otra vez? (s/n)");
    }

    public synchronized void registrarJugador(Jugadores jugador) {
        if (kahootEnProgreso) {
            jugador.sendMessage("🚫 No puedes unirte, el juego ya está en curso.");
            return;
        }
        jugadores.add(jugador);
        puntos.put(jugador.getNombreJugador(), 0);
    }

    private void broadcastMessage(String message) {
        jugadores.forEach(jugador -> jugador.sendMessage(message));
    }

    private void enviarACliente(String clientName, String message) {
        jugadores.stream()
                .filter(c -> c.getNombreJugador().equals(clientName))
                .forEach(c -> c.sendMessage(message));
    }

    public boolean KahootEnProcesoONo() {
        return kahootEnProgreso;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
