
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Getting a Rectangle of interest on the screen.
 Requires the MotivatedEndUser API - sold separately. */
public class cropTool {

    private static final int MAX_SIZE = 600;
    private static String currentFileName;
    private static Logger logger;
    private static double scaleFactor;
    private static int reverseScale;
    private static double aspectRatio;
    private static Logger.LogEntry logEntry;
    private static int left;
    private static boolean needRotate;
    Rectangle captureRect;

    cropTool(final BufferedImage image) {
        final BufferedImage screenCopy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        final JLabel screenLabel = new JLabel(new ImageIcon(screenCopy));
        final JScrollPane screenScroll = new JScrollPane(screenLabel);
        needRotate = false;
        screenScroll.setSize(new Dimension(
                (int) (image.getWidth() / 3),
                (int) (image.getHeight() / 3)));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(screenScroll, BorderLayout.CENTER);

        final JLabel selectionLabel = new JLabel(
                "Drag a rectangle in the image shot!");
        panel.add(selectionLabel, BorderLayout.SOUTH);

        repaint(image, screenCopy);
        screenLabel.repaint();

        screenLabel.addMouseMotionListener(new MouseMotionAdapter() {

            Point start = new Point();
            Point lastEnd = new Point();

            @Override
            public void mouseMoved(MouseEvent me) {
                start = me.getPoint();
                lastEnd = me.getPoint();
                repaint(image, screenCopy);
                selectionLabel.setText("Start Point: [" + start.x + "," + start.y +"]");
                screenLabel.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent me) {
                Point end = me.getPoint();

                if (captureRect != null
                        && lastEnd.x > captureRect.x
                        && lastEnd.y > captureRect.y
                        && lastEnd.x < captureRect.x + captureRect.width
                        && lastEnd.y < captureRect.y + captureRect.height){
                    int deltaX = end.x - lastEnd.x;
                    int deltaY = end.y - lastEnd.y;

                    if (captureRect.x + deltaX < 0)
                        deltaX = -captureRect.x;
                    if (captureRect.x + captureRect.width + deltaX > image.getWidth())
                        deltaX = image.getWidth() - captureRect.x - captureRect.width;

                    if (captureRect.y + deltaY < 0)
                        deltaY = -captureRect.y;
                    if (captureRect.y + captureRect.height + deltaY > image.getHeight())
                        deltaY = image.getHeight() - captureRect.y - captureRect.height;

                    captureRect.x += deltaX;
                    captureRect.y += deltaY;
                } else {
                    end.x = Math.min(end.x, image.getWidth());
                    end.y = Math.min(end.y, image.getHeight());
                    if (aspectRatio < 1) {
                        if (((double)(end.y-start.y)) / (end.x-start.x) > aspectRatio ){
                            end.setLocation(start.x + ((double)(end.y-start.y) * aspectRatio), end.y);
                            if (end.x > image.getWidth()) {
                                end.x = image.getWidth();
                                end.y = (int) (start.y + (end.x - start.x) / aspectRatio);
                            }
                        } else if (((double)(end.y-start.y)) / (end.x-start.x) < aspectRatio){
                            end.setLocation(start.x + ((double)(end.y-start.y) / aspectRatio), end.y);
                        }
                    } else {
                        if (((double)(end.y-start.y)) / (end.x-start.x) < aspectRatio ){
                            end.setLocation(start.x + ((double)(end.y-start.y) * aspectRatio), end.y);
                            if (end.x > image.getWidth()) {
                                end.x = image.getWidth();
                                end.y = (int) (start.y + (end.x - start.x) / aspectRatio);
                            }
                        } else if (((double)(end.y-start.y)) / (end.x-start.x) > aspectRatio){
                            end.setLocation(start.x + ((double)(end.y-start.y) / aspectRatio), end.y);
                            if (end.x > image.getWidth()) {
                                end.x = image.getWidth();
                                end.y = (int) (start.y + (end.x - start.x) * aspectRatio);
                            }
                        }
                    }
                    captureRect = new Rectangle(start,
                            new Dimension(end.x-start.x, end.y-start.y));
                }
                lastEnd = me.getPoint();
                repaint(image, screenCopy);
                screenLabel.repaint();
                selectionLabel.setText("Rectangle: " + captureRect);
            }
        });

        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setResizable(false);
        panel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (captureRect != null) {
                        dialog.dispose();
                        try {
                            logEntry.fileName = currentFileName;
                            logEntry.startLeft = captureRect.x * reverseScale;
                            logEntry.startRight = (captureRect.x + captureRect.width) * reverseScale;
                            logEntry.startTop = captureRect.y * reverseScale;
                            logEntry.startBottom = (captureRect.y + captureRect.height) * reverseScale;

                            logger.addEntry(logEntry);
                            System.out.println("Added entry: " + logEntry);

                        } catch (NullPointerException ignored){}
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_R) {
                    needRotate = true;
                    dialog.dispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
        panel.setFocusable(true);
        //JOptionPane.showMessageDialog(null, panel, "Crop Tool - Just " + (left--) + " more to go...", JOptionPane.QUESTION_MESSAGE);
        dialog.add(panel);
        dialog.setTitle("Crop Tool - Just " + (left--) + " more to go...");
        dialog.pack();
        dialog.setVisible(true);


    }

    public void repaint(BufferedImage orig, BufferedImage copy) {
        Graphics2D g = copy.createGraphics();
        g.drawImage(orig,0,0, null);
        if (captureRect!=null) {
            g.setColor(Color.RED);
            g.draw(captureRect);
            g.setColor(new Color(255, 255, 255, 150));
            g.fill(captureRect);
        }
        g.dispose();
    }

    public static void main(String[] args) throws Exception {

        File directory;
        if (args.length > 0) {
            directory = new File(args[0]);
        } else {
            directory = new File("C:\\robot\\input");
        }
        logger = new Logger();

        cropTool.left = directory.listFiles().length;
        int counter = 0;
        for (File f : directory.listFiles()) {
            currentFileName = f.getName().replaceAll(" ", "");
            BufferedImage original = null;
            try {
                original = ImageIO.read(f);
            } catch (IIOException e) {
                continue;
            }
            reverseScale = 1;
            logEntry = new Logger.LogEntry();
            BufferedImage bufferedImage = original;
            if (original == null) continue;

            if (original.getWidth() > MAX_SIZE || original.getHeight() > MAX_SIZE) {
                for (int i = 2; i < 30; i++) {
                    scaleFactor = 1d/i;
                    reverseScale = i;
                    aspectRatio = ((double)original.getHeight()) / original.getWidth();
                    if (original.getWidth() * scaleFactor < MAX_SIZE && original.getHeight() * scaleFactor < MAX_SIZE) {
                        bufferedImage = scaleImage( original,
                                (int) (original.getWidth() * scaleFactor),
                                (int) (original.getHeight() * scaleFactor));
                        break;
                    }
                }
            }
            new cropTool(bufferedImage);
            BufferedImage rotated = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
            rotated.setData(original.getData());
            int angel = 1;
            while (needRotate) {
                AffineTransform transform = new AffineTransform();
                transform.rotate(Math.PI/2 * angel, angel == 1 ? original.getHeight()/2 : angel == 3 ? original.getWidth()/2 : original.getWidth()/2, angel == 3 ? original.getWidth()/2 : original.getHeight()/2);
                AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
                rotated = new BufferedImage(rotated.getHeight(), rotated.getWidth(), rotated.getType());
                op.filter(original, rotated);
                if (rotated.getWidth() > MAX_SIZE || rotated.getHeight() > MAX_SIZE) {
                    for (int i = 2; i < 30; i++) {
                        scaleFactor = 1d/i;
                        reverseScale = i;
                        aspectRatio = ((double)rotated.getHeight()) / rotated.getWidth();
                        if (rotated.getWidth() * scaleFactor < MAX_SIZE && rotated.getHeight() * scaleFactor < MAX_SIZE) {
                            bufferedImage = scaleImage( rotated,
                                    (int) (rotated.getWidth() * scaleFactor),
                                    (int) (rotated.getHeight() * scaleFactor));
                            break;
                        }
                    }
                }

                new cropTool(bufferedImage);
                angel = (angel + 1) % 4;
            }
            ImageIO.write(rotated, "jpeg", new File(directory.getAbsolutePath().concat("\\" + currentFileName)));
            if (!f.getName().equals(currentFileName)) {
                f.delete();
            }
        }



        logger.writeToFile(directory.getAbsolutePath().concat("\\input.csv"));
    }

    public static BufferedImage scaleImage(BufferedImage img, int width, int height) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        if (imgWidth*height < imgHeight*width) {
            width = imgWidth*height/imgHeight;
        } else {
            height = imgHeight*width/imgWidth;
        }
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setBackground(Color.BLACK);
            g.clearRect(0, 0, width, height);
            g.drawImage(img, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return newImage;
    }
}