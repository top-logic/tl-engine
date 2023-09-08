/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.layoutRenderer.FlexibleFlowLayoutRenderer;
import com.top_logic.layout.structure.Expandable.ExpansionState;
import com.top_logic.mig.html.layout.Layout.LayoutResizeMode;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link FlowLayoutControl}, which gives the user the opportunity to change the horizontal or
 * vertical size interactively.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FlexibleFlowLayoutControl extends FlowLayoutControl<FlexibleFlowLayoutControl>
		implements ExpandableListener {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(UpdateLayoutSizeCommand.INSTANCE);

	private LayoutResizeMode resizeMode;

	private boolean _childCollapsedChanged;

	/**
	 * Creates a {@link FlexibleFlowLayoutControl}.
	 *
	 * @param resizeMode
	 *        See {@link #getResizeMode()}.
	 * @param orientation
	 *        See {@link #getOrientation()}.
	 */
	public FlexibleFlowLayoutControl(LayoutResizeMode resizeMode, Orientation orientation) {
		super(orientation, COMMANDS);
		this.resizeMode = resizeMode;
	}

	@Override
	public Object getModel() {
		return null;
	}

	/**
	 * the way a resizable layout will be resized
	 */
	public final LayoutResizeMode getResizeMode() {
		return resizeMode;
	}

	@Override
	protected void childAdded(LayoutControl newChild) {
		newChild.addListener(Expandable.STATE, this);
	}

	@Override
	protected void childRemoved(LayoutControl oldChild) {
		oldChild.removeListener(Expandable.STATE, this);
	}

	@Override
	public Bubble notifyExpansionStateChanged(Expandable sender, ExpansionState oldValue, ExpansionState newValue) {
		if (newValue == Expandable.ExpansionState.MAXIMIZED
			|| oldValue == Expandable.ExpansionState.MAXIMIZED) {
			return Bubble.BUBBLE;
		}

		Expandable changedChild = sender;
		List<? extends LayoutControl> children = getChildren();
		int changedIndex = children.indexOf(changedChild);
		if (changedIndex < 0) {
			return Bubble.BUBBLE;
		}

		if (isAttached()) {
			_childCollapsedChanged = true;
		}
		return Bubble.BUBBLE;
	}

	@Override
	protected boolean hasUpdates() {
		return super.hasUpdates() || _childCollapsedChanged;
	}

	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();

		// Clear cached state.
		_childCollapsedChanged = false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		super.internalRevalidate(context, actions);

		_childCollapsedChanged = false;

		List<? extends LayoutControl> children = getChildren();
		int lastIndex = children.size() - 1;
		boolean allCollapsedAtTheRight = true;
		for (int index = children.size() - 1; index >= 0; index--) {
			LayoutControl child = children.get(index);

			boolean collapsed = isCollapsed(child);
			boolean resizable = child.isResizable();

			// Update the separator.
			if (index < lastIndex && resizable && children.get(index + 1).isResizable()) {
				boolean separatorCollapsed = collapsed || allCollapsedAtTheRight;
				((FlexibleFlowLayoutRenderer) getRenderer()).updateSeparatorCollapsed(actions, getID(),
					getOrientation(),
					index, separatorCollapsed);
			}

			// Update the aggregate allCollapsedAtTheRight.
			if (!collapsed && resizable) {
				allCollapsedAtTheRight = false;
			}
		}
	}

	/**
	 * Whether the given child control is considered to be collapsed.
	 * 
	 * <p>
	 * Sibling of a collapsed child cannot be resized.
	 * </p>
	 */
	public boolean isCollapsed(LayoutControl child) {
		return ((child instanceof CollapsibleControl) && (((CollapsibleControl) child).isCollapsed())) ||
			((child instanceof Expandable) && ((Expandable) child).getExpansionState() == ExpansionState.MINIMIZED);
	}

	/**
	 * {@link ControlCommand} updating the user defined size {@link FixedFlowLayoutControl} children
	 * a change on the UI.
	 */
	public static class UpdateLayoutSizeCommand extends ControlCommand {

		static final UpdateLayoutSizeCommand INSTANCE = new UpdateLayoutSizeCommand();

		private static final String LAYOUT_SIZES_ID = "layoutSizes";

		private UpdateLayoutSizeCommand() {
			super("updateLayoutSizes");
		}

		@SuppressWarnings("unchecked")
		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			FlexibleFlowLayoutControl flowControl = (FlexibleFlowLayoutControl) control;

			Map<String, Integer> layoutSizes = (Map<String, Integer>) arguments.get(LAYOUT_SIZES_ID);

			for (Entry<String, Integer> layoutSize : layoutSizes.entrySet()) {

				String currentID = layoutSize.getKey();
				int currentLayoutSize = layoutSize.getValue();

				LayoutControl currentChild = null;
				List<? extends LayoutControl> children = flowControl.getChildren();
				for (LayoutControl child : children) {
					if (child.getID().equals(currentID)) {
						currentChild = child;
						break;
					}
				}
				if (flowControl.getOrientation().isHorizontal()) {
					if (currentChild != null) {
						LayoutData previousConstraint = currentChild.getConstraint();
						DisplayDimension newLeftChildWidth =
							DisplayDimension.dim(currentLayoutSize, previousConstraint.getWidthUnit());
						currentChild.setSize(newLeftChildWidth, previousConstraint.getHeightDimension());
					}
				} else {
					if (currentChild != null) {
						LayoutData previousConstraint = currentChild.getConstraint();
						DisplayDimension newUpperChildHeight =
							DisplayDimension.dim(currentLayoutSize, previousConstraint.getHeightUnit());
						currentChild.setSize(previousConstraint.getWidthDimension(), newUpperChildHeight);
					}
				}
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_LAYOUT_SIZE;
		}
	}

	@Override
	protected ControlRenderer<? super FlexibleFlowLayoutControl> createDefaultRenderer() {
		return FlexibleFlowLayoutRenderer.INSTANCE;
	}

	@Override
	public FlexibleFlowLayoutControl self() {
		return this;
	}
}
