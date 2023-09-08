/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.exporter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.bpe.bpml.model.AnnotationAssociation;
import com.top_logic.bpe.bpml.model.AnnotationContainer;
import com.top_logic.bpe.bpml.model.BoundaryEvent;
import com.top_logic.bpe.bpml.model.BusinessRuleTask;
import com.top_logic.bpe.bpml.model.CallActivity;
import com.top_logic.bpe.bpml.model.CancelEventDefinition;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.CompensateEventDefinition;
import com.top_logic.bpe.bpml.model.ComplexGateway;
import com.top_logic.bpe.bpml.model.ConditionalEventDefinition;
import com.top_logic.bpe.bpml.model.DefaultGateway;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.EndEvent;
import com.top_logic.bpe.bpml.model.ErrorEventDefinition;
import com.top_logic.bpe.bpml.model.EscalationEventDefinition;
import com.top_logic.bpe.bpml.model.Event;
import com.top_logic.bpe.bpml.model.EventBasedGateway;
import com.top_logic.bpe.bpml.model.EventDefinition;
import com.top_logic.bpe.bpml.model.ExclusiveGateway;
import com.top_logic.bpe.bpml.model.Externalized;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.InclusiveGateway;
import com.top_logic.bpe.bpml.model.IntermediateCatchEvent;
import com.top_logic.bpe.bpml.model.IntermediateThrowEvent;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.MessageEventDefinition;
import com.top_logic.bpe.bpml.model.MessageFlow;
import com.top_logic.bpe.bpml.model.Named;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.ParallelGateway;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.ReceiveTask;
import com.top_logic.bpe.bpml.model.ScriptTask;
import com.top_logic.bpe.bpml.model.SendTask;
import com.top_logic.bpe.bpml.model.ServiceTask;
import com.top_logic.bpe.bpml.model.SignalEventDefinition;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.bpml.model.SubProcess;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.bpml.model.TerminateEventDefinition;
import com.top_logic.bpe.bpml.model.TextAnnotation;
import com.top_logic.bpe.bpml.model.TimerEventDefinition;
import com.top_logic.bpe.bpml.model.UserTask;
import com.top_logic.bpe.bpml.model.annotation.BPMLExtension;
import com.top_logic.bpe.bpml.model.visit.EventDefinitionVisitor;
import com.top_logic.bpe.bpml.model.visit.NodeVisitor;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * Algorithm exporting a {@link Collaboration} model to BPML syntax.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BPMLExporter
		implements NodeVisitor<Void, Void, XMLStreamException>, EventDefinitionVisitor<Void, Void, XMLStreamException> {

	/**
	 * Namespace URI that defines <i>TopLogic</i> BPML extensions.
	 */
	public static final String TL_EXTENSIONS_NS = "http://www.top-logic.com/ns/BPMN/extensions/7.0";

	private static final String BPMN = "http://www.omg.org/spec/BPMN/20100524/MODEL";

	private static final String TL_EXTENSIONS_PREFIX = "tl";

	private static final String EXTENSION_ELEMENTS = "extensionElements";

	private static final String BPMN_BOUNDARY_EVENT = "boundaryEvent";

	private static final String BPMN_SUB_PROCESS = "subProcess";

	private static final String BPMN_LANE_SET = "laneSet";
	private static final String SOURCE_REF = "sourceRef";
	private static final String TARGET_REF = "targetRef";

	private static final String BPMN_SEQUENCE_FLOW = "sequenceFlow";

	private static final String BPMN_FLOW_NODE_REF = "flowNodeRef";

	private static final String BPMN_CHILD_LANE_SET = "childLaneSet";

	private static final String BPMN_LANE = "lane";

	private static final String BPMN_INCOMING = "incoming";

	private static final String BPMN_OUTGOING = "outgoing";

	private static final String BPMN_PROCESS = "process";

	private static final String BPMN_MESSAGE_FLOW = "messageFlow";

	private static final String BPMN_PARTICIPANT = "participant";

	private static final String BPMN_TEXT_ANNOTATION = "textAnnotation";

	private static final String BPMN_ASSOCIATION = "association";

	private static final String BPMN_TEXT = "text";
	private static final String ID_ATTR = "id";

	private static final String BPMN_COLLABORATION = "collaboration";

	private static final String BPMN_DEFINITIONS = "definitions";

	private XMLStreamWriter _out;

	private boolean _exportExtensions;

	private Log _log = new LogProtocol(BPMLExporter.class);

	private final Map<TLStructuredTypePart, AttributeValueBinding<Object>> _bindings = new HashMap<>();

	/**
	 * Creates a {@link BPMLExporter}.
	 *
	 */
	public BPMLExporter(XMLStreamWriter out, boolean exportExtensions) {
		_out = out;
		_exportExtensions = exportExtensions;
	}

	/**
	 * The error output to write export problems to.
	 */
	public Log getLog() {
		return _log;
	}

	/**
	 * @see #getLog()
	 */
	public void setLog(Log log) {
		_log = log;
	}

	/**
	 * Writes the given model in BPML syntax.
	 */
	public void exportBPML(Collaboration model) throws XMLStreamException {
		_out.writeStartDocument(StringServices.UTF8, "1.0");

		// <bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		// xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
		// xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
		// xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
		// xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_1rrqeeu"
		// targetNamespace="http://bpmn.io/schema/bpmn">
		_out.setPrefix("bpmn", BPMN);
		_out.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		_out.setPrefix("bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI");
		_out.setPrefix("dc", "http://www.omg.org/spec/DD/20100524/DC");
		_out.setPrefix("di", "http://www.omg.org/spec/DD/20100524/DI");
		_out.setPrefix(ConfigurationSchemaConstants.CONFIG_NS_PREFIX, ConfigurationSchemaConstants.CONFIG_NS);
		_out.setPrefix(TL_EXTENSIONS_PREFIX, TL_EXTENSIONS_NS);

		_out.writeStartElement(BPMN, BPMN_DEFINITIONS);
		writeAttribute("targetNamespace", "http://bpmn.io/schema/bpmn");
		// Note: The ID of the definition element is not stored.
		writeCollaboration(model);
		writeProcesses(model);
		writeDiagram(model);
		_out.writeEndElement();

		_out.writeEndDocument();
		_out.flush();
	}

	private void writeId(Externalized model) throws XMLStreamException {
		writeAttribute(ID_ATTR, model.getExtId());
	}

	private void writeCollaboration(Collaboration model) throws XMLStreamException {
		_out.writeStartElement(BPMN, BPMN_COLLABORATION);
		writeId(model);
		for (Participant process : stableOrder(model.getParticipants())) {
			writeParticipant(process);
		}
		for (MessageFlow flowEdge : stableOrder(model.getMessageFlows())) {
			writeFlowEdge(flowEdge);
		}
		writeAnnotationContainer(model);
		writeExtensions(model);
		_out.writeEndElement();
	}

	private void writeAnnotationContainer(AnnotationContainer model) throws XMLStreamException {
		for (TextAnnotation annotation : stableOrder(model.getAnnotationDefinitions())) {
			writeAnnotation(annotation);
			for (AnnotationAssociation link : stableOrder(annotation.getAnnotationAssociations())) {
				_out.writeEmptyElement(BPMN, BPMN_ASSOCIATION);
				writeId(link);
				writeAttribute(SOURCE_REF, link.getSource().getExtId());
				writeAttribute(TARGET_REF, link.getTarget().getExtId());
			}
		}
	}

	private void writeAnnotation(TextAnnotation annotation) throws XMLStreamException {
		_out.writeStartElement(BPMN, BPMN_TEXT_ANNOTATION);
		writeId(annotation);
		{
			_out.writeStartElement(BPMN, BPMN_TEXT);
			_out.writeCharacters(annotation.getText());
			_out.writeEndElement();
		}
		_out.writeEndElement();
	}

	private void writeParticipant(Participant participant) throws XMLStreamException {
		_out.writeStartElement(BPMN, BPMN_PARTICIPANT);
		writeId(participant);
		writeName(participant);
		writeAttribute("processRef", participant.getProcess().getExtId());
		writeExtensions(participant);
		_out.writeEndElement();
	}

	private void writeFlowEdge(MessageFlow flowEdge) throws XMLStreamException {
		_out.writeEmptyElement(BPMN, BPMN_MESSAGE_FLOW);
		writeId(flowEdge);
		writeName(flowEdge);
		writeAttribute(SOURCE_REF, flowEdge.getSource().getExtId());
		writeAttribute(TARGET_REF, flowEdge.getTarget().getExtId());
	}

	private void writeName(Named named) throws XMLStreamException {
		writeAttribute("name", named.getName());
	}

	private void writeProcesses(Collaboration model) throws XMLStreamException {
		for (Process process : stableOrder(model.getProcesses())) {
			writeProcess(process);
		}
	}

	private void writeProcess(Process process) throws XMLStreamException {
		_out.writeStartElement(BPMN, BPMN_PROCESS);
		writeId(process);
		writeAttribute("isExecutable", Boolean.toString(process.getIsExecutable()));
		writeProcessContents(process);
		_out.writeEndElement();
	}

	private void writeProcessContents(Process process) throws XMLStreamException {
		List<? extends Lane> lanes = process.getLanes();
		if (!lanes.isEmpty()) {
			_out.writeStartElement(BPMN, BPMN_LANE_SET);
			for (Lane lane : stableOrder(lanes)) {
				writeLane(lane);
			}
			_out.writeEndElement();
		}

		for (Node node : stableOrder(process.getNodes())) {
			writeNode(node);
		}
		for (Edge edge : stableOrder(process.getEdges())) {
			writeEdge(edge);
		}
		writeAnnotationContainer(process);
		writeExtensions(process);
	}

	private void writeEdge(Edge edge) throws XMLStreamException {
		_out.writeStartElement(BPMN, BPMN_SEQUENCE_FLOW);
		writeId(edge);
		writeName(edge);
		writeAttribute(SOURCE_REF, edge.getSource().getExtId());
		writeAttribute(TARGET_REF, edge.getTarget().getExtId());
		writeExtensions(edge);
		_out.writeEndElement();
	}

	private void writeExtensions(Externalized obj) throws XMLStreamException {
		if (!_exportExtensions) {
			return;
		}

		boolean hasExtensions = false;
		for (TLStructuredTypePart attr : obj.tType().getAllParts()) {
			if (attr.isDerived()) {
				continue;
			}
			if (attr.getAnnotation(BPMLExtension.class) == null) {
				continue;
			}

			hasExtensions = writeExtension(obj, attr, hasExtensions);
		}
		endExtensions(hasExtensions);
	}

	private boolean writeExtension(Externalized obj, TLStructuredTypePart attribute, boolean hasExtensions)
			throws XMLStreamException {
		AttributeValueBinding<Object> binding = getExportBinding(attribute);
		if (binding != null) {
			hasExtensions = startExtendsions(hasExtensions);

			Object value = obj.tValue(attribute);
			boolean emptyElement = binding.useEmptyElement(attribute, value);
			if (emptyElement) {
				_out.writeEmptyElement(TL_EXTENSIONS_NS, attribute.getName());
			} else {
				_out.writeStartElement(TL_EXTENSIONS_NS, attribute.getName());
			}
			binding.storeValue(_log, _out, attribute, value);
			if (!emptyElement) {
				_out.writeEndElement();
			}
		}
		return hasExtensions;
	}

	@SuppressWarnings("unchecked")
	private AttributeValueBinding<Object> getExportBinding(TLStructuredTypePart attr) {
		AttributeValueBinding<Object> cachedBinding = _bindings.get(attr);
		if (cachedBinding != null || _bindings.containsKey(attr)) {
			return cachedBinding;
		}

		AttributeValueBinding<Object> resolvedBinding = (AttributeValueBinding<Object>) AttributeSettings.getInstance()
			.getExportBinding(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, attr);
		_bindings.put(attr, resolvedBinding);
		return resolvedBinding;
	}

	private boolean startExtendsions(boolean hasExtensions) throws XMLStreamException {
		if (!hasExtensions) {
			_out.writeStartElement(BPMN, EXTENSION_ELEMENTS);
			hasExtensions = true;
		}
		return hasExtensions;
	}

	private void endExtensions(boolean hasExtensions) throws XMLStreamException {
		if (hasExtensions) {
			_out.writeEndElement();
		}
	}

	private void writeNode(Node node) throws XMLStreamException {
		node.visit(this, null);
	}

	@Override
	public Void visit(Event event, Void arg) {
		throw new UnsupportedOperationException("Unsupported event type: " + event.tType());
	}

	@Override
	public Void visit(StartEvent event, Void arg) throws XMLStreamException {
		writeEvent(BPMN, "startEvent", event);
		return null;
	}

	@Override
	public Void visit(EndEvent event, Void arg) throws XMLStreamException {
		writeEvent(BPMN, "endEvent", event);
		return null;
	}

	@Override
	public Void visit(IntermediateCatchEvent event, Void arg) throws XMLStreamException {
		writeEvent(BPMN, "intermediateCatchEvent", event);
		return null;
	}

	@Override
	public Void visit(IntermediateThrowEvent event, Void arg) throws XMLStreamException {
		writeEvent(BPMN, "intermediateThrowEvent", event);
		return null;
	}

	@Override
	public Void visit(BoundaryEvent event, Void arg) throws XMLStreamException {
		startNamedTag(BPMN, BPMN_BOUNDARY_EVENT, event);
		writeAttribute("attachedToRef", event.getAttachedTo().getExtId());
		writeNodeContents(event);
		writeEventDefinition(event.getDefinition());
		writeExtensions(event);
		endNode();
		return null;
	}

	private void writeEventDefinition(EventDefinition definition) throws XMLStreamException {
		if (definition != null) {
			definition.visit(this, null);
		}
	}

	@Override
	public Void visit(ConditionalEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "conditionalEventDefinition", model);
		return null;
	}

	@Override
	public Void visit(ErrorEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "errorEventDefinition", model);
		return null;
	}

	@Override
	public Void visit(CancelEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "cancelEventDefinition", model);
		return null;
	}

	@Override
	public Void visit(SignalEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "signalEventDefinition", model);
		return null;
	}

	@Override
	public Void visit(CompensateEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "compensateEventDefinition", model);
		return null;
	}

	@Override
	public Void visit(MessageEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "messageEventDefinition", model);
		return null;
	}

	@Override
	public Void visit(TimerEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "timerEventDefinition", model);
		return null;
	}

	@Override
	public Void visit(EscalationEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "escalationEventDefinition", model);
		return null;
	}

	@Override
	public Void visit(TerminateEventDefinition model, Void arg) throws XMLStreamException {
		writeEventDefinition(BPMN, "terminateEventDefinition", model);
		return null;
	}

	private void writeEventDefinition(String ns, String tag, @SuppressWarnings("unused") EventDefinition model)
			throws XMLStreamException {
		_out.writeEmptyElement(ns, tag);
	}

	@Override
	public Void visit(EventDefinition model, Void arg) {
		throw new UnsupportedOperationException("Unsupported event definition type: " + model.tType());
	}

	@Override
	public Void visit(Gateway model, Void arg) {
		throw new UnsupportedOperationException("Unsupported gateway type: " + model.tType());
	}

	@Override
	public Void visit(ExclusiveGateway model, Void arg) throws XMLStreamException {
		writeDefaultGateway(BPMN, "exclusiveGateway", model);
		return null;
	}

	@Override
	public Void visit(ParallelGateway model, Void arg) throws XMLStreamException {
		writeGateway(BPMN, "parallelGateway", model);
		return null;
	}

	@Override
	public Void visit(InclusiveGateway model, Void arg) throws XMLStreamException {
		writeDefaultGateway(BPMN, "inclusiveGateway", model);
		return null;
	}
	
	@Override
	public Void visit(ComplexGateway model, Void arg) throws XMLStreamException {
		writeDefaultGateway(BPMN, "complexGateway", model);
		return null;
	}
	
	@Override
	public Void visit(EventBasedGateway model, Void arg) throws XMLStreamException {
		writeGateway(BPMN, "eventBasedGateway", model);
		return null;
	}

	@Override
	public Void visit(Task model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "task", model);
		return null;
	}

	@Override
	public Void visit(BusinessRuleTask model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "businessRuleTask", model);
		return null;
	}

	@Override
	public Void visit(ServiceTask model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "serviceTask", model);
		return null;
	}

	@Override
	public Void visit(SendTask model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "sendTask", model);
		return null;
	}

	@Override
	public Void visit(ReceiveTask model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "receiveTask", model);
		return null;
	}

	@Override
	public Void visit(ManualTask model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "manualTask", model);
		return null;
	}

	@Override
	public Void visit(UserTask model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "userTask", model);
		return null;
	}

	@Override
	public Void visit(ScriptTask model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "scriptTask", model);
		return null;
	}

	@Override
	public Void visit(CallActivity model, Void arg) throws XMLStreamException {
		writeTask(BPMN, "callActivity", model);
		return null;
	}

	@Override
	public Void visit(SubProcess model, Void arg) throws XMLStreamException {
		startNode(BPMN, BPMN_SUB_PROCESS, model);
		writeProcessContents(model);
		endNode();
		return null;
	}

	private void writeTask(String ns, String tag, Task model) throws XMLStreamException {
		writeNode(ns, tag, model);
	}

	private void writeLane(Lane model) throws XMLStreamException {
		startNamed(BPMN, BPMN_LANE, model);
		List<? extends Lane> lanes = model.getLanes();
		if (!lanes.isEmpty()) {
			_out.writeStartElement(BPMN, BPMN_CHILD_LANE_SET);
			for (Lane subLane : stableOrder(lanes)) {
				writeLane(subLane);
			}
			_out.writeEndElement();
		}
		writeAllNodeReferences(model);
		writeExtensions(model);
		endNamed();
	}

	private void writeAllNodeReferences(Lane model) throws XMLStreamException {
		writeLocalNodeReferences(model);

		// The format requires redundancy by explicitly referencing all nodes of a lane and all of
		// its sub-lanes.
		for (Lane subLane : stableOrder(model.getLanes())) {
			writeAllNodeReferences(subLane);
		}
	}

	private void writeLocalNodeReferences(Lane model) throws XMLStreamException {
		for (Node node : stableOrder(model.getNodes())) {
			_out.writeStartElement(BPMN, BPMN_FLOW_NODE_REF);
			_out.writeCharacters(node.getExtId());
			_out.writeEndElement();
		}
	}

	private void writeDiagram(Collaboration model) throws XMLStreamException {
		XMLStreamReader in =
			XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(new StringReader(model.getDiagram()));
		while (in.hasNext()) {
			switch (in.next()) {
				case XMLStreamConstants.START_DOCUMENT:
					break;
				case XMLStreamConstants.START_ELEMENT:
					_out.writeStartElement(in.getPrefix(), in.getLocalName(), in.getNamespaceURI());
					for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
						String ns = in.getAttributeNamespace(n);
						if (ns == null) {
							_out.writeAttribute(in.getAttributeLocalName(n), in.getAttributeValue(n));
						} else {
							_out.writeAttribute(in.getAttributePrefix(n), ns,
								in.getAttributeLocalName(n), in.getAttributeValue(n));
						}
					}
					break;
				case XMLStreamConstants.CDATA:
					_out.writeCData(in.getText());
					break;
				case XMLStreamConstants.CHARACTERS:
					_out.writeCharacters(in.getText());
					break;
				case XMLStreamConstants.COMMENT:
					_out.writeComment(in.getText());
					break;
				case XMLStreamConstants.END_ELEMENT:
					_out.writeEndElement();
					break;
				case XMLStreamConstants.END_DOCUMENT:
					break;
			}
		}
	}

	private void writeEvent(String ns, String tag, Event event) throws XMLStreamException {
		startNode(ns, tag, event);
		writeEventDefinition(event.getDefinition());
		endNode();
	}

	private void writeDefaultGateway(String ns, String tag, DefaultGateway model) throws XMLStreamException {
		startNamedTag(ns, tag, model);
		if (model.getDefaultFlow() != null) {
			writeAttribute("default", model.getDefaultFlow().getExtId());
		}
		writeNodeContents(model);
		endNode();
	}

	private void writeAttribute(String name, String value) throws XMLStreamException {
		if (value != null) {
			_out.writeAttribute(name, value);
		}
	}

	private void writeGateway(String ns, String tag, Gateway model) throws XMLStreamException {
		writeNode(ns, tag, model);
	}

	private void writeNode(String ns, String tag, Node model) throws XMLStreamException {
		startNode(ns, tag, model);
		endNode();
	}

	private void startNode(String ns, String tag, Node model) throws XMLStreamException {
		startNamed(ns, tag, model);
		writeNodeContents(model);
	}

	private void writeNodeContents(Node model) throws XMLStreamException {
		for (Edge outgoing : stableOrder(model.getOutgoing())) {
			_out.writeStartElement(BPMN, BPMN_OUTGOING);
			_out.writeCharacters(outgoing.getExtId());
			_out.writeEndElement();
		}
		for (Edge edge : stableOrder(model.getIncomming())) {
			_out.writeStartElement(BPMN, BPMN_INCOMING);
			_out.writeCharacters(edge.getExtId());
			_out.writeEndElement();
		}
		writeExtensions(model);
	}

	private void endNode() throws XMLStreamException {
		endNamed();
	}

	private void startNamed(String ns, String tag, Named model) throws XMLStreamException {
		startNamedTag(ns, tag, model);
	}

	private void startNamedTag(String ns, String tag, Named model) throws XMLStreamException {
		_out.writeStartElement(ns, tag);
		writeId(model);
		writeName(model);
	}

	private void endNamed() throws XMLStreamException {
		_out.writeEndElement();
	}

	private static <T extends Externalized> List<T> stableOrder(Collection<? extends T> nodes) {
		ArrayList<T> result = new ArrayList<>(nodes);
		Collections.sort(result, (x, y) -> x.getExtId().compareTo(y.getExtId()));
		return result;
	}

}
