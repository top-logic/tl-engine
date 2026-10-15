/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.logging.Level;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.xio.importer.binding.IdLookup;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link NestedImportHandler} resolving IDs within its content to objects that already exist
 * before the import.
 * 
 * <p>
 * Within the content of this handler, an ID that is not known to the import is looked up among the
 * {@link Config#getObjects() existing objects} by their {@link Config#getKey() key}. Such an ID
 * resolves references (e.g. {@link ObjectRef}, {@link ReferenceImport}), and an
 * {@link ObjectCreate} with {@link ObjectCreate.Config#getJoinDuplicates()} updates the existing
 * object instead of creating a new one.
 * </p>
 * 
 * <p>
 * When handlers of this kind are nested, the innermost one is asked first.
 * </p>
 *
 * @see ImportContext#withIdLookup(IdLookup, XMLStreamReader, Handler)
 */
public class ResolveExisting<C extends ResolveExisting.Config<?>> extends NestedImportHandler<C> {

	/**
	 * Configuration options for {@link ResolveExisting}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends ResolveExisting<?>> extends NestedImportHandler.Config<I> {

		/**
		 * Tag name of {@link ResolveExisting}.
		 */
		String TAG_NAME = "resolve-existing";

		/**
		 * @see #getObjects()
		 */
		String OBJECTS = "objects";

		/**
		 * @see #getKey()
		 */
		String KEY = "key";

		/**
		 * @see #getArgumentVar()
		 */
		String ARGUMENT_VAR = "argument-var";

		/**
		 * Expression computing the existing objects to resolve IDs against.
		 * 
		 * <p>
		 * The expression receives the value of the {@link #getArgumentVar() argument variable} as
		 * single argument. An expression that does not need it can ignore it, e.g.
		 * <code>all(`my.module:MyType`)</code>; one that needs it is a function, e.g.
		 * <code>scope -> $scope.get(`my.module:Container#items`)</code>.
		 * </p>
		 * 
		 * <p>
		 * The expression is evaluated only once per import, when an ID within the content of this
		 * handler is not found for the first time. The argument is the value of the
		 * {@link #getArgumentVar() argument variable} at the application of this handler in which
		 * this happens.
		 * </p>
		 */
		@Name(OBJECTS)
		@Mandatory
		Expr getObjects();

		/**
		 * Function computing the key of an existing object, against which the IDs of the import are
		 * matched.
		 * 
		 * <p>
		 * The function receives an object computed by {@link #getObjects()} as argument, e.g.
		 * <code>obj -> $obj.get(`my.module:MyType#extId`)</code>. A key that is not a string is
		 * converted to its string representation. An object with an empty key is not found. If
		 * multiple objects have the same key, the first one is used and a warning is reported.
		 * </p>
		 */
		@Name(KEY)
		@Mandatory
		Expr getKey();

		/**
		 * Variable to pass as argument to the {@link #getObjects()} expression.
		 */
		@Name(ARGUMENT_VAR)
		@StringDefault(ImportContext.THIS_VAR)
		@Nullable
		String getArgumentVar();

	}

	/**
	 * The index of existing objects by key, built once per import.
	 */
	@SuppressWarnings("unchecked")
	private final Property<Map<String, Object>> _index = TypedAnnotatable.propertyRaw(Map.class, Config.TAG_NAME);

	/**
	 * Creates a {@link ResolveExisting} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ResolveExisting(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		Object argument = context.getVarOrNull(getConfig().getArgumentVar());
		IdLookup lookup = (location, id) -> index(context, location, argument).get(id);
		return context.withIdLookup(lookup, in, this::importXmlInScope);
	}

	private Map<String, Object> index(ImportContext context, Location location, Object argument) {
		Map<String, Object> result = context.get(_index);
		if (result == null) {
			result = buildIndex(context, location, argument);
			context.set(_index, result);
		}
		return result;
	}

	/**
	 * @implNote Evaluates through the {@link com.top_logic.xio.importer.binding.ModelBinding} of the
	 *           import, so that the security setting of the import applies, see
	 *           {@link com.top_logic.xio.importer.binding.AbstractModelBinding#usesSecurity()}.
	 */
	private Map<String, Object> buildIndex(ImportContext context, Location location, Object argument) {
		Map<String, Object> result = new HashMap<>();
		for (Object obj : asCollection(context.eval(getConfig().getObjects(), argument))) {
			if (obj == null) {
				continue;
			}
			Object keyValue = context.eval(getConfig().getKey(), obj);
			if (keyValue == null) {
				continue;
			}
			String key = keyValue.toString();
			if (key.isEmpty()) {
				continue;
			}
			Object clash = result.putIfAbsent(key, obj);
			if (clash != null) {
				context.log(Level.WARN, location,
					I18NConstants.WARN_AMBIGUOUS_EXISTING_KEY__HANDLER_KEY_USED_IGNORED.fill(location(), key, clash,
						obj));
			}
		}
		return result;
	}

	private static Collection<?> asCollection(Object value) {
		if (value == null) {
			return Collections.emptyList();
		}
		if (value instanceof Collection<?> collection) {
			return collection;
		}
		return Collections.singletonList(value);
	}

}
