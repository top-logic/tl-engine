/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.RegExpUtil;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.wysiwyg.ui.HTMLTextExtractor;
import com.top_logic.layout.wysiwyg.ui.StructuredText;

/**
 * {@link ReactFieldControlProvider} for {@code tl.model.wysiwyg:Html} attributes.
 *
 * <p>
 * The editor's toolbar carries the commands the configuration gives it, next to the formatting
 * buttons of the editor itself. Such a command does whatever a command of the surrounding view
 * does - open a dialog, run a script, write a channel - and reaches the text through the
 * insertion channel: what it writes there is inserted at the cursor.
 * </p>
 *
 * @implNote The editor is {@link #isLarge(FieldSpec) large}: where it has no room, the
 *           {@link #htmlPreview(StructuredText) plain text} of the formatted text stands for it.
 */
public class WysiwygControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration options for {@link WysiwygControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<WysiwygControlProvider> {

		/** @see #getCommands() */
		String COMMANDS = "commands";

		/** @see #getInsertChannel() */
		String INSERT_CHANNEL = "insert-channel";

		@Override
		@ClassDefault(WysiwygControlProvider.class)
		Class<? extends WysiwygControlProvider> getImplementationClass();

		/**
		 * Commands the editor's toolbar offers beyond its formatting buttons.
		 *
		 * <p>
		 * A command goes to the toolbar unless it says otherwise, as every command placed in a
		 * toolbar does. The commands see the channels of the view the edited field is displayed
		 * in, so they take their input from it and hand it on to the dialogs they open, and they
		 * reach the text through the insertion channel.
		 * </p>
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		@Options(fun = AllInAppImplementations.class)
		List<ViewCommand.Config> getCommands();

		/**
		 * Name of the channel whose text the editor inserts at the cursor.
		 *
		 * <p>
		 * Declared where the editor's commands need somewhere to write their result to; the
		 * channel then exists for them alone, beside the channels of the surrounding view. A view
		 * names the channels it works with, and this is one of them.
		 * </p>
		 *
		 * <p>
		 * Left unset, the editor has no such channel and its commands act on the view only.
		 * </p>
		 */
		@Name(INSERT_CHANNEL)
		@Nullable
		String getInsertChannel();

	}

	/**
	 * The HTML elements that display content without containing any text.
	 */
	private static final Set<String> CONTENT_ELEMENTS =
		Set.of("img", "picture", "video", "audio", "iframe", "object", "embed", "svg", "canvas", "hr", "input");

	private final List<ViewCommand> _commands = new ArrayList<>();

	private final List<ViewCommand.Config> _commandConfigs = new ArrayList<>();

	private final String _insertChannel;

	/**
	 * Creates a {@link WysiwygControlProvider} from configuration.
	 */
	@CalledByReflection
	public WysiwygControlProvider(InstantiationContext context, Config config) {
		for (ViewCommand.Config commandConfig : config.getCommands()) {
			ViewCommand command = context.getInstance(commandConfig);
			if (command != null) {
				_commands.add(command);
				_commandConfigs.add(commandConfig);
			}
		}
		_insertChannel = config.getInsertChannel();
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return new ReactWysiwygControl(context, model, _commands, _commandConfigs, _insertChannel);
	}

	@Override
	public boolean isLarge(FieldSpec field) {
		return true;
	}

	@Override
	public String previewText(FieldSpec field, Object value) {
		if (value instanceof StructuredText text) {
			return htmlPreview(text);
		}
		return ReactFieldControlProvider.super.previewText(field, value);
	}

	@Override
	public boolean isEmpty(FieldSpec field, Object value) {
		if (value instanceof StructuredText text) {
			return isEmptyHtml(text);
		}
		return ReactFieldControlProvider.super.isEmpty(field, value);
	}

	/**
	 * Whether the given formatted text displays nothing: it has no embedded images, and its source
	 * holds neither text other than white space nor an element showing content of its own, such as
	 * an image referenced by its address.
	 *
	 * <p>
	 * What an editor leaves behind when its content is deleted - an empty paragraph, a line break -
	 * is thereby empty, too.
	 * </p>
	 *
	 * @param text
	 *        The formatted text, or {@code null}.
	 */
	public static boolean isEmptyHtml(StructuredText text) {
		if (text == null) {
			return true;
		}
		if (!StructuredText.getImagesNullSafe(text).isEmpty()) {
			return false;
		}
		String source = text.getSourceCode();
		if (source == null || source.isBlank()) {
			return true;
		}
		Element body = Jsoup.parseBodyFragment(source).body();
		if (!RegExpUtil.normalizeWhitespace(body.text()).isBlank()) {
			return false;
		}
		for (Element element : body.getAllElements()) {
			if (CONTENT_ELEMENTS.contains(element.normalName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The text of the given formatted text on a single line: without its markup, with its
	 * character references resolved, and with each run of white space - line breaks included -
	 * reduced to a single space.
	 *
	 * @param text
	 *        The formatted text, or {@code null}.
	 * @return The plain text, the empty string for {@code null}.
	 */
	public static String htmlPreview(StructuredText text) {
		if (text == null) {
			return "";
		}
		String plain = HTMLTextExtractor.INSTANCE.getLabel(text);
		return StringServices.normalizeWhiteSpace(RegExpUtil.normalizeWhitespace(plain));
	}

}
