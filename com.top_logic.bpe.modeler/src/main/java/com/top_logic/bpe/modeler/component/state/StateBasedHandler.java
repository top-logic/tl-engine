/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.component.state;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * {@link CommandHandler} that updates state attributes on models based on configuration.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StateBasedHandler extends PreconditionCommandHandler {

	private TLStructuredType _modelType;

	TLStructuredTypePart _stateAttribute;

	TLClassifier _toState;

	private Set<TLClassifier> _fromStates;

	/**
	 * Configuration options for {@link StateBasedHandler}.
	 */
	public interface Config extends PreconditionCommandHandler.Config {
		/**
		 * The supported model type.
		 */
		@Name("modelType")
		String getModelType();

		/**
		 * Name of the state attribute in {@link #getModelType()}.
		 * 
		 * <p>
		 * The state attribute is expected to be of {@link TLEnumeration} type.
		 * </p>
		 */
		@Name("stateAttribute")
		String getStateAttribute();

		/**
		 * Names of state classifiers that activate this handler.
		 */
		@Name("fromStates")
		@Format(CommaSeparatedStrings.class)
		List<String> getFromStates();

		/**
		 * Name of the target state classifier that this handler sets into the
		 * {@link #getStateAttribute()}.
		 */
		@Name("toState")
		String getToState();
	}

	/**
	 * Creates a {@link StateBasedHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StateBasedHandler(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_modelType = (TLStructuredType) TLModelUtil.findType(config.getModelType());
		_stateAttribute = _modelType.getPartOrFail(config.getStateAttribute());
		TLEnumeration stateType = (TLEnumeration) _stateAttribute.getType();
		_fromStates = resolve(stateType, config.getFromStates());
		_toState = resolve(stateType, config.getToState());
	}

	private static TLClassifier resolve(TLEnumeration stateType, String name) throws ConfigurationException {
		TLClassifier result = stateType.getClassifier(name);
		if (result == null) {
			throw new ConfigurationException(I18NConstants.ERROR_NO_SUCH_CLASSIFIER__ENUM_NAME.fill(stateType, name),
				null, name);
		}
		return result;
	}

	private Set<TLClassifier> resolve(TLEnumeration stateType, List<String> stateNames) throws ConfigurationException {
		HashSet<TLClassifier> result = new HashSet<>();
		for (String name : stateNames) {
			result.add(resolve(stateType, name));
		}
		return result;
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof TLObject)) {
			return new Hide();
		}

		TLObject obj = (TLObject) model;
		if (!TLModelUtil.isCompatibleInstance(_modelType, obj)) {
			return new Hide();
		}

		Object oldState = obj.tValue(_stateAttribute);
		boolean collectionValued = AttributeOperations.isCollectionValued(_stateAttribute);
		if (collectionValued) {
			oldState = CollectionUtil.getSingleValueFrom(oldState);
		}
		if (!_fromStates.contains(oldState)) {
			return new Hide();
		}

		Object newState = collectionValued ? Collections.singleton(_toState) : _toState;

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				try (Transaction tx = obj.tKnowledgeBase().beginTransaction()) {
					updateState(obj, newState);

					tx.commit();
				}
			}

		};
	}

	/**
	 * The hook being called for updating the state.
	 *
	 * @param model
	 *        The model with the {@link Config#getStateAttribute()}.
	 * @param newState
	 *        The new state value to set.
	 */
	protected void updateState(TLObject model, Object newState) {
		model.tUpdate(_stateAttribute, newState);
	}
}
