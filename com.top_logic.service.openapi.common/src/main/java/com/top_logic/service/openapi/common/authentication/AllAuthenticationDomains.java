/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.admin.component.TLServiceConfigEditorFormBuilder;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * All domains of {@link AuthenticationConfig} matching a given filter.
 * 
 * @implNote This is used as {@link Options option provider}. It uses both, a reference path to the
 *           authentications configuration to be informed about changes of the authentications, and
 *           {@link DeclarativeFormOptions} to have authentications when the
 *           {@link ConfigurationItem} using this options is newly created.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllAuthenticationDomains extends Function1<List<String>, Map<String, AuthenticationConfig>> {

	private DeclarativeFormOptions _options;

	/**
	 * Creates a new {@link AllAuthenticationDomains}.
	 */
	public AllAuthenticationDomains(DeclarativeFormOptions options) {
		_options = options;
	}

	@Override
	public List<String> apply(Map<String, AuthenticationConfig> authentications) {
		return resolveAuthentications(authentications).values().stream()
			.filter(filter())
			.map(AuthenticationConfig::getDomain)
			.collect(Collectors.toList());
	}

	private Map<String, AuthenticationConfig> resolveAuthentications(
			Map<String, AuthenticationConfig> authentications) {
		if (authentications != null) {
			return authentications;
		}
		TLServiceConfigEditorFormBuilder.EditModel formModel =
			(TLServiceConfigEditorFormBuilder.EditModel) _options.get(DeclarativeFormBuilder.FORM_MODEL);
		return ((AuthenticationConfigs) formModel.getServiceConfiguration()).getAuthentications();
	}

	/**
	 * Filter of {@link AuthenticationConfig}.
	 */
	protected Predicate<? super AuthenticationConfig> filter() {
		return FilterFactory.trueFilter();
	}

}
