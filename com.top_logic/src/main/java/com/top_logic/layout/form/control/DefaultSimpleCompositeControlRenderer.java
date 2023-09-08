/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Renderer that displays the child {@link View}s of a {@link CompositeControl} without any
 * additional mark-up.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultSimpleCompositeControlRenderer extends AbstractSimpleCompositeControlRenderer {

	public static final DefaultSimpleCompositeControlRenderer SPAN_INSTANCE = new DefaultSimpleCompositeControlRenderer();

	public static final DefaultSimpleCompositeControlRenderer DIV_INSTANCE =
		new DefaultSimpleCompositeControlRenderer(HTMLConstants.DIV);
    
    protected DefaultSimpleCompositeControlRenderer() {
        // Singleton constructor.
    }
    
	public DefaultSimpleCompositeControlRenderer(InstantiationContext context,
			PolymorphicConfiguration<? extends DefaultSimpleCompositeControlRenderer> atts) {
        // Singleton constructor.
    }
	
	public DefaultSimpleCompositeControlRenderer(String localName) {
		super(localName, null);
	}

	public DefaultSimpleCompositeControlRenderer(String localName, Map<String, String> someAttributes) {
		super(localName, someAttributes);
	}

	@Override
	public void writeChildren(DisplayContext context, TagWriter out, List<? extends HTMLFragment> views) throws IOException {
		for (int cnt = views.size(), n = 0; n < cnt; n++) {
			views.get(n).write(context, out);
		}
	}

	/**
	 * Creates a {@link DefaultSimpleCompositeControlRenderer} with the {@link HTMLConstants#SPAN}
	 * tag and the given CSS classes.
	 * 
	 * @param cssClasses
	 *        A comma separated list of CSS classes to render in the root tag.
	 */
	public static DefaultSimpleCompositeControlRenderer spanWithCSSClass(String cssClasses) {
		return withCSSClass(SPAN, cssClasses);
	}

	/**
	 * Creates a {@link DefaultSimpleCompositeControlRenderer} with the {@link HTMLConstants#DIV}
	 * tag and the given CSS classes.
	 * 
	 * @param cssClasses
	 *        A comma separated list of CSS classes to render in the root tag.
	 */
	public static DefaultSimpleCompositeControlRenderer divWithCSSClass(String cssClasses) {
		return withCSSClass(DIV, cssClasses);
	}

	/**
	 * Creates a {@link DefaultSimpleCompositeControlRenderer} with the given tag and CSS classes
	 * 
	 * @param tagName
	 *        The tag name to write
	 * @param cssClasses
	 *        A comma separated list of CSS classes to render in the root tag.
	 */
	public static DefaultSimpleCompositeControlRenderer withCSSClass(String tagName, String cssClasses) {
		return new DefaultSimpleCompositeControlRenderer(tagName, Collections.singletonMap(CLASS_ATTR, cssClasses));
	}

}
