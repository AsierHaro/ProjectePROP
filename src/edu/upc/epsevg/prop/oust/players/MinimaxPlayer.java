/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.oust.players;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.IAuto;
import edu.upc.epsevg.prop.oust.PlayerMove;
import edu.upc.epsevg.prop.oust.PlayerType;
import edu.upc.epsevg.prop.oust.SearchType;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author asier
 */
public class MinimaxPlayer implements IPlayer, IAuto{
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
    public MinimaxPlayer(int maxDepth){
        this.name = "Minimax" + maxDepth;
        this.maxDepth = maxDepth;
    } 
    
    /**
     * Obtiene el nombre del jugador.
     * 
     * @return el nombre del jugador en formato "MinimaxN" donde N es la profundidad máxima
     */
    @Override
    public String getName(){
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
        
        if (moviments.isEmpty()) {
            return null; // Pasar turno
        }
        
        Point millorMoviment = null;
        double millorValor = Double.NEGATIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        
        for (Point moviment : moviments) {
            GameStatus seguentEstat = new GameStatus(gs);
            seguentEstat.placeStone(moviment);
            
            double value = minimax(seguentEstat, maxDepth - 1, alpha, beta, false);
            
            if (value > millorValor) {
                millorValor = value;
                millorMoviment = moviment;
            }
            
            alpha = Math.max(alpha, value);
        }
        
        System.out.println("Nodos explorados: " + nodesExplorats);
        
        List<Point> moveSequence = generateMoveSequence(gs, millorMoviment);
        
        return new PlayerMove(moveSequence, nodesExplorats, maxDepth, SearchType.MINIMAX);
    }
    
    /**
     * Genera la secuencia completa de movimientos incluyendo todas las capturas consecutivas.
     * 
     * <p>En algunos juegos como Othello, es posible realizar múltiples capturas en un mismo turno.
     * Este método simula todas las capturas consecutivas hasta que el turno termine o no haya
     * más capturas disponibles.</p>
     * 
     * @param gs el estado actual del juego
     * @param primerMoviment el primer movimiento de la secuencia
     * @return una lista con todos los puntos de la secuencia de movimientos
     */
    private List<Point> generateMoveSequence(GameStatus gs, Point primerMoviment) {
        List<Point> sequencia = new ArrayList<>();
        GameStatus estat = new GameStatus(gs);
        Point movimentActual = primerMoviment;
        
        while (movimentActual != null) {
            sequencia.add(movimentActual);
            PlayerType jugadorActual = estat.getCurrentPlayer();
            estat.placeStone(movimentActual);
            if (estat.getCurrentPlayer() == jugadorActual && !estat.isGameOver()) {
                List<Point> proximMoviments = estat.getMoves();
                if (!proximMoviments.isEmpty()) {
                    movimentActual = selectBestCapture(estat, proximMoviments);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        
        return sequencia;
    }
    
    /**
     * Selecciona la mejor captura de entre los movimientos disponibles.
     * 
     * <p>Evalúa cada movimiento posible y selecciona el que produce el mejor
     * valor según la función de evaluación heurística.</p>
     * 
     * @param gs el estado actual del juego
     * @param moviments la lista de movimientos disponibles
     * @return el punto correspondiente a la mejor captura
     */
    private Point selectBestCapture(GameStatus gs, List<Point> moviments) {
        Point millorMoviment = moviments.get(0);
        double millorValor = Double.NEGATIVE_INFINITY;
        
        for (Point moviment : moviments) {
            GameStatus seguentEstat = new GameStatus(gs);
            seguentEstat.placeStone(moviment);
            double valor = evaluate(seguentEstat);
            
            if (valor > millorValor) {
                millorValor = valor;
                millorMoviment = moviment;
            }
        }
        
        return millorMoviment;
    }
    
    
    /**
     * Algoritmo Minimax con poda Alpha-Beta para búsqueda adversarial.
     * 
     * <p>Explora recursivamente el árbol de juego hasta la profundidad especificada
     * o hasta alcanzar un estado terminal. Utiliza poda Alpha-Beta para reducir
     * el número de nodos que deben ser evaluados.</p>
     * 
     * @param gs el estado actual del juego
     * @param depth la profundidad restante de búsqueda
     * @param alpha el mejor valor ya encontrado para el jugador maximizador (límite inferior)
     * @param beta el mejor valor ya encontrado para el jugador minimizador (límite superior)
     * @param maximitzador {@code true} si es el turno del jugador maximizador,
     *                     {@code false} si es el turno del jugador minimizador
     * @return el valor heurístico del estado evaluado
     */
    private double minimax(GameStatus gs, int depth, double alpha, double beta, boolean maximitzador){
        nodesExplorats++;
        if(depth ==0 || gs.isGameOver()){
            return evaluate(gs);
        }
        List<Point> moviments = gs.getMoves();
        
        if(moviments.isEmpty()){
            GameStatus proximEstat = new GameStatus(gs);
            proximEstat.placeStone(null);
            return minimax(proximEstat,depth-1,alpha,beta,!maximitzador);
        }
        
        if(maximitzador){
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
        }else {
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
     * Función de evaluación heurística que calcula el valor de un estado del juego.
     * 
     * <p>La función considera varios factores:</p>
     * <ul>
     *   <li>Si el juego ha terminado, retorna +10000 para victoria o -10000 para derrota</li>
     *   <li>Diferencia de piezas entre el jugador y el oponente (peso: 10)</li>
     *   <li>Bonificación por número de piezas propias (peso: 2)</li>
     *   <li>Penalización por número de piezas del oponente (peso: 2)</li>
     *   <li>Bonificación si el oponente tiene menos de 5 piezas (peso: 50)</li>
     * </ul>
     * 
     * @param gs el estado del juego a evaluar
     * @return un valor numérico que representa la bondad del estado para el jugador actual.
     *         Valores positivos favorecen al jugador, valores negativos al oponente.
     */
    private double evaluate(GameStatus gs) {
        if (gs.isGameOver()) {
            if (gs.GetWinner() == myColor) {
                return 10000;
            } else {
                return -10000;
            }
        }
        int pecesMeves = 0;
        int pecesOponent = 0;
        int size = gs.getSize();
        
        PlayerType oponent = (myColor == PlayerType.PLAYER1) ? 
                              PlayerType.PLAYER2 : PlayerType.PLAYER1;
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                PlayerType color = gs.getColor(row, col);
                if (color == myColor) {
                    pecesMeves++;
                } else if (color == oponent) {
                    pecesOponent++;
                }
            }
        }
        double score = (pecesMeves - pecesOponent) * 10;
        score += pecesMeves * 2;
        score -= pecesOponent * 2;
        if (pecesOponent < 5) {
            score += 50;
        }
        
        return score;
    }
    @Override
    public void timeout() {
        
    }
    
}
