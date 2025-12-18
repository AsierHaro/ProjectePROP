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

/**
 *
 * @author asier
 */
public class PlayerMiniMax implements IPlayer, IAuto{
    private String name;
    private int maxDepth;
    private PlayerType myColor;
    private long nodesExplorats;
    
   
    public PlayerMiniMax(int maxDepth){
        this.name = "Minimax" + maxDepth;
        this.maxDepth = maxDepth;
    } 
    
    @Override
    public String getName(){
        return name;
    }

     @Override
    public PlayerMove move(GameStatus gs) {
        myColor = gs.getCurrentPlayer();
        nodesExplorats = 0;
        
        // Obtener movimientos posibles
        List<Point> moves = gs.getMoves();
        
        if (moves.isEmpty()) {
            return null; // Pasar turno
        }
        
        Point bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        
        // Explorar cada movimiento posible
        for (Point move : moves) {
            GameStatus nextState = new GameStatus(gs);
            nextState.placeStone(move);
            
            double value = minimax(nextState, maxDepth - 1, alpha, beta, false);
            
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
            
            alpha = Math.max(alpha, value);
        }
        
        System.out.println("Nodos explorados: " + nodesExplorats);
        
        // Generar la secuencia completa de movimientos (incluyendo capturas)
        List<Point> moveSequence = generateMoveSequence(gs, bestMove);
        
        return new PlayerMove(moveSequence, nodesExplorats, maxDepth, SearchType.MINIMAX);
    }
    
    /**
     * Genera la secuencia completa de movimientos incluyendo todas las capturas
     */
    private List<Point> generateMoveSequence(GameStatus gs, Point firstMove) {
        List<Point> sequence = new ArrayList<>();
        GameStatus tempGs = new GameStatus(gs);
        Point currentMove = firstMove;
        
        while (currentMove != null) {
            sequence.add(currentMove);
            PlayerType currentPlayer = tempGs.getCurrentPlayer();
            tempGs.placeStone(currentMove);
            
            // Si después de mover seguimos siendo el mismo jugador, fue captura
            // y debemos continuar la secuencia
            if (tempGs.getCurrentPlayer() == currentPlayer && !tempGs.isGameOver()) {
                // Buscar el mejor siguiente movimiento en esta secuencia de capturas
                List<Point> nextMoves = tempGs.getMoves();
                if (!nextMoves.isEmpty()) {
                    currentMove = selectBestCapture(tempGs, nextMoves);
                } else {
                    break;
                }
            } else {
                // Ya no capturamos más, termina la secuencia
                break;
            }
        }
        
        return sequence;
    }
    
    /**
     * Selecciona la mejor captura de la lista de movimientos disponibles
     */
    private Point selectBestCapture(GameStatus gs, List<Point> moves) {
        Point bestMove = moves.get(0);
        double bestValue = Double.NEGATIVE_INFINITY;
        
        for (Point move : moves) {
            GameStatus nextState = new GameStatus(gs);
            nextState.placeStone(move);
            double value = evaluate(nextState);
            
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
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
