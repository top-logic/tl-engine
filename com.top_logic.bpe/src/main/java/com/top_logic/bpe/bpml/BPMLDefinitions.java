/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.bpe.bpml.exporter.BPMLExporter;
import com.top_logic.bpe.bpml.model.ApprovalState;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.TlBpeBpmlFactory;
import com.top_logic.bpe.execution.engine.InitialProcessSetup;
import com.top_logic.bpe.util.DeepCopy;
import com.top_logic.bpe.util.StateTransition;
import com.top_logic.bpe.util.Updater;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;

/**
 * Operations on workflow definitions ({@link Collaboration}s): creating, importing, updating,
 * copying, exporting, releasing and revoking them.
 *
 * <p>
 * None of the operations opens a transaction. An operation modifying the knowledge base must be
 * called within a transaction of the caller.
 * </p>
 *
 * @see BPMLFunctions
 */
public final class BPMLDefinitions {

	/**
	 * Name of the resource (relative to this class) with the diagram a new workflow starts with.
	 */
	public static final String DEFAULT_DIAGRAM = "default-diagram.bpmn.xml";

	/**
	 * File name suffix of an exported workflow definition.
	 */
	public static final String EXPORT_FILE_SUFFIX = ".bpml";

	/**
	 * Content type of an exported workflow definition.
	 */
	public static final String EXPORT_CONTENT_TYPE = "text/xml";

	private static final String BPMN_NS_DECLARATION = "xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"";

	private static final String BPMNDI_NS_PREFIX = "xmlns:bpmndi";

	private static final String DIAGRAM_NS_DECLARATIONS =
		BPMNDI_NS_PREFIX + "=\"http://www.omg.org/spec/BPMN/20100524/DI\" " +
			"xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" " +
			"xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"";

	private BPMLDefinitions() {
		// Static utility.
	}

	/**
	 * Creates a new workflow with the given name from the {@link #DEFAULT_DIAGRAM}.
	 *
	 * <p>
	 * The name is given to the {@link Collaboration} and to each of its {@link Participant}s.
	 * </p>
	 *
	 * @param name
	 *        The name of the new workflow.
	 * @return The new {@link Collaboration}.
	 */
	public static Collaboration createCollaboration(String name) {
		Collaboration collaboration = importSource(kb(),
			new ClassRelativeBinaryContent(BPMLDefinitions.class, DEFAULT_DIAGRAM));
		collaboration.setName(name);
		for (Participant participant : collaboration.getParticipants()) {
			participant.setName(name);
		}
		return collaboration;
	}

	/**
	 * Imports the given BPMN/BPML file as a new workflow.
	 *
	 * <p>
	 * A workflow without a name in the file is named after the file.
	 * </p>
	 *
	 * @param data
	 *        The BPMN/BPML file.
	 * @return The new {@link Collaboration}.
	 * @throws TopLogicException
	 *         If the file is no valid workflow definition.
	 */
	public static Collaboration importCollaboration(BinaryData data) {
		return InitialProcessSetup.importWorkflow(kb(), data);
	}

	/**
	 * Updates the given workflow so that it is structurally equivalent to the given BPMN/BPML file.
	 *
	 * <p>
	 * Elements of the workflow keep their identity, if the file contains an element with the same
	 * ID ({@link com.top_logic.bpe.bpml.model.Externalized#getExtId()}).
	 * </p>
	 *
	 * @param collaboration
	 *        The workflow to update.
	 * @param data
	 *        The BPMN/BPML file.
	 * @param updateExtensions
	 *        Whether the application specific BPML extensions (forms, actors, ...) are taken from
	 *        the file, too. Otherwise, the extensions of the workflow are kept.
	 * @throws TopLogicException
	 *         If the file is no valid workflow definition.
	 */
	public static void updateCollaboration(Collaboration collaboration, BinaryContent data,
			boolean updateExtensions) {
		try (InputStream in = data.getStream()) {
			updateCollaboration(collaboration, new StreamSource(in), updateExtensions);
		} catch (IOException | XMLStreamException ex) {
			throw importFailed(ex);
		}
	}

	/**
	 * Updates the given workflow from a diagram edited in the browser.
	 *
	 * <p>
	 * The diagram is taken as it is, the application specific BPML extensions of the workflow are
	 * kept.
	 * </p>
	 *
	 * @param collaboration
	 *        The workflow to update.
	 * @param xml
	 *        The BPMN XML of the diagram, as the diagram editor serializes it.
	 * @throws TopLogicException
	 *         If the diagram is no valid workflow definition.
	 *
	 * @see #fixDiagramNamespaces(String)
	 */
	public static void updateFromDiagram(Collaboration collaboration, String xml) {
		try {
			updateCollaboration(collaboration, new StreamSource(new StringReader(fixDiagramNamespaces(xml))),
				false);
		} catch (XMLStreamException ex) {
			throw importFailed(ex);
		}
	}

	/**
	 * Updates the given {@link Collaboration} so that it is structurally equivalent to the BPML
	 * given as source.
	 *
	 * @param collaboration
	 *        The {@link Collaboration} to update.
	 * @param source
	 *        The BPML source.
	 * @param updateExtensions
	 *        Whether the BPML extensions of the original collaboration must be updated with the
	 *        extensions from the new one.
	 * @throws XMLStreamException
	 *         If the source cannot be parsed.
	 */
	public static void updateCollaboration(Collaboration collaboration, Source source, boolean updateExtensions)
			throws XMLStreamException {
		// Importing a process model runs with definer's rights, see
		// AbstractModelBinding#usesSecurity().
		ModelBinding binding =
			new ApplicationModelBinding(collaboration.tKnowledgeBase(), ModelService.getApplicationModel(), false);
		Collaboration newCollaboration = InitialProcessSetup.importBPML(source, binding);
		new Updater(collaboration, newCollaboration, updateExtensions).update();
	}

	/**
	 * Adds the diagram namespace declarations to BPMN XML that uses them without declaring them.
	 *
	 * <p>
	 * The XML serializer of the bpmn-js diagram editor omits these declarations in some cases.
	 * </p>
	 *
	 * @param xml
	 *        The BPMN XML as serialized by the diagram editor.
	 * @return The XML with the namespace declarations.
	 */
	public static String fixDiagramNamespaces(String xml) {
		// TODO #23835: Workaround for buggy XML serializer in bpmn-js.
		if (xml.contains(BPMNDI_NS_PREFIX)) {
			return xml;
		}
		return xml.replace(BPMN_NS_DECLARATION, BPMN_NS_DECLARATION + " " + DIAGRAM_NS_DECLARATIONS);
	}

	/**
	 * Creates an independent copy of the given workflow in state
	 * {@link ApprovalState#DEVELOPMENT_CLASSIFIER}.
	 *
	 * @param orig
	 *        The workflow to copy.
	 * @param newName
	 *        The name of the copy.
	 * @return The copy.
	 */
	public static Collaboration copyCollaboration(Collaboration orig, String newName) {
		Collaboration copy = DeepCopy.copyDeep(orig);
		copy.setName(newName);
		copy.setState(ApprovalState.developmentClassifier());
		return copy;
	}

	/**
	 * Writes the given workflow as BPML XML including the application specific extensions.
	 *
	 * @param collaboration
	 *        The workflow to export.
	 * @param out
	 *        The stream to write to.
	 * @param prettyPrint
	 *        Whether the XML is indented.
	 */
	public static void exportXml(Collaboration collaboration, OutputStream out, boolean prettyPrint)
			throws IOException {
		if (prettyPrint) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			writeXml(collaboration, buffer);
			Document document = DOMUtil.parse(buffer.toString(StringServices.UTF8));
			XMLPrettyPrinter.dump(out, document);
		} else {
			writeXml(collaboration, out);
		}
	}

	/**
	 * The given workflow as BPML file.
	 *
	 * @param collaboration
	 *        The workflow to export.
	 * @param prettyPrint
	 *        Whether the XML is indented.
	 * @return The file named {@link #exportFileName(Collaboration)}.
	 *
	 * @see #exportXml(Collaboration, OutputStream, boolean)
	 */
	public static BinaryData export(Collaboration collaboration, boolean prettyPrint) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			exportXml(collaboration, buffer, prettyPrint);
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_EXPORT_FAILED__DETAILS.fill(ex.getMessage()), ex);
		}
		return BinaryDataFactory.createBinaryData(buffer.toByteArray(), EXPORT_CONTENT_TYPE,
			exportFileName(collaboration));
	}

	/**
	 * Name of the file the given workflow is exported to.
	 */
	public static String exportFileName(Collaboration collaboration) {
		return collaboration.getName() + EXPORT_FILE_SUFFIX;
	}

	private static void writeXml(Collaboration collaboration, OutputStream out) throws IOException {
		try {
			XMLStreamWriter xout =
				XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(out, StringServices.UTF8);
			new BPMLExporter(xout, true).exportBPML(collaboration);
		} catch (XMLStreamException ex) {
			throw new IOException(ex);
		}
	}

	/**
	 * The transition releasing a workflow: from {@link ApprovalState#DEVELOPMENT_CLASSIFIER} or
	 * {@link ApprovalState#RETIRED_CLASSIFIER} to {@link ApprovalState#RELEASED_CLASSIFIER}.
	 */
	public static StateTransition releaseTransition() {
		return stateTransition(
			Set.of(ApprovalState.developmentClassifier(), ApprovalState.retiredClassifier()),
			ApprovalState.releasedClassifier());
	}

	/**
	 * The transition revoking a workflow: from {@link ApprovalState#RELEASED_CLASSIFIER} to
	 * {@link ApprovalState#RETIRED_CLASSIFIER}.
	 */
	public static StateTransition revokeTransition() {
		return stateTransition(Set.of(ApprovalState.releasedClassifier()), ApprovalState.retiredClassifier());
	}

	private static StateTransition stateTransition(Set<TLClassifier> fromStates,
			TLClassifier toState) {
		TLClass type = TlBpeBpmlFactory.getCollaborationType();
		return new StateTransition(type, type.getPartOrFail(Collaboration.STATE_ATTR), fromStates, toState);
	}

	/**
	 * Whether the given workflow can be {@link #release(Collaboration) released}.
	 */
	public static boolean canRelease(Collaboration collaboration) {
		return releaseTransition().isApplicable(collaboration);
	}

	/**
	 * Whether the given workflow can be {@link #revoke(Collaboration) revoked}.
	 */
	public static boolean canRevoke(Collaboration collaboration) {
		return revokeTransition().isApplicable(collaboration);
	}

	/**
	 * Releases the given workflow, so that processes can be started with it.
	 *
	 * @throws TopLogicException
	 *         If the workflow is already released.
	 *
	 * @see #releaseTransition()
	 */
	public static void release(Collaboration collaboration) {
		releaseTransition().apply(collaboration);
	}

	/**
	 * Revokes the given released workflow.
	 *
	 * @throws TopLogicException
	 *         If the workflow is not released.
	 *
	 * @see #revokeTransition()
	 */
	public static void revoke(Collaboration collaboration) {
		revokeTransition().apply(collaboration);
	}

	private static Collaboration importSource(KnowledgeBase kb, BinaryContent data) {
		try {
			return InitialProcessSetup.importBPML(kb, data);
		} catch (XMLStreamException | IOException ex) {
			throw importFailed(ex);
		}
	}

	private static TopLogicException importFailed(Exception ex) {
		TopLogicException result = new TopLogicException(
			com.top_logic.bpe.execution.engine.I18NConstants.ERROR_IMPORT_FAILED__DETAILS.fill(ex.getMessage()), ex);
		result.initDetails(ResKey.text(ex.getMessage()));
		return result;
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

}
