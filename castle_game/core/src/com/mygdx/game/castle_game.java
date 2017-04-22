package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class castle_game extends ApplicationAdapter implements InputProcessor {
    private static final int FRAME_COLS = 4;
    private static final int FRAME_ROWS = 4;
    // Numero de NPC que aparecen en el juego
    private static final int numeroNPCs = 5;
    // Objeto que recoge el mapa de baldosas
    private TiledMap mapa;
    // Objeto con el que se pinta el mapa de baldosas
    private OrthogonalTiledMapRenderer mapaRenderer;
    // Camara que nos da la vista del juego
    private OrthographicCamera camara;
    // Atributo en el que se cargara la hoja de sprites del personaje.
    private Texture img;
    // Atributo que permite dibujar imagenes 2D, en este caso el sprite.
    private SpriteBatch sb;
    // Animacion que se muestra en el metodo render()
    private Animation jugador;
    // Animaciones para cada una de las direcciones de movimiento del personaje del jugador.
    private Animation jugadorArriba;
    private Animation jugadorDerecha;
    private Animation jugadorAbajo;
    private Animation jugadorIzquierda;
    // Tamano del mapa de baldosas.
    private int mapaAncho, mapaAlto;
    // Atributos que indican la anchura y la altura de un tile del mapa de baldosas
    private int anchoCelda, altoCelda;
    // Posicion actual del jugador.
    private float jugadorX, jugadorY;
    // Este atributo indica el tiempo en segundos transcurridos desde que se inicia la animacion
    private float stateTimePC;
    // Frame que se va a mostrar en cada momento.
    private TextureRegion cuadroActual;
    private boolean[][] obstaculo, agujero, barco;
    private TiledMapTileLayer capaObstaculos, capaAgujeros, capaBarco;
    // Atributos que indican la anchura y altura del sprite animado del jugador.
    private int anchoJugador, altoJugador;
    // Animaciones posicionales relacionadas con los NPC del juego
    private Animation noJugadorArriba;
    private Animation noJugadorDerecha;
    private Animation noJugadorAbajo;
    private Animation noJugadorIzquierda;
    // Array con los objetos Animation de los NPC
    private Animation[] noJugador;
    // Atributos que indican la anchura y altura del sprite animado de los NPC.
    private int anchoNoJugador, altoNoJugador;
    // Posiciones iniciales de cada uno de los NPC
    private float[] noJugadorX;
    private float[] noJugadorY;
    // Posiciones finales de cada uno de los NPC
    private float[] destinoX;
    private float[] destinoY;
    // Atributo que indica el tiempo en segundos transcurridos desde que se inicia la animacion
    private float stateTimeNPC = 0;

    // Animacion de los tesoros que se muestra en el metodo render()
    private Animation tesoro;

    // Posicion actual de los tesoros.
    private float tesoro1X, tesoro1Y, tesoro2X, tesoro2Y, tesoro3X, tesoro3Y, tesoro4X, tesoro4Y;

    // Atributos que indican la anchura y altura del sprite de los tesoros
    private int anchoTesoro, altoTesoro;

    // Atributo que indica el tiempo en segundos transcurridos desde que se inicia la animacion
    private float stateTimeTesoro = 0;

    private int conseguidos = 0;
    private int restantes = 4;

    // Musica de fondo del juego
    private Music musica;

    // Sonidos
    private Sound sonidoPasos;
    private Sound sonidoColisionEnemigo;
    private Sound sonidoObstaculo;
    private Sound sonidoCaida;
    private Sound sonidoBarco;
    private Sound sonidoVictoria;

    // Booleanos para fin de juego
    private boolean caida, cazado, hundido, victoria;

    // Objeto font para mensajes en pantalla
    private BitmapFont font, tesoros;

    /**
     * Metodo create. Carga y crea objetos y atributos del juego
     *
     * @param
     * @return
     */
    @Override
    public void create() {
        // Crea una camara y la vincula con el lienzo del juego
        camara = new OrthographicCamera(640, 480);
        // Posiciona la vista de la camara para que su vertice inferior izquierdo sea (0,0)
        camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);
        // Vincula los eventos de entrada a esta clase.
        Gdx.input.setInputProcessor(this);
        camara.update();

        // Carga la imagen de los frames del jugagor en el objeto img de la clase Texture.
        img = new Texture(Gdx.files.internal("jugadores/jugador.png"));

        // Saca los frames de img en un array de TextureRegion.
        TextureRegion[][] tmp = TextureRegion.split(img, img.getWidth() / FRAME_COLS, img.getHeight() / FRAME_ROWS);

        jugadorArriba = new Animation(0.150f, tmp[3]);
        jugadorDerecha = new Animation(0.150f, tmp[2]);
        jugadorAbajo = new Animation(0.150f, tmp[0]);
        jugadorIzquierda = new Animation(0.150f, tmp[1]);

        // Posicion inicial
        jugador = jugadorAbajo;
        jugadorX = 220;
        jugadorY = 410;
        //Pone a cero el atributo stateTime, que marca el tiempo de ejecucion de la animacion.
        stateTimePC = 0f;

        // Crea el objeto SpriteBatch
        sb = new SpriteBatch();

        // Carga el mapa de baldosas desde la carpeta de assets
        mapa = new TmxMapLoader().load("mapa/mapa.tmx");
        mapaRenderer = new OrthogonalTiledMapRenderer(mapa);

        // Determina el alto y ancho del mapa de baldosas
        TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(0);
        anchoCelda = (int) capa.getTileWidth();
        altoCelda = (int) capa.getTileHeight();
        mapaAncho = capa.getWidth() * anchoCelda;
        mapaAlto = capa.getHeight() * altoCelda;

        // Carga la capa de los obstaculos, tercera en el TiledMap.
        capaObstaculos = (TiledMapTileLayer) mapa.getLayers().get(2);

        // Carga la matriz de los obstaculos del mapa de baldosas.
        int anchoCapa = capaObstaculos.getWidth(), altoCapa = capaObstaculos.getHeight();
        obstaculo = new boolean[anchoCapa][altoCapa];
        for (int x = 0; x < anchoCapa; x++) {
            for (int y = 0; y < altoCapa; y++) {
                obstaculo[x][y] = (capaObstaculos.getCell(x, y) != null);
            }
        }

        // Carga la capa de los agujeros, cuarta en el TiledMap.
        capaAgujeros = (TiledMapTileLayer) mapa.getLayers().get(3);

        // Carga la matriz de los obstaculos del mapa de baldosas.
        anchoCapa = capaAgujeros.getWidth();
        altoCapa = capaAgujeros.getHeight();
        agujero = new boolean[anchoCapa][altoCapa];
        for (int x = 0; x < anchoCapa; x++) {
            for (int y = 0; y < altoCapa; y++) {
                agujero[x][y] = (capaAgujeros.getCell(x, y) != null);
            }
        }

        // Carga la capa del barco, quinta en el TiledMap.
        capaBarco = (TiledMapTileLayer) mapa.getLayers().get(4);

        // Carga la matriz de los obstaculos del mapa de baldosas.
        anchoCapa = capaBarco.getWidth();
        altoCapa = capaBarco.getHeight();
        barco = new boolean[anchoCapa][altoCapa];
        for (int x = 0; x < anchoCapa; x++) {
            for (int y = 0; y < altoCapa; y++) {
                barco[x][y] = (capaBarco.getCell(x, y) != null);
            }
        }

        // Carga en los atributos del ancho y alto del sprite sus valores
        cuadroActual = (TextureRegion) jugador.getKeyFrame(stateTimePC);
        anchoJugador = cuadroActual.getRegionHeight() / 2;//ajustamos las colisiones H
        altoJugador = cuadroActual.getRegionHeight();

        // Inicializa el apartado referente a los NPC
        noJugador = new Animation[numeroNPCs];
        noJugadorX = new float[numeroNPCs];
        noJugadorY = new float[numeroNPCs];
        destinoX = new float[numeroNPCs];
        destinoY = new float[numeroNPCs];

        // Crea las animaciones posicionales de los NPC
        // Carga la imagen de los frames del enemigo en el objeto img de la clase Texture.
        img = new Texture(Gdx.files.internal("jugadores/enemigo.png"));

        // Sacamos los frames de img en un array de TextureRegion.
        tmp = TextureRegion.split(img, img.getWidth() / FRAME_COLS, img.getHeight() / FRAME_ROWS);

        // Crea las distintas animaciones
        noJugadorArriba = new Animation(0.150f, tmp[3]);//0
        noJugadorArriba.setPlayMode(Animation.PlayMode.LOOP);
        noJugadorDerecha = new Animation(0.150f, tmp[2]);//1
        noJugadorDerecha.setPlayMode(Animation.PlayMode.LOOP);
        noJugadorAbajo = new Animation(0.150f, tmp[0]);//2
        noJugadorAbajo.setPlayMode(Animation.PlayMode.LOOP);
        noJugadorIzquierda = new Animation(0.150f, tmp[1]);//3
        noJugadorIzquierda.setPlayMode(Animation.PlayMode.LOOP);

        // Carga en los atributos del ancho y alto del sprite del enemigo sus valores
        cuadroActual = (TextureRegion) noJugadorAbajo.getKeyFrame(stateTimeNPC);
        anchoNoJugador = cuadroActual.getRegionWidth() / 2;
        altoNoJugador = cuadroActual.getRegionHeight();

        // Se inicializan, la animacion por defecto y, de forma aleatoria, las posiciones iniciales y finales de los NPC
        for (int i = 0; i < numeroNPCs; i++) {
            noJugadorX[i] = (float) (Math.random() * mapaAncho);
            noJugadorY[i] = (float) (Math.random() * mapaAlto);

            if (i % 2 == 0) {
                // NPC par => mover de forma vertical
                destinoX[i] = noJugadorX[i];
                destinoY[i] = (float) (Math.random() * mapaAlto);
                if (noJugadorY[i] < destinoY[i]) {
                    noJugador[i] = noJugadorArriba;
                } else {
                    noJugador[i] = noJugadorAbajo;
                }
            } else {
                // NPC impar => mover de forma horizontal
                destinoX[i] = (float) (Math.random() * mapaAncho);
                destinoY[i] = noJugadorY[i];
                if (noJugadorX[i] < destinoX[i]) {
                    noJugador[i] = noJugadorDerecha;
                } else {
                    noJugador[i] = noJugadorIzquierda;
                }
            }
        }

        stateTimeNPC = 0f;

        // Carga la imagen de los frames del jugagor en el objeto img de la clase Texture.
        img = new Texture(Gdx.files.internal("mapa/tesoro.png"));

        // Sacamos los frames de img en un array de TextureRegion.
        tmp = TextureRegion.split(img, img.getWidth(), img.getHeight());

        tesoro1X = 30;
        tesoro1Y = 415;

        tesoro2X = 30;
        tesoro2Y = 190;

        tesoro3X = 540;
        tesoro3Y = 415;

        tesoro4X = 605;
        tesoro4Y = 0;
        //Pone a cero el atributo stateTime, que marca el tiempo de ejecucion de la animacion.
        stateTimeTesoro = 0f;

        tesoro = new Animation(0f, tmp[0]);

        // Carga en los atributos del ancho y alto del sprite sus valores
        cuadroActual = (TextureRegion) tesoro.getKeyFrame(stateTimeTesoro);
        anchoTesoro = (cuadroActual.getRegionWidth() / 2)/2;//ajustamos las colisiones H
        altoTesoro = (cuadroActual.getRegionHeight())/2;

        tesoros = new BitmapFont();

        // Inicializa la musica de fondo del juego.
        musica = Gdx.audio.newMusic(Gdx.files.internal("sonidos/main.mp3"));
        musica.play();

        // Inicializa los atributos de los efectos de sonido.
        sonidoColisionEnemigo = Gdx.audio.newSound(Gdx.files.internal("sonidos/qubodup-PowerDrain.ogg"));
        sonidoPasos = Gdx.audio.newSound(Gdx.files.internal("sonidos/Fantozzi-SandR3.ogg"));
        sonidoObstaculo = Gdx.audio.newSound(Gdx.files.internal("sonidos/wall.ogg"));
        sonidoCaida = Gdx.audio.newSound(Gdx.files.internal("sonidos/fall.ogg"));
        sonidoBarco = Gdx.audio.newSound(Gdx.files.internal("sonidos/boat.ogg"));
        sonidoVictoria = Gdx.audio.newSound(Gdx.files.internal("sonidos/win.ogg"));
    }

    /**
     * Metodo render. Muestra en pantalla el juego, animaciones y mensajes
     *
     * @param
     * @return
     */
    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camara.position.set(jugadorX, jugadorY, 0f);
        // Comprueba que la camara no se salga de los limites del mapa de baldosas.
        camara.position.x = MathUtils.clamp(camara.position.x, camara.viewportWidth / 2f,
                mapaAncho - camara.viewportWidth / 2f);
        camara.position.y = MathUtils.clamp(camara.position.y, camara.viewportHeight / 2f,
                mapaAlto - camara.viewportHeight / 2f);

        camara.update();
        mapaRenderer.setView(camara);

        // Dibuja las cinco primeras capas del TiledMap (no incluye a la de altura).
        int[] capas = {0, 1, 2, 3, 4};
        mapaRenderer.render(capas);

        // Extrae el tiempo de la ultima actualizacion del sprite y la acumula a stateTime.
        stateTimePC += Gdx.graphics.getDeltaTime();
        // Extrae el frame que debe ir asociado a al momento actual.
        cuadroActual = (TextureRegion) jugador.getKeyFrame(stateTimePC);
        sb.setProjectionMatrix(camara.combined);

        /*if (cogeTesoro()) {
            CharSequence str = "¡Has conseguido un tesoro! te quedan " + restantes;
            sb.begin();
            tesoros.getData().setScale(1f);
            tesoros.setColor(Color.WHITE);
            tesoros.draw(sb, str, 50, 30);
            sb.end();
        }*/

        sb.begin();

        if (!cazado && !caida && !hundido && !victoria) {
            // Pinta el objeto Sprite a traves del objeto SpriteBatch
            sb.draw(cuadroActual, jugadorX, jugadorY);
            if (Gdx.input.isKeyPressed(Input.Keys.UP))
                actualizaPC(Input.Keys.UP);
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                actualizaPC(Input.Keys.DOWN);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                actualizaPC(Input.Keys.RIGHT);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                actualizaPC(Input.Keys.LEFT);

            // Dibuja las animaciones de los NPC
            for (int i = 0; i < numeroNPCs; i++) {
                actualizaNPC(i, 0.5f);
                cuadroActual = (TextureRegion) noJugador[i].getKeyFrame(stateTimeNPC);
                sb.draw(cuadroActual, noJugadorX[i], noJugadorY[i]);
            }

            cuadroActual = (TextureRegion) tesoro.getKeyFrame(stateTimeTesoro);
            sb.draw(cuadroActual, tesoro1X, tesoro1Y);
            sb.draw(cuadroActual, tesoro2X, tesoro2Y);
            sb.draw(cuadroActual, tesoro3X, tesoro3Y);
            sb.draw(cuadroActual, tesoro4X, tesoro4Y);

            sb.end();

            // Pinta la sexta capa del mapa de baldosas.
            capas = new int[1];
            capas[0] = 5;
            mapaRenderer.render(capas);
        } else {
            // Pinta la pantalla en negro y desactiva los sonidos
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            sonidoPasos.stop();
            sb = new SpriteBatch();
            font = new BitmapFont();
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            font.getData().setScale(2f);
            sb.begin();
            CharSequence str = "GAME OVER";
            font.setColor(Color.RED);
            font.draw(sb, str, (camara.viewportWidth / 2f) - 75f, (camara.viewportHeight / 2f) + 35f);
            sb.end();
            if (caida) {
                musica.stop();
                sonidoColisionEnemigo.stop();
                CharSequence str2 = "¡Has caido a un agujero!";
                sb.begin();
                font.getData().setScale(1.5f);
                font.setColor(Color.RED);
                font.draw(sb, str2, (camara.viewportWidth / 2f) - 100f, (camara.viewportHeight / 2f) + 7f);
                sb.end();
            }
            if (cazado) {
                musica.stop();
                sonidoColisionEnemigo.play(0.25f);
                CharSequence str3 = "¡Has sido atrapado por un enemigo!";
                sb.begin();
                font.getData().setScale(1.5f);
                font.setColor(Color.RED);
                font.draw(sb, str3, (camara.viewportWidth / 2f) - 150f, (camara.viewportHeight / 2f) + 7f);
                sb.end();
            }
            if (hundido) {
                musica.stop();
                sonidoColisionEnemigo.stop();
                CharSequence str4 = "¡Te has hundido en el barco!";
                sb.begin();
                font.getData().setScale(1.5f);
                font.setColor(Color.RED);
                font.draw(sb, str4, (camara.viewportWidth / 2f) - 125f, (camara.viewportHeight / 2f) + 7f);
                sb.end();
            }
            if (victoria) {
                musica.stop();
                sonidoColisionEnemigo.stop();
                sonidoVictoria.play(0.5f);
                CharSequence str5 = "¡HAS GANADO!";
                sb.begin();
                font.getData().setScale(2.5f);
                font.setColor(Color.GREEN);
                font.draw(sb, str5, (camara.viewportWidth / 2f) - 120f, (camara.viewportHeight / 2f) + 5f);
                sb.end();
            }
            // Espera 3 segundos y cierra el juego
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Gdx.app.exit();
                            System.exit(0);
                        }
                    },
                    3000
            );
        }
    }

    /**
     * Metodo KeyUp. Respuesta al soltar las teclas de movimiento del personaje
     *
     * @param keycode
     * @return true
     */
    @Override
    public boolean keyUp(int keycode) {
        jugadorArriba.setPlayMode(Animation.PlayMode.NORMAL);
        jugadorAbajo.setPlayMode(Animation.PlayMode.NORMAL);
        jugadorIzquierda.setPlayMode(Animation.PlayMode.NORMAL);
        jugadorDerecha.setPlayMode(Animation.PlayMode.NORMAL);
        return true;
    }

    /**
     * Metodo KeyDown. Respuesta al pulsar las teclas de movimiento del personaje
     *
     * @param keycode
     * @return true
     */
    @Override
    public boolean keyDown(int keycode) {
        // Si se pulsa uno de los cursores, se desplaza el sprite de forma adecuada un pixel
        stateTimePC = 0;

        actualizaPC(keycode);

        // Si se pulsa la tecla del numero 1, se alterna la visibilidad de la primera capa del mapa de baldosas.
        if (keycode == Input.Keys.NUM_1)
            mapa.getLayers().get(0).setVisible(!mapa.getLayers().get(0).isVisible());
        // Si se pulsa la tecla del numero 2, se alterna la visibilidad de la segunda capa del mapa de baldosas.
        if (keycode == Input.Keys.NUM_2)
            mapa.getLayers().get(1).setVisible(!mapa.getLayers().get(1).isVisible());
        return true;
    }

    /**
     * Metodo KeyTyped. Recoge la tecla pulsada
     *
     * @param character
     * @return false
     */
    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     * Metodo KeyUp. Respuesta al pulsar pantalla tactil
     *
     * @param screenX
     * @param screenY
     * @param pointer
     * @param button
     * @return true
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Vector en tres dimensiones que recoge las coordenadas donde se ha hecho click o toque de la pantalla.
        Vector3 clickCoordinates = new Vector3(screenX, screenY, 0);
        // Transforma las coordenadas del vector a coordenadas de nuestra camara.
        Vector3 posicion = camara.unproject(clickCoordinates);

        stateTimePC = 0;

        // Guarda la posicion anterior del jugador por si topa con un obstaculo, vuelve a la posicion anterior.
        float jugadorAnteriorX = jugadorX;
        float jugadorAnteriorY = jugadorY;

        // Si pulsa por encima de la animacion, se sube 5 pixeles y se reproduce.
        if ((jugadorY + 48) < posicion.y) {
            jugadorY += 5;
            jugador = jugadorArriba;
            // Si pulsa por debajo de la animacion, se sube 5 pixeles y se reproduce.
        } else if ((jugadorY) > posicion.y) {
            jugadorY -= 5;
            jugador = jugadorAbajo;
        }
        // Si pulsa mas de 24 pixels a dcha de la animacion, se sube 5 pixeles y se reproduce.
        if ((jugadorX + 24) < posicion.x) {
            jugadorX += 5;
            jugador = jugadorDerecha;
            // Si pulsa mas de 24 pixels a izqda de la animacion, se sube 5 pixeles y se reproduce.
        } else if ((jugadorX - 24) > posicion.x) {
            jugadorX -= 5;
            jugador = jugadorIzquierda;
        }

        // Detecta las colisiones con los obstaculos del mapa y si el jugador se sale del mismo.
        if ((jugadorX < 0 || jugadorY < 0 ||
                jugadorX > (mapaAncho - anchoJugador) ||
                jugadorY > (mapaAlto - altoJugador)) ||
                ((obstaculo[(int) ((jugadorX + anchoJugador / 4) / anchoCelda)][((int) (jugadorY) / altoCelda)]) ||
                        (obstaculo[(int) ((jugadorX + 3 * anchoJugador / 4) / anchoCelda)][((int) (jugadorY) / altoCelda)]))) {
            jugadorX = jugadorAnteriorX;
            jugadorY = jugadorAnteriorY;
            sonidoObstaculo.play(0.5f);

        } else {
            sonidoPasos.play(0.25f);
        }

        colisionObstaculo(jugadorAnteriorX, jugadorAnteriorY);
        colisionNPC();
        caidaAgujero();
        caidaBarco();

        return true;
    }

    /**
     * Metodo KeyUp. Respuesta al dejar de pulsar pantalla tactil
     *
     * @param screenX
     * @param screenY
     * @param pointer
     * @param button
     * @return false
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Metodo touchDragged. Respuesta al arrastrar pantalla tactil
     *
     * @param screenX
     * @param screenY
     * @param pointer
     * @return false
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Metodo mouseMoved. Respuesta al mover raton
     *
     * @param screenX
     * @param screenY
     * @return false
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    /**
     * Metodo mouseMoved. Respuesta al hacer scroll en pantalla
     *
     * @param amount
     * @return false
     */
    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    /**
     * Metodo dispose. Libera recursos del sistema
     */
    @Override
    public void dispose() {
        sb.dispose();
        img.dispose();
        mapa.dispose();
        mapaRenderer.dispose();
        musica.dispose();
        sonidoObstaculo.dispose();
        sonidoPasos.dispose();
        sonidoColisionEnemigo.dispose();
    }

    /**
     * Metodo actualizaNPC. Metodo que permite cambiar las coordenadas del NPC en la posicion "i",
     * dada una variacion "delta" en ambas coordenadas.
     *
     * @param i
     * @param delta
     */
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

    /**
     * Metodo actualizaPC. Metodo que actualiza posicion del jugador
     *
     * @param keycode
     */
    private void actualizaPC(int keycode) {
        float jugadorAnteriorX = jugadorX;
        float jugadorAnteriorY = jugadorY;


        int speed = 2;
        if (keycode == Input.Keys.LEFT) {
            jugadorX += -speed;
            jugador = jugadorIzquierda;
            jugadorIzquierda.setPlayMode(Animation.PlayMode.LOOP);
        }
        if (keycode == Input.Keys.RIGHT) {
            jugadorX += speed;
            jugador = jugadorDerecha;
            jugadorDerecha.setPlayMode(Animation.PlayMode.LOOP);
        }
        if (keycode == Input.Keys.UP) {
            jugadorY += speed;
            jugador = jugadorArriba;
            jugadorArriba.setPlayMode(Animation.PlayMode.LOOP);
        }
        if (keycode == Input.Keys.DOWN) {
            jugadorY += -speed;
            jugador = jugadorAbajo;
            jugadorAbajo.setPlayMode(Animation.PlayMode.LOOP);
        }
        colisionObstaculo(jugadorAnteriorX, jugadorAnteriorY);
        colisionNPC();
        caidaAgujero();
        caidaBarco();

        if (conseguidos < 4) {
            if (cogeTesoro()) {
                conseguidos++;
                restantes--;
                System.out.println("¡Has conseguido un tesoro! te quedan " + restantes);
            }
        } else {
            victoria = true;
        }
    }

    /**
     * Metodo colisionNPC. Comprueba colision con enemigos
     */
    private void colisionNPC() {
        // Calcula el rectangulo en torno al jugador.
        Rectangle rJugador = new Rectangle(jugadorX, jugadorY, anchoJugador, altoJugador);
        Rectangle rNPC;
        // Recorre el array de NPC, para cada uno genera su rectangulo envolvente y comprueba si hay solape.
        for (int i = 0; i < numeroNPCs; i++) {
            rNPC = new Rectangle(noJugadorX[i], noJugadorY[i], anchoNoJugador, altoNoJugador);
            if (rJugador.overlaps(rNPC)) {
                System.out.println("¡Has sido atrapado!");
                cazado = true;
            }
        }
    }

    /**
     * Metodo colisionObstaculo. Comprueba colision con obstaculos
     */
    private void colisionObstaculo(float jugadorAnteriorX, float jugadorAnteriorY) {
        if ((jugadorX < 0 || jugadorY < 0 ||
                jugadorX > (mapaAncho - anchoJugador) ||
                jugadorY > (mapaAlto - altoJugador)) ||
                ((obstaculo[(int) ((jugadorX + anchoJugador / 4) / anchoCelda)][((int) (jugadorY) / altoCelda)]) ||
                        (obstaculo[(int) ((jugadorX + 3 * anchoJugador / 4) / anchoCelda)][((int) (jugadorY) / altoCelda)]))) {
            jugadorX = jugadorAnteriorX;
            jugadorY = jugadorAnteriorY;
            sonidoObstaculo.play(0.5f);
        } else {
            sonidoPasos.play(0.25f);
        }
    }

    /**
     * Metodo caidaAgujero. Comprueba caidas en agujeros
     */
    private void caidaAgujero() {
        if ((jugadorX < 0 || jugadorY < 0 ||
                jugadorX > (mapaAncho - anchoJugador) ||
                jugadorY > (mapaAlto - altoJugador)) ||
                ((agujero[(int) ((jugadorX + anchoJugador / 4) / anchoCelda)][((int) (jugadorY) / altoCelda)]) ||
                        (agujero[(int) ((jugadorX + 3 * anchoJugador / 4) / anchoCelda)][((int) (jugadorY) / altoCelda)]))) {
            caida = true;
            sonidoCaida.play(0.5f);
        } else {
            sonidoPasos.play(0.25f);
        }
    }

    /**
     * Metodo caidaBarco. Comprueba si personaje se sube al barco
     */
    private void caidaBarco() {
        if ((jugadorX < 0 || jugadorY < 0 ||
                jugadorX > (mapaAncho - anchoJugador) ||
                jugadorY > (mapaAlto - altoJugador)) ||
                ((barco[(int) ((jugadorX + anchoJugador / 4) / anchoCelda)][((int) (jugadorY) / altoCelda)]) ||
                        (barco[(int) ((jugadorX + 3 * anchoJugador / 4) / anchoCelda)][((int) (jugadorY) / altoCelda)]))) {
            hundido = true;
            sonidoBarco.play(1f);
        } else {
            sonidoPasos.play(0.25f);
        }
    }

    /**
     * Metodo cogeTesoro. Comprueba solape con sprite de tesoros
     */
    private boolean cogeTesoro() {
        // Calcula el rectangulo en torno al jugador.
        Rectangle rJugador = new Rectangle(jugadorX, jugadorY, anchoJugador, altoJugador);
        Rectangle tesoro1, tesoro2, tesoro3, tesoro4;

        // Recorre el array de NPC, para cada uno genera su rectangulo envolvente y comprueba si hay solape.
        tesoro1 = new Rectangle(tesoro1X, tesoro1Y, anchoTesoro, altoTesoro);
        tesoro2 = new Rectangle(tesoro2X, tesoro2Y, anchoTesoro, altoTesoro);
        tesoro3 = new Rectangle(tesoro3X, tesoro3Y, anchoTesoro, altoTesoro);
        tesoro4 = new Rectangle(tesoro4X, tesoro4Y, anchoTesoro, altoTesoro);

        if (rJugador.overlaps(tesoro1)) {
            //tesoro1X = 420;
            //tesoro1Y = 320;
            tesoro1X = 30;
            tesoro1Y = 0;
            return true;
        }
        if (rJugador.overlaps(tesoro2)) {
            tesoro2X = 60;
            tesoro2Y = 0;
            return true;
        }
        if (rJugador.overlaps(tesoro3)) {
            tesoro3X = 90;
            tesoro3Y = 0;
            return true;
        }
        if (rJugador.overlaps(tesoro4)) {
            tesoro4X = 120;
            tesoro4Y = 0;
            return true;
        }
        return false;
    }
}