package com.cleotroph.asciicanvas;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * The class for users to extend and implement methods for.
 * Houses drawing and gametick functions which run on their own threads.
 */
public abstract class Game implements KeyListener {

    /**
     * Called to start the game. Opens the prelauncher and the game after settings are selected.
     * @param name Name of the window
     */
    public void LaunchGame(String name){
        ASCIICanvasManager.PreLauncher.Launch(name, this);
    }


    /**
     * Called once per game tick, rate can be modified by calling ASCIICanvasManager.setTickCap();
     */
    public abstract void gameTick();


    /**
     * Called once on init
     */
    public abstract void load();


    /**
     * Called once when the window is closed by an in game command (call of ASCIICanvasManager.stop())
     */
    public abstract void save();

    /**
     * Called once per frame, rate can be modified by calling ASCIICanvas.setFrameRate();
     * @param c Reference to the canvas, used to call all draw functions
      */
    public abstract void render(ASCIICanvas c);

    // Key events (taken from AWT), default is close on escape press.
    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE){
            ASCIICanvasManager.stop();
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}
}
