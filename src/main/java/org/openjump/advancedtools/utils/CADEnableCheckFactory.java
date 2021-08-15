package org.openjump.advancedtools.utils;

import java.util.Collection;

import javax.swing.JComponent;

import com.vividsolutions.jump.I18N;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;

public class CADEnableCheckFactory {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    protected WorkbenchContext workbenchContext;

    /**
     *
     */
    public CADEnableCheckFactory(WorkbenchContext workbenchContext) {
        Assert.isTrue(workbenchContext != null);
        this.workbenchContext = workbenchContext;
    }

    /**
     *
     */
    public WorkbenchContext getWorkbenchContext() {
        return workbenchContext;
    }

    /**
     * Check that all the selected features are only from one layer
     * 
     * @param layerName layer  containing selected features
     */
    public EnableCheck createAllSelectedFeaturesAreInLayerCheck(
            final String layerName) {
        return new EnableCheck() {
            @Override
            public String check(JComponent component) {
                Layer layer = JUMPWorkbench.getInstance().getFrame()
                        .getContext().getLayerManager().getLayer(layerName);

                if (layer == null) {
                    return i18n.get(
                        "org.openjump.core.ui.CADEnableCheckFactory.layer-{0}-does-not-exist", //$NON-NLS-1$
                        layerName);
                }

                int numSelectedFeatures = WorkbenchUtils.getSelectedFeatures()
                        .size();
                int numSelectedFeaturesInLayer = WorkbenchUtils
                        .getSelectedFeatures(layer).size();

                if (numSelectedFeatures != numSelectedFeaturesInLayer) {
                    return i18n.get(
                        "org.openjump.core.ui.CADEnableCheckFactory.all-selected-features-must-be-at-the-layer-{0}", //$NON-NLS-1$
                        layerName);
                }

                return null;
            }
        };
    }

    public EnableCheck createLayerIsCADCheck(final String layerName) {
        return new EnableCheck() {
            @Override
            public String check(JComponent component) {
                Layer layer = JUMPWorkbench.getInstance().getFrame()
                        .getContext().getLayerManager().getLayer(layerName);

                if (layer == null) {
                    return i18n.get(
                        "org.openjump.core.ui.CADEnableCheckFactory.layer-{0}-does-not-exist", //$NON-NLS-1$
                        layerName);
                }
                FeatureCollectionWrapper fcw = layer
                        .getFeatureCollectionWrapper();
                FeatureSchema schema = fcw.getFeatureSchema();
                boolean check = false;
                if (schema.hasAttribute("COLOR") && schema.hasAttribute("TEXT")
                        && schema.hasAttribute("TEXT_HEIGHT")
                        && (schema.hasAttribute("TEXT_ROTATION"))) {
                    check = true;
                }
                if (!check) {

                    return i18n.get("org.openjump.core.ui.CADEnableCheckFactory.layer-is-not-CAD");
                }
                return null;
            }
        };
    }

    public static String test = " You should select: ";

    /**
     * Check if the selected collection of selected features are of <n> number
     * and all of selected geometry. Geometry to be considered: POINT,
     * MULTIPOINT, LINESTRING, MULTILINESTRING, POLYGON, MULTIPOLYGON,
     * GEOMETRYCOLLECTION.<This method should be moved to
     * com.vividsolutions.jump.workbench.plugin.EnableCheckFactory>
     * 
     * @param types
     *            Int Type of geometry to include [...new int[]{
     *            FeatureSchema.POLYGON, FeatureSchema.MULTIPOLYGON}...]
     * @param noTypes
     *            Int Type of geometry to exclude [...new
     *            int[]{FeatureSchema.LINESTRING, FeatureSchema.MULTILINESTRING,
     *            FeatureSchema.POINT}...]
     * @param n
     *            number of selected features
     * @return valid and not valid geometries type
     * 
     *         Example:
     * 
     *         GeneralUtils.checkGeometryType(new int[] {
     *         GeneralUtils.FEATURE_SCHEMA_LINESTRING,
     *         GeneralUtils.FEATURE_SCHEMA_LINEARRING }, new int[] {
     *         GeneralUtils.FEATURE_SCHEMA_GEOMETRYCOLLECTION}, 5)
     * 
     *         Result:
     * 
     *         5 features. Valid geometries: LineString and LinearRing. Not
     *         valid geometry: Geometry collection
     */
    public static EnableCheck createExactlyNFeaturesWithGeometryTypeMustBeSelectedCheck(
            final int[] types, final int[] noTypes, final int n) {
        return new EnableCheck() {
            @Override
            public String check(JComponent component) {
                Collection<Feature> features = ((SelectionManagerProxy) JUMPWorkbench
                        .getInstance().getFrame().getActiveInternalFrame())
                        .getSelectionManager().getFeatureSelection()
                        .getFeaturesWithSelectedItems();
                if (n != features.size()) {
                    if (n == 1) {
                        return i18n.get(
                            "org.openjump.core.ui.CADEnableCheckFactory.exactly-a-feature-must-be-selected"); //$NON-NLS-1$
                    } else {
                        return i18n.get(
                            "org.openjump.core.ui.CADEnableCheckFactory.exactly-{0}-features-must-be-selected", //$NON-NLS-1$
                            n);
                    }
                } else {
                    for (Feature element : features) {
                        StringBuilder sb = new StringBuilder();
                        //String selGeo = "";
                        boolean check = false;
                        for (int i = 0; i < types.length && !check; i++) {
                            int type = types[i];
                            if (type == FEATURE_SCHEMA_POINT) {
                                if (element.getGeometry() instanceof Point) {

                                    check = true;
                                }
                                sb.append(" Point");
                            } else if (type == FEATURE_SCHEMA_MULTIPOINT) {
                                if (element.getGeometry() instanceof MultiPoint) {

                                    check = true;
                                }
                                sb.append(" MultiPoint");
                            } else if (type == FEATURE_SCHEMA_POLYGON) {
                                if (element.getGeometry() instanceof Polygon) {

                                    check = true;
                                }
                                sb.append(" Polygon");
                            } else if (type == FEATURE_SCHEMA_MULTIPOLYGON) {
                                if (element.getGeometry() instanceof MultiPolygon) {

                                    check = true;
                                }
                                sb.append(" MultiPolygon");
                            } else if (type == FEATURE_SCHEMA_LINESTRING) {
                                if (element.getGeometry() instanceof LineString) {

                                    check = true;

                                }
                                sb.append("Linestring");
                            } else if (type == FEATURE_SCHEMA_MULTILINESTRING) {
                                if (element.getGeometry() instanceof MultiLineString) {

                                    check = true;
                                }
                                sb.append(" MultiLineString");
                            } else if (type == FEATURE_SCHEMA_GEOMETRYCOLLECTION) {
                                if (element.getGeometry() instanceof GeometryCollection) {

                                    check = true;
                                }
                                sb.append(" GeometryCollection");
                            } else if (type == FEATURE_SCHEMA_LINEARRING) {
                                if (element.getGeometry() instanceof LinearRing) {

                                    check = true;
                                }
                                sb.append(" LinearRing");
                            }
                            //selGeo = "" + element.getGeometry().getGeometryType();
                        }

                        if (!check) {
                            return i18n.get(
                                "org.openjump.core.ui.CADEnableCheckFactory.Incorrect-geometry-type-correct-{0}",
                                sb);

                        }

                        // Check geoemtry types
                        if (noTypes != null) {
                            for (int i = 0; i < noTypes.length && check; i++) {
                                int type = noTypes[i];
                                if (type == FEATURE_SCHEMA_POINT) {
                                    if (element.getGeometry() instanceof Point) {
                                        check = false;
                                    }
                                } else if (type == FEATURE_SCHEMA_MULTIPOINT) {
                                    if (element.getGeometry() instanceof MultiPoint) {
                                        check = false;
                                    }
                                } else if (type == FEATURE_SCHEMA_POLYGON) {
                                    if (element.getGeometry() instanceof Polygon) {
                                        check = false;
                                    }
                                } else if (type == FEATURE_SCHEMA_MULTIPOLYGON) {
                                    if (element.getGeometry() instanceof MultiPolygon) {
                                        check = false;
                                    }
                                } else if (type == FEATURE_SCHEMA_LINESTRING) {
                                    if (element.getGeometry() instanceof LineString) {
                                        check = false;
                                    }
                                } else if (type == FEATURE_SCHEMA_MULTILINESTRING) {
                                    if (element.getGeometry() instanceof MultiLineString) {
                                        check = false;
                                    }
                                } else if (type == FEATURE_SCHEMA_GEOMETRYCOLLECTION) {
                                    if (element.getGeometry() instanceof GeometryCollection) {
                                        check = false;
                                    }
                                } else if (type == FEATURE_SCHEMA_LINEARRING) {
                                    if (element.getGeometry() instanceof LinearRing) {
                                        check = false;
                                    }
                                }

                            }
                            if (!check) {
                                return i18n.get("org.openjump.core.ui.CADEnableCheckFactory.Incorrect-geometry-type");//$NON-NLS-1$
                            }
                        }
                    }
                }
                return null;
            }
        };
    }

    /**
     * Check if the selected collection of selected features are all of selected
     * geometry. Geometry to be considered: POINT, MULTIPOINT, LINESTRING,
     * MULTILINESTRING, POLYGON, MULTIPOLYGON, GEOMETRYCOLLECTION.<This method
     * should be moved to
     * com.vividsolutions.jump.workbench.plugin.EnableCheckFactory>
     * 
     * @param types
     *            Int Type of geometry to include [...new int[]{
     *            FeatureSchema.POLYGON, FeatureSchema.MULTIPOLYGON}...]
     * @param noTypes
     *            Int Type of geometry to exclude [...new
     *            int[]{FeatureSchema.LINESTRING, FeatureSchema.MULTILINESTRING,
     *            FeatureSchema.POINT}...]
     * @return valid and not valid geometries type
     * 
     *         Example:
     * 
     *         GeneralUtils.checkGeometryType(new int[] {
     *         GeneralUtils.FEATURE_SCHEMA_LINESTRING,
     *         GeneralUtils.FEATURE_SCHEMA_LINEARRING }, new int[] {
     *         GeneralUtils.FEATURE_SCHEMA_GEOMETRYCOLLECTION})
     * 
     *         Result:
     * 
     *         Valid geometries: LineString and LinearRing. Not valid geometry:
     *         Geometry collection
     */

    public static EnableCheck createGeometryTypeOnSelectedFeaturesCheck(
            final int[] types, final int[] noTypes) {
        return new EnableCheck() {
            @Override
            public String check(JComponent component) {
                Collection<Feature> features = ((SelectionManagerProxy) JUMPWorkbench
                        .getInstance().getFrame().getActiveInternalFrame())
                        .getSelectionManager().getFeatureSelection()
                        .getFeaturesWithSelectedItems();

                for (Feature element : features) {
                    StringBuffer sb = new StringBuffer();
                    //String selGeo = "";
                    boolean check = false;
                    for (int i = 0; i < types.length && !check; i++) {
                        int type = types[i];
                        if (type == FEATURE_SCHEMA_POINT) {
                            if (element.getGeometry() instanceof Point) {

                                check = true;
                            }
                            sb.append(" Point");
                        } else if (type == FEATURE_SCHEMA_MULTIPOINT) {
                            if (element.getGeometry() instanceof MultiPoint) {

                                check = true;
                            }
                            sb.append(" MultiPoint");
                        } else if (type == FEATURE_SCHEMA_POLYGON) {
                            if (element.getGeometry() instanceof Polygon) {

                                check = true;
                            }
                            sb.append(" Polygon");
                        } else if (type == FEATURE_SCHEMA_MULTIPOLYGON) {
                            if (element.getGeometry() instanceof MultiPolygon) {

                                check = true;
                            }
                            sb.append(" MultiPolygon");
                        } else if (type == FEATURE_SCHEMA_LINESTRING) {
                            if (element.getGeometry() instanceof LineString) {

                                check = true;

                            }
                            sb.append("Linestring");
                        } else if (type == FEATURE_SCHEMA_MULTILINESTRING) {
                            if (element.getGeometry() instanceof MultiLineString) {

                                check = true;
                            }
                            sb.append(" MultiLineString");
                        } else if (type == FEATURE_SCHEMA_GEOMETRYCOLLECTION) {
                            if (element.getGeometry() instanceof GeometryCollection) {

                                check = true;
                            }
                            sb.append(" GeometryCollection");
                        } else if (type == FEATURE_SCHEMA_LINEARRING) {
                            if (element.getGeometry() instanceof LinearRing) {

                                check = true;
                            }
                            sb.append(" LinearRing");
                        }
                        //selGeo = "" + element.getGeometry().getGeometryType();
                    }

                    if (!check) {
                        return i18n.get(
                            "org.openjump.core.ui.CADEnableCheckFactory.Incorrect-geometry-type-correct-{0}",
                            sb);

                    }

                    // Check geoemtry types
                    if (noTypes != null) {
                        for (int i = 0; i < noTypes.length && check; i++) {
                            int type = noTypes[i];
                            if (type == FEATURE_SCHEMA_POINT) {
                                if (element.getGeometry() instanceof Point) {
                                    check = false;
                                }
                            } else if (type == FEATURE_SCHEMA_MULTIPOINT) {
                                if (element.getGeometry() instanceof MultiPoint) {
                                    check = false;
                                }
                            } else if (type == FEATURE_SCHEMA_POLYGON) {
                                if (element.getGeometry() instanceof Polygon) {
                                    check = false;
                                }
                            } else if (type == FEATURE_SCHEMA_MULTIPOLYGON) {
                                if (element.getGeometry() instanceof MultiPolygon) {
                                    check = false;
                                }
                            } else if (type == FEATURE_SCHEMA_LINESTRING) {
                                if (element.getGeometry() instanceof LineString) {
                                    check = false;
                                }
                            } else if (type == FEATURE_SCHEMA_MULTILINESTRING) {
                                if (element.getGeometry() instanceof MultiLineString) {
                                    check = false;
                                }
                            } else if (type == FEATURE_SCHEMA_GEOMETRYCOLLECTION) {
                                if (element.getGeometry() instanceof GeometryCollection) {
                                    check = false;
                                }
                            } else if (type == FEATURE_SCHEMA_LINEARRING) {
                                if (element.getGeometry() instanceof LinearRing) {
                                    check = false;
                                }
                            }

                        }
                        if (!check) {
                            return i18n.get("org.openjump.core.ui.CADEnableCheckFactory.Incorrect-geometry-type");//$NON-NLS-1$
                        }
                    }
                }

                return null;
            }
        };
    }

    /**
     * Part of checkGeometryType Types of geometry <Should be moved to
     * com.vividsolutions.jump.workbench.plugin.EnableCheckFactory>
     */
    //
    public static final int FEATURE_SCHEMA_UNKNOWN = 0;
    public static final int FEATURE_SCHEMA_POINT = 1;
    public static final int FEATURE_SCHEMA_MULTILINESTRING = 2;
    public static final int FEATURE_SCHEMA_LINESTRING = 3;
    public static final int FEATURE_SCHEMA_MULTIPOLYGON = 4;
    public static final int FEATURE_SCHEMA_POLYGON = 5;
    public static final int FEATURE_SCHEMA_LINEARRING = 6;
    public static final int FEATURE_SCHEMA_MULTIPOINT = 8;
    public static final int FEATURE_SCHEMA_GEOMETRYCOLLECTION = 15;

    /**
     * Part of checkGeometryType Gets the geometry type for the given JTS
     * geometry class <Should be moved to
     * com.vividsolutions.jump.workbench.plugin.EnableCheckFactory>
     * 
     * @param geomClass geometry class
     * @return an int representing the geometry class
     */
    public static int getGeometryType(Class<? extends Geometry> geomClass) {
        if (geomClass.equals(Point.class)) {
            return FEATURE_SCHEMA_POINT;
        } else if (geomClass.equals(MultiPoint.class)) {
            return FEATURE_SCHEMA_MULTIPOINT;
        } else if (geomClass.equals(LineString.class)) {
            return FEATURE_SCHEMA_LINESTRING;
        } else if (geomClass.equals(MultiLineString.class)) {
            return FEATURE_SCHEMA_MULTILINESTRING;
        } else if (geomClass.equals(Polygon.class)) {
            return FEATURE_SCHEMA_POLYGON;
        } else if (geomClass.equals(MultiPolygon.class)) {
            return FEATURE_SCHEMA_MULTIPOLYGON;
        } else if (geomClass.equals(GeometryCollection.class)) {
            return FEATURE_SCHEMA_GEOMETRYCOLLECTION;
        } else if (geomClass.equals(LinearRing.class)) {
            return FEATURE_SCHEMA_LINEARRING;
        } else {
            return FEATURE_SCHEMA_UNKNOWN;
        }
    }

}
