/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MOAlternativeBuilder;
import com.top_logic.dob.MetaObject;

/**
 * Default implementation of {@link MOAlternative} which is its own builder.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MOAlternativeImpl extends AbstractMetaObject implements MOAlternative, MOAlternativeBuilder {

	private volatile Set<MetaObject> specialisations = new HashSet<>();

	public MOAlternativeImpl(String name) {
		super(name);
	}

	@Override
	public Set<? extends MetaObject> getSpecialisations() {
		if (!isFrozen()) {
			return resolveSpecialisations(); 
		}
		return specialisations;
	}
	
	@Override
	protected void afterFreeze() {
		super.afterFreeze();
		specialisations = resolveSpecialisations();
	}

	private Set<MetaObject> resolveSpecialisations() {
		HashSet<MetaObject> resolvedSpecialisations = new HashSet<>();
		for (MetaObject special : specialisations) {
			switch (special.getKind()) {
				case alternative:
					MOAlternative alternative = (MOAlternative) special;
					resolvedSpecialisations.addAll(alternative.getSpecialisations());
					break;
				default:
					resolvedSpecialisations.add(special);
					break;
			}
			
		}
		return resolvedSpecialisations;
	}

	@Override
	public void registerSpecialisation(MetaObject specialisation) {
		checkUpdate();
		specialisations.add(specialisation);
	}

	@Override
	public MOAlternative createAlternative() {
		return this;
	}

	@Override
	public Kind getKind() {
		return Kind.alternative;
	}

	@Override
	public MetaObject copy() {
		MOAlternativeImpl copy = new MOAlternativeImpl(getName());
		copy.specialisations = CollectionUtil.newSet(specialisations.size());
		for (MetaObject specialisation : specialisations) {
			copy.specialisations.add(typeRef(specialisation));
		}
		return copy;
	}

	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		MetaObject[] deferredSpecialisations = specialisations.toArray(new MetaObject[specialisations.size()]);
		specialisations.clear();
		for (MetaObject deferredSpecialisation : deferredSpecialisations) {
			MetaObject specialisedType = context.getType(deferredSpecialisation.getName());
			specialisedType.resolve(context);
			specialisations.add(specialisedType);
		}
		return this;
	}
}

