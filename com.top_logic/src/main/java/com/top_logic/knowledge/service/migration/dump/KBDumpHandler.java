/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.dump;

import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractDownloadHandler} delivering the database dump as download.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBDumpHandler extends AbstractCommandHandler {

	/**
	 * Configuration for {@link KBDumpHandler}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/** Configuration name for {@link #getExcludeTypes()} */
		String EXCLUDE_TYPES_NAME = "excludeTypes";

		/**
		 * The types to exclude.
		 * 
		 * <p>
		 * If this property is not set, the default exclude types are used.
		 * </p>
		 * 
		 * @return The types not to export.
		 */
		@Format(CommaSeparatedStringSet.class)
		@Name(EXCLUDE_TYPES_NAME)
		@FormattedDefault(SecurityStorage.TABLE_NAME)
		Set<String> getExcludeTypes();

	}

	private final Set<String> _excludeTypes;

	/**
	 * Creates a {@link KBDumpHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public KBDumpHandler(InstantiationContext context, Config config) {
		super(context, config);
		if (config.valueSet(config.descriptor().getProperty(Config.EXCLUDE_TYPES_NAME))) {
			_excludeTypes = config.getExcludeTypes();
		} else {
			_excludeTypes = null;
		}
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		return handleCommand(context, _excludeTypes);
	}

	/**
	 * Delivers a data dump with default exclude types for download.
	 */
	@CalledFromJSP
	public static HandlerResult handleCommand(DisplayContext aContext) {
		return handleCommand(aContext, KBDump.DEFAULT_EXCLUDE_TYPES);
	}

	/**
	 * Delivers a data dump for download.
	 * 
	 * @param excludeTypes
	 *        The names of the types and tables that must not be exported. May be <code>null</code>
	 *        which means the default exclude types.
	 */
	@CalledFromJSP
	public static HandlerResult handleCommand(DisplayContext aContext, Set<String> excludeTypes) {
		BinaryData data = KBDump.newDump(PersistencyLayer.getKnowledgeBase(), excludeTypes);
		aContext.getWindowScope().deliverContent(data);
		return HandlerResult.DEFAULT_RESULT;
	}

}
