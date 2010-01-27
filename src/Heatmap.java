import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jdesktop.swingx.graphics.GraphicsUtilities;

/**
 * An example of a "heat map".
 * 
 */
public class Heatmap extends JPanel implements MouseListener {
  /** */
  private static final long serialVersionUID = -2105845119293049049L;

  /** Image drawn below the heat map */
  private final BufferedImage backgroundImage;

  /**  */
  private final BufferedImage dotImage = createFadedCircleImage(96);

  private final BufferedImage monochromeImage;

  /** Lookup operation used to color the monochrome image according to "heat" */
  private LookupOp colorOp;


  public Heatmap(BufferedImage backgroundImage) {
    this.backgroundImage = backgroundImage;
    int width = backgroundImage.getWidth();
    int height = backgroundImage.getHeight();
    
    monochromeImage = createCompatibleTranslucentImage(width, height);

    // Create lookup operation for adding color the monochrome image
    final BufferedImage colorImage =
        createGradientImage(new Dimension(64, 1), Color.WHITE, Color.RED, Color.YELLOW,
            Color.GREEN.darker(), Color.CYAN, Color.BLUE, new Color(0, 0, 0x33));
    final LookupTable colorTable = createColorLookupTable(colorImage, .5f);    
    colorOp = new LookupOp(colorTable, null);
    
    setPreferredSize(new Dimension(width, height));
    addMouseListener(this);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    Graphics2D g2 = (Graphics2D) g;
    g2.drawImage(backgroundImage, null, 0, 0);
    g2.drawImage(monochromeImage, colorOp, 0, 0);
  }

  public void mouseClicked(MouseEvent e) {
    addDotImage(e.getPoint(), .75f);
    repaint();
  }

  /**
   * 
   * @param p
   * @param alpha
   */
  private void addDotImage(Point p, float alpha) {
    int circleRadius = dotImage.getWidth() / 2;
    Graphics2D g = monochromeImage.createGraphics();
    // g.setComposite(BlendComposite.Multiply.derive(alpha));
    g.setComposite(AlphaComposite.SrcOver);
    g.drawImage(dotImage, null, p.x - circleRadius, p.y - circleRadius);
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  /**
   * Creates the color lookup table from an image.
   * 
   * @param im
   * @param alpha alpha channel (between 0.0 and 1.0)
   * @return
   */
  public static LookupTable createColorLookupTable(BufferedImage im, float alpha) {
    int tableSize = 256;
    Raster imageRaster = im.getData();
    double sampleStep = 1d * im.getWidth() / tableSize; // Sample pixels evenly
    byte[][] colorTable = new byte[4][tableSize];
    int[] pixel = new int[1]; // Sample pixel
    Color c;

    for (int i = 0; i < tableSize; ++i) {
      imageRaster.getDataElements((int) (i * sampleStep), 0, pixel);
      c = new Color(pixel[0]);
      colorTable[0][i] = (byte) c.getRed();
      colorTable[1][i] = (byte) c.getGreen();
      colorTable[2][i] = (byte) c.getBlue();
      colorTable[3][i] = (byte) i;
    }

    return new ByteLookupTable(0, colorTable);
  }

  /**
   * Creates an image filled with a linear gradient
   * 
   * @param size size of the image
   * @param colors gradient colors
   * @return
   */
  public static BufferedImage createGradientImage(Dimension size, Color... colors) {
    float[] fractions = new float[colors.length];
    float step = 1f / colors.length;

    for (int i = 0; i < colors.length; i++) {
      fractions[i] = i * step;
    }

    LinearGradientPaint gradient = new LinearGradientPaint(
        0, 0, size.width, 1, fractions, colors, MultipleGradientPaint.CycleMethod.REPEAT);
    BufferedImage im = createCompatibleTranslucentImage(size.width, size.height);
    Graphics2D g = im.createGraphics();
    g.setPaint(gradient);
    g.fillRect(0, 0, size.width, size.height);
    g.dispose();
    return im;
  }

  /**
   * Creates a translucent image.
   * 
   * @param width
   * @param height
   * @return
   */
  public static BufferedImage createCompatibleTranslucentImage(int width, int height) {
    return GraphicsUtilities.createCompatibleTranslucentImage(width, height);
  }

  /**
   * Creates an image with a monochrome circle (fading from center to edges,
   * black to white).
   * 
   * @param size diameter of the circle and width and height of the image
   * @return
   */
  public static BufferedImage createFadedCircleImage(int size) {
    float radius = size / 2f;
    RadialGradientPaint gradient =
        new RadialGradientPaint(radius, radius, radius, new float[] {0f, 1f}, new Color[] {
            Color.BLACK, new Color(0x00ffffff, true)});
    BufferedImage im = createCompatibleTranslucentImage(size, size);
    Graphics2D g = im.createGraphics();
    g.setPaint(gradient);
    g.fillOval(0, 0, size, size);
    g.dispose();
    return im;
  }

  public static void main(String... args) throws IOException {
    BufferedImage backgroundImage = ImageIO.read(Heatmap.class.getResource("map.png"));
    JPanel comp = new Heatmap(backgroundImage);
    JFrame frame = new JFrame("Heatmap");
    frame.add(comp);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    frame.pack();
    frame.setVisible(true);
  }
}
