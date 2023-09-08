/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.layoutRenderer.DefaultCollapsibleRenderer;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.scripting.recorder.DynamicRecordable;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.layout.toolbar.TitleControl;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolbarControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * The class {@link CollapsibleControl} is a special {@link WrappingControl} which allows to
 * collapse the content of child control.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CollapsibleControl extends AbstractMaximizableControl<CollapsibleControl> {

	/**
	 * HTML5 data attribute marking a {@link LayoutControl} as collapsed/minimized.
	 */
	public static final String DATA_COLLAPSED_ATTR = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "collapsed";

	/**
	 * HTML5 data attribute providing the size of a {@link LayoutControl} in collapsed/minimized
	 * state.
	 */
	public static final String DATA_COLLAPSED_SIZE_ATTR = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "collapsed-size";

	private static final Map<String, ControlCommand> NO_COMMANDS = Collections.<String, ControlCommand> emptyMap();

	private ToggleMinimized _collapse;

	private ToggleMaximized _maximize;

	private final ToolBar _toolbar;

	/**
	 * Creates a {@link CollapsibleControl}.
	 * 
	 * @param titleKey
	 *        Resource key to render in the title area.
	 * @param model
	 *        The {@link Expandable} model to observe.
	 * @param canMaximize
	 *        See {@link ToolBar#canMaximize()}.
	 * @param showMaximize
	 *        See {@link ToolBar#showMaximize()}.
	 * @param showMinimize
	 *        See {@link ToolBar#showMinimize()}.
	 * @see AbstractLayoutControl#AbstractLayoutControl(Map)
	 */
	public CollapsibleControl(ResKey titleKey, Expandable model, boolean canMaximize,
			Decision showMaximize, Decision showMinimize) {
		this(titleKey, model, NO_COMMANDS, canMaximize, showMaximize, showMinimize);
	}

	/**
	 * Creates a {@link CollapsibleControl}.
	 * 
	 * @param titleKey
	 *        Resource key to render in the title area.
	 * @param model
	 *        The {@link Expandable} model to observe.
	 * @see AbstractLayoutControl#AbstractLayoutControl(Map)
	 */
	protected CollapsibleControl(ResKey titleKey, Expandable model,
			Map<String, ControlCommand> commandsByName,
			boolean canMaximize, Decision showMaximize, Decision showMinimize) {
		super(model, commandsByName);

		_toolbar = new DefaultToolBar(this, model, new ResourceText(titleKey), canMaximize, showMaximize, showMinimize);
		_maximize = new ToggleMaximized();
		_collapse = new ToggleMinimized();
	}

	/**
	 * Creates a {@link CollapsibleControl} for an existing {@link ToolBar} model.
	 *
	 * @param toolbar
	 *        See {@link #getToolbar()}.
	 */
	public CollapsibleControl(ToolBar toolbar) {
		super(toolbar.getModel(), NO_COMMANDS);

		_toolbar = toolbar;
		_maximize = new ToggleMaximized();
		_collapse = new ToggleMinimized();
	}

	/**
	 * The {@link ToolBar} of this layout.
	 */
	public ToolBar getToolbar() {
		return _toolbar;
	}

	/**
	 * Writes the toolbar that contains the buttons next to the title in the header.
	 * 
	 * @return {@link ToolbarControl} which contains the buttons.
	 */
	@TemplateVariable("toolbar")
	public ToolbarControl getToolbarControl() {
		return new ToolbarControl(_toolbar);
	}

	/**
	 * Writes the title.
	 * 
	 * @return {@link TitleControl} which writes the header.
	 */
	@TemplateVariable("title")
	public TitleControl getTitleControl() {
		return new TitleControl(_toolbar, _toolbar.getTitle());
	}

	@Override
	protected boolean canMaximize() {
		return _toolbar.canMaximize();
	}

	/**
	 * @see #canMaximize()
	 */
	public void setCanMaximize(boolean canMaximize) {
		_toolbar.setCanMaximize(canMaximize);
	}

	@Override
	protected boolean canCollapse() {
		return true;
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		updateButtons();
	}

	@Override
	protected void internalNotifyExpansionStateChanged(Expandable sender, ExpansionState oldValue,
			ExpansionState newValue) {
		super.internalNotifyExpansionStateChanged(sender, oldValue, newValue);
		updateButtons();
	}

	private void updateButtons() {
		if (_toolbar.showMinimize()) {
			_collapse.update();
		}
		if (_toolbar.showMaximize()) {
			_maximize.update();
		}
	}

	/**
	 * Whether this control is collapsed/minimized.
	 */
	@TemplateVariable("collapsed")
	public final boolean isCollapsed() {
		return canCollapse() && getExpansionState() == Expandable.ExpansionState.MINIMIZED;
	}

	/**
	 * The minimize button, if {@link ToolBar#showMinimize()}, <code>null</code> otherwise.
	 */
	public CommandModel getCollapse() {
		return _collapse;
	}

	/**
	 * The maximize button, if {@link ToolBar#showMaximize()}, <code>null</code> otherwise.
	 */
	public ToggleMaximized getMaximize() {
		return _maximize;
	}

	private abstract class Toggle extends AbstractCommandModel implements DynamicRecordable {

		public Toggle() {
			update();
		}

		protected void setExpansionState(DisplayContext context, ExpansionState state) {
			CollapsibleControl.this.setExpansionState(state);
			if (ScriptingRecorder.isRecordingActive()) {
				Maybe<? extends ModelName> ownerName = ModelResolver.buildModelNameIfAvailable(
					DefaultDisplayContext.getDisplayContext().getWindowScope(), getToolbar().getModel());
				if (ownerName.hasValue()) {
					ScriptingRecorder.recordCollapseToolbar(ownerName.get(), state);
				}
			}
		}

		protected abstract void update();

	}

	private final class ToggleMinimized extends Toggle {

		public ToggleMinimized() {
			super();
		}

		@Override
		protected HandlerResult internalExecuteCommand(DisplayContext context) {
			setCollapsed(context, !isCollapsed());
			return HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * This method sets the server side state of the control. It is not necessary that after
		 * setting a new server side collapse state, the value returned by {@link #isCollapsed()} is
		 * in sync with the new value.
		 * 
		 * @see #isCollapsed()
		 * @see #reset()
		 */
		void setCollapsed(DisplayContext context, boolean collapsed) {
			setExpansionState(context,
				collapsed ? Expandable.ExpansionState.MINIMIZED : Expandable.ExpansionState.NORMALIZED);
		}

		@Override
		protected void update() {
			boolean collapsed = isCollapsed();

			setImage(collapsed ? com.top_logic.layout.structure.Icons.WINDOW_NORMALIZE : Icons.WINDOW_MINIMIZE);
			ResKey tooltipKey = collapsed ? I18NConstants.EXPAND_IMAGE_TEXT : I18NConstants.COLLAPSE_IMAGE_TEXT;
			setTooltip(Resources.getInstance().getString(tooltipKey));
			setLabel(Resources.getInstance().getString(tooltipKey));
		}

	}

	private final class ToggleMaximized extends Toggle {

		public ToggleMaximized() {
			super();
		}

		@Override
		protected HandlerResult internalExecuteCommand(DisplayContext context) {
			boolean maximized = isMaximized();
			setMaximized(context, !maximized);
			return HandlerResult.DEFAULT_RESULT;
		}

		private void setMaximized(DisplayContext context, boolean maximize) {
			setExpansionState(context,
				maximize ? Expandable.ExpansionState.MAXIMIZED : Expandable.ExpansionState.NORMALIZED);
		}

		@Override
		protected void update() {
			update(isMaximized());
		}

		private void update(boolean maximized) {
			setImage(maximized ? com.top_logic.layout.structure.Icons.WINDOW_NORMALIZE
				: com.top_logic.layout.structure.Icons.WINDOW_MAXIMIZE);
			ResKey tooltipKey = maximized ? I18NConstants.RESTORE_WINDOW : I18NConstants.MAXIMIZE_WINDOW;
			setTooltip(Resources.getInstance().getString(tooltipKey));
			setLabel(Resources.getInstance().getString(tooltipKey));
		}
	}

	@Override
	protected ControlRenderer<? super CollapsibleControl> createDefaultRenderer() {
		return DefaultCollapsibleRenderer.INSTANCE;
	}

	@Override
	public CollapsibleControl self() {
		return this;
	}

	/**
	 * The orientation this control collapses.
	 */
	@Override
	public Orientation getOrientation() {
		return getParent().getOrientation();
	}

	/**
	 * The layout information of this control to be written to the
	 * {@value LayoutControlRenderer#LAYOUT_DATA_ATTRIBUTE} attribute.
	 */
	@TemplateVariable("layout")
	public void writeLayoutInformation(TagWriter out) throws IOException {
		LayoutControlRenderer.writeLayoutInformation(out, Orientation.VERTICAL, 100);
	}

	/**
	 * Writes client-side data for the collapsed state of the {@value #DATA_COLLAPSED_SIZE_ATTR}
	 * attribute.
	 */
	@TemplateVariable("collapsedSize")
	public void writeCollapsedSize(TagWriter out) throws IOException {
		LayoutControlRenderer.writeConstraint(out,
			com.top_logic.layout.structure.Icons.COLLAPSIBLE_IMAGE_WIDTH.get(), 0);
	}

	/**
	 * Writes client-side data for the {@value LayoutControlRenderer#LAYOUT_SIZE_DATA_ATTRIBUTE}
	 * attribute.
	 */
	@TemplateVariable("toolbarLayoutSize")
	public void writeToolbarLayoutInformation(TagWriter out) throws IOException {
		DisplayDimension size;
		if (isHorizontal()) {
			size = com.top_logic.layout.structure.Icons.COLLAPSIBLE_IMAGE_WIDTH.get();
		} else {
			size = com.top_logic.layout.structure.Icons.COLLAPSIBLE_IMAGE_HEIGHT.get();
		}
		LayoutControlRenderer.writeConstraint(out, size.getValue(), 0, size.getUnit());
	}

	/**
	 * Whether the collapse button should be rendered.
	 */
	@TemplateVariable("showCollapse")
	public boolean showCollapse() {
		boolean showCollapse = false;
		LayoutControl parent = getParent();
		if (parent instanceof FlowLayoutControl) {
			if (!isMaximized() && getToolbar().showMinimize()) {
				showCollapse = true;
			}
		}
		return showCollapse;
	}

	/**
	 * The button that minimizes the view by collapsing the contents.
	 */
	@TemplateVariable("collapseButton")
	public ButtonControl getCollapseButton() {
		return new ButtonControl(getCollapse());
	}

	/**
	 * Whether a maximize button should be rendered.
	 */
	@TemplateVariable("showMaximize")
	public boolean showMaximize() {
		boolean showMaximize = false;
		if (!isCollapsed()) {
			if (hasCockpit() && getToolbar().showMaximize()) {
				showMaximize = true;
			}
		}
		return showMaximize;
	}

	/**
	 * The button that allows maximizing the contents.
	 */
	@TemplateVariable("maximizeButton")
	public ButtonControl getMaximizeButton() {
		return new ButtonControl(getMaximize());
	}
}
