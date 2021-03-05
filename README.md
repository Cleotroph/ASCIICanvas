# ASCII Canvas

Cleotroph - 03/02/2021

A library for java based ASCII games. 

Built using only default java libs (AWT + Swing).

Free for unlicensed use.

# How to include the library

[How to add ASCII Canvas to a project using maven or gradle](https://jitpack.io/#Cleotroph/ASCIICanvas/main-SNAPSHOT)

# How to use the library

The first thing you'll want to do is set up your canvas class, which can be done by extending ASCIICanvas and adding implementations to the abstract methods.

```java
public class ExampleProject extends ASCIICanvas {
  @Override
  public void load() {

  }

  @Override
  public void save() {

  }

  @Override
  public void render() {

  }

  @Override
  public void tick() {

  }

  @Override
  public void keyTyped(KeyEvent keyEvent) {

  }

  @Override
  public void keyPressed(KeyEvent keyEvent) {

  }

  @Override
  public void keyReleased(KeyEvent keyEvent) {

  }
}
```
Each of these methods is called automatically at certain times.
- load() is called when the canvas is started.
- save() is called when the canvas is closed using exit().
- render() is called once per frame.
- tick() is called once per game tick.
- The key functions are inherited from the AWT KeyListener class, more info can be found in the [AWT docs](https://docs.oracle.com/javase/7/docs/api/java/awt/package-summary.html)

The next step is to initialize your canvas class. One way to do this is by adding a main method inside the canvas class.
```java
public class ExampleProject extends ASCIICanvas{
//...
  public static void main(String[] args){
    final ExampleProject canvas = new ExampleProject();
  }
//...
}
```
This will initialize, but not start the canvas. To actually start the canvas you need to call the super of the constructor and then the start() function. Normally you pass just the window name into the constructor.
```java
public class ExampleProject extends ASCIICanvas{
//...
  public ExampleProject(){
    super("My Project");
    start();
  }
//...
}
```
Now your project is ready to go, and you can start calling render functions from the draw function. It is recommeneded that you avoid draw calls outside of the render function as this could cause desynchronization issues. The tick function should be used for periodic logic updates. This separation will keep the draw loop running smoothly and independently of game logic.

# Reference

## clear()
This function clears the screen and should be called at the begining of every frame. If you don't call this every frame, you need to call syncBuffer() instead, though this is not recommeneded as this could cause screen tearing

## frameRate(int rate)
This function sets the frame rate of the canvas to `rate` fps.

## tickRate(int rate)
This function sets the tick rate of the canvas to `rate` tps.

## start()
This function is called to start the canvas drawing and ticking.

## exit()
This function can be called to stop the canvas and the program.

## setColor(int color)
This function sets the color for subsequent draw calls. The color is specified as the index in the internal colors array. The default color array is defined as follows
```java
{
  new Color(0xFFFFFF),
  new Color(0xFF0000),
  new Color(0x00FF00),
  new Color(0x0000FF),
  new Color(0xFFFF00),
  new Color(0x00FFFF),
  new Color(0xFF00FF),
  new Color(0xC0C0C0),
  new Color(0x808080),
  new Color(0x800000),
  new Color(0xC0C000),
  new Color(0x008000),
  new Color(0x800080),
  new Color(0x008080),
  new Color(0x000080),
  new Color(0x333333)
}
```
Color map support will be added in an upcoming version.

## setBrush(char brush)
This sets the character which will be used for subsequent draw functions.

## line(int x, int y, int l, boolean vertical)
This draws a line using the brush + color, starting at (x, y) and extending l characters either down or to the right (in positive x and y).

## rect(int x, int y, int w, int h, boolean filled)
This draws a rectangle either filled or unfilled at (x, y) and extending to (x + w, y + h).

## point(int x, int y)
This draws the brush at (x, y)

## drawPerimiter(int x, int y, int w, int h)
This draws an unfilled rectange like rect(), but uses the ASCII double like characters (such as '║') to generate a border.

# More info

- A custom preloader can be added using the preloader class in the documentation. This will be added to the reference at some point in the future. 

[Documentation](https://javadoc.jitpack.io/com/github/Cleotroph/ASCIICanvas/main-SNAPSHOT/javadoc/index.html)
