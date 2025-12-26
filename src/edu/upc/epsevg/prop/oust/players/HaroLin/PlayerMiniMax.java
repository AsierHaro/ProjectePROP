/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.oust.players.HaroLin;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.IAuto;
import edu.upc.epsevg.prop.oust.PlayerMove;
import edu.upc.epsevg.prop.oust.PlayerType;
import edu.upc.epsevg.prop.oust.SearchType;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author asier
 */
public class PlayerMiniMax implements IPlayer, IAuto {
    private String name;
    private int maxDepth;
    private PlayerType myColor;
    private long nodesExplorats;
    
    /**
     * Constructor que inicializa un jugador Minimax con la profundidad especificada.
     * 
     * @param maxDepth la profundidad máxima de búsqueda en el árbol de juego.
     *                 Valores mayores implican mejor juego pero mayor tiempo de cómputo.
     */
    public PlayerMiniMax(int maxDepth) {
        this.name = "Minimax" + maxDepth;
        this.maxDepth = maxDepth;
    } 
    
    /**
     * Obtiene el nombre del jugador.
     * 
     * @return el nombre del jugador en formato "MinimaxN" donde N es la profundidad máxima
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Calcula y ejecuta el mejor movimiento posible para el estado actual del juego.
     * 
     * <p>Este método utiliza el algoritmo Minimax con poda Alpha-Beta para explorar
     * el árbol de posibles jugadas y seleccionar la que maximiza la ventaja del jugador.
     * También genera la secuencia completa de movimientos incluyendo todas las capturas
     * consecutivas si las hay.</p>
     * 
     * @param gs el estado actual del juego
     * @return un objeto {@code PlayerMove} que contiene la secuencia de movimientos,
     *         el número de nodos explorados, la profundidad y el tipo de búsqueda,
     *         o {@code null} si no hay movimientos posibles
     */
    @Override
    public PlayerMove move(GameStatus gs) {
        myColor = gs.getCurrentPlayer();
        nodesExplorats = 0;
        
        // Obtener movimientos posibles
        List<Point> moviments = gs.getMoves();
        
        if (moviments == null || moviments.isEmpty()) {
            return new PlayerMove(new ArrayList<>(), 0, 0, SearchType.MINIMAX);
        }
        
        // Ordenar movimientos para mejorar la poda alpha-beta
        List<Point> movimentsOrdenats = ordenarMoviments(gs, moviments);
        
        Point millorMoviment = null;
        double millorValor = Double.NEGATIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        
        for (Point moviment : movimentsOrdenats) {
            GameStatus seguentEstat = new GameStatus(gs);
            seguentEstat.placeStone(moviment);
            
            // CORREGIDO: Verificar quién juega después del movimiento
            boolean nextIsMax = (seguentEstat.getCurrentPlayer() == myColor);
            double value = minimax(seguentEstat, maxDepth - 1, alpha, beta, nextIsMax);
            
            if (value > millorValor) {
                millorValor = value;
                millorMoviment = moviment;
            }
            
            alpha = Math.max(alpha, value);
        }
        
        System.out.println("Minimax - Profundidad: " + maxDepth + ", Nodos: " + nodesExplorats);
        
        List<Point> moveSequence = generarSequenciaMoviments(gs, millorMoviment);
        
        return new PlayerMove(moveSequence, nodesExplorats, maxDepth, SearchType.MINIMAX);
    }
    
    /**
    * Ordena los movimientos disponibles según una evaluación heurística
    * rápida para mejorar la eficiencia de la poda Alpha-Beta.
    *
    * @param gs estado actual del juego
    * @param moviments lista de movimientos disponibles
    * @return lista de movimientos ordenados de mejor a peor
    */
    private List<Point> ordenarMoviments(GameStatus gs, List<Point> moviments) {
        // Usar un Map para asociar cada movimiento con su valor
        Map<Point, Double> valores = new HashMap<>();
        
        for (Point mov : moviments) {
            GameStatus temp = new GameStatus(gs);
            temp.placeStone(mov);
            double valor = evaluacioRapida(temp);
            valores.put(mov, valor);
        }
        
        // Ordenar los movimientos por su valor descendente
        List<Point> ordenados = new ArrayList<>(moviments);
        ordenados.sort((a, b) -> Double.compare(valores.get(b), valores.get(a)));
        
        return ordenados;
    }
    
    /**
    * Realiza una evaluación heurística rápida de un estado del juego.
    * Se utiliza únicamente para ordenar movimientos.
    *
    * @param gs estado del juego a evaluar
    * @return valor heurístico aproximado del estado
    */
    private double evaluacioRapida(GameStatus gs) {
        if (gs.isGameOver()) {
            if (gs.GetWinner() == myColor) return 100000;
            if (gs.GetWinner() == null) return 0;
            return -100000;
        }
        
        int misPiezas = 0;
        int oponentePiezas = 0;
        int size = gs.getSize();
        
        PlayerType oponent = (myColor == PlayerType.PLAYER1) ? 
                              PlayerType.PLAYER2 : PlayerType.PLAYER1;
        
        for (int row = 0; row < (2*size)-1; row++) {
            for (int col = 0; col < (2*size)-1; col++) {
                PlayerType color = gs.getColor(row, col);
                if (color == myColor) {
                    misPiezas++;
                } else if (color == oponent) {
                    oponentePiezas++;
                }
            }
        }
        
        return (misPiezas - oponentePiezas) * 10;
    }
    
    /**
    * Genera la secuencia completa de movimientos incluyendo capturas
    * consecutivas a partir de un primer movimiento.
    *
    * @param gs estado actual del juego
    * @param primerMoviment primer movimiento de la secuencia
    * @return lista de movimientos que forman la secuencia
    */
    private List<Point> generarSequenciaMoviments(GameStatus gs, Point primerMoviment) {
        List<Point> sequencia = new ArrayList<>();
        
        if (primerMoviment == null) {
            return sequencia;
        }
        
        GameStatus estat = new GameStatus(gs);
        Point movimentActual = primerMoviment;
        PlayerType jugadorInicial = gs.getCurrentPlayer();
        int maxIterations = 50;
        int iterations = 0;
        
        while (movimentActual != null && iterations < maxIterations) {
            sequencia.add(movimentActual);
            
            estat.placeStone(movimentActual);
            
            if (estat.getCurrentPlayer() == jugadorInicial && !estat.isGameOver()) {
                List<Point> proximMoviments = estat.getMoves();
                if (proximMoviments != null && !proximMoviments.isEmpty()) {
                    movimentActual = seleccionarMillorCaptura(estat, proximMoviments);
                } else {
                    break;
                }
            } else {
                break;
            }
            
            iterations++;
        }
        
        return sequencia;
    }
    
    /**
     * Selecciona la mejor captura de entre los movimientos disponibles.
     * 
     * @param gs el estado actual del juego
     * @param moviments la lista de movimientos disponibles
     * @return el punto correspondiente a la mejor captura
     */
    private Point seleccionarMillorCaptura(GameStatus gs, List<Point> moviments) {
        if (moviments == null || moviments.isEmpty()) {
            return null;
        }
        
        Point millorMoviment = moviments.get(0);
        double millorValor = Double.NEGATIVE_INFINITY;
        
        for (Point moviment : moviments) {
            try {
                GameStatus seguentEstat = new GameStatus(gs);
                seguentEstat.placeStone(moviment);
                double valor = evaluar(seguentEstat);
                
                if (valor > millorValor) {
                    millorValor = valor;
                    millorMoviment = moviment;
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        return millorMoviment;
    }
    
    /**
    * Implementación del algoritmo Minimax con poda Alpha-Beta.
    *
    * @param gs estado actual del juego
    * @param depth profundidad restante de búsqueda
    * @param alpha mejor valor para el jugador maximizador
    * @param beta mejor valor para el jugador minimizador
    * @param maximitzador indica si el nodo actual es maximizador
    * @return valor heurístico del estado evaluado
    */
    private double minimax(GameStatus gs, int depth, double alpha, double beta, boolean maximitzador) { 
        nodesExplorats++;
        
        if (depth == 0 || gs.isGameOver()) {
            return evaluar(gs);
        }
        
        List<Point> moviments = gs.getMoves();
        
        if (moviments == null || moviments.isEmpty()) {
            GameStatus proximEstat = new GameStatus(gs);
            proximEstat.placeStone(null);
            boolean nextIsMax = (proximEstat.getCurrentPlayer() == myColor);
            return minimax(proximEstat, depth - 1, alpha, beta, nextIsMax);
        }
        
        if (maximitzador) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Point moviment : moviments) {
                GameStatus nextState = new GameStatus(gs);
                nextState.placeStone(moviment);
                
                boolean proximEsMax = (nextState.getCurrentPlayer() == myColor);
                
                double eval = minimax(nextState, depth - 1, alpha, beta, proximEsMax);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            
            for (Point moviment : moviments) {
                GameStatus nextState = new GameStatus(gs);
                nextState.placeStone(moviment);
                
                boolean nextIsMax = (nextState.getCurrentPlayer() == myColor);
                
                double eval = minimax(nextState, depth - 1, alpha, beta, nextIsMax);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }
    
     /**
    * Evalúa heurísticamente un estado del juego considerando piezas,
    * movilidad, control del centro y condiciones de victoria.
    *
    * @param gs estado del juego a evaluar
    * @return valor heurístico del estado
    */
    private double evaluar(GameStatus gs) {
        if (gs.isGameOver()) {
            if (gs.GetWinner() == myColor) {
                return 100000;
            } else if (gs.GetWinner() == null) {
                return 0;
            } else {
                return -100000;
            }
        }
        
        int pecesMeves = 0;
        int pecesOponent = 0;
        int size = gs.getSize();
        int controlCentre = 0;
        int centre = size - 1;
        
        PlayerType oponent = (myColor == PlayerType.PLAYER1) ? 
                              PlayerType.PLAYER2 : PlayerType.PLAYER1;
        
        for (int row = 0; row < (2*size)-1; row++) {
            for (int col = 0; col < (2*size)-1; col++) {
                PlayerType color = gs.getColor(row, col);
                if (color == myColor) {
                    pecesMeves++;
                    int distCentre = Math.abs(row - centre) + Math.abs(col - centre);
                    controlCentre += (size - distCentre);
                } else if (color == oponent) {
                    pecesOponent++;
                }
            }
        }
        
        List<Point> movimentsDisponibles = gs.getMoves();
        int movilitat = (movimentsDisponibles != null) ? movimentsDisponibles.size() : 0;
        
        double score = 0;
        score += (pecesMeves - pecesOponent) * 100;
        score += pecesMeves * 10;
        score -= pecesOponent * 10;
        score += movilitat * 5;
        score += controlCentre * 2;
        
        if (pecesOponent < 3) {
            score += 500;
        }
        
        return score;
    }
    
    @Override
    public void timeout() {
        // No se usa en minimax sin IDS
    }
}
