/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;

/**
 * {@link InstanceResolver} that resolves instances by their {@code name} attribute.
 *
 * <p>
 * During export, the {@code name} value is used as the global identifier. During import, existing
 * instances with the same name are deleted and re-created from the XML, making the
 * {@code objects.xml} the authoritative source of truth.
 * </p>
 */
public class NameBasedInstanceResolver implements InstanceResolver {

	/**
	 * Configuration for {@link NameBasedInstanceResolver}.
	 */
	public interface Config extends PolymorphicConfiguration<NameBasedInstanceResolver> {
		// No additional properties — always resolves by "name".
	}

	/** Creates a new {@link NameBasedInstanceResolver} from configuration. */
	public NameBasedInstanceResolver(InstantiationContext context, Config config) {
		// Nothing to configure.
	}

	/** Creates a new {@link NameBasedInstanceResolver}. */
	public NameBasedInstanceResolver() {
		// Nothing to configure.
	}

	@Override
	public String buildId(TLObject object) {
		return (String) object.tValueByName("name");
	}

	@Override
	public TLObject resolve(I18NLog log, Object context, String typeName, String globalId) {
		String[] parts = typeName.split(":");
		TLClass type = MetaElementUtil.getGlobalType(parts[0], parts[1]);
		for (TLObject instance : MetaElementUtil.getAllInstancesOf(type, TLObject.class)) {
			// An earlier resolve() call in the same import transaction may already have deleted an
			// instance. getAllInstancesOf still returns such locally-deleted objects until commit,
			// and reading their "name" would throw DeletedObjectAccess — so skip them.
			if (!instance.tValid()) {
				continue;
			}
			if (globalId.equals(instance.tValueByName("name"))) {
				instance.tDelete();
				return null;
			}
		}
		return null;
	}
}
