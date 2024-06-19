/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.Drag;
import com.top_logic.layout.DragAndDrop;
import com.top_logic.layout.DragAndDropCommand;
import com.top_logic.layout.Drop;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ConstantCommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.control.ColorChooserSelectionControl.ColorDisplay.ColorListener;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.renderers.DefaultTabBarRenderer;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.tabbar.DefaultTabBarModel;
import com.top_logic.layout.tabbar.TabBarControl;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.SimpleCard;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * This class provides the selection of a color
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class ColorChooserSelectionControl extends AbstractConstantControl {

	public static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ControlCommand[] {
															new SetColorCommand()
	});
	
	/**
	 * Colors to be displayed by color chooser for direct selection
	 */
	public static interface Config extends ConfigurationItem {
		
		/**
		 * Height of the dialog in pixels.
		 */
		@IntDefault(250)
		int getHeight();
		
		/**
		 * Width of the dialog in pixels.
		 */
		@IntDefault(442)	
		int getWidth();

		/**
		 * Number of columns in the area with predefined colors.
		 */
		@IntDefault(4)
		int getColumns();

		/**
		 * Number of rows in the area with predefined colors.
		 */
		@IntDefault(4)
		int getRows();
		
		/**
		 * Predefined color of the named cell in the area with predefined colors.
		 * 
		 * <p>
		 * The color cells are named in an Excel-like fashion: A1,..., Z1 for the first row, A1,...,
		 * A99 for the first column.
		 * </p>
		 */
		@Key(ColorItem.NAME_ATTRIBUTE)
		Map<String,ColorItem> getColors();

		/**
		 * A {@link ColorItem} has a name which is the name of the cell for which this color is to
		 * be used and an hex value which defines the actual color value.
		 * 
		 * @see Config#getColors()
		 */
		interface ColorItem extends NamedConfigMandatory {
			/**
			 * Color hex value, <code>null</code> means no predefined color (can be customized by
			 * the user).
			 */
			@NullDefault
			public String getHexValue();
		}

	}

	static class Reset extends ConstantCommandModel {

		private final ColorChooserSelectionControl _chooser;

		public Reset(ColorChooserSelectionControl chooser) {
			_chooser = chooser;
		}

		@Override
		public ThemeImage getImage() {
			return Icons.RESET_COLORS;
		}

		@Override
		public String getTooltip() {
			return Resources.getInstance().getString(I18NConstants.RESET_COLORS);
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			_chooser._palette.resetColors();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private final Config _config;

	private final PopupDialogModel _dialog;

	private final ColorChooserControl _colorDisplay;

	private ColorPalette _palette;

	/**
	 * Creates a {@link ColorChooserSelectionControl}.
	 * 
	 * @param dialogModel
	 *        The {@link PopupDialogModel} of the currently open popup.
	 * @param control
	 *        The opening {@link ColorChooserControl} displaying the current color.
	 */
	public ColorChooserSelectionControl(PopupDialogModel dialogModel, ColorChooserControl control, Config config) {
		super(COMMANDS);
		
		_config = config;
		_dialog = dialogModel;
		_colorDisplay = control;

		dialogModel.getToolBar().defineGroup(CommandHandlerFactory.TABLE_BUTTONS_GROUP).addButton(new Reset(this));
	}

	@Override
	protected String getTypeCssClass() {
		return "cColorComposer";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		DragAndDrop<Color> dnd = new DragAndDrop<>(getScope().getFrameScope());

		String currentHtmlColor = _colorDisplay.getHtmlColor();
		Color currentColor = toColor(currentHtmlColor);
		ColorDisplay oldColor = new ColorDisplay(this, dnd, currentColor, ColorDisplay.Kind.DRAG);
		ColorDisplay newColor = new ColorDisplay(this, dnd, currentColor, ColorDisplay.Kind.DRAG);
		_palette = new ColorPalette(this, _config, dnd);
		ColorMixer mixer = new ColorMixer();
		DefaultTabBarModel tabBarModel =
			new DefaultTabBarModel(Arrays.<Card> asList(
				new SimpleCard(TabInfo.newTabInfo(I18NConstants.COLOR_PALETTE), I18NConstants.COLOR_PALETTE.getKey(),
					_palette),
				new SimpleCard(TabInfo.newTabInfo(I18NConstants.COLOR_MIXER), I18NConstants.COLOR_MIXER.getKey(),
					mixer)));
		tabBarModel.setSelectedIndex(0);

		TabBarControl tabBar = new TabBarControl(tabBarModel);
		tabBar.setRenderer(DefaultTabBarRenderer.INSTANCE);
		DeckPaneControl display = new DeckPaneControl(tabBarModel, CardControlContent.INSTANCE);
		
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		{
			tabBar.write(context, out);

			out.beginBeginTag(TABLE);
			out.writeAttribute(CLASS_ATTR, "colorDialog");
			out.writeAttribute(STYLE_ATTR, "margin:0px; padding:0px;");
			out.endBeginTag();
			{
				out.beginTag(TR);
				out.beginTag(TD);
				{
					out.beginTag(TABLE);
					{
						out.beginTag(TR);
						{
							// color selection fields
							out.beginBeginTag(TD);
							writeId(out, ("selectionCell"));
							// out.writeAttribute(COLSPAN_ATTR, "5");
							out.endBeginTag();
							{
								display.write(context, out);
							}
							out.endTag(TD);

							// control of color selection
							out.beginBeginTag(TD);
							writeId(out, ("controlCell"));
							// out.writeAttribute(ROWSPAN_ATTR, "2");
							out.endBeginTag();
							{
								writeControlPart(context, out, oldColor, newColor);
							}
							out.endTag(TD);
						}
						out.endTag(TR);
					}
					out.endTag(TABLE);
				}
				out.endTag(TD);
				out.endTag(TR);
			}
			out.endTag(TABLE);
		}
		out.beginScript();
		out.append("colorChooser.dialogId=\"" + getID() + "\";");
		out.append("colorChooser.previewId=\"" + newColor.getID() + "\";");
		out.append("colorChooser.colorOfElement = new colorChooser.MyColor(\"" + currentHtmlColor + "\"); ");
		out.endScript();
		out.endTag(DIV);
	}

	private void writeId(TagWriter out, String suffix) throws IOException {
		out.beginAttribute(ID_ATTR);
		out.writeAttributeText(getID());
		out.writeAttributeText('-');
		out.writeAttributeText(suffix);
		out.endAttribute();
	}

	private void writeControlPart(DisplayContext context, TagWriter out, ColorDisplay oldColor, ColorDisplay newColor)
			throws IOException {
		out.beginBeginTag(TABLE);
		out.writeAttribute(CLASS_ATTR, "colorDisplay");
		out.writeAttribute(STYLE_ATTR, "width:100%");
		out.endBeginTag();
		{
			out.beginTag(COLGROUP);
			{
				out.beginBeginTag(COL);
				out.writeAttribute(STYLE_ATTR, "width:40%");
				out.endEmptyTag();

				out.beginBeginTag(COL);
				out.writeAttribute(STYLE_ATTR, "width:60%");
				out.endEmptyTag();
			}
			out.endTag(COLGROUP);

			out.beginTag(TR);
			{
				out.beginTag(TD);
				out.writeText(Resources.getInstance().getString(I18NConstants.CURRENT_COLOR) + ":");
				out.endTag(TD);

				out.beginBeginTag(TD);
				out.writeAttribute(CLASS_ATTR, "colorDisplay");
				out.endBeginTag();
				oldColor.write(context, out);
				out.endTag(TD);
			}
			out.endTag(TR);

			out.beginBeginTag(TR);
			out.writeAttribute(CLASS_ATTR, "spacer");
			out.endBeginTag();
			out.endTag(TR);

			out.beginTag(TR);
			{
				out.beginTag(TD);
				out.writeText(Resources.getInstance().getString(I18NConstants.CHOSEN_COLOR) + ":");
				out.endTag(TD);

				out.beginBeginTag(TD);
				out.writeAttribute(CLASS_ATTR, "colorDisplay");
				out.endBeginTag();
				newColor.write(context, out);
				out.endTag(TD);
			}
			out.endTag(TR);
		}

		out.beginBeginTag(TR);
		out.writeAttribute(CLASS_ATTR, "spacer");
		out.endBeginTag();
		out.endTag(TR);

		{
			Color color = _colorDisplay.getColor();
			writeColorInputField(out, Resources.getInstance().getString(I18NConstants.RED_COMPONENT), "red",
				Integer.toString(color.getRed()));
			writeColorInputField(out, Resources.getInstance().getString(I18NConstants.GREEN_COMPONENT), "green",
				Integer.toString(color.getGreen()));
			writeColorInputField(out, Resources.getInstance().getString(I18NConstants.BLUE_COMPONENT), "blue",
				Integer.toString(color.getBlue()));

			String currentColor = _colorDisplay.getHtmlColor();
			writeColorInputField(out, Resources.getInstance().getString(I18NConstants.HEX_CODE), "hex", currentColor);
		}
		out.endTag(TABLE);

		out.beginBeginTag(INPUT);
		out.writeAttribute(TYPE_ATTR, "button");
		writeId(out, ("okButton"));
		out.writeAttribute(VALUE_ATTR, Resources.getInstance().getString(I18NConstants.POPUP_SELECT_SUBMIT));
		writeOnClickSetColor(out);
		out.writeAttribute(STYLE_ATTR, "width:100%");
		out.endEmptyTag();

		out.beginBeginTag(INPUT);
		out.writeAttribute(TYPE_ATTR, "button");
		writeId(out, ("cancelButton"));
		out.writeAttribute(VALUE_ATTR, Resources.getInstance().getString(I18NConstants.POPUP_SELECT_CANCEL));
		out.writeAttribute(ONCLICK_ATTR, "services.form.BrowserWindowControl.closeAllPopupDialogs();");
		out.writeAttribute(STYLE_ATTR, "width:100%");
		out.endEmptyTag();
	}

	private void writeOnClickSetColor(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("services.form.ColorChooserControl.setColor(");
		writeIdJsString(out);
		out.append(");");
		out.endAttribute();
	}

	private void writeColorInputField(TagWriter out, String label, String suffix, String value) throws IOException {
		out.beginTag(TR);
		{
			out.beginTag(TD);
			out.writeText(label + ":");
			out.endTag(TD);

			out.beginTag(TD);
			{
				out.beginBeginTag(INPUT);
				out.writeAttribute(TYPE_ATTR, "text");
				writeId(out, suffix);
				out.writeAttribute(SIZE_ATTR, "7");
				out.writeAttribute(VALUE_ATTR, value);
				out.writeAttribute(ONCHANGE_ATTR, "colorChooser.fieldChanged(this);");
				out.endEmptyTag();
			}
			out.endTag(TD);
		}
		out.endTag(TR);
	}

	static Config getConfig() {
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		if (config == null) {
			config = TypedConfiguration.newConfigItem(Config.class);
		}
		return config;
	}

	static Color toColor(String htmlColor) {
		if (StringServices.isEmpty(htmlColor)) {
			return null;
		}
		return new Color(Integer.parseInt(htmlColor.substring(1), 16));
	}

	static class ColorPalette extends AbstractVisibleControl implements ColorListener {

		private static final String COLOR_PALETTE_KEY = "colorPalette";

		private final ColorChooserSelectionControl _parent;

		private final Config _config;

		private final int _rows;

		private final int _columns;

		private final ColorDisplay[] _cells;

		public ColorPalette(ColorChooserSelectionControl parent, Config config, DragAndDrop<Color> dnd) {
			_parent = parent;
			_config = config;
			_rows = config.getRows();
			_columns = config.getColumns();
			_cells = new ColorDisplay[_rows * _columns];

			PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
			@SuppressWarnings("unchecked")
			List<String> colors = (List<String>) personalConfiguration.getJSONValue(COLOR_PALETTE_KEY);

			int n = 0;
			for (int row = 0; row < _rows; row++) {
				for (int column = 0; column < _columns; column++) {
					String htmlColor;
					if (colors != null && n < colors.size()) {
						htmlColor = colors.get(n);
					} else {
						htmlColor = getHtmlColor(config, row, column);
					}
					Color color = toColor(htmlColor);
					ColorDisplay colorDisplay = new ColorDisplay(_parent, dnd, color, ColorDisplay.Kind.DRAG_DROP);
					colorDisplay.addListener(ColorDisplay.COLOR_PROPERTY, this);
					_cells[n++] = colorDisplay;
				}
			}
		}

		@Override
		public Object getModel() {
			return null;
		}

		public void resetColors() {
			for (ColorDisplay display : _cells) {
				display.removeListener(ColorDisplay.COLOR_PROPERTY, this);
			}

			int n = 0;
			for (int row = 0; row < _rows; row++) {
				for (int column = 0; column < _columns; column++) {
					String htmlColor = getHtmlColor(_config, row, column);
					if (htmlColor != null) {
						Color color = toColor(htmlColor);
						_cells[n++].setColor(color);
					}
				}
			}

			for (ColorDisplay display : _cells) {
				display.addListener(ColorDisplay.COLOR_PROPERTY, this);
			}
			updateColors();
		}

		@Override
		public void handleColorChanged(ColorDisplay sender, Color oldValue, Color newValue) {
			updateColors();
		}

		private void updateColors() {
			List<String> colors = new ArrayList<>(_cells.length);
			for (ColorDisplay display : _cells) {
				colors.add(ColorChooserControl.toHtmlColor(display.getColor()));
			}

			PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
			personalConfiguration.setJSONValue(COLOR_PALETTE_KEY, colors);
		}

		@Override
		protected String getTypeCssClass() {
			return "cColorPalette";
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			// standard part
			out.beginBeginTag(DIV);
			writeControlAttributes(context, out);
			out.writeAttribute(STYLE_ATTR, "height:192px;width:225px;");
			out.endBeginTag();
			{
				int n = 0;
				for (int row = 0, rows = _rows; row < rows; row++) {
					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, "row");
					out.endBeginTag();
					for (int column = 0, columns = _columns; column < columns; column++) {
						ColorDisplay cell = _cells[n++];
						cell.fetchID(getScope().getFrameScope());
						out.beginBeginTag(SPAN);
						out.writeAttribute(ONCLICK_ATTR, "return colorChooser.colorCellClick('" + cell.getID() + "');");
						out.endBeginTag();
						cell.write(context, out);
						out.endTag(SPAN);
					}
					out.endTag(DIV);
				}
			}
			out.endTag(DIV);
		}

		private String getHtmlColor(Config config, int row, int column) {
			String cellName = "" + Character.toString((char) ('A' + column)) + row;
			Config.ColorItem colorConfigItem = config.getColors().get(cellName);
			if (colorConfigItem != null) {
				return colorConfigItem.getHexValue();
			} else {
				return null;
			}
		}

	}

	static class ColorDisplay extends AbstractVisibleControl implements Drag<Color>, Drop<Color> {

		public enum Kind {
			NONE,

			DRAG {
				@Override
				boolean canDrag() {
					return true;
				}
			},

			DROP {
				@Override
				boolean canDrop() {
					return true;
				}
			},

			DRAG_DROP {
				@Override
				boolean canDrag() {
					return true;
				}

				@Override
				boolean canDrop() {
					return true;
				}
			};

			boolean canDrag() {
				return false;
			}

			boolean canDrop() {
				return false;
			}
		}

		protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ControlCommand[] {
			DragAndDropCommand.INSTANCE, SetColor.INSTANCE, SelectColor.INSTANCE
		});

		public static final EventType<ColorListener, ColorDisplay, Color> COLOR_PROPERTY =
			new NoBubblingEventType<>("color") {

				@Override
				protected void internalDispatch(ColorListener listener, ColorDisplay sender, Color oldValue,
						Color newValue) {
					listener.handleColorChanged(sender, oldValue, newValue);
				}

			};

		public interface ColorListener extends PropertyListener {

			void handleColorChanged(ColorDisplay sender, Color oldValue, Color newValue);

		}

		private final ColorChooserSelectionControl _parent;

		private final DragAndDrop<Color> _dnd;

		private Color _color;

		private final Kind _kind;

		public ColorDisplay(ColorChooserSelectionControl parent, DragAndDrop<Color> dnd, Color color, Kind kind) {
			super(COMMANDS);
			_parent = parent;

			_dnd = dnd;
			_color = color;
			_kind = kind;

			if (_kind.canDrop()) {
				_dnd.addDropTarget(this);
			}
		}

		@Override
		public Object getModel() {
			return null;
		}

		public Color getColor() {
			return _color;
		}

		public void setColor(Color newColor) {
			Color oldColor = _color;
			_color = newColor;
			notifyListeners(COLOR_PROPERTY, this, oldColor, newColor);
			requestRepaint();
		}

		@Override
		public void notifyDrag(String dropId, Object dragInfo, Object dropInfo) {
			try {
				Drop<? super Color> drop = _dnd.getDrop(dropId);
				if (drop != null) {
					Color myColor = _color;

					Color otherColor = null;
					if (_kind.canDrop()) {
						if (drop instanceof ColorDisplay) {
							ColorDisplay display = (ColorDisplay) drop;
							otherColor = display.getColor();
						}
					}

					drop.notifyDrop(myColor, dropInfo);

					if (otherColor != null) {
						setColor(otherColor);
					}
				}
				Drop<? super Color> other = drop;
			} catch (DropException ex) {
				Logger.error("Drag'n drop failed.", ex, ColorDisplay.class);
			}
		}

		@Override
		public void notifyDrop(Color value, Object dropInfo) throws com.top_logic.layout.Drop.DropException {
			setColor(value);
			requestRepaint();
		}

		@Override
		protected String getTypeCssClass() {
			return "cColorDisplay";
		}

		@Override
		protected void writeControlClassesContent(Appendable out) throws IOException {
			super.writeControlClassesContent(out);

			if (_kind.canDrag()) {
				HTMLUtil.appendCSSClass(out, "canDrag");
			}
			if (_kind.canDrop()) {
				HTMLUtil.appendCSSClass(out, "canDrop");
			}

			if (_color == null) {
				HTMLUtil.appendCSSClass(out, "noColor");
			}
		}

		@Override
		protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
			super.writeControlAttributes(context, out);
			if (_kind.canDrag()) {
				_dnd.writeDNDInfo(out);
			}

			if (_kind.canDrag() || _kind.canDrop()) {
				out.writeAttribute(TL_TYPE_ATTR, "services.DnD.Draggable");
			}

			if (_color != null) {
				out.writeAttribute(STYLE_ATTR, "background-color: " + ColorChooserControl.toHtmlColor(_color));
			} else {
				String tooltipHtml = Resources.getInstance().getString(I18NConstants.CUSTOMIZE_COLOR_TOOLTIP);
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltipHtml);
			}

			out.writeAttribute(ONDBLCLICK_ATTR, "services.form.ColorDisplay.selectColor('" + getID() + "');");
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
			out.endBeginTag();
			out.writeText(NBSP);
			out.endTag(SPAN);
		}

		public static final class SetColor extends ControlCommand {

			/**
			 * Singleton {@link ColorChooserSelectionControl.ColorDisplay.SetColor} instance.
			 */
			public static final SetColor INSTANCE = new SetColor();

			private SetColor() {
				super("setColor");
			}

			@Override
			protected HandlerResult execute(DisplayContext commandContext, Control control,
					Map<String, Object> arguments) {
				String htmlColor = (String) arguments.get("value");
				if (htmlColor != null) {
					((ColorDisplay) control).setColor(toColor(htmlColor));
				}
				return HandlerResult.DEFAULT_RESULT;
			}

			@Override
			public ResKey getI18NKey() {
				return com.top_logic.layout.form.control.I18NConstants.SET_DIALOG_COLOR_ENTRY;
			}
		}

		public static final class SelectColor extends ControlCommand {

			/**
			 * Singleton {@link ColorChooserSelectionControl.ColorDisplay.SetColor} instance.
			 */
			public static final SelectColor INSTANCE = new SelectColor();

			private SelectColor() {
				super("selectColor");
			}

			@Override
			protected HandlerResult execute(DisplayContext commandContext, Control control,
					Map<String, Object> arguments) {
				((ColorDisplay) control).onSelect();
				return HandlerResult.DEFAULT_RESULT;
			}

			@Override
			public ResKey getI18NKey() {
				return com.top_logic.layout.form.control.I18NConstants.COLOR_SELECTION;
			}
		}

		void onSelect() {
			_parent.selectColor(ColorChooserControl.toHtmlColor(getColor()));
		}

	}

	static class ColorMixer extends AbstractVisibleControl {

		@Override
		public Object getModel() {
			return null;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(DIV);
			writeControlAttributes(context, out);
			out.writeAttribute(STYLE_ATTR, "position:relative; height:192px; width:225px;");
			out.endBeginTag();
			{
				// construction of transparentBackground.png in GIMP: Color
				// gradient, color to transparent,
				// (black,normal mode,up),(white,normal mode,left to
				// right),(black,multiplicative mode,up)
				XMLTag icon1 = Icons.TRANSPARENT_BACKGROUND.toIcon();
				icon1.beginBeginTag(context, out);
				writeId(out, ("colorField"));
				out.writeAttribute(CLASS_ATTR, "tl-colorchooser__colorf-field");
				icon1.endEmptyTag(context, out);

				XMLTag icon2 = Icons.CIRCLE.toIcon();
				icon2.beginBeginTag(context, out);
				writeId(out, ("circle"));
				out.writeAttribute(CLASS_ATTR, "tl_colorchooser__circle");
				icon2.endEmptyTag(context, out);

				// construction of colorline.png in GIMP. Color gradient HSV
				// from red to red
				XMLTag icon3 = Icons.COLORLINE.toIcon();
				icon3.beginBeginTag(context, out);
				writeId(out, ("colorLine"));
				out.writeAttribute(CLASS_ATTR, "tl_colorchooser__color-line");
				icon3.endEmptyTag(context, out);

				XMLTag icon4 = Icons.LINE.toIcon();
				icon4.beginBeginTag(context, out);
				writeId(out, ("hline"));
				out.writeAttribute(CLASS_ATTR, "tl_colorchooser__hline");
				icon4.endEmptyTag(context, out);
			}

			out.beginScript();
			out.append("colorChooser.composerId=\"" + getID() + "\";");
			out.append("colorChooser.initColorComposer(); ");
			out.endScript();

			out.endTag(DIV);
		}

		private void writeId(TagWriter out, String suffix) throws IOException {
			out.beginAttribute(ID_ATTR);
			out.writeAttributeText(getID());
			out.writeAttributeText('-');
			out.writeAttributeText(suffix);
			out.endAttribute();
		}

	}

	/**
	 * This command sets the color of the color chooser control
	 */
	public static final class SetColorCommand extends ControlCommand {
		
		private static final String COLOR_PARAM = "color";
		private static final String COMMAND_NAME = "setSelectedColor";
				
		public SetColorCommand() {
			super(COMMAND_NAME);
		}
								
		@Override
		protected HandlerResult execute(DisplayContext commandContext,
										Control control, Map<String, Object> arguments) {
													
			ColorChooserSelectionControl colorChooserSelectionControl = (ColorChooserSelectionControl) control;
			if((arguments != null) && arguments.containsKey(COLOR_PARAM))
			{
				String colorSpec = (String) arguments.get(COLOR_PARAM);
				colorChooserSelectionControl.selectColor(colorSpec);
			}
			
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return com.top_logic.layout.form.control.I18NConstants.SET_FIELD_COLOR;
		}
	}

	void selectColor(final String colorSpec) {
		_dialog.setClosed();
		final ComplexField fieldModel = (ComplexField) _colorDisplay.getFieldModel();
		try {
			FormFieldInternals.updateField(fieldModel, colorSpec);
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					try {
						Object parsedValue = FormFieldInternals.parseRawValue(fieldModel, colorSpec);
						fieldModel.setValue(parsedValue);
					} catch (CheckException ex) {
						// Ignore
					}
					return HandlerResult.DEFAULT_RESULT;
				}
			});
			ex.process(getWindowScope());
		}
	}

	/**
	 * This method creates a new color chooser selection dialog
	 *  
	 * @param commandContext - the display context
	 * @param control - the control, that wants to create the dialog
	 * @return the color chooser selection dialog
	 */
	public static PopupDialogControl createDialog(DisplayContext commandContext, final ColorChooserControl control) {
		Config config = getConfig();
		
		// Create dialog model
		int height = config.getHeight();
		int tabHeight = ThemeFactory.getTheme().getValue(Icons.TAB_COMPONENT_DEFAULT_TAB_HEIGHT);
		height += tabHeight;
	
		DefaultLayoutData layout = new DefaultLayoutData(
			dim(config.getWidth(), PIXEL), 100,
			dim(height, PIXEL), 100,
			Scrolling.AUTO);
		DisplayValue title = new ResourceText(I18NConstants.COLOR_CHOOSER_TITLE);
		PopupDialogModel dialogModel = new DefaultPopupDialogModel(title, layout, 3);
		
		// Create Controls and setup
		final PopupDialogControl dialog =
			new PopupDialogControl(control.getFrameScope(), dialogModel, control.getInputId());
		
		ColorChooserSelectionControl contentControl =
			new ColorChooserSelectionControl(dialogModel, control, config);
		dialog.setContent(contentControl);
					
		return dialog;
	}
}
