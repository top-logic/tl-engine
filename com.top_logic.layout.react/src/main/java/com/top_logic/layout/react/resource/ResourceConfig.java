/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;

/**
 * Common configuration of a single client-side resource of the React UI.
 *
 * <p>
 * Concrete kinds are {@link ModuleScriptConfig} and {@link StyleSheetConfig}. Every resource is
 * identified by its {@link #getName() name}, which is referenced by {@link #getRequires()} to
 * establish an emission order.
 * </p>
 */
@Abstract
public interface ResourceConfig extends NamedConfigMandatory {

	/** Configuration name for {@link #getRequires()}. */
	String REQUIRES = "requires";

	/** Configuration name for {@link #getBundle()}. */
	String BUNDLE = "bundle";

	/**
	 * {@link #getName() Names} of other resources that must be emitted before this resource.
	 *
	 * <p>
	 * Defines a partial order over all registered resources. {@link ClientResources} performs a
	 * topological sort so that, for stylesheets, the cascade order and, for module scripts, the
	 * evaluation order are deterministic.
	 * </p>
	 */
	@Name(REQUIRES)
	@Format(CommaSeparatedStrings.class)
	List<String> getRequires();

	/**
	 * Logical identifier of the bundle this resource is merged into when a production deployment is
	 * built.
	 *
	 * <p>
	 * Unused at runtime in the unbundled (development) mode. Consumed only by the optional
	 * production bundle tooling.
	 * </p>
	 */
	@Name(BUNDLE)
	String getBundle();

}
