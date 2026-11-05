/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link ViewCommand} that stores the modified views of a design session: back to their
 * {@code .view.xml} files, or into the attributes of the model objects they are stored on.
 *
 * <p>
 * Reads the {@link DesignSession} from the configured channel and {@link DesignSource#save(String)
 * saves} every view opened in it that was modified, the views not shown at the moment included. A
 * view file is reloaded by the {@link com.top_logic.layout.view.ViewLoader}, whose cache
 * invalidates itself via timestamps on next access, and the main application window is told to
 * show the saved files anew. A stored view is shown anew by the elements displaying it, which
 * observe the object it is stored on. The user is told which views were saved, or that there was
 * nothing to save.
 * </p>
 */
public class SaveDesignCommand implements ViewCommand {

	/**
	 * Configuration for {@link SaveDesignCommand}.
	 */
	@TagName("save-design")
	public interface Config extends ViewCommand.Config {

		/** Configuration name for {@link #getSession()}. */
		String SESSION = "session";

		@Override
		@ClassDefault(SaveDesignCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * Reference to the channel containing the {@link DesignSession}.
		 */
		@Name(SESSION)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getSession();
	}

	private final Config _config;

	/**
	 * Creates a new {@link SaveDesignCommand}.
	 */
	@CalledByReflection
	public SaveDesignCommand(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		ViewContext viewContext = (ViewContext) context;
		ViewChannel sessionChannel = viewContext.resolveChannel(_config.getSession());
		if (!(sessionChannel.get() instanceof DesignSession session)) {
			return HandlerResult.DEFAULT_RESULT;
		}

		// Only the modified views, of all views opened in the session.
		Map<DesignSource, ViewElement.Config> dirtyViews = session.getDirtyViews();

		if (dirtyViews.isEmpty()) {
			ViewMessages.info(context, I18NConstants.DESIGNER_NOTHING_TO_SAVE);
			return HandlerResult.DEFAULT_RESULT;
		}

		// Serialize every dirty view and check that it can be read back again, before storing any of
		// them. An editor can produce a configuration that serializes but that the view loader
		// rejects, and storing it would leave the application unable to load the view.
		Map<DesignSource, String> contents = new LinkedHashMap<>();
		for (Map.Entry<DesignSource, ViewElement.Config> entry : dirtyViews.entrySet()) {
			contents.put(entry.getKey(), serialize(entry.getKey(), entry.getValue()));
		}
		for (Map.Entry<DesignSource, String> entry : contents.entrySet()) {
			checkLoadable(entry.getKey(), entry.getValue());
		}

		// Store only the dirty views.
		Set<String> savedFiles = new LinkedHashSet<>();
		for (Map.Entry<DesignSource, String> entry : contents.entrySet()) {
			DesignSource source = entry.getKey();
			source.save(entry.getValue());
			session.markSaved(List.of(source));
			if (source instanceof FileDesignSource file) {
				savedFiles.add(file.getPath());
			}
		}

		ViewMessages.info(context, I18NConstants.DESIGNER_SAVED__VIEWS.fill(
			contents.keySet().stream().map(DesignSource::getLabel).collect(Collectors.joining(", "))));

		// Trigger hot-reload of the saved files in the main application window.
		if (!savedFiles.isEmpty() && viewContext.hasChannel(OpenDesignerCommand.APP_CONTEXT_CHANNEL)) {
			ViewChannel appContextChannel =
				viewContext.resolveChannel(new ChannelRef(OpenDesignerCommand.APP_CONTEXT_CHANNEL));
			if (appContextChannel.get() instanceof ViewContext appViewContext) {
				appViewContext.fireViewChanged(savedFiles);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Serializes the given view configuration to XML.
	 */
	private String serialize(DesignSource source, ViewElement.Config viewConfig) {
		StringWriter buffer = new StringWriter();
		try (Writer out = buffer) {
			new ConfigurationWriter(out).write("view", ViewElement.Config.class, viewConfig);
		} catch (XMLStreamException | IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SAVE_VIEW_FAILED__PATH.fill(source.getLabel()), ex);
		}
		return buffer.toString();
	}

	/**
	 * Checks that the serialized view can be loaded again.
	 *
	 * <p>
	 * The check reads the content back through {@link ViewLoader#parseConfig(java.util.List)}, the
	 * parser the view system itself uses, so every constraint it enforces is covered rather than a
	 * hand-picked selection of them.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the content cannot be loaded. Nothing is stored in that case, so the stored views
	 *         stay loadable and the reported problem can be corrected in the designer.
	 */
	private void checkLoadable(DesignSource source, String content) {
		try {
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(content, source.getId())));
		} catch (ConfigurationException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_SAVE_VIEW_NOT_LOADABLE__PATH_DETAILS.fill(source.getLabel(), ex.getMessage()), ex);
		}
	}
}
