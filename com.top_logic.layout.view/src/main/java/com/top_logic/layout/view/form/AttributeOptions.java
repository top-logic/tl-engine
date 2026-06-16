/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.col.Sink;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.ListOptionProvider;
import com.top_logic.element.meta.SimpleEditContext;
import com.top_logic.element.meta.complex.CountryOptionProvider;
import com.top_logic.element.meta.complex.LanguageOptionProvider;
import com.top_logic.element.meta.complex.TimeZoneOptionProvider;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.element.meta.kbbased.storage.mappings.JavaEnumMapping;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.search.providers.OptionsByExpression;
import com.top_logic.model.util.Pointer;
import com.top_logic.model.util.TLModelUtil;

/**
 * Resolves the selectable options for a model attribute in the React view form layer.
 *
 * <p>
 * Unlike the legacy form framework, option resolution here is fully decoupled from
 * {@code AttributeFormContext}/{@code AttributeUpdateContainer}/{@code TLFormObject}:
 * </p>
 * <ul>
 * <li>A TL-Script options annotation ({@link OptionsByExpression}) is evaluated <em>natively</em>
 * through a {@link ScriptTracer} against the current (possibly edited) object, reporting the
 * attributes it reads to a {@link Sink} so the caller can recompute options when a dependency
 * changes.</li>
 * <li>All other generators, enumerations, references and option-bearing datatypes are resolved via
 * the platform model metadata.</li>
 * </ul>
 *
 * @see AttributeSelectFieldModel
 */
public class AttributeOptions {

	/**
	 * {@link OverlayLookup} used in display mode, where no editing overlays exist.
	 */
	public static final OverlayLookup NO_OVERLAYS = new OverlayLookup() {
		@Override
		public TLObject getExistingOverlay(TLObject object) {
			return null;
		}

		@Override
		public Iterable<? extends TLObject> getOverlays() {
			return Collections.emptyList();
		}
	};

	/**
	 * Option sources for the well-known core datatypes that have a finite value range but no
	 * per-attribute options annotation.
	 */
	private static final Map<String, ListOptionProvider> DATATYPE_OPTIONS = Map.of(
		"tl.util:Country", CountryOptionProvider.INSTANCE,
		"tl.util:Language", LanguageOptionProvider.INSTANCE,
		"tl.util:TimeZone", new TimeZoneOptionProvider());

	/**
	 * Compiled options expressions, keyed by the attribute they belong to.
	 */
	private static final ConcurrentHashMap<TLStructuredTypePart, ScriptTracer> TRACERS =
		new ConcurrentHashMap<>();

	private AttributeOptions() {
		// Utility class.
	}

	/**
	 * Whether the given attribute should be edited through a select control backed by a defined set
	 * of options.
	 */
	public static boolean isSelect(TLStructuredTypePart part) {
		if (isComposition(part)) {
			// Compositions are edited through a (composition) table, not a select.
			return false;
		}
		if (AttributeOperations.getOptions(part) != null) {
			return true;
		}
		TLType type = part.getType();
		if (type instanceof TLEnumeration || type instanceof TLClass) {
			return true;
		}
		if (type instanceof TLPrimitive) {
			return isEnumDatatype((TLPrimitive) type) || DATATYPE_OPTIONS.containsKey(TLModelUtil.qualifiedName(type));
		}
		return false;
	}

	/**
	 * Computes the option list for the given attribute in the context of the given object.
	 *
	 * @param self
	 *        The object owning the attribute (an editing overlay in edit mode, the persistent object
	 *        in display mode). Option expressions are evaluated against this object, so they see the
	 *        current (edited) values of other fields.
	 * @param part
	 *        The attribute whose options to compute.
	 * @param overlays
	 *        Lookup for resolving the editing overlays of other in-form objects referenced by an
	 *        option expression, or {@code null} for display mode.
	 * @param dependencies
	 *        Receives the attributes read while evaluating an option expression, so the caller can
	 *        recompute options when one of them changes.
	 * @return The available options.
	 */
	public static List<?> optionsFor(TLObject self, TLStructuredTypePart part, OverlayLookup overlays,
			Sink<Pointer> dependencies) {
		Generator generator = AttributeOperations.getOptions(part);
		if (generator instanceof OptionsByExpression) {
			Expr function = ((OptionsByExpression) generator).getConfig().getFunction();
			ScriptTracer tracer = TRACERS.computeIfAbsent(part, x -> ScriptTracer.compile(function));
			OverlayLookup lookup = overlays != null ? overlays : NO_OVERLAYS;
			return SearchExpression.asList(tracer.execute(dependencies, lookup, self));
		}
		if (generator != null) {
			// A non-expression generator (e.g. supported locales) - no form-state dependency.
			return toList(generator.generate(SimpleEditContext.createContext(self, part)));
		}
		TLType type = part.getType();
		if (type instanceof TLPrimitive) {
			TLPrimitive primitive = (TLPrimitive) type;
			if (isEnumDatatype(primitive)) {
				return enumOptions(primitive);
			}
			ListOptionProvider datatypeOptions = DATATYPE_OPTIONS.get(TLModelUtil.qualifiedName(type));
			if (datatypeOptions != null) {
				return datatypeOptions.getOptionsList(SimpleEditContext.createContext(self, part));
			}
		}
		// Enumeration (classifiers) or reference (all instances).
		return toList(AttributeOperations.allOptions(SimpleEditContext.createContext(self, part)));
	}

	private static boolean isComposition(TLStructuredTypePart part) {
		return part instanceof TLReference && ((TLReference) part).isComposite();
	}

	private static boolean isEnumDatatype(TLPrimitive primitive) {
		return primitive.getStorageMapping() instanceof JavaEnumMapping;
	}

	private static List<?> enumOptions(TLPrimitive primitive) {
		JavaEnumMapping<?, ?> mapping = (JavaEnumMapping<?, ?>) primitive.getStorageMapping();
		return Arrays.asList(mapping.getConfig().getEnum().getEnumConstants());
	}

	private static List<?> toList(OptionModel<?> model) {
		if (model instanceof ListOptionModel) {
			return new ArrayList<>(((ListOptionModel<?>) model).getBaseModel());
		}
		List<Object> result = new ArrayList<>();
		for (Object option : model) {
			result.add(option);
		}
		return result;
	}

	/**
	 * Whether the given value is a non-{@code null} {@link Collection}.
	 */
	static boolean isCollection(Object value) {
		return value instanceof Collection;
	}

}
