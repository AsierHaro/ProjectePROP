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
 * Implementación de un jugador automático basado en el algoritmo Minimax
 * con profundización iterativa (IDS) y poda Alpha-Beta.
 *
 * <p>El jugador incrementa progresivamente la profundidad de búsqueda mientras
 * respeta un límite de tiempo (timeout), seleccionando el mejor movimiento
 * encontrado hasta el momento.</p>
 *
 * <p>Incluye optimizaciones como ordenación heurística de movimientos y una
 * función de evaluación para estimar la calidad de los estados del juego.</p>
 *
 * Implementa las interfaces {@link IPlayer} y {@link IAuto}.
 *
 * @author jieke
 */
public class PlayerMiniMaxIDS implements IPlayer, IAuto {
    
    private String name;
    private PlayerType myColor;
    private long nodesExplorats;
    private int maxDepthReached;
    private boolean timeoutOccurred;
    private long startTime;
    private static final long TIMEOUT_MS = 4500;   

    /**
    * Crea un jugador Minimax con profundización iterativa (IDS).
    */
    public PlayerMiniMaxIDS() {
        this.name = "MiniMaxIDS";
    }

    /**
    * Devuelve el nombre del jugador.
    *
    * @return nombre del jugador
    */
    @Override
    public String getName() {
        return name;
    }
    
    /**
    * Calcula el mejor movimiento a realizar usando Minimax con
    * profundización iterativa y poda Alpha-Beta.
    *
    * @param gs estado actual del juego
    * @return movimiento elegido junto con información de la búsqueda
    */
    @Override
    public PlayerMove move(GameStatus gs) {
        myColor = gs.getCurrentPlayer();
        nodesExplorats = 0;
        maxDepthReached = 0;
        timeoutOccurred = false;
        startTime = System.currentTimeMillis();
        
        List<Point> moves = gs.getMoves();
        
        if (moves == null || moves.isEmpty()) {
            return new PlayerMove(new ArrayList<>(), 0, 0, SearchType.MINIMAX_IDS);
        }
        
        Point bestMove = moves.get(0);
        Point currentBestMove = null;
        int depth = 1;
        
        // IDS: incrementar profundidad mientras haya tiempo
        while (!timeoutOccurred && depth < 50) {
            currentBestMove = searchAtDepth(gs, depth);
            
            if (!timeoutOccurred && currentBestMove != null) {
                bestMove = currentBestMove;
                maxDepthReached = depth;
            }
            
            depth++;
            
            // Parar si ya hemos usado el 70% del tiempo
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > TIMEOUT_MS * 0.7) {
                break;
            }
        }
        
        System.out.println("IDS - Profundidad: " + maxDepthReached + ", Nodos: " + nodesExplorats);
        
        List<Point> moveSequence = generarSequenciaMoviments(gs, bestMove);
        
        return new PlayerMove(moveSequence, nodesExplorats, maxDepthReached, SearchType.MINIMAX_IDS);
    }
    
    /**
    * Ejecuta una búsqueda Minimax hasta una profundidad concreta.
    *
    * @param gs estado actual del juego
    * @param depth profundidad máxima de búsqueda
    * @return el mejor movimiento encontrado a esa profundidad
    */
    private Point searchAtDepth(GameStatus gs, int depth) {
        List<Point> moves = gs.getMoves();
        
        List<Point> movesOrdenados = ordenarMoviments(gs, moves);
        
        Point bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        
        for(Point move : movesOrdenados) {
            if(timeoutOccurred) {
                break;
            }
            
            GameStatus nextState = new GameStatus(gs);
            nextState.placeStone(move);
            
            boolean nextIsMax = (nextState.getCurrentPlayer() == myColor);
            
            double value = minimax(nextState, depth - 1, alpha, beta, nextIsMax);
            
            if(value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
            
            alpha = Math.max(alpha, value);
        }
        
        return bestMove;
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
        // CORREGIDO: Chequear timeout periódicamente
        if(nodesExplorats % 500 == 0) {
            if(System.currentTimeMillis() - startTime > TIMEOUT_MS) {
                timeoutOccurred = true;
                return 0;
            }
        }
        
        if(timeoutOccurred) {
            return 0;
        }
        
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
                if(timeoutOccurred) break;
                
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
                if(timeoutOccurred) break;
                
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
        timeoutOccurred = true;
    }
}
