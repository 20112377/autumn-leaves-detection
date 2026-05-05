package utils;

import javafx.animation.PauseTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for drawing the rectangles on main image after analysis and all TSP methods
 */
public class LeafRenderer {
    private final GraphicsContext gc;
    private final Canvas canvas;
    private final double scaleX;
    private final double scaleY;

    public LeafRenderer(Canvas canvas, double scaleX, double scaleY) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * Draws rectangles on main view after analysis
     * @param clusters - List of all leaves
     * @param showRankNumbers - ranking based on size of leaf
     */
    public void drawRectangles(List<LeafCluster> clusters, boolean showRankNumbers) {
        //clear any already drawn rectangles
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setWidth(canvas.getWidth());
        canvas.setHeight(canvas.getHeight());

        //Rectangle will be blue
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2.0);

        //loop through each leaf and calculate the positioning of each corner of the rectangle.
        for (LeafCluster c : clusters) {
            double x = c.minX * scaleX;
            double y = c.minY * scaleY;
            double w = (c.maxX - c.minX) * scaleX;
            double h = (c.maxY - c.minY) * scaleY;
            gc.strokeRect(x, y, w, h); //draw rectangle
        }

        //show the sequential numbering if user chooses
        if (showRankNumbers) {
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
            for (LeafCluster c : clusters) {
                double x = c.minX * scaleX;
                double y = c.minY * scaleY;
                String label = String.valueOf(c.rank); //uses the rank variable of the leaf
                double textX = x + 3; //position the text in the top right corner
                double textY = y + 13;
                gc.setFill(Color.rgb(0, 0, 0, 0.5));
                gc.fillRect(textX - 1, textY - 11, label.length() * 8 + 2, 14); //have a background around the text
                gc.setFill(Color.YELLOW);
                gc.fillText(label, textX, textY);
            }
        }
    }

    /**
     * Draw rectangles on the TSP view, one rectangle will be a different colour to highlight the current path
     * @param clusters - list of leaves
     * @param highlightIndex - index of the rectangle to change colour to highlight, -1 for no highlight
     * @param highlightColor - the colour to highlight the rectangle (yellow)
     */
    public void drawTSPRectangles(List<LeafCluster> clusters, int highlightIndex, Color highlightColor) {
        gc.setLineWidth(2.0);
        for (int i = 0; i < clusters.size(); i++) {
            LeafCluster c = clusters.get(i);
            double x = c.minX * scaleX;
            double y = c.minY * scaleY;
            double w = (c.maxX - c.minX) * scaleX;
            double h = (c.maxY - c.minY) * scaleY;
            gc.setStroke(i == highlightIndex ? highlightColor : Color.BLUE);
            gc.strokeRect(x, y, w, h);
        }
    }

    /**
     * Draw the red lines between leaves for the TSP animation over 5 seconds, each leaf will be visited
     * sequentially using nearest neighbour until all leaves have been visited and the current leaf will
     * flash yellow.
     * @param clusters - list of all leaves
     * @param path - ordered list of leaves used to find the route, generated using nearest neighbour
     */
    public void animateTSPPath(List<LeafCluster> clusters, List<Integer> path) {
        int n = path.size();
        double delayPerStep = 5000.0 / n;

        for (int step = 0; step < n; step++) {
            final int s = step;
            final int currentIdx = path.get(s);
            final int previousIdx = s > 0 ? path.get(s - 1) : -1;

            //each step of the path uses a different amount of time
            PauseTransition pause = new PauseTransition(Duration.millis(s * delayPerStep));

            //rectangles goes to blue before continuing
            pause.setOnFinished(_ -> {
                drawTSPRectangles(clusters, -1, Color.BLUE);

                if (previousIdx != -1) {
                    gc.setStroke(Color.RED);
                    gc.setLineWidth(2.0);
                    //redraw the previous lines to build up the path
                    for (int i = 1; i <= s; i++) {
                        LeafCluster a = clusters.get(path.get(i - 1)); //cluster your travelling from
                        LeafCluster b = clusters.get(path.get(i)); //cluster your travelling to
                        double ax = ((a.minX + a.maxX) / 2.0) * scaleX;
                        double ay = ((a.minY + a.maxY) / 2.0) * scaleY;
                        double bx = ((b.minX + b.maxX) / 2.0) * scaleX;
                        double by = ((b.minY + b.maxY) / 2.0) * scaleY;
                        gc.strokeLine(ax, ay, bx, by);
                    }
                }

                drawTSPRectangles(clusters, currentIdx, Color.YELLOW); //draw the current rectangle yellow

                //after 300ms the rectangle goes to blue
                PauseTransition flash = new PauseTransition(Duration.millis(300));
                flash.setOnFinished(_ -> drawTSPRectangles(clusters, -1, Color.BLUE));
                flash.play();
            });
            //start animation after the delay
            pause.play();
        }
    }

    /**
     * Calculates the route for the TSP using nearest neighbour algorithm, starts from the users selected
     * leaf and travels to the nearest unvisited leaf.
     * @param clusters - list of all leaves
     * @param startIndex - index of starting leaf
     *
     */
    public List<Integer> nearestNeighbourTSP(List<LeafCluster> clusters, int startIndex) {
        int n = clusters.size();
        boolean[] visited = new boolean[n];
        List<Integer> path = new ArrayList<>();

        //start at the leaf specified by the user
        int current = startIndex;
        visited[current] = true;
        path.add(current);

        //Each remaining cluster can only be visited once
        for (int step = 1; step < n; step++) {
            double bestDist = Double.MAX_VALUE;
            int bestNext = -1;

            // Centre point of current cluster
            double cx = (clusters.get(current).minX + clusters.get(current).maxX) / 2.0;
            double cy = (clusters.get(current).minY + clusters.get(current).maxY) / 2.0;

            //loop through each unvisited leaf, keeping track of closest
            for (int i = 0; i < n; i++) {
                if (visited[i]) continue;
                double nx = (clusters.get(i).minX + clusters.get(i).maxX) / 2.0; //centre coord of next cluster
                double ny = (clusters.get(i).minY + clusters.get(i).maxY) / 2.0;
                double dist = Math.hypot(cx - nx, cy - ny); //use pythagoras theorem to find distance
                if (dist < bestDist) {
                    bestDist = dist;
                    bestNext = i;
                }
            }
            //move to the closest leaf, mark as visited
            visited[bestNext] = true;
            path.add(bestNext);
            current = bestNext;
        }

        return path;
    }


}
