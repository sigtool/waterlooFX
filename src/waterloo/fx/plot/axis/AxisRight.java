/* 
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright King's College London  2014. Copyright Ironduke Publishing Limited, UK 2014-.
 * 
 * @author Malcolm Lidierth, <a href="https://github.com/sigtool/waterlooFX/issues"> [Contact]</a>
 * 
 * Project Waterloo is free software:  you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project Waterloo is distributed in the hope that it will  be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package waterloo.fx.plot.axis;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.text.Text;
import waterloo.fx.plot.Chart;

/**
 * Implementation of the {@code AbstractAxisRegion} for a right axis.
 *
 * @author ML
 */
public class AxisRight extends AbstractAxisRegion {

    public AxisRight(Chart layer) {
        super(layer);
        getChildren().add(getLine().get());
        getAxisLabel().setRotate(90d);
        setCursor(Cursor.DEFAULT);
        getAxisLabel().setFont(getFont());
        getChildren().add(getAxisLabel());
        prefHeightProperty().bind(layer.getFirstLayer().getView().prefHeightProperty());
        // Bind mouse sensitivity to the layer painted poperty for this axis
        mouseTransparentProperty().set(!layer.isRightAxisPainted());
        mouseTransparentProperty().bind(layer.rightAxisPaintedProperty().not());
        requestLayout();
    }

    /**
     * Returns an estimate of the width required to display this axis given the
     * current {@code TickLabel}s and {@code AxisLabel}.
     *
     * @param h - not used. Specify 0d.
     * @return the required width
     */
    @Override
    public final double computePrefWidth(double h) {
        double w = findMinX();
        if (getTickLabels().size() > 0) {
            for (TickLabel text : getTickLabels()) {
                w = Math.max(w, text.getBoundsInParent().getWidth());
            }
        } else {
            Text t = new Text("000");
            t.setFont(getFont());
            w = t.prefWidth(-1d);
        }
        w += getAxisLabel().prefHeight(-1d) + 5d;
        return Math.max(w, Chart.getDefaultInsets().getRight());
    }

    private void doLayout() {
        getLine().get();
        computeValue();
        if (getLayer().isRightAxisLabelled()) {
            double p = getLine().get().getBoundsInParent().getMaxX();
            if (getTickLabels().size() > 0) {
                getChildren().stream().filter(x -> x instanceof TickLabel).forEach((Node x) -> {
                    TickLabel text = (TickLabel) x;
                    //text.setFont(getFont());
                    text.setLayoutX(p);
                    text.setLayoutY(text.getYpos());
                });
                addAxisLabel();
                getAxisLabel().setFont(getFont());
                getAxisLabel().setLayoutX(5d + findMinX() - getAxisLabel().prefWidth(-1d) / 2d + getAxisLabel().prefHeight(-1d));
                getAxisLabel().setLayoutY(getHeight() / 2d);
            }
        } else {
            getTickLabels().stream().forEach(x -> getChildren().remove(x));
            removeAxisLabel();
        }
    }

    /**
     * Recalculates the layout for {@code Nodes} in this axis and calls
     * {@code super.layoutChildren()} to do the real work.
     */
    @Override
    public void layoutChildren() {
        doLayout();
        super.layoutChildren();
    }
    
    private double findMinX() {
        double minx = Double.POSITIVE_INFINITY;
        for (Text text : getTickLabels()) {
            minx = Math.min(minx, text.prefWidth(-1d));
        }
        return minx;
    }

//    private double findMaxX() {
//        double maxx = Double.NEGATIVE_INFINITY;
//        for (Text text : getTickLabels()) {
//            maxx = Math.max(maxx, text.prefWidth(-1d));
//        }
//        if (Double.isFinite(maxx)) {
//            return maxx;
//        } else {
//            Text t = new Text("000");
//            t.setFont(getFont());
//            return t.prefWidth(-1d);
//        }
//    }

    private void computeValue() {
        getTickLabels().stream().forEach((TickLabel x) -> {
            getChildren().remove(x);
        });
        if (getLayer().isRightAxisLabelled()) {
            getLayer().getAxisSet().getYTransform().get().stream()
                    .filter(y -> y >= getLayer().getYMin() && y <= getLayer().getYMax())
                    .forEach((Double y) -> {
                        TickLabel text = null;
                        if (isCategorical()) {
                            if (getCategories().containsKey(y.intValue())) {
                                text = new TickLabel(getCategories().get(y.intValue()));
                            }
                        } else {
                            text = new TickLabel(getLayer().getAxisSet().getYTransform().getTickLabel(y));
                        }
                        if (text != null) {
                            Point2D p1;
                            p1 = getLayer().toPixel(getLayer().getXLeft(), y);
                            p1 = getLayer().getView().localToParent(p1);
                            p1 = parentToLocal(p1);
                            getChildren().add(text);
                            text.setFont(getFont());
                            text.setFill(getLayer().getAxisColor());
                            text.setX(0d);
                            text.setYpos(p1.getY());
                        }
                    });
        }
    }

}
