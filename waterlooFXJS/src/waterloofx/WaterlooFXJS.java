/*
 *
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright Malcolm Lidierth 2015-.
 *
 * @author Malcolm Lidierth <a href="https://github.com/sigtool/waterlooFX/issues"> [Contact]</a>
 *
 * waterlooFX is free software:  you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * waterlooFX is distributed in the hope that it will  be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package waterloofx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import waterloo.fx.plot.AbstractPlot.VisualModel;
import waterloo.fx.plot.*;

/**
 *
 * @author ML
 */
public class WaterlooFXJS extends Application {

    private static final String fxmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<?import waterloo.fx.plot.*?>\n"
            + "<?import java.lang.*?>\n"
            + "<?import java.util.*?>\n"
            + "<?import javafx.scene.*?>\n"
            + "<?import javafx.scene.control.*?>\n"
            + "<?import javafx.scene.layout.*?>";

    Parent root = null;
    Chart chart = null;

    @Override
    public void start(Stage stage) throws Exception {
        String s = (String) getParameters().getNamed().get("fxml");
        if (s == null || s.isEmpty()) {
            chart = new Chart();
            chart.setId("myChart");
            chart.setLeftAxisTitle("Y");
            chart.setBottomAxisTitle("X");
            root = new Pane(new Chart());
        } else if (!s.startsWith("fxml/")) {
            s = "fxml/".concat(s);
            if (!s.endsWith(".fxml")) {
                s = s.concat(".fxml");
            }
            root = FXMLLoader.load(WaterlooFXJS.class.getResource(s));
            chart = (Chart) root.lookup("#myChart");
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void setStyle(String id, String style) {
        Platform.runLater(() -> {
            getChart().lookup(id).setStyle(style);
        });
    }

    private Object runAndWait(Callable callable) throws InterruptedException, ExecutionException {
        FutureTask<Object> future = new FutureTask(callable);
        Platform.runLater(future);
        return future.get();
    }

    /**
     * Thread-safe and blocking method to invoke {@code lookup()} on the root.
     *
     * Finds this Node, or the first sub-node, based on the given CSS selector.
     * If this node is a Parent, then this function will traverse down into the
     * branch until it finds a match. If more than one sub-node matches the
     * specified selector, this function returns the first of them.
     *
     * The lookup will be performed on the FX Platform thread and the method
     * will return the result once the lookup has completed.
     *
     * @param selector
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @see
     * <a href="http://docs.oracle.com/javase/8/javafx/api/javafx/scene/Node.html#lookup-java.lang.String-">
     * http://docs.oracle.com/javase/8/javafx/api/javafx/scene/Node.html#lookup-java.lang.String-</a>
     *
     *
     */
    public Node lookup(String selector) throws InterruptedException, ExecutionException {
        return (Node) runAndWait(() -> {
            return root.lookup(selector);
        });
    }

    /**
     * Thread-safe and blocking method to invoke {@code lookupAll()} on the
     * root.
     *
     * Finds all Nodes, including the root and any children, which match the
     * given CSS selector. If no matches are found, an empty ArrayList is
     * returned. The list is explicitly unordered.
     *
     * The lookup will be performed on the FX Platform thread and the method
     * will return the result once the lookup has completed.
     *
     * @param selector CSS selector {@code String} e.g. ".plot" or "#myNode"
     * @return an ArrayList of nodes satisfying the {@code selector}
     * @throws InterruptedException
     * @throws ExecutionException
     *
     * @see
     * <a href="http://docs.oracle.com/javafx/2/api/javafx/scene/Node.html#lookupAll(java.lang.String)">
     * http://docs.oracle.com/javafx/2/api/javafx/scene/Node.html#lookupAll(java.lang.String)</a>
     *
     */
    public ArrayList<Node> lookupAll(String selector) throws InterruptedException, ExecutionException {
        return (ArrayList<Node>) runAndWait(() -> {
            Set<Node> n0 = root.lookupAll(selector);
            Node[] n1 = new Node[n0.size()];
            n1 = n0.toArray(n1);
            return new ArrayList(Arrays.asList(n1));
        });
    }

    public boolean isRootVisible() {
        if (root == null) {
            return false;
        } else {
            return root.isVisible();
        }
    }

    public Chart getChart() {
        return chart;
    }

    public void add(Node node) throws InterruptedException {
        int counter = 0;
        if (!isRootVisible()) {
            Thread.sleep(20L);
            if (counter++ > 250) {
                return;
            }
        }
        Platform.runLater(() -> {
            getChart().getChildren().add(node);
            getChart().requestLayout();
        });
    }

    public Object parseFXML(String s) {
        // HACKS FOR JAVASCRIPT SUPPORT OF JAVA ENUMS
        s = s.replace("markerType=", "markerTypeAsString=");
        FXMLLoader loader = new FXMLLoader();
        InputStream stream;
        try {
            //StringReader reader = new StringReader(s);
            stream = new ByteArrayInputStream(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            return "WaterlooFXJS:".concat(ex.toString());
        }
        Node node = null;
        try {
            node = loader.load(stream);
        } catch (IOException ex) {
            return "WaterlooFXJS:".concat(ex.toString());
        }
        try {
            stream.close();
        } catch (IOException ex) {
            return "WaterlooFXJS:".concat(ex.toString());
        }
        return node;
    }

    public static VisualModel newVisualModel() {
        return new ScatterPlot().getVisualModel();
    }

//    public void print() {
//        Chart chart = getChart();
//        PrinterJob job = PrinterJob.createPrinterJob();
//        if (job != null && chart != null) {
//            boolean success = job.showPageSetupDialog(null);
//            if (success) {
//                success = job.printPage(chart);
//                if (success) {
//                    job.endJob();
//                }
//            }
//        }
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
