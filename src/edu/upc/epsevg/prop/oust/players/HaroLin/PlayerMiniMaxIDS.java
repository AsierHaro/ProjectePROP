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
 * Jugador Minimax con IDS.
 * Aumenta progresivamente la profundidad respetando el timeout de 5 segundos.
 * 
 * @author jieke
 */
public class PlayerMiniMaxIDS implements IPlayer, IAuto {
    
    private String name;
    private PlayerType myColor;
    private long nodesExplored;
    private int maxDepthReached;
    private boolean timeoutOccurred;
    private long startTime;
    private static final long TIMEOUT_MS = 4500; // Dejamos un margen de seguridad (4.5 segundos)

    public PlayerMiniMaxIDS() {
        this.name = "MiniMaxIDS";
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public PlayerMove move(GameStatus gs) {
        myColor = gs.getCurrentPlayer();
        nodesExplored = 0;
        maxDepthReached = 0;
        timeoutOccurred = false;
        startTime = System.currentTimeMillis();
        
        List<Point> moves = gs.getMoves();
        
        // Si no hay movimientos, pasar turno con lista vacía
        if (moves.isEmpty() || moves == null) {
            return new PlayerMove(new ArrayList<>(), 0, 0, SearchType.MINIMAX_IDS);
        }
        
        Point bestMove = moves.get(0);
        Point currentBestMove = null;
        int depth = 1;
        
        // Iterative Deepening
        while (!timeoutOccurred && depth < 50) {
            currentBestMove = searchAtDepth(gs, depth);
            
            if (!timeoutOccurred && currentBestMove != null) {
                bestMove = currentBestMove;
                maxDepthReached = depth;
            }
            
            depth++;
            
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > TIMEOUT_MS * 0.7) {
                break;
            }
        }
        
        System.out.println("IDS - Profundidad: " + maxDepthReached + ", Nodos: " + nodesExplored);
        
        List<Point> moveSequence = generateMoveSequence(gs, bestMove);
        
        return new PlayerMove(moveSequence, nodesExplored, maxDepthReached, SearchType.MINIMAX_IDS);
    }
    
    private Point searchAtDepth(GameStatus gs, int depth) {
        List<Point> moves = gs.getMoves();
        Point bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        
        for(Point move : moves) {
            if(timeoutOccurred) {
                break;
            }
            
            GameStatus nextState = new GameStatus(gs);
            nextState.placeStone(move);
            
            double value = minimax(nextState, depth - 1, alpha, beta, false);
            
            if(value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
            
            alpha = Math.max(alpha, value);
        }
        
        return bestMove;
    }
    
    private double minimax(GameStatus gs, int depth, double alpha, double beta, boolean maximizing) {
        if(nodesExplored % 500 == 0) {
            if(System.currentTimeMillis() - startTime > TIMEOUT_MS) {
                timeoutOccurred = true;
                return 0;
            }
        }
        
        if(timeoutOccurred) {
            return 0;
        }
        
        nodesExplored++;
        
        if(depth == 0 || gs.isGameOver()) {
            return evaluate(gs);
        }
        
        List<Point> moves = gs.getMoves();
        
        if(moves.isEmpty()) {
            GameStatus nextState = new GameStatus(gs);
            nextState.placeStone(null);
            return minimax(nextState, depth - 1, alpha, beta, !maximizing);
        }
        
        if(maximizing) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for(Point move : moves) {
                if(timeoutOccurred) break;
                
                GameStatus nextState = new GameStatus(gs);
                nextState.placeStone(move);
                
                boolean nextIsMax = (nextState.getCurrentPlayer() == myColor);
                
                double eval = minimax(nextState, depth - 1, alpha, beta, nextIsMax);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                if(beta <= alpha) {
                    break;
                }           
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            
            for(Point move : moves) {
                if(timeoutOccurred) break;
                
                GameStatus nextState = new GameStatus(gs);
                nextState.placeStone(move);
                
                boolean nextIsMax = (nextState.getCurrentPlayer() == myColor);
                
                double eval = minimax(nextState, depth - 1, alpha, beta, nextIsMax);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                if(beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }
    
    private double evaluate(GameStatus gs) {
        if(gs.isGameOver()) {
            if(gs.GetWinner() == myColor) {
                return 100000;
            } else if(gs.GetWinner() == null) {
                return 0;
            } else {
                return -100000;
            }
        }
        int myPieces = 0;
        int opponentPieces = 0;
        int size = gs.getSize();
        int centerControl = 0;
        int center = size;

        PlayerType opponent = (myColor == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;

        for(int row = 0; row < (2*size)-1; row++) {
            for(int col = 0; col < (2*size)-1; col++) {
                    PlayerType color = gs.getColor(row, col);
                if(color == myColor) {
                    myPieces++;
                    int distToCenter = Math.abs(row - center) + Math.abs(col - center);
                    centerControl += (size - distToCenter);
                } else if(color == opponent) {
                    opponentPieces++;
                }
            }
        }

        double score = 0;

        score += (myPieces - opponentPieces) * 100;
        score += myPieces * 10;
        score -= opponentPieces * 10;

        if(opponentPieces < 3) {
            score += 500;
        }

        return score;
    }
    
    private List<Point> generateMoveSequence(GameStatus gs, Point firstMove) {
        List<Point> sequence = new ArrayList<>();
        
        if (firstMove == null) {
            return sequence;
        }
        
        GameStatus tempGs = new GameStatus(gs);
        Point currentMove = firstMove;
        int maxIterations = 50;
        int iterations = 0;
        
        while (currentMove != null && iterations < maxIterations) {
            sequence.add(currentMove);
            PlayerType currentPlayer = tempGs.getCurrentPlayer();
            
            try {
                tempGs.placeStone(currentMove);
            } catch (Exception e) {
                System.err.println("Error en generateMoveSequence: " + currentMove);
                break;
            }
            
            if (tempGs.getCurrentPlayer() == currentPlayer && !tempGs.isGameOver()) {
                List<Point> nextMoves = tempGs.getMoves();
                if (nextMoves != null && !nextMoves.isEmpty()) {
                    currentMove = selectBestCapture(tempGs, nextMoves);
                } else {
                    break;
                }
            } else {
                break;
            }
            
            iterations++;
        }
        
        return sequence;
    }
        
    private Point selectBestCapture(GameStatus gs, List<Point> moves) {
        if (moves == null || moves.isEmpty()) {
            return null;
        }

        Point bestMove = moves.get(0);
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Point move : moves) {
            try {
                GameStatus nextState = new GameStatus(gs);
                nextState.placeStone(move);
                double value = evaluate(nextState);

                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
            } catch (Exception e) {
                continue;
            }
        }

        return bestMove;
    }
        
        @Override
        public void timeout() {
            timeoutOccurred = true;
    }
}
