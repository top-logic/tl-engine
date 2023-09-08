/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.bpe.bpml.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SyserrProtocol;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.bpe.bpml.exporter.BPMLExporter;
import com.top_logic.bpe.bpml.importer.BPMLImporter;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;

/**
 * Test for {@link BPMLImporter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBPMLImporter extends BasicTestCase {

	private BPMLImporter _importer;

	private ModelBinding _binding;

	private SyserrProtocol _log;

	private KnowledgeBase _kb;

	private BufferingProtocol _errors;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_errors = new BufferingProtocol();
		_log = new SyserrProtocol(_errors);
		Resources bundle = Resources.getInstance();
		_importer = new BPMLImporter(_log.asI18NLog(bundle));
		_kb = PersistencyLayer.getKnowledgeBase();
		_binding = new ApplicationModelBinding(_kb, ModelService.getApplicationModel());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		assertFalse(_errors.getError(), _log.hasErrors());
	}

	public void testBoundaryEvents() throws IOException, XMLStreamException {
		importFixture("diagram-boundary-events.bpmn.xml");
	}

	public void testGateways() throws IOException, XMLStreamException {
		importFixture("diagram-gateways.bpmn.xml");
	}

	public void testIntermediateEvents() throws IOException, XMLStreamException {
		importFixture("diagram-intermediate-events.bpmn.xml");
	}

	public void testMessageFlow() throws IOException, XMLStreamException {
		importFixture("diagram-message-flow.bpmn.xml");
	}

	public void testSubLanes() throws IOException, XMLStreamException {
		importFixture("diagram-sub-lanes.bpmn.xml");
	}

	public void testTaskTypes() throws IOException, XMLStreamException {
		importFixture("diagram-task-types.bpmn.xml");
	}

	public void testDefaultFlow() throws IOException, XMLStreamException {
		importFixture("diagram-default-flow.bpmn.xml");
	}

	public void testScenario() throws IOException, XMLStreamException {
		importFixture("aema-10-bpmn.xml");
	}

	private void importFixture(String fixture) throws IOException, XMLStreamException {
		Collaboration result;
		Logger.info("Importing: " + fixture, TestBPMLImporter.class);
		try (Transaction tx = _kb.beginTransaction()) {
			try (InputStream in = fixture(fixture)) {
				Source source = new StreamSource(in, fixture);
				result = _importer.importBPML(_binding, source);
				assertNotNull(result);
			}

			tx.commit();
		}

		StringWriter buffer = new StringWriter();
		XMLStreamWriter out = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(buffer);
		new BPMLExporter(out, false).exportBPML(result);
		Document exported = DOMUtil.parse(buffer.toString());

		checkReferentialConsistency(exported);
	}

	private void checkReferentialConsistency(Document exported) {
		Map<String, Element> definitions = new HashMap<>();
		index(definitions, exported);
		checkReferences(definitions, exported);
	}

	private void checkReferences(Map<String, Element> definitions, Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			checkRefAttr(definitions, element, "bpmnElement");
			checkRefAttr(definitions, element, "sourceRef");
			checkRefAttr(definitions, element, "targetRef");
			checkRefElement(definitions, element, "incoming");
			checkRefElement(definitions, element, "outgoing");
		}
		for (Node child : DOMUtil.children(node)) {
			checkReferences(definitions, child);
		}
	}

	private void checkRefElement(Map<String, Element> definitions, Element element, String name) {
		if (element.getLocalName().equals(name)) {
			String id = element.getTextContent();
			checkId(definitions, element, id);
		}
	}

	private void checkRefAttr(Map<String, Element> definitions, Element element, String name) {
		String id = element.getAttribute(name);
		if (id != null && !id.isEmpty()) {
			checkId(definitions, element, id);
		}
	}

	private void checkId(Map<String, Element> definitions, Element element, String id) {
		if (!definitions.containsKey(id)) {
			fail("Reference to undefined identifier '" + id + "': " + element);
		}
	}

	private void index(Map<String, Element> definitions, Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			String id = element.getAttribute("id");
			if (id != null && !id.isEmpty()) {
				definitions.put(id, element);
			}
		}
		for (Node child : DOMUtil.children(node)) {
			index(definitions, child);
		}
	}

	private InputStream fixture(String fixture) {
		InputStream result = getClass().getResourceAsStream("/test/com/top_logic/bpe/bpml/fixtures/" + fixture);
		assertNotNull(result);
		return result;
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(TestBPMLImporter.class);
	}

}
