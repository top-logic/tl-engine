/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link ViewCommand} that serializes modified
 * {@link com.top_logic.layout.view.UIElement.Config} trees back to {@code .view.xml} files.
 *
 * <p>
 * Reads the root {@link DesignTreeNode} from the configured channel, traverses the tree to collect
 * all unique source files, and writes each modified {@link com.top_logic.layout.view.ViewElement.Config} back to disk. The
 * {@link com.top_logic.layout.view.ViewLoader} cache auto-invalidates via timestamps on next
 * access.
 * </p>
 */
public class SaveDesignCommand implements ViewCommand {

	/**
	 * Configuration for {@link SaveDesignCommand}.
	 */
	@TagName("save-design")
	public interface Config extends ViewCommand.Config {

		/** Configuration name for {@link #getDesignTree()}. */
		String DESIGN_TREE = "design-tree";

		@Override
		@ClassDefault(SaveDesignCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * Reference to the channel containing the root {@link DesignTreeNode}.
		 */
		@Name(DESIGN_TREE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getDesignTree();
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
		// Resolve the design tree root from the configured channel.
		ViewContext viewContext = (ViewContext) context;
		ViewChannel designTreeChannel = viewContext.resolveChannel(_config.getDesignTree());
		DesignTreeNode root = (DesignTreeNode) designTreeChannel.get();
		if (root == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		// Collect only dirty source files and their root ViewElement.Config.
		Map<String, ViewElement.Config> fileConfigs = new LinkedHashMap<>();
		collectDirtySourceFiles(root, fileConfigs);

		if (fileConfigs.isEmpty()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		// Write only dirty files back to disk.
		for (Map.Entry<String, ViewElement.Config> entry : fileConfigs.entrySet()) {
			String viewPath = entry.getKey();
			ViewElement.Config viewConfig = entry.getValue();
			writeViewConfig(viewPath, viewConfig);
		}

		// Trigger hot-reload in the main application window.
		if (viewContext.hasChannel("appContext")) {
			ViewChannel appContextChannel = viewContext.resolveChannel(new ChannelRef("appContext"));
			Object appContextValue = appContextChannel.get();
			if (appContextValue instanceof ViewContext appViewContext) {
				appViewContext.fireViewChanged(fileConfigs.keySet());
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Traverses the design tree and maps each dirty source file to its root
	 * {@link com.top_logic.layout.view.ViewElement.Config}. Only files containing dirty nodes are
	 * included. After collection, dirty flags on included nodes are cleared.
	 */
	private void collectDirtySourceFiles(DesignTreeNode node, Map<String, ViewElement.Config> fileConfigs) {
		if (node.isDirty() && node.getConfig() instanceof ViewElement.Config viewConfig) {
			fileConfigs.put(node.getSourceFile(), viewConfig);
		}
		for (DesignTreeNode child : node.getChildren()) {
			collectDirtySourceFiles(child, fileConfigs);
		}
	}

	/**
	 * Writes the given {@link com.top_logic.layout.view.ViewElement.Config} to the specified view path.
	 */
	private void writeViewConfig(String viewPath, ViewElement.Config viewConfig) {
		File file = FileManager.getInstance().getIDEFileOrNull(viewPath);
		if (file == null) {
			throw new TopLogicException(
				com.top_logic.layout.view.I18NConstants.ERROR_SAVE_VIEW_NO_IDE_FILE__PATH.fill(viewPath));
		}

		try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StringServices.CHARSET_UTF_8)) {
			new ConfigurationWriter(out).write("view", ViewElement.Config.class, viewConfig);
		} catch (XMLStreamException | IOException ex) {
			throw new TopLogicException(
				com.top_logic.layout.view.I18NConstants.ERROR_SAVE_VIEW_FAILED__PATH.fill(viewPath), ex);
		}
		try {
			XMLPrettyPrinter.normalizeFile(file);
		} catch (Exception ex) {
			Logger.warn("Failed to normalize XML: " + viewPath, ex, SaveDesignCommand.class);
		}
	}
}
