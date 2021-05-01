package org.openjump.advancedtools.annotation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.language.I18NPlug;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.snap.SnapToFeaturesPolicy;

public class AddTextTool extends NClickTool {

    /** Name of the tool */
    public final static String NAME = I18NPlug
            .getI18N("org.openjump.core.ui.plugins.annotation.AddTextTool.name");
    /** Description of the tool */
    public final static String DESCRIPTION = I18NPlug
            .getI18N("org.openjump.core.ui.plugins.annotation.AddTextTool.description");

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

    private final JTextField textArea;
    private Mode mode;

    private final Cursor addtxtcursor = createCursor(new ImageIcon(IconLoader.icon(
            "textblock/annotation_add.gif").getImage()).getImage());

    private final Cursor modifytxtcursor = createCursor(new ImageIcon(IconLoader
            .icon("textblock/annotation_edit.gif").getImage()).getImage());

    private final KeyListener cursorSwitcher = new KeyListener() {
        boolean shift = false;

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            shift = e.isShiftDown() && e.getKeyCode() != KeyEvent.VK_SHIFT;
            setCursor();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            shift = e.isShiftDown() || e.getKeyCode() == KeyEvent.VK_SHIFT;
            setCursor();
        }

        private void setCursor() {
            JPanel panel = getPanel();
            // System.out.println("rsi "+shift);
            if (panel != null) {
                panel.setCursor(shift ? modifytxtcursor : addtxtcursor);
            }
        }
    };

    public AddTextTool() {
        super(1);
        this.panel = getPanel();
        getSnapManager().addPolicies(
                Collections.singleton(new SnapToFeaturesPolicy()));
        this.textArea = TextFinder.createTextArea();
        this.textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (AddTextTool.this.panelContainsTextArea()) {
                    boolean doit = AddTextTool.this.textArea.getText().trim()
                            .length() > 0;
                    if (doit)
                        AddTextTool.this.getPanel().getLayerManager()
                                .getUndoableEditReceiver().startReceiving();
                    AddTextTool.this.removeTextAreaFromPanel();
                    if (doit)
                        AddTextTool.this.getPanel().getLayerManager()
                                .getUndoableEditReceiver().stopReceiving();
                }
            }
        });
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {

        super.activate(layerViewPanel);
        // System.out.println("rsi register listener " + cursorSwitcher);
        JUMPWorkbench.getInstance().getFrame()
                .addEasyKeyListener(cursorSwitcher);
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
        JUMPWorkbench.getInstance().getFrame()
                .removeEasyKeyListener(cursorSwitcher);
    }

    @Override
    public Cursor getCursor() {
        return addtxtcursor;
    }

    private final ImageIcon icon = IconLoader.icon("textblock/annotation_add.png");

    @Override
    public Icon getIcon() {
        return icon;
    }

    EnableCheckFactory checkFactory = new EnableCheckFactory(JUMPWorkbench
            .getInstance().getContext());

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();

        if (!check(checkFactory.createAtLeastNLayersMustExistCheck(1))) {
            return;
        }

        removeTextAreaFromPanel();

        this.mode = new CreateMode(getModelDestination());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {

                    AddTextTool.this.addTextAreaToPanel(AddTextTool.this.mode
                            .location());
                } catch (NoninvertibleTransformException e) {
                    AddTextTool.this.getPanel().getContext().handleThrowable(e);
                }
            }
        });
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
        if (!panelContainsTextArea())
            return;
        this.mode.commit(this.textArea.getText().trim());
        getPanel().remove(this.textArea);
        getPanel().superRepaint();
    }

    private void disableAutomaticInitialZooming() {
        getPanel().setViewportInitialized(true);
    }

    public Layer layer() {
        LayerManager layerManager = getPanel().getLayerManager();
        if (layerManager.getLayers().isEmpty()) {

        }
        Layer noteLayer;
        final WorkbenchContext wbcontext = this.getWorkbench().getContext();
        // LayerManager layerManager = getPanel().getLayerManager();
        Layer layer = wbcontext.createPlugInContext().getSelectedLayer(0);
        if (isCad(layer)) {
            noteLayer = layer;
        } else if (layerManager.getLayer(TextFinder.NAME) != null) {
            noteLayer = layerManager.getLayer(TextFinder.NAME);
        } else {

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
            layerManager
                    .addLayer(
                            I18NPlug.getI18N("org.openjump.core.ui.plugins.annotation.annotation-layer"),
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

        return featureDrawingUtil.prepare(new AddTextTool(), true);
    }

    private class CreateMode extends Mode {

        public CreateMode(final Coordinate location) {
            super(new BasicFeature(layer().getFeatureCollectionWrapper()
                    .getFeatureSchema()) {

                private static final long serialVersionUID = 1L;

                {
                    setAttribute(TextFinder.GEOMETRY,
                            new GeometryFactory().createPoint(location));
                }
            });
        }

        @Override
        public void commit(String text) {
            if (text.length() > 0) {
                AddTextTool.this.disableAutomaticInitialZooming();

                getNoteFeature().setAttribute("LAYER", null);

                getNoteFeature().setAttribute("TEXT", text);
                getNoteFeature().setAttribute("TEXT_HEIGHT",
                        TextPanel.textDimensionSpinner.getValue());
                Integer cadvalue = 360 - (Integer) TextPanel.textRotationSpinner
                        .getValue();

                getNoteFeature().setAttribute("TEXT_ROTATION", cadvalue);

                getNoteFeature().setAttribute("TEXT_STYLE", "STANDARD");

                EditTransaction transaction = new EditTransaction(
                        Collections.EMPTY_LIST, getName(),
                        AddTextTool.this.layer(),
                        AddTextTool.this.isRollingBackInvalidEdits(), true,
                        AddTextTool.this.getPanel());
                transaction.createFeature(getNoteFeature());
                transaction.commit();
            }
        }

        @Override
        public String initialText() {
            return "";
        }
    }

    private abstract class Mode {

        private final Feature noteFeature;

        public String getName() {
            return I18NPlug
                    .getI18N("org.openjump.core.ui.plugins.annotation.annotation-layer");
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
        EnableCheckFactory checkFactory = new EnableCheckFactory(
                workbenchContext);

        return new MultiEnableCheck().add(
                checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck())
                .add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
    }

}