/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedResource;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.json.adapt.ReaderR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Creates a popup where the icons of font awesome can be chosen.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class IconInputControl extends AbstractFormFieldControl {

	/**
	 * {@link ConfigurationItem} for the {@link IconChooserControl}. Loads the {@link NamedResource}
	 * in a list to generate a list of {@link ThemeImage}s out of it.
	 * 
	 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * List of files including {@link ThemeImage}s.
		 */
		@Key(NamedResource.NAME_ATTRIBUTE)
		List<ThemeImageMetaData> getResources();

		/**
		 * height of displayed control
		 */
		@IntDefault(200)
		int getHeight();

		/**
		 * width of displayed control
		 */
		@IntDefault(300)
		int getWidth();

	}

	/**
	 * Config for the Metadata of a {@link NamedResource}.
	 * 
	 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
	 */
	public interface ThemeImageMetaData extends NamedResource {

		/**
		 * Mapping between the style of the ressource and the encoded {@link ThemeImage}s.
		 */
		@MapBinding(key = "style", attribute = "encoded-icon")
		Map<String, String> getIconMappings();
	}

	private List<IconBundle> _resourceList;

	private static final String CSS_CLASS = "preview";

	/**
	 * Commands registered at this control.
	 * 
	 * @see #getCommand(String)
	 */
	public static final Map<?, ?> ICON_CHOOSER_COMMANDS = createCommandMap(TextInputControl.COMMANDS,
		new ControlCommand[] {
			new OpenIconChooserCommand()
		});

	@Override
	protected String getTypeCssClass() {
		return "cIconChooser";
	}

	/**
	 * Creates an {@link IconInputControl}.
	 * 
	 * @param model
	 *        Server-side representation for this input field.
	 */
	public IconInputControl(FormField model) {
		super(model, ICON_CHOOSER_COMMANDS);
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			renderButton(context, out);
		}
		out.endTag(SPAN);
	}

	/**
	 * If the list of resources does not exist it will parse the resources.
	 * 
	 * @see #parseResources()
	 * @return The list of resources.
	 */
	public List<IconBundle> getResources() {
		if (_resourceList == null) {
			_resourceList = parseResources();
		}
		return _resourceList;
	}

	private void renderButton(DisplayContext context, TagWriter out) throws IOException {
		FormField field = (FormField) getModel();

		ButtonWriter buttonWriter = new ButtonWriter(this, getIcon(), getCommand(OpenIconChooserCommand.COMMAND_NAME));
		buttonWriter.setTooltip(I18NConstants.ICON_CHOOSER__OPEN.fill(getModel().getLabel()));
		buttonWriter.setCss(CSS_CLASS);
		
		if (field.isDisabled()) {
			buttonWriter.writeDisabledButton(context, out);
		} else {
			buttonWriter.writeButton(context, out);
		}
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		getIcon().write(context, out);
		out.endTag(SPAN);
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		// If the value changed repaint the element
		requestRepaint();
	}

	/**
	 * Creates a list of resource-items. One resource-item is a {@link Pair} of a parsed
	 * {@link NamedResource} as {@link JSON} and its associated style-to-encoded-icon-mappings.
	 * 
	 * @return List of resource-items.
	 */
	protected List<IconBundle> parseResources() {
		List<IconBundle> resourceList = new ArrayList<>();

		FileManager fm = FileManager.getInstance();

		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		List<ThemeImageMetaData> resources = config.getResources();

		Map<String, String> prefixMapping = new HashMap<>();

		for (ThemeImageMetaData resource : resources) {
			prefixMapping = resource.getIconMappings();
			String resourceName = resource.getResource();
			try {
				try (InputStream stream = fm.getStream(resourceName)) {
					Reader reader = new InputStreamReader(stream, StringServices.UTF8);
					JsonReader json = new JsonReader(new ReaderR(reader));
					resourceList.add(IconBundle.read(json, prefixMapping));
				}
			} catch (IOException ex) {
				Logger.warn("Cannot read icon definition '" + resourceName + "'.", ex, IconInputControl.class);
			}
		}

		return resourceList;
	}

	/**
	 * The theme image of the icon being edited.
	 */
	public ThemeImage getIcon() {
		FormField field = (FormField) getModel();
		ThemeImage result = (ThemeImage) field.getValue();
		if (result == null) {
			return Icons.EMPTY_ICON;
		}
		return result;
	}

	/**
	 * This command opens a new icon chooser dialog.
	 * 
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	public static final class OpenIconChooserCommand extends ControlCommand {

		private static final String COMMAND_NAME = "openIconChooser";

		OpenIconChooserCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {

			IconInputControl iconChooseControl = (IconInputControl) control;

			openIconChooserDialog(commandContext, iconChooseControl);

			return HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * Opens icon chooser dialog for given icon input control.
		 * 
		 * @param commandContext
		 *        The display context
		 * @param iconControl
		 *        The control owning the popup
		 */
		private void openIconChooserDialog(DisplayContext commandContext, IconInputControl iconControl) {

			Config config = ApplicationConfig.getInstance().getConfig(Config.class);
			int height = config.getHeight();
			int width = config.getWidth();
			// Create dialog model
			final PopupDialogModel dialogModel = new DefaultPopupDialogModel(
				null,
				new DefaultLayoutData(width, DisplayUnit.PIXEL, 100, height, DisplayUnit.PIXEL, 100,
					Scrolling.AUTO),
				1);

			dialogModel.addListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, new DialogClosedListener() {

				@Override
				public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
					dialogModel.removeListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, this);
				}
			});

			// Create Controls and setup
			final PopupDialogControl iconDialog =
				new PopupDialogControl(iconControl.getFrameScope(), dialogModel, iconControl.getID());

			IconChooserControl iconChooserControl =
				new IconChooserControl(dialogModel, iconControl.getFieldModel(), iconControl.getResources());
			iconDialog.setContent(iconChooserControl);

			iconControl.getFrameScope().getWindowScope().openPopupDialog(iconDialog);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.ICON_CHOOSER_COMMAND;
		}
	}

}
