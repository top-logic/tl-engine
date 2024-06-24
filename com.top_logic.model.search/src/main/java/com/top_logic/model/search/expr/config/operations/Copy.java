/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.util.model.ModelService;

/**
 * Deep object graph copy operation.
 * 
 * <p>
 * The operation takes up to three arguments:
 * </p>
 * 
 * <ul>
 * <li>A context object in which to allocate the top-level object(s) of the copied graph.</li>
 * <li>A filter function deciding whether to copy a specific value.</li>
 * <li>A constructor function allocating a copy of a specific original.</li>
 * </ul>
 * 
 * @see Copy.CopyFilter The filter function.
 * @see Copy.CopyConstructor The constructor function.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Copy extends GenericMethod implements WithFlatMapSemantics<Copy.Operation> {

	private static final NamedConstant COPY_OPERATION = new NamedConstant("copyOperation");

	/**
	 * Creates a {@link Copy} operation.
	 */
	protected Copy(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Copy(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		OperationImpl outerOperation = (OperationImpl) definitions.getVarOrNull(COPY_OPERATION);

		OperationImpl operation;
		if (outerOperation == null) {
			operation = new InitialOperation();
		} else {
			operation = new NestedOperation(outerOperation);
		}
		definitions.defineVar(COPY_OPERATION, operation);
		try {
			Object copyContext = arguments[1];
			Object filterArgument = arguments[2];
			Object constructor = arguments[3];
			Boolean transientCopy = asBooleanOrNull(arguments[4]);

			if (copyContext != null) {
				operation.setContext(asTLObject(copyContext), null);
			}

			if (filterArgument != null) {
				if (filterArgument instanceof Boolean) {
					// Interpret as constant function.
					if (!((Boolean) filterArgument).booleanValue()) {
						operation.setFilter((part, value, context) -> false);
					} else {
						// The true filter is default.
					}
				} else {
					SearchExpression filterExpr = asSearchExpression(filterArgument);
					operation.setFilter((part, value, context) -> {
						return asBoolean(filterExpr.eval(definitions, part, value, context));
					});
				}
			}

			if (constructor != null) {
				SearchExpression constructorExpr = asSearchExpression(constructor);
				operation.setConstructor((orig, reference, context) -> {
					return asTLObject(constructorExpr,
						constructorExpr.eval(definitions, orig, reference, context));
				});
			}

			operation.setTransient(transientCopy);

			Object result = evalPotentialFlatMap(definitions, arguments[0], operation);

			operation.finish();
			return result;
		} finally {
			if (outerOperation != null) {
				definitions.defineVar(COPY_OPERATION, outerOperation);
			} else {
				definitions.deleteVar(COPY_OPERATION);
			}
		}
	}

	private static Boolean asBooleanOrNull(Object object) {
		return object == null ? null : asBoolean(object);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, Operation operation) {
		if (singletonValue instanceof TLObject) {
			return operation.copy((TLObject) singletonValue);
		}
		return singletonValue;
	}

	/**
	 * Filter function deciding whether to copy a specific part.
	 */
	public interface CopyFilter {
		/**
		 * Whether to copy the given part.
		 *
		 * @param part
		 *        The {@link TLStructuredTypePart} being copied.
		 * @param value
		 *        The current value being copied.
		 * @param context
		 *        The context object defining the given part and holding the given value.
		 * @return Whether to copy the value.
		 */
		boolean accept(TLStructuredTypePart part, Object value, TLObject context);
	}

	/**
	 * Constructor function allocating a copy of a specific original object.
	 */
	public interface CopyConstructor {
		/**
		 * Allocates a copy of the given original.
		 *
		 * @param orig
		 *        The object to copy.
		 * @param reference
		 *        The reference in which the original is stored. <code>null</code> for a top-level
		 *        copy operation.
		 * @param context
		 *        The context object holding the original. <code>null</code> for a top-level copy
		 *        operation.
		 * @return A newly allocated uninitialized copy of the given original.
		 */
		TLObject allocate(TLObject orig, TLReference reference, TLObject context);
	}

	/**
	 * The actual implementation of the copy.
	 */
	public static abstract class Operation {

		/**
		 * Copies the given object.
		 */
		public abstract Object copy(TLObject orig);

	}

	static abstract class OperationImpl extends Operation implements CopyFilter, CopyConstructor {

		final TLFactory _factory = ModelService.getInstance().getFactory();

		private TLReference _contextRef;

		private TLObject _context;

		CopyFilter _filter = this;

		private CopyConstructor _constructor = this;

		private Boolean _transientCopy;

		public void setFilter(CopyFilter filter) {
			_filter = filter;
		}

		/**
		 * Whether to allocate transient objects by default (if no explicit constructor function is
		 * given).
		 */
		public void setTransient(Boolean transientCopy) {
			_transientCopy = transientCopy;
		}

		public void setConstructor(CopyConstructor constructor) {
			_constructor = constructor;
		}

		public final TLObject getContext() {
			return _context;
		}

		public TLObject setContext(TLObject object, TLReference contextRef) {
			setContextRef(contextRef);

			TLObject before = _context;
			_context = object;
			return before;
		}

		private TLReference getContextRef() {
			return _contextRef;
		}

		private void setContextRef(TLReference contextRef) {
			_contextRef = contextRef;
		}

		public abstract TLObject resolveCopy(TLObject orig);

		public abstract void enterCopy(TLObject orig, TLObject copy);

		protected abstract Set<Entry<TLObject, TLObject>> localCopies();

		@Override
		public Object copy(TLObject orig) {
			return createCopy(orig);
		}

		/**
		 * Last step of {@link #copy(TLObject)} after all nested copies have finished.
		 */
		public abstract void finish();

		@Override
		public boolean accept(TLStructuredTypePart part, Object value, TLObject context) {
			return true;
		}

		private Object createCopy(TLObject orig) {
			TLObject existing = resolveCopy(orig);
			if (existing != null) {
				return existing;
			}

			TLReference contextRef = getContextRef();
			TLObject context = getContext();
			TLObject copy = _constructor.allocate(orig, contextRef, context);
			if (copy == null && _constructor != this) {
				// Note: Returning null from a constructor function means to invoke the default
				// constructor. Suppressing a copy must be done in a filter expression.
				copy = allocate(orig, contextRef, context);
			}
			enterCopy(orig, copy);
			return copy;
		}

		@Override
		public TLObject allocate(TLObject orig, TLReference reference, TLObject context) {
			TLStructuredType type = orig.tType();
			if (type.getModelKind() != ModelKind.CLASS) {
				// Not an object, no copy.
				return orig;
			}

			TLStructuredType currentType = WrapperHistoryUtils.getCurrent(type);
			if (currentType == null) {
				// Type does no longer exist, keep historic reference.
				return orig;
			}

			TLClass classType = (TLClass) currentType;
			boolean copyTransient = _transientCopy == null ? orig.tTransient() : _transientCopy;
			if (copyTransient) {
				return TransientObjectFactory.INSTANCE.createObject(classType, context);
			} else {
				return _factory.createObject(classType, context);
			}
		}

		final void copyComposite(TLObject orig, TLReference reference, TLObject copy) {
			// TODO #28073: This check becomes unnecessary, after update.
			TLReference currentReference;
			if (WrapperHistoryUtils.isCurrent(reference)) {
				currentReference = reference;
			} else {
				currentReference = WrapperHistoryUtils.getCurrent(reference);
				if (currentReference == null) {
					// Reference does no longer exist.
					return;
				}
			}

			// Note: The target object may be of another type than the source object and not
			// define all properties of the source.
			if (!defines(copy, currentReference)) {
				return;
			}

			Object value = orig.tValue(reference);

			if (!_filter.accept(reference, value, orig)) {
				return;
			}

			Object valueCopy = copyValue(orig, reference, value);
			copy.tUpdate(currentReference, valueCopy);
		}

		private Object copyValue(TLObject orig, TLReference reference, Object value) {
			if (value instanceof Collection) {
				return copyCollection(orig, reference, (Collection<?>) value);
			} else if (value == null) {
				return null;
			} else {
				return copyObject(orig, reference, (TLObject) value);
			}
		}

		private Object copyCollection(TLObject orig, TLReference reference, Collection<?> collection) {
			Collection<Object> result = allocateCopy(collection);
			for (Object element : collection) {
				result.add(copyObject(orig, reference, (TLObject) element));
			}
			return result;
		}

		private Object copyObject(TLObject orig, TLReference reference, TLObject value) {
			TLReference refBefore = getContextRef();
			TLObject before = setContext(orig, reference);
			try {
				return createCopy(value);
			} finally {
				setContext(before, refBefore);
			}
		}

	}

	static class InitialOperation extends OperationImpl {

		private final Map<TLObject, TLObject> _copies = new HashMap<>();

		private final Map<TLStructuredType, CopyAttributes> _copyAttributes = new HashMap<>();

		private Map<TLReference, DescendBatch> _unresolvedCopies = new HashMap<>();

		private Map<TLStructuredType, ValueBatch> _valueBatches = new HashMap<>();

		private class CopyAttributes {

			private List<TLReference> _compositions = new ArrayList<>();

			private List<TLStructuredTypePart> _properties = new ArrayList<>();

			public List<TLReference> compositions() {
				return _compositions;
			}

			public List<TLStructuredTypePart> getProperties() {
				return _properties;
			}

			public void add(TLStructuredTypePart part) {
				if (part.getModelKind() == ModelKind.REFERENCE) {
					TLReference reference = (TLReference) part;
					if (reference.getEnd().isComposite()) {
						_compositions.add((TLReference) part);
						return;
					}
				}

				_properties.add(part);
			}
		}

		private class Batch {
			protected final List<TLObject> _origBatch = new ArrayList<>();

			protected final List<TLObject> _copyBatch = new ArrayList<>();

			public void add(TLObject orig, TLObject copy) {
				_origBatch.add(orig);
				_copyBatch.add(copy);
			}
		}

		private class DescendBatch extends Batch {
			private final TLReference _reference;

			/**
			 * Creates a {@link DescendBatch}.
			 */
			public DescendBatch(TLReference reference) {
				_reference = reference;
			}

			public void excuteBatch() {
				try (PreloadContext context = new PreloadContext()) {
					TLReference reference = _reference;
					MetaElementUtil.preloadAttribute(context, _origBatch, reference);

					for (int n = 0, cnt = _origBatch.size(); n < cnt; n++) {
						TLObject orig = _origBatch.get(n);
						TLObject copy = _copyBatch.get(n);

						copyComposite(orig, reference, copy);
					}
				}
			}
		}

		private class ValueBatch extends Batch {
			private final TLStructuredType _type;

			/**
			 * Creates a {@link DescendBatch}.
			 */
			public ValueBatch(TLStructuredType type) {
				_type = type;
			}

			public void excuteBatch() {
				List<TLStructuredTypePart> properties = copyAttributes(_type).getProperties();
				if (properties.isEmpty()) {
					return;
				}

				try (PreloadContext context = new PreloadContext()) {
					MetaElementUtil.preloadAttributes(context, _origBatch,
						properties.toArray(new TLStructuredTypePart[0]));

					for (int n = 0, cnt = _origBatch.size(); n < cnt; n++) {
						TLObject orig = _origBatch.get(n);
						TLObject copy = _copyBatch.get(n);

						copyValues(properties, orig, copy);
					}
				}
			}
		}

		@Override
		public TLObject resolveCopy(TLObject orig) {
			return _copies.get(orig);
		}

		@Override
		public void enterCopy(TLObject orig, TLObject copy) {
			_copies.put(orig, copy);

			TLStructuredType type = orig.tType();

			for (TLReference reference : copyAttributes(type).compositions()) {
				_unresolvedCopies.computeIfAbsent(reference, DescendBatch::new).add(orig, copy);
			}

			_valueBatches.computeIfAbsent(type, ValueBatch::new).add(orig, copy);
		}

		private CopyAttributes copyAttributes(TLStructuredType type) {
			CopyAttributes cache = _copyAttributes.get(type);
			if (cache == null) {
				cache = new CopyAttributes();
				for (TLStructuredTypePart part : type.getAllParts()) {
					if (part.isDerived()) {
						continue;
					}

					cache.add(part);
				}
				_copyAttributes.put(type, cache);
			}
			return cache;
		}

		@Override
		protected final Set<Entry<TLObject, TLObject>> localCopies() {
			return _copies.entrySet();
		}

		@Override
		public void finish() {
			// Copy along composition references in batch mode.
			while (true) {
				Map<TLReference, DescendBatch> batch = _unresolvedCopies;
				if (batch.isEmpty()) {
					break;
				}

				_unresolvedCopies = new HashMap<>();

				for (Entry<TLReference, DescendBatch> entry : batch.entrySet()) {
					entry.getValue().excuteBatch();
				}
			}

			for (ValueBatch valueBatch : _valueBatches.values()) {
				valueBatch.excuteBatch();
			}
		}

		private void copyValues(List<TLStructuredTypePart> parts, TLObject orig, TLObject copy) {
			for (TLStructuredTypePart part : parts) {
				TLStructuredTypePart currentPart;
				// TODO #28073: This check becomes unnecessary, after update.
				if (WrapperHistoryUtils.isCurrent(part)) {
					currentPart = part;
				} else {
					currentPart = WrapperHistoryUtils.getCurrent(part);
					if (currentPart == null) {
						// Property no longer exists.
						continue;
					}
				}

				// Note: The target object may be of another type than the source object and not
				// define all properties of the source.
				if (!defines(copy, currentPart)) {
					continue;
				}

				Object value = orig.tValue(part);
				if (!_filter.accept(part, value, orig)) {
					continue;
				}

				Object copyValue;
				if (part.getModelKind() == ModelKind.REFERENCE) {
					TLReference reference = (TLReference) part;
					copyValue = rewriteReferences(reference.getHistoryType(), value);
				} else {
					copyValue = value;
				}
				copy.tUpdate(currentPart, copyValue);
			}
		}

		private Object rewriteReferences(HistoryType historyType, Object value) {
			if (value instanceof Collection<?>) {
				return rewriteCollection(historyType, (Collection<?>) value);
			} else {
				return rewriteReference(historyType, (TLObject) value);
			}
		}

		private Object rewriteCollection(HistoryType historyType, Collection<?> value) {
			Collection<Object> result = allocateCopy(value);
			for (Object orig : value) {
				result.add(rewriteReference(historyType, (TLObject) orig));
			}
			return result;
		}

		private Object rewriteReference(HistoryType historyType, TLObject orig) {
			if (orig == null) {
				return null;
			}

			TLObject copy = resolveCopy(orig);
			if (copy == null) {
				switch (historyType) {
					case HISTORIC:
					case MIXED:
						return orig;

					case CURRENT:
						// When copying a stable version, current references must only contain
						// current values. If the target object is not copied (because it is not
						// part of the copy operation, it's current version must be used).
						return WrapperHistoryUtils.getCurrent(orig);
				}
				throw new UnreachableAssertion("No such history type: " + historyType);
			} else {
				return copy;
			}
		}
	}

	static class NestedOperation extends OperationImpl {

		private OperationImpl _outer;

		/**
		 * Creates a {@link NestedOperation}.
		 */
		public NestedOperation(OperationImpl outer) {
			_outer = outer;
		}

		@Override
		public TLObject resolveCopy(TLObject orig) {
			return _outer.resolveCopy(orig);
		}

		@Override
		public void enterCopy(TLObject orig, TLObject copy) {
			_outer.enterCopy(orig, copy);
		}

		@Override
		protected Set<Entry<TLObject, TLObject>> localCopies() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void finish() {
			// Is done by the outermost copy operation.
		}

	}

	static Collection<Object> allocateCopy(Collection<?> value) {
		if (value instanceof Set<?>) {
			return new LinkedHashSet<>();
		}
		return new ArrayList<>();
	}

	static boolean defines(TLObject object, TLStructuredTypePart part) {
		return part.getDefinition() == definition(object, part);
	}

	private static TLStructuredTypePart definition(TLObject object, TLStructuredTypePart part) {
		TLStructuredTypePart resolved = resolve(object, part);
		if (resolved == null) {
			return null;
		}
		return resolved.getDefinition();
	}

	private static TLStructuredTypePart resolve(TLObject object, TLStructuredTypePart part) {
		return object.tType().getPart(part.getName());
	}

	/**
	 * {@link MethodBuilder} creating {@link Copy}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Copy> {
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("orig")
			.optional("context")
			.optional("filter")
			.optional("constructor")
			.optional("transient")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Copy build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Copy(getConfig().getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}

}
