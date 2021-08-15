package org.openjump.advancedtools.block;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openjump.advancedtools.icon.IconLoader;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.TaskFrame;

public class SaveBlockPlugIn extends AbstractPlugIn {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    public static ImageIcon ICON = IconLoader.icon("textblock/block_save.png");

    private static final String NAME = i18n
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn");
    private static final String Warning1 = i18n
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.description-title");
    private static final String Warning2 = i18n
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.warning-positive");
    private static final String Warning3 = i18n
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.warning-negative");
    private static final String Description = i18n
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.description");

    private final BlockPanel blockPanel;
    private final JTextField jTextField_wktOut;


    private String blockFolder = "VertexImages";
    private Geometry geomSelected = null;

    public SaveBlockPlugIn(BlockPanel blockPanel) {
        super();
        this.blockPanel = blockPanel;
        this.jTextField_wktOut =  new JTextField();
    }

    @Override
    public String getName() {
        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 320px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += Warning1 + "<br>";
        tooltip += Description + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    public ImageIcon getIcon() {
        return ICON;
    }

    @Override
    public void initialize(PlugInContext context) {

    }

    public static MultiEnableCheck createEnableCheck(
            WorkbenchContext workbenchContext) {

        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();

        return new MultiEnableCheck().add(
                checkFactory
                        .createWindowWithSelectionManagerMustBeActiveCheck())
                .add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1));
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);

        if (!(JUMPWorkbench.getInstance().getFrame().getActiveInternalFrame() instanceof TaskFrame)) {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                            I18N.JUMP.get("com.vividsolutions.jump.workbench.plugin.A-Task-Window-must-be-active"));
            return false;
        } else {
            try {
                Collection<Feature> selectedFeatures = context.getLayerViewPanel()
                        .getSelectionManager()
                        .createFeaturesFromSelectedItems();
                if (selectedFeatures.size() > 1 || selectedFeatures.size() == 0) {
                    context.getWorkbenchFrame().warnUser(Warning1 + "!");
                    return false;
                }
                File pluginDir = context.getWorkbenchContext().getWorkbench()
                        .getPlugInManager().getPlugInDirectory();
                String wd = pluginDir.getAbsolutePath();
                String filenamedir;
                MultiInputDialog dialog = new MultiInputDialog(
                        context.getWorkbenchFrame(), NAME, true);

                dialog.addRow(createMainPanel());

                GUIUtil.centreOnWindow(dialog);
                dialog.setVisible(true);

                if (!dialog.wasOKPressed()) {
                    return false;
                }
                if (jTextField_wktOut.getText().isEmpty()) {
                    context.getWorkbenchFrame().warnUser(Warning3);
                    return false;
                }

                else {

                    String filename = jTextField_wktOut.getText();

                    for (Feature featureSelected : selectedFeatures) {
                        geomSelected = featureSelected.getGeometry();
                    }

                    //   Geometry targetGeom = new GeometryFactory()
                    //           .createLineString(new Coordinate[] {
                    //                   new Coordinate(0, 0),
                    //                   new Coordinate(0, 8.49) });
                    //    double targetLength = targetGeom.getLength();
                    //   double scale = 0;

                    //    Envelope envSelected = geomSelected.getEnvelopeInternal();

                    //     Geometry sourceGeom = new GeometryFactory()
                    //             .createLineString(new Coordinate[] {
                    //                     new Coordinate(envSelected.getMinX(),
                    //                             envSelected.getMinY()),
                    //                     new Coordinate(envSelected.getMaxY(),
                    //                             envSelected.getMaxY()) });
                    //   double sourceLength = sourceGeom.getLength();

                    /*
                     * if (sourceLength>22){ scale =
                     * (targetLength/sourceLength)*100; } else{ scale =
                     * (22/sourceLength)*100; }
                     */
                    Geometry newGeom = geomSelected.copy();
                    // Get the centroid coordinates of the geometry
                    Coordinate coord = newGeom.getEnvelope().getCentroid()
                            .getCoordinate();

                    // Calculate the displacement of the geometry
                    Coordinate displacement = CoordUtil.subtract(
                            new Coordinate(0, 0), coord);

                    //   scale = (targetLength / sourceLength);

                    GeometryUtils.centerGeometry(newGeom, displacement);
                    //   GeometryUtils.scaleGeometry(newGeom, scale);

                    filenamedir = wd + File.separator + blockFolder
                            + File.separator + filename;
                    File f = new File(filenamedir);
                    if (f.exists() && f.isDirectory()) {
                        GeometryUtils.writeToFile(newGeom, filenamedir);
                    } else {
                        File dir = new File(wd + File.separator + blockFolder);
                        dir.mkdir();
                        filenamedir = dir.getAbsolutePath() + File.separator
                                + filename;
                        GeometryUtils.writeToFile(newGeom, filenamedir);
                    }
                	if (checkSymbolLibrary()) {
						com.cadplan.jump.utils.LoadSymbolFiles loadSymbols = new com.cadplan.jump.utils.LoadSymbolFiles(context);
						loadSymbols.start();
					}
                    context.getWorkbenchFrame().setStatusMessage(Warning2);

                    if (blockPanel != null) {
                        blockPanel.initComboBox(context);
                    }
                }


            } catch (Exception ex) {
                Logger.warn(ex);
            }
            return true;
        }
    }

  boolean checkSymbolLibrary() {
		ClassLoader cl = this.getClass().getClassLoader();
		Class<?> c = null;
		try {
			c = cl.loadClass("com.cadplan.jump.utils.LoadSymbolFiles");
		}
		catch (ClassNotFoundException e) {
			Logger.warn("Could not load Symbol file library", e);
		}

		return c != null;
	}
    
    
    private JPanel createMainPanel() {
        JPanel jpanel = new JPanel(new GridBagLayout());
        JLabel jLabel = new JLabel();
        String OUTPUT_FILE = i18n
            .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.dialog");
        jLabel.setText(OUTPUT_FILE);
        jTextField_wktOut.setEditable(true);
        jTextField_wktOut.setPreferredSize(new Dimension(250, 20));
        FormUtils.addRowInGBL(jpanel, 3, 0, OUTPUT_FILE, jTextField_wktOut);
        return jpanel;
    }

}