/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.component.configuration.OpenGuiInspectorFragment;
import com.top_logic.layout.component.configuration.ToolRowCommandRenderer;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.tag.js.JSBoolean;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.layoutRenderer.DialogRenderer;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolbarControl;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.css.CssUtil;

/**
 * The class {@link DialogWindowControl} represents a dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DialogWindowControl extends WindowControl<DialogWindowControl> implements DialogClosedListener {

	/**
	 * Property that is set to the {@link DisplayContext} when the "maxmimized changed" event is
	 * triggered by the client. In this case it is not necessary to repaint control, because client
	 * side is already up to date.
	 */
	private static final Property<Boolean> TRIGGERED_BY_CLIENT =
		TypedAnnotatable.property(Boolean.class, "triggered by client");

	private static final String DIALOG_CONTROL_CSS_CLASS = "dlgDialog";

	private static final ResKey MAXIMIZE_DIALOG = com.top_logic.layout.layoutRenderer.I18NConstants.MAXIMIZE_DIALOG;

	private static final ResKey RESTORE_DIALOG = com.top_logic.layout.layoutRenderer.I18NConstants.RESTORE_DIALOG;

	private static final ResKey CLOSE_DIALOG = com.top_logic.layout.layoutRenderer.I18NConstants.CLOSE_DIALOG;

	private static final Map<String, ControlCommand> DIALOG_COMMANDS =
		createCommandMap(CloseDialogCommand.INSTANCE, UpdateDialogSizeCommand.INSTANCE,
			UpdateMaximizedCommand.INSTANCE);

	/**
	 * Instance to write the command to run the gui inspector in the title bar as a clickable
	 * button.
	 */
	public static final OpenGuiInspectorFragment OPEN_GUI_INSPECTOR_FRAGMENT = new OpenGuiInspectorFragment(
		ToolRowCommandRenderer.INSTANCE,
		Icons.BUTTON_INSPECTOR,
		com.top_logic.layout.layoutRenderer.I18NConstants.GUI_INSPECTOR);

	/**
	 * Creates a new {@link DialogWindowControl} based on the given {@link DialogModel}.
	 * 
	 * @param dialogModel
	 *        The new model of this control
	 * @see WindowControl#WindowControl(WindowModel, Map)
	 */
	public DialogWindowControl(DialogModel dialogModel) {
		super(dialogModel, DIALOG_COMMANDS);
		if (dialogModel.isClosed()) {
			/* dialog models can not be reopened. If this Control is displayed, it never disappears
			 * from the client */
			throw new IllegalArgumentException("Dialog model must not already be closed.");
		}
		getDialogModel().addListener(DialogModel.CLOSED_PROPERTY, this);
	}
	
	/**
	 * This method returns the {@link BrowserWindowControl} which opened this
	 * {@link DialogWindowControl}.
	 * 
	 * @return the dialog parent. never <code>null</code>.
	 */
	public BrowserWindowControl getDialogParent() {
		return (BrowserWindowControl) this.getParent();
	}

	@Override
	protected String getTypeCssClass() {
		return DIALOG_CONTROL_CSS_CLASS;
	}

	/**
	 * This method returns the command for lay out this
	 * {@link DialogWindowControl}.
	 * 
	 * @param context
	 *        the context the rendering occurs
	 */
	public final void writeRenderingCommand(DisplayContext context, Appendable out) throws IOException {
		String width;
		String widthUnit;
		String height;
		String heightUnit;
		if (getDialogModel().hasCustomizedSize()) {
			Dimension customizedSize = getDialogModel().getCustomizedSize();
			width = Integer.toString(customizedSize.width);
			height = Integer.toString(customizedSize.height);
			widthUnit = heightUnit = "px";
		} else {
			LayoutData constraint = getConstraint();
			width = Float.toString(constraint.getWidth());
			widthUnit = constraint.getWidthUnit().toString();
			height = Float.toString(constraint.getHeight());
			heightUnit = constraint.getHeightUnit().toString();
		}
		out.append("(function() {\n");
		out.append("	dlgCenter('");
		out.append(getID());
		out.append("-window',");
		out.append(width);
		out.append(",'");
		out.append(widthUnit);
		out.append("',");
		out.append(height);
		out.append(",'");
		out.append(heightUnit);
		out.append("');\n");
		out.append("	dlgAdjustment('");
		out.append(getID());
		out.append("', ");
		out.append("false);\n");

		out.append("	document.getElementById('");
		out.append(getID());
		out.append("-window').resizeContent = function(maximized){");
		out.append("dlgAdjustment('");
		out.append(getID());
		out.append("', ");
		out.append("maximized)");
		out.append("};\n");
		out.append("    dlgRegisterCaptureStopHandlers('");
		out.append(getID());
		out.append("');\n");
		out.append("	document.getElementById('");
		out.append(getID());
		out.append("').style.visibility = 'visible';");
		if (getDialogModel().isMaximized()) {
			out.append("dlgMaximizeDialog(document.getElementById(");
			TagUtil.writeJsString(out, getID());
			out.append("));");
		}
		out.append("})();");
	}

	/**
	 * This method returns the {@link DialogModel} this Control is displaying.
	 */
	public final DialogModel getDialogModel() {
		return (DialogModel) getWindowModel();
	}

	/**
	 * Writes the handler to close the underlying dialog when the user clicks on the background.
	 */
	@TemplateVariable("closeDialogOnBackgroundClick")
	public void closeDialogOnBackgroundClick(DisplayContext context, TagWriter out) throws IOException {
		if (MainLayout.getMainLayout(context).closeDialogOnBackgroundClick() && getDialogModel().hasCloseButton()) {
			CloseDialogCommand.INSTANCE.writeInvokeExpression(out, this, new JSObject());
		}
	}

	/**
	 * 
	 * Specifies the height of the title of the dialog.
	 * 
	 */
	@TemplateVariable("titleBarHeight")
	public int getTitleBareight() {
		return (int) ThemeFactory.getTheme().getValue(Icons.DIALOG_TITLE_HEIGHT).getValue();
	}

	/**
	 * 
	 * Specifies the width of the left and right border of the dialog.
	 * 
	 */
	@TemplateVariable("verticalBorderWidth")
	public int getVerticalBoderWidth() {
		return (int) ThemeFactory.getTheme().getValue(Icons.DIALOG_VERTICAL_BORDER_WIDTH).getValue();
	}

	/**
	 * 
	 * Specifies the height of the bottom border of the dialog.
	 * 
	 */
	@TemplateVariable("bottomBorderHeight")
	public int getBottomBoderWidth() {
		return (int) ThemeFactory.getTheme().getValue(Icons.DIALOG_BOTTOM_BORDER_HEIGHT).getValue();
	}

	/**
	 * 
	 * Specifies the title text.
	 * 
	 */
	@TemplateVariable("dialogTitle")
	public void writeDialogTitle(DisplayContext context, TagWriter out) throws IOException {
		this.getTitle().write(context, out);
	}

	/**
	 * 
	 * Writes the tool list next to the title, which contains various commands that can be used to
	 * modify the dialog window.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        Writes the dialog toolbar.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("writeDialogToolbar")
	public void writeDialogToolbar(DisplayContext context, TagWriter out) throws IOException {
		DialogModel dialogModel = this.getDialogModel();
		ToolBar dialogToolbar = dialogModel.getToolbar();
		new ToolbarControl(dialogToolbar).write(context, out);

		OPEN_GUI_INSPECTOR_FRAGMENT.write(context, out);

		if (dialogModel.isResizable()) {
			writeMaximizeButton(context, out);
			writeRestoreButton(context, out);
		}
		if (dialogModel.hasCloseButton()) {
			writeCloseButton(context, out);
		}
	}

	private void writeMaximizeButton(DisplayContext context, TagWriter out) throws IOException {
		XMLTag maximizeTag = Icons.WINDOW_MAXIMIZE.toButton();
		maximizeTag.beginBeginTag(context, out);
		CssUtil.writeCombinedCssClasses(out, FormConstants.INPUT_IMAGE_CSS_CLASS, "dlgMaximize");
		out.writeAttribute(ALT_ATTR, context.getResources().getString(MAXIMIZE_DIALOG));
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, MAXIMIZE_DIALOG);
		writeMaximizeCommand(out);
		out.writeAttribute(ONMOUSEDOWN_ATTR, "return dlgIgnore(arguments[0]);");
		maximizeTag.endEmptyTag(context, out);
	}

	private void writeRestoreButton(DisplayContext context, TagWriter out) throws IOException {
		XMLTag restoreTag = Icons.WINDOW_NORMALIZE_AFTER_MAXIMIZED.toButton();
		restoreTag.beginBeginTag(context, out);
		CssUtil.writeCombinedCssClasses(out, FormConstants.INPUT_IMAGE_CSS_CLASS, "dlgRestore");
		out.writeAttribute(ALT_ATTR, context.getResources().getString(RESTORE_DIALOG));
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, RESTORE_DIALOG);
		writeRestoreCommand(out);
		out.writeAttribute(ONMOUSEDOWN_ATTR, "return dlgIgnore(arguments[0]);");
		restoreTag.endEmptyTag(context, out);
	}

	private void writeCloseButton(DisplayContext context, TagWriter out) throws IOException {
		XMLTag tag = Icons.CLOSE_DIALOG.toButton();
		tag.beginBeginTag(context, out);
		CssUtil.writeCombinedCssClasses(out, FormConstants.INPUT_IMAGE_CSS_CLASS, "dlgClose");
		out.writeAttribute(ALT_ATTR, context.getResources().getString(CLOSE_DIALOG));
		out.writeAttribute(TITLE_ATTR, "");
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, CLOSE_DIALOG);
		writeCloseDialogCommand(out);
		out.writeAttribute(ONMOUSEDOWN_ATTR, "return dlgIgnore(arguments[0]);");
		tag.endEmptyTag(context, out);
	}

	/**
	 * This method returns the JS function to update the "maximized" property on server side. It
	 * handles the restore and maximize of a dialog window.
	 */

	private void writeRestoreCommand(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		appendUpdateMaximizedInvocation(out, false);
		out.append("return dlgRestore(arguments[0], this);");
		out.endAttribute();
	}

	private void writeMaximizeCommand(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		appendUpdateMaximizedInvocation(out, true);
		out.append("return dlgMaximize(arguments[0], this);");
		out.endAttribute();
	}

	private void appendUpdateMaximizedInvocation(TagWriter out, boolean maximized) throws IOException {
		UpdateMaximizedCommand.INSTANCE.writeInvokeExpression(out, this,
			UpdateMaximizedCommand.INSTANCE.newArguments(maximized));
	}

	/**
	 * This method returns the JS function to trigger closing this dialog.
	 */
	private void writeCloseDialogCommand(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.write("return ");
		CloseDialogCommand.INSTANCE.writeInvokeExpression(out, this);
		out.endAttribute();
	}

	/**
	 * 
	 * Writes the content of the dialog.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        Writes the dialog content.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("writeDialogContent")
	public void writeDialogContent(DisplayContext context, TagWriter out) throws IOException {
		getChildControl().write(context, out);
	}

	/**
	 * The class {@link DialogWindowControl.CloseDialogCommand} is the
	 * {@link ControlCommand} to trigger closing of this dialog.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class CloseDialogCommand extends ControlCommand {

		@SuppressWarnings("hiding")
		public static final String COMMAND = "close";

		public static final CloseDialogCommand INSTANCE = new CloseDialogCommand(COMMAND);

		public CloseDialogCommand(String command) {
			super(command);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			DialogWindowControl dialog = (DialogWindowControl) control;
			DialogModel dialogModel = dialog.getDialogModel();
			if (! dialogModel.hasCloseButton()) {
				HandlerResult result = new HandlerResult();
				result.addErrorMessage(I18NConstants.ERROR_DIALOG_NOT_CLOSABLE);
				return result;
			}

			return getCloseDialogHandlerResult(context, dialogModel);
		}

		/**
		 * Checks if a dialog has unsaved changes before it closes itself.
		 */
		private HandlerResult getCloseDialogHandlerResult(DisplayContext context, DialogModel dialogModel) {
			Collection<? extends ChangeHandler> affectedFormHandlers = dialogModel.getAffectedFormHandlers();
			DirtyHandling dirtyHandling = DirtyHandling.getInstance();
			boolean dirty = dirtyHandling.checkDirty(affectedFormHandlers);

			if (dirty) {
				dirtyHandling.openConfirmDialog(dialogModel.getCloseAction(), affectedFormHandlers,
					context.getWindowScope());
				return HandlerResult.DEFAULT_RESULT;
			}
			return dialogModel.getCloseAction().executeCommand(context);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CLOSE_DIALOG;
		}
	}

	private static class UpdateDialogSizeCommand extends ControlCommand {

		public static final String COMMAND_ID = "updateSize";

		public static final UpdateDialogSizeCommand INSTANCE = new UpdateDialogSizeCommand();

		public UpdateDialogSizeCommand() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			int width = ((Number) arguments.get("width")).intValue();
			int height = ((Number) arguments.get("height")).intValue();
			DialogWindowControl dialog = (DialogWindowControl) control;

			dialog.getDialogModel().saveCustomizedSize(new Dimension(width, height));

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_DIALOG_SIZE;
		}
	}
	
	private static class UpdateMaximizedCommand extends ControlCommand {

		public static final String COMMAND_ID = "updateMaxmized";

		public static final UpdateMaximizedCommand INSTANCE = new UpdateMaximizedCommand();

		private static final String MAXIMIZED_PROPERTY = "maximized";

		public UpdateMaximizedCommand() {
			super(COMMAND_ID);
		}

		public JSObject newArguments(boolean updateMaximized) {
			return new JSObject().addProperty(MAXIMIZED_PROPERTY, new JSBoolean(updateMaximized));
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			DialogWindowControl dialog = (DialogWindowControl) control;
			boolean maximal = ((Boolean) arguments.get(MAXIMIZED_PROPERTY)).booleanValue();
			dialog.notifyMaximalityChanged(commandContext, maximal);
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.UPDATE_MAXIMIZED;
		}
	}

	void notifyMaximalityChanged(DisplayContext context, boolean maximal) {
		context.set(TRIGGERED_BY_CLIENT, true);
		try {
			getDialogModel().setMaximized(maximal);
		} finally {
			context.reset(TRIGGERED_BY_CLIENT);
		}
	}

	@Override
	public void maximalityChanged(Object sender, boolean maximal) {
		if (!getDialogModel().equals(sender)) {
			// foreign model
			return;
		}
		if (DefaultDisplayContext.getDisplayContext().get(TRIGGERED_BY_CLIENT) != null) {
			// Event is triggered be client, The GUI is up to date.
			return;
		}
		super.maximalityChanged(sender, maximal);
	}

	@Override
	protected void handleRepaintRequested(UpdateQueue actions) {
		super.handleRepaintRequested(actions);
		actions.add(new JSSnipplet(new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				writeRenderingCommand(context, out);
			}
		}));
	}

	private BrowserWindowControl getWindowParent() {
		return (BrowserWindowControl) getParent();
	}

	@Override
	public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
		if (!newValue) {
			// Cannot happen, since dialog models cannot be reopened.
			return;
		}
		BrowserWindowControl window = getWindowParent();
		if (window == null) {
			// This dialog was not yet opened. Cannot happen, because this
			// dialog attaches to its model only during rendering.
			return;
		}
		// Finally drop the dialog.
		window.unregisterDialog(this);
	}

	@Override
	protected ControlRenderer<? super DialogWindowControl> createDefaultRenderer() {
		return DialogRenderer.INSTANCE;
	}

	@Override
	public Scrolling getScrolling() {
		return null;
	}

	@Override
	public DialogWindowControl self() {
		return this;
	}
}
