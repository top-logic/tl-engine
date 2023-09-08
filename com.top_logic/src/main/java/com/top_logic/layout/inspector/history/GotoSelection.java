/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.history;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.ReferenceResolver;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.func.Identity;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandHandlerCommand;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.tool.execution.I18NConstants;

/**
 * {@link CommandHandlerCommand} switching the component model to the current selection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GotoSelection extends PreconditionCommandHandler {

	private Mapping<Object, Object> _selectionMapping;

	LayoutComponent _targetComponent;

	/**
	 * Configuration options for {@link GotoSelection}.
	 */
	public interface Config<I extends GotoSelection> extends AbstractCommandHandler.Config {
		/**
		 * {@link Mapping} to apply the the component's selection before using it as model.
		 */
		@ItemDefault(IdentityConfig.class)
		PolymorphicConfiguration<Mapping<Object, Object>> getSelectionMapping();

		/**
		 * Name of component to send the new model to.
		 */
		@Mandatory
		ComponentName getTargetComponent();

		/**
		 * Default value for {@link Config#getSelectionMapping()}.
		 */
		interface IdentityConfig extends PolymorphicConfiguration<Identity<?>> {
			// No additional properties.
		}
	}

	/**
	 * Creates a {@link GotoSelection} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GotoSelection(InstantiationContext context, Config<?> config) {
		super(context, config);

		_selectionMapping = context.getInstance(config.getSelectionMapping());
		context.resolveReference(config.getTargetComponent(), LayoutComponent.class, new ReferenceResolver<LayoutComponent>() {
			@Override
			public void setReference(LayoutComponent value) {
				_targetComponent = value;
			}
		});
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		Object selected = ((Selectable) component).getSelected();
		final Object newModel = _selectionMapping.map(selected);

		if (_targetComponent.supportsModel(newModel)) {
			return new Success() {
				@Override
				public void doExecute(DisplayContext context) {
					_targetComponent.setModel(newModel);
				}
			};
		} else {
			return new Failure(I18NConstants.ERROR_NO_MODEL);
		}
	}

}
