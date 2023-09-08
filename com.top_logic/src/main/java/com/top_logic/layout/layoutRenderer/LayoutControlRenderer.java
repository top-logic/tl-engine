/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;
import java.io.StringWriter;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.TagWriter.State;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.structure.AbstractLayoutControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The class {@link LayoutControlRenderer} is an abstract superclass for rendering
 * {@link LayoutControl}s. It provides methods to write the layout information of the
 * {@link LayoutControl}s.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LayoutControlRenderer<T extends AbstractLayoutControl<?>> extends DefaultControlRenderer<T> {

	/**
	 * HTML5 data Attribute annotating a layout to a HTML element.
	 */
	public static final String LAYOUT_DATA_ATTRIBUTE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "layout";

	/**
	 * HTML5 data attribute transporting the size of an element to the client-side layout algorithm.
	 */
	public static final String LAYOUT_SIZE_DATA_ATTRIBUTE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "layout-size";

	private static final String RESIZE_DATA_ATTRIBUTE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "resize";

	/**
	 * The result of {@link #writeLayoutInformationAttribute(Orientation, int, TagWriter)} as
	 * string.
	 */
	public static String getLayoutInformation(Orientation orientation, int maxSize) {
		StringWriter buffer = new StringWriter();
		try {
			TagWriter out = new TagWriter(buffer);
			out.setState(State.START_TAG);
			writeLayoutInformationAttribute(orientation, maxSize, out);
		} catch (IOException e) {
			throw new UnreachableAssertion(e);
		}
		return buffer.toString();
	}
	
	/**
	 * @deprecated Use {@link #getLayoutInformation(Orientation, int)}.
	 */
	@Deprecated
	public static String getLayoutInformation(boolean horizontal, int maxSize) {
		return getLayoutInformation(Orientation.horizontal(horizontal), maxSize);
	}

	/**
	 * Write layouting information of child layouts for client-side layouting to the given
	 * {@link TagWriter}.
	 * 
	 * @param orientation
	 *        The layout direction of children.
	 * @param maxSize
	 *        - maximum size in percent of the available space for all of these child elements,
	 *        whose size is given in pixel
	 * @param aTagWriter
	 *        - the sink to which data is dumped
	 */
	public static void writeLayoutInformationAttribute(Orientation orientation, int maxSize, TagWriter aTagWriter)
			throws IOException {
		aTagWriter.beginAttribute(LAYOUT_DATA_ATTRIBUTE);
		{
			writeLayoutInformation(aTagWriter, orientation, maxSize);
		}
		aTagWriter.endAttribute();
	}

	/**
	 * Write layouting information of child layouts for client-side layouting to the given
	 * {@link TagWriter}.
	 * 
	 * @param orientation
	 *        The layout direction of children.
	 * @param maxSize
	 *        - maximum size in percent of the available space for all of these child elements,
	 *        whose size is given in pixel
	 * @param aTagWriter
	 *        - the sink to which data is dumped
	 */
	public static void writeLayoutInformation(TagWriter aTagWriter, Orientation orientation, int maxSize)
			throws IOException {
		aTagWriter.write("{horizontal:");
		aTagWriter.write(Boolean.toString(orientation.isHorizontal()));
		aTagWriter.write(", maxSize:");
		aTagWriter.writeInt(maxSize);
		aTagWriter.write("}");
	}
	
	/**
	 * @deprecated Use {@link #writeLayoutInformationAttribute(Orientation, int, TagWriter)}
	 */
	@Deprecated
	public static void writeLayoutInformation(boolean horizontal, int maxSize, TagWriter aTagWriter) throws IOException {
		writeLayoutInformationAttribute(Orientation.horizontal(horizontal), maxSize, aTagWriter);
	}

	/**
	 * The result of {@link #writeLayoutConstraintInformation(float, DisplayUnit, TagWriter)} as
	 * string.
	 */
	public static String getLayoutConstraintInformation(float aSize, DisplayUnit aUnit) {
		StringWriter buffer = new StringWriter();
		try {
			TagWriter out = new TagWriter(buffer);
			out.setState(State.START_TAG);
			writeLayoutConstraintInformation(aSize, 0, aUnit, out);
		} catch (IOException e) {
			throw new UnreachableAssertion(e);
		}
		return buffer.toString();
	}

	/**
	 * Write size information for client-side layouting to the given
	 * {@link TagWriter}.
	 * 
	 * @param out
	 *        The writer to write size information to.
	 * @param dimension
	 *        The size description.
	 */
	public static void writeLayoutConstraintInformation(TagWriter out, DisplayDimension dimension) throws IOException {
		writeLayoutConstraintInformation(dimension.getValue(), 0, dimension.getUnit(), out);
	}
	
	/**
	 * Write size information for client-side layouting to the given {@link TagWriter}.
	 * 
	 * @param aSize
	 *        - element size
	 * @param aUnit
	 *        - {@link DisplayUnit} of element size
	 * @param aTagWriter
	 *        - the sink to which data is dumped
	 */
	public static void writeLayoutConstraintInformation(float aSize, DisplayUnit aUnit, TagWriter aTagWriter)
			throws IOException {
		writeLayoutConstraintInformation(aSize, 0, aUnit, aTagWriter);
	}

	/**
	 * Write size information for client-side layouting to the given {@link TagWriter}.
	 * 
	 * @param aSize
	 *        - element size
	 * @param minSize
	 *        - minimum size in pixel, that the element must have
	 * @param aUnit
	 *        - {@link DisplayUnit} of element size
	 * @param aTagWriter
	 *        - the sink to which data is dumped
	 */
	public static void writeLayoutConstraintInformation(float aSize, int minSize, DisplayUnit aUnit,
			TagWriter aTagWriter)
			throws IOException {
		aTagWriter.beginAttribute(LAYOUT_SIZE_DATA_ATTRIBUTE);
		{
			writeConstraint(aTagWriter, aSize, minSize, aUnit);
		}
		aTagWriter.endAttribute();
	}

	/**
	 * Write size information for client-side layouting to the given {@link TagWriter}.
	 * 
	 * @param out
	 *        - the sink to which data is dumped
	 * @param aSize
	 *        - element size
	 * @param minSize
	 *        - minimum size in pixel, that the element must have
	 */
	public static void writeConstraint(TagWriter out, DisplayDimension aSize, int minSize) throws IOException {
		writeConstraint(out, aSize.getValue(), minSize, aSize.getUnit());
	}

	/**
	 * Write size information for client-side layouting to the given {@link TagWriter}.
	 * 
	 * @param out
	 *        - the sink to which data is dumped
	 * @param aSize
	 *        - element size
	 * @param minSize
	 *        - minimum size in pixel, that the element must have
	 * @param aUnit
	 *        - {@link DisplayUnit} of element size
	 */
	public static void writeConstraint(TagWriter out, float aSize, int minSize, DisplayUnit aUnit)
			throws IOException {
		out.write("{size:");
		out.write(String.valueOf(aSize));
		out.write(", minSize:");
		out.writeInt(minSize);
		out.write(", unit:'");
		out.write(aUnit.getExternalName());
		out.write("'}");
	}

	/**
	 * This method writes code of a js function to be used to resize the content of the currently
	 * written tag.
	 */
	public static void writeLayoutResizeFunction(TagWriter out, String resizeFunction)
			throws IOException {
		out.beginAttribute(RESIZE_DATA_ATTRIBUTE);
		out.write(resizeFunction);
		out.endAttribute();
	}

	@Override
	protected String getControlTag(T control) {
		return DIV;
	}

}
