/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.model.DefaultGateway;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.EndEvent;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.TerminateEventDefinition;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.TLClass;
import com.top_logic.util.Resources;

/**
 * A choice of the user finishing a {@link Token}: the path of {@link Edge}s to walk to the next
 * step.
 *
 * <p>
 * The path starts with an outgoing edge of the token's node. When this edge leads to a gateway
 * that needs a decision, the path continues with the chosen outgoing edge of the gateway. The
 * {@link #getLastEdge() last edge} therefore identifies the choice among all choices of the token.
 * </p>
 *
 * @see GuiEngine#getDecisions(Token)
 */
public class Decision {

	private final Token _token;

	private final List<Edge> _path;

	private final ResKey _label;

	private final List<ResKey> _errors;

	private final List<ResKey> _warnings;

	/**
	 * Creates a {@link Decision}.
	 *
	 * @param token
	 *        The token being finished.
	 * @param path
	 *        The edges to walk, starting with an outgoing edge of the node of the token.
	 */
	public Decision(Token token, List<Edge> path) {
		_token = token;
		_path = List.copyOf(path);
		_label = label(getLastEdge());

		GuiEngine engine = GuiEngine.getInstance();
		List<ResKey> errors = new ArrayList<>();
		List<ResKey> warnings = new ArrayList<>();
		for (Edge edge : _path) {
			errors.addAll(engine.checkErrors(token, edge));
			warnings.addAll(engine.checkWarnings(token, edge));
		}
		_errors = Collections.unmodifiableList(errors);
		_warnings = Collections.unmodifiableList(warnings);
	}

	private static ResKey label(Edge edge) {
		ResKey title = edge.getTitle();
		if (title != null) {
			return title;
		}
		String edgeName = edge.getName();
		if (!StringServices.isEmpty(edgeName)) {
			return ResKey.text(edgeName);
		}
		Node target = edge.getTarget();
		String targetName = target.getName();
		if (!StringServices.isEmpty(targetName)) {
			return ResKey.text(targetName);
		}
		if (target instanceof EndEvent end) {
			if (end.getDefinition() instanceof TerminateEventDefinition) {
				return I18NConstants.EDGE_OPTION_LABEL_ABORT;
			} else {
				return I18NConstants.EDGE_OPTION_LABEL_CANCEL;
			}
		}
		return I18NConstants.EDGE_OPTION_NO_NAME;
	}

	/**
	 * The {@link Token} being finished.
	 */
	public Token getToken() {
		return _token;
	}

	/**
	 * The {@link Edge}s to walk.
	 */
	public List<Edge> getPath() {
		return _path;
	}

	/**
	 * The {@link Edge} pointing to the target {@link Node} for which a new {@link Token} is
	 * created.
	 */
	public Edge getLastEdge() {
		return _path.get(_path.size() - 1);
	}

	/**
	 * The label of this choice: the title or name of the {@link #getLastEdge() last edge}, or the
	 * name of its target.
	 */
	public ResKey getLabel() {
		return _label;
	}

	/**
	 * The {@link #getLabel() label}, marked as impossible when {@link #getErrors() errors} block
	 * this choice.
	 */
	public ResKey getDisplayLabel() {
		return isBlocked() ? I18NConstants.IMPOSSIBLE_EDGE__LABEL.fill(_label) : _label;
	}

	/**
	 * The tooltip of the {@link #getLastEdge() last edge}, if it has a {@link Edge#getTitle()
	 * title}, <code>null</code> otherwise.
	 */
	public I18NStructuredText getTooltip() {
		Edge lastEdge = getLastEdge();
		return lastEdge.getTitle() != null ? lastEdge.getTooltip() : null;
	}

	/**
	 * Whether {@link #getErrors() errors} prevent walking the {@link #getPath() path}.
	 */
	public boolean isBlocked() {
		return !_errors.isEmpty();
	}

	/**
	 * The messages of the conditions that prevent walking the {@link #getPath() path}.
	 *
	 * @see GuiEngine#checkErrors(Token, Edge)
	 */
	public List<ResKey> getErrors() {
		return _errors;
	}

	/**
	 * The warnings the user confirms before walking the {@link #getPath() path}.
	 *
	 * @see GuiEngine#checkWarnings(Token, Edge)
	 */
	public List<ResKey> getWarnings() {
		return _warnings;
	}

	/**
	 * Whether this is the choice of the default flow of the gateway the user decides at.
	 */
	public boolean isDefault() {
		Edge lastEdge = getLastEdge();
		return lastEdge.getSource() instanceof DefaultGateway gateway && lastEdge.equals(gateway.getDefaultFlow());
	}

	/**
	 * The type of the additional data the user enters with this choice, <code>null</code> if none.
	 *
	 * <p>
	 * The form type of the {@link #getLastEdge() last edge} is preferred over the one of the first
	 * edge, which is preferred over the {@link com.top_logic.bpe.bpml.model.Participant#getEdgeFormType()
	 * edge form type} of the process.
	 * </p>
	 */
	public TLClass getFormType() {
		TLClass lastEdgeType = formType(getLastEdge());
		if (lastEdgeType != null) {
			return lastEdgeType;
		}
		TLClass firstEdgeType = formType(_path.get(0));
		if (firstEdgeType != null) {
			return firstEdgeType;
		}
		return _token.getNode().getProcess().getParticipant().getEdgeFormType();
	}

	private static TLClass formType(Edge edge) {
		return edge instanceof SequenceFlow flow ? flow.getFormType() : null;
	}

	/**
	 * Walks the {@link #getPath() path} of this choice.
	 *
	 * <p>
	 * Neither the {@link #getErrors() errors} nor the actor of the token are checked, see
	 * {@link ProcessExecutions#finish(Token, Edge, Object, com.top_logic.knowledge.wrap.person.Person)}
	 * for a checked completion.
	 * </p>
	 *
	 * @param additional
	 *        Additional data entered with the choice, an object of the {@link #getFormType() form
	 *        type}, or <code>null</code>. The default operation of the participant and the
	 *        operations of the sequence flows on the path get it as their fourth argument, after the
	 *        process execution, the finished and the new token; the engine stores it nowhere, an
	 *        operation keeps what the process needs of it.
	 */
	public void execute(Object additional) {
		ExecutionEngine.getInstance().execute(_token, _path, additional);
	}

	/**
	 * The order in which choices are offered: choices with a {@link Edge#getWeight() weight} first,
	 * ordered by weight, then the others by their {@link #getDisplayLabel() label}.
	 */
	public static Comparator<Decision> order(Locale locale) {
		Resources resources = Resources.getInstance(locale);
		Collator collator = Collator.getInstance(locale);
		return (self, other) -> {
			Double weight = self.getLastEdge().getWeight();
			Double otherWeight = other.getLastEdge().getWeight();
			if (weight != null) {
				if (otherWeight != null) {
					return Double.compare(weight.doubleValue(), otherWeight.doubleValue());
				}
				return -1;
			}
			if (otherWeight != null) {
				return 1;
			}
			return collator.compare(resources.getString(self.getDisplayLabel()),
				resources.getString(other.getDisplayLabel()));
		};
	}

}
