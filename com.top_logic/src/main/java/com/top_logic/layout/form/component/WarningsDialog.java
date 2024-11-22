/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.MessageArea;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Utility to open a dialog displaying and confirming form warnings.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WarningsDialog {

	/**
	 * {@link Command} expanding all groups containing fields with warnings.
	 */
	public static final class ShowWarnings implements Command {
		private final FormContext _formContext;

		private final Command _continuation;

		/**
		 * Creates a {@link ShowWarnings}.
		 * 
		 * @param formContext
		 *        The {@link FormContext} to analyze.
		 * @param continuation
		 *        The another {@link Command} to execute afterwards.
		 */
		public ShowWarnings(FormContext formContext, Command continuation) {
			_formContext = formContext;
			_continuation = continuation;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			boolean first = true;
			for (Iterator<? extends FormField> it = _formContext.getDescendantFields(); it.hasNext();) {
				FormField field = it.next();
				if (field.hasWarnings()) {
					field.makeVisible();
					if (first) {
						field.focus();
						first = false;
					}
				}
			}

			return _continuation.executeCommand(context);
		}
	}

	private static final String CONFIRM_TITLE_SUFFIX = ".confirmTitle";

	private static final String CONFIRM_HEADING_SUFFIX = ".confirmHeading";

	private static final String CONFIRM_MESSAGE_SUFFIX = ".confirmMessage";

	private static final String CONFIRM_ACTION_SUFFIX = ".confirmAction";

	private static final ResourceText DEFAULT_TITLE = new ResourceText(I18NConstants.WARNING_CONFIRM_TITLE);

	/** Layout size definition for warning dialog. */
	public static final DefaultLayoutData LAYOUT = new DefaultLayoutData(
		DisplayDimension.dim(500, DisplayUnit.PIXEL), 100,
		DisplayDimension.dim(350, DisplayUnit.PIXEL), 100, Scrolling.AUTO);

	/**
	 * Like {@link #openWarningsDialog(WindowScope, ResKey, FormContext, Command, Command)} without
	 * special action upon cancel.
	 */
	public static HandlerResult openWarningsDialog(WindowScope scope, ResKey resourceBaseKey,
			FormContext formContext, Command onConfirm) {
		return openWarningsDialog(scope, resourceBaseKey, formContext, onConfirm, Command.DO_NOTHING);
	}

	/**
	 * Open the {@link WarningsDialog}.
	 * 
	 * @param scope
	 *        The window to open the dialog in.
	 * @param resourceBaseKey
	 *        The base resource key (the command) that is used to customize the dialog.
	 * @param formContext
	 *        The {@link FormContext} with warnings that should be displayed.
	 * @param onConfirm
	 *        The action to perform after the user has confirmed.
	 * @param onCancel
	 *        The action to perform when the user cancels the confirmation.
	 * @return The technical result of opening the dialog.
	 */
	public static HandlerResult openWarningsDialog(WindowScope scope, final ResKey resourceBaseKey,
			final FormContext formContext, Command onConfirm, final Command onCancel) {

		DisplayValue title =
			new ResourceText(Resources.derivedKey(resourceBaseKey, CONFIRM_TITLE_SUFFIX), DEFAULT_TITLE);

		MessageArea message = new MessageArea(MessageType.INFO.getTypeImage()) {
			@Override
			protected void writeMessage(DisplayContext context, TagWriter out) throws IOException {
				Resources resources = Resources.getInstance();
				
				out.beginTag("h2");
				out.writeText(
					getResource(resources,
						Resources.derivedKey(resourceBaseKey, CONFIRM_HEADING_SUFFIX),
						I18NConstants.WARNING_CONFIRM_HEADING));
				out.endTag("h2");

				out.writeText(
					getResource(resources,
						Resources.derivedKey(resourceBaseKey, CONFIRM_MESSAGE_SUFFIX),
						I18NConstants.WARNING_CONFIRM_MESSAGE));

				out.beginTag(UL);
				for (Iterator<? extends FormField> it = formContext.getDescendantFields(); it.hasNext();) {
					FormField field = it.next();
					if (field.hasWarnings()) {
						String label = StringServices.nonEmpty(field.getLabel());
						for (String warning : field.getWarnings()) {
							out.beginTag(LI);
							if (label != null) {
								out.writeText(label);
								out.writeText(": ");
							}
							writeWarning(out, warning);

							out.endTag(LI);
						}
					}
				}
				out.endTag(UL);

				out.beginTag(PARAGRAPH);
				out.writeText(
					getResource(resources,
						Resources.derivedKey(resourceBaseKey, CONFIRM_ACTION_SUFFIX),
						I18NConstants.WARNING_CONFIRM_ACTION));
				out.endTag(PARAGRAPH);
			}

			private void writeWarning(TagWriter out, String warning) throws IOException {
				warning = StringServices.nonNull(warning);
				int lineBreakIndex = warning.indexOf('\n');
				if (lineBreakIndex < 0) {
					// no line break
					out.writeText(warning);
				} else {
					out.writeText(warning.subSequence(0, lineBreakIndex));
					while (true) {
						// write separator
						HTMLUtil.writeBr(out);
						int newLineStart = lineBreakIndex + 1;
						lineBreakIndex = warning.indexOf('\n', newLineStart);
						if (lineBreakIndex < 0) {
							// write end
							out.writeText(warning.subSequence(newLineStart, warning.length()));
							break;
						} else {
							// write middle of warning
							out.writeText(warning.subSequence(newLineStart, lineBreakIndex));
						}
					}
				}
			}

			private String getResource(Resources resources, ResKey key, ResKey defaultKey) {
				return resources.getStringWithDefaultKey(key, defaultKey);
			}
		};

		Command expandWarnings = new ShowWarnings(formContext, onCancel);

		return MessageBox.confirm(scope, LAYOUT, true,
			title, message,
			MessageBox.button(ButtonType.YES, onConfirm),
			MessageBox.button(ButtonType.CANCEL, expandWarnings));
	}

	/**
	 * Opens the WarningsDialog without using FormContext, taking a list of ResKey warnings instead.
	 *
	 * @param scope
	 *        The window to open the dialog in.
	 * @param resourceBaseKey
	 *        The base resource key for customizing the dialog.
	 * @param warnings
	 *        List of ResKey warning keys to display.
	 * @param onConfirm
	 *        The action to perform after user confirmation.
	 * @param onCancel
	 *        The action to perform if the user cancels.
	 * @return The technical result of opening the dialog.
	 */
	public static HandlerResult openWarningsDialogWithoutFormContext(WindowScope scope, ResKey resourceBaseKey,
			List<ResKey> warnings, Command onConfirm, Command onCancel) {

		DisplayValue title =
			new ResourceText(Resources.derivedKey(resourceBaseKey, CONFIRM_TITLE_SUFFIX), DEFAULT_TITLE);

		MessageArea message = new MessageArea(MessageType.INFO.getTypeImage()) {
			@Override
			protected void writeMessage(DisplayContext context, TagWriter out) throws IOException {
				Resources resources = Resources.getInstance();

				out.beginTag("h2");
				out.writeText(getResource(resources, Resources.derivedKey(resourceBaseKey, CONFIRM_HEADING_SUFFIX),
					I18NConstants.WARNING_CONFIRM_HEADING));
				out.endTag("h2");

				out.writeText(getResource(resources, Resources.derivedKey(resourceBaseKey, CONFIRM_MESSAGE_SUFFIX),
					I18NConstants.WARNING_CONFIRM_MESSAGE));

				out.beginTag(UL);
				for (ResKey warning : warnings) {
					out.beginTag(LI);
					String warningMessage = Resources.getInstance().getString(warning);
					writeWarning(out, warningMessage);
					out.endTag(LI);
				}
				out.endTag(UL);

				out.beginTag(PARAGRAPH);
				out.writeText(getResource(resources, Resources.derivedKey(resourceBaseKey, CONFIRM_ACTION_SUFFIX),
					I18NConstants.WARNING_CONFIRM_ACTION));
				out.endTag(PARAGRAPH);
			}

			private void writeWarning(TagWriter out, String warning) throws IOException {
				int lineBreakIndex = warning.indexOf('\n');
				if (lineBreakIndex < 0) {
					out.writeText(warning);
				} else {
					out.writeText(warning.substring(0, lineBreakIndex));
					while (lineBreakIndex != -1) {
						HTMLUtil.writeBr(out);
						int newLineStart = lineBreakIndex + 1;
						lineBreakIndex = warning.indexOf('\n', newLineStart);
						out.writeText(warning.substring(newLineStart,
							(lineBreakIndex != -1) ? lineBreakIndex : warning.length()));
					}
				}
			}

			private String getResource(Resources resources, ResKey key, ResKey defaultKey) {
				return resources.getStringWithDefaultKey(key, defaultKey);
			}
		};

		Command expandWarnings = new ShowWarningsWithoutFormContext(warnings, onCancel);

		return MessageBox.confirm(scope, LAYOUT, true,
			title, message,
			MessageBox.button(ButtonType.YES, onConfirm),
			MessageBox.button(ButtonType.CANCEL, expandWarnings));
	}

	// Inner class to handle warnings directly from the list of ResKey
	public static final class ShowWarningsWithoutFormContext implements Command {
		private final List<ResKey> warnings;

		private final Command continuation;

		public ShowWarningsWithoutFormContext(List<ResKey> warnings, Command continuation) {
			this.warnings = warnings;
			this.continuation = continuation;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			// Implement any additional logic for displaying ResKey-based warnings if needed.
			return continuation.executeCommand(context);
		}
	}

}
