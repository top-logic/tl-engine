/**
 * <p>
 * This package provides classes for handling the {@link com.top_logic.util.Resources resources}
 * used for internationalization.
 * </p>
 * <h3>Locating resources</h3>
 * <p>
 * The following classes locate the resources used and this data for the other classes.
 * <dl>
 * <dt>ResourceFinder</dt>
 * <dd>This interface defines the methods for locating the resources used.</dd>
 * <dt>ResourceFileFinder</dt>
 * <dd>A concrete ResourceFinder locating resource files defined in the top-logic.xml.</dd>
 * <dt>ResourceSetData</dt>
 * <dd>Holds the information collected by a ResourceFinder. This information contains the resource
 * sets, the locales, and the resource per set/locale.</dd>
 * </dl>
 * </p>
 * <h3>Edit resources</h3>
 * <p>
 * The following classes provide methods to view and edit the resources.
 * <dl>
 * <dt>gui.ResourceTreeModel</dt>
 * <dd>An instance of DefaultIDTreeModel holding a tree representation of the resource keys. Keys
 * are interpreted as dot-separated paths.</dd>
 * <dt>gui.ResourceTreeRendere</dt>
 * <dd>An instance of HTreeCellRenderer for rendering the resource tree.</dd>
 * <dt>ResourceEditorController</dt>
 * <dd>Provides static methods for the JSPs used for editing the resources (jsp.admin.resources.*).
 * </dd>
 * </dl>
 * </p>
 * <h3>Updating resources</h3>
 * <p>
 * The following classes provide methods for updating the resources.
 * <dl>
 * <dt>ResourceUpdate</dt>
 * <dd>The ResourceUpdate holds the new/changed key-value pairs per resource set and locale.</dd>
 * <dt>ResourceUpdater</dt>
 * <dd>This interface defines the methods for updating the resources given via a ResourceSetData
 * with the data provided via a ResourceUpdate.</dd>
 * <dt>ResourceFileUpdater</dt>
 * <dd>A concrete ResourceUpdater for updating reources stored in propertie files.</dd>
 * </dl>
 * </p>
 */
package com.top_logic.util.resource;
