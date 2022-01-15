package org.openjump.advancedtools.annotation;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import org.openjump.advancedtools.CadExtension;

import com.vividsolutions.jump.I18N;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerAdapter;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.SystemLayerFinder;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;

public class TextFinder extends SystemLayerFinder {

    private static final I18N i18n = CadExtension.I18N;

    // public static final int WIDTH = 20;
    // public static final int HEIGHT = 20;
    public static final String NAME = i18n
        .get("org.openjump.core.ui.plugins.annotation");
    public static final String LAYER = "LAYER";
    public static final String TEXT = "TEXT";
    public static final String TEXT_HEIGHT = "TEXT_HEIGHT";
    public static final String TEXT_ROTATION = "TEXT_ROTATION";
    public static final String TEXT_STYLE = "TEXT_STYLE";
    public static final String GEOMETRY = "GEOMETRY";
    private double rotation = 0;
    private double dimension = 14;
    // private JTextField myTextArea = createTextArea();

    // private Layer layer;
    private static Layer textLayer = null;

    public TextFinder(LayerManagerProxy layerManagerProxy) {
        super(NAME, layerManagerProxy);
    }

    @Override
    protected void applyStyles(Layer layer) {

        // // TRASPARENZA /////
        layer.getBasicStyle().setAlpha(255);

        // / ETICHETTE ///
        LabelStyle labelStyle = layer.getLabelStyle();
        labelStyle.setEnabled(true);
        labelStyle.setAttribute("TEXT");
        labelStyle.setColor(Color.black);

        labelStyle.setAngleAttribute("TEXT_ROTATION");

        labelStyle.setHeightAttribute("TEXT_HEIGHT");
        labelStyle.setVerticalAlignment(LabelStyle.ABOVE_LINE);
        labelStyle.setHorizontalPosition(LabelStyle.RIGHT_SIDE);
        labelStyle.setScaling(true);
        labelStyle.setHidingOverlappingLabels(false);
        labelStyle.setHideAtScale(false);
        // labelStyle.setHorizontalAlignment(1);

        // ColorScheme colorScheme = ColorScheme.create(colorSchemeName);
        // /layer.setName("(" + colorScheme.getColors().size() + ") " +
        // colorSchemeName);
        Map<Object, BasicStyle> attributeToStyleMap = new HashMap<>();
        // ColorScheme colorScheme = ColorScheme.create(colorSchemeName);
        layer.getBasicStyle().setEnabled(false);
        ColorThemingStyle themeStyle = new ColorThemingStyle("COLOR",
                attributeToStyleMap, new BasicStyle(Color.white));
        themeStyle.setEnabled(true);
        layer.addStyle(themeStyle);
        layer.getBasicStyle().setLineColor(Color.white);

    }

    public static JTextField createTextArea() {
        return new JTextField() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            {
                Integer size = (Integer) TextPanel.textDimensionSpinner
                        .getValue();
                Font font = new Font("Arial", Font.PLAIN, size);
                setForeground(Color.GRAY);
                setFont(font);
                setBorder(BorderFactory.createEmptyBorder());
            }
        };
    }

    public Layer getTextLayer() {
        if (getLayer() == null) {
            initLayer();
        }
        return textLayer;
    }

    /**
     * Initialises the Layer.
     */
    private void initLayer() {
        // textLayer = createLayer();
        textLayer.setSelectable(false);
        // Add a LayerListener to compute the FeatureAttributes, if a Feature
        // was changed
        textLayer.getLayerManager().addLayerListener(new LayerAdapter() {
            @Override
            public void featuresChanged(FeatureEvent e) {
                super.featuresChanged(e);
                // we are only interested on the measureLayer
                if (e.getLayer().equals(textLayer)) {
                    Collection<Feature> features = e.getFeatures();
                    for (Feature feature : features) {
                        feature.setAttribute(LAYER, "TEXT");
                        // feature.setAttribute(TEXT, new
                        // Double(myTextArea.setText(f.getString(TEXT))));
                        feature.setAttribute(TEXT_HEIGHT, rotation);
                        feature.setAttribute(TEXT_ROTATION, dimension);
                        feature.setAttribute(TEXT_STYLE, "STANDARD");
                        feature.setAttribute(GEOMETRY, feature.getGeometry()
                                .getNumPoints());
                    }
                }
            }

        });

        textLayer.getFeatureCollectionWrapper().getFeatureSchema()
                .addAttribute(LAYER, AttributeType.STRING);
        textLayer.getFeatureCollectionWrapper().getFeatureSchema()
                .addAttribute(TEXT, AttributeType.STRING);
        textLayer.getFeatureCollectionWrapper().getFeatureSchema()
                .addAttribute(TEXT_HEIGHT, AttributeType.DOUBLE);
        textLayer.getFeatureCollectionWrapper().getFeatureSchema()
                .addAttribute(TEXT_ROTATION, AttributeType.DOUBLE);
        textLayer.getFeatureCollectionWrapper().getFeatureSchema()
                .addAttribute(TEXT_STYLE, AttributeType.STRING);
        textLayer.getFeatureCollectionWrapper().getFeatureSchema()
                .addAttribute(GEOMETRY, AttributeType.GEOMETRY);

    }

    public static FeatureSchema createFeatureSchema() {
        return new FeatureSchema() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            {
                // addAttribute(CREATED, AttributeType.DATE);
                // addAttribute(MODIFIED, AttributeType.DATE);
                addAttribute(LAYER, AttributeType.STRING);
                addAttribute(TEXT, AttributeType.STRING);
                addAttribute(TEXT_HEIGHT, AttributeType.INTEGER);
                addAttribute(TEXT_ROTATION, AttributeType.INTEGER);
                addAttribute(TEXT_STYLE, AttributeType.STRING);
                addAttribute(GEOMETRY, AttributeType.GEOMETRY);
            }
        };
    }

}
