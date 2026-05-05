package utils;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.util.List;

public class ImageProcessor {

    /**
     * Adjusts the image with the various sliders for hue, saturation and brightness
     * @param original - the original image that is getting changed
     * @param hueAdjust - the value hue is getting changed by
     * @param satAdjust - the value saturation is getting changed by
     * @param brightAdjust - the value brightness is getting changed by
     * @return - the image with the relevant adjustments added
     */
    public static Image adjustImage(Image original, double hueAdjust, double satAdjust, double brightAdjust) {
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();


        WritableImage output = new WritableImage(width, height);
        PixelReader reader = original.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        //loop through each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y); //get the colour of the current pixel

                //add any adjustments
                double hue = color.getHue() + hueAdjust;
                double saturation = color.getSaturation() * satAdjust;
                double brightness = color.getBrightness() * brightAdjust;

                //saturation and brightness both have a max value of 1 and a min value of 0,
                saturation = Math.min(1.0, Math.max(0.0, saturation));
                brightness = Math.min(1.0, Math.max(0.0, brightness));

                Color newColor = Color.hsb(hue % 360, saturation, brightness, color.getOpacity());
                //creates a new colour, hue is wrapped to 360.
                writer.setColor(x, y, newColor); //writes new colour to pixel
            }
        }

        return output;
    }

    /**
     * Processes the image from colour to black and white, white pixels being part of the chosen colour and
     * black being any other pixel.
     * @param original - the original image
     * @param targets - the list of colours to be found.
     * @param hueTolerance - range the hue can fall in
     * @param minSat - minimum saturation of a pixel
     * @param minBright - minimum brightness of a pixel
     * @return - the image processed to black and white, white being chosen colours black being everything else
     */
    public static WritableImage processBlackWhiteConversion(Image original, List<Color> targets, double hueTolerance, double minSat, double minBright) {
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        WritableImage output = new WritableImage(width, height);

        PixelReader reader = original.getPixelReader();
        PixelWriter writer = output.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color colour = reader.getColor(x, y);

                //check if pixel matches a target colour
                if (isMatch(colour, targets, hueTolerance, minSat, minBright))  {
                    writer.setColor(x, y, Color.WHITE); //pixel matches to chosen colour
                } else {
                    writer.setColor(x, y, Color.BLACK);
                }
            }
        }
        return output;
    }

    /**
     * Checks the colour against a list of chosen colours, if the colour matches return true
     * @param current - current colour being checked
     * @param targets - list of chosen colours
     * @param tolerance - the range of the tolerance for hue
     * @param minSat - the minimum saturation
     * @param minBright - the minimum brightness
     * @return - returns a boolean value based on if the colour is a match
     */
    static boolean isMatch(Color current, List<Color> targets, double tolerance, double minSat, double minBright) {
        if (targets.isEmpty()) return false;
        if (current.getSaturation() < minSat || current.getBrightness() < minBright) return false;

        //loop through all colours in the list of colours
        for (Color target : targets) {
            double diff = Math.abs(current.getHue() - target.getHue()); //find the difference between the hue of the current pixel and the hue of the pixel in the list.
            if (diff > 180) diff = 360 - diff; //corrects for wraparound if difference is greater than 180
            if (diff <= tolerance + 1e-5) { //very small decimal added on to account for rounding errors
                return true;
            }
        }
        return false;
    }
}


