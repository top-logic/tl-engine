/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.data;

import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;

/**
 * {@link InstanceResolver} that finds a {@link Currency} by its ISO code, for the demo's initial
 * data ({@code WEB-INF/data/events.objects.xml}).
 *
 * <p>
 * The currencies are created by the engine, so the data file can only refer to them. A TL-Script
 * resolver cannot do it: initial data is imported before any user is logged in, and TL-Script
 * applies the current user's read rights, which then see no currency at all.
 * </p>
 */
public class CurrencyResolver implements InstanceResolver {

	@Override
	public TLObject resolve(I18NLog log, Object context, String kind, String id) {
		return Currency.getCurrencyInstance(id);
	}

	@Override
	public String buildId(TLObject obj) {
		return ((Currency) obj).getName();
	}

}
