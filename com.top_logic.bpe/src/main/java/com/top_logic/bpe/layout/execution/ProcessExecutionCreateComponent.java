/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Iconified;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.engine.ExecutionProcessCreateHandler;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.gui.DefaultCreateAttributedComponent;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormMode;
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

	public ProcessExecutionCreateComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public TLClass getMetaElement() {
		return ExecutionProcessCreateHandler.getModelType(startEvent());
	}

	private StartEvent startEvent() {
		return (StartEvent) getModel();
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

	public ThemeImage getIcon() {
		StartEvent startEvent = startEvent();
		ThemeImage res = startEvent.getIcon();
		if (res == null) {
			ManualTask task = nextManualTask(startEvent);
			if (task instanceof Iconified) {
				res = ((Iconified) task).getIcon();
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
	protected void addMoreAttributes(TLClass aME, AttributeFormContext aContext) {
		super.addMoreAttributes(aME, aContext);

		FormDefinition fd = getDisplayDescription();
		FormEditorUtil.createEditorGroup(aContext, getMetaElement(), fd, null, FormMode.CREATE);
	}

	private ManualTask nextManualTask() {
		return nextManualTask(startEvent());

	}

	public static ManualTask nextManualTask(StartEvent startEvent) {
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
		PersonManager r = PersonManager.getManager();
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
