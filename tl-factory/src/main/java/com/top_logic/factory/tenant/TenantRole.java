/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.tenant;

import com.top_logic.factory.project.FactoryModel;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.util.TLModelUtil;

/**
 * The role of a member in a tenant: the reference of the tenant holding the member.
 *
 * <p>
 * Mirrors the enumeration {@link #ENUM} of the model module {@link FactoryModel#MODULE}; the name
 * of a constant is the name of its classifier.
 * </p>
 */
public enum TenantRole {

	/** Manages the members of the tenant and works on its projects. */
	ADMINISTRATOR(FactoryModel.TENANT_ADMINISTRATORS),

	/** Works on the projects of the tenant. */
	DEVELOPER(FactoryModel.TENANT_DEVELOPERS),

	/** Reads the projects of the tenant. */
	READER(FactoryModel.TENANT_READERS);

	/** Name of the enumeration of the roles in the model module {@link FactoryModel#MODULE}. */
	public static final String ENUM = "TenantRole";

	private final String _reference;

	private TenantRole(String reference) {
		_reference = reference;
	}

	/**
	 * The attribute of a tenant holding the members of this role, one of
	 * {@link FactoryModel#TENANT_MEMBERS}.
	 */
	public String getReference() {
		return _reference;
	}

	/**
	 * The classifier of this role in the model.
	 */
	public TLClassifier classifier() {
		TLEnumeration enumeration = (TLEnumeration) TLModelUtil.findType(FactoryModel.MODULE, ENUM);
		return enumeration.getClassifier(name());
	}

	/**
	 * The role of the given classifier of the enumeration {@link #ENUM}.
	 *
	 * @param classifier
	 *        The classifier, {@code null} for none.
	 * @return The role, {@code null} for no classifier.
	 */
	public static TenantRole fromClassifier(TLClassifier classifier) {
		return classifier == null ? null : valueOf(classifier.getName());
	}

}
