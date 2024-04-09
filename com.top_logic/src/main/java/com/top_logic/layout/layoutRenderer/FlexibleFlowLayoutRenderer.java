/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.structure.CollapsibleControl;
import com.top_logic.layout.structure.ContainerControl;
import com.top_logic.layout.structure.FlexibleFlowLayoutControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * The class {@link FlexibleFlowLayoutRenderer} is used to render the content of a
 * {@link FlexibleFlowLayoutControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FlexibleFlowLayoutRenderer extends FlowLayoutRenderer {

	/**
	 * Singleton {@link FlexibleFlowLayoutRenderer} instance.
	 */
	public static final FlexibleFlowLayoutRenderer INSTANCE = new FlexibleFlowLayoutRenderer();

	private FlexibleFlowLayoutRenderer() {
		// Singleton constructor.
	}

	@Override
	protected void writeLayoutChildren(DisplayContext context, TagWriter out, ContainerControl container)
			throws IOException {
		FlexibleFlowLayoutControl flowLayout = (FlexibleFlowLayoutControl) container;
		List<? extends LayoutControl> layoutChildren = flowLayout.getChildren();

		int rightmostNormalizedChildIndex = layoutChildren.size() - 1;
		while (rightmostNormalizedChildIndex >= 0) {
			LayoutControl childLayout = layoutChildren.get(rightmostNormalizedChildIndex);
			if (childLayout.isResizable() && !flowLayout.isCollapsed(childLayout)) {
				break;
			}
			rightmostNormalizedChildIndex--;
		}

		Orientation orientation = flowLayout.getOrientation();
		for (int index = 0, size = layoutChildren.size(); index < size; index++) {
			LayoutControl childLayout = layoutChildren.get(index);
			if (flowLayout.isHidden(childLayout)) {
				continue;
			}
			childLayout.write(context, out);
			if ((index < size - 1) && childLayout.isResizable() && layoutChildren.get(index + 1).isResizable()) {
				boolean separatorCollapsed =
					flowLayout.isCollapsed(childLayout) || index == rightmostNormalizedChildIndex;
				writeSeparatorElement(out, flowLayout, orientation, index, separatorCollapsed);
			}
		}
	}

	private void writeSeparatorElement(TagWriter out, FlexibleFlowLayoutControl self, Orientation orientation,
			int separatorID, boolean collapsed) throws IOException {
		int collapsedSize = Icons.LAYOUT_ADJUSTMENT_GRABBER_COLLAPSED_SIZE.get();
		int expandedSize = Icons.LAYOUT_ADJUSTMENT_GRABBER_EXPANDED_SIZE.get();
		out.beginBeginTag(DIV);
		
		out.beginAttribute(ID_ATTR);
		writeSeparatorId(out, self.getID(), separatorID);
		out.endAttribute();

		out.writeAttribute(CLASS_ATTR, separatorClass(orientation, collapsed));
		writeOnMouseDown(out, self, separatorID, orientation);
		writeLayoutConstraintInformation(expandedSize, DisplayUnit.PIXEL, out);
		writeLayoutCollapsedProperty(out, collapsedSize);
		writeCollapsedProperty(out, collapsed);
		writeLayoutInformationAttribute(orientation, 100, out);
		out.endBeginTag();
		out.endTag(DIV);
	}

	/**
	 * Writes client information for the collapsing state of {@link CollapsibleControl}
	 * 
	 * @param out
	 *        the writer to write to
	 * @param collapsedSize
	 *        the size which is occupied by the control in collapsed state on client-side in px.
	 * 
	 * @throws IOException
	 *         iff the writer throws some
	 */
	public static void writeLayoutCollapsedProperty(TagWriter out, int collapsedSize) throws IOException {
		out.beginAttribute(CollapsibleControl.DATA_COLLAPSED_SIZE_ATTR);
		{
			writeConstraint(out, collapsedSize, 0, DisplayUnit.PIXEL);
		}
		out.endAttribute();
	}

	/**
	 * Writes a {@link CollapsibleControl#DATA_COLLAPSED_ATTR}.
	 */
	public static void writeCollapsedProperty(TagWriter out, boolean collapsed) {
		out.writeAttribute(CollapsibleControl.DATA_COLLAPSED_ATTR, collapsed);
	}

	private String separatorClass(Orientation orientation, boolean collapsed) {
		if (orientation.isHorizontal()) {
			return
				collapsed ? "layoutHorizontalAdjustmentGrabber fflCollapsed" : "layoutHorizontalAdjustmentGrabber";
		} else {
			return collapsed ? "layoutVerticalAdjustmentGrabber fflCollapsed" : "layoutVerticalAdjustmentGrabber";
		}
	}

	private void writeOnMouseDown(TagWriter out, FlexibleFlowLayoutControl control, int separatorID,
			Orientation orientation) throws IOException {
		out.beginAttribute(ONMOUSEDOWN_ATTR);
		out.append("services.form.FlexibleFlowLayout.initLayoutAdjustment(event, '");
		out.append(control.getID());
		out.append("',");
		out.writeInt(separatorID);
		out.append(",  this, '");
		out.append(control.getResizeMode().getExternalName());
		out.append("', ");
		out.append(Boolean.toString(orientation.isHorizontal()));
		out.append(");");
		out.endAttribute();
	}

	/**
	 * Produces updates after collapsing/expanding a separator.
	 * 
	 * @param actions
	 *        The {@link UpdateQueue} deliver actions to.
	 * @param controlId
	 *        The {@link FlexibleFlowLayoutControl#getID()} of the control.
	 * @param orientation
	 *        The layout orientation.
	 * @param index
	 *        The index of the separator to update.
	 * @param collapsed
	 *        Whether the separator is collapsed.
	 */
	public void updateSeparatorCollapsed(UpdateQueue actions, String controlId, Orientation orientation, int index,
			boolean collapsed) {
		String id = separatorId(controlId, index);
		actions.add(new PropertyUpdate(id, CLASS_PROPERTY,
			new ConstantDisplayValue(separatorClass(orientation, collapsed))));
		actions.add(
			new PropertyUpdate(id, CollapsibleControl.DATA_COLLAPSED_ATTR,
				new ConstantDisplayValue(Boolean.toString(collapsed))));
	}

	/**
	 * The ID of the separator with the given index.
	 * 
	 * @param controlId
	 *        The {@link FlexibleFlowLayoutControl#getID()} of the control.
	 * @param index
	 *        The index of the separator.
	 * @return The client-side ID of the separator element.
	 */
	private String separatorId(String controlId, int index) {
		return controlId + '-' + index;
	}

	/**
	 * Optimized variant of writing {@link #separatorId(String, int)} to the given output.
	 */
	private void writeSeparatorId(Appendable out, String controlId, int separatorID) throws IOException {
		out.append(controlId);
		out.append('-');
		StringServices.append(out, separatorID);
	}

}
