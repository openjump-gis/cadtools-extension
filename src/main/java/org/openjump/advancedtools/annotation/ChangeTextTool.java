package org.openjump.advancedtools.annotation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbenchContext;
import org.openjump.advancedtools.icon.IconLoader;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.snap.SnapToFeaturesPolicy;

public class ChangeTextTool extends NClickTool {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    /** Name of the tool */
    public final static String NAME = i18n
            .get("org.openjump.core.ui.plugins.annotation.ChangeTextTool.name");
    /** Description of the tool */
    public final static String DESCRIPTION = i18n
            .get("org.openjump.core.ui.plugins.annotation.ChangeTextTool.description");

    private final JTextField textArea;
    private Mode mode;

    public ChangeTextTool() {
        super(JUMPWorkbench.getInstance().getContext(), 1);
        this.panel = getPanel();
        getSnapManager().addPolicies(
                Collections.singleton(new SnapToFeaturesPolicy()));
        this.textArea = TextFinder.createTextArea();
        this.textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (ChangeTextTool.this.panelContainsTextArea()) {
                    boolean doit = ChangeTextTool.this.textArea.getText()
                            .trim().length() > 0;
                    if (doit)
                        ChangeTextTool.this.getPanel().getLayerManager()
                                .getUndoableEditReceiver().startReceiving();
                    ChangeTextTool.this.removeTextAreaFromPanel();
                    if (doit)
                        ChangeTextTool.this.getPanel().getLayerManager()
                                .getUndoableEditReceiver().stopReceiving();
                }
            }
        });
    }

    @Override
    public void deactivate() {
        if (panelContainsTextArea()) {
            boolean doit = (textArea.getText().trim().length() > 0);
            if (doit)
                getPanel().getLayerManager().getUndoableEditReceiver()
                        .startReceiving();
            removeTextAreaFromPanel();
            if (doit)
                getPanel().getLayerManager().getUndoableEditReceiver()
                        .stopReceiving();
        }
        super.deactivate();
    }

    @Override
    public Cursor getCursor() {
        return createCursor(IconLoader.icon("textblock/annotation_edit.gif")
                .getImage());
    }

    private final ImageIcon icon = IconLoader.icon("textblock/annotation_edit.png");

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getName() {

        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 200px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += DESCRIPTION + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        if (!check(getWorkbenchContext().createPlugInContext().getCheckFactory()
            .createAtLeastNLayersMustExistCheck(1))) {
            return;
        }

        try {
            Feature noteFeatureAtClick = noteFeature(getModelDestination());

            removeTextAreaFromPanel();

            this.mode = new EditMode(noteFeatureAtClick);
            // this.mode = mode(noteFeatureAtClick, getModelDestination());

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        ChangeTextTool.this
                                .addTextAreaToPanel(ChangeTextTool.this.mode
                                        .location());
                    } catch (NoninvertibleTransformException e) {
                        ChangeTextTool.this.getPanel().getContext()
                                .handleThrowable(e);
                    }
                }
            });
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }

    }

    private Feature noteFeature(Coordinate click) {
        return noteFeature(new Envelope(click, new Coordinate(click.x - 20.0D
                / scale(), click.y + 20.0D / scale())));
    }

    private Feature noteFeature(Envelope envelope) {
        return (Feature) firstOrNull(layer().getFeatureCollectionWrapper()
                .query(envelope));
    }

    private Object firstOrNull(Collection<?> items) {
        return !items.isEmpty() ? items.iterator().next() : null;
    }

    private double scale() {
        return getPanel().getViewport().getScale();
    }

    private void addTextAreaToPanel(Coordinate location)
            throws NoninvertibleTransformException {

        layer().setVisible(true);
        if (getPanel().getLayout() != null) {
            getPanel().setLayout(null);
        }
        this.textArea.setText(this.mode.initialText());
        this.textArea.setBackground(layer().getBasicStyle().getFillColor());
        getPanel().add(this.textArea);

        this.textArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                JTextField textArea = (JTextField) e.getSource();
                int ht = (int) TextPanel.textDimensionSpinner.getValue();// textArea.getPreferredSize().height;
                int wt = textArea.getPreferredSize().width;
                if (ht < 20)
                    ht = (int) TextPanel.textDimensionSpinner.getValue() + 2;
                if (wt < 20)
                    wt = 20;
                int x = textArea.getBounds().x;
                int y = textArea.getBounds().y;
                textArea.setBounds(x, y, wt, ht);
            }
        });
        int ht = (int) TextPanel.textDimensionSpinner.getValue();// ;this.textArea.getPreferredSize().height;
        int wt = this.textArea.getPreferredSize().width;
        if (ht < 20)
            ht = (int) TextPanel.textDimensionSpinner.getValue() + 2;
        if (wt < 20)
            wt = 20;
        this.textArea.setBounds(
                (int) getPanel().getViewport().toViewPoint(location).getX(),
                (int) getPanel().getViewport().toViewPoint(location).getY()
                        - ht, wt, ht);
        this.textArea.setBackground(null);
        this.textArea.requestFocus();
    }

    private boolean panelContainsTextArea() {
        return getPanel() != null
                && Arrays.asList(getPanel().getComponents()).contains(textArea);
    }

    private void removeTextAreaFromPanel() {
        if (!panelContainsTextArea()) {
            return;
        } else {
            this.mode.commit(this.textArea.getText().trim());
            getPanel().remove(this.textArea);
            getPanel().superRepaint();
        }
    }

    public Layer layer() {
        Layer noteLayer;
        final WorkbenchContext wbcontext = this.getWorkbench().getContext();
        LayerManager layerManager = getPanel().getLayerManager();
        Layer layer = wbcontext.createPlugInContext().getSelectedLayer(0);
        if (isCad(layer)) {
            noteLayer = layer;
        } else {

            if (layerManager.getLayer(TextFinder.NAME) != null) {
                return layerManager.getLayer(TextFinder.NAME);
            }
            noteLayer = new Layer(TextFinder.NAME, Color.WHITE,
                    new FeatureDataset(TextFinder.createFeatureSchema()),
                    layerManager);

            boolean firingEvents = layerManager.isFiringEvents();
            layerManager.setFiringEvents(false);
            try {
                LabelStyle labelStyle = noteLayer.getLabelStyle();
                noteLayer.getBasicStyle().setAlpha(100);
                labelStyle.setEnabled(true);
                labelStyle.setAttribute("TEXT");
                labelStyle.setColor(Color.black);
                labelStyle.setAngleAttribute("TEXT_ROTATION");

                labelStyle.setHeightAttribute("TEXT_HEIGHT");
                labelStyle.setVerticalAlignment("ABOVE_LINE");
                labelStyle.setHorizontalPosition("RIGHT_SIDE");
                labelStyle.setScaling(false);
                labelStyle.setHidingOverlappingLabels(false);
                labelStyle.setHideAtScale(false);
                labelStyle.setHorizontalAlignment(1);
            } finally {
                layerManager.setFiringEvents(firingEvents);
            }
            layerManager.addLayer(i18n
                    .get("org.openjump.core.ui.plugins.annotation.annotation-layer"),
                    noteLayer);
        }
        return noteLayer;

    }

    /**
     * Layer.class
     * 
     * @return true Check if the layer is a cad or annotation Layer following
     *         DXF PlugIn schema it defines Cad layer with the presence of COLOR
     *         and TEXT attributes
     */
    public static boolean isCad(Layer layer) {
        if (layer.getFeatureCollectionWrapper().getFeatureSchema()
                .hasAttribute("TEXT")
                && layer.getFeatureCollectionWrapper().getFeatureSchema()
                        .hasAttribute("COLOR")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        return null;
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil.prepare(new ChangeTextTool(), true);
    }

    private class EditMode extends ChangeTextTool.Mode {
        public EditMode(Feature noteFeature) {
            super(noteFeature);
        }

        @Override
        public void commit(final String text) {
            final String oldText = getNoteFeature().getString("TEXT");
            ChangeTextTool.this.execute(new UndoableCommand(getName()) {
                @Override
                public void execute() {

                    ChangeTextTool.EditMode.this.update(
                            ChangeTextTool.EditMode.this.getNoteFeature(),
                            text, ChangeTextTool.this.layer());

                }

                @Override
                public void unexecute() {
                    ChangeTextTool.EditMode.this.update(
                            ChangeTextTool.EditMode.this.getNoteFeature(),
                            oldText, ChangeTextTool.this.layer());
                }
            });
        }

        private void update(Feature noteFeature, String text, Layer layer) {

            noteFeature.setAttribute("TEXT", text);
            noteFeature.setAttribute("TEXT_HEIGHT",
                    TextPanel.textDimensionSpinner.getValue());
            Integer cadvalue = 360 - (Integer) TextPanel.textRotationSpinner
                    .getValue();

            noteFeature.setAttribute("TEXT_ROTATION", cadvalue);

            layer.getLayerManager().fireFeaturesChanged(
                    Collections.singleton(noteFeature),
                    FeatureEventType.ATTRIBUTES_MODIFIED, layer);
        }

        @Override
        public String initialText() {
            return getNoteFeature().getString("TEXT");
        }
    }

    private abstract class Mode {

        private final Feature noteFeature;

        public String getName() {
            return i18n
                .get("org.openjump.core.ui.plugins.annotation.annotation-layer");
        }

        public Mode(Feature noteFeature) {
            this.noteFeature = noteFeature;
        }

        public Coordinate location() {
            return this.noteFeature.getGeometry().getCoordinate();
        }

        public abstract void commit(String paramString);

        protected Feature getNoteFeature() {
            return this.noteFeature;
        }

        public abstract String initialText();
    }

    public static EnableCheck createEnableCheck(
            WorkbenchContext workbenchContext, boolean b) {
        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();

        return new MultiEnableCheck().add(
                checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck())
                .add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
    }

}