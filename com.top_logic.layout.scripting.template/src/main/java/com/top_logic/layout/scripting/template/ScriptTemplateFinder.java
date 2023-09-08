/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template;

import java.io.File;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileLocation;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.scripting.template.ScriptTemplateFinder.ScriptTemplateFinderConfig;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * Finds script templates by the business action they perform and the business object this action is
 * performed on. The name of this action and object is used to build the file name and path of the
 * script template.
 * <p>
 * Returns a {@link FileLocation} for lazy access to the actual {@link File} object. The lazy access
 * is important, as a direct access to a non-existing file could result in an error during the test
 * hierarchy creation which could kill it, along with all other tests.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@ServiceDependencies({
	TemplateSourceFactory.Module.class,
})
public class ScriptTemplateFinder extends ConfiguredManagedClass<ScriptTemplateFinderConfig> {

	/**
	 * {@link TypedConfiguration} for the {@link ScriptTemplateFinder}.
	 */
	public interface ScriptTemplateFinderConfig extends ConfiguredManagedClass.Config<ScriptTemplateFinder> {

		/** The default value for {@link #getMiscPath()}. */
		String DEFAULT_MISC_PATH = "misc";

		/**
		 * The path to the actions that don't belong to a business object.
		 */
		@StringDefault(DEFAULT_MISC_PATH)
		String getMiscPath();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ScriptTemplateFinder}.
	 * <p>
	 * <b>Don't call directly.</b> Use {@link #getInstance()} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ScriptTemplateFinder(InstantiationContext context, ScriptTemplateFinderConfig config) {
		super(context, config);
	}

	/**
	 * Convenience redirect to {@link ScriptTemplateFinder.Module#getImplementationInstance()}.
	 */
	public static ScriptTemplateFinder getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Finds the script for the given business action and object.
	 * <p>
	 * <b>It is not guaranteed that the returned file is existing!</b> <br/>
	 * Returned is the path where it has to be, if it is existing.
	 * </p>
	 * 
	 * @param businessObject
	 *        Is allowed to be <code>null</code>.
	 * @param businessAction
	 *        Is allowed to be <code>null</code>.
	 * 
	 * @return A script resource name to be resolved by {@link TemplateSourceFactory}.
	 */
	public String findScriptResourceFor(String businessObject, String businessAction) {
		String fileName = buildFileName(businessObject, businessAction);
		String scriptPrefix = getScriptResourcePrefix(businessObject, businessAction);
		String resourceName = scriptPrefix + '/' + fileName;
		return resourceName;
	}


	/**
	 * Builds the file name (not the path) for the script.
	 * 
	 * @param businessObject
	 *        Is allowed to be <code>null</code>.
	 * @param businessAction
	 *        Is allowed to be <code>null</code>.
	 * 
	 * @return Never <code>null</code>.
	 */
	protected String buildFileName(String businessObject, String businessAction) {
		if (StringServices.isEmpty(businessAction) && StringServices.isEmpty(businessObject)) {
			return "Action.xml";
		}
		if (StringServices.isEmpty(businessAction)) {
			return "Action-" + businessObject + ".xml";
		}
		if (StringServices.isEmpty(businessObject)) {
			return "Action-" + businessAction + ".xml";
		}
		return "Action-" + businessObject + "-" + businessAction + ".xml";
	}

	/**
	 * Returns a prefix to the resource name from which the script is loaded.
	 * 
	 * @param businessObject
	 *        Is allowed to be <code>null</code>.
	 * @param businessAction
	 *        Is allowed to be <code>null</code>.<br/>
	 *        Is not used in the standard implementation but might be used by subclasses overriding
	 *        this method.
	 * 
	 * @return Never <code>null</code>.
	 */
	protected String getScriptResourcePrefix(String businessObject, String businessAction) {
		String dirName = businessObject;
		if (StringServices.isEmpty(dirName)) {
			dirName = getMiscPath();
		}
		return "script:" + "/" + dirName;
	}

	/**
	 * @see ScriptTemplateFinderConfig#getMiscPath()
	 */
	private String getMiscPath() {
		return getConfig().getMiscPath();
	}

	/**
	 * {@link TypedRuntimeModule} of the {@link ScriptTemplateFinder}.
	 * 
	 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
	 */
	public static final class Module extends TypedRuntimeModule<ScriptTemplateFinder> {

		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<ScriptTemplateFinder> getImplementation() {
			return ScriptTemplateFinder.class;
		}

	}

}
