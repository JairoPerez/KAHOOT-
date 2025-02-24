package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Lector {
    public static List<Preguntas> cargarPreguntas(String rutaArchivo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = Lector.class.getResourceAsStream(rutaArchivo);
        if (is == null) {
            throw new IOException("No se pudo encontrar el archivo: " + rutaArchivo);
        }
        PreguntasWrapper wrapper = mapper.readValue(is, PreguntasWrapper.class);
        return wrapper.getPreguntas();
    }

    private static class PreguntasWrapper {
        private List<Preguntas> preguntas;

        public List<Preguntas> getPreguntas() {
            return preguntas;
        }

        public void setPreguntas(List<Preguntas> preguntas) {
            this.preguntas = preguntas;
        }
    }
}