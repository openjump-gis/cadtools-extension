package org.openjump.advancedtools.tools;

import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.Icon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.icon.IconLoader;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

import es.kosmo.desktop.tools.algorithms.BezierCurve;

/**
 * Tools that allows draw a bezier curve Original code from Kosmo 3.0 SAIG -
 * http://www.opengis.es/
 * 
 * @author Gabriel Bellido Perez - gbp@saig.es
 * @since Kosmo SAIG 1.2
 * @author Giuseppe Aruta rewrite code to adapt to OpenJUMP 1.10
 *         (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10
 */

public class CuadraticBezierCurveTool extends ConstrainedNClickTool {

    private static final I18N i18n = CadExtension.I18N;

    private final FeatureDrawingUtil featureDrawingUtil;
    //protected SnapIndicatorTool snapIndicatorTool;
    private final BezierCurve bezierCurve = new BezierCurve();

    public CuadraticBezierCurveTool(FeatureDrawingUtil featureDrawingUtil) {
        super(JUMPWorkbench.getInstance().getContext(), 3);
        allowSnapping();
        this.featureDrawingUtil = featureDrawingUtil;
    }

    /** Nombre asociado a la herramienta */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.plugins.DrawCuadraticBezierCurveTool");
    /** Nombre asociado a la herramienta */
    public final static String NAME2 = i18n
        .get("org.openjump.core.ui.plugins.DrawCuadraticBezierCurveTool.description");

    @Override
    public Cursor getCursor() {
        return new Cursor(Cursor.CROSSHAIR_CURSOR);
        // return createCursor(IconLoader.icon("cross_cursor.gif").getImage());
    }

    @Override
    public Icon getIcon() {
        return GUIUtil.resize(IconLoader.icon("drawCurve.png"), 20);
    }

    @Override
    public String getName() {

        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 200px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += NAME2 + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    // public Cursor getCursor() { return
    // createCursor(IconLoader.icon("draw_curve.png").getImage()); }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil.prepare(new CuadraticBezierCurveTool(
                featureDrawingUtil), true);
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        if (!checkLineString()) {
            return;
        }
        this.featureDrawingUtil.drawLineString(getCurve(),
                isRollingBackInvalidEdits(), this, getPanel());
    }

    protected boolean checkLineString() {
        if (getCoordinates().size() < 3) {
            getPanel()
                    .getContext()
                    .setStatusMessage(
                            "Drag between start and end points, than click on mid point");

            return false;
        }

        IsValidOp isValidOp = new IsValidOp(getCurve());

        if (!isValidOp.isValid()) {
            getPanel().getContext().warnUser(
                    isValidOp.getValidationError().getMessage());

            if (getWorkbench().getBlackboard().get(
                    EditTransaction.ROLLING_BACK_INVALID_EDITS_KEY, false)) {
                return false;
            }
        }

        return true;
    }

    protected LineString getCurve() {
        if (this.coordinates.size() == 0) {
            return null;
        }
        if (this.coordinates.size() == 1) {
            return this.bezierCurve.calculateCuadraticBezier(
                    (Coordinate) this.coordinates.get(0), new Coordinate(0.0D,
                            0.0D), snap(this.tentativeCoordinate), 1);
        }
        if (this.coordinates.size() == 2) {
            return this.bezierCurve.calculateCuadraticBezier(
                    (Coordinate) this.coordinates.get(0),

                    snap(this.tentativeCoordinate),
                    (Coordinate) this.coordinates.get(1), 30);
        }
        if (this.coordinates.size() == 3) {
            return this.bezierCurve.calculateCuadraticBezier(
                    (Coordinate) this.coordinates.get(0),

                    (Coordinate) this.coordinates.get(2),
                    (Coordinate) this.coordinates.get(1), 30);
        }

        return null;
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        if (this.coordinates.size() == 0) {
            return null;
        }
        LineString curve = getCurve();
        return getPanel().getJava2DConverter().toShape(curve);
    }

    public static MultiEnableCheck createEnableCheck(
            WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();
        MultiEnableCheck check = new MultiEnableCheck();

        check.add(checkFactory.createTaskWindowMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));

        return check;
    }
}
