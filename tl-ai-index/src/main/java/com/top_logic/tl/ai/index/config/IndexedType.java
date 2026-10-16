/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.index.text.Chunker;
import com.top_logic.tl.ai.index.text.TextValues;

/**
 * The compiled {@link IndexedTypeConfig} of one type: what of an object of the type goes into the
 * index.
 */
public final class IndexedType {

	/**
	 * One text of an object.
	 *
	 * @param name
	 *        The name stored with each chunk of the text.
	 * @param attribute
	 *        The attribute whose value is the text, or <code>null</code> for a script.
	 * @param script
	 *        The script deriving the text, or <code>null</code> for an attribute.
	 * @param reads
	 *        The attributes a reader must be allowed to read to see a chunk of this text.
	 */
	public record Part(String name, String attribute, QueryExecutor script, List<String> reads) {

		/**
		 * The text of this part for the given object.
		 */
		public String text(TLObject object, int maxChars) {
			Object value;
			if (attribute != null) {
				TLStructuredTypePart part = object.tType().getPart(attribute);
				value = part == null ? null : object.tValue(part);
			} else {
				value = script.execute(object);
			}
			return TextValues.text(value, maxChars);
		}

		/**
		 * The attributes of the given object a reader must be allowed to read to see a chunk of this
		 * text; <code>null</code> when one of them does not exist, so nobody may.
		 */
		public List<TLStructuredTypePart> readAttributes(TLObject object) {
			return attributes(object, reads);
		}
	}

	/**
	 * One keyword of an object.
	 *
	 * @param script
	 *        The script deriving the values.
	 * @param reads
	 *        The attributes a reader must be allowed to read to see a hit of a search filtered by
	 *        this keyword.
	 */
	private record Keyword(QueryExecutor script, List<String> reads) {
		// A plain carrier.
	}

	/**
	 * A chunk of an object before it has a vector.
	 *
	 * @param part
	 *        The text part the chunk is of.
	 * @param chunk
	 *        The chunk's text and place.
	 * @param embeddingText
	 *        What is embedded: the chunk's text, after the object's label when the type wants it.
	 */
	public record PendingChunk(String part, Chunker.Chunk chunk, String embeddingText) {
		// A plain carrier.
	}

	/**
	 * What the index holds of one object, before the vectors.
	 *
	 * @param types
	 *        The qualified names of the object's type and its super types.
	 * @param scopes
	 *        The identifiers of the objects that scope it.
	 * @param keywords
	 *        Its keyword values by keyword name.
	 * @param chunks
	 *        Its chunks, over all text parts, in configuration order.
	 */
	public record Content(List<String> types, List<String> scopes, Map<String, List<String>> keywords,
			List<PendingChunk> chunks) {
		// A plain carrier.
	}

	private final TLClass _type;

	private final List<Part> _parts;

	private final Map<String, Part> _partsByName;

	private final QueryExecutor _scope;

	private final Map<String, Keyword> _keywords;

	private final boolean _labelPrefix;

	/**
	 * Compiles the given configuration.
	 *
	 * @throws IllegalArgumentException
	 *         When the configuration names no class, an attribute the class does not have, or a part
	 *         twice.
	 */
	public IndexedType(IndexedTypeConfig config) {
		Object type = TLModelUtil.resolveQualifiedName(config.getName());
		if (!(type instanceof TLClass clazz)) {
			throw new IllegalArgumentException("'" + config.getName() + "' is no class; only objects of a class are indexed.");
		}
		_type = clazz;
		_parts = new ArrayList<>();
		_partsByName = new LinkedHashMap<>();
		for (IndexedTypeConfig.TextPart part : config.getTexts()) {
			Part compiled = compile(clazz, part);
			if (_partsByName.put(compiled.name(), compiled) != null) {
				throw new IllegalArgumentException("Type '" + config.getName() + "' names the text '" + compiled.name()
					+ "' twice.");
			}
			_parts.add(compiled);
		}
		_scope = config.getScope() == null ? null : unfiltered(config.getScope());
		_keywords = new LinkedHashMap<>();
		for (IndexedTypeConfig.Keyword keyword : config.getKeywords()) {
			if (keyword.getReads().isEmpty()) {
				throw new IllegalArgumentException("The keyword '" + keyword.getName() + "' of '" + config.getName()
					+ "' needs 'reads': the attributes its script reads.");
			}
			checkReads(clazz, keyword.getReads(), "keyword '" + keyword.getName() + "'");
			_keywords.put(keyword.getName(),
				new Keyword(unfiltered(keyword.getScript()), List.copyOf(keyword.getReads())));
		}
		_labelPrefix = config.getLabelPrefix();
	}

	private static Part compile(TLClass type, IndexedTypeConfig.TextPart part) {
		String attribute = part.getAttribute();
		if ((attribute == null) == (part.getScript() == null)) {
			throw new IllegalArgumentException("A text of '" + TLModelUtil.qualifiedName(type)
				+ "' needs either an attribute or a script.");
		}
		if (attribute != null) {
			if (type.getPart(attribute) == null) {
				throw new IllegalArgumentException("Type '" + TLModelUtil.qualifiedName(type) + "' has no attribute '"
					+ attribute + "'.");
			}
			String name = part.getPartName() == null ? attribute : part.getPartName();
			return new Part(name, attribute, null, List.of(attribute));
		}
		if (part.getPartName() == null) {
			throw new IllegalArgumentException("A script text of '" + TLModelUtil.qualifiedName(type)
				+ "' needs a name.");
		}
		checkReads(type, part.getReads(), "text '" + part.getPartName() + "'");
		return new Part(part.getPartName(), null, unfiltered(part.getScript()), List.copyOf(part.getReads()));
	}

	private static void checkReads(TLClass type, List<String> reads, String reader) {
		for (String read : reads) {
			if (type.getPart(read) == null) {
				throw new IllegalArgumentException("Type '" + TLModelUtil.qualifiedName(type) + "' has no attribute '"
					+ read + "' that the " + reader + " reads.");
			}
		}
	}

	/**
	 * The attributes of the given names of the object's type; <code>null</code> when one of them
	 * does not exist.
	 */
	private static List<TLStructuredTypePart> attributes(TLObject object, List<String> names) {
		List<TLStructuredTypePart> result = new ArrayList<>(names.size());
		TLStructuredType type = object.tType();
		for (String name : names) {
			TLStructuredTypePart part = type.getPart(name);
			if (part == null) {
				return null;
			}
			result.add(part);
		}
		return result;
	}

	/**
	 * A script whose result is not filtered by the rights of the thread that runs it.
	 *
	 * <p>
	 * The index holds every object of a configured type, whoever may read it; what a reader is shown
	 * is decided when the reader searches. A scope or keyword the writing thread could not read
	 * would otherwise go missing from the index, and a reader who may read it would not find the
	 * object.
	 * </p>
	 */
	private static QueryExecutor unfiltered(Expr script) {
		QueryExecutor executor = QueryExecutor.compile(script);
		executor.disableSecurity();
		return executor;
	}

	/**
	 * The configured class.
	 */
	public TLClass type() {
		return _type;
	}

	/**
	 * The texts, in configuration order.
	 */
	public List<Part> parts() {
		return _parts;
	}

	/**
	 * The text part of the given name, or <code>null</code>.
	 */
	public Part part(String name) {
		return _partsByName.get(name);
	}

	/**
	 * Whether the type has a scope rule.
	 */
	public boolean scoped() {
		return _scope != null;
	}

	/**
	 * The keyword names of the type.
	 */
	public Set<String> keywordNames() {
		return _keywords.keySet();
	}

	/**
	 * The attributes of the given object a reader must be allowed to read to see it in a search
	 * filtered by the given keyword; <code>null</code> when the type has no such keyword or one of
	 * them does not exist, so nobody may.
	 */
	public List<TLStructuredTypePart> keywordAttributes(String keyword, TLObject object) {
		Keyword compiled = _keywords.get(keyword);
		return compiled == null ? null : attributes(object, compiled.reads());
	}

	/**
	 * What the index holds of the given object.
	 *
	 * <p>
	 * Runs the type's scripts, so it must run where they may see the object - the index worker's
	 * system interaction.
	 * </p>
	 */
	public Content content(TLObject object, Chunker chunker, int maxTextChars) {
		String label = _labelPrefix ? MetaLabelProvider.INSTANCE.getLabel(object) : null;
		List<PendingChunk> chunks = new ArrayList<>();
		for (Part part : _parts) {
			for (Chunker.Chunk chunk : chunker.chunk(part.text(object, maxTextChars))) {
				String embedded = label == null || label.isBlank() ? chunk.text() : label + "\n\n" + chunk.text();
				chunks.add(new PendingChunk(part.name(), chunk, embedded));
			}
		}
		Map<String, List<String>> keywords = new LinkedHashMap<>();
		for (Map.Entry<String, Keyword> keyword : _keywords.entrySet()) {
			keywords.put(keyword.getKey(), keywordValues(keyword.getValue().script().execute(object)));
		}
		return new Content(typeNames(object), scopes(object), keywords, chunks);
	}

	/**
	 * The identifiers of the objects that scope the given object; empty without a scope rule.
	 */
	public List<String> scopes(TLObject object) {
		if (_scope == null) {
			return List.of();
		}
		Set<String> result = new LinkedHashSet<>();
		addScopes(result, _scope.execute(object));
		return List.copyOf(result);
	}

	private static void addScopes(Set<String> result, Object value) {
		if (value instanceof TLObject scope) {
			String id = ReadFilter.idOf(scope);
			if (id != null) {
				result.add(id);
			}
		} else if (value instanceof Collection<?> many) {
			for (Object element : many) {
				addScopes(result, element);
			}
		}
	}

	/**
	 * The qualified names of the object's type and of all its super types.
	 */
	public static List<String> typeNames(TLObject object) {
		List<String> result = new ArrayList<>();
		if (object.tType() instanceof TLClass clazz) {
			for (TLClass type : TLModelUtil.getReflexiveTransitiveGeneralizations(clazz)) {
				result.add(TLModelUtil.qualifiedName(type));
			}
		}
		return result;
	}

	/**
	 * The keyword values of a script result: an object by its identifier, an enumeration value by
	 * its name, anything else by its text.
	 */
	public static List<String> keywordValues(Object value) {
		Set<String> result = new LinkedHashSet<>();
		addKeywords(result, value);
		return List.copyOf(result);
	}

	private static void addKeywords(Set<String> result, Object value) {
		if (value == null) {
			return;
		}
		if (value instanceof Collection<?> many) {
			for (Object element : many) {
				addKeywords(result, element);
			}
		} else if (value instanceof TLClassifier classifier) {
			result.add(classifier.getName());
		} else if (value instanceof TLObject object) {
			String id = ReadFilter.idOf(object);
			if (id != null) {
				result.add(id);
			}
		} else {
			result.add(String.valueOf(value));
		}
	}

}
