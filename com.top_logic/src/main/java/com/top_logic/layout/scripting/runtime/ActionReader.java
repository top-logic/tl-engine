/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.scripting.action.ApplicationAction;

/**
 * Deserializes {@link ApplicationAction}s from XML.
 * <p>
 * Assumes that the Action is encoded in {@link #ACTION_CHARSET}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ActionReader {

	/**
	 * The file ending of {@link ApplicationAction} scripts.
	 */
	public static final String FILE_ENDING = ".xml";

	/**
	 * The {@link Charset} used for reading {@link ApplicationAction} scripts. (UTF-8)
	 */
	public static final Charset ACTION_CHARSET = Charset.forName("UTF-8");

	/**
	 * The instance of the {@link ActionReader}. This is not a singleton, as (potential) subclasses
	 * can create further instances.
	 */
	public static final ActionReader INSTANCE = new ActionReader();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected ActionReader() {
		// See JavaDoc above.
	}

	/**
	 * Tries to parse an {@link ApplicationAction} from the given String.
	 */
	public Maybe<ApplicationAction> tryReadAction(String actionXml) {
		try {
			return Maybe.some(ActionReader.INSTANCE.readAction(CharacterContents.newContent(actionXml)));
		} catch (ConfigurationException parsingException) {
			if (Logger.isDebugEnabled(ActionReader.class)) {
				Logger.debug("Tried whether some text is a parsable xml script of an action. It's not.",
					parsingException, ActionReader.class);
			}
			return Maybe.none();
		}
	}

	/**
	 * Read the {@link ApplicationAction} configuration from the given resource local to the given
	 * context class.
	 * 
	 * @param contextClass
	 *        The class locally to which the configuration is expected.
	 * @param resourceName
	 *        The name of the resource to read.
	 * @return The parsed {@link ApplicationAction} configuration.
	 * @throws ConfigurationException
	 *         If reading the configuration fails.
	 */
	public ApplicationAction readAction(Class<?> contextClass, String resourceName)
			throws ConfigurationException {
		return readAction(new ClassRelativeBinaryContent(contextClass, resourceName));
	}

	/**
	 * Read the {@link ApplicationAction} configuration from the given String.
	 * 
	 * @param actionXml
	 *        The string with the configuration.
	 * @return The parsed {@link ApplicationAction} configuration.
	 * @throws ConfigurationException
	 *         If reading the configuration fails.
	 */
	public ApplicationAction readAction(String actionXml) throws ConfigurationException {
		return readAction(CharacterContents.newContent(actionXml));
	}

	/**
	 * Read the {@link ApplicationAction} configuration from the given {@link File}.
	 * <p>
	 * Convenience shortcut for {@link #readAction(BinaryContent)}.
	 * </p>
	 */
	public ApplicationAction readAction(File file) {
		BinaryContent binaryContent = FileBasedBinaryContent.createBinaryContent(file);
		try {
			return ActionReader.INSTANCE.readAction(binaryContent);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read the {@link ApplicationAction} configuration from the given {@link CharacterContent}.
	 * 
	 * @return The parsed {@link ApplicationAction} configuration.
	 * @throws ConfigurationException
	 *         If reading the configuration fails.
	 */
	public ApplicationAction readAction(CharacterContent actionXml) throws ConfigurationException {
		BufferingProtocol protocol = new BufferingProtocol();
		protocol.setVerbosity(Protocol.DEBUG);
		DefaultInstantiationContext context = new DefaultInstantiationContext(protocol);
		try {
			return readAction(actionXml, context);
		} catch (ConfigurationException ex) {
			// Add collected error information.
			throw new ConfigurationException(protocol.getError(), ex);
		}
	}

	private ApplicationAction readAction(CharacterContent actionXml, InstantiationContext context)
			throws ConfigurationException {
		Map<String, ConfigurationDescriptor> globalDescriptors = getGlobalDescriptors();
		return (ApplicationAction) new ConfigurationReader(context, globalDescriptors).setSource(actionXml).read();
	}

	/**
	 * Read the {@link ApplicationAction} configuration from the given {@link BinaryContent}.
	 * 
	 * @return The parsed {@link ApplicationAction} configuration.
	 * @throws ConfigurationException
	 *         If reading the configuration fails.
	 */
	public ApplicationAction readAction(BinaryContent actionXml) throws ConfigurationException {
		BufferingProtocol protocol = new BufferingProtocol();
		protocol.setVerbosity(Protocol.DEBUG);
		DefaultInstantiationContext context = new DefaultInstantiationContext(protocol);
		try {
			return readAction(actionXml, context);
		} catch (ConfigurationException ex) {
			// Add collected error information.
			throw new ConfigurationException(protocol.getError(), ex);
		}
	}

	private ApplicationAction readAction(BinaryContent actionXml, InstantiationContext context)
			throws ConfigurationException {
		Map<String, ConfigurationDescriptor> globalDescriptors = getGlobalDescriptors();
		return (ApplicationAction) new ConfigurationReader(context, globalDescriptors).setSource(actionXml).read();
	}

	private Map<String, ConfigurationDescriptor> getGlobalDescriptors() {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ApplicationAction.class);
		return Collections.singletonMap("action", descriptor);
	}

}
