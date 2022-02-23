# cadtools-extension

cadtool-extension adds CAD tools to OpenJUMP 

## Maven build

as the extension depends on Vertex-Symbols, the dependency might be needed to be downloaded before a fresh build or after `mvn clean`. if Maven complains similar to

> Could not resolve dependencies for project org.openjump:cadtools-extension:jar:2.1.2: Could not find artifact com.cadplan:vertex-symbols:jar:2.2.0

the simply run `mvn initialize` one time before building like

```
mvn initialize
mvn package
```

## Source code

* ConstrainedNClickTool class from SkyJUMP ver. 2
* CAD tools from from Kosmo SAIG Cad Tools (version 3.0)
* Line decorations, Block and Annotation tools from AdvancedTools (Giuseppe Aruta - GeoArbores Project)
* Other enhancements have been adapted from OpenJUMP 1.10 source code

## Licenses
All copyright reserved under GNU version 3
except org.openjump.advancedtools.tools.ConstrainedNClickTool which is under GNU version 2

## Contact

- OpenJUMP [User Mailing List](http://groups.google.com/group/openjump-users)
- OpenJUMP [Development Mailing List](https://lists.sourceforge.net/lists/listinfo/jump-pilot-devel)
- OpenJUMP [WIKI page](https://sourceforge.net/apps/mediawiki/jump-pilot/index.php?title=Main_Page)
-------------------------------------- 

Giuseppe Aruta - GeoArbores Project (https://sourceforge.net/projects/opensit)

Geo Arbores is a project for developing tools and extensions for GIS software and spatial
opensource libraries (Kosmo, OpenJUMP and GDAL / OGR).
