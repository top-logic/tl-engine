/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Iconified;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.engine.ExecutionProcessCreateHandler;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.layout.execution.start.ModelAsStartEvent;
import com.top_logic.bpe.layout.execution.start.StartEventSelector;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.gui.DefaultCreateAttributedComponent;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.MonomorphicCreateFormBuilderByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * {@link DefaultCreateAttributedComponent} showing the configured attributes of the first task when
 * starting a process.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ProcessExecutionCreateComponent extends DefaultCreateAttributedComponent
		implements DisplayDescriptionAware {

	/**
	 * Configuration options for {@link ProcessExecutionCreateComponent}.
	 */
	public interface Config extends DefaultCreateAttributedComponent.Config,
			MonomorphicCreateFormBuilderByExpression.WithInitialization, UIOptions {
		// Pure sum interface.
	}

	/**
	 * Options to directly configure in the layout editor.
	 */
	public interface UIOptions extends ConfigurationItem {

		/**
		 * Strategy of selecting the {@link StartEvent} of the process to start.
		 */
		@Name("startEvent")
		@ItemDefault
		@ImplementationClassDefault(ModelAsStartEvent.class)
		PolymorphicConfiguration<? extends StartEventSelector> getStartEvent();

	}

	private final StartEventSelector _selector;

	private StartEvent _startEvent;

	private final QueryExecutor _initialization;

	/**
	 * Creates a {@link ProcessExecutionCreateComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ProcessExecutionCreateComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_selector = context.getInstance(config.getStartEvent());
		_initialization = QueryExecutor.compileOptional(config.getInitialization());
	}

	@Override
	public TLClass getMetaElement() {
		return ExecutionProcessCreateHandler.getModelType(startEvent());
	}

	/**
	 * The event to start the process with.
	 */
	public StartEvent startEvent() {
		return _startEvent;
	}

	@Override
	public boolean isModelValid() {
		return _startEvent != null && super.isModelValid();
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean result = super.validateModel(context);

		_startEvent = _selector.getStartEvent(this);

		return result;
	}

	@Override
	public Set<String> getExcludeForUI() {
		Set<String> res = super.getExcludeForUI();

		TLModelUtil.addPartNames(res, getMetaElement());
		FormDefinition fd = getDisplayDescription();
		Set<String> includes = new HashSet<>();
		includes.add(ProcessExecution.COLLABORATION_ATTR);
		// TODO #24573: Get excludes for the UI.
		if (fd != null) {
//			collectNames(includes, fd.getParts());
		}
		res.removeAll(includes);
//		res.remove("name");
		return res;
	}

	/**
	 * The icon to display in the header of the process creation.
	 */
	public ThemeImage getIcon() {
		StartEvent startEvent = startEvent();
		ThemeImage res = startEvent.getIcon();
		if (res == null) {
			ManualTask nextTask = nextManualTask(startEvent);
			if (nextTask != null) {
				res = ((Iconified) nextTask).getIcon();
			}
		}
		if (res == null) {
			res = MetaResourceProvider.INSTANCE.getImage(startEvent, Flavor.ENLARGED);
		}
		return res;
	}

	@Override
	public TLObject getContextModel() {
		return null;
	}

	@Override
	protected void addAttributedConstraints(TLClass type, AttributeFormContext formContext) {
		// Prevent default, all attributes are added to a custom group.
		super.addAttributedConstraints(type, formContext);
	}

	@Override
	protected void addMoreAttributes(TLClass tlClass, AttributeFormContext aContext) {
		super.addMoreAttributes(tlClass, aContext);

		FormDefinition fd = getDisplayDescription();
		FormEditorUtil.createEditorGroup(aContext, getMetaElement(), fd, null, FormMode.CREATE);

		TLFormObject newCreation = aContext.createObject(tlClass, null, getContextModel());

		if (_initialization != null) {
			_initialization.execute(newCreation, this.getModel());
		}
	}

	private ManualTask nextManualTask() {
		return nextManualTask(startEvent());

	}

	private static ManualTask nextManualTask(StartEvent startEvent) {
		Edge edge = getSingleValueOrNull(startEvent.getOutgoing());
		int numOfTasksFound = 0;
		ManualTask singleTask = null;
		if (edge != null) {
			Node target = edge.getTarget();
			if (target instanceof ManualTask) {
				if (suitableActor(target, startEvent)) {
					return (ManualTask) target;
				}
				else {
					numOfTasksFound++;
					singleTask = (ManualTask) target;
				}
			}
		}

		// if there is only one Manual task after the StartEvent: take it without
		// taking care of actor
		if (numOfTasksFound == 1) {
			return singleTask;
		}

		return null;
	}

	private static boolean suitableActor(Node target, StartEvent startEvent) {
		GuiEngine guiEngine = GuiEngine.getInstance();
		Lane lane = startEvent.getLane();
		Lane targetLane = target.getLane();
		Person currentPerson = TLContext.currentUser();
		return targetLane == lane || guiEngine.isActor(currentPerson, targetLane, null);
	}

	private static <T> T getSingleValueOrNull(Collection<T> coll) {
		if (coll != null && coll.size() == 1) {
			return CollectionUtil.getSingleValueFromCollection(coll);
		}
		return null;
	}

	@Override
	public FormDefinition getDisplayDescription() {
		ManualTask manualTask = nextManualTask();
		if (manualTask != null) {
			return manualTask.getDisplayDescription();
		}
		return null;
	}

}
