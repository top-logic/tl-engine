/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;

/**
 * A set constant definitions for use as keys for a request's attribute and
 * parameters.
 * 
 * The values may be changed as long as uniqueness of the values is guaranteed.
 * Parameters are additionally restricted to english characters and underscores.
 * 
 *  
 * @author  <a href="mailto:nvh@top-logic.com">nvh</a>
 */
public interface LayoutConstants {

    /**
     * Holds the used {@link LayoutComponent}.
     */
	public static final Property<LayoutComponent> ATTRIBUTE_LAYOUT = TypedAnnotatable.property(LayoutComponent.class,
		"com.top_logic.mig.html.layout.LayoutConstants.ATTRIBUTE_LAYOUT");

    /**
     * Signal	 to the Taglets, mainly the {@link com.top_logic.mig.html.layout.tag.LayoutHtml} 
     * not to write the HTML framework &lt;html&gt;&lt;
     * head&gt; &lt;/head&gt; &lt;body&gt;&lt;/body&gt;&lt;/html&gt;, but only
     * the body content.
     */
    public static final String ATTRIBUTE_SUPRESS_HTML_FRAMEWORK =
        "com.top_logic.mig.html.layout.LayoutConstants.ATTRIBUTE_SUPRESS_HTML_FRAMEWORK";
    
	public static final Property<Boolean> ATTRIBUTE_CONTENTS_ONLY = TypedAnnotatable.property(Boolean.class,
		"com.top_logic.mig.html.layout.LayoutConstants.ATTRIBUTE_CONTENTS_ONLY", false);
        
	/**
	 * The name of the action number parameter. Used in forms and links.
	 */        
    public static final String PARAM_SUBMIT_NUMBER = "SUBMIT_NUMBER";

    /**
	 * The name of the sequence number parameter. Used to bring AJAX commands
	 * and legacy commands in sync.
	 */
    public static final String PARAM_SEQUENCE_NUMBER = "tx";

    /**
	 * Parameter name to mark a command as read-only.
	 * 
	 * <p>
	 * Read-only commands do not require a sequence number and are executed in
	 * parallel with other display requests.
	 * </p>
	 */
    public static final String PARAM_READ_ONLY = "ro";
    
	/**
	 * Base directory for layout files.
	 */
	public static final String LAYOUT_BASE_DIRECTORY = "WEB-INF/layouts";

	/**
	 * Base directory for layout files.
	 */
	public static final String LAYOUT_BASE_RESOURCE = "/" + LAYOUT_BASE_DIRECTORY;

    /**
	 * Value passed to the {@link #PARAM_READ_ONLY} parameter to activate this
	 * option.
	 */
    public static final String PARAM_READ_ONLY_VALUE = "true";
    
    /**
     * The name of the parameter indicating which LayoutComponent to use. Used
     * in forms and links.
     */
    public static final String PARAM_LAYOUT = "LAYOUT";
    
    /**
	 * Constant for {@link #UTF_8} character encoding.
	 */
	public static final String UTF_8 = "UTF-8";

    /**
	 * Constant for {@link #ISO_8859_1} character encoding.
	 *
	 * @see #UTF_8
	 */
	public static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * Synthetic, unique Names will be prefixed with this String.  
     */
    public static final String UNIQUE_PREFIX = "__";

	/**
	 * Root application layout file.
	 */
	public static final String MASTER_FRAME_LAYOUT = "masterFrame.layout.xml";

	/**
	 * Whether the given {@link LayoutComponent} has a synthetic name.
	 * 
	 * @see #isSyntheticName(String)
	 */
	static boolean hasSynthesizedName(LayoutComponent component) {
		return isSyntheticName(component.getName());
	}

	/**
	 * Whether the given component name is a synthetic component name.
	 */
	static boolean isSyntheticName(ComponentName name) {
		return isSyntheticName(name.qualifiedName());
	}

	/**
	 * Whether the given component name is a synthetic component name.
	 */
	static boolean isSyntheticName(String name) {
		return name.startsWith(UNIQUE_PREFIX);
	}
}
