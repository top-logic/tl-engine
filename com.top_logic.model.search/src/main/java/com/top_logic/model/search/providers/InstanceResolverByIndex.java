/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.io.StringReader;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link InstanceResolver} that builds in index of all instances of a certain type to resolve
 * objects by an identifying attribute.
 */
public class InstanceResolverByIndex implements InstanceResolver {

	private TLClass _type;

	private TLStructuredTypePart _part;

	private TLPrimitive _idType;

	private QueryExecutor _indexBuilder;

	private Map<Object, TLObject> _index;

	/**
	 * Creates a {@link InstanceResolverByIndex} from configuration.
	 * 
	 * @param type
	 *        The type to load all instances from.
	 * @param part
	 *        The identifying primitive attribute.
	 */
	@CalledByReflection
	public InstanceResolverByIndex(TLClass type, TLStructuredTypePart part) throws ParseException {
		_type = type;
		_part = part;
		_idType = (TLPrimitive) _part.getType();

		_indexBuilder = QueryExecutor
			.compile(
				new SearchExpressionParser(new StringReader("t -> attr -> all($t).indexBy(x -> $x.get($attr))"))
					.expr());
	}

	@Override
	public String buildId(TLObject obj) {
		return XMLInstanceExporter.serialize(_idType, obj.tValue(_part));
	}

	@Override
	public TLObject resolve(Log log, String kind, String id) {
		Object value = XMLInstanceImporter.parse(log, _idType, id);

		if (_index == null) {
			@SuppressWarnings("unchecked")
			Map<Object, TLObject> index = (Map<Object, TLObject>) _indexBuilder.execute(_type, _part);

			_index = index;
		}

		return _index.get(value);
	}
}
