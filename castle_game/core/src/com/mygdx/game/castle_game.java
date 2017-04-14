package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class castle_game extends ApplicationAdapter implements InputProcessor {
    //Objeto que recoge el mapa de baldosas
    private TiledMap mapa;
    //Objeto con el que se pinta el mapa de baldosas
    private OrthogonalTiledMapRenderer mapaRenderer;
    // Cámara que nos da la vista del juego
    private OrthographicCamera camara;
    // Atributo en el que se cargará la hoja de sprites del mosquetero.
    private Texture img;
    //Atributo que permite dibujar imágenes 2D, en este caso el sprite.
    private SpriteBatch batch;

    //Constantes que indican el número de filas y columnas de la hoja de sprites.
    private static final int FRAME_COLS = 4;
    private static final int FRAME_ROWS = 4;

    //Animación que se muestra en el método render()
    private Animation jugador;
    //Animaciones para cada una de las direcciones de movimiento del personaje del jugador.
    private Animation jugadorArriba;
    private Animation jugadorDerecha;
    private Animation jugadorAbajo;
    private Animation jugadorIzquierda;
    // Tamaño del mapa de baldosas.
    private int mapaAncho, mapaAlto;
    //Atributos que indican la anchura y la altura de un tile del mapa de baldosas
    int anchoCelda, altoCelda;
    //Posición actual del jugador.
    private float jugadorX, jugadorY;
    // Este atributo indica el tiempo en segundos transcurridos desde que se inicia la animación
    // , servirá para determinar cual es el frame que se debe representar.
    private float stateTime;

    //Contendrá el frame que se va a mostrar en cada momento.
    private TextureRegion cuadroActual;

    private boolean[][] obstaculo;
    private TiledMapTileLayer capaObstaculos;

    //Atributos que indican la anchura y altura del sprite animado del jugador.
    int anchoJugador, altoJugador;

    //Animaciones posicionales relacionadas con los NPC del juego
    private Animation noJugadorArriba;
    private Animation noJugadorDerecha;
    private Animation noJugadorAbajo;
    private Animation noJugadorIzquierda;

    //Array con los objetos Animation de los NPC
    private Animation[] noJugador;
    //Atributos que indican la anchura y altura del sprite animado de los NPC.
    int anchoNoJugador, altoNoJugador;
    //Posición inicial X de cada uno de los NPC
    private float[] noJugadorX;
    //Posición inicial Y de cada uno de los NPC
    private float[] noJugadorY;
    //Posición final X de cada uno de los NPC
    private float[] destinoX;
    //Posición final Y de cada uno de los NPC
    private float[] destinoY;
    //Número de NPC que van a aparecer en el juego
    private static final int numeroNPCs = 3;
    // Este atributo indica el tiempo en segundos transcurridos desde que se inicia la animación
    //de los NPC , servirá para determinar cual es el frame que se debe representar.
    private float stateTimeNPC = 0;

    // Música de fondo del juego
    private Music musica;

    // Sonidos
    private Sound sonidoPasos;
    private Sound sonidoColisionEnemigo;
    private Sound sonidoObstaculo;

    @Override
    public void create() {
        //Creamos una cámara y la vinculamos con el lienzo del juego.
        //En este caso le damos unos valores de tamaño que haga que el juego
        //se muestre de forma idéntica en todas las plataformas.
        camara = new OrthographicCamera(640, 480);
        //Posicionamos la vista de la cámara para que su vértice inferior izquierdo sea (0,0)
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        //Vinculamos los eventos de entrada a esta clase.
        Gdx.input.setInputProcessor(this);
        camara.update();

        // Cargamos la imagen de los frames del mosquetero en el objeto img de la clase Texture.
        img = new Texture(Gdx.files.internal("jugadores/jugador.png"));

        //Sacamos los frames de img en un array de TextureRegion.
        TextureRegion[][] tmp = TextureRegion.split(img, img.getWidth() / FRAME_COLS, img.getHeight() / FRAME_ROWS);

        // Creamos las distintas animaciones, teniendo en cuenta que el tiempo de muestra de cada frame
        // será de 150 milisegundos.
        jugadorArriba = new Animation(0.150f, tmp[0]);
        jugadorDerecha = new Animation(0.150f, tmp[1]);
        jugadorAbajo = new Animation(0.150f, tmp[2]);
        jugadorIzquierda = new Animation(0.150f, tmp[3]);
        //En principio se utiliza la animación del jugador arriba como animación por defecto.
        jugador = jugadorArriba;
        // Posición inicial del jugador.
        jugadorX = 220;
        jugadorY = 410;
        //Ponemos a cero el atributo stateTime, que marca el tiempo e ejecución de la animación.
        stateTime = 0f;

        //Creamos el objeto SpriteBatch que nos permitirá representar adecuadamente el sprite
        //en el método render()
        batch = new SpriteBatch();

        //Cargamos el mapa de baldosas desde la carpeta de assets
        mapa = new TmxMapLoader().load("mapa/mapa.tmx");
        mapaRenderer = new OrthogonalTiledMapRenderer(mapa);

        //Determinamos el alto y ancho del mapa de baldosas. Para ello necesitamos extraer la capa
        //base del mapa y, a partir de ella, determinamos el número de celdas a lo ancho y alto,
        //así como el tamaño de la celda, que multiplicando por el número de celdas a lo alto y
        //ancho, da como resultado el alto y ancho en pixeles del mapa.
        TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(0);
        anchoCelda = (int) capa.getTileWidth();
        altoCelda = (int) capa.getTileHeight();
        mapaAncho = capa.getWidth() * anchoCelda;
        mapaAlto = capa.getHeight() * altoCelda;

        //Cargamos la capa de los obstáculos, que es la tercera en el TiledMap.
        capaObstaculos = (TiledMapTileLayer) mapa.getLayers().get(2);

        //Cargamos la matriz de los obstáculos del mapa de baldosas.
        int anchoCapa = capaObstaculos.getWidth(), altoCapa = capaObstaculos.getHeight();
        obstaculo = new boolean[anchoCapa][altoCapa];
        for (int x = 0; x < anchoCapa; x++) {
            for (int y = 0; y < altoCapa; y++) {
                obstaculo[x][y] = (capaObstaculos.getCell(x, y) != null);
            }
        }

        //Cargamos en los atributos del ancho y alto del sprite sus valores
        cuadroActual = (TextureRegion) jugador.getKeyFrame(stateTime);
        anchoJugador = cuadroActual.getRegionHeight();
        altoJugador = cuadroActual.getRegionHeight();

        //Inicializamos el apartado referente a los NPC
        noJugador = new Animation[numeroNPCs];
        noJugadorX = new float[numeroNPCs];
        noJugadorY = new float[numeroNPCs];
        destinoX = new float[numeroNPCs];
        destinoY = new float[numeroNPCs];

        //Creamos las animaciones posicionales de los NPC
        //Cargamos la imagen de los frames del monstruo en el objeto img de la clase Texture.
        img = new Texture(Gdx.files.internal("jugadores/magorojo.png"));

        //Sacamos los frames de img en un array de TextureRegion.
        tmp = TextureRegion.split(img, img.getWidth() / FRAME_COLS, img.getHeight() / FRAME_ROWS);

        // Creamos las distintas animaciones, teniendo en cuenta que el tiempo de muestra de cada frame
        // será de 150 milisegundos.
        noJugadorArriba = new Animation(0.150f, tmp[0]);
        noJugadorArriba.setPlayMode(Animation.PlayMode.LOOP);
        noJugadorDerecha = new Animation(0.150f, tmp[1]);
        noJugadorDerecha.setPlayMode(Animation.PlayMode.LOOP);
        noJugadorAbajo = new Animation(0.150f, tmp[2]);
        noJugadorAbajo.setPlayMode(Animation.PlayMode.LOOP);
        noJugadorIzquierda = new Animation(0.150f, tmp[3]);
        noJugadorIzquierda.setPlayMode(Animation.PlayMode.LOOP);

        //Cargamos en los atributos del ancho y alto del sprite del monstruo sus valores
        cuadroActual = (TextureRegion) noJugadorAbajo.getKeyFrame(stateTimeNPC);
        anchoNoJugador = cuadroActual.getRegionWidth();
        altoNoJugador = cuadroActual.getRegionHeight();

        //Se inicializan, la animación por defecto y, de forma aleatoria, las posiciones
        //iniciales y finales de los NPC. Para simplificar un poco, los NPC pares, se moveran
        //de forma vertical y los impares de forma horizontal.
        for (int i = 0; i < numeroNPCs; i++) {
            noJugadorX[i] = (float) (Math.random() * mapaAncho);
            noJugadorY[i] = (float) (Math.random() * mapaAlto);

            if (i % 2 == 0) {
                // NPC par => mover de forma vertical
                destinoX[i] = noJugadorX[i];
                destinoY[i] = (float) (Math.random() * mapaAlto);
                //Determinamos cual de las animaciones verticales se utiliza.
                if (noJugadorY[i] < destinoY[i]) {
                    noJugador[i] = noJugadorArriba;
                } else {
                    noJugador[i] = noJugadorAbajo;
                }
            } else {
                // NPC impar => mover de forma horizontal
                destinoX[i] = (float) (Math.random() * mapaAncho);
                destinoY[i] = noJugadorY[i];
                //Determinamos cual de las animaciones horizontales se utiliza.
                if (noJugadorX[i] < destinoX[i]) {
                    noJugador[i] = noJugadorDerecha;
                } else {
                    noJugador[i] = noJugadorIzquierda;
                }
            }

        }

        // Ponemos a cero el atributo stateTimeNPC, que marca el tiempo e ejecución de la animación
        // de los NPC.
        stateTimeNPC = 0f;

        //Inicializamos la música de fondo del juego y la reproducimos.
        musica = Gdx.audio.newMusic(Gdx.files.internal("sonidos/main.mp3"));
        musica.play();

        //Inicializamos los atributos de los efectos de sonido.
        sonidoColisionEnemigo = Gdx.audio.newSound(Gdx.files.internal("sonidos/qubodup-PowerDrain.ogg"));
        sonidoPasos = Gdx.audio.newSound(Gdx.files.internal("sonidos/Fantozzi-SandR3.ogg"));
        sonidoObstaculo = Gdx.audio.newSound(Gdx.files.internal("sonidos/wall.ogg"));

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Trasladamos la cámara para que se centre en el mosquetero.
        camara.position.set(jugadorX, jugadorY, 0f);
        //Comprobamos que la cámara no se salga de los límites del mapa de baldosas,
        //Verificamos, con el método clamp(), que el valor de la posición x de la cámara
        //esté entre la mitad de la anchura de la vista de la cámara y entre la diferencia entre
        //la anchura del mapa restando la mitad de la anchura de la vista de la cámara,
        camara.position.x = MathUtils.clamp(camara.position.x, camara.viewportWidth / 2f,
                mapaAncho - camara.viewportWidth / 2f);
        //Verificamos, con el método clamp(), que el valor de la posición y de la cámara
        //esté entre la mitad de la altura de la vista de la cámara y entre la diferencia entre
        //la altura del mapa restando la mitad de la altura de la vista de la cámara,
        camara.position.y = MathUtils.clamp(camara.position.y, camara.viewportHeight / 2f,
                mapaAlto - camara.viewportHeight / 2f);

        //Actualizamos la cámara del juego
        camara.update();
        //Vinculamos el objeto de dibuja el TiledMap con la cámara del juego
        mapaRenderer.setView(camara);

        //Dibujamos las tres primeras capas del TiledMap (no incluye a la de altura)
        int[] capas = {0, 1, 2};
        mapaRenderer.render(capas);


        // extraemos el tiempo de la última actualización del sprite y la acumulamos a stateTime.
        stateTime += Gdx.graphics.getDeltaTime();
        //Extraermos el frame que debe ir asociado a al momento actual.
        cuadroActual = (TextureRegion) jugador.getKeyFrame(stateTime);

        // le indicamos al SpriteBatch que se muestre en el sistema de coordenadas
        // específicas de la cámara.
        batch.setProjectionMatrix(camara.combined);

        batch.begin();
        batch.draw(cuadroActual, jugadorX, jugadorY);

        //Dibujamos las animaciones de los NPC
        for (int i = 0; i < numeroNPCs; i++) {
            actualizaNPC(i, 0.5f);
            cuadroActual = (TextureRegion) noJugador[i].getKeyFrame(stateTimeNPC);
            batch.draw(cuadroActual, noJugadorX[i], noJugadorY[i]);
        }

        batch.end();

        //Pintamos la quinta capa del mapa de baldosas.
        capas = new int[1];
        capas[0] = 4;
        mapaRenderer.render(capas);
        //Comprobamos si hay o no colisiones entre el jugador y los obstáculos
        detectaColisiones();
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
        mapa.dispose();
        mapaRenderer.dispose();
        musica.dispose();
        sonidoObstaculo.dispose();
        sonidoPasos.dispose();
        sonidoColisionEnemigo.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    //Método que permite cambiar las coordenadas del NPC en la posición "i",
    //dada una variación "delta" en ambas coordenadas.
    private void actualizaNPC(int i, float delta) {
        if (destinoY[i] > noJugadorY[i]) {
            noJugadorY[i] += delta;
            noJugador[i] = noJugadorArriba;
        }
        if (destinoY[i] < noJugadorY[i]) {
            noJugadorY[i] -= delta;
            noJugador[i] = noJugadorAbajo;
        }
        if (destinoX[i] > noJugadorX[i]) {
            noJugadorX[i] += delta;
            noJugador[i] = noJugadorDerecha;
        }
        if (destinoX[i] < noJugadorX[i]) {
            noJugadorX[i] -= delta;
            noJugador[i] = noJugadorIzquierda;
        }
    }

    private void detectaColisiones() {
        //Vamos a comprobar que el rectángulo que rodea al jugador, no se solape
        //con el rectángulo de alguno de los NPC. Primero calculamos el rectángulo
        //en torno al jugador.
        Rectangle rJugador = new Rectangle(jugadorX, jugadorY, anchoJugador, altoJugador);
        Rectangle rNPC;
        //Ahora recorremos el array de NPC, para cada uno generamos su rectángulo envolvente
        //y comprobamos si se solapa o no con el del Jugador.
        for (int i = 0; i < numeroNPCs; i++) {
            rNPC = new Rectangle(noJugadorX[i], noJugadorY[i], anchoNoJugador, altoNoJugador);
            //Se comprueba si se solapan.
            if (rJugador.overlaps(rNPC)) {
                //hacer lo que haya que hacer en este caso, como puede ser reproducir un efecto
                //de sonido, una animación del jugador alternativa y, posiblemente, que este muera
                //y se acabe la partida actual. En principio, en este caso, lo único que se hace
                //es mostrar un mensaje en la consola de texto.
                System.out.println("Hay colisión!!!");
                sonidoColisionEnemigo.play(0.25f);
            }
        }
    }
}
