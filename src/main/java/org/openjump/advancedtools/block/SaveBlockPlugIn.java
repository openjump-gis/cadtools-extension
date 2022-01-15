package org.openjump.advancedtools.block;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.icon.IconLoader;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

import com.cadplan.vertex_symbols.jump.VertexSymbolsExtension;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class SaveBlockPlugIn extends AbstractPlugIn {

    public static ImageIcon ICON = IconLoader.icon("textblock/block_save.png");

    private static final String NAME = CadExtension.I18N
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn");
    private static final String Warning1 = CadExtension.I18N
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.description-title");
    private static final String Warning2 = CadExtension.I18N
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.warning-positive");
    private static final String Warning3 = CadExtension.I18N
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.warning-negative");
    private static final String Description = CadExtension.I18N
        .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.description");

    private final BlockPanel blockPanel;
    private final JTextField jTextField_wktOut;


    private String blockFolderName = "VertexImages";
    private File blockFolderPath = null;
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
    public void initialize(PlugInContext context) throws Exception {
      super.initialize(context);
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
      String msg;
      if ((msg = getEnableCheck().check(null)) != null) {
        context.getWorkbenchFrame().warnUser(msg);
        return false;
      }

      reportNothingToUndoYet(context);

      Collection<Feature> selectedFeatures = context.getLayerViewPanel().getSelectionManager()
          .createFeaturesFromSelectedItems();
      if (selectedFeatures.size() > 1 || selectedFeatures.size() == 0) {
        context.getWorkbenchFrame().warnUser(Warning1 + "!");
        return false;
      }

      MultiInputDialog dialog = new MultiInputDialog(context.getWorkbenchFrame(), NAME, true);

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

      String filename = jTextField_wktOut.getText();

      for (Feature featureSelected : selectedFeatures) {
        geomSelected = featureSelected.getGeometry();
      }

      // Geometry targetGeom = new GeometryFactory()
      // .createLineString(new Coordinate[] {
      // new Coordinate(0, 0),
      // new Coordinate(0, 8.49) });
      // double targetLength = targetGeom.getLength();
      // double scale = 0;

      // Envelope envSelected = geomSelected.getEnvelopeInternal();

      // Geometry sourceGeom = new GeometryFactory()
      // .createLineString(new Coordinate[] {
      // new Coordinate(envSelected.getMinX(),
      // envSelected.getMinY()),
      // new Coordinate(envSelected.getMaxY(),
      // envSelected.getMaxY()) });
      // double sourceLength = sourceGeom.getLength();

      /*
       * if (sourceLength>22){ scale =
       * (targetLength/sourceLength)*100; } else{ scale =
       * (22/sourceLength)*100; }
       */
      Geometry newGeom = geomSelected.copy();
      // Get the centroid coordinates of the geometry
      Coordinate coord = newGeom.getEnvelope().getCentroid().getCoordinate();

      // Calculate the displacement of the geometry
      Coordinate displacement = CoordUtil.subtract(new Coordinate(0, 0), coord);

      // scale = (targetLength / sourceLength);

      GeometryUtils.centerGeometry(newGeom, displacement);
      // GeometryUtils.scaleGeometry(newGeom, scale);

      // TODO: use reflection? remove dependency from pom?
      File blockFolder = VertexSymbolsExtension.getVertexImagesFolder(context.getWorkbenchContext());
      // String filenamedir = wd + File.separator + blockFolder
      // + File.separator + filename;
      // File f = new File(filenamedir);
      if (blockFolder.isDirectory()) {
        GeometryUtils.writeToFile(newGeom, new File( blockFolder, filename ).getAbsolutePath());
      }

      // only add if Vertex-Symbols is installed
      Class<?> c = checkSymbolLibrary();
      if (c != null) {
        // the same code as below, but using reflection
        Object loadSymbols = c.getConstructor(PlugInContext.class).newInstance(context);
        Method m = c.getMethod("start");
        m.invoke(loadSymbols, null);
        // LoadSymbolFiles loadSymbols = new LoadSymbolFiles(context);
        // loadSymbols.start();
      }
      context.getWorkbenchFrame().setStatusMessage(Warning2);

      if (blockPanel != null) {
        blockPanel.initComboBox(context);
      }

      return true;
    }

    private Class checkSymbolLibrary() {
      ClassLoader cl = this.getClass().getClassLoader();
      Class<?> c = null;
      try {
        c = cl.loadClass("com.cadplan.vertex_symbols.jump.utils.LoadSymbolFiles");
      } catch (ClassNotFoundException e) {
        Logger.warn("Could not load Symbol file library", e);
      }

      return c;
    }

    private JPanel createMainPanel() {
        JPanel jpanel = new JPanel(new GridBagLayout());
        JLabel jLabel = new JLabel();
        String OUTPUT_FILE = CadExtension.I18N
            .get("org.openjump.core.ui.plugins.block.SaveBlockPlugIn.dialog");
        jLabel.setText(OUTPUT_FILE);
        jTextField_wktOut.setEditable(true);
        jTextField_wktOut.setPreferredSize(new Dimension(250, 20));
        FormUtils.addRowInGBL(jpanel, 3, 0, OUTPUT_FILE, jTextField_wktOut);
        return jpanel;
    }

    @Override
    public EnableCheck getEnableCheck() {
      EnableCheckFactory factory = EnableCheckFactory.getInstance(getWorkbenchContext());
      return new MultiEnableCheck().add(factory.createTaskWindowMustBeActiveCheck()).add(new EnableCheck() {
        @Override
        public String check(JComponent component) {
          if (checkSymbolLibrary()==null)
            return CadExtension.I18N.get("org.openjump.advancedtools.vertex-symbols.missing");
          return null;
        }
      });
    }

}