/* 
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London  2014-
 * 
 * @author Malcolm Lidierth, <a href="http://sourceforge.net/p/waterloo/discussion/"> [Contact]</a>
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

package waterloo.fx.axis;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.text.Text;
import waterloo.fx.plot.Chart;

/**
 *
 * @author ML
 */
    public class AxisLeft extends AbstractAxisRegion {

        public AxisLeft(Chart layer) {
            super(layer);
            getChildren().add(getLine().get());
            getAxisLabel().setRotate(-90d);
            getAxisLabel().setTextOrigin(VPos.CENTER);
            getChildren().add(getAxisLabel());
            setCursor(Cursor.DEFAULT);
            prefHeightProperty().bind(layer.getFirstLayer().getView().prefHeightProperty());
            requestLayout();
        }

        @Override
        public final double computePrefWidth(double h) {
            double w = Double.NEGATIVE_INFINITY;
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
            return Math.max(w, 20);
        }

        @Override
        public void layoutChildren() {
            getLine().get();
            computeValue();
            if (getLayer().isLeftAxisLabelled()) {
                if (getTickLabels().size() > 0) {
                    double p = getLine().get().getBoundsInParent().getMinX();
                    getChildren().stream().filter(x -> x instanceof TickLabel).forEach((Node x) -> {
                        TickLabel text = (TickLabel) x;
                        //text.setFont(getFont());
                        text.setLayoutX(p - text.getBoundsInParent().getWidth());
                        text.setLayoutY(text.getYpos());
                    });
                }
                addAxisLabel();
                getAxisLabel().setFont(getFont());
                getAxisLabel().setLayoutY(getHeight() / 2d);
                getAxisLabel().setLayoutX(findMinX() - (getAxisLabel().prefWidth(0) / 2d) - 5d);
            } else {
                getTickLabels().stream().forEach(x -> getChildren().remove(x));
                removeAxisLabel();
            }
            super.layoutChildren();
        }

        private double findMinX() {
            double minx = Double.POSITIVE_INFINITY;
            for (Text text : getTickLabels()) {
                minx = Math.min(minx, text.prefWidth(-1d));
            }
            return minx;
        }

        private void computeValue() {
            getTickLabels().stream().forEach((TickLabel x) -> {
                getChildren().remove(x);
            });

            if (getLayer().isBottomAxisLabelled()) {
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
                                text.setFont(getFont());
                                text.setFill(getLayer().getAxisColor());
                                text.setYpos(p1.getY());
                                getChildren().add(text);
                            }
                        });
            }
        }

    }