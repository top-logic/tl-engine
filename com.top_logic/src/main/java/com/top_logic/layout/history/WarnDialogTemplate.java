/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import org.w3c.dom.Element;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.Icons;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.provider.ButtonComponentButtonsControlProvider;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogModelAdapter;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link WarnDialogTemplate} is a helper class for opening simple
 * dialogs with a message header, a message, and some buttons in the button
 * section.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WarnDialogTemplate {
	
	protected static final NamedConstant WARN_DIALOG_OPENED = new NamedConstant("dialogOpened");
	
	public enum WarnMode implements ExternallyNamed {

		WARN_MODE_DIALOG("show-dialog"),
		WARN_MODE_IGNORE("ignore-action"),
		WARN_MODE_CONTINUE("continue-action"), ;

		private final String _externalName;

		private WarnMode(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		public static UnreachableAssertion noSuchMode(WarnMode warnMode) {
			throw new UnreachableAssertion("Unknown mode: " + warnMode.getExternalName());
		}
	}


	private static final String BUTTONS_GROUP = "buttons";

	/**
	 * Opens a simple dialog with a header, a message and some buttons
	 * 
	 * @param scope
	 *        the server side representation of the browser window in which the
	 *        dialog should be opened
	 * @param dialogModel
	 *        the model of the dialog to open
	 * @param messageHeader
	 *        the resource key of the header to display
	 * @param message
	 *        the resource key of the message to display
	 * @param resPrefix
	 *        the resource prefix of all I18N used in the dialog
	 * @param buttons
	 *        a list of buttons to show in the button section
	 */
	protected static void openDialog(WindowScope scope, DialogModel dialogModel, String messageHeader, String message, ResPrefix resPrefix,
			CommandField... buttons) {
		FrameScope ids = scope.getTopLevelFrameScope();
		String fcId = ids.createNewID();

		FormContext ctx = new FormContext(fcId, resPrefix);

		FormGroup buttonsField = new FormGroup(BUTTONS_GROUP, resPrefix);
		buttonsField.addMembers(buttons);
		ctx.addMember(buttonsField);

		scope.openDialog(createDialog(dialogModel, ctx, messageHeader, message, resPrefix));
	}

	static DialogWindowControl createDialog(DialogModel dialogModel, FormContext ctx, String messageHeaderKey, String messageKey, ResPrefix resPrefix) {
		final DialogWindowControl dialog = new DialogWindowControl(dialogModel);

		final Element groupTemplate = getGroupTemplate(messageHeaderKey, messageKey);
		FormGroupControl contentControl = new FormGroupControl(ctx, ButtonComponentButtonsControlProvider.INSTANCE, groupTemplate, resPrefix);

		LayoutControlAdapter contentLayout = new LayoutControlAdapter(contentControl);
		dialog.setChildControl(contentLayout);
		return dialog;
	}

	static Element getGroupTemplate(String messageHeaderKey, String messageKey) {
		String templateAsString = "" + "<t:group" + " xmlns='" + HTMLConstants.XHTML_NS + "'" + " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS
				+ "'" + " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'" + ">" + "<div class='layoutControl'"
				+ LayoutControlRenderer.getLayoutConstraintInformation(100, DisplayUnit.PERCENT)
				+ LayoutControlRenderer.getLayoutInformation(Orientation.VERTICAL, 100)
				+ ">"
				+ "<div class='layoutControl'"
				+ LayoutControlRenderer.getLayoutConstraintInformation(100, DisplayUnit.PERCENT)
				+ LayoutControlRenderer.getLayoutInformation(Orientation.HORIZONTAL, 100)
				+ ">"
				// Workaround for DivLayout not automatically providing scroll
				// bars for content that cannot completely be displayed.
				+ "<div style='width:100%; height: 100%; overflow: auto;'>" + "<div style='margin: 15px;'>" + "<h2><t:text key='" + messageHeaderKey
				+ "'/></h2>" + "<t:text key='" + messageKey + "'/>" + "</div>";
		// See above.
		DisplayDimension height = ThemeFactory.getTheme().getValue(Icons.BUTTON_COMP_HEIGHT);
		templateAsString += "</div>" + "</div>" + "<div class='layoutControl'"
			+ LayoutControlRenderer.getLayoutConstraintInformation(height.getValue(), height.getUnit())
				+ LayoutControlRenderer.getLayoutInformation(Orientation.HORIZONTAL, 100) + ">" + "<p:field name='" + BUTTONS_GROUP + "'>" + "<t:list>"
				+ "<div class='cmdButtons'>" + "<t:items/>" + "</div>" + "</t:list>" + "</p:field>" + "</div>" + "</div>" + "</t:group>";

		return DOMUtil.getFirstElementChild(DOMUtil.parse(templateAsString).getDocumentElement());
	}

	protected static CommandField createCancelButton(String fieldName, Command closeAction) {
		return FormFactory.newCommandField(fieldName, closeAction);
	}

	/**
	 * Creates a dialog model which resets {@link HistoryControl#warnDialogOpened} if it is closed
	 */
	protected static DialogModel createDialogModel(final HistoryControl hControl, HTMLFragment dialogTitle) {
		LayoutData dialogDimension = new DefaultLayoutData(450, DisplayUnit.PIXEL, 100, 300, DisplayUnit.PIXEL, 100,
			Scrolling.AUTO);
		final DialogModel dialogModelImpl =
			new DefaultDialogModel(dialogDimension, dialogTitle, false, true, null);
		return new DialogModelAdapter() {
	
			@Override
			protected DialogModel getDialogModelImplementation() {
				return dialogModelImpl;
			}
	
			@Override
			public Command getCloseAction() {
				final Command actualCloseAction = super.getCloseAction();
				return new Command() {
	
					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						HandlerResult closeResult = actualCloseAction.executeCommand(context);
						hControl.warnDialogOpened = false;
						return closeResult;
					}
				};
			}
		};
	}
}
