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
    
    
    
    
    
    
}
