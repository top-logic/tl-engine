/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link InstanceResolver} that builds in index of all instances of a certain type to resolve
 * objects by an identifying attribute.
 */
public class InstanceResolverByIndex implements InstanceResolver {

	private TLClass _type;

	private TLStructuredTypePart _part;

	private TLPrimitive _idType;

	private Map<Object, TLObject> _index;

	/**
	 * Creates a {@link InstanceResolverByIndex} from configuration.
	 * 
	 * @param part
	 *        The identifying primitive attribute.
	 */
	@CalledByReflection
	public InstanceResolverByIndex(TLStructuredTypePart part) {
		_part = part;
		_idType = (TLPrimitive) _part.getType();
	}

	@Override
	public void initType(TLStructuredType type) {
		_type = (TLClass) type;
	}

	@Override
	public String buildId(TLObject obj) {
		return XMLInstanceExporter.serialize(_idType, obj.tValue(_part));
	}

	@Override
	public TLObject resolve(I18NLog log, Object context, String kind, String id) {
		Object value = XMLInstanceImporter.parse(log, _idType, id);

		if (_index == null) {
			_index = new HashMap<>();

			Set<Object> clashes = null;
			for (TLObject obj : MetaElementUtil.getAllInstancesOf(_type, TLObject.class)) {
				Object key = obj.tValue(_part);
				TLObject clash = _index.put(key, obj);
				if (clash != null) {
					log.log(Level.WARN, I18NConstants.NON_UNIQUE_KEY__TYPE_ATTR_KEY_OBJ1_OBJ2.fill(
						TLModelUtil.qualifiedName(_type),
						TLModelUtil.qualifiedName(_part),
						MetaLabelProvider.INSTANCE.getLabel(key),
						MetaLabelProvider.INSTANCE.getLabel(obj),
						MetaLabelProvider.INSTANCE.getLabel(clash)));

					if (clashes == null) {
						clashes = new HashSet<>();
					}
					clashes.add(key);
				}
			}

			if (clashes != null) {
				// Do not provide random data.
				_index.keySet().removeAll(clashes);
			}
		}

		return _index.get(value);
	}
}
