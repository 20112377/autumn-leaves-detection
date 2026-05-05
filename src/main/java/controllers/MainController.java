package controllers;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.*;
import utils.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainController {

    public Label filenameLabel;
    public Label widthLabel;
    public Label heightLabel;
    public Label hueLabel;
    public Label satLabel;
    public Label brightLabel;
    public Button resetButton;

    @FXML
    public Label statusLabel;
    @FXML
    private ImageView imageView;
    private Image originalImage;
    private Image workingImage;
    @FXML
    private Slider hueSlider;
    @FXML
    private Slider saturationSlider;
    @FXML
    private Slider brightnessSlider;
    @FXML
    private StackPane imageContainer;
    @FXML
    private Canvas analysisCanvas;
    @FXML
    private javafx.scene.layout.FlowPane colourSwatchPane;
    @FXML
    private Slider toleranceSlider;
    @FXML
    private Slider minSatSlider;
    @FXML
    private Slider minBrightSlider;
    private UnionFind uf;

    private boolean showRankNumbers = false;
    private final List<Color> targetColours = new ArrayList<>();
    private final List<LeafCluster> leafClusters = new ArrayList<>();
    private int minClusterSize = 50;
    private int maxClusterSize = Integer.MAX_VALUE;
    private List<LeafCluster> displayedClusters = new ArrayList<>();



    @FXML
    public void initialize() {
        handleOriginal();
        //Disable sliders until an image is loaded
        setSlidersDisabled(true);
        analysisCanvas.setMouseTransparent(true);
        analysisCanvas.setOnMouseClicked(this::handleImageClick);


        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageContainer.heightProperty());

        // Add listeners to sliders to apply adjustments AND update status
        ChangeListener<Number> sliderListener = (_, _, _) -> {
            applyAdjustments();
            updateStatus();
        };

        for (Slider slider : List.of(brightnessSlider, hueSlider, saturationSlider, toleranceSlider, minSatSlider, minBrightSlider)) {
            slider.valueProperty().addListener(sliderListener);
        }
    }

    //disables the sliders when no image is loaded
    private void setSlidersDisabled(boolean disabled) {
        brightnessSlider.setDisable(disabled);
        brightLabel.setDisable(disabled);
        resetButton.setDisable(disabled);
        hueSlider.setDisable(disabled);
        hueLabel.setDisable(disabled);
        saturationSlider.setDisable(disabled);
        satLabel.setDisable(disabled);
        toleranceSlider.setDisable(disabled);
        minSatSlider.setDisable(disabled);
        minBrightSlider.setDisable(disabled);

    }

    //gets the scale at which the rectangles will be drawn
    private double[] getScale() {
        double displayWidth = imageView.getBoundsInParent().getWidth();
        double displayHeight = imageView.getBoundsInParent().getHeight();
        return new double[]{
                displayWidth / originalImage.getWidth(),
                displayHeight / originalImage.getHeight()
        };
    }

    //IMAGE LOADING
    @FXML
    private void handleOpenImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.gif")
        );

        File file = fileChooser.showOpenDialog(imageView.getScene().getWindow());
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            filenameLabel.setText(file.getName());

            // Add tooltip so full filename shows on hover
            Tooltip tooltip = new Tooltip(file.getName());
            filenameLabel.setTooltip(tooltip);

            widthLabel.setText(String.valueOf(image.getWidth()));
            heightLabel.setText(String.valueOf(image.getHeight()));
            originalImage = image;
            workingImage = image;
            imageView.setImage(image);
            resetSliders();
            setSlidersDisabled(false);
        }
    }
    // reset the sliders to the default value
    private void resetSliders() {
        brightnessSlider.setValue(1);
        hueSlider.setValue(1);
        saturationSlider.setValue(1);
        minSatSlider.setValue(0.15);
        toleranceSlider.setValue(15);
        minBrightSlider.setValue(0.30);

    }

    //IMAGE DISPLAY
    @FXML
    private void handleOriginal() {
        //doesnt reset image just changes view
        if (originalImage != null) {
            imageView.setImage(originalImage);
            workingImage = originalImage;
            statusLabel.setText("Showing original image.");
        }
    }
    @FXML
    private void handleAdjustments() {
        //adjusts the image based on slider values
        if (originalImage == null) return;


        Image adjusted = ImageProcessor.adjustImage(
                originalImage,
                hueSlider.getValue(),
                saturationSlider.getValue(),
                brightnessSlider.getValue()
        );

        imageView.setImage(adjusted);
    }
    private void applyAdjustments() {
        //apply the slider adjustments to the working image
        if (originalImage == null) return;
        workingImage = ImageProcessor.adjustImage(
                originalImage,
                hueSlider.getValue(),
                saturationSlider.getValue(),
                brightnessSlider.getValue()
        );

        imageView.setImage(workingImage);
    }
    private void updateStatus() {
        //updates the status bar with info from the current image, and slider info
        if (filenameLabel.getText() == null || filenameLabel.getText().isEmpty()) {
            statusLabel.setText("No image loaded");
            return;
        }

        String statusText = String.format(
                "Image: %s | Hue: %.0f | Saturation: %.2f | Brightness: %.2f",
                filenameLabel.getText(),
                hueSlider.getValue(),
                saturationSlider.getValue(),
                brightnessSlider.getValue()
        );

        statusLabel.setText(statusText);
    }
    //RESET-EXIT
    @FXML
    private void handleReset() {
        //clears all info from analysis and adjustments
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset");
        alert.setHeaderText("Are you sure you want to reset?");
        alert.setContentText("This will clear all current changes!");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                //clear everything
                if (originalImage != null) {
                    imageView.setImage(originalImage);
                    workingImage = originalImage;
                }
                analysisCanvas.getGraphicsContext2D().clearRect(0, 0, analysisCanvas.getWidth(), analysisCanvas.getHeight());
                uf = null;
                leafClusters.clear();
                targetColours.clear();
                showRankNumbers = false;
                analysisCanvas.setMouseTransparent(true);
                analysisCanvas.setOnMouseMoved(null);
                resetSliders();
                colourSwatchPane.getChildren().clear();
                statusLabel.setText(originalImage == null ? "No image loaded." : "Image Reset.");
            }
        });
    }
    @FXML
    private void handleExit() {
        //Allow user to exit program using exit button
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    //COLOUR PICKING
    @FXML
    public void handleImageClick(MouseEvent mouseEvent) {
        if (originalImage == null) return;

        if (uf == null) {
            // No analysis done yet — always pick colours
            processColorPick(mouseEvent);
        } else {
            // Analysis done — colour the disjoint set
            applyRandomColourToDisjointClicked(mouseEvent);
        }
    }

    private void processColorPick(MouseEvent mouseEvent) {
        double mouseX = mouseEvent.getX();
        double mouseY = mouseEvent.getY();

        double viewWidth = imageView.getBoundsInParent().getWidth();
        double viewHeight = imageView.getBoundsInParent().getHeight();

        // Calculate the ratio
        int pixelX = (int) (mouseX * originalImage.getWidth() / viewWidth);
        int pixelY = (int) (mouseY * originalImage.getHeight() / viewHeight);

        if (pixelX >= 0 && pixelX < originalImage.getWidth() && pixelY >= 0 && pixelY < originalImage.getHeight()) {
            PixelReader reader = workingImage.getPixelReader();
            Color clickedColour = reader.getColor(pixelX, pixelY);

            System.out.println("Leaf Colour Picked: " + clickedColour.toString());


            updateTargetColour(clickedColour);
            statusLabel.setText("Color Added. Ready to Analyze.");
        }
    }

    private void updateTargetColour(Color clickedColour) {
        if (!targetColours.contains(clickedColour)) {
            targetColours.add(clickedColour);

            javafx.scene.shape.Rectangle swatch = new javafx.scene.shape.Rectangle(20, 20);
            swatch.setFill(clickedColour);
            swatch.setStroke(Color.GRAY);
            swatch.setStrokeWidth(1);

            Tooltip.install(swatch, new Tooltip("Click to remove"));

            swatch.setOnMouseClicked(_ -> {
                targetColours.remove(clickedColour);
                colourSwatchPane.getChildren().remove(swatch);
                statusLabel.setText("Colour removed.");
            });

            swatch.setOnMouseEntered(_ -> swatch.setStroke(Color.RED));
            swatch.setOnMouseExited(_ -> swatch.setStroke(Color.GRAY));

            colourSwatchPane.getChildren().add(swatch);
        } else {
            statusLabel.setText("Colour already selected!");
        }
    }

    //ANALYSIS

    /**
     * Converts the image to black and white, white being the colour selected from the original image
     */
    public void handleShowBW() {
        if (originalImage == null) {
            statusLabel.setText("No image loaded");
            return;
        }
        double tolerance = toleranceSlider.getValue();
        double minSat = minSatSlider.getValue();
        double minBright = minBrightSlider.getValue();
        Image adjusted = ImageProcessor.adjustImage(originalImage, hueSlider.getValue(), saturationSlider.getValue(), brightnessSlider.getValue());
        WritableImage bwImage = ImageProcessor.processBlackWhiteConversion(adjusted, targetColours, tolerance, minSat, minBright);
        imageView.setImage(bwImage);
    }

    /**
     * Analyses the image using union-find to identify and find leaf clusters
     */
    public void handleAnalyseLeaves() {
        uf = null;
        //use the values of the javafx slider to reduce noise and increase accuracy
        double tolerance = toleranceSlider.getValue();
        double minSat = minSatSlider.getValue();
        double minBright = minBrightSlider.getValue();
        //use the image adjusted by the sliders to convert to black and white
        Image adjusted = ImageProcessor.adjustImage(originalImage, hueSlider.getValue(), saturationSlider.getValue(), brightnessSlider.getValue());
        WritableImage bwImage = ImageProcessor.processBlackWhiteConversion(adjusted, targetColours, tolerance, minSat, minBright);
        //find the size of the image
        int width = (int) bwImage.getWidth();
        int height = (int) bwImage.getHeight();
        PixelReader pr = bwImage.getPixelReader();

        //makes a new union find with the size of the image
        uf = new UnionFind(width * height);

        //loops through all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
              //if the current pixel is white it is part of a leaf
                if (pr.getColor(x, y).equals(Color.WHITE)) {
                    int current = y * width + x; //get the coords of the leaf
                    uf.activate(current); //activate the pixel and set its root

                   //look at the pixels on the right only if the pixel is not the right most pixel
                    if (x < width - 1 && pr.getColor(x + 1, y).equals(Color.WHITE)) {
                        int right = y * width + (x + 1); //if it is a match note coords
                        uf.activate(right); //if the right pixel is white activate it
                        uf.union(current, right); //union the found pixel to the original pixel, original becomes root
                    }
                    //look at the pixels below only if the pixel is not the bottom most pixel
                    if (y < height - 1 && pr.getColor(x, y + 1).equals(Color.WHITE)) {
                        int down = (y + 1) * width + x;
                        uf.activate(down);
                        uf.union(current, down);
                    }
                }
            }
        }
      // create an array the size of the image, root can appear at any pixel
        int[] minX = new int[width * height];
        int[] maxX = new int[width * height];
        int[] minY = new int[width * height];
        int[] maxY = new int[width * height];


        //loop through each pixel
        for (int i = 0; i < width * height; i++) {
            minX[i] = width;
            minY[i] = height; //the minimum is set to the max value, all roots will be smaller
            maxX[i] = -1;
            maxY[i] = -1; //the maximum is set to -1 as all root will be bigger
        }

        //Loops through each pixel finding the roots
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int root = uf.find(y * width + x); //find the root of the current pixel
                if (root != -1 && uf.getSize()[root] > 50) { // Ignore black pixels tiny clusters of white pixels
                    if (x < minX[root]) minX[root] = x; //if the current coord is smaller than the min then it becomes the min
                    if (x > maxX[root]) maxX[root] = x; //if the current coord is bigger than the max then it becomes the max
                    if (y < minY[root]) minY[root] = y;
                    if (y > maxY[root]) maxY[root] = y;
                }
            }
        }
        //set the image back to the original image
        imageView.setImage(originalImage);
        analysisCanvas.setMouseTransparent(true);

        //clear currently stored leaves
        leafClusters.clear();
        for (int i = 0; i < width * height; i++) {
            //if the current pixel is a root and satisfies the size requirements
            if (uf.getParent()[i] == i && maxX[i] != -1 && uf.getSize()[i] > 50) {
                //add the info to leafClusters list
                leafClusters.add(new LeafCluster(i, minX[i], maxX[i], minY[i], maxY[i], uf.getSize()[i]));
            }
        }

        //sort the list by size
        MergeSort.sort(leafClusters, (a, b) -> b.pixelCount - a.pixelCount);
        for (int i = 0; i < leafClusters.size(); i++) {
            leafClusters.get(i).rank = i + 1; //rank the leaves by size
        }
        imageView.setImage(originalImage);
        double[] scale = getScale();
        double displayWidth = imageView.getBoundsInParent().getWidth();
        double displayHeight = imageView.getBoundsInParent().getHeight();
        analysisCanvas.setWidth(displayWidth);
        analysisCanvas.setHeight(displayHeight);
        LeafRenderer renderer = new LeafRenderer(analysisCanvas, scale[0], scale[1]);
        renderer.drawRectangles(leafClusters, showRankNumbers);
        displayedClusters = new ArrayList<>(leafClusters);

        // Enable hover
        analysisCanvas.setMouseTransparent(false);
        analysisCanvas.setOnMouseMoved(this::handleCanvasHover);

        //the total amount of leaves is equal to the size of the list
        int totalClusters = leafClusters.size();
        statusLabel.setText("Found " + totalClusters + " potential leaves!"); //add a label informing user
    }

    //INTERACTIONS
    private void handleCanvasHover(MouseEvent e) {
        double displayWidth = imageView.getBoundsInParent().getWidth();
        double displayHeight = imageView.getBoundsInParent().getHeight();
        double scaleX = displayWidth / originalImage.getWidth();
        double scaleY = displayHeight / originalImage.getHeight();

        for (LeafCluster c : displayedClusters) {
            if (isMouseOverCluster(c, e.getX(), e.getY(), scaleX, scaleY)) {
                statusLabel.setText(String.format(
                        "Leaf/Cluster Number: %d | Estimated size (pixels): %d", // current cluster info
                        c.rank, c.pixelCount
                ));
                return;
            }
        }
        //If the mouse is not over any leaf, show how many leaves found
        statusLabel.setText("Found " + displayedClusters.size() + " potential leaves!");
    }
    @FXML
    private void handleToggleNumbers() {
        //toggle the sequential numbering of leaves
        showRankNumbers = !showRankNumbers;
        if (!leafClusters.isEmpty()) {
            double[] scale = getScale();
            LeafRenderer renderer = new LeafRenderer(analysisCanvas, scale[0], scale[1]);
            renderer.drawRectangles(displayedClusters, showRankNumbers);
        }
    }
    @FXML
    private void applyRandomColourToDisjointClicked(MouseEvent mouseEvent) {
       //if the analysis hasn't happened don't colour anything
        if (originalImage == null || uf == null) {
            statusLabel.setText("Please analyze the image first!");
            return;
        }

        double tolerance = toleranceSlider.getValue();
        double minSat = minSatSlider.getValue();
        double minBright = minBrightSlider.getValue();
        WritableImage bwImage = ImageProcessor.processBlackWhiteConversion(originalImage, targetColours, tolerance, minSat, minBright);
        PixelWriter pw = bwImage.getPixelWriter();

        //get the bounds of the image and mouse coords
        double displayWidth = imageView.getBoundsInParent().getWidth();
        double displayHeight = imageView.getBoundsInParent().getHeight();
        int imgW = (int) originalImage.getWidth();
        int imgH = (int) originalImage.getHeight();

        int pixelX = (int) (mouseEvent.getX() * imgW / displayWidth);
        int pixelY = (int) (mouseEvent.getY() * imgH / displayHeight);


        if (pixelX >= 0 && pixelX < imgW && pixelY >= 0 && pixelY < imgH) {

            int indexClick = pixelY * imgW + pixelX;

            //find the root of the clicked cluster
            int root = uf.find(indexClick);

            //if it is a valid root colour that cluster
            if (root != -1 && uf.getSize()[root] > 50) {
                System.out.println("Clicked leaf root: " + root);

                //get a random colour to be used
                Color randomColor = Color.color(Math.random(), Math.random(), Math.random());

                //loop through each pixel in the set colouring each the random colour
                for (int j = 0; j < uf.getParent().length; j++) {
                    if (uf.find(j) == root) {
                        int x = j % imgW;
                        int y = j / imgW;
                        pw.setColor(x, y, randomColor);
                    }
                }
                //change view to the b/w image with the random colour disjoint set
                imageView.setImage(bwImage);
            }
        }
    }
    @FXML
    private void handleRandomColourAllSets() {
        if (originalImage == null || uf == null) {
            statusLabel.setText("Please analyse the image first!");
            return;
        }
        double tolerance = toleranceSlider.getValue();
        double minSat = minSatSlider.getValue();
        double minBright = minBrightSlider.getValue();
        WritableImage bwImage = ImageProcessor.processBlackWhiteConversion(originalImage, targetColours, tolerance, minSat, minBright);
        PixelWriter pw = bwImage.getPixelWriter();

        int imgW = (int) originalImage.getWidth();


        // Assign a random colour to each valid root
        HashMap<Integer, Color> rootColours = new HashMap<>();
        for (LeafCluster c : displayedClusters) {
            rootColours.put(c.root, Color.color(Math.random(), Math.random(), Math.random()));
        }

        // Paint every pixel with its cluster's colour
        for (int j = 0; j < uf.getParent().length; j++) {
            int root = uf.find(j);
            if (root != -1 && rootColours.containsKey(root)) {
                int x = j % imgW;
                int y = j / imgW;
                pw.setColor(x, y, rootColours.get(root));
            }
        }

        imageView.setImage(bwImage);
        statusLabel.setText("Randomly coloured " + displayedClusters.size() + " clusters!");
    }

    //TSP
    @FXML
    private void handleTSP() {
        if (displayedClusters.isEmpty()) {
            statusLabel.setText("Please analyse the image first!");
            return;
        }

        // Build a new window
        Stage tspStage = new Stage();
        tspStage.setTitle("TSP Leaf Path");

        Canvas tspCanvas = new Canvas();
        ImageView tspView = new ImageView(originalImage);
        tspView.setPreserveRatio(true);
        tspView.setFitWidth(800);
        tspView.setFitHeight(600);

        StackPane pane = new StackPane(tspView, tspCanvas);
        Scene scene = new Scene(pane, 800, 600);
        tspStage.setScene(scene);
        tspStage.show();


        tspCanvas.setWidth(tspView.getBoundsInParent().getWidth());
        tspCanvas.setHeight(tspView.getBoundsInParent().getHeight());

        double displayWidth = tspView.getBoundsInParent().getWidth();
        double displayHeight = tspView.getBoundsInParent().getHeight();
        double scaleX = displayWidth / originalImage.getWidth();
        double scaleY = displayHeight / originalImage.getHeight();

        // Draw all rectangles in blue first
        final LeafRenderer tspRenderer = new LeafRenderer(tspCanvas, scaleX, scaleY);
        tspRenderer.drawTSPRectangles(displayedClusters, -1, Color.BLUE);

        Label hint = new Label("Click a leaf cluster to start the path");
        hint.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-padding: 5;");
        pane.getChildren().add(hint);

        // Wait for user to click a starting cluster
        tspCanvas.setOnMouseClicked(e -> {
            // Find which cluster was clicked
            int startIndex = -1;
            for (int i = 0; i < displayedClusters.size(); i++) {
                LeafCluster c = displayedClusters.get(i);
                if (isMouseOverCluster(c, e.getX(), e.getY(), scaleX, scaleY)) {
                    startIndex = i;
                    break;
                }
            }

            if (startIndex == -1) return;
            pane.getChildren().remove(hint);
            tspCanvas.setOnMouseClicked(null); // disable further clicks

            // Run nearest neighbour TSP
            List<Integer> path = tspRenderer.nearestNeighbourTSP(displayedClusters, startIndex);
            tspRenderer.animateTSPPath(displayedClusters, path);
        });
    }

    //NOISE FILTERING
    @FXML
    private void handleNoiseSettings() {
        //popup allowing users to change the max cluster size, with recommended values calculated using IQR
        javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Noise Reduction Settings");
        dialog.setHeaderText("Set min/max cluster size (pixels)");

        javafx.scene.control.TextField minField = new javafx.scene.control.TextField(String.valueOf(minClusterSize));
        javafx.scene.control.TextField maxField = new javafx.scene.control.TextField(
                maxClusterSize == Integer.MAX_VALUE ? "" : String.valueOf(maxClusterSize) //if max is the Maximum integer dont show any value
        );

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Min cluster size:"), 0, 0);
        grid.add(minField, 1, 0);
        grid.add(new Label("Max cluster size:"), 0, 1);
        grid.add(maxField, 1, 1);

        if (!leafClusters.isEmpty()) { //if an analysis was done, calculate and display suggested range
            int[] bounds = calculateIQRBounds(leafClusters);
            grid.add(new Label("IQR suggested range:"), 0, 2);
            grid.add(new Label(bounds[0] + " – " + bounds[1] + " pixels"), 1, 2);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try { //reads values inputted by user
                    minClusterSize = Integer.parseInt(minField.getText().trim());
                    maxClusterSize = maxField.getText().trim().isEmpty()
                            ? Integer.MAX_VALUE
                            : Integer.parseInt(maxField.getText().trim());
                    if (!leafClusters.isEmpty()) redrawWithFilter();
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Invalid input — please enter whole numbers.");
                }
            }
        });
    }

    private int[] calculateIQRBounds(List<LeafCluster> clusters) {
        //calculates the min/max cluster size using Inter Quartile Range
        if (clusters.isEmpty()) return new int[]{0, Integer.MAX_VALUE};

        List<Integer> sizes = new ArrayList<>();
        for (LeafCluster c : clusters) sizes.add(c.pixelCount);
        MergeSort.sort(sizes, Integer::compareTo); //sorts the size from smallest to biggest to perform calculations


        int n = sizes.size();
        int q1 = sizes.get(n / 4); //1st quartile
        int q3 = sizes.get((3 * n) / 4); //3rd quartile
        int iqr = q3 - q1;

        int lower = (int) (q1 - 1.5 * iqr);
        int upper = (int) (q3 + 1.5 * iqr);

        lower = Math.max(lower, minClusterSize);
        return new int[]{lower, upper};
    }

    private List<LeafCluster> getFilteredClusters() {
        //filter clusters based on user defined min and max size
        List<LeafCluster> filtered = new ArrayList<>();
        for (LeafCluster c : leafClusters) {
            if (c.pixelCount >= minClusterSize && c.pixelCount <= maxClusterSize) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    private void redrawWithFilter() {
        //redraws the canvas applying the filters
        displayedClusters = getFilteredClusters();
        double[] scale = getScale();
        LeafRenderer renderer = new LeafRenderer(analysisCanvas, scale[0], scale[1]);
        renderer.drawRectangles(displayedClusters, showRankNumbers);
        statusLabel.setText("Showing " + displayedClusters.size() + " clusters after noise filter " +
                "(min:" + minClusterSize + " max:" + (maxClusterSize == Integer.MAX_VALUE ? "∞" : maxClusterSize) + ")");
    }

    //HELPERS
    private boolean isMouseOverCluster(LeafCluster c, double mouseX, double mouseY, double scaleX, double scaleY) {
        double x = c.minX * scaleX;
        double y = c.minY * scaleY;
        double w = (c.maxX - c.minX) * scaleX;
        double h = (c.maxY - c.minY) * scaleY;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

}



