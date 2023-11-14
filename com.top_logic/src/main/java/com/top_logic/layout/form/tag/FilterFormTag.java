/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.NothingCommand;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * Form tag for filter components which have a defined refresh command.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FilterFormTag extends FormTag {

	/**
	 * Suffix to the {@link LayoutComponent#getResPrefix()} to customize the "No model" message.
	 */
	private static final String NO_MODEL_KEY_SUFFIX = "noModel";

	/**
	 * Suffix to the {@link LayoutComponent#getResPrefix()} to customize the "Filter settings"
	 * message.
	 */
	private static final String CONFIGURATION_KEY_SUFFIX = "title";

	/** Default CSS-class for this tag. */
	public static final String FORM_CSS_CLASS = "fltForm";

	private static final String SCROLL_BOX_CSS_CLASS = "fltScrollBox";

	private static final String AREA_CSS_CLASS = "fltArea";
	
	private static final String CONTAINER_CSS_CLASS = "fltContainer";

	private static final String TITLE_CSS_CLASS = "fltTitle";

	private static final String ICON_CSS_CLASS = "fltIcon";

	private static final String REFRESH_CSS_CLASS = "fltRefresh";

	private static final String CONTENT_CSS_CLASS = "fltContent";

	private static final String NO_MODEL_CSS_CLASS = "fltNoModel";

	private String _commandId;

	private String _buttonName;

	private ThemeImage _icon;

	/**
	 * ID of the {@link CommandHandler} in {@link #getComponent()} that should be rendered as filter
	 * refresh button.
	 * 
	 * @see #getButtonName()
	 */
	public String getCommandId() {
		return _commandId;
	}

	/**
	 * @see #getCommandId()
	 */
	public void setCommandId(String commandId) {
		_commandId = commandId;
	}

	/**
	 * Name of the command field in {@link #getFormContext()} that should be rendered as filter
	 * refresh button.
	 * 
	 * @see #getCommandId()
	 */
	public String getButtonName() {
		return _buttonName;
	}

	/**
	 * @see #getButtonName()
	 */
	public void setButtonName(String buttonName) {
		_buttonName = buttonName;
	}

	/**
	 * The custom filter icon {@link ThemeImage} key, or <code>null</code> to use the default filter
	 * icon.
	 */
	public ThemeImage getIcon() {
		return _icon;
	}

	/**
	 * @see #getIcon()
	 */
	@CalledFromJSP
	public void setIcon(ThemeImage icon) {
		_icon = icon;
	}

	/**
	 * Use {@link #setIcon(ThemeImage)}.
	 * 
	 * @see #getIcon()
	 */
	@CalledFromJSP
	@Deprecated
	public void setImage(String image) {
		setIcon(ThemeImage.icon(image));
	}

	@Override
	public int startElement() throws JspException {
		String customFormClass = getCssClass();
		if (StringServices.isEmpty(customFormClass)) {
			setCssClass(FilterFormTag.FORM_CSS_CLASS);
		}
		
		return super.startElement();
	}
	
	@Override
	protected void writeNoModel() throws IOException {
		writeFilterStart();
		writeFilterContentNoModel();
		writeFilterEnd();
	}

	@Override
	protected void writeHiddenFields() throws IOException {
		super.writeHiddenFields();

		writeFilterStart();
	}

	@Override
	protected void endForm() throws IOException {
		writeFilterEnd();
		
		super.endForm();
	}

	@Override
	protected boolean defaultDisplayWithoutModel() {
		return true;
	}

	/**
	 * Write the start part of the filter UI.
	 */
	protected void writeFilterStart() throws IOException {
		beginBeginTag(DIV);
		writeAttribute(CLASS_ATTR, SCROLL_BOX_CSS_CLASS);
		endBeginTag();

		beginBeginTag(DIV);
		writeAttribute(CLASS_ATTR, AREA_CSS_CLASS);
		endBeginTag();
		
		beginBeginTag(DIV);
		writeAttribute(CLASS_ATTR, CONTAINER_CSS_CLASS);
		endBeginTag();
		if (hasInternalRefresh()) {
			beginBeginTag(DIV);
			writeAttribute(CLASS_ATTR, TITLE_CSS_CLASS);
			endBeginTag();
			{
				getFilterImage().writeWithCss(getDisplayContext(), getOut(), ICON_CSS_CLASS);
				writeHeaderText();
			}
			endTag(DIV);
		}

		beginBeginTag(DIV);
		writeAttribute(CLASS_ATTR, CONTENT_CSS_CLASS);
		endBeginTag();
	}

	/**
	 * Write the end section for the horizontal display.
	 */
	protected void writeFilterEnd() throws IOException {
		endTag(DIV);

		if (shouldDisplay() && hasInternalRefresh()) {
			// Write refresh button after filter contents, to ensure that it is last in the tab order.
			beginBeginTag(DIV);
			writeAttribute(CLASS_ATTR, REFRESH_CSS_CLASS);
			endBeginTag();
			{
				writeRefreshButton();
			}
			endTag(DIV);
		}

		endTag(DIV);

		endTag(DIV);
		
		endTag(DIV);
	}

	/**
	 * Return the text for the header area.
	 */
	protected void writeHeaderText() throws IOException {
		writeI18NWithFallback(CONFIGURATION_KEY_SUFFIX, I18NConstants.FILTER_CONFIGURATION);
	}

	/**
	 * Write the text, when no model selected.
	 */
	protected void writeFilterContentNoModel() throws IOException {
		beginBeginTag(DIV);
		writeAttribute(CLASS_ATTR, NO_MODEL_CSS_CLASS);
		endBeginTag();
		{
			writeI18NWithFallback(NO_MODEL_KEY_SUFFIX, I18NConstants.FILTER_NO_MODEL);
		}
		endTag(DIV);
	}

	/**
	 * Write the translated string for the given parameters.
	 * 
	 * @param keySuffix
	 *        The I18N suffix to {@link LayoutComponent#getResPrefix()} of {@link #getComponent()}.
	 * @param fallbackKey
	 *        The I18N key to use, if no translation is found.
	 */
	protected void writeI18NWithFallback(String keySuffix, ResKey fallbackKey) throws IOException {
		ResPrefix componentResPrefix = getComponent().getResPrefix();
		Resources resources = Resources.getInstance();
		String text = resources.getString(componentResPrefix.key(keySuffix), null);

		if (text == null) {
			text = resources.getString(fallbackKey);
		}

		writeText(text);
	}

	/**
	 * Write the refresh command to the UI.
	 */
	protected void writeRefreshButton() throws IOException {
		HTMLFragment refreshControl = createRefreshButton();
		if (refreshControl != null) {
			ControlTagUtil.writeControl(this, pageContext, refreshControl);
		}
	}

	/**
	 * Return the refresh control to be used by this form.
	 * 
	 * @return The control to be used for refresh, <code>null</code> to render no refresh button.
	 */
	protected HTMLFragment createRefreshButton() {
		boolean enableRefresh = getFormContext() != null;

		if (!enableRefresh) {
			return createDisabledRefreshButton();
		} else {
			if (hasCommand()) {
				return createRefrehByCommandHandler(getCommandId());
			}

			if (hasButton()) {
				return createRefreshByCommandField(getButtonName());
			}

			return null;
		}
	}

	private boolean hasInternalRefresh() {
		return hasCommand() || hasButton();
	}

	private boolean hasCommand() {
		return getCommandId() != null;
	}

	private boolean hasButton() {
		return getButtonName() != null;
	}

	/**
	 * Return the control which is not executable.
	 * 
	 * @return The requested control, never <code>null</code>.
	 */
	protected Control createDisabledRefreshButton() {
		CommandModel model = CommandModelFactory.commandModel(NothingCommand.INSTANCE, getComponent());

		model.setNotExecutableImage(Icons.RELOAD_SMALL_DISABLED);
		model.setNotExecutable(I18NConstants.FILTER_NO_MODEL);

		return createButtonControl(model);
	}

	/**
	 * Return the refresh control to be used by this form (taken from the command handlers known by
	 * the component).
	 * 
	 * @param commandId
	 *        The ID of the requested command.
	 * @return The control to be used for refresh.
	 */
	protected Control createRefrehByCommandHandler(String commandId) {
		LayoutComponent component = getComponent();
		CommandHandler commandHandler = component.getCommandById(commandId);
		if (commandHandler == null) {
			throw new IllegalArgumentException("No such command with ID '" + commandId + "' registered in '"
				+ component.getName() + "'.");
		}

		CommandModel model = CommandModelFactory.commandModel(commandHandler, component);
		model.setImage(Icons.RELOAD_SMALL);
		model.setNotExecutableImage(Icons.RELOAD_SMALL_DISABLED);

		return createButtonControl(model);
	}

	/**
	 * Return the refresh control to be used by this form (taken from the given form context).
	 * 
	 * @param buttonName
	 *        The name of the requested command.
	 * @return The control to be used for refresh.
	 */
	protected HTMLFragment createRefreshByCommandField(String buttonName) {
		CommandField commandField = (CommandField) getFormContext().getMember(buttonName);

		ControlProvider cp = commandField.getControlProvider();
		if (cp == null) {
			return createButtonControl(commandField);
		} else {
			return cp.createFragment(commandField, FormTemplateConstants.STYLE_DIRECT_VALUE);
		}
	}

	/**
	 * Create a new button control.
	 * 
	 * @param commandModel
	 *        The command model to create the button control for.
	 * @return The requested button control, never <code>null</code>.
	 */
	protected ButtonControl createButtonControl(CommandModel commandModel) {
		return new ButtonControl(commandModel, ImageButtonRenderer.INSTANCE);
	}

	/**
	 * The filter icon.
	 * 
	 * <p>
	 * May be customized using {@link #setImage(String)}.
	 * </p>
	 */
	protected ThemeImage getFilterImage() {
		ThemeImage customImage = getIcon();
		return StringServices.isEmpty(customImage) ? Icons.FILTER_FORM : customImage;
	}

	@Override
	protected void teardown() {
		_commandId = null;
		_buttonName = null;
		_icon = null;

		super.teardown();
	}

}
