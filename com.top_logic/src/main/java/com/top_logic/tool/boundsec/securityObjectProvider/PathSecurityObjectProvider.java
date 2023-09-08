/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.path.PathFormat;
import com.top_logic.tool.boundsec.securityObjectProvider.path.SecurityPath;

/**
 * Use a path to find the {@link BoundObject} in the layout hierarchy.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PathSecurityObjectProvider extends AbstractConfiguredInstance<PathSecurityObjectProvider.Config>
		implements SecurityObjectProvider {

	/**
	 * Configuration of a {@link PathSecurityObjectProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<PathSecurityObjectProvider> {

		/** Entry tag for {@link #getPath()}. */
		String PATH_ELEMENT = "path-element";

		/** Configuration name for {@link #getPath()}. */
		String PATH = "path";

		/**
		 * The path to navigate.
		 */
		@Name(PATH)
		@EntryTag(PATH_ELEMENT)
		@Format(PathFormat.class)
		List<PolymorphicConfiguration<? extends SecurityPath>> getPath();

		/**
		 * Setter for {@link #getPath()}.
		 */
		void setPath(List<PolymorphicConfiguration<? extends SecurityPath>> list);

	}

	/** Separator for path elements. */
	public static final char PATH_ELEMENT_SEPARATOR        = '.';
	
	// How to get the correct layout
	/** Get component by name. */
	public static final String PATH_ELEMENT_COMPONENT = "component";

	/** Get opener (for dialogs). */
	public static final String PATH_ELEMENT_OPENER           = "opener";
	/** Get master. */
	public static final String PATH_ELEMENT_MASTER           = "master";
	/** Get parent. */
	public static final String PATH_ELEMENT_PARENT           = "parent";
	/** Get selectable master. */
	public static final String PATH_ELEMENT_SELECTABLEMASTER = "selectablemaster";
	/** Get enclosing window. */
	public static final String PATH_ELEMENT_WINDOW           = "window";
	
	// How to get the BoundObject
	/** Selection. */
	public static final String PATH_ELEMENT_SELECTION        = "selection";
	/** Model. */
	public static final String PATH_ELEMENT_MODEL            = "model";
	/** Current BoundObject from getCurrentObject(BoundCommandGroup). NOTE: do not use plain, i.e. on current component. */
	public static final String PATH_ELEMENT_CURRENTOBJECT      = "currentobject";
	
	public static final PathSecurityObjectProvider MODEL_INSTANCE;
	static {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.getPath().add(SecurityPath.model().getConfig());
		MODEL_INSTANCE = TypedConfigUtil.createInstance(config);
	}

	private List<? extends SecurityPath> _paths;

	/**
	 * Creates a new {@link PathSecurityObjectProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link PathSecurityObjectProvider}.
	 */
	public PathSecurityObjectProvider(InstantiationContext context, Config config) {
		super(context, config);
		_paths = TypedConfiguration.getInstanceList(context, config.getPath());
	}

	/**
	 * Create with path
	 * 
	 * @param property
	 *        The property used to configure the given path.
	 * @param path
	 *        the path. Must consistent of the elements defined above separated with the separator
	 * 
	 * @throws ConfigurationException
	 *         if the path is not syntactically correct.
	 */
	public static PathSecurityObjectProvider newPathSecurityObjectProvider(String property, String path) throws ConfigurationException {
		return TypedConfigUtil.createInstance(toConfig(property, path));
	}

	static Config toConfig(String propertyName, String path) throws ConfigurationException {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setPath(PathFormat.INSTANCE.getValue(propertyName, path));
		return config;
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker checker, Object model, BoundCommandGroup commandGroup) {
		Object theModel = this.getModel(checker, model, commandGroup);
		
		/**
		 * This is compatibility code for a feature used in DPM:
		 * 		- The model might be a Collection of BoundObjects
		 * 		- The BoundChecker interface has no possibility to check Collections
		 * 		- Therefore we return the first BoundObject that will be allowed when checking the given BoundCommandGroup
		 */
		if (theModel instanceof Collection) {
			BoundObject theBO = null;
			Iterator<?> theIt = ((Collection<?>) theModel).iterator();
			while (theIt.hasNext()) {
				Object theElt = theIt.next();
				if (theElt instanceof BoundObject) {
					theBO = (BoundObject) theElt;
					if (checker.allow(commandGroup, theBO)) {
						return theBO;
					}
				}
			}
		}
		else if (theModel instanceof BoundObject) {
			return (BoundObject) theModel;
		}
		
		return null;
	}
	
	/**
	 * Go through the path to find the {@link BoundObject}
	 */
	public Object getModel(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		LayoutComponent theComp   = (LayoutComponent) aChecker;

		for (int i = 0, size = _paths.size(); i < size; i++) {
			SecurityPath pathElt = _paths.get(i);
			LayoutComponent nextComponent = pathElt.nextComponent(theComp, i, size);
			if (nextComponent == null) {
				return pathElt.getModel(theComp, model, aCommandGroup, i, size);
			}
			theComp = nextComponent;
		}

		return null;
	}
	
	@Override
	public String toString() {
		return super.toString() + _paths;
	}

}
