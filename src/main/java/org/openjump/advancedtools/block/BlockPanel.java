package org.openjump.advancedtools.block;

import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
//import java.awt.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.locationtech.jts.geom.Geometry;
import org.openjump.advancedtools.CadExtension;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;

public class BlockPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    public static final String blockFolder = "VertexImages";
    public static final String NAME = "Insert symbol as block";

    public static JComboBox<BlockCell> chooseBox = new JComboBox<>();

 // TODO: language strings are located in OJ core. Why?
    public static String circle = I18N.JUMP
            .get("deejump.ui.style.RenderingStylePanel.circle");
    public static String triangle = I18N.JUMP
            .get("deejump.ui.style.RenderingStylePanel.triangle");
    public static String square = I18N.JUMP
            .get("deejump.ui.style.RenderingStylePanel.square");
    public static String cross = I18N.JUMP
            .get("deejump.ui.style.RenderingStylePanel.cross");
    public static String star = I18N.JUMP
            .get("deejump.ui.style.RenderingStylePanel.star");

    private SpinnerModel rotationModel = new SpinnerNumberModel(0, // initial
            // value
            0,   // min
            359, // max
            1);

    private SpinnerModel dimensionModel = new SpinnerNumberModel(100, // initial
            // value
            10, // min
            1000, // max
            10);

    public static JSpinner rotationSpinner = new JSpinner();

    public static JSpinner dimensionSpinner = new JSpinner();

    private static WorkbenchToolBar toolBar = new WorkbenchToolBar(null) {

        private static final long serialVersionUID = 1L;

        @Override
        public JButton addPlugIn(Icon icon, PlugIn plugIn,
                                 EnableCheck enableCheck, WorkbenchContext workbenchContext) {
            return super.addPlugIn(icon, plugIn, enableCheck, workbenchContext);
        }

    };

    @SuppressWarnings({})
    public JPanel panel_blocks(PlugInContext context) throws Exception {
        final JPanel mainPanel = new JPanel(new GridBagLayout());

        final Dimension d = new Dimension(140, /*
                                                * chooseBox.getPreferredSize().
                                                * height
                                                */28);
        chooseBox
            .setToolTipText(CadExtension.I18N
                .get("org.openjump.core.ui.plugins.block.dialog.select-block"));
        chooseBox.setPreferredSize(d);

        chooseBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {

                final JLabel label = (JLabel) super
                        .getListCellRendererComponent(list, value, index,
                                isSelected, cellHasFocus);
                if (/*-1 < index && null != value &&*/value instanceof BlockCell) {
                    label.setIcon(((BlockCell) value).getIcon());
                }
                return label;
            }
        });
        initComboBox(context);
        initToolBar(context);
        // setBlockChooserPanel(context);

        dimensionSpinner = new JSpinner(dimensionModel);
        final JLabel labelDimension = new JLabel(
            CadExtension.I18N.get("org.openjump.core.ui.plugins.block.dialog-dimension"));
        labelDimension.setToolTipText(gedDimensionTooltip());

        rotationSpinner = new JSpinner(rotationModel);
        final JLabel labelRotation = new JLabel(
            CadExtension.I18N.get("org.openjump.core.ui.plugins.block.dialog-rotation"));
        labelRotation
            .setToolTipText(CadExtension.I18N
                .get("org.openjump.core.ui.plugins.annotation.dialog.font-rotation-message"));

        final TitledBorder titledBorder = new TitledBorder(
                BorderFactory.createEtchedBorder(Color.white, new Color(148,
                        145, 140)),
                CadExtension.I18N.get("org.openjump.core.ui.plugins.block"));
        mainPanel.setBorder(titledBorder);

        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = WEST;
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        // FormUtils.addRowInGBL(mainPanel, 0, 0, toolBar);
        mainPanel.add(toolBar, c);

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        mainPanel.add(chooseBox, c);
        // c.gridy = 1;
        // mainPanel.add(blockChooserPanel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        mainPanel.add(labelDimension, c);
        c.gridx = 1;
        mainPanel.add(dimensionSpinner, c);
        c.gridx = 2;
        mainPanel.add(labelRotation, c);
        c.gridx = 3;
        mainPanel.add(rotationSpinner, c);

        return mainPanel;
    }

    @Deprecated
    private JPanel getBlockChooserPanel(PlugInContext context) {

        Dimension d = new Dimension(125, /*chooseBox.getPreferredSize().height*/ 26);
        chooseBox.setPreferredSize(d);

        chooseBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);
                if (/*-1 < index && null != value &&*/ value instanceof BlockCell) {
                    label.setIcon(((BlockCell)value).getIcon());
                }
                return label;
            }
        });
        initComboBox(context);

        JPanel blockChooserPanel = new JPanel();
        JLabel chooseBlockLabel = new JLabel(
            CadExtension.I18N.get("org.openjump.core.ui.plugins.block.dialog.select-block"));
        blockChooserPanel.setToolTipText(
            CadExtension.I18N.get("org.openjump.core.ui.plugins.block.dialog.dialog.select-block-message"));
        blockChooserPanel.add(chooseBlockLabel);
        blockChooserPanel.add(chooseBox);

        return blockChooserPanel;
    }

    private void initToolBar(PlugInContext context) throws Exception {

        DrawBlockPlugIn draw = new DrawBlockPlugIn(this);
        // SaveBlockPlugIn must contain a reference to this BlockPanel
        SaveBlockPlugIn save = new SaveBlockPlugIn(this);
        // ShowHelpPlugIn help = new ShowHelpPlugIn();
        DrawOrientedBlockPlugIn drag = new DrawOrientedBlockPlugIn(this);

        // initialize plugins
        draw.initialize(context);
        save.initialize(context);
        drag.initialize(context);
        // install
        toolBar.addPlugIn(draw.getIcon(), draw, null, context.getWorkbenchContext());
        toolBar.addPlugIn(drag.getIcon(), drag, null, context.getWorkbenchContext());
        toolBar.addPlugIn(save.getIcon(), save, null, context.getWorkbenchContext());
    }

    void initComboBox(PlugInContext context) {
        chooseBox.removeAllItems();
        for (BlockCell block : getAllBlockCells(context)) {
            chooseBox.addItem(block);
        }
        chooseBox.revalidate();
    }

    public Geometry getSelection() {
        return chooseBox.getSelectedItem() == null ? null :
            ((BlockCell)chooseBox.getSelectedItem()).getGeometry().copy();
    }

    private List<BlockCell> getAllBlockCells(PlugInContext context) {
		File pluginDir = context.getWorkbenchContext().getWorkbench()
				.getPlugInManager().getPlugInDirectory();
		File folder = new File(pluginDir,blockFolder);
		List<BlockCell> blocks = new ArrayList<>();
		blocks.add(BlockCell.SQUARE);
		blocks.add(BlockCell.CIRCLE);
		blocks.add(BlockCell.TRIANGLE);
		blocks.add(BlockCell.CROSS);
		blocks.add(BlockCell.STAR);
		try {
			File[] fList = folder.listFiles();
			if (fList != null) {
        Arrays.sort(fList);
				for (File file : fList) {
					if (file.isFile() && file.getName().toUpperCase().endsWith(".WKT")) {
						blocks.add(new BlockCell(file));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return blocks;
	}


    private String gedDimensionTooltip() {

        String dimension   = CadExtension.I18N.get("org.openjump.core.ui.plugins.block.define-dimension");
        String description = CadExtension.I18N.get("org.openjump.core.ui.plugins.block.define-dimension-description");
        String style       = "width: 200px; text-justification: justify;";

        return String.format(
                "<HTML><BODY><DIV style=\"%s\"><b>%s</b><br>%s</DIV></BODY></HTML>",
                style, dimension, description
        );
    }

}