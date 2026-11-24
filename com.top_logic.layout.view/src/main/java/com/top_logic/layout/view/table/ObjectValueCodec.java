/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLObject;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.table.filter.BoundCodec;

/**
 * {@link BoundCodec} of the values of a column showing its values by their label - business
 * objects and texts - as the options of a list of values.
 *
 * <p>
 * A value is written as its label. A business object is stored by its identity, the name of its
 * table ({@link #TYPE}) and its identifier ({@link #ID}), and read back from the knowledge base; an
 * object that no longer exists is read as {@code null}. A value of a datatype is stored in the form
 * its {@link StorageMapping} stores it in the database, where that is a text (e.g. a color as its
 * hexadecimal code, an icon as its name), and a text without a storage mapping as itself. Any other
 * value has no persistent form. A value is not entered as text, so {@link #parse(String)} reads
 * none.
 * </p>
 */
final class ObjectValueCodec implements BoundCodec<Object> {

	/** JSON key of the name of the table of a stored business object. */
	static final String TYPE = "type";

	/** JSON key of the identifier of a stored business object. */
	static final String ID = "id";

	private final Function<Object, String> _label;

	private final StorageMapping<?> _mapping;

	/**
	 * Creates an {@link ObjectValueCodec}.
	 *
	 * @param label
	 *        The label a value is written as.
	 * @param mapping
	 *        The storage mapping of the datatype of the values, {@code null} for values of no
	 *        datatype (e.g. business objects).
	 */
	ObjectValueCodec(Function<Object, String> label, StorageMapping<?> mapping) {
		_label = label;
		_mapping = mapping;
	}

	@Override
	public String format(Object value) {
		return value == null ? null : _label.apply(value);
	}

	@Override
	public Object parse(String text) {
		return null;
	}

	@Override
	public Object toJson(Object value) {
		if (value instanceof TLObject object && object.tHandle() != null) {
			Map<String, Object> json = new LinkedHashMap<>();
			json.put(TYPE, object.tTable().getName());
			json.put(ID, IdentifierUtil.toExternalForm(object.tIdLocal()));
			return json;
		}
		if (_mapping != null) {
			return storedText(value);
		}
		if (value instanceof String) {
			return value;
		}
		return null;
	}

	/**
	 * The text the {@link StorageMapping} stores the given value as, {@code null} if it stores it
	 * otherwise or cannot store it.
	 */
	private Object storedText(Object value) {
		try {
			if (!_mapping.isCompatible(value)) {
				return null;
			}
			Object stored = _mapping.getStorageObject(value);
			return stored instanceof String ? stored : null;
		} catch (RuntimeException ex) {
			// A value the mapping cannot store has no persistent form.
			return null;
		}
	}

	@Override
	public Object fromJson(Object json) {
		if (json instanceof String text) {
			if (_mapping == null) {
				return text;
			}
			try {
				return _mapping.getBusinessObject(text);
			} catch (RuntimeException ex) {
				// A text the mapping cannot read (any longer) is no value.
				return null;
			}
		}
		if (!(json instanceof Map<?, ?> map)) {
			return null;
		}
		Object type = map.get(TYPE);
		Object id = map.get(ID);
		if (type == null || id == null) {
			return null;
		}
		try {
			TLID identifier = IdentifierUtil.fromExternalForm(id.toString());
			KnowledgeObject item = PersistencyLayer.getKnowledgeBase().getKnowledgeObject(type.toString(), identifier);
			return item == null || !item.isAlive() ? null : item.getWrapper();
		} catch (RuntimeException ex) {
			// A table or an identifier that is not (any longer) known identifies no object.
			return null;
		}
	}

}
