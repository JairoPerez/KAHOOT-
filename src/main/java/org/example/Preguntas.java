package org.example;

import java.util.ArrayList;
import java.util.List;

public class Preguntas {
    private String pregunta;
    private List<String> opciones;
    private int respuesta;
    private int tiempo;

    // Setters y Getters (deben coincidir con los nombres del JSON)
    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public Preguntas() {
        this.opciones = new ArrayList<>();
    }

    // Setters y Getters
    public String getPreguntas() { return pregunta; }
    public void setPreguntas(String preguntas) { this.pregunta = preguntas; }

    public List<String> getOpciones() { return opciones; }
    public void setOpciones(List<String> opciones) { this.opciones = opciones; }

    public int getRespuesta() { return respuesta; }
    public void setRespuesta(int respuesta) { this.respuesta = respuesta; }

    public int getTiempo() { return tiempo; }
    public void setTiempo(int tiempo) { this.tiempo = tiempo; }

    public String formatear() {
        StringBuilder sb = new StringBuilder();
        sb.append(pregunta).append("\n");
        for (int i = 0; i < opciones.size(); i++) {
            sb.append((i + 1) + ") ").append(opciones.get(i)).append("\n");
        }
        return sb.toString();
}}