/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.awt.dnd.DropTarget;
import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.component.AJAXComponent;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOpener;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOwner;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.component.dnd.ComponentDropEvent;
import com.top_logic.layout.component.dnd.ComponentDropTarget;
import com.top_logic.layout.dnd.DnD;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link LayoutControl} acting as a placeholder for a component that fills the content with
 * dynamically created layouts.
 */
public class ComponentWrappingControl extends WrappingControl<ComponentWrappingControl> implements ContextMenuOwner {

	/**
	 * Factory method for {@link ComponentWrappingControl}s.
	 * 
	 * @param component
	 *        The context component being displayed.
	 */
	public static ComponentWrappingControl create(LayoutComponent component) {
		return create(component, NoContextMenuProvider.INSTANCE);
	}

	/**
	 * Factory method for {@link ComponentWrappingControl}s.
	 * 
	 * @param component
	 *        The context component being displayed.
	 * @param contextMenu
	 *        The global context menu for the component's background.
	 */
	public static ComponentWrappingControl create(LayoutComponent component, ContextMenuProvider contextMenu) {
		return new ComponentWrappingControl(component, contextMenu, COMMANDS);
	}

	private final Property<LayoutComponent> FORMER_CONTEXT_COMPONENT =
		TypedAnnotatable.property(LayoutComponent.class, "formerContextComponent");

	/**
	 * Built-in commands of {@link ComponentWrappingControl}.
	 */
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		ComponentInspector.INSTANCE,
		ComponentDropAction.INSTANCE,
		ContextMenuOpener.INSTANCE);

	private final LayoutComponent _component;

	private ContextMenuProvider _contextMenu;

	/**
	 * @see #create(LayoutComponent, ContextMenuProvider)
	 * @see AbstractLayoutControl#AbstractLayoutControl(Map)
	 */
	protected ComponentWrappingControl(LayoutComponent aBusinessComponent, ContextMenuProvider contextMenu,
			Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		_component = aBusinessComponent;
		_contextMenu = contextMenu;
		setConstraint(DefaultLayoutData.DEFAULT_CONSTRAINT);
	}

	@Override
	public LayoutComponent getModel() {
		return _component;
	}

	/**
	 * the {@link LayoutComponent} which is written by this {@link ComponentWrappingControl}
	 */
	public final LayoutComponent getBusinessComponent() {
		return _component;
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);
		out.writeAttribute(TL_CONTEXT_MENU_ATTR, getID());
	}

	@Override
	public Menu createContextMenu(String contextInfo) {
		return _contextMenu.getContextMenu(_component.getModel());
	}

	@Override
	protected void beforeRendering(DisplayContext context) {
		super.beforeRendering(context);
		context.set(FORMER_CONTEXT_COMPONENT, MainLayout.getComponent(context));
		LayoutUtils.setContextComponent(context, _component);
	}

	/**
	 * Calls {@link LayoutComponent#afterRendering() afterRendering} of the business component.
	 * 
	 * @see AbstractLayoutControl#afterRendering(DisplayContext)
	 */
	@Override
	protected void afterRendering(DisplayContext context) {
		LayoutUtils.setContextComponent(context, context.reset(FORMER_CONTEXT_COMPONENT));
		_component.afterRendering();
		super.afterRendering(context);
	}

	/**
	 * attaches a listener to its {@link #getBusinessComponent() business component} to react on
	 * {@link LayoutComponent#invalidate()}.
	 * 
	 * @see AbstractControlBase#internalAttach()
	 */
	@Override
	protected void internalAttach() {
		super.internalAttach();
		_component.addInvalidationListener(this);
		_component.getEnclosingFrameScope().setUrlContext(getScope().getFrameScope());
	}

	@Override
	protected void revalidateControl(DisplayContext context, UpdateQueue actions) {
		LayoutComponent formerContext = MainLayout.getComponent(context);
		LayoutUtils.setContextComponent(context, _component);
		try {
			super.revalidateControl(context, actions);
		} finally {
			LayoutUtils.setContextComponent(context, formerContext);
		}
	}

	/**
	 * detaches the listener added in {@link #internalAttach()}
	 * 
	 * @see #internalAttach()
	 * @see AbstractControlBase#internalDetach()
	 */
	@Override
	protected void internalDetach() {
		_component.getEnclosingFrameScope().dropUrlContext();
		_component.removeInvalidationListener(this);
		if (_component instanceof AJAXComponent) {
			((AJAXComponent) _component).invalidateAJAXSupport();
		}
		super.internalDetach();
	}

	/**
	 * Whether the {@link DropTarget} of the displayed component is enabled.
	 */
	public boolean dropEnabled() {
		LayoutComponent component = getBusinessComponent();
		ComponentDropTarget dropTarget = component.getConfig().getDropTarget();
		return dropTarget.dropEnabled(component);
	}

	/**
	 * {@link ControlCommand} implements a generic drop on a component.
	 */
	public static class ComponentDropAction extends ControlCommand {

		private static final String COMMAND_ID = "dndDrop";

		/**
		 * Singleton {@link ComponentDropAction} instance.
		 */
		public static final ComponentDropAction INSTANCE = new ComponentDropAction();

		private ComponentDropAction() {
			super(COMMAND_ID);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.COMPONENT_DROP;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			ComponentWrappingControl contentControl = (ComponentWrappingControl) control;
			LayoutComponent component = contentControl.getBusinessComponent();
			ComponentDropTarget dropTarget = component.getConfig().getDropTarget();
			if (!dropTarget.dropEnabled(component)) {
				throw new TopLogicException(com.top_logic.layout.dnd.I18NConstants.DROP_NOT_POSSIBLE);
			}

			DndData data = DnD.getDndData(commandContext, arguments);
			if (data != null) {
				dropTarget.handleDrop(new ComponentDropEvent(data, component));
			}

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	@Override
	public ComponentWrappingControl self() {
		return this;
	}

}
