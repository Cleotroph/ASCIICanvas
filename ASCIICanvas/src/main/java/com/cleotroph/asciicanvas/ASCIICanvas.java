package com.cleotroph.asciicanvas;

public class ASCIICanvas implements Runnable {
    // canvas dimensions
    private static final int cwidth = ASCIICanvasManager.ScreenBuffer.getWidth();
    private static final int cheight = ASCIICanvasManager.ScreenBuffer.getHeight();

    // References to buffers, must be refreshed every swap
    private static char[] chars;
    private static int[] colors;

    // current draw state
    private static int color;
    private static char brush;

    // thread for calling ascii render (distinct from AWT render in ScreenBuffer)
    private static Thread thread;

    // instance reference
    private static final ASCIICanvas canvas = new ASCIICanvas();

    // Frame rate management
    private static long lastFrameTime = 0;
    private static int frameCap = 15;

    /**
     * get canvas height in characters
     * @return height of canvas in characters
     */
    public static int getHeight(){
        return cheight;
    }

    /**
     * get canvas width in characters
     * @return width of canvas in characters
     */
    public static int getWidth(){
        return cwidth;
    }

    /**
     * set max frame rate for character canvas drawing.
     * @param fps max frames per second
     */
    public void setFrameRate(int fps){
        frameCap = fps;
    }

    // begin thread
    static void start(){
        thread = new Thread(canvas);
        thread.start();
    }

    // thread management for drawing frames, also manages buffer swap
    @Override
    public void run() {
        onFrame();
        while(true) {
            if (System.currentTimeMillis() > lastFrameTime + (1000 / frameCap)) {
                ASCIICanvasManager.callRender(canvas);
                onFrame();
                lastFrameTime = System.currentTimeMillis();
            }
        }
    }

    // Called every frame after the canvas is drawn to. Swaps the buffer and refreshes buffer references.
    private static void onFrame(){
        ASCIICanvasManager.ScreenBuffer.swapBuffer();
        chars = ASCIICanvasManager.ScreenBuffer.getCharBuffer();
        colors = ASCIICanvasManager.ScreenBuffer.getColorBuffer();
    }

    /**
     * <h1>try to avoid using this.</h1>
     * syncs buffers for when clear is not being called every frame.
     * should be used if there is any movement between frames.
     * warning, this is volatile! could cause screen tearing if called often.
     */
    public void syncBuffer(){
        ASCIICanvasManager.ScreenBuffer.syncBuffer();
    }

    //--------------------------- Draw Functions ------------------------------

    /**
     * Wipes the buffer entirely replacing with empty chars.
     * Normally this should be called on the start of every frame.
     */
    public void clear(){
        ASCIICanvasManager.ScreenBuffer.clear();
        chars = ASCIICanvasManager.ScreenBuffer.getCharBuffer();
    }

    /**
     * set brush color for draw operations.
     * @param color_in index of color.
     */
    public void setColor(int color_in){
        color = color_in;
    }

    /**
     * set the brush character for draw operations.
     * @param brush_in character to draw.
     */
    public void setBrush(char brush_in){
        brush = brush_in;
    }

    /**
     * Draws a vertical or horizontal line using the brush settings.
     * @param x x position of top/left.
     * @param y y position of top/left.
     * @param l length, extends towards positive x/y depending on orientation.
     * @param vertical orientation of the line, true for vertical, false for horizontal.
     */
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

    /**
     * Fills a region with the brush settings.
     * @param x x position of top/left.
     * @param y y position of top/left.
     * @param w width of rect.
     * @param h height of rect.
     * @param filled whether or not the interior is filled (false for only the boarder).
     */
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

    /**
     * Paints a single point with the brush settings.
     * @param x x of point.
     * @param y y of point.
     */
    public void point(int x, int y){
        int pos = x + y * cwidth;
        chars[pos] = brush;
        colors[pos] = color;
    }

    /**
     * draws an unfilled rect using ASCII border chars
     * @param x x position of top/left.
     * @param y y position of top/left.
     * @param w width of rect.
     * @param h height of rect.
     */
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
