/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.util;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.dob.util.DataObjectFiller;
import com.top_logic.dob.util.MapDataObjectFiller;

/**
 * Testcase for the {@link MapDataObjectFiller}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestMapDataObjectFiller extends TestCase {

	/**
	 * @see DataObjectFiller#getDateFormat()
	 */
	private static final DateFormat getDateFormat() {
		return CalendarUtil.newSimpleDateFormat("dd.MM.yyyy");
	}

    /**
     * Test {@link com.top_logic.dob.util.DataObjectFiller#fill(DataObject)} 
     */
	public void testFill() throws DataObjectException, ParseException {
        
		HashMap<String, Object> map1 = new HashMap<>(16);
        map1.put("t", "A String Value");
		map1.put("i", Integer.valueOf(42));
		map1.put("s", Short.valueOf((short) 22));
		map1.put("b", Byte.valueOf((byte) -3));
        map1.put("d", new java.util.Date(1234567L));
        map1.put("D", new java.sql.Date(2234567L));
		DataObject toBeFilled = new ExampleDataObject(map1);
        
		HashMap<String, Object> map2 = new HashMap<>(16);
        map2.put("t", "Another String Value");
        map2.put("i", "372");
        map2.put("s", "-17");
        map2.put("b", "88");
		String dateValue = "14.07.1965";
		map2.put("d", dateValue);
        map2.put("D", "2005-09-28");
        
        MapDataObjectFiller mdof = new MapDataObjectFiller(map2);
        mdof.fill(toBeFilled);
        
        assertEquals("Another String Value"         , toBeFilled.getAttributeValue("t"));
		assertEquals(Integer.valueOf(372), toBeFilled.getAttributeValue("i"));
		assertEquals(Short.valueOf((short) -17), toBeFilled.getAttributeValue("s"));
		assertEquals(Byte.valueOf((byte) 88), toBeFilled.getAttributeValue("b"));
        
		assertEquals(getDateFormat().parse(dateValue), toBeFilled.getAttributeValue("d"));
        assertEquals(java.sql.Date.valueOf("2005-09-28")  , toBeFilled.getAttributeValue("D"));
    }

	public void testMapValues() throws ParseException, DataObjectException {
		Map<String, Object> innerValues = new HashMap<>();
		innerValues.put("i", Integer.valueOf(1));
		innerValues.put("s", "stringValue");
		ExampleDataObject innerObj = new ExampleDataObject(innerValues);

		Map<String, Object> formerValues = new HashMap<>();
		formerValues.put("inner", innerObj);
		formerValues.put("d", getDateFormat().parse("12.02.2013"));
		ExampleDataObject toBeFilled = new ExampleDataObject(formerValues);
    	
		Map<String, Object> map = new HashMap<>();
		map.put("unknown", "attribute");
		map.put("d", "01.03.666");
		map.put("inner", new MapBuilder<String, Object>().put("i", "15").put("unknown2", "hh").toMap());
		
		new MapDataObjectFiller(map).fill(toBeFilled);
		
		assertEquals("Changed date.", getDateFormat().parse("01.03.666"), toBeFilled.getAttributeValue("d"));
		// actually should throw exception but does not.
		assertFalse("attribute".equals(toBeFilled.getAttributeValue("unknown")));
		DataObject dataObject = (DataObject) toBeFilled.getAttributeValue("inner");
		assertEquals("String value is not changed", "stringValue", dataObject.getAttributeValue("s"));
		assertEquals("Integer value was changed", Integer.valueOf(15), dataObject.getAttributeValue("i"));
    }

	public void testListValues() throws DataObjectException {
		MOStructureImpl innerType = new MOStructureImpl("inner");
		innerType.addAttribute(new MOAttributeImpl("s", MOPrimitive.STRING));
		innerType.addAttribute(new MOAttributeImpl("l", MOPrimitive.LONG));
		innerType.freeze();
		MOStructureImpl elementType = new MOStructureImpl("moName");
		elementType.addAttribute(new MOAttributeImpl("i", MOPrimitive.INTEGER));
		elementType.addAttribute(new MOAttributeImpl("inner", innerType));
		elementType.freeze();
		MOCollection listType = MOCollectionImpl.createListType(elementType);
		listType.freeze();

		DOList currentList = new DOList(listType);
		DefaultDataObject listEntry = new DefaultDataObject(elementType);
		listEntry.setAttributeValue("i", Integer.valueOf(3));
		DefaultDataObject innerEntry = new DefaultDataObject(innerType);
		listEntry.setAttributeValue("inner", innerEntry);
		innerEntry.setAttributeValue("s", "string");
		innerEntry.setAttributeValue("l", Long.valueOf(123));
		currentList.add(listEntry);
		ExampleDataObject toBeFilled = new ExampleDataObject(2);
		toBeFilled.setAttributeValue("listValue", currentList);

		Map<String, Object> fillValues = new HashMap<>();
		List<Object> listValues = new ArrayList<>();
		fillValues.put("listValue", listValues);
		listValues.add(new MapBuilder<String, Object>().put("i", "12").toMap());
		listValues.add(new MapBuilder<String, Object>().put("inner",
			new MapBuilder<String, Object>().put("s", "stringc").put("l", "666").toMap()).toMap());
		new MapDataObjectFiller(fillValues).fill(toBeFilled);
		
		Object listAttrValue = toBeFilled.getAttributeValue("listValue");
		assertInstanceof(listAttrValue, DOList.class);
		DOList list = (DOList) listAttrValue;
		assertEquals(3, list.size());
		assertEquals(listEntry, list.get(0));
		
		Object first = list.get(1);
		assertNotNull(first);
		assertNull(((DataObject)first).getAttributeValue("inner"));
		assertEquals(Integer.valueOf(12), ((DataObject)first).getAttributeValue("i"));

		Object second = list.get(2);
		assertNotNull(second);
		assertNull(((DataObject)second).getAttributeValue("i"));
		Object inner = ((DataObject)second).getAttributeValue("inner");
		assertNotNull(inner);
		assertEquals(Long.valueOf(666), ((DataObject) inner).getAttributeValue("l"));
		assertEquals("stringc", ((DataObject) inner).getAttributeValue("s"));
	}

	public void testListOfLists() throws DataObjectException {
		MOStructureImpl innerType = new MOStructureImpl("inner");
		innerType.addAttribute(new MOAttributeImpl("s", MOPrimitive.STRING));
		innerType.addAttribute(new MOAttributeImpl("l", MOPrimitive.LONG));
		innerType.freeze();
		MOCollection listType = MOCollectionImpl.createListType(innerType);
		listType.freeze();
		MOCollection listOfListsType = MOCollectionImpl.createListType(listType);
		listOfListsType.freeze();
		MOStructureImpl t = new MOStructureImpl("inner");
		t.addAttribute(new MOAttributeImpl("a", listOfListsType));
		t.freeze();

		DefaultDataObject toBeFilled = new DefaultDataObject(t);

		Map<String, Object> fillValues = new HashMap<>();
		List<Object> listValues = new ArrayList<>();
		fillValues.put("a", listValues);
		listValues.add(new ListBuilder<>().add(Collections.singletonMap("l", "-3"))
			.add(Collections.singletonMap("l", "15")).toList());
		listValues.add(new ListBuilder<>().add(Collections.singletonMap("s", "empty"))
			.add(Collections.singletonMap("s", "empty2")).add(Collections.singletonMap("s", "empty2"))
			.add(Collections.emptyMap()).toList());

		new MapDataObjectFiller(fillValues).fill(toBeFilled);

		DOList inner = (DOList) toBeFilled.getAttributeValue("a");
		assertEquals(2, inner.size());
		DOList inner1 = (DOList) inner.get(0);
		assertEquals(2, inner1.size());
		assertEquals(Long.valueOf(-3), ((DataObject) inner1.get(0)).getAttributeValue("l"));
		assertEquals(Long.valueOf(15), ((DataObject) inner1.get(1)).getAttributeValue("l"));
		DOList inner2 = (DOList) inner.get(1);
		assertEquals(4, inner2.size());
		assertEquals("empty", ((DataObject) inner2.get(0)).getAttributeValue("s"));
		assertEquals("empty2", ((DataObject) inner2.get(1)).getAttributeValue("s"));
		assertEquals("empty2", ((DataObject) inner2.get(2)).getAttributeValue("s"));
		assertEquals(null, ((DataObject) inner2.get(3)).getAttributeValue("s"));
	}

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestMapDataObjectFiller.class);
        return suite;
    }

}
