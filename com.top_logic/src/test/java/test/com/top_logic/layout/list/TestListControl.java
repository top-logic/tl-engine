/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.xml.parsers.DocumentBuilder;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import test.com.top_logic.basic.StringWriterNonNull;
import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.TestingRenderer;
import test.com.top_logic.layout.list.model.AbstractSelectorContextTest;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.DOMAction;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DefaultUpdateQueue;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.selection.OptimizedSelectorContext;
import com.top_logic.layout.list.ListControl;
import com.top_logic.layout.list.model.ListSelectionManager;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Test case for {@link ListControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestListControl extends AbstractLayoutTest {
	
	private DefaultListModel model;
	private DefaultListSelectionModel selection;
	private ListControl control;
	private TestingRenderer renderer;

	DocumentBuilder builder;
	
	int nextElement;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		builder = DOMUtil.newDocumentBuilderNamespaceAware();
		
		nextElement = 0;
		
		model = new DefaultListModel();
		selection = new DefaultListSelectionModel();
		selection.setLeadAnchorNotificationEnabled(false);
		
		// Synchronize selection with data.
		model.addListDataListener(new ListSelectionManager(selection));
		
		control = new ListControl(model, selection);
		renderer = new TestingRenderer();
		control.setItemContentRenderer(renderer);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		builder = null;
		
		model = null;
		control = null;
		renderer = null;
	}
	
	public void testUpdates() throws IOException {
		// Add elements 0,..9.
		doAdd(range(0, 10));

		selection.addSelectionInterval(9, 9);

		// Attach control and produce output.
		doWrite();
		
		// Check, whether all elements have been rendered.
		assertEquals(range(0, 10), renderer.getRenderedObjects());
		
		// Attach control and produce output.
		doWrite();
		
		// Check, whether all elements have been rendered.
		assertEquals(range(0, 10), renderer.getRenderedObjects());
		
		model.removeElementAt(4); // Remove 4
		model.removeElementAt(4); // Remove 5
		model.removeElementAt(4); // Remove 6
		
		assertEquals(6, selection.getMinSelectionIndex());
		
		doWrite();
		assertEquals(concat(range(0, 4), range(7, 10)), renderer.getRenderedObjects());
		
		model.removeElementAt(6); // Remove 9
		model.removeElementAt(5); // Remove 8
		model.add(0, Integer.valueOf((-1)));
		model.addElement(Integer.valueOf(10));

		assertEquals(-1, selection.getMinSelectionIndex());

		assertTrue(control.isInvalid());
		List<ClientAction> actions = doRevalidate();
		assertFalse(control.isInvalid());
		
		doRenderUpdates(actions);
		assertEquals("New elements have been rendered.", list(-1, 10), renderer.getRenderedObjects());
		
		doWrite();
		assertEquals(concat(range(-1, 4), list(7, 10)), renderer.getRenderedObjects());
	}

	public void testWriteAfterInsert() throws IOException {
		doAdd(range(0, 10));
		doWrite();
		
		doAdd(0, range(-2, 0));

		// Directly call write instead of revalidate. This might produce an
		// error, if the internal state of the control has not been adjusted to
		// the changed model.
		doWrite();
		
		assertEquals(range(-2, 10), renderer.getRenderedObjects());
	}
	
	public void testSelectionUpdateOnInsert() throws IOException {
		doAdd(range(0, 10));
		selection.addSelectionInterval(1, 1);
		
		doWrite();
		assertEquals(1, selection.getMinSelectionIndex());
		
		doAdd(0, range(-2, 0));
		
		assertEquals("Selection has adjusted according to insert.", 3, selection.getMinSelectionIndex());
		
		doRevalidate();
		
		assertEquals("Selection keeps stable during revalidation.", 3, selection.getMinSelectionIndex());
		
		doWrite();
		assertEquals(range(-2, 10), renderer.getRenderedObjects());
	}

	public void testSelectionUpdateOnDelete() throws IOException {
		doAdd(range(0, 10));
		selection.addSelectionInterval(4, 4);
		
		doWrite();
		assertEquals(4, selection.getMinSelectionIndex());
		
		model.remove(7);
		model.remove(6);
		model.remove(3);
		model.remove(0);
		
		assertEquals("Selection has adjusted according to delete.", 2, selection.getMinSelectionIndex());
		
		doRevalidate();
		
		assertEquals("Selection keeps stable during revalidation.", 2, selection.getMinSelectionIndex());
		
		doWrite();
		assertEquals(concat(list(1, 2), list(4, 5), list(8, 9)), renderer.getRenderedObjects());
	}
	
	/**
	 * Test multiple random updates to search for inconsistency problems.
	 */
	public void testRandom() throws IOException, SAXException {
		Random rnd = new Random(42);
		
		doWrite();

		ArrayList<Integer> expectedElements = new ArrayList<>();
		ArrayList<String> elementIdsBefore = new ArrayList<>();
		
		int updateCnt = 50;
		for (int update = 0; update < updateCnt; update++) {
			
			Logger.info("Current state: " + expectedElements, TestListControl.class);
			Logger.info("Current IDs: " + elementIdsBefore, TestListControl.class);
			
			ArrayList<Integer> elementsBefore = new ArrayList<>(expectedElements);
			

			insertElements(rnd, expectedElements, 100);
			assertUpdatesOk(expectedElements, elementsBefore, elementIdsBefore, 100);
			
			int maxModificationsPerUpdate = 20 + update / 20;
			randomlyChangeModel(rnd, expectedElements, maxModificationsPerUpdate);
			assertUpdatesOk(expectedElements, elementsBefore, elementIdsBefore, maxModificationsPerUpdate);
		}
	}

	private void assertUpdatesOk(List<Integer> expectedElements, List<Integer> elementsBefore,
			ArrayList<String> elementIdsBefore, int maxModifications) throws IOException, SAXException {
		int numberOfUpdates = 0;
		
		for (ClientAction action : doRevalidate()) {
			if (!(action instanceof DOMAction)) {
				// May be JSSnipplet to delete client side cache
				continue;
			}
			Document document = parseAction((DOMAction) action);
			
			numberOfUpdates += interpretAction(document, elementIdsBefore, elementsBefore);
		}
		
		assertEquals("Client actions describe the correct update.", expectedElements, elementsBefore);
		if (maxModifications >= 0) {
			assertTrue("numberOfUpdates(" + numberOfUpdates + ") <= maxModificationsPerUpdate(" + maxModifications + ")", numberOfUpdates <= maxModifications);
		}
	}

	private void randomlyChangeModel(Random rnd, ArrayList<Integer> expectedElements, int maxModifications) {
		int size = model.getSize();
		for (int n = 0; n < maxModifications; n++) {
			float choice = rnd.nextFloat();
			
			if (choice < 0.4) {
				if (size > 0) {
					int from = rnd.nextInt(size);
					int to = from + rnd.nextInt(size - from);
					model.removeRange(from, to);
					for (int i = to; i >= from; i--) {
						expectedElements.remove(i);
					}
					size -= (to-from) + 1;
				}
			}

			else if (choice < 0.9) {
				int index = size == 0 ? 0 : rnd.nextInt(size + 1);
				Integer element = Integer.valueOf(nextElement++);
				model.add(index, element);
				expectedElements.add(index, element);
				size++;
			}

			else {
				if (size > 0) {
					int index = rnd.nextInt(size);
					Integer element = Integer.valueOf(nextElement++);
					model.set(index, element);
					expectedElements.set(index, element);
				}
			}
		}
	}

	private void insertElements(Random rnd, ArrayList<Integer> expectedElements, int count) {
		int size = model.getSize();
		int index = size == 0 ? 0 : rnd.nextInt(size + 1);
		for (int n = 0; n < count; n++) {
			Integer element = Integer.valueOf(nextElement++);
			model.add(index, element);
			expectedElements.add(index, element);
			
			index++;
		}
	}
	
	private int interpretAction(Document document, ArrayList<String> elementIdsBefore, List<Integer> elementsBefore) {
		Element actionElement = getChild(document.getDocumentElement(), AJAXConstants.AJAX_NAMESPACE, localname(AJAXConstants.AJAX_ACTION_ELEMENT));
		String type = actionElement.getAttributeNS(AJAXConstants.XSI_NAMESPACE, localname(AJAXConstants.XSI_TYPE_ATTRIBUTE));
		
		int numberOfUpdates;
		if (RangeReplacement.RANGE_REPLACEMENT_XSI_TYPE.equals(type)) {
			String firstId = textContent(getChild(actionElement, AJAXConstants.AJAX_NAMESPACE, localname(AJAXConstants.AJAX_ID_ELEMENT)));
			int firstIndex = elementIdsBefore.indexOf(firstId);

			String lastId = textContent(getChild(actionElement, AJAXConstants.AJAX_NAMESPACE, localname(AJAXConstants.AJAX_STOP_ID_ELEMENT)));
			int lastIndex = elementIdsBefore.indexOf(lastId);
			
			for (int n = lastIndex; n >= firstIndex; n--) {
				elementsBefore.remove(n);
				elementIdsBefore.remove(n);
			}
			
			int insertedElements = interpretNewElements(elementIdsBefore, elementsBefore, actionElement, firstIndex);
			
			numberOfUpdates = Math.max(insertedElements, 1);
			// numberOfUpdates = Math.max(insertedElements.getLength(), lastIndex - firstIndex + 1);
		}
		else if (FragmentInsertion.FRAGMENT_INSERTION_XSI_TYPE.equals(type)) {
			String firstId = textContent(getChild(actionElement, AJAXConstants.AJAX_NAMESPACE, localname(AJAXConstants.AJAX_ID_ELEMENT)));
			String position = actionElement.getAttributeNS(null, AJAXConstants.AJAX_POSITION_ATTRIBUTE);
			
			int firstIndex;
			if (AJAXConstants.AJAX_POSITION_START_VALUE.equals(position)) {
				firstIndex = 0;
			} else if (AJAXConstants.AJAX_POSITION_END_VALUE.equals(position)) {
				firstIndex = elementsBefore.size();
			} else {
				firstIndex = elementIdsBefore.indexOf(firstId);
				
				if (AJAXConstants.AJAX_POSITION_AFTER_VALUE.equals(position)) {
					firstIndex++;
				}
			}
			
			numberOfUpdates = interpretNewElements(elementIdsBefore, elementsBefore, actionElement, firstIndex);
		}
		else if (ElementReplacement.ELEMENT_REPLACEMENT_XSI_TYPE.equals(type)) {
			String firstId = textContent(getChild(actionElement, AJAXConstants.AJAX_NAMESPACE, localname(AJAXConstants.AJAX_ID_ELEMENT)));
			if (control.getID().equals(firstId)) {
				assertEquals("Received repaint.", control.getID(), firstId);
				
				elementsBefore.clear();
				elementIdsBefore.clear();
				
				numberOfUpdates = interpretNewElements(elementIdsBefore, elementsBefore, actionElement, 0);
			} else {
				int indexOfID = elementIdsBefore.indexOf(firstId);
				elementIdsBefore.remove(indexOfID);
				elementsBefore.remove(indexOfID);
				
				int insertedElements = interpretNewElements(elementIdsBefore, elementsBefore, actionElement, indexOfID);
				
				numberOfUpdates = Math.max(insertedElements, 1);
			}
		}
		else {
			fail("Unknown update type: " + type);
			throw new UnreachableAssertion("fail");
		}
		return numberOfUpdates;
	}

	private int interpretNewElements(ArrayList<String> elementIdsBefore,
			List<Integer> elementsBefore, Element actionElement, int firstIndex) {
		Iterable<Element> fragments = DOMUtil.elementsNS(actionElement, AJAXConstants.AJAX_NAMESPACE, "fragment");
		String htmlFragment = getSingleElement(fragments).getTextContent();
		Element rootElement = DOMUtil.parse(wrapWithRootTag(htmlFragment)).getDocumentElement();
		NodeList insertedElements = rootElement.getElementsByTagNameNS(HTMLConstants.XHTML_NS, HTMLConstants.LI);
		for (int n = 0, cnt = insertedElements.getLength(); n < cnt; n++) {
			Element insertedElement = (Element) insertedElements.item(n);
			int index = firstIndex + n;
			elementsBefore.add(index, Integer.valueOf(textContent(insertedElement.getFirstChild())));
			elementIdsBefore.add(index, insertedElement.getAttributeNS(null, HTMLConstants.ID_ATTR));
		}
		return insertedElements.getLength();
	}

	private Element getSingleElement(Iterable<Element> fragments) {
		Iterator<Element> iterator = fragments.iterator();
		Element fragmentElement = iterator.next();
		assertFalse(iterator.hasNext());
		return fragmentElement;
	}

	private String wrapWithRootTag(String htmlFragment) {
		return "<div xmlns='" + HTMLConstants.XHTML_NS + "'>" + htmlFragment + "</div>";
	}

	private Document parseAction(DOMAction action) throws IOException, SAXException {
		StringWriter buffer = new StringWriterNonNull();
		TagWriter out = new TagWriter(buffer);
		
		// Required to declare namespaces.
		out.beginBeginTag(AJAXConstants.AJAX_ACTIONS_ELEMENT);
		out.writeAttribute("xmlns", HTMLConstants.XHTML_NS);
		out.writeAttribute(AJAXConstants.AJAX_XMLNS_ATTRIBUTE, AJAXConstants.AJAX_NAMESPACE);
		out.endBeginTag();
		
		action.writeAsXML(displayContext(), out);
		
		out.endTag(AJAXConstants.AJAX_ACTIONS_ELEMENT);
		
		String actionString = buffer.toString();
		Logger.info(actionString, TestListControl.class);
		
		Document document = builder.parse(new InputSource(new StringReader(actionString)));
		return document;
	}
	
	public void testWithSelector() throws IOException, SAXException, CheckException, VetoException {
		int optionCount = 2000;
		int optionsPerPage = 77;
		
		ArrayList<String> elementIdsBefore = new ArrayList<>();
		List<Integer> elementsBefore = new ArrayList<>();
		
		doWrite();
		
		FormContext origContext = new FormContext("form", ResPrefix.forTest("none"));
		SelectField origField = FormFactory.newSelectField("selection", range(0, optionCount));
		origField.setOptionLabelProvider(DefaultLabelProvider.INSTANCE);
		origContext.addMember(origField);
		
		OptimizedSelectorContext selectorContext = new OptimizedSelectorContext(origField, DefaultLabelProvider.INSTANCE, 
		                                           optionsPerPage, Command.DO_NOTHING, false);
		
		model = (DefaultListModel) selectorContext.getOptionList().getListModel();
		control.setListModel(model);
		
		selection = (DefaultListSelectionModel) selectorContext.getOptionList().getSelectionModel();
		control.setSelectionModel(selection);
		
		assertUpdatesOk(range(0, optionsPerPage), elementsBefore, elementIdsBefore, -1);
		
		FormFieldInternals.updateField(selectorContext.getPattern(), "13");
		assertUpdatesOk(createExpected(optionCount, optionsPerPage, 0, "13"), elementsBefore, elementIdsBefore, -1);
		
		Object newOptionPage = AbstractSelectorContextTest.getOptionPages(selectorContext).getOptions().get(1);
		FormFieldInternals.updateField(AbstractSelectorContextTest.getOptionPages(selectorContext), Collections.singletonList(AbstractSelectorContextTest.getOptionPages(selectorContext).getOptionID(newOptionPage)));
		assertUpdatesOk(createExpected(optionCount, optionsPerPage, 1, "13"), elementsBefore, elementIdsBefore, -1);
		
		FormFieldInternals.updateField(selectorContext.getPattern(), "3");
		int newPage =
			AbstractSelectorContextTest.getOptionPages(selectorContext).getOptions().indexOf(AbstractSelectorContextTest.getOptionPages(selectorContext).getSingleSelection());
		assertUpdatesOk(createExpected(optionCount, optionsPerPage, newPage, "3"), elementsBefore, elementIdsBefore, -1);
		
		List<Integer> expectedElements = createExpected(optionCount, optionsPerPage, 0, "3");
		Random rnd = new Random(42);
		String pattern = "3";
		int page = 0;
		for (int update = 0; update < 100; update++) {
			float choice = rnd.nextFloat();
			if (choice < 0.1) {
				pattern = "";
			} else  if (choice < 0.5) {
				// Add char to pattern
				pattern += Integer.toString(rnd.nextInt(10));
			}
			else {
				if (pattern.length() > 0) {
					int removedPos = rnd.nextInt(pattern.length());
					pattern = pattern.substring(0, removedPos) + pattern.substring(removedPos + 1);
				}
			}
			FormFieldInternals.updateField(selectorContext.getPattern(), pattern);
			
			if (AbstractSelectorContextTest.getOptionPages(selectorContext).getOptions().size() > 0) {
				page = rnd.nextInt(AbstractSelectorContextTest.getOptionPages(selectorContext).getOptions().size());
				FormFieldInternals.updateField(AbstractSelectorContextTest.getOptionPages(selectorContext), Collections.singletonList(AbstractSelectorContextTest.getOptionPages(selectorContext).getOptionID(AbstractSelectorContextTest.getOptionPages(selectorContext).getOptions().get(page))));
			} else {
				page = 0;
			}

			Logger.info("Current state: " + elementsBefore, TestListControl.class);
			Logger.info("Current IDs: " + elementIdsBefore, TestListControl.class);
			Logger.info("Updating: pattern='" + pattern + "', page='" + page + "', update='" + update + "'", TestListControl.class);
			elementsBefore = expectedElements;
			expectedElements = createExpected(optionCount, optionsPerPage, page, pattern);
			if (expectedElements.size() == 0) {
				expectedElements = elementsBefore;
			}
			assertUpdatesOk(expectedElements, elementsBefore, elementIdsBefore, -1);
		}
	}

	private List<Integer> createExpected(int optionCount, int optionsPerPage, int page, final String pattern) {
		List<Integer> result = FilterUtil.filterList(new Filter<Object>() {
			@Override
			public boolean accept(Object anObject) {
				return anObject.toString().indexOf(pattern) >= 0;
			}
		}, range(0, optionCount));
		
		for (int n = 0; n < page; n++) {
			for (int i = 0; i < optionsPerPage; i++) {
				result.remove(0);
			}
		}
		
		while (result.size() > optionsPerPage) {
			result.remove(result.size() - 1);
		}
		
		return result;
	}

	private String textContent(Node element) {
		Node firstChild = element.getFirstChild();
		if (firstChild == null) return "";
		
		// Assume a single text node.
		return firstChild.getNodeValue();
	}

	private Element getChild(Element element, String namespace, String localname) {
		return (Element) element.getElementsByTagNameNS(namespace, localname).item(0);
	}

	private String localname(String qualifiedName) {
		return qualifiedName.substring(qualifiedName.indexOf(':') + 1);
	}

	private void doAdd(List<?> range) {
		for (Iterator<?> it = range.iterator(); it.hasNext();) {
			model.addElement(it.next());
		}
	}
	
	private void doAdd(int index, List<?> range) {
		for (Iterator<?> it = range.iterator(); it.hasNext();) {
			model.add(index++, it.next());
		}
	}
	
	private List<ClientAction> doRevalidate() {
		renderer.clear();
		
		DefaultUpdateQueue updates = new DefaultUpdateQueue();
		control.revalidate(displayContext(), updates);

		return updates.getStorage();
	}

	private void doRenderUpdates(List<ClientAction> actions) throws IOException {
		for (Iterator<ClientAction> it = actions.iterator(); it.hasNext();) {
			ClientAction action = it.next();
			
			StringWriter buffer = new StringWriterNonNull();
			action.writeAsXML(displayContext(), new TagWriter(buffer));
			Logger.info(buffer.toString(), this);
		}
	}
	
	private void doWrite() throws IOException {
		renderer.clear();
		
		StringWriter buffer = new StringWriterNonNull();
		control.write(displayContext(), new TagWriter(buffer));
		Logger.info(buffer.toString(), this);
	}

	private static List<Integer> list(int e1, int e2) {
		return Arrays.asList(new Integer[] {Integer.valueOf(e1), Integer.valueOf(e2)});
	}

	private static List<Integer> range(int start, int stop) {
		ArrayList<Integer> result = new ArrayList<>(stop - start);
		
		for (int n = start; n < stop; n++) {
			result.add(Integer.valueOf(n));
		}
		
		return result;
	}

	private static <T> List<T> concat(List<? extends T> l1, List<? extends T> l2, List<? extends T> l3) {
		return concat(concat(l1, l2), l3);
	}

	private static <T> List<T> concat(List<? extends T> l1, List<? extends T> l2) {
		ArrayList<T> result = new ArrayList<>(l1.size() + l2.size());
		result.addAll(l1);
		result.addAll(l2);
		return result;
	}

	public static Test suite() {
		return suite(TestListControl.class);
	}
}
