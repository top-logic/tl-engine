/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Set;

import com.top_logic.basic.col.Mapping;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.IGroup;

/**
 * {@link SecurityProvider} for an attribute with the given name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityProviderImpl implements SecurityProvider {

	private final String _partName;

	private final Mapping<Object, ? extends TLObject> _modelMapping;

	/**
	 * Creates a {@link SecurityProviderImpl}.
	 * 
	 * @param partName
	 *        The name of the model part that protects the values.
	 * @param modelMapping
	 *        {@link Mapping} that returns the business objects for the input objects.
	 */
	public SecurityProviderImpl(String partName, Mapping<Object, ? extends TLObject> modelMapping) {
		_partName = partName;
		_modelMapping = modelMapping;
	}

	@Override
	public boolean isRestricted(Object object, String property) {
		AccessChecker accessAware = getAccessChecker(toTLObject(object), property);
		return accessAware != LiberalAccessChecker.INSTANCE;
	}

	private AccessChecker getAccessChecker(TLObject object, String property) {
		if (!_partName.equals(property) || object == null) {
			return LiberalAccessChecker.INSTANCE;
		}
		TLType type = object.tType();
		TLStructuredTypePart part;
		if (type instanceof TLClass) {
			part = ((TLClass) type).getPart(_partName);
		} else {
			return LiberalAccessChecker.INSTANCE;
		}
		if (!TLModelUtil.isAccessAware(part)) {
			return LiberalAccessChecker.INSTANCE;
		}
		return TLModelUtil.getAccessAware(part);
	}

	private TLObject toTLObject(Object object) {
		return _modelMapping.map(object);
	}

	@Override
	public Set<BoundCommandGroup> getAccessRights(Object object, String property, IGroup group) {
		TLObject tlObject = toTLObject(object);
		return getAccessChecker(tlObject, property).getAccessRights(tlObject, group);
	}

	@Override
	public boolean hasAccessRight(Object object, String property, IGroup group, BoundCommandGroup accessRight) {
		TLObject tlObject = toTLObject(object);
		return getAccessChecker(tlObject, property).hasAccessRight(tlObject, group, accessRight);
	}

}

