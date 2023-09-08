/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.meta;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.dob.DOBTestSetup;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.AbstractTypeSystem;
import com.top_logic.dob.meta.DefaultTypeSystem;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.meta.MOTuple;
import com.top_logic.dob.meta.MOTupleImpl;

/**
 * The class {@link TestDefaultTypeSystem} tests {@link DefaultTypeSystem}
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDefaultTypeSystem extends AbstractTypeSystemTest {
	
	/** name of {@link MOStructure} */
	private static final String STRUCT = "struct";
	/** root of {@link MetaObject} tree */
	private static final String ROOT = "root";
	/** child of {@link #ROOT} */
	private static final String _2 = "2";
	/** child of {@link #ROOT} */
	private static final String _1 = "1";
	/** child of {@link #_1} */
	private static final String _1_1 = "1.1";
	/** child of {@link #_1} */
	private static final String _1_2 = "1.2";
	/** name of {@link MOTuple} */
	private String moTuple;
	/** name of {@link MOCollection} */
	private String moColl;
	
	private Iterable<? extends MetaObject> types;
	
	@Override
	protected void tearDown() throws Exception {
		types = null;
		super.tearDown();
	}

	@Override
	protected AbstractTypeSystem createTypeSystem() {
		createTypes();
		return new DefaultTypeSystem(types);
	}

	private void createTypes() {
		ArrayList<MetaObject> tmp = new ArrayList<>();
		MOClassImpl treeRoot = new MOClassImpl(ROOT);
		add(tmp, treeRoot);

		MOClassImpl child_1 = new MOClassImpl(_1);
		child_1.setSuperclass(treeRoot);
		add(tmp, child_1);
		
		MOClassImpl child_1_1 = new MOClassImpl(_1_1);
		child_1_1.setSuperclass(child_1);
		add(tmp, child_1_1);
		
		MOClassImpl child_1_2 = new MOClassImpl(_1_2);
		child_1_2.setSuperclass(child_1);
		add(tmp, child_1_2);
		
		MOClassImpl child_2 = new MOClassImpl(_2);
		child_2.setSuperclass(treeRoot);
		add(tmp, child_2);
		
		tmp.add(MetaObject.NULL_TYPE);
		tmp.add(MetaObject.INVALID_TYPE);
		tmp.add(MetaObject.ANY_TYPE);
		tmp.add(MOPrimitive.STRING);
		
		add(tmp, new MOStructureImpl(STRUCT));
		MetaObject moTupleImpl = new MOTupleImpl(list(MOPrimitive.STRING, MetaObject.ANY_TYPE));
		this.moTuple = moTupleImpl.getName();
		add(tmp, moTupleImpl);
		MetaObject moCollImpl = MOCollectionImpl.createListType(MOPrimitive.STRING);
		this.moColl = moCollImpl.getName();
		add(tmp, moCollImpl);

		types = tmp;
	}

	private boolean add(ArrayList<MetaObject> list, MetaObject mo) {
		mo.freeze();
		return list.add(mo);
	}
	
	@Override
	public void testUnionType() throws Exception {
		super.testUnionType();
		checkUnionTypeByName(_1_1, _1_2, _1);
		checkUnionTypeByName(_1_1, _1, _1);
		checkUnionTypeByName(_1_1, _2, ROOT);
		
		checkUnionTypeByName(STRUCT, moColl, MetaObject.ANY_TYPE.getName());
		checkUnionTypeByName(STRUCT, ROOT, MetaObject.ANY_TYPE.getName());
		checkUnionTypeByName(STRUCT, moTuple, MetaObject.ANY_TYPE.getName());
		checkUnionTypeByName(moColl, moTuple, MetaObject.ANY_TYPE.getName());
		checkUnionTypeByName(moColl, ROOT, MetaObject.ANY_TYPE.getName());
		checkUnionTypeByName(moTuple, ROOT, MetaObject.ANY_TYPE.getName());
	}
	
	@Override
	public void testIntersectionType() throws Exception {
		super.testIntersectionType();
		checkIntersectionTypeByName(_1_1, _1_2, MetaObject.INVALID_TYPE.getName());
		checkIntersectionTypeByName(_1_1, _1, _1_1);
		checkIntersectionTypeByName(ROOT, _1_1, _1_1);

		checkIntersectionTypeByName(STRUCT, moColl, MetaObject.INVALID_TYPE.getName());
		checkIntersectionTypeByName(STRUCT, ROOT, MetaObject.INVALID_TYPE.getName());
		checkIntersectionTypeByName(STRUCT, moTuple, MetaObject.INVALID_TYPE.getName());
		checkIntersectionTypeByName(moColl, moTuple, MetaObject.INVALID_TYPE.getName());
		checkIntersectionTypeByName(moColl, ROOT, MetaObject.INVALID_TYPE.getName());
		checkIntersectionTypeByName(moTuple, ROOT, MetaObject.INVALID_TYPE.getName());
	}
	
	public static Test suite() {
		return DOBTestSetup.createDOBTestSetup(new TestSuite(TestDefaultTypeSystem.class));
	}
}

