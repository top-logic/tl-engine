/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel;

import static com.top_logic.basic.StringServices.*;

import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;

/**
 * Knows all {@link ExcelActionOp}s that can be used.
 * <p>
 * If there are {@link ExcelActionOp}s that are not known to this class, they can not be used for
 * excel tests.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ExcelActionRegistry {

	/** Configuration for the {@link ExcelActionRegistry}. */
	public interface Config extends ConfigurationItem {

		/** The registrations of excel actions, grouped by business objects. */
		@Key(ExcelBusinessObject.NAME_ATTRIBUTE)
		@EntryTag("object-type")
		Map<String, ExcelBusinessObject> getRegistrations();

	}

	/** The registrations of excel actions for this business object. */
	public interface ExcelBusinessObject extends NamedConfiguration {

		/** The name of the business object whose excel action registration this is. */
		@Override
		String getName();

		/** The actions for this business object. */
		@Key(ExcelBusinessObject.NAME_ATTRIBUTE)
		Map<String, ExcelBusinessAction> getActions();

	}

	/** The registration of one excel action. */
	public interface ExcelBusinessAction extends NamedConfiguration {

		/** The name of the business action whose excel action registration this is. */
		@Override
		String getName();

		/** The implementation of the excel action. */
		Class<? extends ExcelActionOp<?>> getImplementation();

	}

	/** Creates and returns the {@link Config}. */
	public static Config getExcelActionRegistry() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Returns the registered {@link ExcelActionOp} for the given business action and business
	 * object name.
	 * 
	 * @return <code>null</code>, if there is no {@link ExcelActionOp} registered.
	 */
	public static Class<? extends ExcelActionOp<?>> getExcelActionOpClass(
			Config registry, String businessAction, String businessObject) {
		ExcelBusinessObject actionsForObject = registry.getRegistrations().get(nonNull(businessObject));
		if (actionsForObject == null) {
			return null;
		}
		ExcelBusinessAction actionRegistration = actionsForObject.getActions().get(nonNull(businessAction));
		if (actionRegistration == null) {
			return null;
		}
		return actionRegistration.getImplementation();
	}

}
