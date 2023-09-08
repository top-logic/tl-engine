/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.version;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.version.model.Organisation;
import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * {@link Accessor} delivering the raw name of an organization of a dependency.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OrganizationAccessor extends ReadOnlyAccessor<VersionInfo> {

	/**
	 * Singleton {@link OrganizationAccessor} instance.
	 */
	public static final OrganizationAccessor INSTANCE = new OrganizationAccessor();

	private OrganizationAccessor() {
		// Singleton constructor.
	}

	@Override
	public Object getValue(VersionInfo object, String property) {
		Organisation organization = object.getOrganization();
		String name = organization == null ? null : organization.getName();
		if (name == null) {
			// Group ID is (should be) a reversed domain name.
			List<String> parts = Arrays.asList(object.getGroupId().split("\\."));
			Collections.reverse(parts);
			name = parts.stream().collect(Collectors.joining("."));
		}
		return name;
	}

}
