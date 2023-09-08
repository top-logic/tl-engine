/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.InvalidationListener;
import com.top_logic.layout.UpdateListener;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;

/**
 * The interface {@link LayoutControl} defines a layout of a business component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LayoutControl
		extends CompositeControl, UpdateListener, InvalidationListener, OrientationAware, PropertyObservable,
		Unimplementable {

	/**
	 * This method returns the parent of this {@link LayoutControl} object.
	 * 
	 * @return the parent of this {@link LayoutControl}. must be <code>null</code> if this
	 *         {@link LayoutControl} is the root of the layout structure tree this
	 *         {@link LayoutControl} belongs to.
	 */
	public LayoutControl getParent();

	/**
	 * This method returns either configured {@link LayoutData} of this {@link LayoutControl} or
	 * {@link LayoutData}, based on {@link #setSize(DisplayDimension, DisplayDimension)}.
	 */
	public LayoutData getConstraint();

	/**
	 * This method sets the {@link LayoutData} for this {@link LayoutControl}.
	 */
	public void setConstraint(LayoutData aConstraint);

	@Override
	default Orientation getOrientation() {
		return Orientation.HORIZONTAL;
	}

	/**
	 * Whether this element is part of a horizontal row.
	 */
	@TemplateVariable("horizontal")
	default boolean isHorizontal() {
		LayoutControl parent = getParent();
		if (parent == null) {
			return false;
		}
		return parent.getOrientation().isHorizontal();
	}

	/**
	 * Updates the user defined size of this {@link LayoutControl} and stores it in the
	 * {@link PersonalConfiguration}.
	 * 
	 * <p>
	 * In contrast to {@link #setConstraint(LayoutData)}, this update is initiated from the client,
	 * which means that the UI is already updated to the new sizes. Therefore, the control needs no
	 * repaint. Only the updated size value have to be stored to the personalization.
	 * </p>
	 */
	void setSize(DisplayDimension width, DisplayDimension height);

	/**
	 * This method sets, whether this {@link LayoutControl} is resizable, or not
	 * 
	 * @param resizable
	 *        true, if this {@link LayoutControl} shall be resizable, false otherwise
	 */
	void setResizable(boolean resizable);

	/**
	 * true, if this {@link LayoutControl} is resizable, false otherwise
	 */
	boolean isResizable();

	/**
	 * This method returns a list of {@link LayoutControl}s such that this {@link LayoutControl} is
	 * the parent of each {@link LayoutControl} in the returned list.
	 * 
	 * @return list of {@link LayoutControl}. never <code>null</code>, but it may be empty.
	 */
	@Override
	public List<? extends LayoutControl> getChildren();

	/**
	 * Creates information for client-side layouting.
	 */
	default void writeLayoutSizeAttribute(TagWriter out) throws IOException {
		out.beginAttribute(LayoutControlRenderer.LAYOUT_SIZE_DATA_ATTRIBUTE);
		writeLayoutSizeInformation(out);
		out.endAttribute();
	}

	/**
	 * Creates information for client-side layouting to be rendered into the
	 * {@value LayoutControlRenderer#LAYOUT_SIZE_DATA_ATTRIBUTE} attribute.
	 */
	@TemplateVariable("layoutSize")
	default void writeLayoutSizeInformation(TagWriter out) throws IOException {
		LayoutData aLayoutData = getConstraint();
		if (isHorizontal()) {
			LayoutControlRenderer.writeConstraint(out, aLayoutData.getWidth(), aLayoutData.getMinWidth(),
				aLayoutData.getWidthUnit());
		} else {
			LayoutControlRenderer.writeConstraint(out, aLayoutData.getHeight(), aLayoutData.getMinHeight(),
				aLayoutData.getHeightUnit());
		}
	}
}
