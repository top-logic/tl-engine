/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.mime;

import java.io.InputStream;

import jakarta.activation.FileTypeMap;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service resolving file extensions to content types.
 */
public class MimeTypesModule extends ManagedClass implements Reloadable {
	/** The standard type for HTML. */
	public static final String HTML_TYPE = "text/html";

	/** The standard type for HTML. */
	public static final String XML_TYPE = "text/xml";

	/** The map containing all known MIME types. */
	private FileTypeMap mimeMap;

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link MimeTypesModule}.
	 */
	public MimeTypesModule(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);

		ReloadableManager.getInstance().addReloadable(this);
	}

	@Override
	protected void startUp() {
		super.startUp();
		getMimeTypeMap();
	}

	/**
	 * Delivers the description of the instance of this class.
	 *
	 * @return The description for debugging.
	 */
	@Override
	public String toString() {
		return (this.getClass().getName() + " ["
			+ "mimeMap: " + this.mimeMap
			+ ']');
	}

	/**
	 * Forces the instance to reload the configuration.
	 *
	 * This implementation initialize the properties again and returns true afterwards.
	 *
	 * @return true, if reloading succeeds.
	 */
	@Override
	public boolean reload() {
		this.mimeMap = null;

		return (true);
	}

	/**
	 * Returns a user understandable name of the implementing class.
	 *
	 * If the returned value is empty, the value will not be displayed in the user interface.
	 *
	 * @return The name of the class.
	 */
	@Override
	public String getName() {
		return ("Mime Types");
	}

	/**
	 * Returns the description of the functionality of the implementing class.
	 *
	 * @return The description of the function of this class.
	 */
	@Override
	public String getDescription() {
		return ("The information about file extentions and their names and images.");
	}

	/**
	 * Returns true, if the instance uses the XMLproperties.
	 *
	 * This is important for the Reloadable function to clarify, if the XMLProperties have to be
	 * reloaded also.
	 *
	 * @return true, if the XMLProperties are in use.
	 */
	@Override
	public boolean usesXMLProperties() {
		return (true);
	}

	/**
	 * Returns a Translated file type for the given Mime-Type.
	 * 
	 * Translation is done by subclasses, later.
	 *
	 * @param aType
	 *        A MimeType
	 * @return name describing the MimeType.
	 */
	public String getDescription(String aType) {
		return aType;
	}

	/**
	 * Finds an official MIME type for a given file name.
	 * 
	 * @param fileName
	 *        The file name. Can be <code>null</code>.
	 * @return The mime type for the given file name. Never <code>null</code>.
	 */
	public final String getMimeType(String fileName) {
		if (StringServices.isEmpty(fileName)) {
			return BinaryData.CONTENT_TYPE_OCTET_STREAM;
		}

		String contentType = lookupMimetype(fileName.toLowerCase());

		// Pure safety:
		if (contentType == null) {
			return BinaryData.CONTENT_TYPE_OCTET_STREAM;
		}

		return contentType;
	}

	/**
	 * Finds an official MIME type for a given lower-case file name.
	 */
	protected String lookupMimetype(String lowerCaseName) {
		return this.getMimeTypeMap().getContentType(lowerCaseName);
	}

	/**
	 * Returns the map with the Mime types.
	 *
	 * @return The requested map.
	 */
	protected FileTypeMap getMimeTypeMap() {
		if (this.mimeMap == null) {
			try (InputStream theFile = FileManager.getInstance().getStream("/WEB-INF/web.xml")) {
				this.mimeMap = MimetypesParser.parse(theFile);
			} catch (Exception ex) {
				Logger.error("getMimeTypeMap() failed, using defaults", ex, this);

				this.mimeMap = FileTypeMap.getDefaultFileTypeMap();
			}
		}

		return (this.mimeMap);
	}

	/**
	 * Returns the only instance of this class. If this instance doesn't exist, it'll be created
	 * automatically.
	 *
	 * @return The only instance of this class.
	 */
	public static MimeTypesModule getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link MimeTypesModule}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven F?rster</a>
	 */
	public static final class Module extends TypedRuntimeModule<MimeTypesModule> {

		/**
		 * Module instance.
		 */
		public static final MimeTypesModule.Module INSTANCE = new MimeTypesModule.Module();

		@Override
		public Class<MimeTypesModule> getImplementation() {
			return MimeTypesModule.class;
		}

	}
}
