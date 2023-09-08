/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.io.File;

import com.top_logic.base.office.AbstractOffice;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.io.BinaryContent;

/**
 * Access class for MS-Powerpoint.
 * 
 * This class will provide high level methods for reading and writing 
 * Powerpoint files. High level means that values from the Powerpoint
 * can be read and written by simple {@link java.util.Map maps}.
 * 
 * The maps are expected to be the following:
 * 
 * <ul>
 *     <li>getValues(): <em>key</em>: A generated unique ID of the text field. 
 *                      <em>value</em>: The text within the text field.</li>
 *     <li>setValues(): <em>key</em>: The curren value of the text field. 
 *                      <em>value</em>: The new content of the text field. 
 *                      This method will change different text fields, which
 *                      have the same content! If the new content is a file
 *                      this method will add that as an image to the 
 *                      presentation.</li>
 * </ul>
 * 
 * @see #getInstance(boolean)
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class Powerpoint extends AbstractOffice {

	/**
	 * Global configuration options for {@link Powerpoint}.
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * {@link PowerpointFactory} for the legacy format.
		 */
		@InstanceFormat
		PowerpointFactory getLegacyFactory();

		/**
		 * {@link PowerpointFactory} for the new XML format.
		 */
		@InstanceFormat
		PowerpointFactory getXMLFactory();

	}

	/**
	 * Factory creating {@link Powerpoint} instances.
	 */
	public interface PowerpointFactory {

		/**
		 * Creates a new {@link Powerpoint} instance.
		 * 
		 * @see Powerpoint#getInstance(boolean)
		 */
		Powerpoint newInstance();

	}

	/** File extension for MS-Powerpoint presentations. */
	public static final String PPT_EXT = ".ppt";

    /** File extension for MS-Powerpoint presentations. */
	public static final String PPTX_EXT = ".pptx";

    /** File extension for MS-Powerpoint templates. */
    public static final String TEMP_EXT = ".pot";

    /** Help flag for communicating with powerpoint (boolean type true). */
    public static final int TRUE = -1;

    /** Help flag for communicating with powerpoint (boolean type false). */
    public static final int FALSE = 0;

    /**
     * Default CTor.
     */
    protected Powerpoint() {
        super();
    }

    /**
     * Return the file extension of powerpoint presentations (is {@link #PPT_EXT}).
     * 
     * @return    {@link #PPT_EXT} (is ".ppt").
     * @see       com.top_logic.base.office.AbstractOffice#getExtension()
     */
    @Override
	public String getExtension() {
        return (PPT_EXT);
    }

	@Override
	public Mapping getStringMapping() {
		return Mappings.identity();
	}

    /**
     * Convert a MS-Office file from an old version to one of the installed MS-Office.
     * 
     * @param    anIn     The name of the file to be converted.
     * @param    anOut    The name of the resulting file.
     * @return   The new converted file in the format of the installed Office (ppt --> ppt2000 xls --> xls2000)
     *           or <code>null</code>, if converter fails.
     */
    public abstract File convert(String anIn, String anOut);

    public static boolean isXmlFormat(File aWorkFile) {
		return aWorkFile.getName().toLowerCase().endsWith(PPTX_EXT);
	}

	public static boolean isXmlFormat(BinaryContent aWorkFile) {
		return aWorkFile.getName().toLowerCase().endsWith(PPTX_EXT);
	}

	/**
	 * Return the only instance of this class.
	 * 
	 * This class is a ingleton, because it'll create an instance of the powerpoint application in
	 * system. After closing this instance, the application will stay in memory.
	 * 
	 * @param useXFormat
	 *        Whether to use the XML format.
	 * 
	 * @return The only instance of this class.
	 */
	public static Powerpoint getInstance(boolean useXFormat) {
		if (useXFormat) {
			return ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getXMLFactory().newInstance();
		} else {
			return ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getLegacyFactory().newInstance();
		}
    }
}
