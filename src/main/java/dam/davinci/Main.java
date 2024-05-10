package dam.davinci;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/* Las librerías (drivers) tienen que estar añadidas como dependencias mediante 'jarfiles' en la carpeta del 'ClassPath'
 * o mediante algún gestor de paquetes. En este caso se ha añadido mediante Maven.
 * Cada driver declara en su paquete que es un controlador de acceso a una base de datos, de esta manera la máquina
 * virtual de Java lo detectará e intentará ejecutar las instrucciones sobre uno de los controladores disponibles.
 *
 * El funcionamiento en general para el uso de una base de datos es:
 * 1.- Crear la conexión
 * 2.- Ejecutar la consulta o actualización
 * 3.- Procesar la información recibida
 * 4.- Cerrar la conexión
 *
 * La base de datos que se va a utilizar es una instancia de CockroachDB en la nube, en la que se ha creado una
 * tabla 'alumnos' con el siguiente esquema:
 * nombre       tipo            notas
 * id           int             este campo se generará automáticamente, no es necesario introducirlo en el insert
 * nombre       string(50)      not null
 * apellidos    string(150)
 */


 /** Las bases de datos no trabajan con clases y Java no trabaja con filas de bases de datos, por lo que debe crearse
  * un tipo que corresponda, al menos, con los campos de la base de datos. A las clases del código que representan a
  * la base de datos se denominan 'modelo'.
  * La clase Alumno es nuestro 'modelo' que corresponde con la tabla 'alumnos' de la base de datos.
  * La clase se mantiene lo más sencilla posible, pero puede contener tanta lógica como necesitemos.
  */
class Alumno{
    private long id;
    private String nombre;
    private String apellidos;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
}


public class Main {

    /**
     * Este método se encarga únicamente de crear la conexión.
     *
     * @return La conexión establecida con la base de datos o null si ocurre un error durante la conexión.
     */
    public static Connection crearConexion() {
        /* Lo primero es crear una conexión a la base de datos; para ello necesitamos la URL de conexión, el usuario
         * y la contraseña. La cadena de conexión la tenéis que conseguir de vuestra propia base de datos por algún
         * medio; en caso de duda consultar la documentación.
         * IMPORTANTE: No debe incluirse la contraseña en el código real BAJO NINGÚN CONCEPTO. */
        String url ="jdbc:postgresql://prog10-dam-davinci-9651.7tc.aws-eu-central-1.cockroachlabs.cloud:26257/prog2324?sslmode=require";
        String usuario = "progdemo";
        String contrasenya = "rhuwRpjFB4DEWUwhiSt2dQ";

        /* Con los datos de conexión creamos un objeto Connection que utilizaremos para enviar y recibir los datos.
         * TODAS las interacciones con la base devolverán un SQLException en caso de error. */
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(url, usuario, contrasenya);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Si todo ha salido bien, la variable conexion debe contener la conexión a la BD.
        return conexion;
    }

    /* Por medio de la conexión creada, le mandaremos a la base de datos los comandos SQL que queremos que ejecute.
    * La representación de un comando SQL en el código corresponde con un objeto de tipo 'statement'. Normalmente
    * se crea un String que contiene el comando; se compone igual que lo haríamos en la consola de SQL.
    * El objeto 'statement' contiene la información de la conexión y el comando o sentencia SQL.
    * Los objetos statement se extraen de la conexión, es decir que no crearemos un statement (new Statement...),
    * sino que se lo pediremos a la conexión.
    * El statement permite añadir parámetros hasta que tengamos completamente formada la sentencia SQL como queramos
    * y cuando esté lista, le decimos que se ejecute; si es un query 'executeQuery', si es un update 'executeUpdate'.
    * En este código se va a utilizar un 'PreparedStatement' ya que es una manera en la que se minimizan los ataques
    * maliciosos a las bases de datos.
    * Lo que diferencia a un 'PreparedStatement' es que los valores que se van a insertar en la instrucción SQL se
    * marcan con el comodín '?' dentro de la sentencia SQL y van numerados de izquierda a derecha comenzando con 1.
    *
    * En el código de los siguientes métodos irá viendo.
    * */


    /**
     * En una aplicación real, cada modelo lleva asociado una clase que se encarga de realizar las operaciones
     * con la base de datos; estas clases se llaman DAO y contienen la lógica para crear, modificar y consultar
     * cada una de las tablas.
     *
     * Este método estaría dentro de la clase AlumnoDAO y lo usaríamos cada vez que quisiéramos añadir un alumno a la
     * base de datos.
     *
     * Recibe dos parámetros:
     * @param alumno El alumno a introducir en la base de datos
     * @param conexion La conexión a la base de datos
     * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/Statement.html">Interfaz Statement</a>
     * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/PreparedStatement.html">Clase PreparedStatement</a>
     */
    static void insertarAlumno (Connection conexion, Alumno alumno) {
        //Comando SQL que queremos ejecutar en la BD; los comodines representan los valores que queremos insertar.
        String query = "INSERT INTO alumnos (nombre, apellidos) VALUES (?, ?)";
        try {
            //Se pide a la conexión que nos proporcione un objeto statement
            PreparedStatement statement = conexion.prepareStatement(query);
            /*
            Los valores se insertan utilizando los métodos que corresponden con los tipos, así que
            hay que conocer el esquema de la tabla. La que usamos en este ejemplo está en la cabecera.
            */

            //Se inserta un String(se indica el comodín al que representa, se pasa el valor)
            statement.setString(1, alumno.getNombre());
            statement.setString(2, alumno.getApellidos());
            /*
            Si fuera un entero 'setInt', si fuera una fecha 'setDate', marca de tiempo 'setTimestamp'...
            La interfaz 'Statement' declara muchos tipos. A veces es necesario comprobar en la documentación
            para asegurarse que los tipos de Java son compatibles con los tipos de la base de datos.
            */

            /*
            Una vez preparada la sentencia SQL, se ejecuta.
            En este caso, al ser una inserción en la BD se usa el método 'executeUpdate()'
            No se inserta en campo 'id' porque la base de datos está configurada para añadirlo automáticamente
            en cada inserción.
             */
            statement.executeUpdate();

            //Se cierra la conexión y se cerrará también el statement automáticamente por estar vinculados
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /*
    Cuando se realizan búsquedas en una base de datos, el resultado obtenido se devuelve en un objeto 'ResultSet'.
    Un result set contiene los nombres de las columnas devueltos (nombre, apellidos, edad, email...) y un iterador
    que apunta a las filas que corresponde a los datos de búsqueda introducidos.

    Mediante el iterador y los nombres de las columnas iremos recorriendo el objeto 'ResultSet' e iremos creando, con
    la información obtenida, los objetos que corresponden a nuestro modelo.

    Si en la inserción convertíamos nuestros objetos Java a datos que SQL pudiera entender, ahora hacemos el
    proceso contrario: convertir los datos SQL (que son básicamente cadenas de texto) a objetos Java con los que
    podamos trabajar en nuestro código.

    Para ello hacemos un proceso similar en cuanto a la consulta se refiere:
    1.- Creamos nuestro string de consulta
    2.- Creamos el PreparedStatement
    3.- Ejecutamos la consulta

    Como es una consulta, va a devolver un resultado que lo recibimos en un 'ResultSet'. Una vez recibido lo iteramos
    y vamos consultando el dato correspondiente columna por columna y añadiéndolo a nuestros objetos Java

    Lo vemos en el código:
    */
    static List<Alumno> buscarAlumnoPorNombre (Connection conexion, String nombre){
        //Preparamos el comando SQL utilizando los comodines, como anteriormente.
        String query = "SELECT * FROM alumnos WHERE nombre = ?";

        //Declaramos nuestro objeto ResultSet que recibirá el resultado de la consulta
        ResultSet resultado = null;
        try {
            //Solicitamos un statement a la conexión
            PreparedStatement statement = conexion.prepareStatement(query);
            //Insertamos los datos que van en el lugar de los comodines
            statement.setString(1, nombre);

            //Asignamos al objeto 'ResultSet' el resultado de ejecutar la consulta (executeQuery)
            resultado = statement.executeQuery();

            //IMPORTANTE: Como vamos a seguir utilizando el objeto ResultSet NO SE PUEDE cerrar la conexión.
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /*
        Si todo ha salido bien, en este punto tendremos un objeto 'ResultSet'.
        Ahora lo iteraremos y crearemos nuestros objetos que correspondan con el modelo, en nuestro caso Alumno
        */

        //Preparamos nuestra colección en la que vamos a insertar los resultados ya convertidos en objetos
        List<Alumno> alumnos = new ArrayList<>();
        if (resultado == null) {
            //Si el código tiene varias rutas de salida, hay que asegurarse que en todas se cierra la conexión
            try {
                conexion.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return alumnos;
        }
        try {
            while (resultado.next()) {
                /*
                Haciendo uso del iterador del ResultSet, vamos a recorrer el listado de filas devueltos por SQL.
                Cada fila SQL representada por el iterador, contiene los datos que necesitamos
                para crear su correspondiente objeto Alumno, por lo que en cada iteración crearemos un Alumno que
                'rellenaremos' con los datos de la fila.
                */
                Alumno alumno = new Alumno();

                /*
                Al igual que convertimos nuestros campos 'String' de Java a 'String' de SQL para insertarlos en la BD,
                ahora hacemos lo contrario utilizando los métodos que nos proporciona el propio result set.

                Para convertir los datos a tipos Java utilizaremos el método que corresponda. En nuestro modelo Alumno
                el campo 'id' es del tipo Long por lo que utilizaremos el método 'getLong' para convertir el dato
                de la columna 'id' en un tipo Java; es IMPORTANTE que el nombre de la columna sea, literalmente, el que
                figure en la BD, ya que las columnas son case sensitive.
                El dato, ya convertido a un tipo Java, se lo asignaremos al objeto utilizando su correspondiente
                método, en este caso 'setId'
                */
                alumno.setId(resultado.getLong("id"));
                //Mismo procedimiento con el campo nombre
                alumno.setNombre(resultado.getString("nombre"));
                //Idem
                alumno.setApellidos(resultado.getString("apellidos"));
                alumnos.add(alumno);
            }

            //Ahora que ya hemos finalizado con el ResultSet, podemos cerrar la conexión
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alumnos;
    }

    static void borrarAlumnoPorNombre (Connection conexion, String nombre) {
        String query = "DELETE FROM alumnos WHERE nombre = ?";
        try {
            PreparedStatement statement = conexion.prepareStatement(query);
            statement.setString(1, nombre);
            statement.executeUpdate();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static List<Alumno> consultarTodosAlumnos(Connection conexion) {
        String query = "SELECT * FROM alumnos";
        List<Alumno> alumnos = new ArrayList<>();
        try {
            PreparedStatement statement = conexion.prepareStatement(query);
            ResultSet resultado = statement.executeQuery();
            while (resultado.next()) {
                Alumno alumno = new Alumno();
                alumno.setId(resultado.getLong("id"));
                alumno.setNombre(resultado.getString("nombre"));
                alumno.setApellidos(resultado.getString("apellidos"));
                alumnos.add(alumno);
                conexion.close();
            }
            return alumnos;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alumnos;
    }

    /* Se pueden seguir añadiendo tantos métodos como necesitemos para otro tipo de búsquedas,
    * modificaciones, eliminaciones de la BD...
    * El procedimiento siempre será el mismo.
    * Aclarar que hay maneras mejores de hacerlo, pero es un ejemplo básico aunque funcional.
    * La BD estará abierta durante el curso 23/24 */

    public static void main(String[] args) {
        //Algunas pruebas:
        //Ver los alumnos de la BD
        List<Alumno> alumnos = consultarTodosAlumnos(crearConexion());
        for (Alumno alumno : alumnos) {
            System.out.println(alumno.getId() + " " + alumno.getNombre() + " " + alumno.getApellidos());
        }

        //Insertar un alumno
        Alumno alumnoPruebas = new Alumno();
        alumnoPruebas.setNombre("Pedro");
        alumnoPruebas.setApellidos("Sánchez");
        insertarAlumno(crearConexion(), alumnoPruebas);

        //Buscar un alumno por nombre
        alumnos = buscarAlumnoPorNombre(crearConexion(), "Pedro");
        for (Alumno alumno : alumnos) {
            System.out.println(alumno.getId() + " " + alumno.getNombre() + " " + alumno.getApellidos());
        }

        //Eliminar un alumno
        borrarAlumnoPorNombre(crearConexion(), "Pedro");
        for (Alumno alumno : consultarTodosAlumnos(crearConexion())) {
            System.out.println(alumno.getId() + " " + alumno.getNombre() + " " + alumno.getApellidos());
        }


    }
}