/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.derived;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.Counter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.A;
import test.com.top_logic.basic.config.derived.ScenarioDerived.B;
import test.com.top_logic.basic.config.derived.ScenarioDerived.BaseConditionalPath;
import test.com.top_logic.basic.config.derived.ScenarioDerived.BaseConditionalPath.OtherWithFoo;
import test.com.top_logic.basic.config.derived.ScenarioDerived.C;
import test.com.top_logic.basic.config.derived.ScenarioDerived.DerivedUsingCollectionProps;
import test.com.top_logic.basic.config.derived.ScenarioDerived.DerivedUsingCollectionProps.Other1;
import test.com.top_logic.basic.config.derived.ScenarioDerived.DerivedUsingCollectionProps.Other2;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ExampleEnum;
import test.com.top_logic.basic.config.derived.ScenarioDerived.G;
import test.com.top_logic.basic.config.derived.ScenarioDerived.I;
import test.com.top_logic.basic.config.derived.ScenarioDerived.M;
import test.com.top_logic.basic.config.derived.ScenarioDerived.R;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeArgumentTypeMismatch;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeClass;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDate;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDefaultFromFormat;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDefaultFromValueBinding;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedClass;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedDate;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedEnum;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedEnumSubtype;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedNavigatesThroughDerived;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedObjectBoolean;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedObjectByte;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedObjectCharacter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedObjectDouble;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedObjectFloat;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedObjectInteger;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedObjectLong;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedObjectShort;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedOverridesDerivedSub;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedPrimitiveBoolean;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedPrimitiveByte;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedPrimitiveCharacter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedPrimitiveDouble;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedPrimitiveFloat;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedPrimitiveInteger;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedPrimitiveLong;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedPrimitiveShort;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedString;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedUsesDerived;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedWithInheritedSetter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDerivedWithSetter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeDirectCyclicDerive;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeEnum;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeEnumSubtype;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeExampleIndirectSubtype;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeExampleSubtype;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFromIndirectSubtype;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFromSubtype;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFromSupertype;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFromWrongType;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFunctionInstantiationFails;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFunctionMultipleParameters;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFunctionParameterFromCorrectType;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFunctionParameterFromWrongType;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFunctionParameterViaOtherItem;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeFunctionResultOfWrongType;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeIndirectCyclicDerive;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeIntProperty;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeMutualDependenciesReferenceLeft;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeMutualDependenciesReferenceRight;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeNoneExistingPropertyRef;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeNullNavigationInner;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeNullNavigationOuter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeNullNavigationPrimitiveInner;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeNullNavigationPrimitiveOuter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeNullNavigationStringInner;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeNullNavigationStringOuter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectBoolean;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectByte;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectCharacter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectDouble;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectFloat;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectFromPrimitive;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectInteger;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectLong;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectLongFromInt;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeObjectShort;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveBoolean;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveByte;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveCharacter;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveDouble;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveFloat;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveFromObject;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveInteger;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveLong;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveLongFromInt;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypePrimitiveShort;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeSimpleDerived;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeString;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeTooFewArguments;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeTooManyArguments;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeViaNonConfigItem;
import test.com.top_logic.basic.config.derived.ScenarioDerived.ScenarioTypeViaNonExistingProperty;
import test.com.top_logic.basic.config.derived.ScenarioDerived.T1;
import test.com.top_logic.basic.config.derived.ScenarioDerived.T2;
import test.com.top_logic.basic.config.derived.ScenarioDerived.T4;
import test.com.top_logic.basic.config.derived.ScenarioDerived.Y;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;

/**
 * Test case for {@link ConfigurationItem}s with {@link Derived} properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDerived extends AbstractTypedConfigurationTestCase {

	private static final String TYPE_ERROR_MARKER = "Wrong argument type";

	public void testUpdate() {
		A a = TypedConfiguration.newConfigItem(A.class);
		
		Counter counter = new Counter();
		a.addConfigurationListener(a.descriptor().getProperty(A.FOOBAR_NAME), counter);

		// Initially, there is no A.b value.
		assertEquals("undefined", a.getFooBar());
		assertEquals(0, counter.getCount());

		B b = TypedConfiguration.newConfigItem(B.class);
		a.setB(b);

		// The B.foo default is seen in A.b.
		assertEquals("foo", a.getFooBar());
		assertEquals(1, counter.getCount());

		C c = TypedConfiguration.newConfigItem(C.class);
		b.setC(c);

		// The C.bar default is seen in A.b.c.
		assertEquals("foobar", a.getFooBar());
		assertEquals(2, counter.getCount());

		c.setBar("yyy");

		// Explicitly setting a value in A.b.c.bar is reflected in A.foobar.
		assertEquals("fooyyy", a.getFooBar());
		assertEquals(3, counter.getCount());

		b.setFoo("xxx");

		// Explicitly setting a value in A.b.foo is reflected in A.foobar.
		assertEquals("xxxyyy", a.getFooBar());
		assertEquals(4, counter.getCount());

		C c2 = TypedConfiguration.newConfigItem(C.class);
		c2.setBar("zzz");
		b.setC(c2);

		// Changing a whole C in A.b.c is is reflected in A.foobar.
		assertEquals("xxxzzz", a.getFooBar());
		assertEquals(5, counter.getCount());

		B b3 = TypedConfiguration.newConfigItem(B.class);
		C c3 = TypedConfiguration.newConfigItem(C.class);
		b3.setC(c3);
		b3.setFoo("Hello ");
		c3.setBar("world!");

		// Still no change on A.
		assertEquals("xxxzzz", a.getFooBar());
		assertEquals(5, counter.getCount());

		a.setB(b3);

		// Changing a whole B in A.b is is reflected in A.foobar.
		assertEquals("Hello world!", a.getFooBar());
		assertEquals(6, counter.getCount());

		b.setFoo("dont-care");
		b.setC(null);
		c.setBar("dont-care");
		c2.setBar("dont-care");

		// Changing an unlinked part of the model does not trigger any more updates.
		assertEquals("Hello world!", a.getFooBar());
		assertEquals(6, counter.getCount());
	}

	public void testFailUpdateDerived() {
		A a = TypedConfiguration.newConfigItem(A.class);
	
		try {
			a.setFooBar("must-not-work");
			fail("Must not allow updating a derived property.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}

	public void testFailDerivedPropertyUpdatesViaReflection() {
		A a = create(A.class);
		try {
			a.update(a.descriptor().getProperty(A.FOOBAR_NAME), "must-not-work");
			fail("Must not allow updating a derived property.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}

	/**
	 * Test declaring an inherited property derived.
	 */
	public void testT1SimpleInheritance() {
		// T1.s can be normally updated.
		T1 t1 = TypedConfiguration.newConfigItem(T1.class);
		assertEquals("t1-default-s", t1.getS());

		t1.setS("t1-s");
		assertEquals("t1-s", t1.getS());
	}

	public void testT2InheritingAsDerived() {
		// T2.s can only be updated through T2.x.
		T2 t2 = TypedConfiguration.newConfigItem(T2.class);
		checkT2(t2);
	}

	public void testT4InheritingDerived() {
		// T4 should exactly behave as T2.
		T4 t4 = TypedConfiguration.newConfigItem(T4.class);
		checkT2(t4);
	}

	private void checkT2(T2 t2) {
		assertEquals("0", t2.getS());

		t2.setX(42);
		assertEquals("42", t2.getS());

		try {
			t2.setS("t2-s");
			fail("Must not allow updating derived property.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}

		assertTrue(t2.descriptor().getProperty(T2.S_NAME).isDerived());
	}

	public void testMultiple() {
		M a = TypedConfiguration.newConfigItem(M.class);

		Counter addedCounter = new Counter();
		Counter multipliedCounter = new Counter();
		a.addConfigurationListener(a.descriptor().getProperty(M.ADDED), addedCounter);
		a.addConfigurationListener(a.descriptor().getProperty(M.MULTIPLIED), multipliedCounter);

		assertEquals(0, a.getAdded());
		assertEquals(0, a.getMultiplied());
		assertEquals(0, a.getMulMinusAdd());

		assertEquals(0, addedCounter.getCount());
		assertEquals(0, multipliedCounter.getCount());

		a.setX(4);
		assertEquals(1, addedCounter.getCount());
		// Note: Since, y is still 0, the multiplication result does not change. Therefore, no event
		// must be fired.
		assertEquals(0, multipliedCounter.getCount());

		a.setY(5);
		assertEquals(2, addedCounter.getCount());
		assertEquals(1, multipliedCounter.getCount());

		assertEquals(9, a.getAdded());
		assertEquals(20, a.getMultiplied());
		assertEquals(11, a.getMulMinusAdd());

		a.setX(0);
		assertEquals(3, addedCounter.getCount());
		assertEquals(2, multipliedCounter.getCount());

		assertEquals(5, a.getAdded());
		assertEquals(0, a.getMultiplied());
		assertEquals(-5, a.getMulMinusAdd());

		a.setY(6);
		assertEquals(4, addedCounter.getCount());
		// Note: The multiplication result did not change, therefore, no event must be fired.
		assertEquals(2, multipliedCounter.getCount());

		assertEquals(6, a.getAdded());
		assertEquals(0, a.getMultiplied());
		assertEquals(-6, a.getMulMinusAdd());
	}

	public void testG() {
		G g = TypedConfiguration.newConfigItem(G.class);
		assertEquals("x", g.getFoo());
		assertEquals("xy", g.getBar());
		assertEquals("xy42", g.getFooBar());
	}
	
	public void testR() {
		R r = create(R.class);
		assertEquals("", r.getFoo());
		assertEquals("", r.getBar());
		assertEquals(null, r.getBFoo());
		assertEquals(null, r.getBCBar());

		r.setFoo("foo");
		assertEquals("foo", r.getFoo());
		assertEquals("foo", r.getBar());

		r.setB(TypedConfiguration.newConfigItem(B.class));
		assertEquals("foo", r.getBFoo());
		assertEquals(null, r.getBCBar());

		r.getB().setC(TypedConfiguration.newConfigItem(C.class));
		assertEquals("bar", r.getBCBar());

		r.getB().setFoo("bFoo");
		assertEquals("bFoo", r.getB().getFoo());
		assertEquals("bFoo", r.getBFoo());

		r.getB().getC().setBar("cBar");
		assertEquals("cBar", r.getB().getC().getBar());
		assertEquals("cBar", r.getBCBar());

		assertEquals(r, r.getSelf());
	}

	public void testY() {
		Y y = TypedConfiguration.newConfigItem(Y.class);
		assertEquals("0", y.getXString());

		assertTrue(y.descriptor().getProperty(Y.X_STRING).isDerived());
	}

	public void testI() {
		I x = TypedConfiguration.newConfigItem(I.class);
		assertEquals("z", x.getX());
		assertEquals("z", x.getZ());
	}

	public void testTooManyArguments() {
		String errorMessage = "Mismatch of argument count is not detected when too many arguments are given.";
		assertIllegal(errorMessage, "Mismatch of argument count.", ScenarioTypeTooManyArguments.class);
	}

	public void testTooFewArguments() {
		String errorMessage = "Mismatch of argument count is not detected when too few arguments are given.";
		assertIllegal(errorMessage, "Mismatch of argument count.", ScenarioTypeTooFewArguments.class);
	}

	public void testArgumentTypeMismatch() {
		String errorMessage = "Mismatching argument types are not detected when the config item is being created.";
		String errorPart = TYPE_ERROR_MARKER;
		assertIllegal(errorMessage, errorPart, ScenarioTypeArgumentTypeMismatch.class);
	}

	public void testNoneExistingPropertyRef() {
		String errorMessage = "Reference of none existing property is not detected.";
		assertIllegal(errorMessage, "Undefined property", ScenarioTypeNoneExistingPropertyRef.class);
	}

	public void testFunctionInstantiationFails() {
		String errorMessage =
			"Failing instantiation of derived property function does not cause the descriptor instantiation to fail.";
		String expectedErrorPart = "Cannot access class";
		assertIllegal(errorMessage, expectedErrorPart, ScenarioTypeFunctionInstantiationFails.class);
	}

	public void testNullNavigation() {
		ScenarioTypeNullNavigationOuter outer = create(ScenarioTypeNullNavigationOuter.class);
		assertNull(outer.getDerived()); // Initially null

		ScenarioTypeNullNavigationInner inner = create(ScenarioTypeNullNavigationInner.class);
		outer.setInner(inner);
		Object expectedValue = new Object();
		inner.setNullable(expectedValue);
		assertEquals(expectedValue, outer.getDerived());
		outer.setInner(null);
		assertNull(outer.getDerived()); // Explicitly set to null
	}

	public void testNullNavigationPrimitive() {
		ScenarioTypeNullNavigationPrimitiveOuter outer = create(ScenarioTypeNullNavigationPrimitiveOuter.class);
		assertEquals(0, outer.getDerived()); // Initially null

		ScenarioTypeNullNavigationPrimitiveInner inner = create(ScenarioTypeNullNavigationPrimitiveInner.class);
		outer.setInner(inner);
		int expectedValue = 7;
		inner.setNullable(expectedValue);
		assertEquals(expectedValue, outer.getDerived());
		outer.setInner(null);
		assertEquals(0, outer.getDerived()); // Explicitly set to null
	}

	public void testNullNavigationString() {
		ScenarioTypeNullNavigationStringOuter outer = create(ScenarioTypeNullNavigationStringOuter.class);
		assertEquals(null, outer.getDerived()); // Initially null

		ScenarioTypeNullNavigationStringInner inner = create(ScenarioTypeNullNavigationStringInner.class);
		outer.setInner(inner);
		String expectedValue = "test";
		inner.setNullable(expectedValue);
		assertEquals(expectedValue, outer.getDerived());
		outer.setInner(null);
		assertEquals(null, outer.getDerived()); // Explicitly set to null
	}

	public void testDerivedOverridesDerived() {
		ScenarioTypeDerivedOverridesDerivedSub item = create(ScenarioTypeDerivedOverridesDerivedSub.class);
		assertEquals(0, item.getExample());
		item.setFirst(-3);
		assertEquals(0, item.getExample());
		item.setSecond(-7);
		assertEquals(-7, item.getExample());
	}

	public void testDerivedUsesDerived() {
		ScenarioTypeDerivedUsesDerived item = create(ScenarioTypeDerivedUsesDerived.class);
		assertEquals(0, item.getThird());
		item.setFirst(-3);
		assertEquals(-3, item.getThird());
		item.setFirst(-7);
		assertEquals(-7, item.getThird());
	}

	public void testDerivedNavigatesThroughDerived() {
		ScenarioTypeDerivedNavigatesThroughDerived item = create(ScenarioTypeDerivedNavigatesThroughDerived.class);
		assertNull(item.getDerived());

		ScenarioTypeString otherSource = create(ScenarioTypeString.class);
		item.setOtherSource(otherSource);
		assertEquals("", item.getDerived());

		String newValue = "test value";
		otherSource.setExample(newValue);
		assertEquals(newValue, item.getDerived());
	}

	public void testDerivedIsNotWritten() {
		ScenarioTypeSimpleDerived configItem = create(ScenarioTypeSimpleDerived.class);
		configItem.setSimple(3);
		assertEquals(3, configItem.getDerived());
		String xml = write(configItem);
		if (xml.contains(ScenarioTypeSimpleDerived.DERIVED)) {
			fail("Derived property values must not be serialized, but are.");
		}
	}

	public void testReadDerivedFails() {
		String xml = "<ScenarioTypeSimpleDerived simple='3' derived='5' />";
		String expectedMessage = "Derived property 'derived' .* cannot be configured";
		String message = "Reading an explicit value for a derived property has to fail with message: >>>"
			+ expectedMessage + "<<<";
		Pattern expectedMessagePattern = Pattern.compile(expectedMessage);
		assertIllegalXml(message, xml, expectedMessagePattern, ScenarioTypeSimpleDerived.class);
	}

	public void testDerivedWithSetter() {
		ScenarioTypeDerivedWithSetter item = create(ScenarioTypeDerivedWithSetter.class);
		try {
			item.setDerived(3);
		} catch (UnsupportedOperationException ex) {
			return;
		}
		fail("Setting a derived property has to fail with an UnsupportedOperationException, but does not.");
	}

	public void testDerivedWithInheritedSetter() {
		ScenarioTypeDerivedWithInheritedSetter item = create(ScenarioTypeDerivedWithInheritedSetter.class);
		try {
			item.setDerived(3);
		} catch (UnsupportedOperationException ex) {
			return;
		}
		fail("Setting a derived property has to fail with an UnsupportedOperationException, but does not.");
	}

	public void testDirectCyclicDerived() {
		ScenarioTypeDirectCyclicDerive item = create(ScenarioTypeDirectCyclicDerive.class);
		assertEquals(0, item.getDerived());
	}

	public void testIndirectCyclicDerived() {
		ScenarioTypeIndirectCyclicDerive item = create(ScenarioTypeIndirectCyclicDerive.class);
		assertEquals(0, item.getLeft());
	}

	public void testNavigationCausesNullOnPrimitiveBoolean() {
		ScenarioTypeDerivedPrimitiveBoolean item = create(ScenarioTypeDerivedPrimitiveBoolean.class);
		assert item.getSource() == null;
		assertEquals(false, item.getDerived());
		ScenarioTypePrimitiveBoolean source = create(ScenarioTypePrimitiveBoolean.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		boolean newValue = true;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnPrimitiveCharacter() {
		ScenarioTypeDerivedPrimitiveCharacter item = create(ScenarioTypeDerivedPrimitiveCharacter.class);
		assert item.getSource() == null;
		assertEquals((char) 0, item.getDerived());
		ScenarioTypePrimitiveCharacter source = create(ScenarioTypePrimitiveCharacter.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		char newValue = (char) 1;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnPrimitiveByte() {
		ScenarioTypeDerivedPrimitiveByte item = create(ScenarioTypeDerivedPrimitiveByte.class);
		assert item.getSource() == null;
		assertEquals((byte) 0, item.getDerived());
		ScenarioTypePrimitiveByte source = create(ScenarioTypePrimitiveByte.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		byte newValue = (byte) 1;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnPrimitiveShort() {
		ScenarioTypeDerivedPrimitiveShort item = create(ScenarioTypeDerivedPrimitiveShort.class);
		assert item.getSource() == null;
		assertEquals((short) 0, item.getDerived());
		ScenarioTypePrimitiveShort source = create(ScenarioTypePrimitiveShort.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		short newValue = (short) 1;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnPrimitiveInteger() {
		ScenarioTypeDerivedPrimitiveInteger item = create(ScenarioTypeDerivedPrimitiveInteger.class);
		assert item.getSource() == null;
		assertEquals(0, item.getDerived());
		ScenarioTypePrimitiveInteger source = create(ScenarioTypePrimitiveInteger.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		int newValue = 1;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnPrimitiveLong() {
		ScenarioTypeDerivedPrimitiveLong item = create(ScenarioTypeDerivedPrimitiveLong.class);
		assert item.getSource() == null;
		assertEquals(0L, item.getDerived());
		ScenarioTypePrimitiveLong source = create(ScenarioTypePrimitiveLong.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		long newValue = 1L;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnPrimitiveFloat() {
		ScenarioTypeDerivedPrimitiveFloat item = create(ScenarioTypeDerivedPrimitiveFloat.class);
		assert item.getSource() == null;
		assertEquals(0f, item.getDerived());
		ScenarioTypePrimitiveFloat source = create(ScenarioTypePrimitiveFloat.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		float newValue = 1f;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnPrimitiveDouble() {
		ScenarioTypeDerivedPrimitiveDouble item = create(ScenarioTypeDerivedPrimitiveDouble.class);
		assert item.getSource() == null;
		assertEquals(0d, item.getDerived());
		ScenarioTypePrimitiveDouble source = create(ScenarioTypePrimitiveDouble.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		double newValue = 1d;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnObjectBoolean() {
		ScenarioTypeDerivedObjectBoolean item = create(ScenarioTypeDerivedObjectBoolean.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeObjectBoolean source = create(ScenarioTypeObjectBoolean.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Boolean newValue = true;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnObjectCharacter() {
		ScenarioTypeDerivedObjectCharacter item = create(ScenarioTypeDerivedObjectCharacter.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeObjectCharacter source = create(ScenarioTypeObjectCharacter.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Character newValue = (char) 1;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnObjectByte() {
		ScenarioTypeDerivedObjectByte item = create(ScenarioTypeDerivedObjectByte.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeObjectByte source = create(ScenarioTypeObjectByte.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Byte newValue = (byte) 1;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnObjectShort() {
		ScenarioTypeDerivedObjectShort item = create(ScenarioTypeDerivedObjectShort.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeObjectShort source = create(ScenarioTypeObjectShort.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Short newValue = (short) 1;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnObjectInteger() {
		ScenarioTypeDerivedObjectInteger item = create(ScenarioTypeDerivedObjectInteger.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeObjectInteger source = create(ScenarioTypeObjectInteger.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Integer newValue = 1;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnObjectLong() {
		ScenarioTypeDerivedObjectLong item = create(ScenarioTypeDerivedObjectLong.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeObjectLong source = create(ScenarioTypeObjectLong.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Long newValue = 1L;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnObjectFloat() {
		ScenarioTypeDerivedObjectFloat item = create(ScenarioTypeDerivedObjectFloat.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeObjectFloat source = create(ScenarioTypeObjectFloat.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Float newValue = 1f;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigationCausesNullOnObjectDouble() {
		ScenarioTypeDerivedObjectDouble item = create(ScenarioTypeDerivedObjectDouble.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeObjectDouble source = create(ScenarioTypeObjectDouble.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Double newValue = 1d;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigateCausesNullOnString() {
		ScenarioTypeDerivedString item = create(ScenarioTypeDerivedString.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeString source = create(ScenarioTypeString.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		String newValue = "Hello World";
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigateCausesNullOnClass() {
		ScenarioTypeDerivedClass item = create(ScenarioTypeDerivedClass.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeClass source = create(ScenarioTypeClass.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Class<?> newValue = String.class;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigateCausesNullOnDate() {
		ScenarioTypeDerivedDate item = create(ScenarioTypeDerivedDate.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeDate source = create(ScenarioTypeDate.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Date newValue = new Date();
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigateCausesNullOnEnum() {
		ScenarioTypeDerivedEnum item = create(ScenarioTypeDerivedEnum.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeEnum source = create(ScenarioTypeEnum.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		Enum<?> newValue = ExampleEnum.SECOND;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	public void testNavigateCausesNullOnEnumSubtype() {
		ScenarioTypeDerivedEnumSubtype item = create(ScenarioTypeDerivedEnumSubtype.class);
		assert item.getSource() == null;
		assertEquals(null, item.getDerived());
		ScenarioTypeEnumSubtype source = create(ScenarioTypeEnumSubtype.class);
		// Cross-check whether it is actually derived and uses the source, i.e. test the test
		ExampleEnum newValue = ExampleEnum.SECOND;
		source.setExample(newValue);
		item.setSource(source);
		assertEquals(newValue, item.getDerived());
	}

	// Test the type-checking of the paths:

	public void testPrimitiveFromObject() {
		String message = "A derived property of a primitive type getting its value from "
			+ "a property of the corresponding object type is an error and has to fail.";
		String errorPart = TYPE_ERROR_MARKER;
		assertIllegal(message, errorPart, ScenarioTypePrimitiveFromObject.class);
	}

	public void testObjectFromPrimitive() {
		ScenarioTypeObjectFromPrimitive item = create(ScenarioTypeObjectFromPrimitive.class);
		assertEquals(Integer.valueOf(0), item.getDerived());
		Integer newValue = 1;
		setValue(item, ScenarioTypeObjectFromPrimitive.SOURCE, newValue);
		assertEquals(newValue, item.getDerived());
	}

	public void testFromUnrelatedType() {
		String message = "Expected a type error, as the derived property is getting its value from "
			+ "a property of an unrelated type.";
		String errorPart = TYPE_ERROR_MARKER;
		assertIllegal(message, errorPart, ScenarioTypeFromWrongType.class);
	}

	public void testFromSupertype() {
		String message = "Expected a type error, as the derived property is getting its value from "
			+ "a property of a supertype of its own.";
		String errorPart = TYPE_ERROR_MARKER;
		assertIllegal(message, errorPart, ScenarioTypeFromSupertype.class);
	}

	public void testFromSubtype() {
		ScenarioTypeFromSubtype item = create(ScenarioTypeFromSubtype.class);
		assertNull(item.getDerived());
		ScenarioTypeExampleSubtype newValue = create(ScenarioTypeExampleSubtype.class);
		setValue(item, ScenarioTypeFromSubtype.SOURCE, newValue);
		assertEquals(newValue, item.getDerived());
	}

	public void testFromIndirectSubtype() {
		ScenarioTypeFromIndirectSubtype item = create(ScenarioTypeFromIndirectSubtype.class);
		assertNull(item.getDerived());
		ScenarioTypeExampleIndirectSubtype newValue = create(ScenarioTypeExampleIndirectSubtype.class);
		setValue(item, ScenarioTypeFromIndirectSubtype.SOURCE, newValue);
		assertEquals(newValue, item.getDerived());
	}

	public void testViaNonConfigItem() {
		String message = "Expected an error, as the derived property is navigating "
			+ "through something that is not a ConfigItem.";
		String errorPart = "Undefined property 'i-dont-exist'";
		assertIllegal(message, errorPart, ScenarioTypeViaNonConfigItem.class);
	}

	public void testViaNonExistingProperty() {
		String message = "Expected a type error, as the derived property is navigating "
			+ "through a non-existing property.";
		String errorPart = "Undefined property";
		assertIllegal(message, errorPart, ScenarioTypeViaNonExistingProperty.class);
	}

	public void testMutualDependenciesReference() {
		ScenarioTypeMutualDependenciesReferenceLeft left = create(ScenarioTypeMutualDependenciesReferenceLeft.class);
		assertNull(left.getDerived());
		ScenarioTypeMutualDependenciesReferenceRight right = create(ScenarioTypeMutualDependenciesReferenceRight.class);
		String valueRight = "value right";
		right.setExample(valueRight);
		left.setSource(right);
		assertEquals(valueRight, left.getDerived());
		assertNull(right.getDerived());
		String valueLeft = "value left";
		left.setExample(valueLeft);
		right.setSource(left);
		assertEquals(valueLeft, right.getDerived());
	}

	public void testFunctionResultOfWrongType() {
		String message = "Expected a type error as the function result is of the wrong type.";
		String errorPart = "Wrong result type";
		assertIllegal(message, errorPart, ScenarioTypeFunctionResultOfWrongType.class);
	}

	public void testFunctionParameterFromWrongType() {
		String message = "Expected a type error as the function parameter navigates to a property of the wrong type.";
		String errorPart = TYPE_ERROR_MARKER;
		assertIllegal(message, errorPart, ScenarioTypeFunctionParameterFromWrongType.class);
	}

	public void testFunctionParameterFromCorrectType() {
		ScenarioTypeFunctionParameterFromCorrectType item = create(ScenarioTypeFunctionParameterFromCorrectType.class);
		assertEquals("0", item.getDerived());
		item.setSource(1);
		assertEquals("1", item.getDerived());
	}

	public void testFunctionParameterViaOtherItem() {
		ScenarioTypeFunctionParameterViaOtherItem item = create(ScenarioTypeFunctionParameterViaOtherItem.class);
		assertEquals("", item.getDerived());
		ScenarioTypeIntProperty source = create(ScenarioTypeIntProperty.class);
		source.setExample(1);
		item.setSource(source);
		assertEquals("1", item.getDerived());
	}

	public void testFunctionMultipleParameters() {
		ScenarioTypeFunctionMultipleParameters item = create(ScenarioTypeFunctionMultipleParameters.class);
		item.setSourceA(1);
		item.setSourceB(1);
		assertEquals(2, item.getDerived());
	}

	/**
	 * Documentary example of the java primitive type compatibility.
	 * <p>
	 * Used for the following tests to check what is allowed in Java <b>without casts</b> and what
	 * not.
	 * </p>
	 */
	@SuppressWarnings("unused")
	private void primitiveTypeCompatibility() {
//		byte b1 = Byte.valueOf("");
//		byte b2 = Short.valueOf("");
//		byte b3 = Integer.valueOf("");
//		byte b4 = Long.valueOf("");
//		byte b5 = Character.valueOf(' ');
//		byte b6 = Float.valueOf("");
//		byte b7 = Double.valueOf("");
//
//		short s1 = Byte.valueOf("");
//		short s2 = Short.valueOf("");
//		short s3 = Integer.valueOf("");
//		short s4 = Long.valueOf("");
//		short s5 = Character.valueOf(' ');
//		short s6 = Float.valueOf("");
//		short s7 = Double.valueOf("");
//
//		int i1 = Byte.valueOf("");
//		int i2 = Short.valueOf("");
//		int i3 = Integer.valueOf("");
//		int i4 = Long.valueOf("");
//		int i5 = Character.valueOf(' ');
//		int i6 = Float.valueOf("");
//		int i7 = Double.valueOf("");
//
//		long l1 = Byte.valueOf("");
//		long l2 = Short.valueOf("");
//		long l3 = Integer.valueOf("");
//		long l4 = Long.valueOf("");
//		long l5 = Character.valueOf(' ');
//		long l6 = Float.valueOf("");
//		long l7 = Double.valueOf("");
//
//		char c1 = Byte.valueOf("");
//		char c2 = Short.valueOf("");
//		char c3 = Integer.valueOf("");
//		char c4 = Long.valueOf("");
//		char c5 = Character.valueOf(' ');
//		char c6 = Float.valueOf("");
//		char c7 = Double.valueOf("");
//
//		float f1 = Byte.valueOf("");
//		float f2 = Short.valueOf("");
//		float f3 = Integer.valueOf("");
//		float f4 = Long.valueOf("");
//		float f5 = Character.valueOf(' ');
//		float f6 = Float.valueOf("");
//		float f7 = Double.valueOf("");
//
//		double d1 = Byte.valueOf("");
//		double d2 = Short.valueOf("");
//		double d3 = Integer.valueOf("");
//		double d4 = Long.valueOf("");
//		double d5 = Character.valueOf(' ');
//		double d6 = Float.valueOf("");
//		double d7 = Double.valueOf("");
	}

	public void testPrimitiveLongFromInt() {
		ScenarioTypePrimitiveLongFromInt item = create(ScenarioTypePrimitiveLongFromInt.class);
		assertEquals(0L, item.getDerived());

		item.setSource(1);
		assertEquals(1L, item.getDerived());
	}

	public void testObjectLongFromInt() {
		String message = "Expected a type error as a non-primitive "
			+ "long tries to get its value from a non-primitive int, which is forbidden in Java.";
		assertIllegal(message, TYPE_ERROR_MARKER, ScenarioTypeObjectLongFromInt.class);
	}

	public void testDefaultFromFormat() {
		ScenarioTypeDefaultFromFormat item = create(ScenarioTypeDefaultFromFormat.class);
		assertNull(item.getSource());
		assertEquals(Integer.valueOf(42), item.getDerived());
	}

	public void testDefaultFromValueBinding() {
		ScenarioTypeDefaultFromValueBinding item = create(ScenarioTypeDefaultFromValueBinding.class);
		assertEquals(Collections.emptyList(), item.getDerived());
	}

	public void testConditionalPath() {
		BaseConditionalPath item = create(BaseConditionalPath.class);
		assertNull(item.getConditionalFoo());

		OtherWithFoo otherWithFoo = create(BaseConditionalPath.OtherWithFoo.class);
		otherWithFoo.setFoo("initial-value");

		item.setOther(otherWithFoo);
		assertEquals("initial-value", item.getConditionalFoo());

		otherWithFoo.setFoo("new-value");
		assertEquals("new-value", item.getConditionalFoo());

		item.setOther(create(BaseConditionalPath.OtherWithoutFoo.class));
		assertNull(item.getConditionalFoo());

		item.setOther(null);
		assertNull(item.getConditionalFoo());
	}

	public void testDerivedNavigationUsingCollections() {
		DerivedUsingCollectionProps item = create(DerivedUsingCollectionProps.class);
		assertEquals(0, item.getSum());
		item.setOther(create(Other1.class));
		assertEquals(0, item.getSum());
		item.getOther().setValue(1);
		assertEquals(1, item.getSum());
		item.getOthersList().add(create(Other1.class));
		assertEquals(1, item.getSum());
		item.getOthersList().get(0).setValue(2);
		assertEquals(3, item.getSum());
		Other1 o1 = create(Other1.class);
		o1.setValue(1);
		item.getOthersList().add(o1);
		assertEquals(4, item.getSum());

		Other2 o2 = create(Other2.class);
		o2.setName("a");
		item.getOthersMap().put(o2.getName(), o2);
		assertEquals(4, item.getSum());
		o2.getOther1().add(create(Other1.class));
		assertEquals(4, item.getSum());
		o2.getOther1().get(0).setValue(1);
		assertEquals(5, item.getSum());

		Other2 o3 = create(Other2.class);
		o3.setName("b");
		o3.getOther1().add(create(Other1.class));
		o3.getOther1().get(0).setValue(1);
		item.getOthersMap().put(o3.getName(), o3);
		assertEquals(6, item.getSum());

		Other2 o4 = create(Other2.class);
		o4.setName("b");
		o4.getOther1().add(create(Other1.class));
		o4.getOther1().add(create(Other1.class));
		o4.getOther1().get(0).setValue(2);
		item.getOthersMap().put(o4.getName(), o4);
		o4.getOther1().get(1).setValue(4);
		assertEquals(11, item.getSum());

		Other1[] o5 = new Other1[] { create(Other1.class), create(Other1.class) };
		item.setOthersArray(o5);
		assertEquals(11, item.getSum());
		item.getOthersArray()[0].setValue(1);
		assertEquals(12, item.getSum());
		item.getOthersArray()[1].setValue(2);
		assertEquals(14, item.getSum());

		assertNotNull(item.getOthersMap().remove("b"));
		assertEquals(8, item.getSum());
		assertEquals(2, item.getOthersList().remove(0).getValue());
		assertEquals(6, item.getSum());
	}

	private static String write(ConfigurationItem config) {
		try {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Class<? extends ConfigurationItem> configItemClass = (Class) config.getConfigurationInterface();
			String rootTag = configItemClass.getSimpleName();
			StringWriter buffer = new StringWriter();
			new ConfigurationWriter(buffer).write(rootTag, configItemClass, config);
			return buffer.toString();
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestDerived.class);
	}

}
