/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.utility.AbstractOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.util.Pointer;

/**
 * {@link Generator} that can be configured dynamically using {@link Expr search expressions} for
 * finding the options.
 * 
 * @see TLOptions
 * @see Config#getFunction()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT }, priority = 1000)
public class OptionsByExpression implements Generator, ConfiguredInstance<OptionsByExpression.Config<?>> {

	private final ScriptTracer _function;

	private final Config<?> _config;

	/**
	 * Configuration options for {@link OptionsByExpression}.
	 */
	@TagName("options-by-expression")
	public interface Config<I extends OptionsByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * The function that is executed with the object containing the property for which options
		 * are to be computed as single argument.
		 */
		Expr getFunction();

	}

	/**
	 * Creates a {@link OptionsByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OptionsByExpression(InstantiationContext context, Config<?> config) {
		_config = config;
		_function = ScriptTracer.compile(config.getFunction());
	}

	@Override
	public OptionModel<?> generate(EditContext editContext) {
		return new ScriptObservingOptionList(editContext.getOverlay(), editContext.getValueType());
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	private class ScriptObservingOptionList extends ScriptObservingOptions implements ListOptionModel<Object> {
		private List<?> _list;

		private Function<Object, Object> _normalizer;

		/**
		 * Creates a {@link ScriptObservingOptions}.
		 * 
		 * @param object
		 *        The base object that owns the attribute for which options are generated.
		 * @param valueType
		 *        The type of option values.
		 */
		private ScriptObservingOptionList(TLObject object, TLType valueType) {
			super(object);

			if (valueType.getModelKind() == ModelKind.DATATYPE) {
				StorageMapping<?> storageMapping = ((TLPrimitive) valueType).getStorageMapping();

				// Normalize value in a way as it has been retrieved from the persistency layer.
				// Without this code, attributes of type tl.core:Integer that have been edited in
				// the UI return de-facto values of type java.lang.Long. This is a problem, because
				// select fields try to remove the current selection from options in the pop-up
				// dialog.
				_normalizer = x -> storageMapping.getBusinessObject(storageMapping.getStorageObject(x));
			}
		}

		@Override
		public List<?> getBaseModel() {
			if (_list == null) {
				_list = createOptions();
			}
			return _list;
		}
	
		private List<?> createOptions() {
			List<?> list = SearchExpression.asList(_function.execute(this, getObject()));
			if (_normalizer != null) {
				return list.stream().map(_normalizer).collect(Collectors.toList());
			}
			return list;
		}

		@Override
		protected void reset() {
			_list = null;
		}
	}

	/**
	 * Base class for {@link OptionModel}s that observe {@link TLObject}s for change.
	 */
	abstract static class ScriptObservingOptions extends AbstractOptionModel<Object>
			implements Sink<Pointer>, ValueListener {

		private final TLObject _object;

		private List<FormField> _observed = new ArrayList<>();

		/**
		 * Creates a {@link ScriptObservingOptions}.
		 * 
		 * @param object
		 *        The base object that owns the attribute for which options are generated.
		 */
		public ScriptObservingOptions(TLObject object) {
			_object = object;
		}

		/**
		 * The base object that is the context for generating options.
		 */
		public TLObject getObject() {
			return _object;
		}

		@Override
		public void add(Pointer value) {
			TLObject obj = value.object();
			if (obj instanceof TLFormObject) {
				TLFormObject formObj = (TLFormObject) obj;

				AttributeUpdate update = formObj.getUpdate(value.attribute());
				if (update != null) {
					FormMember member = update.getField();
					if (member instanceof FormField) {
						FormField field = (FormField) member;

						boolean observing = field.addValueListener(this);
						if (observing) {
							_observed.add(field);
						}
					}
				}
			}
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			// Invalidate.
			detach();
			reset();
			notifyChanged();
		}

		/**
		 * Resets the generated options.
		 */
		protected abstract void reset();

		private void detach() {
			for (FormField observed : _observed) {
				observed.removeValueListener(this);
			}
			_observed.clear();
		}

	}

}
