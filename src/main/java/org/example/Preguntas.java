package org.example;

import java.util.ArrayList;
import java.util.List;

public class Preguntas {
    private String pregunta;
    private List<String> opciones;
    private int respuesta;

    public Preguntas() {
        this.opciones = new ArrayList<>();
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public List<String> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<String> opciones) {
        this.opciones = opciones;
    }

    public int getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(int respuesta) {
        this.respuesta = respuesta;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(pregunta).append("\n");
        for (int i = 0; i < opciones.size(); i++) {
            sb.append((i + 1) + ") ").append(opciones.get(i)).append("\n");
        }
        return sb.toString();
    }
}