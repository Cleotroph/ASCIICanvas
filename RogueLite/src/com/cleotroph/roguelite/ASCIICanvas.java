package com.cleotroph.roguelite;

public class ASCIICanvas implements Runnable {
    private static final int cwidth = RogueLite.ScreenBuffer.getWidth();
    private static final int cheight = RogueLite.ScreenBuffer.getHeight();
    private static char[] chars;
    private static int[] colors;
    private static int color;
    private static char brush;
    private static Thread thread;
    private static final ASCIICanvas canvas = new ASCIICanvas();
    private static long lastFrameTime = 0;
    private static final int frameCap = 15;

    public static void start(){
        thread = new Thread(canvas);
        thread.start();
    }

    @Override
    public void run() {
        onFrame();
        while(true) {
            if (System.currentTimeMillis() > lastFrameTime + (1000 / frameCap)) {
                Game.render(canvas);
                onFrame();
                lastFrameTime = System.currentTimeMillis();
            }
        }
    }

    private static void onFrame(){
        RogueLite.ScreenBuffer.swapBuffer();
        chars = RogueLite.ScreenBuffer.getCharBuffer();
        colors = RogueLite.ScreenBuffer.getColorBuffer();
    }

    public void syncBuffer(){
        RogueLite.ScreenBuffer.syncBuffer();
    }

    public void clear(){
        RogueLite.ScreenBuffer.clear();
        chars = RogueLite.ScreenBuffer.getCharBuffer();
    }

    public void setColor(int color_in){
        color = color_in;
    }

    public void setBrush(char brush_in){
        brush = brush_in;
    }

    public void line(int x, int y, int l, boolean vertical){
        if(vertical){
            for(int iy = 0; iy < l;  iy++){
                int pos = x + (y + iy) * cwidth;
                chars[pos] = brush;
                colors[pos] = color;
            }
        }else{
            for(int ix = 0; ix < l;  ix++){
                int pos = (x + ix) + y * cwidth;
                chars[pos] = brush;
                colors[pos] = color;
            }
        }
    }

    public void rect(int x, int y, int w, int h, boolean filled){
        if(filled){
            for(int iy = 0; iy < h; iy++){
                for(int ix = 0; ix < w; ix++){
                    int pos = ix + (iy + y) * cwidth + x;
                    chars[pos] = brush;
                    colors[pos] = color;
                }
            }
        }else{
            for(int ix = 0; ix < w; ix++){
                int pos = x + ix + y * cwidth;
                chars[pos] = brush;
                colors[pos] = color;
                pos = x + ix + (y + (h - 1)) * cwidth;
                chars[pos] = brush;
                colors[pos] = color;
            }
            for(int iy = 1; iy < h - 1; iy++){
                int pos = x + (iy + y) * cwidth;
                chars[pos] = brush;
                colors[pos] = color;
                pos = x + (w - 1) + (iy + y) * cwidth;
                chars[pos] = brush;
                colors[pos] = color;
            }
        }
    }

    public void point(int x, int y){
        int pos = x + y * cwidth;
        chars[pos] = brush;
        colors[pos] = color;
    }

    public void drawPerimieter(int x, int y, int w, int h){
        brush = '═';
        for(int ix = 1; ix < w - 1; ix++){
            int pos = x + ix + y * cwidth;
            chars[pos] = brush;
            colors[pos] = color;
            pos = x + ix + (y + (h - 1)) * cwidth;
            chars[pos] = brush;
            colors[pos] = color;
        }
        brush = '║';
        for(int iy = 1; iy < h - 1; iy++){
            int pos = x + (iy + y) * cwidth;
            chars[pos] = brush;
            colors[pos] = color;
            pos = x + (w - 1) + (iy + y) * cwidth;
            chars[pos] = brush;
            colors[pos] = color;
        }
        brush = '╔';
        int pos = x + y * cwidth;
        chars[pos] = brush;
        colors[pos] = color;
        brush = '╝';
        pos = (x + w - 1) + (y + h - 1) * cwidth;
        chars[pos] = brush;
        colors[pos] = color;
        brush = '╚';
        pos = x + (y + h - 1) * cwidth;
        chars[pos] = brush;
        colors[pos] = color;
        brush = '╗';
        pos = (x + w - 1) + y * cwidth;
        chars[pos] = brush;
        colors[pos] = color;
    }
}
