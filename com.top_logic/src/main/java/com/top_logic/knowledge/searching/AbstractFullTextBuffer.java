/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLClassifier;

/**
 * Base functionality of a {@link FullTextBuBuffer}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFullTextBuffer implements FullTextBuBuffer {

	@Override
	public void genericAddValue(Object value) {
		if (value == null) {
			return;
		} else if (value instanceof String) {
			add((String) value);
		} else if (value instanceof TLClassifier) {
			add((TLClassifier) value);
		} else if (value instanceof Date) {
			add((Date) value);
		} else if (value instanceof Collection) {
			add((Collection<?>) value);
		} else if (value instanceof ResKey) {
			add((ResKey) value);
		} else if (value instanceof FullTextSearchable) {
			add((FullTextSearchable) value);
		} else {
			genericAddLabel(value);
		}
	}

	@Override
	public void genericAddLabel(Object value) {
		String label = MetaLabelProvider.INSTANCE.getLabel(value);
		if (label != null) {
			add(label);
		}
	}

	@Override
	public void add(FullTextSearchable searchable) {
		searchable.generateFullText(this);
	}

	@Override
	public void add(Collection<?> collection) {
		for (Object element : collection) {
			genericAddValue(element);
		}
	}

	@Override
	public void add(Date value) {
		List<Locale> locales = ResourcesModule.getInstance().getSupportedLocales();
		for (Locale locale : locales) {
			add(HTMLFormatter.getInstance(TimeZones.defaultUserTimeZone(), locale).formatDate(value));
		}
	}

	@Override
	public void add(ResKey key) {
		ResourcesModule resources = ResourcesModule.getInstance();
		for (Locale language : resources.getSupportedLocales()) {
			add(resources.getBundle(language).getString(key));
		}
	}

	@Override
	public void add(TLClassifier classifier) {
		add(FastListElementLabelProvider.labelKey(classifier));
	}

}
