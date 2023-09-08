/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * Navigation history with {@link Back} and {@link Forward} commands.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class History {

	private static final Property<History> HISTORY = TypedAnnotatable.property(History.class, "modelHistory");

	private static final int MAX_HINSTORY = 15;

	final LayoutComponent _component;

	int _index;

	List<Object> _models = new ArrayList<>();

	boolean _stepping;

	/**
	 * Creates a {@link History}.
	 *
	 * @see #add(LayoutComponent, Object)
	 */
	private History(LayoutComponent component) {
		_component = component;
	}

	/**
	 * Inserts the given new model into the history.
	 */
	public static void add(LayoutComponent component, Object newModel) {
		history(component).add(newModel);
	}

	static History history(LayoutComponent component) {
		History result = component.get(HISTORY);
		if (result == null) {
			result = new History(component);
			component.set(HISTORY, result);
		}
		return result;
	}

	private void add(Object model) {
		if (model == null) {
			return;
		}
		if (_stepping) {
			// Ignore.
			return;
		}
		while (_models.size() > _index) {
			_models.remove(_models.size() - 1);
		}

		// Note: Normally at most one old history entry is removed.
		while (_models.size() > MAX_HINSTORY) {
			_models.remove(0);
		}

		_models.add(model);
		_index++;
	}

	/**
	 * {@link CommandHandler} showing the previous object in the inspector.
	 * 
	 * @see Forward
	 * @see GotoSelection
	 */
	public static class Back extends PreconditionCommandHandler {

		/**
		 * Creates a {@link History.Back} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Back(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
			return history(component).back();
		}

	}

	/**
	 * {@link CommandHandler} showing the next object from history in the inspector.
	 * 
	 * @see Back
	 * @see GotoSelection
	 */
	public static class Forward extends PreconditionCommandHandler {

		/**
		 * Creates a {@link History.Back} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Forward(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
			return history(component).forwards();
		}

	}

	CommandStep back() {
		if (_index <= 1) {
			return new Failure(I18NConstants.ERROR_NO_HISTORY);
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				_stepping = true;
				try {
					_index--;
					Object oldModel = _models.get(_index - 1);

					_component.setModel(oldModel);
				} finally {
					_stepping = false;
				}
			}
		};
	}

	CommandStep forwards() {
		if (_index == _models.size()) {
			return new Failure(I18NConstants.ERROR_NO_HISTORY);
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				_stepping = true;
				try {
					_index++;
					Object oldModel = _models.get(_index - 1);

					_component.setModel(oldModel);
				} finally {
					_stepping = false;
				}
			}
		};
	}

}
