/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.format.ThemeImageFormat;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;


/**
 * This class creates an displayable, interactive icon chooser. The amount of displayed icons can be
 * limited by a search tag which can be entered in a {@link StringField}. If the {@link StringField}
 * is empty, there will be shown all icons.
 * 
 * @see IconInputControl
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class IconChooserControl extends AbstractVisibleControl {

	/**
	 * Commands registered at this control.
	 * 
	 * @see #getCommand(String)
	 */
	public static final Map<String, ControlCommand> ICON_CHOOSER_COMMANDS = createCommandMap(TextInputControl.COMMANDS,
		new ControlCommand[] {
			SetThemeImageCommand.INSTANCE,
			ThemeImageIntoStringFieldCommand.INSTANCE,
			ToggleExpertModeCommand.INSTANCE
		});

	private static final String SET_IMG_EXPERT_NAME = "setThemeImageExpertCommand";

	private static final String RESET_NAME = "resetThemeImageCommand";

	private static final String CONTROL_NAME = "IconChooserControl";

	private PopupDialogModel _dialog;
	private FormField _model;
	private List<IconBundle> _resources;
	private StringField _field = null;

	private boolean _searchmode = true;

	/**
	 * Creates a {@link IconChooserControl}.
	 * 
	 * @param dialog
	 *        The dialog in which the control is displayed. The dialog is closed when a new icon is
	 *        chosen.
	 * @param model
	 *        The {@link FormField} model that is updated, when a new icon is chosen.
	 */
	public IconChooserControl(PopupDialogModel dialog, FormField model, List<IconBundle> ressources) {
		super(ICON_CHOOSER_COMMANDS);

		_dialog = dialog;
		_model = model;
		_resources = ressources;
	}

	@Override
	protected String getTypeCssClass() {
		return "cIconChooser";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		writeFixedHeader(context, out);
		writeFixedBody(context, out);

		out.endTag(DIV);
	}

	private void writeFixedHeader(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "header");
		out.endBeginTag();

		if (_searchmode) {
			writeFixedHeaderSearch(context, out);
		} else {
			writeFixedHeaderExpert(context, out);
		}

		ThemeImage img = _searchmode ? Icons.ICON_CHOOSER__EXPERT_MODE_BUTTON : Icons.ICON_CHOOSER__SEARCH_MODE_BUTTON;
		renderButton(context, out, img, "toggleExpertButton",
			Resources.getInstance().getString(I18NConstants.ICON_CHOOSER__SWITCH_MODE),
			ToggleExpertModeCommand.COMMAND_NAME, false);

		out.endTag(DIV);
	}

	private void writeFixedHeaderSearch(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "searchTextField");
		out.endBeginTag();
		createPatternFieldControl().write(context, out);
		Icons.ICON_CHOOSER__SEARCH.write(context, out);
		out.endTag(DIV);

		renderButton(context, out, Icons.ICON_CHOOSER__RESET_BUTTON, "resetButton",
			Resources.getInstance().getString(I18NConstants.ICON_CHOOSER__RESET_ICON), RESET_NAME, false);
	}

	private void writeFixedHeaderExpert(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "expertTextField");
		out.endBeginTag();

		String value = "";
		if (_model.getValue() != null) {
			ThemeImage img = (ThemeImage) _model.getValue();
			value = ThemeImageFormat.INSTANCE.format(img);
		}

		_field = FormFactory.newStringField("icon-input");
		_field.setValue(value);
		ScriptingRecorder.annotateAsDontRecord(_field);
		TextInputControl result = new TextInputControl(_field);
		result.write(context, out);
		out.endTag(DIV);

		renderButton(context, out, Icons.ICON_CHOOSER__OK_BUTTON, "okButton",
			Resources.getInstance().getString(I18NConstants.ICON_CHOOSER__SET_ICON), SET_IMG_EXPERT_NAME, false);
	}

	private void writeFixedBody(DisplayContext context, TagWriter out) throws IOException {
		List<IconDescription> images = createThemeImageList(""); // initial search

		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, iconListId());
		out.writeAttribute(CLASS_ATTR, "fptBody");
		writeOnClick(out);
		out.endBeginTag();

		writeFixedContent(context, out, images);

		out.endTag(DIV);
	}

	void writeFixedContent(DisplayContext context, TagWriter out, List<IconDescription> images)
			throws IOException {
		ThemeImage selectedImage = (ThemeImage) _model.getValue();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "fptBodyContent");
		out.endBeginTag();

		// render all icons
		for (IconDescription icon : images) {
			String cssClass;

			for (ThemeImage img : icon.getImages()) {
				if (img.equals(selectedImage)) {
					cssClass = "displayedIcons selected";
				} else {
					cssClass = "displayedIcons";
				}

				renderButton(context, out, img, cssClass, icon.getLabel(), null, true);
			}
		}

		out.endTag(DIV);
	}

	private List<IconDescription> createThemeImageList(String searchTag) {
		final String searchTagLower = searchTag.toLowerCase();

		return _resources.stream()
			.flatMap(bundle -> bundle.getIcons().stream())
			.filter(icon -> icon.matches(searchTagLower))
			.collect(Collectors.toList());
	}

	private void renderButton(DisplayContext context, TagWriter out, ThemeImage img, String cssClass,
			String label, String onclickName, boolean setDataIcon) throws IOException {
		XMLTag tag = img.toButton();

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, cssClass);
		writeDataIcon(out, img, setDataIcon);
		out.endBeginTag();

		tag.beginBeginTag(context, out);
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, TagUtil.encodeXML(label));
		writeOnClick(out, onclickName);
		tag.endEmptyTag(context, out);

		out.endTag(DIV);
	}

	private void writeDataIcon(TagWriter out, ThemeImage img, boolean setDataAttribute) {
		if (setDataAttribute) {
			out.writeAttribute(HTMLConstants.DATA_ATTRIBUTE_PREFIX + "icon", img.toEncodedForm());
		}
	}

	private void writeOnClick(TagWriter out, String onclickName) throws IOException {
		if (onclickName != null) {
			out.beginAttribute(ONCLICK_ATTR);
			appenJsCommand(out, onclickName);
			out.endAttribute();
		}
	}

	private void appenJsCommand(TagWriter out, String commandName) throws IOException {
		out.append("return ");
		out.append(FormConstants.FORM_PACKAGE + "." + CONTROL_NAME + ".");
		out.append(commandName);
		out.append("(");
		writeIdJsString(out);
		out.append(");");
	}

	private void writeOnClick(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		out.append(FormConstants.FORM_PACKAGE + "." + CONTROL_NAME + ".");
		if (isSearchMode()) {
			out.append(SetThemeImageCommand.COMMAND_NAME);
		} else {
			out.append(ThemeImageIntoStringFieldCommand.COMMAND_NAME);
		}
		out.append("(");
		writeIdJsString(out);
		out.append(", event");
		out.append(");");
		out.endAttribute();
	}

	void closeIconChooser() {
		if (_dialog != null) {
			_dialog.setClosed();
		}
	}

	@Override
	public Object getModel() {
		return _model;
	}

	boolean isSearchMode() {
		return _searchmode;
	}

	void updateIcons(Object searchTag) {
		List<IconDescription> images = createThemeImageList((String) searchTag);
		addUpdate(new ContentReplacement(iconListId(), new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				writeFixedContent(context, out, images);
			}
		}));
	}

	private String iconListId() {
		return getID() + "-fptBody";
	}

	private TextInputControl createPatternFieldControl() {
		StringField patternField = FormFactory.newStringField("icon-search");
		ScriptingRecorder.annotateAsDontRecord(patternField);
		patternField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				updateIcons(newValue);
			}
		});
		TextInputControl result = new TextInputControl(patternField);

		PatternTextInput onKeyUp = new PatternTextInput(result);
		result.setOnInput(onKeyUp);
		return result;
	}

	void switchMode() {
		_searchmode = !_searchmode;
	}

	String getExpertValue() {
		return (String) _field.getValue();
	}

	void setExpertValue(String value) {
		_field.setValue(value);
	}

	/**
	 * This command sets a {@link ThemeImage}.
	 * 
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	public static final class SetThemeImageCommand extends ControlCommand {

		private static final String THEME_IMAGE = "themeImage";

		/**
		 * Singleton of {@link SetThemeImageCommand}.
		 */
		public static final SetThemeImageCommand INSTANCE = new SetThemeImageCommand();

		private static final String COMMAND_NAME = "setThemeImageCommand";

		SetThemeImageCommand() {
			super(COMMAND_NAME);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.ICON_CHOOSER__SET_ICON;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			AbstractFormField field = (AbstractFormField) control.getModel();
			final IconChooserControl iconChooserControl = (IconChooserControl) control;
			String encodedImg;
			
			if (iconChooserControl.isSearchMode()) {
				encodedImg = (String) arguments.get(THEME_IMAGE);
			} else {
				encodedImg = iconChooserControl.getExpertValue();
			}

			iconChooserControl.closeIconChooser();

			try {
				FormFieldInternals.updateField(field, encodedImg);
			} catch (VetoException ex) {
				ex.setContinuationCommand(createUpdateContinuation(field, encodedImg));
				ex.process(commandContext.getWindowScope());
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		private Command createUpdateContinuation(AbstractFormField field, String encodedImg) {
			return new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext commandContext) {
					field.setValue(encodedImg);
					return HandlerResult.DEFAULT_RESULT;
				}
			};
		}
	}

	/**
	 * This command sets the {@link ThemeImage} String into a {@link StringField} within the expert
	 * mode.
	 * 
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	public static final class ThemeImageIntoStringFieldCommand extends ControlCommand {

		private static final String THEME_IMAGE = "themeImage";

		/**
		 * Singleton of {@link ThemeImageIntoStringFieldCommand}.
		 */
		public static final ThemeImageIntoStringFieldCommand INSTANCE = new ThemeImageIntoStringFieldCommand();

		private static final String COMMAND_NAME = "themeImageIntoStringFieldCommand";

		ThemeImageIntoStringFieldCommand() {
			super(COMMAND_NAME);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.ICON_CHOOSER__ICON_TO_STRINGFIELD;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			final IconChooserControl iconChooserControl = (IconChooserControl) control;
			String encodedImg = (String) arguments.get(THEME_IMAGE);
			iconChooserControl.setExpertValue(encodedImg);

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * This command switches the mode of the {@link IconChooserControl} between search and expert.
	 * 
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	public static final class ToggleExpertModeCommand extends ControlCommand {

		/**
		 * Singleton of {@link ToggleExpertModeCommand}.
		 */
		public static final ToggleExpertModeCommand INSTANCE = new ToggleExpertModeCommand();

		private static final String COMMAND_NAME = "switchModeCommand";

		ToggleExpertModeCommand() {
			super(COMMAND_NAME);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.ICON_CHOOSER__SWITCH_MODE;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			final IconChooserControl iconChooserControl = (IconChooserControl) control;
			iconChooserControl.switchMode();
			iconChooserControl.requestRepaint();

			return HandlerResult.DEFAULT_RESULT;
		}
	}
}