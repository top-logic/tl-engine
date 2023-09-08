/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model.visit;

import com.top_logic.bpe.bpml.model.BoundaryEvent;
import com.top_logic.bpe.bpml.model.BusinessRuleTask;
import com.top_logic.bpe.bpml.model.CallActivity;
import com.top_logic.bpe.bpml.model.ComplexGateway;
import com.top_logic.bpe.bpml.model.EndEvent;
import com.top_logic.bpe.bpml.model.Event;
import com.top_logic.bpe.bpml.model.EventBasedGateway;
import com.top_logic.bpe.bpml.model.ExclusiveGateway;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.InclusiveGateway;
import com.top_logic.bpe.bpml.model.IntermediateCatchEvent;
import com.top_logic.bpe.bpml.model.IntermediateEvent;
import com.top_logic.bpe.bpml.model.IntermediateMessageThrowEvent;
import com.top_logic.bpe.bpml.model.IntermediateThrowEvent;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.ParallelGateway;
import com.top_logic.bpe.bpml.model.ReceiveTask;
import com.top_logic.bpe.bpml.model.ScriptTask;
import com.top_logic.bpe.bpml.model.SendTask;
import com.top_logic.bpe.bpml.model.ServiceTask;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.bpml.model.SubProcess;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.bpml.model.UserTask;

/**
 * Visit interface for the {@link Node} hierarchy.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NodeVisitor<R, A, E extends Throwable> {

	/**
	 * Default visit case for {@link Event}s, if no other specialized visit method is implemented.
	 */
	R visit(Event event, A arg) throws E;

	/**
	 * Visit case for {@link StartEvent}.
	 */
	default R visit(StartEvent event, A arg) throws E {
		return visit(asEvent(event), arg);
	}

	/**
	 * Visit case for {@link EndEvent}.
	 */
	default R visit(EndEvent event, A arg) throws E {
		return visit(asEvent(event), arg);
	}

	/**
	 * Visit case for {@link IntermediateEvent}.
	 */
	default R visit(IntermediateEvent event, A arg) throws E {
		return visit(asEvent(event), arg);
	}

	/**
	 * Visit case for {@link IntermediateCatchEvent}.
	 */
	default R visit(IntermediateCatchEvent event, A arg) throws E {
		return visit(asIntermediateEvent(event), arg);
	}

	/**
	 * Visit case for {@link IntermediateThrowEvent}.
	 */
	default R visit(IntermediateThrowEvent event, A arg) throws E {
		return visit(asIntermediateEvent(event), arg);
	}

	/**
	 * Visit case for {@link IntermediateMessageThrowEvent}.
	 */
	default R visit(IntermediateMessageThrowEvent event, A arg) throws E {
		return visit(asIntermediateThrowEvent(event), arg);
	}

	/**
	 * Visit case for {@link BoundaryEvent}.
	 */
	default R visit(BoundaryEvent event, A arg) throws E {
		return visit(asEvent(event), arg);
	}

	/**
	 * Default visit case for {@link Gateway}s, if no other specialized visit method is implemented.
	 */
	R visit(Gateway model, A arg) throws E;

	/**
	 * Visit case for {@link ComplexGateway}.
	 */
	default R visit(ComplexGateway model, A arg) throws E {
		return visit(asGateway(model), arg);
	}

	/**
	 * Visit case for {@link EventBasedGateway}.
	 */
	default R visit(EventBasedGateway model, A arg) throws E {
		return visit(asGateway(model), arg);
	}

	/**
	 * Visit case for {@link ExclusiveGateway}.
	 */
	default R visit(ExclusiveGateway model, A arg) throws E {
		return visit(asGateway(model), arg);
	}

	/**
	 * Visit case for {@link InclusiveGateway}.
	 */
	default R visit(InclusiveGateway model, A arg) throws E {
		return visit(asGateway(model), arg);
	}

	/**
	 * Visit case for {@link ParallelGateway}.
	 */
	default R visit(ParallelGateway model, A arg) throws E {
		return visit(asGateway(model), arg);
	}

	/**
	 * Default visit case for {@link Task}s, if no other specialized visit method is implemented.
	 */
	R visit(Task model, A arg) throws E;

	/**
	 * Visit case for {@link BusinessRuleTask}.
	 */
	default R visit(BusinessRuleTask model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Visit case for {@link ServiceTask}.
	 */
	default R visit(ServiceTask model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Visit case for {@link CallActivity}.
	 */
	default R visit(CallActivity model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Visit case for {@link ManualTask}.
	 */
	default R visit(ManualTask model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Visit case for {@link ReceiveTask}.
	 */
	default R visit(ReceiveTask model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Visit case for {@link ScriptTask}.
	 */
	default R visit(ScriptTask model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Visit case for {@link SendTask}.
	 */
	default R visit(SendTask model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Visit case for {@link SubProcess}.
	 */
	default R visit(SubProcess model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Visit case for {@link UserTask}.
	 */
	default R visit(UserTask model, A arg) throws E {
		return visit(asTask(model), arg);
	}

	/**
	 * Up-cast to {@link Event}.
	 */
	static Event asEvent(Event event) {
		return event;
	}

	/**
	 * Up-cast to {@link IntermediateEvent}.
	 */
	static IntermediateEvent asIntermediateEvent(IntermediateEvent event) {
		return event;
	}

	/**
	 * Up-cast to {@link IntermediateThrowEvent}.
	 */
	static IntermediateThrowEvent asIntermediateThrowEvent(IntermediateThrowEvent event) {
		return event;
	}

	/**
	 * Up-cast to {@link Gateway}.
	 */
	static Gateway asGateway(Gateway model) {
		return model;
	}

	/**
	 * Up-cast to {@link Task}.
	 */
	static Task asTask(Task model) {
		return model;
	}

}
