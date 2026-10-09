/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AccessParentFunction} leading to the container of an object.
 *
 * @param composition
 *        The composition the container must hold the object through, navigated backwards;
 *        <code>null</code> for whichever composition holds it.
 * @param configured
 *        Whether the relation is configured for the type, in contrast to the {@link #DEFAULT} a
 *        composition part gets.
 */
public record ContainerRelation(TLReference composition, boolean configured) implements AccessParentFunction {

	/**
	 * The container, whichever composition holds the object, as a composition part gets it by
	 * default.
	 */
	public static final ContainerRelation DEFAULT = new ContainerRelation(null, false);

	/** The container, whichever composition holds the object, as configured for the type. */
	public static final ContainerRelation ANY = new ContainerRelation(null, true);

	@Override
	public TLObject resolve(TLObject object) {
		if (composition == null) {
			return object.tContainer();
		}
		TLReference via = object.tContainerReference();
		if (via == null || !via.getDefinition().equals(composition.getDefinition())) {
			return null;
		}
		return object.tContainer();
	}

	/**
	 * @return <code>null</code> without a {@link #composition()}: the possible containers are known
	 *         to the {@link SecurityConfigurationService}, which indexes the compositions.
	 */
	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		if (composition == null) {
			return null;
		}
		return Set.of(composition.getOwner());
	}

	@Override
	public ResKey getLabel() {
		if (composition != null) {
			return I18NConstants.ACCESS_PARENT_CONTAINER_VIA__COMPOSITION
				.fill(TLModelUtil.qualifiedName(composition));
		}
		return configured ? I18NConstants.ACCESS_PARENT_CONTAINER : I18NConstants.ACCESS_PARENT_CONTAINER_DEFAULT;
	}

}
