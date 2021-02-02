package com.cleotroph.roguelite;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game implements KeyListener {
    public static final Game listenerInstance = new Game();
    static int i = 0;
    public static void gameTick(){

    }

    public static void render(ASCIICanvas c){
        //c.clear();

        c.setColor(2);
        c.setBrush('A');
        c.rect(0,1, 10, 10, true);
        c.setColor(3);
        c.setBrush('B');
        c.drawPerimieter(10, 10, 10, 10);
        c.setColor(15);
        c.setBrush('[');
        c.line(i,0, 50, true);
        c.line(0,i, 50, false);
        i++;
        i%=50;
        c.syncBuffer();
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE){
            RogueLite.stop();
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
