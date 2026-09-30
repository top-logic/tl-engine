/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.buttonbar;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.ButtonBar;
import com.top_logic.knowledge.gui.layout.I18NConstants;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateListener;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.DeferredPopupMenuModel;
import com.top_logic.layout.basic.PopupMenuModel;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.layout.form.control.AbstractCompositeControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.IButtonRenderer;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCompositeControl} that renders a button bar.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ButtonBarControl extends AbstractCompositeControl<ButtonBarControl> implements ModelChangeListener {

	/** Type CSS class for {@link ButtonBarControl}. */
	public static final String BUTTON_BAR_CSS_CLASS = "cmdBody";

	private class ButtonMenuProvider implements Provider<List<List<CommandModel>>> {

		private int _hiddenButtonCount = 0;

		ButtonMenuProvider() {
			// Narrow visibility
		}

		@Override
		public List<List<CommandModel>> get() {
			List<CommandModel> overallButtons = getButtonBarModel().getButtons();
			List<CommandModel> popupMenuButtons = new LinkedList<>();
			for (int i = 0; i < _hiddenButtonCount; i++) {
				CommandModel button = overallButtons.get(i);
				if (button.isVisible()) {
					popupMenuButtons.add(button);
				}
			}
			return Collections.singletonList(popupMenuButtons);
		}

		/**
		 * Setter for {@link #getHiddenButtonCount()}
		 */
		public void setHiddenButtonCount(int hiddenButtonCount) {
			_hiddenButtonCount = hiddenButtonCount;
		}

		/**
		 * Number of hidden buttons.
		 */
		public int getHiddenButtonCount() {
			return _hiddenButtonCount;
		}

	}

	private static class VisibleButtonCountCommand extends ControlCommand {

		public VisibleButtonCountCommand(String command) {
			super(command);
		}

		@Override
		protected boolean executeCommandIfViewDisabled() {
			return true;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			ButtonBarControl buttonBarControl = (ButtonBarControl) control;
			int hiddenButtonCount = ((Integer) arguments.get("hiddenElements"));
			if (hiddenButtonCount <= getOverallButtonCount(buttonBarControl)) {
				buttonBarControl.getMenuProvider().setHiddenButtonCount(hiddenButtonCount);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		public int getOverallButtonCount(ButtonBarControl buttonBarControl) {
			List<CommandModel> buttons = buttonBarControl.getButtonBarModel().getButtons();
			if (buttons != null) {
				return buttons.size();
			} else {
				return 0;
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.VISIBLE_BUTTON_COUNT;
		}
	}

	/**
	 * JavaScript identifier of of the button component's slider that is enabled during a command is
	 * being executed.
	 */
	private static final String PROGRESS_DIV_SUFFIX = "_progressDiv";

	static final String VISIBLE_BUTTON_COUNT_COMMAND = "visibleButtonCountCommand";

	private static final Map<String, ControlCommand> COMMANDS = AbstractControlBase.createCommandMap(
		new ControlCommand[] {
			new VisibleButtonCountCommand(VISIBLE_BUTTON_COUNT_COMMAND)
		});

	private ButtonBarModel _buttonBarModel;

	private ButtonMenuProvider _menuProvider;

	/**
	 * Create a new {@link ButtonBarControl}
	 * 
	 * @see ButtonBarFactory#createButtonBar(ButtonBarModel)
	 */
	ButtonBarControl(ButtonBarModel buttonBarModel) {
		super(COMMANDS);
		_buttonBarModel = buttonBarModel;
		_menuProvider = new ButtonMenuProvider();
	}

	@Override
	public ButtonBarModel getModel() {
		return _buttonBarModel;
	}

	@Override
	public boolean isVisible() {
		return getButtonBarModel().isVisible();
	}

	@Override
	protected String getTypeCssClass() {
		return BUTTON_BAR_CSS_CLASS;
	}

	@Override
	protected void internalDetach() {
		getButtonBarModel().removeModelChangeListener(this);
		super.internalDetach();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		initializeChildren();
		getButtonBarModel().addModelChangeListener(this);
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		super.internalRevalidate(context, actions);
		actions.add(new JSSnipplet("(function(){"
			+ "var tabBar = document.getElementById('" + getID() + "');"
			+ "tabBar.layout.render();"
			+ "})()"));
	}

	@Override
	protected boolean hasUpdates() {
		for (HTMLFragment child : getChildren()) {
			if (child instanceof UpdateListener) {
				boolean invalid = ((UpdateListener) child).isInvalid();
				if (invalid) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
		initializeChildren();
		return Bubble.BUBBLE;
	}

	private void initializeChildren() {
		// remove old buttons
		removeChildren();
		// re-create buttons
		List<CommandModel> buttons = getButtonBarModel().getButtons();
		if (buttons != null) {
			for (CommandModel button : buttons) {
				addChild(createButtonControl(button));
			}
		}
	}

	private Control createButtonControl(CommandModel theModel) {
		if (theModel.showProgress()) {
			theModel.set(ButtonControl.SHOW_PROGRESS_DIV_ID, new AbstractDisplayValue() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					appendProgressDivID(out);
				}
			});
		}

		ControlProvider controlProvider = theModel.get(ButtonBar.BUTTON_CONTROL_PROVIDER);
		Control control;
		if (controlProvider != null) {
			control = controlProvider.createControl(theModel);
		} else {
			IButtonRenderer renderer = theModel.get(ButtonControl.BUTTON_RENDERER);
			if (renderer == null) {
				renderer = ButtonComponentButtonRenderer.INSTANCE;
			}
			control = new ButtonControl(theModel, renderer);
		}
		return control;
	}

	/**
	 * This method returns the ID of the progress div written by this {@link ButtonBarControl}.
	 * <p>
	 * This must not be called before the rendering of the content of this {@link ButtonBarControl}
	 * has started.
	 * </p>
	 */
	public final void appendProgressDivID(Appendable out) throws IOException {
		out.append(getID());
		out.append(PROGRESS_DIV_SUFFIX);
	}

	ButtonMenuProvider getMenuProvider() {
		return _menuProvider;
	}

	ButtonBarModel getButtonBarModel() {
		return _buttonBarModel;
	}

	@Override
	public ButtonBarControl self() {
		return this;
	}

	/**
	 * Number of currently hidden buttons.
	 */
	@TemplateVariable("hiddenButtonCount")
	public int hiddenButtonCount() {
		return getMenuProvider().getHiddenButtonCount();
	}

	/**
	 * Writes a JavaScript function that makes the button bar adapt to the changed browser size.
	 * 
	 * @param out
	 *        Writes data-resize attribute.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("dataResize")
	public void writeResizeFunction(TagWriter out) throws IOException {
		out.append("services.viewport.initMenuDisplay(" + getJSONViewportReferences() + ")");
	}

	private String getJSONViewportReferences() {
		String controlID = getID();
		return "{"
			+ "controlID:'" + controlID + "',"
			+ "visibleButtonCountCommand:'" + VISIBLE_BUTTON_COUNT_COMMAND + "',"
			+ "viewport:'" + controlID + "',"
			+ "elementContainer:'" + controlID + "-buttonContainer" + "',"
			+ "elementSizeContainer:'" + controlID + "-buttonContainer" + "',"
			+ "scrollContainer:'" + controlID + "-buttonScrollContainer" + "',"
			+ "buttonSpaceHolderContainer:'" + controlID + "-buttonSpaceHolderContainer" + "',"
			+ "buttonDialogOpener:'" + controlID + "-buttonDialogOpener" + "',"
			+ "buttonDialogOpenerImage:'" + controlID + "-buttonDialogOpenerImage" + "',"
			+ "visibleElement:'" + "containerButton" + "'}";
	}

	/**
	 * If the browser screen does not have enough space to display all the buttons, an arrow is
	 * displayed. The arrow can be clicked to access the buttons that are not displayed on the
	 * screen.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        For writing the buttons.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("dialogOpener")
	public void writeButtonDialogOpener(DisplayContext context, TagWriter out) throws IOException {
		PopupMenuButtonControl menuControl = newPopupMenuControl();
		menuControl.fetchID(context.getExecutionScope().getFrameScope());
		menuControl.write(context, out);
	}

	private PopupMenuButtonControl newPopupMenuControl() {
		PopupMenuModel menuModel = new DeferredPopupMenuModel(getMenuProvider());
		menuModel.setImage(com.top_logic.layout.tabbar.Icons.TAB_LEFT_SCROLL_BUTTON);
		PopupMenuButtonControl menuControl =
			new PopupMenuButtonControl(menuModel, ImageButtonRenderer.newSystemButtonRenderer("bcPopup"));
		return menuControl;
	}

	/**
	 * Writes the user-configured CSS classes inherent to this control.
	 * 
	 * @param out
	 *        For writing the buttons.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("scrollContainerClasses")
	public void writeButtonScrollContainerCssClasses(TagWriter out) throws IOException {
		DefaultButtonBarRenderer defaultButtonBarRenderer = (DefaultButtonBarRenderer) getRenderer();
		out.append(defaultButtonBarRenderer.getMasterClass());
		out.append("buttonScrollContainer");
	}

	/**
	 * Writes the buttons.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        For writing the buttons.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("buttons")
	public void writeButtons(DisplayContext context, TagWriter out) throws IOException {
		List<? extends HTMLFragment> buttons = getChildren();
		if (buttons != null) {
			for (int i = 0, size = buttons.size(); i < size; i++) {
				HTMLFragment button = buttons.get(i);
				writeButton(context, out, button);
			}
		}
	}

	/**
	 * 
	 * Renders one button for a button bar.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        For writing the buttons.
	 * @param button
	 *        Current button in loop {@link ButtonBarControl#writeButtons}
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	public void writeButton(DisplayContext context, TagWriter out, HTMLFragment button) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "containerButton");
		out.endBeginTag();
		button.write(context, out);
		out.endTag(SPAN);
	}

	/**
	 * Writes an animated image at the place where the buttons should be if the button commands are
	 * not ready yet.
	 */
	@TemplateVariable("imageSlider")
	public void writeImageSlider(DisplayContext context, TagWriter out) throws IOException {
		Icons.SLIDER_IMG.get().write(context, out);
	}

}