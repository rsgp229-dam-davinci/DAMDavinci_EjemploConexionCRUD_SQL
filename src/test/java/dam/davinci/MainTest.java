package dam.davinci;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void crearConexion() {
        assertDoesNotThrow(() ->{
            Connection connection = Main.crearConexion();
            System.out.println("Connectado a " + connection.getMetaData().getDatabaseProductName());
        });
    }

    @Test
    void insertarAlumno() {
        assertDoesNotThrow(() -> {
            Alumno alumno = new Alumno();
            alumno.setNombre("Antonomasio");
            alumno.setApellidos("Predisposio");
            Main.insertarAlumno(Main.crearConexion(), alumno);
        });
    }

    @Test
    void buscarAlumnoPorNombre() {
        assertDoesNotThrow(() -> {
            String nombre = "Antonomasio";
            List<Alumno> alumnos = Main.buscarAlumnoPorNombre(Main.crearConexion(), nombre);
            assertEquals(1, alumnos.size());
            assertEquals(nombre, alumnos.get(0).getNombre());
            for (Alumno alumno : alumnos) {
                System.out.println(alumno.getId() + " " + alumno.getNombre() + " " + alumno.getApellidos());
            }
        });
    }

    @Test
    void borrarAlumnoPorNombre() {
        assertDoesNotThrow(() -> {
            String nombre = "Antonomasio";
            Main.borrarAlumnoPorNombre(Main.crearConexion(), nombre);
        });
    }

    @Test
    void consultarTodosAlumnos() {
        assertDoesNotThrow(() -> {
            List<Alumno> alumnos = Main.consultarTodosAlumnos(Main.crearConexion());
            for (Alumno alumno : alumnos) {
                System.out.println(alumno.getId() + " " + alumno.getNombre() + " " + alumno.getApellidos());
            }
        });
    }
}