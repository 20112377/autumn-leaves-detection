package utils;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageProcessorTest {

    @Test
    void isMatchReturnsTrueWhenHueWithinTolerance() {
        List<Color> targets = List.of(Color.hsb(120, 0.8, 0.8));
        assertTrue(ImageProcessor.isMatch(Color.hsb(115, 0.8, 0.8), targets, 10,0.5,0.5));
    }
    @Test
    void withinHueToleranceSaturationBelow() {
        List<Color> targets = List.of(Color.hsb(120, 0.8, 0.8));
        assertFalse(ImageProcessor.isMatch(Color.hsb(120, 0.2, 0.8), targets, 10, 0.4 , 0.5));
    }
    @Test
    void withinHueBrightnessBelow() {
        List<Color> targets = List.of(Color.hsb(120, 0.8, 0.8));
        assertFalse(ImageProcessor.isMatch(Color.hsb(120, 0.8, 0.12), targets, 10, 0.4 , 0.5));
    }
    @Test
    void wraparoundTest(){
        List<Color> targets = List.of(Color.hsb(5, 0.8, 0.8));
        assertTrue(ImageProcessor.isMatch(Color.hsb(355, 0.8, 0.8), targets, 10, 0.5, 0.5));
    }
    @Test
    void outsideToleranceReturnsFalse() {
        List<Color> targets = List.of(Color.hsb(120, 0.8, 0.8));
        assertFalse(ImageProcessor.isMatch(Color.hsb(60, 0.8, 0.8), targets, 10, 0.9,0.95));
    }
    @Test
    void emptyTargetsReturnsFalse() {
        assertFalse(ImageProcessor.isMatch(Color.hsb(120, 0.8, 0.8), List.of(), 10, 0,0));
    }

}