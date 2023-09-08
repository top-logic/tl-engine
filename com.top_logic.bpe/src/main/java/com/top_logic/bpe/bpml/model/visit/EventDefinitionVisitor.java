/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model.visit;

import com.top_logic.bpe.bpml.model.CancelEventDefinition;
import com.top_logic.bpe.bpml.model.CompensateEventDefinition;
import com.top_logic.bpe.bpml.model.ConditionalEventDefinition;
import com.top_logic.bpe.bpml.model.ErrorEventDefinition;
import com.top_logic.bpe.bpml.model.EscalationEventDefinition;
import com.top_logic.bpe.bpml.model.EventDefinition;
import com.top_logic.bpe.bpml.model.MessageEventDefinition;
import com.top_logic.bpe.bpml.model.SignalEventDefinition;
import com.top_logic.bpe.bpml.model.TerminateEventDefinition;
import com.top_logic.bpe.bpml.model.TimerEventDefinition;

/**
 * Visit interface for the {@link EventDefinition} hierarchy.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EventDefinitionVisitor<R, A, E extends Throwable> {

	/**
	 * Default visit case to apply if no other visit methods are overridden.
	 */
	R visit(EventDefinition model, A arg) throws E;

	/**
	 * Visit case for {@link CancelEventDefinition}.
	 */
	default R visit(CancelEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Visit case for {@link CompensateEventDefinition}.
	 */
	default R visit(CompensateEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Visit case for {@link ConditionalEventDefinition}.
	 */
	default R visit(ConditionalEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Visit case for {@link ErrorEventDefinition}.
	 */
	default R visit(ErrorEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Visit case for {@link EscalationEventDefinition}.
	 */
	default R visit(EscalationEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Visit case for {@link MessageEventDefinition}.
	 */
	default R visit(MessageEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Visit case for {@link SignalEventDefinition}.
	 */
	default R visit(SignalEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Visit case for {@link TimerEventDefinition}.
	 */
	default R visit(TimerEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Visit case for {@link TerminateEventDefinition}.
	 */
	default R visit(TerminateEventDefinition model, A arg) throws E {
		return visit(asEventDefinition(model), arg);
	}

	/**
	 * Up-cast to {@link EventDefinition}.
	 */
	static EventDefinition asEventDefinition(EventDefinition model) {
		return model;
	}

}
