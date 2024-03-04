/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.AbstractCompositeControl;

/**
 * {@link Tag} interface that denotes a {@link BodyTag}, which accepts
 * control-creating tags as its children.
 * 
 * <p>
 * Children {@link Control}s of a {@link ControlBodyTag} are not rendered by
 * their creating tags themselves, but {@link #addControl(HTMLFragment) added} to
 * their parent {@link ControlBodyTag}. The parent {@link ControlBodyTag}
 * decides whether and where to render the control.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ControlBodyTag {

	/**
	 * Adds the given control as new child to the {@link AbstractCompositeControl} of
	 * this parent tag.
	 * 
	 * <p>
	 * Instead of rendering their control, {@link ControlTag}s call this method
	 * on their parent tag, if they are occur within the body of a
	 * {@link ControlBodyTag}.
	 * </p>
	 * 
	 * @param childControl
	 *        The child to be added to this control's {@link AbstractCompositeControl}.
	 * @return The replacement string to write instead of the created control.
	 *         <code>null</code>, if nothing should be written by child tags.
	 */
    public String addControl(HTMLFragment childControl);

}
