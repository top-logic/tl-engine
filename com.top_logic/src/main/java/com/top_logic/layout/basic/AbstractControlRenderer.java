/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.control.ButtonControl;

/**
 * Abstract base class for all renderers/views of {@link com.top_logic.layout.Control}s that are
 * derived from {@link AbstractControlBase}.
 * 
 * <p>
 * A renderer for controls is responsible for creating the XHTML fragment that finally displays the
 * control in the browser. As an implementation of {@link Renderer}, it expects as value a concrete
 * sub-class of {@link AbstractControlBase}, which should be displayed.
 * </p>
 * 
 * <p>
 * In addition to initially creating the display of a control, sub-classes of
 * {@link AbstractControlRenderer} are also responsible for keeping the initially created display
 * up-to-date, if properties of the control are modified. To do so, control-specific sub-classes of
 * {@link AbstractControlRenderer} have event handling methods that are invoked by the control that
 * they display.
 * </p>
 * 
 * @see AbstractButtonRenderer for an example of a complete view interface that is specific for
 *      {@link ButtonControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractControlRenderer<T extends Control> implements ControlRenderer<T> {

	/**
	 * Renders attributes of the control root element.
	 * 
	 * @param context
	 *        The {@link DisplayContext} in which the given control is rendered.
	 * @param out
	 *        The {@link TagWriter} to write output to.
	 * @param control
	 *        The {@link Control} to write.
	 */
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, T control)
		throws IOException 
	{
		((AbstractControlBase) control).writeControlAttributes(context, out);
	}
	
	/**
	 * Accessor method to {@link AbstractControl#addUpdate(ClientAction)} for
	 * sub-classes of {@link AbstractControlRenderer}.
	 * 
	 * <p>
	 * To allow all subclasses of {@link AbstractControlRenderer} (no matter in which
	 * package they reside) access to the protected method
	 * {@link AbstractControl#addUpdate(ClientAction)}, the class
	 * {@link AbstractControlRenderer} must reside in the very same package as
	 * {@link AbstractControl}.
	 * </p>
	 * 
	 * @param control
	 *     The control to add the update to.
	 * @param update
	 *     The update to add to the control's update queue.
	 */
	protected void addUpdate(AbstractControl control, ClientAction update) {
		control.addUpdate(update);
	}

}
