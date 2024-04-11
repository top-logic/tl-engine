/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.toolrow;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultView;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DefaultComponentView} of main header's tool row.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ToolRowView extends DefaultComponentView implements HTMLConstants {

	private static final String CSS_CLASS_TOOL_BAR_CONTAINER = "ToolBarContainer";

	private static final String CSS_CLASS_TOOL_CONTAINER = "ToolContainer";

	private static final String CSS_CLASS_TOOL_CONTAINER_RIGHT = "ToolContainerRight";

	private static final String CSS_CLASS_ICON_CONTAINER = "IconContainer";

	private static final String CSS_CLASS_BUTTON_GROUP = "mtbGroup";

	private static final String CSS_CLASS_HEADER_RIGHT_IMAGE = "header-logo";

	public static final ThemeImage MAPPING_KEY_HEADER_RIGHT_IMAGE = ThemeImage.themeIcon("HEADER_RIGHT_IMAGE");

	private List<ViewConfiguration> _buttonConfigs;

	private List<ViewConfiguration> _messageConfigs;

	/**
	 * Configuration interface of {@link ToolRowView}
	 */
	public interface Config extends PolymorphicConfiguration<ToolRowView> {

		/**
		 * {@link ViewConfiguration}s of clickable buttons of this {@link ToolRowView}
		 */
		List<PolymorphicConfiguration<? extends ViewConfiguration>> getViews();

		/**
		 * {@link ViewConfiguration}s of messages to display of this {@link ToolRowView}.
		 */
		List<PolymorphicConfiguration<? extends ViewConfiguration>> getMessages();
	}

	/**
	 * Creates a {@link ToolRowView} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ToolRowView(InstantiationContext context, Config config) {
		_buttonConfigs = TypedConfiguration.getInstanceList(context, config.getViews());
		_messageConfigs = TypedConfiguration.getInstanceList(context, config.getMessages());
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		return new ToolRowViewImpl(toViews(component, _buttonConfigs), toViews(component, _messageConfigs));
	}

	static class ToolRowViewImpl extends DefaultView {

		private static final ResKey APPLICATION_TITLE = com.top_logic.layout.I18NConstants.APPLICATION_TITLE;

		private List<HTMLFragment> _buttons;

		private List<HTMLFragment> _messages;

		/**
		 * Creates a {@link ToolRowViewImpl}.
		 */
		public ToolRowViewImpl(List<HTMLFragment> buttons, List<HTMLFragment> messages) {
			_buttons = buttons;
			_messages = messages;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			beginDiv(out, CSS_CLASS_TOOL_BAR_CONTAINER, true);
			{
				// tool bar
				beginDiv(out, CSS_CLASS_TOOL_CONTAINER, true);
				{
					if (_buttons.size() > 0) {
						beginDiv(out, CSS_CLASS_ICON_CONTAINER, true);
						out.beginTag(UL);
						{
							for (int index = 0, size = _buttons.size(); index < size; index++) {
								HTMLFragment viewName = _buttons.get(index);
								out.beginTag(LI);
								viewName.write(context, out);
								out.endTag(LI);
							}
						}
						out.endTag(UL);
						out.endTag(DIV); // IconContainer
					}
				}
				out.endTag(DIV); // ToolContainer
			}
			{
				// tool bar
				beginDiv(out, CSS_CLASS_TOOL_CONTAINER_RIGHT, true);
				{
					beginDiv(out, CSS_CLASS_ICON_CONTAINER, true);
					out.beginTag(UL);
					for (int index = 0, size = _messages.size(); index < size; index++) {
						out.beginBeginTag(LI);
						out.writeAttribute(STYLE_ATTR, "width:3px");
						out.endBeginTag();
						out.writeText(HTMLConstants.NBSP);
						out.endTag(LI);
						out.beginTag(LI);
						_messages.get(index).write(context, out);
						out.endTag(LI);
					}
					{
						// right image
						Theme theTheme = ThemeFactory.getTheme();
						out.beginBeginTag(LI);
						out.writeAttribute(STYLE_ATTR, "width:3px");
						out.endBeginTag();
						out.writeText(HTMLConstants.NBSP);
						out.endTag(LI);
						out.beginTag(LI);
						out.beginBeginTag(DIV);
						out.writeAttribute(CLASS_ATTR, CSS_CLASS_HEADER_RIGHT_IMAGE);
						out.endBeginTag();
						XMLTag tag = MAPPING_KEY_HEADER_RIGHT_IMAGE.toIcon();
						tag.beginBeginTag(context, out);
						out.writeAttribute(WIDTH_ATTR,
							(int) theTheme.getValue(Icons.HEADER_RIGHT_IMAGE_WIDTH).getValue());
						out.writeAttribute(HEIGHT_ATTR,
							(int) theTheme.getValue(Icons.HEADER_RIGHT_IMAGE_HEIGHT).getValue());
						out.writeAttribute(ALT_ATTR, context.getResources().getString(APPLICATION_TITLE));
						tag.endEmptyTag(context, out);
						out.endTag(DIV);
						out.endTag(LI);
					}
					out.endTag(UL);
					out.endTag(DIV); // IconContainer
				}
				out.endTag(DIV); // ToolContainer
			}
			out.endTag(DIV); // ToolBarContainer
		}
	}

	static void beginDiv(TagWriter out, String aClass, boolean endTagBegin) {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, aClass);
		if (endTagBegin) {
			out.endBeginTag();
		}
	}

	/**
	 * Special {@link DefaultComponentView}, to separate buttons or messages of the
	 * {@link ToolRowView}
	 */
	public static final class GroupView extends DefaultComponentView {

		private List<ViewConfiguration> _buttonConfigs;

		/**
		 * Configuration interface of {@link GroupView}
		 */
		@TagName("group")
		public interface Config extends PolymorphicConfiguration<GroupView> {
			
			/**
			 * {@link ViewConfiguration}s of clickable buttons of this {@link ToolRowView}
			 */
			List<PolymorphicConfiguration<? extends ViewConfiguration>> getViews();

		}

		/**
		 * Creates a new {@link GroupView} from the given configuration.
		 * 
		 * @param context
		 *        {@link InstantiationContext} to instantiate sub configurations.
		 * @param config
		 *        Configuration for this {@link GroupView}.
		 */
		public GroupView(InstantiationContext context, Config config) {
			_buttonConfigs = TypedConfiguration.getInstanceList(context, config.getViews());
		}

		@Override
		public HTMLFragment createView(LayoutComponent component) {
			return new GroupViewImpl(toViews(component, _buttonConfigs));
		}

		static class GroupViewImpl extends DefaultView {

			private List<HTMLFragment> _buttons;

			/**
			 * Creates a {@link GroupViewImpl}.
			 */
			public GroupViewImpl(List<HTMLFragment> buttons) {
				_buttons = buttons;
			}

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.beginBeginTag(UL);
				out.writeAttribute(CLASS_ATTR, CSS_CLASS_BUTTON_GROUP);
				out.endBeginTag();
				for (HTMLFragment view : _buttons) {
					out.beginTag(LI);
					view.write(context, out);
					out.endTag(LI);
				}
				out.endTag(UL);
			}

			@Override
			public boolean isVisible() {
				for (HTMLFragment view : _buttons) {
					if (Fragments.isVisible(view)) {
						return true;
					}
				}
				return false;
			}
		}
	}
}
