/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.tag.FormPageTag;
import com.top_logic.layout.form.tag.PageControl;
import com.top_logic.layout.form.tag.PageRenderer;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DialogModel;

/**
 * {@link AbstractFormDialogBase} rendering a page with title and content.
 * 
 * @see PageRenderer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormPageDialog extends AbstractFormDialogBase {

	/**
	 * The suffix to the dialog's {@link ResPrefix} for creating the header text.
	 * 
	 * @deprecated Use {@link #AbstractFormPageDialog(DialogModel, ResKey, ResKey)}
	 */
	@Deprecated
	protected static final String HEADER_RESOURCE_SUFFIX = "header";

	/**
	 * The suffix to the dialog's {@link ResPrefix} for creating the message text.
	 * 
	 * @deprecated Use {@link #AbstractFormPageDialog(DialogModel, ResKey, ResKey)}
	 */
	@Deprecated
	protected static final String MESSAGE_RESOURCE_SUFFIX = "message";

	private final ResKey _message;

	private final ResKey _header;

	/**
	 * Creates a {@link AbstractFormPageDialog}.
	 * 
	 * @param width
	 *        The dialog width, see {@link #getDialogModel()}.
	 * @param height
	 *        The dialog height, see {@link #getDialogModel()}.
	 * 
	 * @deprecated Use {@link #AbstractFormPageDialog(DialogModel, ResKey, ResKey)}, or
	 *             {@link #AbstractFormPageDialog(ResKey, ResKey, ResKey, DisplayDimension, DisplayDimension)}.
	 */
	@Deprecated
	public AbstractFormPageDialog(ResPrefix resourcePrefix, DisplayDimension width, DisplayDimension height) {
		this(resourcePrefix.key("title"), resourcePrefix.key(HEADER_RESOURCE_SUFFIX),
			resourcePrefix.key(MESSAGE_RESOURCE_SUFFIX), width, height);
	}

	/**
	 * Creates a {@link AbstractFormPageDialog}.
	 * 
	 * @param title
	 *        The dialog title.
	 * @param header
	 *        The text displayed in the header area of the dialog.
	 * @param message
	 *        The text below the header.
	 * @param width
	 *        The width of the dialog.
	 * @param height
	 *        The height of the dialog.
	 */
	public AbstractFormPageDialog(ResKey title, ResKey header, ResKey message, DisplayDimension width,
			DisplayDimension height) {
		this(DefaultDialogModel.dialogModel(title, width, height), header, message);
	}

	/**
	 * Creates a {@link AbstractFormPageDialog}.
	 * 
	 * @param dialogModel
	 *        The description of the dialog border.
	 * @param header
	 *        The text displayed in the header area of the dialog.
	 * @param message
	 *        The text below the header.
	 */
	public AbstractFormPageDialog(DialogModel dialogModel, ResKey header, ResKey message) {
		super(dialogModel);
		_header = header;
		_message = message;
	}

	@Override
	protected HTMLFragment createView() {
		PageControl pageControl = new PageControl(PageRenderer.getThemeInstance());
		pageControl.setTitleContent(createTitleContent());
		pageControl.setSubtitleContent(createSubtitleContent());
		pageControl.setIconBarContent(createIconBarContent());
		pageControl.setBodyContent(createBodyContent());
		return pageControl;
	}

	/**
	 * Creates the title area content.
	 */
	protected HTMLFragment createTitleContent() {
		return Fragments.message(_header);
	}

	/**
	 * Creates the subtitle area content.
	 */
	protected HTMLFragment createSubtitleContent() {
		return Fragments.message(_message);
	}

	/**
	 * Creates the icon bar content.
	 */
	protected HTMLFragment createIconBarContent() {
		IconControl icon = createTitleIcon();
		if (icon == null) {
			return Fragments.empty();
		}

		IconControl iconOverlay = createTitleIconOverlay();
		if (iconOverlay == null) {
			icon.setCssClass(FormPageTag.IMAGE_CSS_CLASS);
			return icon;
		} else {
			iconOverlay.setCssClass(FormPageTag.ACTION_IMAGE_CSS_CLASS);

			icon.setCssClass(FormPageTag.IMAGE_WITH_ACTION_CSS_CLASS);
			return Fragments.concat(icon, iconOverlay);
		}
	}

	/**
	 * The title bar icon.
	 */
	protected abstract IconControl createTitleIcon();

	/**
	 * An optional overlay to the {@link #createTitleIcon()}.
	 */
	protected IconControl createTitleIconOverlay() {
		return null;
	}

	/**
	 * Creates the body content.
	 */
	protected abstract HTMLFragment createBodyContent();

	/**
	 * Utility for creating a display of the label of the {@link FormMember} with the given name.
	 */
	protected final HTMLFragment label(String name) {
		return FormPageTag.label(getControlProvider(), member(name));
	}

	/**
	 * Utility for creating a display of the input element of the {@link FormMember} with the given
	 * name.
	 */
	protected final HTMLFragment input(String name) {
		return FormPageTag.input(getControlProvider(), member(name));
	}

	/**
	 * Utility for creating a display of the {@link FormMember} with the given name using a
	 * compressed/embedded label and error icon.
	 */
	protected final HTMLFragment inputEmbedded(String fieldName) {
		return FormPageTag.inputEmbedded(getControlProvider(), member(fieldName));
	}

	/**
	 * Utility for creating a display of the error icon for the {@link FormMember} with the given
	 * name.
	 */
	protected final HTMLFragment errorIcon(String name) {
		return FormPageTag.error(getControlProvider(), member(name));
	}

	/**
	 * Utility to look up the {@link FormMember} with the given name.
	 */
	protected final FormMember member(String name) {
		return getFormContext().getMember(name);
	}

	/**
	 * The {@link ControlProvider} to use for controls created in e.g. {@link #createBodyContent()}.
	 */
	protected ControlProvider getControlProvider() {
		return DefaultFormFieldControlProvider.INSTANCE;
	}

}
