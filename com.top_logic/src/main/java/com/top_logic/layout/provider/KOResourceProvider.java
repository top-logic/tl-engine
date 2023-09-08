/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;

/**
 * {@link ResourceProvider} for {@link KnowledgeObject} that actually displays the {@link Wrapper}
 * implementation.
 * 
 * @see KOLabelProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KOResourceProvider implements ResourceProvider {

	/** Singleton {@link KOResourceProvider} instance. */
	public static final KOResourceProvider INSTANCE = new KOResourceProvider();

	private KOResourceProvider() {
		// singleton instance
	}

	private ResourceProvider impl(Object object, Object orig) {
		if (object == orig) {
			// avoid recursive call. This may happen because some KO's are their own wrapper.
			return WrapperResourceProvider.INSTANCE;
		}
		return MetaResourceProvider.INSTANCE;
	}

	private Object toWrapper(Object object) {
		if (object == null) {
			return object;
		}
		return WrapperFactory.getWrapper((KnowledgeObject) object);
	}

	@Override
	public String getLabel(Object object) {
		Object mapped = toWrapper(object);
		return impl(mapped, object).getLabel(mapped);
	}

	@Override
	public String getType(Object anObject) {
		Object mapped = toWrapper(anObject);
		return impl(mapped, anObject).getType(mapped);
	}

	@Override
	public String getTooltip(Object anObject) {
		Object mapped = toWrapper(anObject);
		return impl(mapped, anObject).getTooltip(mapped);
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		Object mapped = toWrapper(anObject);
		return impl(mapped, anObject).getImage(mapped, aFlavor);
	}

	@Override
	public String getLink(DisplayContext context, Object anObject) {
		Object mapped = toWrapper(anObject);
		return impl(mapped, anObject).getLink(context, mapped);
	}

	@Override
	public String getCssClass(Object anObject) {
		Object mapped = toWrapper(anObject);
		return impl(mapped, anObject).getCssClass(mapped);
	}

}

