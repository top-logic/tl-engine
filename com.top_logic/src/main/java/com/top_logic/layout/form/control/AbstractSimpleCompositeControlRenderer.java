/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.View;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Convenience super-class for {@link Renderer}s for {@link CompositeControl}
 * contents that render a single element for the composite control itself.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSimpleCompositeControlRenderer extends DefaultControlRenderer<CompositeControl> {

	/**
	 * @see #getLocalName()
	 */
	private final String localName;

	/**
	 * May be <code>null</code>.
	 * 
	 * @see #AbstractSimpleCompositeControlRenderer(String, Map)
	 */
	private final Map<String, String> attributes;

	public AbstractSimpleCompositeControlRenderer() {
		this(null, null);
	}

	/**
	 * Create a new {@link DefaultControlRenderer} that renders
	 * {@link CompositeControl}s with HTML elements with the given local name
	 * and the given set of attributes.
	 *
	 * @param aLocalName
	 *        The HTML tag name of {@link Control} elements rendered by this
	 *        renderer.
	 * @param someAttributes
	 *        A set of HTML attribute names and values that are rendered into
	 *        {@link Control} elements created by this renderer. The
	 *        {@link HTMLConstants#ID_ATTR} must not be within the given
	 *        attributes set.
	 */
	public AbstractSimpleCompositeControlRenderer(String aLocalName, Map<String, String> someAttributes) {
		if (aLocalName != null) {
			if (aLocalName.length() == 0) {
				throw new IllegalArgumentException("Local tag name may not be empty.");
			}
			assert validChars(aLocalName) : "Local name may only contain letter characters.";

			this.localName = aLocalName;
		} else {
			this.localName = HTMLConstants.SPAN;
		}

		// Copy attributes map to ensure that it stays constant.
		if (someAttributes != null && !someAttributes.isEmpty()) {
			this.attributes = new HashMap<>(someAttributes);

			if (this.attributes.containsKey(HTMLConstants.ID_ATTR)) {
				throw new IllegalArgumentException("Attribute '" + HTMLConstants.ID_ATTR + "' is reserved for internal purpose.");
			}
		} else {
			this.attributes = null;
		}
	}

	@Override
	protected String getControlTag(CompositeControl control) {
		return localName;
	}

	private boolean validChars(String string) {
		for (int n = 0, cnt = string.length(); n < cnt; n++) {
			if (! Character.isLetter(string.charAt(n))) return false;
		}
		return true;
	}

	/**
	 * The local name of the HTML element used as client-side representation of
	 * this control.
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Removes the attribute with the given name.
	 *
	 * @see #AbstractSimpleCompositeControlRenderer(String, Map)
	 *
	 * @param name
	 *        The name of the attribute to remove.
	 * @return The previous value of the removed attribute, or <code>null</code>,
	 *         if no such attribute was set.
	 */
	public String removeAttribute(String name) {
		if (this.attributes == null) {
			return null;
		}
		return this.attributes.remove(name);
	}

	/**
	 * Writes a set of name/value pairs that represent the HTML attributes of this
	 * control's client-side representation as HTML element.
	 *
	 * @see #getLocalName() for the HTML element used as client-side
	 *      representation.
	 */
	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, CompositeControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);
		if (attributes == null) {
			return;
		}
		for (Iterator<Entry<String, String>> it = attributes.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> attribute = it.next();
			String key = attribute.getKey();
			if (CLASS_ATTR.equals(key)) {
				// classes are written in other method
				continue;
			}
			out.writeAttribute(key, attribute.getValue());
		}
	}

	@Override
	public void appendControlCSSClasses(Appendable out, CompositeControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		if (attributes != null) {
			HTMLUtil.appendCSSClass(out, attributes.get(CLASS_ATTR));
		}
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, CompositeControl control)
			throws IOException {
		writeChildren(context, out, control.getChildren());
	}

	/**
	 * Renders the contents of a composite control.
	 *
	 * <p>
	 * The contents of a {@link CompositeControl} consists of the {@link View}s
	 * of the composite plus additional mark-up for layout. The contents does
	 * not consist of the top-level HTML element that represents the
	 * {@link CompositeControl} itself.
	 * </p>
	 */
	public abstract void writeChildren(DisplayContext context, TagWriter out, List<? extends HTMLFragment> views) throws IOException;

}
