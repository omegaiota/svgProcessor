package src.jackiealgorithmicquilting;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static src.jackiealgorithmicquilting.Main.SkeletonPathType.*;
import static src.jackiealgorithmicquilting.Main.SkeletonRenderType.*;

public class Main extends Application {
    /* Constant */
    private static final int columnItemSpacing = 15, COL_GROUP_SPACING = 3;
    public static int seedNum = 0;
    /* Labels */
    private final Label textFieldLabel = new Label(), patternRenderFieldLabel = new Label("Repetitions"),
            skeletonGenParam1Label = new Label("Rows"), regionLabel = new Label("Region"),
            regionSelectionLabel = new Label("Select Region"), skeletonLabel = new Label("Skeleton Path"),
            skeletonGnerationlabel = new Label("Point Distribution"), patternLabel = new Label("Decorative Element"),
            patternSelectionLabel = new Label("Select Pattern"), skeletonRenderinglabel = new Label("Skeleton Path Rendering"),
            patternRenderLabel = new Label("Pattern Rendering"), toolLabel = new Label("Tools"),
            svgToPatLabel = new Label(".SVG to .PAT"), quiltingPatternGeneration = new Label("Quilting Pattern Generation"),
            skeletonRenderFieldLabel1 = new Label("Decoration Size"),
            skeletonRenderFieldLabel2 = new Label(""),
            skeletonRenderFieldLabel4 = new Label(""),
            skeletonRenderFieldLabel3 = new Label("");

    /* TextField */
    private TextField patternRenderTextFiled = new TextField(),
            skeletonGenTextField = new TextField(),
            skeletonRenderTextField1 = new TextField(),
            skeletonRenderTextField2 = new TextField(),
            skeletonRenderTextField3 = new TextField(),
            skeletonRenderTextField4 = new TextField();
    private List<Label> skeletonRenderParamLabels = Arrays.asList(skeletonRenderFieldLabel1,skeletonRenderFieldLabel2, skeletonRenderFieldLabel3, skeletonRenderFieldLabel4);
    private Map<Label, TextField> skeletonRenderLabelTexfieldMap = new HashMap<Label, TextField>() {
        {
            put(skeletonRenderFieldLabel1, skeletonRenderTextField1);
            put(skeletonRenderFieldLabel2, skeletonRenderTextField2);
            put(skeletonRenderFieldLabel3, skeletonRenderTextField3);
            put(skeletonRenderFieldLabel4, skeletonRenderTextField4);
        }
    };
    /* Toggle Group */
    private final ToggleGroup patternSourceGroup = new ToggleGroup();
    /* Buttons */
    private final Button loadRegionButton = new Button("Load region ..."), loadCollisionGeoButton = new Button("Load collision file ...");
    private final Button loadDecoElementButton = new Button("Load a pattern file ...");
    private final Button loadSvgFileButton = new Button("Load SVG file");
    private final Button generateButton = new Button("Generate");
    private final Button loadConcrdePoints = new Button("Load Concorde Output");
    List<String> tileList = new ArrayList<>(), alongPathList = new ArrayList<>(), endpointList = new ArrayList<>();
    private PatternRenderer patternRenderer;
    /* Layout: VBox, HBox*/
    //Column
    private VBox regionColumn = new VBox(columnItemSpacing), skeletonColumn = new VBox(columnItemSpacing),
            patternColumn = new VBox(columnItemSpacing), toolColumn = new VBox(columnItemSpacing);
    //Label + item
    private VBox regionSelection = new VBox(COL_GROUP_SPACING), skeletonGeneration = new VBox(COL_GROUP_SPACING),
            skeletonRendering = new VBox(COL_GROUP_SPACING), patternRendering = new VBox(COL_GROUP_SPACING),
            patternPropertyInput = new VBox(COL_GROUP_SPACING), svgToPat = new VBox(COL_GROUP_SPACING),
            skeletonGenPropertyInput = new VBox(COL_GROUP_SPACING), skeletonRenderPropertyInput = new VBox(COL_GROUP_SPACING),
            patternSelection = new VBox(COL_GROUP_SPACING);
    private VBox layouts = new VBox(5);
    private HBox menu = new HBox(15), patternSourceBox = new HBox(2), fileSourceBox = new HBox(2);
    private Pane visualizationPane = new StackPane();
    //ComboBox
    private ComboBox skeletonGenComboBox = new ComboBox(), skeletonRenderComboBox = new ComboBox(),
            patternLibraryComboBox = new ComboBox(), patternRenderComboBox = new ComboBox();
    /* Fonts */
    private final Font COL_LABEL_FONT = new Font("Luminari", 22),
            FUNC_LABEL_FONT = new Font("Avenir Light", 14),
            BUTTON_FONT = new Font("Avenir", 10),
            TITLE_FONT = new Font("Luminari", 40);
    private Color labelColor = Color.ORANGE, columnLabelColor = Color.SILVER;
    private ToggleButton patternFromFile = new ToggleButton("from file"), noPattern = new ToggleButton("none"),
            patternFromLibrary = new ToggleButton("from library");
    /* Folder */
    private final File TILE_LIB_PATH = new File("./src/resources/patterns/tiles/"),
            ALONG_LIB_PATH = new File("./src/resources/patterns/alongPath/"),
            ENDPOINTS_LIB_PATH = new File("./src/resources/patterns/endpoints/");

    /* File processor, renderer */
    private SVGElement decoElementFile = null;
    private GenerationInfo info = new GenerationInfo();

    private PatternRenderer skeletonrenderer = null;
    private List<SvgPathCommand> collisionCommands = new ArrayList<>();
    private SVGElement  renderedDecoElemFileProcessor = null, collisionFile = null;
    private List<SVGElement> decoElements = new ArrayList<>();
    private List<SVGElement> decoElementsCollision = new ArrayList<>();
    private List<SvgPathCommand> renderedDecoCommands = new ArrayList<>(),
            skeletonCopy = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Quilt Pattern Generation");
        stage.setScene(new Scene(setLayoutWithGraph(stage), Color.rgb(35, 39, 50)));
        stage.show();
        setDefaultValue();
    }

    private void loadWandererDefault() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
//        String patternName = "wanderer-dizzy";
//        String patternName = "wanderer-mixed";
        String patternName = "wanderer-whistle";
        String folder = "/Users/JacquelineLi/IdeaProjects/svgProcessor/src/resources/patterns/endpoints/set/";
        File dir = new File(folder);
        File[] collisions = dir.listFiles((dir1, name) -> name.contains(patternName) && name.contains("collision") && name.contains("set"));
        File[] patternFiles = dir.listFiles((dir1, name) -> name.contains(patternName) && name.contains("set") && !name.contains("collision"));
//        assert (collisions.length == patternFiles.length);

        if (collisions.length == 1) {
            collisionFile = new SVGElement(collisions[0]);
            collisionFile.processSvg();
        } else {
            System.out.println("collision file not found");
        }

        decoElements.clear();
        decoElementsCollision.clear();
        for (File pattern : patternFiles) {
            decoElements.add(new SVGElement(pattern));
        }


        for (File pattern : collisions) {
            decoElementsCollision.add(new SVGElement(pattern));
        }

        for (SVGElement f : decoElements) {
            f.processSvg();
            if (f.getCommandList().get(0).getDestinationPoint().x > f.getCommandList().get(f.getCommandList().size() - 1).getDestinationPoint().x)
                f.setCommandLists(SvgPathCommand.reverseCommands(f.getCommandList()));
        }

        for (SVGElement f : decoElementsCollision) {
            f.processSvg();

        }

    }

    private void setDefaultValue() {
        String patternName = "wanderer-whistle";
//        String patternName = "wanderer-flower2";
        String regionName = "16in-star"; // darkWhole-pebbleRegion
        File file = new File(String.format("/Users/JacquelineLi/IdeaProjects/svgProcessor/src/resources/Region/%s.svg", regionName));
        File collision = new File(String.format("/Users/JacquelineLi/IdeaProjects/svgProcessor/src/resources/patterns/endpoints/%s-collision.svg", patternName));
        File deco = new File(String.format("/Users/JacquelineLi/IdeaProjects/svgProcessor/src/resources/patterns/endpoints/%s.svg", patternName));
        patternLibraryComboBox.setValue(patternName);
        if (file != null) {
            info.regionFile = new SVGElement(file);
            collisionFile = new SVGElement(collision);
            decoElementFile = new SVGElement(deco);
            try {
                /** Process the svg file */
                try {
                    info.regionFile.processSvg();
                    collisionFile.processSvg();
                    decoElementFile.processSvg();
                    info.decoElementFile = decoElementFile;
                    info.collisionFile = collisionFile;
                    SVGElement.outputSvgCommands(info.regionFile.getCommandList(),
                            "region-" + info.regionFile.getfFileName(), info);
                } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
//
//        String patternName = "wanderer-dizzy";
////        String patternName = "wanderer-flower2";
//        String regionName = "16in-star"; // darkWhole-pebbleRegion
        skeletonRenderTextField2.setText("8"); // Deco size
        skeletonRenderTextField3.setText("2.0"); // Gap Length
        skeletonGenComboBox.setValue(POISSON_DISK);

        skeletonRenderTextField4.setText(String.valueOf("90")); // deco density
//        skeletonGenComboBox.setValue(SNAKE);
        skeletonGenTextField.setText("20");
        skeletonGenTextField.setText("70"); // tessellation density
        skeletonRenderComboBox.setValue(WANDERER);
        patternRenderComboBox.setValue("No Rendering");

        /*
        String patternName = "wanderer-dizzy";
        String patternName = "wanderer-flower2";
        String regionName = "16in-star"; // darkWhole-pebbleRegion
        skeletonRenderTextField2.setText("6.5"); // Deco size
        skeletonRenderTextField3.setText("2.0"); // Gap Length
        skeletonRenderTextField4.setText(String.valueOf("90")); // deco density
        skeletonGenComboBox.setValue(POISSON_DISK);
        skeletonGenTextField.setText("70"); // tessellation density
        skeletonRenderComboBox.setValue(WANDERER);
        patternRenderComboBox.setValue("No Rendering");
        * */
        /*
        wanderer-whistle on large quilt
                skeletonRenderTextField2.setText("5.5"); // Deco size
        skeletonRenderTextField3.setText("1.0"); // Gap Length
        skeletonRenderTextField4.setText(String.valueOf("100")); // deco density
        skeletonGenComboBox.setValue(HEXAGON_TESS);
        skeletonGenTextField.setText("80");
        skeletonRenderComboBox.setValue(WANDERER);
        patternRenderComboBox.setValue("No Rendering");

        * */
        patternFromLibrary.setSelected(true);
    }

    public BorderPane setLayoutWithGraph(Stage stage) {
        BorderPane layout = new BorderPane();
        setUpFont();

        //Region
        /* Region Selection*/
        regionSelection.getChildren().addAll(regionSelectionLabel, loadRegionButton);
        regionColumn.getChildren().addAll(regionLabel, regionSelection);

        //Skeleton
        /* Skeleton Path Generation */
        skeletonGenComboBox.getItems().addAll(SkeletonPathType.values());
        skeletonGeneration.getChildren().addAll(skeletonGnerationlabel, skeletonGenComboBox);

        /* Skeleton Path Rendering */
        skeletonRenderComboBox.getItems().addAll(SkeletonRenderType.values());
        skeletonRendering.getChildren().addAll(skeletonRenderinglabel, skeletonRenderComboBox);

        skeletonColumn.getChildren().addAll(skeletonLabel, skeletonGeneration, skeletonGenPropertyInput, skeletonRendering, skeletonRenderPropertyInput);

        //Pattern Column

        /* Pattern selection*/

        /*  Toggle group*/
        patternSourceGroup.getToggles().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSourceBox.getChildren().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSelection.getChildren().addAll(patternSelectionLabel, patternSourceBox, fileSourceBox);

        /* initialize pattern library */
        addLibraryFilesToList(TILE_LIB_PATH, tileList);
        addLibraryFilesToList(ALONG_LIB_PATH, alongPathList);
        addLibraryFilesToList(ENDPOINTS_LIB_PATH, endpointList);

        /* Pattern rendering */
        patternRenderComboBox.getItems().addAll("Repeat with Rotation", "Echo", "No Rendering");


//        patternRendering.getChildren().setAll(patternRenderLabel, patternRenderComboBox);
        patternColumn.getChildren().addAll(patternLabel, patternSelection, patternRendering, patternPropertyInput);


        //Tool Column
        //svgToPat
        svgToPat.getChildren().addAll(svgToPatLabel, loadSvgFileButton, loadConcrdePoints);
        toolColumn.getChildren().addAll(toolLabel, svgToPat);
//        toolColumn.setPrefWidth(40);
//        skeletonColumn.setPrefWidth(40);

        menu.getChildren().addAll(regionColumn, skeletonColumn, patternColumn, toolColumn);

        layouts.getChildren().addAll(menu);

        layout.setCenter(layouts);
        layout.setBottom(generateButton);
        layout.setTop(quiltingPatternGeneration);

        layout.setPadding(new Insets(30, 5, 30, 5));
//        layout.setPrefWidth(2000);
        layout.setPrefHeight(1000);
        BorderPane.setAlignment(generateButton, Pos.BOTTOM_CENTER);
        BorderPane.setAlignment(quiltingPatternGeneration, Pos.TOP_CENTER);
        BorderPane.setMargin(generateButton, new Insets(10, 8, 8, 8));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        //Listeners
        setupListeners();

        //Buttons, File loader
        buttonActions(stage);
        return layout;
    }

    private void addLibraryFilesToList(File folderFile, List<String> fileList) {
        for (File tileFile : folderFile.listFiles()) {
            String fileName = tileFile.getName();
            if (!fileName.contains(".svg"))
                continue;
            fileName = fileName.substring(0, fileName.length() - ".svg".length()); // get rid of .svg
            fileList.add(fileName);
        }

        fileList.sort(String::compareTo);
    }


    private void buttonActions(Stage stage) {
        loadSvgFileButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a skeleton path ....");
                SVGElement pointFile = new SVGElement(file);
                try {
                    /** Process the svg file */
                    try {
                        pointFile.processSvg();
                        SVGElement.outputPat(pointFile.getCommandList(), pointFile.getfFileName());
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        loadConcrdePoints.setOnAction((ActionEvent e) -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            List<Point> points = new ArrayList<>();
            if (file != null) {
                System.out.println("Loading a skeleton path ....");
                SVGElement pointFile = new SVGElement(file);
                try {
                    /** Process the svg file */
                    try {
                        points = pointFile.processConcordePoints();
                        SVGElement.outputPat(pointFile.getCommandList(), pointFile.getfFileName());
                    } catch (ParserConfigurationException | SAXException e1) {
                        e1.printStackTrace();
                    } catch (XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


            file = fileChooser.showOpenDialog(stage);
            List<SvgPathCommand> commands;
            if (file != null) {
                System.out.println("Loading a skeleton path ....");
                SVGElement orderFile = new SVGElement(file);
                try {
                    /** Process the svg file */
                    try {
                        seedNum = (int) (Math.random() * 10000);
                        commands = orderFile.processCommand(points);
                        toWandererVersion3(commands);
                        Visualizer.addCommandsPlot(info.skeletonPathCommands, visualizationPane);
                        Stage visualizationStage = new Stage();
                        ScrollPane newPane = new ScrollPane();
                        newPane.setContent(Visualizer.addCommandsPlot(info.skeletonPathCommands));
                        newPane.setPrefWidth(visualizationPane.getPrefWidth());
                        newPane.setPrefHeight(visualizationPane.getPrefHeight());
                        visualizationStage.setScene(new Scene(newPane, Color.rgb(35, 39, 50)));
                        visualizationStage.setHeight(visualizationPane.getPrefHeight());
                        visualizationStage.setWidth(visualizationPane.getPrefWidth());
                        visualizationStage.show();

                        SVGElement.outputSvgCommands(info.skeletonPathCommands, "Added Skeleton path", info);
                        SVGElement.outputPoints(points, info);
                        SVGElement.outputSvgCommandsAndPoints(info.skeletonPathCommands, points, "pointsAndCommand", info);

                    } catch (ParserConfigurationException | SAXException e1) {
                        e1.printStackTrace();
                    } catch (XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


        });

        loadRegionButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a region ....");
                info.regionFile = new SVGElement(file);
                try {
                    /** Process the svg file */
                    try {
                        info.regionFile.processSvg();
                        SVGElement.outputSvgCommands(info.regionFile.getCommandList(),
                                "", info);
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        loadDecoElementButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a pattern....");
                try {
                    /** Process the svg file */
                    try {
                        decoElementFile.processSvg();
                        SVGElement.outputSvgCommands(decoElementFile.getCommandList(),
                                "decoElem-" + decoElementFile.getfFileName(), info);
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        loadCollisionGeoButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a pattern....");
                collisionFile = new SVGElement(file);
                try {
                    /** Process the svg file */
                    try {
                        collisionFile.processSvg();
                        SVGElement.outputSvgCommands(collisionFile.getCommandList(),
                                "collision-" + collisionFile.getfFileName(), info);
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        generateButton.setOnAction((ActionEvent e) -> {
            seedNum = (int) (Math.random() * 100000);

            /* Pattern Selection */
            List<SvgPathCommand> decoCommands = new ArrayList<>();
            switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                case "from file": case "from library":
                    decoCommands = decoElementFile.getCommandList();
                    break;
                default:
                    break;
            }


            /* Pattern rendering */

            renderedDecoCommands.clear();
            renderedDecoElemFileProcessor = null;
            info.skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
            info.decoElementFile = decoElementFile;


            String patternRenderMethod = patternRenderComboBox.getValue() == null ? "No Rendering" : patternRenderComboBox.getValue().toString();

            switch (patternRenderMethod) {
                case "No Rendering":
                    renderedDecoCommands = decoCommands;
                    renderedDecoElemFileProcessor = decoElementFile;
                    break;
                case "Repeat with Rotation":
                case "Echo":
                    patternRenderer = new PatternRenderer("", decoCommands, PatternRenderer.RenderType.ROTATION, info);
                    int repetitionNum = Integer.valueOf(patternRenderTextFiled.getText());
                    if (patternRenderMethod.equals("Echo")) {
                        patternRenderer.echoPattern(repetitionNum);
                    } else {
                        patternRenderer.repeatWithRotation(repetitionNum);
                    }
                    renderedDecoCommands = patternRenderer.getRenderedCommands();
                    renderedDecoElemFileProcessor = new SVGElement(patternRenderer.outputRotated(Integer.valueOf(patternRenderTextFiled.getText())));
                    break;
            }
            if (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText() != "none")
                try {
                    renderedDecoElemFileProcessor.processSvg();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            /* Skeleton Path Generation */
            pointGeneration();
            SVGElement.outputSvgCommands(info.skeletonPathCommands, "skeletonPath-", info);

            /* Tree Structure based rendering */
            determineSkeleton();
            SVGElement.outputSvgCommands(info.skeletonPathCommands, "Skeleton path", info);

        });
    }

    private void pointGeneration() {
        if (info.skeletonPathType.isTessellation) {
            info.pointDistributionDist = Double.valueOf(skeletonGenTextField.getText());
            System.out.println("Distribution dist set to" + info.pointDistributionDist);
            System.out.println("Skeleton Path: Grid Tessellation");

                /* Using tessellation method*/
            info.tessellationDistribution = new PointDistribution(info.skeletonPathType.getPointDistributionType(), info);
            info.tessellationDistribution.generate();
            info.tessellationDistribution.outputDistribution();
            info.skeletonPathCommands = info.tessellationDistribution.toTraversal();
            skeletonCopy.addAll(info.skeletonPathCommands);


            List<Point> perterbed = info.tessellationDistribution.getPoints().stream().map(p -> new Point(p.x + Math.random(), p.y + Math.random())).collect(Collectors.toList());
            Map<Point, Point> oldNewMap = new HashMap<>();
            for (int i = 0; i < info.tessellationDistribution.getPoints().size(); i++) {
                oldNewMap.put(perterbed.get(i), info.tessellationDistribution.getPoints().get(i));
            }

            info.spanningTree = PointDistribution.toMST(perterbed);
            PointDistribution.remapPoint(info.spanningTree, oldNewMap);
            TreeTraversal renderer = new TreeTraversal(info.spanningTree);
            renderer.traverseTree();
            info.skeletonPathCommands = renderer.getRenderedCommands();
            skeletonCopy.addAll(info.skeletonPathCommands);
            info.nodeType = renderer.getNodeLabel();
            info.generatedPoints = info.tessellationDistribution.getPoints();
            SVGElement.outputSvgCommands(info.skeletonPathCommands, "Tessellation Skeleton", info);
        } else {
            System.out.println("SKELETON PATH TYPE:" + info.skeletonPathType);
            switch (info.skeletonPathType) {
                case POISSON_DISK:
                    info.pointDistributionDist = Double.valueOf(skeletonGenTextField.getText());
                    info.generatedPoints = PointDistribution.poissonDiskSamples(info);
                    info.spanningTree = PointDistribution.toMST(info.generatedPoints);
                    SVGElement.outputPoints(info.generatedPoints, info);
                    TreeTraversal renderer = new TreeTraversal(info.spanningTree);
                    renderer.traverseTree();
                    info.skeletonPathCommands = renderer.getRenderedCommands();
                    info.nodeType = renderer.getNodeLabel();
                    skeletonCopy.addAll(info.skeletonPathCommands);

                    break;

//                case HILBERT_CURVE:
//                    HilbertCurveGenerator hilbertcurve = new HilbertCurveGenerator(info.regionFile.getMinPoint(),
//                            new Point(info.regionFile.getMaxPoint().x, 0),
//                            new Point(0, info.regionFile.getMaxPoint().y), Integer.valueOf(skeletonGenTextField.getText()));
//                    info.skeletonPathCommands = hilbertcurve.patternGeneration();
//                    PatternRenderer interpolationRenderer = new PatternRenderer(info.regionFile.getfFileName(), info.skeletonPathCommands, "",
//                            renderedDecoCommands, PatternRenderer.RenderType.CATMULL_ROM, info);
//                    interpolationRenderer.toCatmullRom();
//                    info.skeletonPathCommands = interpolationRenderer.getRenderedCommands();
//                    break;

//                case ECHO:
//                    PatternRenderer traversaler = new PatternRenderer(info.regionFile.getCommandList(), PatternRenderer.RenderType.ECHO, info);
//                    info.skeletonPathCommands = traversaler.echoPattern(Integer.valueOf(skeletonGenTextField.getText()));
//                    break;
//
//                case MEDIAL_AXIS:
//                    info.skeletonPathCommands = info.regionFile.getBoundary().generateMedialAxis();
//                    break;
//
//                case SNAKE:
//                    SkeletonPathGenerator generator = new SkeletonPathGenerator(info);
//                    generator.snakePathGenerator(Integer.valueOf(skeletonGenTextField.getText()));
//                    info.skeletonPathCommands = generator.getSkeletonPath();
//                    break;
            }
        }
    }


    private Map<SVGElement, Double> getDecoElmentAndAngleMap() {
        Map<SVGElement, Double> decoElementAngleMap = new HashMap<>(); // angle is in radians
        for (SVGElement deco : decoElements) {
            SvgPathCommand firstCommand = deco.getCommandList().get(1);
            SvgPathCommand lastCommand = deco.getCommandList().get(deco.getCommandList().size() - 1);
            Point startStart = deco.getCommandList().get(0).getDestinationPoint();
            Point startEnd = firstCommand.isLineTo() ? firstCommand.getDestinationPoint() : Spline.evaluate(startStart, firstCommand.getControlPoint1(), firstCommand.getControlPoint2(), firstCommand.getDestinationPoint(), 0.2);
            Vector2D startVector = new Vector2D(startStart, startEnd);

            Point endStart = lastCommand.getDestinationPoint();
            Point endEnd = lastCommand.isLineTo() ? lastCommand.getDestinationPoint() : Spline.evaluate(deco.getCommandList().get(deco.getCommandList().size() - 2).getDestinationPoint(), lastCommand.getControlPoint1(), lastCommand.getControlPoint2(), endStart, 0.8);
            Vector2D endVector = new Vector2D(endStart, endEnd);

            double angle = Vector2D.getAngle(startVector, endVector);
            if (angle > Math.PI)
                angle = Math.PI * 2 - angle;
            decoElementAngleMap.put(deco, angle);
        }
        return decoElementAngleMap;
    }

    private void toWandererVersion3(List<SvgPathCommand> commands) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        //add random points to make commands look prettier
        String output = "";
        loadWandererDefault();
        info.skeletonPathCommands = commands;
        List<SvgPathCommand> outputCommands = new ArrayList<>();

        // calculate decoElementAnd Angle
//        Map<SVGElement, SVGElement> decoElemCollisionMap = new HashMap<>();
//        for (int i = 0; i < decoElements.size(); i++) {
//            decoElemCollisionMap.put(decoElements.get(i), decoElementsCollision.get(i));
//        }
        Map<SVGElement, Double> decoElementAngleMap = getDecoElmentAndAngleMap();
        System.out.println("Mymap:");
        System.out.println(decoElementAngleMap);
        PatternRenderer interpolationRenderer = new PatternRenderer(info.regionFile.getfFileName(), info.skeletonPathCommands, "",
                renderedDecoCommands, PatternRenderer.RenderType.CATMULL_ROM, info);
        interpolationRenderer.toCatmullRom();
        info.skeletonPathCommands = interpolationRenderer.getRenderedCommands();
        SVGElement.outputSvgCommands(info.skeletonPathCommands, "Skeleton path", info);
        renderedDecoCommands = decoElementFile.getCommandList();
        normalizeDecoElement();
        for (SVGElement f : decoElements) {
            f.setCommandLists(normalizeDecoElement(f));
        }
        setBranchingParameter();

        collisionCommands = SvgPathCommand.commandsScaling(collisionCommands,
                info.decorationSize,
                collisionCommands.get(0).getDestinationPoint());
        SVGElement.outputSvgCommands(info.skeletonPathCommands, "Without deco", info);

        for (SVGElement f : decoElements) {
            f.setCommandLists(SvgPathCommand.commandsScaling(f.getCommandList(), info.decorationSize, f.getCommandList().get(0).getDestinationPoint()));
        }

        Point secondControlPoint = new Point();
        Point prevDecoSecondPoint = new Point();
        Point prevDecoFirstPoint = new Point();

        List<Point> allDestionation = new ArrayList<>();
        List<Point> allFirstControl = new ArrayList<>();
        List<Point> allSecondControl = new ArrayList<>();
        for (int i = info.skeletonPathCommands.size() - 1; i >= 1; i--) {
            boolean isLast = i == info.skeletonPathCommands.size() - 1;
            Point prevPoint = isLast ? info.skeletonPathCommands.get(i).getDestinationPoint() : info.skeletonPathCommands.get(i + 1).getDestinationPoint();
            Point thisPoint = info.skeletonPathCommands.get(i).getDestinationPoint();
            Point nextPoint = info.skeletonPathCommands.get(i - 1).getDestinationPoint();
            Double between = Math.abs(Point.getAngle(thisPoint, prevPoint) - Point.getAngle(thisPoint, nextPoint));
            Vector2D nextVec = new Vector2D(thisPoint, nextPoint).unit();
            Vector2D prevVec = new Vector2D(thisPoint, prevPoint).unit();
            double betweenAngle = Vector2D.getAngle(nextVec, prevVec);
            double rotation = nextVec.add(prevVec).getAngle() - Math.PI * 0.5;
            while (rotation > Math.PI * 2.0)
                rotation -= Math.PI * 2.0;
            while (rotation < 0)
                rotation += Math.PI * 2.0;
            if (between > Math.PI)
                between = Math.PI * 2 - between;
            SVGElement insertDeco = bestDeco(between, decoElementAngleMap);
            output += SVGElement.outputText(String.format("%.1f-%s-%.1f", between / Math.PI * 180, insertDeco.getfFileName(), decoElementAngleMap.get(insertDeco) / Math.PI * 180), thisPoint, "FF0000");

            List<SvgPathCommand> nonStrippedDecoCommands = insertDeco.getCommandList();

            List<SvgPathCommand> nonStrippedRotated = PatternRenderer.insertPatternToList(nonStrippedDecoCommands, null, nonStrippedDecoCommands.get(0).getDestinationPoint(), rotation, false, false);


             /* insertion point calc */
            Point startPoint = nonStrippedRotated.get(1).getDestinationPoint(); // skipped first
            List<SvgPathCommand> strippedRotatedCommands = nonStrippedRotated.subList(1, nonStrippedRotated.size() - 1);
            strippedRotatedCommands.get(0).setCommandType(SvgPathCommand.CommandType.LINE_TO);
            strippedRotatedCommands.get(strippedRotatedCommands.size() - 1).setCommandType(SvgPathCommand.CommandType.LINE_TO);
            Point centerPoint = new ConvexHullBound(SvgPathCommand.toPoints(strippedRotatedCommands)).getBox().getCenter(); // skip first and last

            Point insertPoint = thisPoint.add(startPoint.minus(centerPoint));
            List<SvgPathCommand> nonStrippedTranslated = PatternRenderer.insertPatternToList(nonStrippedRotated, null, insertPoint.add(nonStrippedRotated.get(0).getDestinationPoint().minus(nonStrippedRotated.get(1).getDestinationPoint())), 0, false, false);
            List<SvgPathCommand> strippedTranslated = PatternRenderer.insertPatternToList(strippedRotatedCommands, null, insertPoint, 0, false, false);

            if (Point.getDistance(nonStrippedTranslated.get(0).getDestinationPoint(), nextPoint) >
                    Point.getDistance(nonStrippedTranslated.get(nonStrippedTranslated.size() - 1).getDestinationPoint(), nextPoint)) {
                strippedTranslated = SvgPathCommand.reverseCommands(strippedTranslated);
                nonStrippedTranslated = SvgPathCommand.reverseCommands(nonStrippedTranslated);
                strippedRotatedCommands = SvgPathCommand.reverseCommands(strippedRotatedCommands);
                nonStrippedRotated = SvgPathCommand.reverseCommands(nonStrippedRotated);

            }
            double len = info.tangentPercentage;

            //scale the tangents at connecting points
            Point prevDecoFirstVector = new Point(secondControlPoint.minus(prevDecoSecondPoint)).unit();
            secondControlPoint = prevDecoSecondPoint.add(prevDecoFirstVector.multiply(Point.getDistance(thisPoint, prevPoint) * len));


            Point firstControlPoint = nonStrippedTranslated.get(nonStrippedTranslated.size() - 1).getDestinationPoint();
            Point thisDecoSecondLastConnect = nonStrippedTranslated.get(nonStrippedTranslated.size() - 2).getDestinationPoint();
            Point thisDecoLastVector = new Point(firstControlPoint.minus(thisDecoSecondLastConnect)).unit();
            firstControlPoint = thisDecoSecondLastConnect.add(thisDecoLastVector.multiply(Point.getDistance(thisPoint, prevPoint) * len));


            allFirstControl.add(firstControlPoint);
            allSecondControl.add(secondControlPoint);
//            SvgPathCommand trainsitionCommand = new SvgPathCommand(firstControlPoint, secondControlPoint, prevDecoSecondPoint, SvgPathCommand.CommandType.CURVE_TO);
            SvgPathCommand trainsitionCommand = new SvgPathCommand(firstControlPoint, secondControlPoint, prevDecoFirstPoint, SvgPathCommand.CommandType.CURVE_TO);

            secondControlPoint = nonStrippedTranslated.get(0).getDestinationPoint();
            prevDecoFirstPoint = nonStrippedTranslated.get(0).getDestinationPoint();
            prevDecoSecondPoint = strippedTranslated.get(0).getDestinationPoint(); //destination
            allDestionation.add(prevDecoSecondPoint);


            if (!isLast) {
                outputCommands.add(0, trainsitionCommand);
//                outputCommands.addAll(0, strippedTranslated);
                outputCommands.addAll(0, nonStrippedTranslated);
                SVGElement.outputSvgCommands(strippedTranslated, "strippedTranslated", info);
                SVGElement.outputSvgCommands(nonStrippedTranslated, "nonstrippedTranslated", info);
                SVGElement.outputSvgCommands(strippedRotatedCommands, "strippedRotated", info);
                SVGElement.outputSvgCommands(nonStrippedRotated, "nonStrippedRotated", info);
                System.out.println("wtf");

            }

//            SVGElement.outputSvgCommands(outputCommands, "developinPath", info);

        }
        System.out.println(output);
        info.skeletonPathCommands = outputCommands;
        SVGElement.outputPoints(allDestionation, info, "allDestionation");
        SVGElement.outputPoints(allFirstControl, info, "allFirstControl");
        SVGElement.outputPoints(allSecondControl, info, "allSecondControl");
        List<Point> allControl = new ArrayList<>(allFirstControl);
        allControl.addAll(allSecondControl);
        SVGElement.outputSvgCommands(info.skeletonPathCommands, "beforeStrip", info);
        SVGElement.outputSvgCommandsAndPoints(info.skeletonPathCommands, allControl, "commandsWithControlPoints", info);
        SVGElement.outputSvgCommandsAndPointsAndText(info.skeletonPathCommands, output, allControl, "commandsWithControlPointsAndAngle", info);
//        int currN = skeletonPathCommands.size();
//        for (int i = 0; i < currN; i++) {
//            skeletonPathCommands.add(new SvgPathCommand(skeletonPathCommands.get(i).getControlPoint1(), SvgPathCommand.CommandType.LINE_TO));
//            skeletonPathCommands.add(new SvgPathCommand(skeletonPathCommands.get(i).getControlPoint2(), SvgPathCommand.CommandType.LINE_TO));
//            skeletonPathCommands.add(new SvgPathCommand(skeletonPathCommands.get(i).getDestinationPoint(), SvgPathCommand.CommandType.LINE_TO));
//        }
    }

    private SVGElement bestDeco(double betweenAngle, Map<SVGElement, Double> decoElementAngleMap) {
        SVGElement bestDeco = decoElements.get(0);
        while (betweenAngle < 0)
            betweenAngle += Math.PI * 2;
        while (betweenAngle > Math.PI * 2)
            betweenAngle -= Math.PI * 2;

        for (SVGElement e : decoElements) {
            if (Math.abs(decoElementAngleMap.get(e) - betweenAngle) > Math.abs(decoElementAngleMap.get(bestDeco) - betweenAngle))
                bestDeco = e;
        }

        return bestDeco;
    }

    private List<SvgPathCommand> quadraplePoints(List<SvgPathCommand> commands, boolean perturb) {
        for (int i = commands.size() - 2; i >= 0; i--) {
            Point thisPoint = commands.get(i).getDestinationPoint();
            Point nextPoint = commands.get(i + 1).getDestinationPoint();
            Point thisToNextVector = nextPoint.subtract(thisPoint).unit();
            Point thisToNextPerpendicularVector = new Point(-1 * thisToNextVector.y, thisToNextVector.x).unit();
            Point quarter = Point.intermediatePointWithProportion(thisPoint, nextPoint, 0.33);
            Point threeQuarter = Point.intermediatePointWithProportion(thisPoint, nextPoint, 0.66);

            double rad = Point.getDistance(thisPoint, nextPoint) / 4.0;

            if (perturb) {
                quarter = quarter.add(thisToNextPerpendicularVector.multiply(Math.random() * rad - rad * 0.5));

                threeQuarter = threeQuarter.add(thisToNextPerpendicularVector.multiply(Math.random() * rad - rad * 0.5));

            }

            commands.add(i + 1, new SvgPathCommand(quarter));
            commands.add(i + 2, new SvgPathCommand(threeQuarter));

        }

        return commands;
    }

    private void outputAllTreeRendering() {
        PatternRenderer interpolationRenderer = new PatternRenderer(info.regionFile.getfFileName(), info.skeletonPathCommands, "",
                renderedDecoCommands, PatternRenderer.RenderType.CATMULL_ROM, info);
        interpolationRenderer.toCatmullRom();
        SVGElement.outputSvgCommands(interpolationRenderer.getRenderedCommands(), "", info);

        PatternRenderer stippleRenderer, fixedWidthRenderer;
        stippleRenderer = new PatternRenderer(info.skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
        fixedWidthRenderer = new PatternRenderer(info.skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
        stippleRenderer.fixedWidthFilling(info.pointDistributionDist / 3, Double.valueOf(skeletonRenderTextField2.getText()));
        fixedWidthRenderer.fixedWidthFilling(info.pointDistributionDist / 5.0, Double.valueOf(skeletonRenderTextField2.getText()));


        stippleRenderer.setRenderedCommands(PatternRenderer.interpolate(stippleRenderer.getRenderedCommands()));
        SVGElement.outputSvgCommands(stippleRenderer.getRenderedCommands(), "", info);
        SVGElement.outputSvgCommands(fixedWidthRenderer.getRenderedCommands(), "", info);
    }

    private void determineSkeleton() {
        info.skeletonRenderType = (SkeletonRenderType) skeletonRenderComboBox.getValue();
        if (info.skeletonPathType.isTreeStructure) {
            switch (info.skeletonRenderType) {
                case CATMULL_ROM:
                    // Normalize deco element to 100 * 100
                    normalizeDecoElement();
                    skeletonrenderer = new PatternRenderer(info.regionFile.getfFileName(), info.skeletonPathCommands, "",
                            renderedDecoCommands, PatternRenderer.RenderType.CATMULL_ROM, info);
                    skeletonrenderer.toCatmullRom();
                        /* A curved tree is not really "rendered"/ we still want to be able to put patterns on it. needs to add additional
                         * parameterization here for selecting how we want the deco elements put on the curved tree */
                    SVGElement.outputSvgCommands(skeletonrenderer.getRenderedCommands(), "", info);
                        /*TODO: rewrite code! below code is exactly the same as FIXED WIDTH FILL*/
                    if (!((ToggleButton) patternSourceGroup.getSelectedToggle()).getText().equals("none")) {
                                    /* scale deco to full*/
//                            info.setDecoElmentScalingFactor(info.getRegionFile().getPoints().getArea() / (2.5 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())));

                        info.collisionFile = collisionFile;


                        final List<SvgPathCommand> renderedCommandsCopy = new ArrayList<>(renderedDecoCommands),
                                collisionCopy = new ArrayList<>(collisionCommands),
                                skeletonCopy = new ArrayList<>(skeletonrenderer.getRenderedCommands());

                        boolean batchOutput = false; // for producing set of 3 x 3 patterns with different parameters
                        if (batchOutput) {
                            for (int i = 1; i < 2; i++) {
//                            info.setPointDistributionDist();
                                for (double decorationSize = 0.8; decorationSize < 1.65; decorationSize += 0.4) {
                                    List<List<SvgPathCommand>> setsOfNine = new ArrayList<>();
                                    List<String> setsOfNineName = new ArrayList<>();
                                    info.decorationSize = Double.valueOf(decorationSize);
                                    for (int gapMultiplier = 0; gapMultiplier < 3; gapMultiplier++) {
                                        double gap = gapMultiplier * 0.3 + 0.7;
                                        info.setGapLen(gap * 10.0);
                                        for (int angleMultiplier = 1; angleMultiplier < 4; angleMultiplier++) {
                                            double initialAngle = angleMultiplier * 15;
                                            collisionCommands = new ArrayList<>(collisionCopy);
                                            renderedDecoCommands = new ArrayList<>(renderedCommandsCopy);
                                            skeletonrenderer.setRenderedCommands(new ArrayList<>(skeletonCopy));
                                            info.initialAngle = initialAngle;
                                            collisionCommands = SvgPathCommand.commandsScaling(collisionCommands,
                                                    info.decorationSize,
                                                    renderedDecoCommands.get(0).getDestinationPoint());
                                            renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                                    info.decorationSize,
                                                    renderedDecoCommands.get(0).getDestinationPoint());

                                        /* Adding alternating leaves*/
                                            info.collisionCommands = collisionCommands;
                                            String paramterStr = "_size_" + decorationSize + "_gap_" + gap + "_angle_" + initialAngle + "_poisson_" + info.pointDistributionDist;
                                            System.out.println(paramterStr);
                                            skeletonrenderer.addAlternatingDecoElmentToSplineTree(renderedDecoCommands, info);

                                            Point firstPoint = skeletonrenderer.getRenderedCommands().get(0).getDestinationPoint();
                                            setsOfNine.add(SvgPathCommand.commandsShift(skeletonrenderer.getRenderedCommands(), new Point(firstPoint.x + 750.0 * (angleMultiplier - 1), firstPoint.y + 750 * gapMultiplier)));
                                            setsOfNineName.add(paramterStr);
                                            List<SvgPathCommand> trimmed;
//                                            trimmed = boundary.fitCommandsToRegionTrimToBoundary(skeletonrenderer.getRenderedCommands(), info);
//                                            SVGElement.outputSvgCommands(trimmed, "trimmed" + paramterStr, info);
//                                            SVGElement.outputSvgCommands(skeletonrenderer.getRenderedCommands(), decoFileName + paramterStr, info);

                                        }

                                    }

                                    List<SvgPathCommand> flattened = new ArrayList<>();
                                    for (List<SvgPathCommand> li : setsOfNine)
                                        flattened.addAll(li);
                                    SVGElement.outputSvgCommands(flattened, "", 2200, 2200);

                                }
                            }
                        } else {
                            setBranchingParameter();
                            collisionCommands = SvgPathCommand.commandsScaling(collisionCommands, info.decorationSize, renderedDecoCommands.get(0).getDestinationPoint());
                            renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands, info.decorationSize, renderedDecoCommands.get(0).getDestinationPoint());
                                /* Adding a "pair" of leaves */
//                            skeletonrenderer.addDecoElmentToSplineTree(renderedDecoCommands, info);
                                /* Adding alternating leaves*/
                            info.collisionCommands = collisionCommands;

                            skeletonrenderer.addAlternatingDecoElmentToSplineTree(renderedDecoCommands, info);

                        }


                    }

                    SVGElement.outputSvgCommands(skeletonrenderer.getRenderedCommands(), "", info);
                    SVGElement.outputSvgCommandsWithBoundary(skeletonrenderer.getRenderedCommands(), "withboundary", info);
                    break;
                case WANDERER:
                    info.tangentPercentage = Double.valueOf(skeletonRenderTextField2.getText());
                    double div;
                    switch (info.skeletonPathType) {
                        case POISSON_DISK:
                            div = 2.5f;
                            break;
                        default:
                            div = 3.0f;
                    }

                    double width = info.pointDistributionDist / div;
                    String concordeSessionName = "concordeInput";
                    PointDistribution.writeoutToConcorderFormat(info.generatedPoints, concordeSessionName);
                    String concordeInputPath = "./out/" + concordeSessionName + ".tsp";
                    String command = String.format("./src/resources/concorde/TSP/concorde -n %s %s", concordeSessionName, concordeInputPath);
                    Process p = null;
                    try {
                        p = Runtime.getRuntime().exec(command);
                        p.waitFor();
                        File j = new File(concordeSessionName + ".sol") ;
                        SVGElement processed = new SVGElement(j);
                        List<SvgPathCommand> concordeTSPPath = processed.processConcordeCommand(info.generatedPoints);
                        SVGElement.outputSvgCommands(concordeTSPPath, "concordePath", info);
                        toWandererVersion3(new ArrayList<>(concordeTSPPath));
                        Visualizer.addCommandsPlot(info.skeletonPathCommands, visualizationPane);
                        Stage visualizationStage = new Stage();
                        ScrollPane newPane = new ScrollPane();
                        newPane.setContent(Visualizer.addCommandsPlot(info.skeletonPathCommands));
                        newPane.setPrefWidth(visualizationPane.getPrefWidth());
                        newPane.setPrefHeight(visualizationPane.getPrefHeight());
                        visualizationStage.setScene(new Scene(newPane, Color.rgb(35, 39, 50)));
                        visualizationStage.setHeight(visualizationPane.getPrefHeight());
                        visualizationStage.setWidth(visualizationPane.getPrefWidth());
                        visualizationStage.show();

                        SVGElement.outputSvgCommands(info.skeletonPathCommands, "Added Skeleton path", info);
                        SVGElement.outputPoints(info.generatedPoints, info);
                        SVGElement.outputSvgCommandsAndPoints(info.skeletonPathCommands, info.generatedPoints, "pointsAndCommand", info);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (XPathExpressionException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

//                    skeletonrenderer = new PatternRenderer(info.skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
//                    skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField2.getText()));
//                    skeletonrenderer.setRenderedCommands(PatternRenderer.interpolate(skeletonrenderer.getRenderedCommands()));
//
//                    normalizeDecoElement();
//                    setBranchingParameter();
//
//                    collisionCommands = SvgPathCommand.commandsScaling(collisionCommands,
//                            info.decorationSize,
//                            collisionCommands.get(0).getDestinationPoint());
//                    renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
//                            info.decorationSize,
//                            renderedDecoCommands.get(0).getDestinationPoint());
//
//
//                    info.collisionCommands = collisionCommands;
//                    skeletonrenderer.addAlternatingDecoElmentToSplineTree(renderedDecoCommands, info);
//                    SVGElement.outputSvgCommands(skeletonrenderer.getRenderedCommands(), "", info);
//                    SVGElement.outputSvgCommandsWithBoundary(skeletonrenderer.getRenderedCommands(), "", info);
                    break;
                case FIXED_WIDTH_FILL:
                case STIPPLING:
                    boolean isStippling = info.skeletonRenderType == STIPPLING || info.skeletonRenderType == WANDERER;
                    width = isStippling ? info.pointDistributionDist / 3 : info.pointDistributionDist / 4.0;

                    switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                        case "none":
                            skeletonrenderer = new PatternRenderer(info.skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
                            skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField2.getText()));
                            break;
                        default:
                                    /* scale deco to full*/

                            collisionCommands = SvgPathCommand.commandsScaling(collisionCommands,
                                    info.decorationSize,
                                    collisionCommands.get(0).getDestinationPoint());
                            renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                    info.decorationSize,
                                    renderedDecoCommands.get(0).getDestinationPoint());
                            skeletonrenderer = new PatternRenderer(info.regionFile.getfFileName(), info.skeletonPathCommands, "",
                                    renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION, info);
                            skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField2.getText()));
                            break;
                    }
                    if (isStippling) {
                        skeletonrenderer.setRenderedCommands(PatternRenderer.interpolate(skeletonrenderer.getRenderedCommands()));
                    }


                    SVGElement.outputSvgCommands(skeletonrenderer.getRenderedCommands(), "", info);
                    SVGElement.outputSvgCommandsWithBoundary(skeletonrenderer.getRenderedCommands(), "", info);
                    break;

                case RECTANGLE:
                    skeletonrenderer = new RectanglePacking(renderedDecoCommands, info,
                            info.skeletonRenderType != RECTANGLE && skeletonGenComboBox.getValue().equals(VINE));
                    ((RectanglePacking) skeletonrenderer).rectanglePacking();
                    SVGElement.outputSvgCommands(skeletonrenderer.getRenderedCommands(), "", info);
                    List<SvgPathCommand> skeletonAndRender = new ArrayList<>(skeletonCopy),
                            regionAndRender = new ArrayList<>(info.regionFile.getCommandList());
                    skeletonAndRender.addAll(skeletonrenderer.getRenderedCommands());
                    regionAndRender.addAll(skeletonrenderer.getRenderedCommands());
                    SVGElement.outputSvgCommands(skeletonAndRender, "visualize_skeleton_", info);
                    SVGElement.outputSvgCommands(regionAndRender, "visualize_region_", info);
                    break;
                case PEBBLE:

                    List<List<SvgPathCommand>> setsOfNine = new ArrayList<>();
                    info.drawBound = false;
                    if (info.spanningTree == null) {
                    } else {
                        skeletonrenderer = new PebbleRenderer(renderedDecoCommands, info,
                                info.skeletonRenderType != RECTANGLE && skeletonGenComboBox.getValue().equals(VINE));
                        info.randomFactor = Double.valueOf(skeletonRenderTextField2.getText());
                        info.initialLength = Double.valueOf(skeletonRenderTextField1.getText());
                        skeletonrenderer.pebbleFilling();

                        SVGElement.outputSvgCommands(skeletonrenderer.getRenderedCommands(), "", info);
                        List<SvgPathCommand> skeletonAddRender = new ArrayList<>(skeletonCopy),
                                regionAddRender = new ArrayList<>(info.regionFile.getCommandList());
                        skeletonAddRender.addAll(skeletonrenderer.getRenderedCommands());
                        regionAddRender.addAll(skeletonrenderer.getRenderedCommands());
                        SVGElement.outputSvgCommands(skeletonAddRender, "visualize_skeleton_", info);
                        SVGElement.outputSvgCommands(regionAddRender, "visualize_region_", info);
                        List<SvgPathCommand> trimmed = info.regionFile.getBoundary().fitCommandsToRegionTrimToBoundary(skeletonrenderer.getRenderedCommands(), info);
                        SVGElement.outputSvgCommands(trimmed, "trimmed_", info);
                        trimmed.addAll(info.regionFile.getCommandList());
                        SVGElement.outputSvgCommands(trimmed, "trimmed_with region", info);

                    }

                    break;
                case NONE:
                    if (info.skeletonPathCommands.size() != 0) {
                        System.out.println("Skeleton Size" + info.skeletonPathCommands.size());
                        switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                            case "none":
                                skeletonrenderer = new PatternRenderer(info.skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
                                skeletonrenderer.fixedWidthFilling(0, Double.valueOf(skeletonRenderTextField2.getText()));
                                break;
                            default:
                                    /* scale deco to full*/
                                renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                        info.pointDistributionDist / (1.4 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
                                        renderedDecoCommands.get(0).getDestinationPoint());
                                skeletonrenderer = new PatternRenderer(info.regionFile.getfFileName(), info.skeletonPathCommands, "",
                                        renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION, info);
                                skeletonrenderer.fixedWidthFilling(0, Double.valueOf(skeletonRenderTextField2.getText()));
                                break;
                        }
                        SVGElement.outputSvgCommands(skeletonrenderer.getRenderedCommands(), "", info);
                    } else {
                        System.out.println("ERROR: skeleton path commands");
                    }
                    break;
            }
        } else {
            List<SvgPathCommand> renderedCommands = info.skeletonPathCommands;
            switch ((SkeletonRenderType) skeletonRenderComboBox.getValue()) {
                case FIXED_WIDTH_FILL:
                    switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                        case "none":
                            skeletonrenderer = new PatternRenderer(info.skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
                            break;
                        case "from file":
                        case "from library":
                            skeletonrenderer = new PatternRenderer(info.regionFile.getfFileName(), info.skeletonPathCommands, "",
                                    renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION, info);
                            break;
                    }
                    renderedCommands = skeletonrenderer.fixedWidthFilling(5, Double.valueOf(skeletonRenderTextField2.getText()));
                    break;

//                case ALONG_PATH:
//                    mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
//                    /** Combine pattern */
//                    mergedPattern.combinePattern();
//                    renderedCommands = boundary.fitCommandsToRegionTrimToBoundary(mergedPattern.getCombinedCommands(), info);
//                    break;
//                case TILING:
//                    double patternHeight = skeletonPathFileProcessor.getHeight() / rows;
//                    mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
//                    /** Combine pattern */
//                    mergedPattern.tilePattern(patternHeight);
//                    renderedCommands = boundary.fitCommandsToRegionTrimToBoundary(mergedPattern.getCombinedCommands(), info);
//                    break;
            }
            SVGElement.outputSvgCommands(renderedCommands, "", info);
        }
    }
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

    private List<SvgPathCommand> normalizeDecoElement(SVGElement myDecoFile) {
        double maxDimension = Double.max(myDecoFile.getWidth(), myDecoFile.getHeight());

        List<SvgPathCommand> scaledCommands = SvgPathCommand.commandsScaling(myDecoFile.getCommandList(), 10.0 / maxDimension, myDecoFile.getCommandList().get(0).getDestinationPoint());
        return scaledCommands;
    }

    private void normalizeDecoElement() {
        double maxDimension = decoElementFile.getWidth();
        collisionCommands.clear();
        if (collisionFile != null) {
            collisionCommands.add(renderedDecoCommands.get(0));
            collisionCommands.addAll(collisionFile.getCommandList());
        }
        renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands, 10.0 / maxDimension, renderedDecoCommands.get(0).getDestinationPoint());
        collisionCommands = SvgPathCommand.commandsScaling(collisionCommands, 10.0 / maxDimension, collisionCommands.get(0).getDestinationPoint());
        SVGElement.outputSvgCommands(renderedDecoCommands, "scaledDeco", info);

    }

    private void setBranchingParameter() {
        info.decorationSize = Double.valueOf(skeletonRenderTextField1.getText());
        info.setGapLen(Double.valueOf(skeletonRenderTextField2.getText()) * 10);
        info.initialAngle = Double.valueOf(skeletonRenderTextField3.getText());
    }

    private void setUpFont() {
        Label[] functionLabels = {textFieldLabel, regionSelectionLabel, skeletonGnerationlabel, skeletonRenderinglabel,
                patternLabel, svgToPatLabel, skeletonGenParam1Label, skeletonRenderFieldLabel1, skeletonRenderFieldLabel2, skeletonRenderFieldLabel3,
                patternSelectionLabel, patternRenderFieldLabel, patternRenderFieldLabel, patternRenderLabel};
        Label[] columnLabels = {regionLabel, skeletonLabel, patternLabel, toolLabel};
        Button[] functionButtons = {loadRegionButton};
        ToggleButton[] toggleButtons = {patternFromFile, noPattern, patternFromLibrary};
        for (Label label : functionLabels) {
            label.setFont(FUNC_LABEL_FONT);
            label.setTextFill(labelColor);
        }

        for (Label label : columnLabels) {
            label.setFont(COL_LABEL_FONT);
            label.setTextFill(columnLabelColor);
        }

        for (Button button : functionButtons)
            button.setFont(BUTTON_FONT);

        for (ToggleButton button : toggleButtons)
            button.setFont(BUTTON_FONT);

        generateButton.setFont(COL_LABEL_FONT);
        quiltingPatternGeneration.setFont(TITLE_FONT);
        quiltingPatternGeneration.setTextFill(Color.ALICEBLUE);
        generateButton.setTextFill(Color.DARKBLUE);

    }

    private void setupListeners() {
        //Selection Listeners
        /* Pattern Source Listener */
        patternLibraryComboBox.getItems().addAll("feather");
        patternSourceGroup.selectedToggleProperty().addListener((ov, toggle, new_toggle) -> {
            if (new_toggle == null) {
                fileSourceBox.getChildren().setAll();
            } else {
                if (patternSourceGroup.getSelectedToggle() == patternFromFile) {
                    System.out.println("New Pattern Source: Pattern From File ");
                    fileSourceBox.getChildren().setAll(loadDecoElementButton);
                }

                if (patternSourceGroup.getSelectedToggle() == patternFromLibrary) {
                    System.out.println("New Pattern Source: Pattern From Library ");
                    fileSourceBox.getChildren().setAll(patternLibraryComboBox);
                    info.skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
                    if (info.skeletonPathType.isTreeStructure) {
                        patternLibraryComboBox.getItems().setAll(endpointList);
                    } else {
                        if (skeletonRenderComboBox.getValue().equals(TILING))
                            patternLibraryComboBox.getItems().setAll(tileList);
                        else
                            patternLibraryComboBox.getItems().setAll(alongPathList);
                    }

                    System.out.println(patternLibraryComboBox.getItems().toString());
                }

                if (patternSourceGroup.getSelectedToggle() == noPattern) {
                    System.out.println("New Pattern Source: No Pattern");
                    fileSourceBox.getChildren().setAll();
                }
            }
        });

        /* Skeleton Generation Listener */
        skeletonGenComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(patternLibraryComboBox.getItems().toString());
            info.skeletonPathType  = (SkeletonPathType) skeletonGenComboBox.getValue();
            System.out.println("Skeleton generation method changed: " + info.skeletonPathType);
            skeletonRenderComboBox.setValue(NONE);
            if (info.skeletonPathType.isTreeStructure || info.skeletonPathType == POISSON_DISK) {
                patternLibraryComboBox.getItems().setAll(endpointList);
                skeletonRenderComboBox.getItems().setAll(NONE, FIXED_WIDTH_FILL, PEBBLE, RECTANGLE, CATMULL_ROM, STIPPLING, WANDERER);
            } else if (info.skeletonPathType.equals(SNAKE)) {
                skeletonRenderComboBox.getItems().setAll(NONE, ALONG_PATH, TILING);
            } else {
                skeletonRenderComboBox.getItems().setAll(NONE, ALONG_PATH);
            }

            if (info.skeletonPathType.parameterLabel == "") {
                skeletonGenPropertyInput.getChildren().setAll();
            } else {
                skeletonGenParam1Label.setText(info.skeletonPathType.parameterLabel);
                skeletonGenPropertyInput.getChildren().setAll(skeletonGenParam1Label, skeletonGenTextField);
            }

            /* Tree structured */
            if (info.skeletonPathType.isTreeStructure) {
                skeletonRenderPropertyInput.getChildren().setAll(skeletonRenderFieldLabel1, skeletonRenderTextField2);
            } else {
                skeletonRenderPropertyInput.getChildren().removeAll(skeletonRenderFieldLabel1, skeletonRenderTextField2);
            }
        });

        /* Skeleton Rendering Listener */
        skeletonRenderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            SkeletonRenderType newSkeletonRenderType = (SkeletonRenderType) skeletonRenderComboBox.getValue();
            List<String> params = newSkeletonRenderType.typeParameters;
            skeletonRenderPropertyInput.getChildren().setAll();
            for (int i = 0; i < params.size(); i++) {
                Label myLabel = skeletonRenderParamLabels.get(i);
                TextField myTextField = skeletonRenderLabelTexfieldMap.get(myLabel);
                myLabel.setText(params.get(i));
                myTextField.setText(newSkeletonRenderType.parameterDefaultVale.get(i));
                skeletonRenderPropertyInput.getChildren().add(myLabel);
                skeletonRenderPropertyInput.getChildren().add(myTextField);
            }
            System.out.println("Skeleton rendering method changed: " + newSkeletonRenderType);
            if (newSkeletonRenderType.equals(PEBBLE) || newSkeletonRenderType.equals(RECTANGLE)) {
                patternSourceGroup.selectToggle(noPattern);
            } else {
                patternSourceGroup.selectToggle(patternFromFile);
            }

            if (newSkeletonRenderType.equals(TILING)) {
                patternLibraryComboBox.getItems().setAll(tileList);
            }
        });


        /* Library ComboBox Listener/ pattern library combobox Listener */
        patternLibraryComboBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            System.out.println(patternLibraryComboBox.getItems().toString());
            String newPatternFile = patternLibraryComboBox.getValue().toString();
            File library = ALONG_LIB_PATH;
            if (skeletonRenderComboBox.getValue().equals(TILING))
                library = TILE_LIB_PATH;
            /* Tree Structured */
            info.skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
            if (info.skeletonPathType.isTreeStructure)
                library = ENDPOINTS_LIB_PATH;
            File file = new File(library.getPath() + "/" + newPatternFile + ".svg");
            System.out.println("Loading a pattern....");
            decoElementFile = new SVGElement(file);
            try {
                decoElementFile.processSvg();
                SVGElement.outputSvgCommands(decoElementFile.getCommandList(),
                        "decoElem-" + decoElementFile.getfFileName(), info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        /* Pattern rendering Listener */
        patternRenderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            String newSelected = patternRenderComboBox.getValue().toString();
            if (newSelected.equals("No Rendering")) {
                patternPropertyInput.getChildren().removeAll(patternRenderFieldLabel, patternRenderTextFiled);
            } else {
                patternPropertyInput.getChildren().setAll(patternRenderFieldLabel, patternRenderTextFiled);
            }
        });
    }

    public enum SkeletonPathType {
        GRID_TESS(true, true, "Distribution Len"), TRIANGLE_TESS(true, true, "Distribution Len"),
        HEXAGON_TESS(true, true, "Distribution Len"), THREE_3_4_3_4_TESS(true, true, "Distribution Len"),
        POISSON_DISK(false, true, "Distribution Len"), HILBERT_CURVE(false, false, "Level:"),
        ECHO(false, false, "Repetitions:"), MEDIAL_AXIS(false, false, ""),
        SNAKE(false, false, "Rows:"), VINE(false, true, "");
        public final boolean isTessellation, isTreeStructure;
        public final String parameterLabel;

        SkeletonPathType(boolean isTessellation, boolean isTreeStructure, String parameter) {
            this.isTessellation = isTessellation;
            this.isTreeStructure = isTreeStructure;
            this.parameterLabel = parameter;
        }

        public PointDistribution.RenderType getPointDistributionType() {
            switch (this) {
                case GRID_TESS: return PointDistribution.RenderType.GRID;
                case VINE: return PointDistribution.RenderType.VINE;
                case TRIANGLE_TESS: return PointDistribution.RenderType.TRIANGLE;
                case HEXAGON_TESS: return PointDistribution.RenderType.HEXAGON;
                default: return PointDistribution.RenderType.THREE_THREE_FOUR_THREE_FOUR;
            }
        }
    }

    public enum SkeletonRenderType {
        NONE(), TILING(), ALONG_PATH(), RECTANGLE(),
        FIXED_WIDTH_FILL("Deco Size", "3"), CATMULL_ROM("Deco Size", "3", "Gap Len", "0.3", "Initial Angle", "15"),
        PEBBLE("Initial Len", "0.3", "Random Factor", "0.8"), STIPPLING("Deco Size", "3"),
        WANDERER("Deco Size", "3", "Tangent Percentage", "0.3");

        public final List<String> typeParameters, parameterDefaultVale;
        SkeletonRenderType(String... params) {
            this.typeParameters = new ArrayList<>();
            this.parameterDefaultVale = new ArrayList<>();
            for (int i = 0; i < params.length; i += 2) {
                typeParameters.add(params[i]);
                parameterDefaultVale.add(params[i + 1]);

            }
        }

    }


}
