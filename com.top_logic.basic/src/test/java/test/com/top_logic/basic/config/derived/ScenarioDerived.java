/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.derived;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.func.Function4;
import com.top_logic.basic.func.GenericFunction;

/**
 * Container interfaces for testing the {@link Derived}, {@link DerivedRef} and {@link Ref}
 * annotations and the corresponding features of the typed configuration.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioDerived {

	public enum ExampleEnum {
		FIRST, SECOND
	}

	public interface A extends ConfigurationItem {
		String B_NAME = "b";
	
		String FOOBAR_NAME = "foo-bar";
	
		@Name(B_NAME)
		B getB();
	
		@Name(FOOBAR_NAME)
		@Derived(fun = FooBar.class, args = {
			@Ref({ B_NAME, B.FOO_NAME }),
			@Ref({ B_NAME, B.C_NAME, C.BAR_NAME }) })
		String getFooBar();
	
		class FooBar extends Function2<String, String, String> {
			@Override
			public String apply(String arg1, String arg2) {
				if (arg1 == null) {
					return "undefined";
				}
				if (arg2 == null) {
					return arg1;
				}
				return arg1 + arg2;
			}
	
		}
	
		void setB(B value);
	
		void setFooBar(String value);
	}

	public interface B extends ConfigurationItem {
		String C_NAME = "c";
	
		String FOO_NAME = "foo";
	
		@Name(FOO_NAME)
		@StringDefault("foo")
		String getFoo();
	
		@Name(C_NAME)
		C getC();
	
		void setC(C value);
	
		void setFoo(String value);
	
	}

	public interface C extends ConfigurationItem {
		String BAR_NAME = "bar";
	
		@Name(BAR_NAME)
		@StringDefault("bar")
		String getBar();
	
		void setBar(String value);
	}

	/**
	 * Base interface for inheritance tests.
	 */
	public interface S extends ConfigurationItem {
	
		public static final String S_NAME = "s";
	
		@Name(S_NAME)
		@Mandatory
		String getS();
	
		void setS(String value);
	
	}

	/**
	 * Inheriting a property and declaring a default.
	 */
	public interface T1 extends S {
	
		@Override
		@StringDefault("t1-default-s")
		String getS();
	
	}

	/**
	 * Inheriting a property and declaring it derived.
	 */
	public interface T2 extends S {
	
		String X = "x";
	
		@Name(X)
		int getX();
	
		void setX(int value);
	
		@Override
		@Derived(fun = IntegerToString.class, args = @Ref(X))
		String getS();
	}

	/**
	 * Simply inherited derived property.
	 */
	public interface T4 extends T2 {
		int getY();
	
		void setY(int value);
	}

	/**
	 * Test case for {@link ConfigurationItem}s with multiple {@link Derived} properties that depend
	 * on each other.
	 */
	public interface M extends ConfigurationItem {
		String X = "x";
	
		String Y = "y";
	
		String ADDED = "added";
	
		String MULTIPLIED = "multiplied";
	
		@Name(X)
		int getX();
	
		@Name(Y)
		int getY();
	
		@Name(ADDED)
		@Derived(fun = Add.class, args = { @Ref(X), @Ref(Y) })
		int getAdded();
	
		@Name(MULTIPLIED)
		@Derived(fun = Mul.class, args = { @Ref(X), @Ref(Y) })
		int getMultiplied();
	
		@Derived(fun = Sub.class, args = { @Ref(MULTIPLIED), @Ref(ADDED) })
		int getMulMinusAdd();
	
		class Add extends Function2<Integer, Integer, Integer> {
			@Override
			public Integer apply(Integer arg1, Integer arg2) {
				return arg1 + arg2;
			}
		}
	
		class Mul extends Function2<Integer, Integer, Integer> {
			@Override
			public Integer apply(Integer arg1, Integer arg2) {
				return arg1 * arg2;
			}
		}
	
		class Sub extends Function2<Integer, Integer, Integer> {
			@Override
			public Integer apply(Integer arg1, Integer arg2) {
				return arg1 - arg2;
			}
		}
	
		void setX(int value);
	
		void setY(int value);
	}

	/**
	 * Derived properties using a {@link GenericFunction} with a variable argument list.
	 */
	public interface G extends ConfigurationItem {
		
		String X = "x";
		String Y = "y";
		String Z = "Z";
	
		@Derived(fun=Concat.class, args= {@Ref(X)})
		public String getFoo();
		
		@Derived(fun=Concat.class, args= {@Ref(X), @Ref(Y)})
		public String getBar();
		
		@Derived(fun=Concat.class, args= {@Ref(X), @Ref(Y), @Ref(Z)})
		public String getFooBar();
		
		@Name(X)
		@StringDefault("x")
		String getX();
		
		@Name(Y)
		@StringDefault("y")
		String getY();
		
		@Name(Z)
		@IntDefault(42)
		int getZ();
		
		class Concat extends GenericFunction<String> {
	
			@Override
			public String invoke(Object... args) {
				StringBuilder buffer = new StringBuilder();
				for (Object arg : args) {
					if (arg != null) {
						buffer.append(arg);
					}
				}
				return buffer.toString();
			}
	
			@Override
			public int getArgumentCount() {
				return 0;
			}
			
			@Override
			public final boolean hasVarArgs() {
				return true;
			}

		}
		
	}

	/**
	 * Derived properties using {@link DerivedRef}.
	 */
	public interface R extends ConfigurationItem {
	
		String FOO = "foo";
	
		String B_NAME = "b";
	
		@Name(FOO)
		String getFoo();
	
		@DerivedRef(FOO)
		String getBar();
	
		@Name(B_NAME)
		B getB();
	
		@DerivedRef({ B_NAME, B.FOO_NAME })
		String getBFoo();
	
		@DerivedRef({ B_NAME, B.C_NAME, C.BAR_NAME })
		String getBCBar();
	
		void setFoo(String value);
	
		void setB(B value);

		@DerivedRef({})
		R getSelf();
	
	}

	@Abstract
	public interface X extends ConfigurationItem {
	
		String X = "x";
	
		String X_STRING = "x-string";
	
		@Name(X)
		int getX();
	
		@Name(X_STRING)
		@Derived(fun = IntegerToString.class, args = @Ref(X))
		String getXString();
	
	}

	public interface Y extends X {
		// Concrete version of X. Ensure that X any Y are loaded at once.
	}

	/**
	 * Implements abstract property inherited from I2 through implementation inherited from I3.
	 */
	public interface I extends I2, I3 {
		// Pure sum interface.
	}

	@Abstract
	public interface I1 extends ConfigurationItem {
		@Abstract
		String getX();
	}

	@Abstract
	public interface I2 extends I1 {
		String Y = "y";
	
		@Name(Y)
		String getY();
	}

	public interface I3 extends I1 {
		String Z = "z";
	
		@Name(Z)
		@StringDefault("z")
		String getZ();
	
		@DerivedRef(Z)
		@Override
		public String getX();
	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeTooManyArguments extends ConfigurationItem {
		// Mismatch of argument count.
		@Derived(fun = IsFoo.class, args = { @Ref("a"), @Ref("b") })
		boolean getV();
	
		String getA();
	
		String getB();
	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeTooFewArguments extends ConfigurationItem {
		// Mismatch of argument count.
		@Derived(fun = IsFoo.class, args = {})
		boolean getV();
	
		String getA();
	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeArgumentTypeMismatch extends ConfigurationItem {
		String X_NAME = "x";
	
		// Mismatch of argument type.
		@Derived(fun = IsFoo.class, args = { @Ref({ X_NAME }) })
		boolean getV();
	
		@Name(X_NAME)
		int getX();
	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeNoneExistingPropertyRef extends ConfigurationItem {
		// Invalid property reference.
		@Derived(fun = IsFoo.class, args = { @Ref({ "b" }) })
		boolean getV();
	
		String getA();
	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeFunctionInstantiationFails extends ConfigurationItem {
		String A_NAME = "a";
	
		@Derived(fun = CannotInstantiate.class, args = { @Ref({ A_NAME }) })
		boolean getV();
	
		@Name(A_NAME)
		String getA();
	
		public class CannotInstantiate extends Function1<Boolean, Object> {
			private CannotInstantiate() {
				// Cannot be instantiated.
			}
	
			@Override
			public Boolean apply(Object arg) {
				return true;
			}
		}
	}

	public static class IsFoo extends Function1<Boolean, String> {
		@Override
		public Boolean apply(String arg) {
			return "foo".equals(arg);
		}
	}

	public static class IntegerToString extends Function1<String, Integer> {
		@Override
		public String apply(Integer arg) {
			if (arg == null) {
				return "";
			}
			return Integer.toString(arg);
		}
	}

	public interface ScenarioTypeNullNavigationInner extends ConfigurationItem {

		public static final String NULLABLE = "nullable";

		@InstanceFormat
		@Name(NULLABLE)
		@Subtypes({})
		Object getNullable();

		void setNullable(Object value);

	}

	public interface ScenarioTypeNullNavigationOuter extends ConfigurationItem {

		public static final String INNER = "inner";

		@Name(INNER)
		ScenarioTypeNullNavigationInner getInner();

		void setInner(ScenarioTypeNullNavigationInner value);

		@DerivedRef({ INNER, ScenarioTypeNullNavigationInner.NULLABLE })
		Object getDerived();
	}

	public interface ScenarioTypeNullNavigationPrimitiveInner extends ConfigurationItem {

		public static final String NULLABLE = "nullable";

		@Name(NULLABLE)
		int getNullable();

		void setNullable(int value);

	}

	public interface ScenarioTypeNullNavigationPrimitiveOuter extends ConfigurationItem {

		public static final String INNER = "inner";

		@Name(INNER)
		ScenarioTypeNullNavigationPrimitiveInner getInner();

		void setInner(ScenarioTypeNullNavigationPrimitiveInner value);

		@DerivedRef({ INNER, ScenarioTypeNullNavigationPrimitiveInner.NULLABLE })
		int getDerived();
	}

	public interface ScenarioTypeNullNavigationStringInner extends ConfigurationItem {

		public static final String NULLABLE = "nullable";

		@Name(NULLABLE)
		String getNullable();

		void setNullable(String value);

	}

	public interface ScenarioTypeNullNavigationStringOuter extends ConfigurationItem {

		public static final String INNER = "inner";

		@Name(INNER)
		ScenarioTypeNullNavigationStringInner getInner();

		void setInner(ScenarioTypeNullNavigationStringInner value);

		@DerivedRef({ INNER, ScenarioTypeNullNavigationStringInner.NULLABLE })
		String getDerived();
	}

	public interface ScenarioTypeDerivedOverridesDerivedSuper extends ConfigurationItem {

		String EXAMPLE = "example";

		String FIRST = "first";

		@DerivedRef(FIRST)
		@Name(EXAMPLE)
		int getExample();

		@Name(FIRST)
		int getFirst();

		void setFirst(int value);

	}

	public interface ScenarioTypeDerivedOverridesDerivedSub extends ScenarioTypeDerivedOverridesDerivedSuper {

		String SECOND = "second";

		@DerivedRef(SECOND)
		@Override
		int getExample();

		@Name(SECOND)
		int getSecond();

		void setSecond(int value);

	}

	public interface ScenarioTypeDerivedUsesDerived extends ConfigurationItem {

		String FIRST = "first";

		String SECOND = "second";

		@Name(FIRST)
		int getFirst();

		void setFirst(int value);

		@Name(SECOND)
		@DerivedRef(FIRST)
		int getSecond();

		@DerivedRef(SECOND)
		int getThird();

	}

	public interface ScenarioTypeDerivedNavigatesThroughDerived extends ConfigurationItem {

		String DERIVED = "derived";

		String OTHER_DERIVED = "other-derived";

		String OTHER_SOURCE = "other-source";

		@Name(DERIVED)
		@DerivedRef({ OTHER_DERIVED, ScenarioTypeString.EXAMPLE })
		String getDerived();

		@Name(OTHER_DERIVED)
		@DerivedRef(OTHER_SOURCE)
		ScenarioTypeString getOtherDerived();

		@Name(OTHER_SOURCE)
		ScenarioTypeString getOtherSource();

		void setOtherSource(ScenarioTypeString otherSource);

	}

	public interface ScenarioTypeSimpleDerived extends ConfigurationItem {

		String SIMPLE = "simple";

		String DERIVED = "derived";

		@Name(SIMPLE)
		int getSimple();

		void setSimple(int value);

		@Name(DERIVED)
		@DerivedRef(SIMPLE)
		int getDerived();

	}

	public interface ScenarioTypeDerivedWithSetter extends ConfigurationItem {

		String SIMPLE = "simple";

		String DERIVED = "derived";

		@Name(SIMPLE)
		int getSimple();

		void setSimple(int value);

		@Name(DERIVED)
		@DerivedRef(SIMPLE)
		int getDerived();

		void setDerived(int value);

	}

	public interface ScenarioTypeDerivedWithInheritedSetterParent extends ConfigurationItem {

		String SIMPLE = "simple";

		String DERIVED = "derived";

		@Name(SIMPLE)
		int getSimple();

		void setSimple(int value);

		@Name(DERIVED)
		int getDerived();

		void setDerived(int value);

	}

	public interface ScenarioTypeDerivedWithInheritedSetter extends ScenarioTypeDerivedWithInheritedSetterParent {

		@Override
		@DerivedRef(SIMPLE)
		int getDerived();

	}

	public interface ScenarioTypeDirectCyclicDerive extends ConfigurationItem {

		String DERIVED = "derived";

		@Name(DERIVED)
		@DerivedRef(DERIVED)
		int getDerived();

	}

	public interface ScenarioTypeIndirectCyclicDerive extends ConfigurationItem {

		String LEFT = "left";

		String RIGHT = "right";

		@Name(LEFT)
		@DerivedRef(RIGHT)
		int getLeft();

		@Name(RIGHT)
		@DerivedRef(LEFT)
		int getRight();

	}

	public interface ScenarioTypePrimitiveBoolean extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		boolean getExample();

		void setExample(boolean value);

	}

	public interface ScenarioTypeDerivedPrimitiveBoolean extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypePrimitiveBoolean getSource();

		void setSource(ScenarioTypePrimitiveBoolean source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypePrimitiveBoolean.EXAMPLE })
		boolean getDerived();

	}

	public interface ScenarioTypePrimitiveCharacter extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		char getExample();

		void setExample(char value);

	}

	public interface ScenarioTypeDerivedPrimitiveCharacter extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypePrimitiveCharacter getSource();

		void setSource(ScenarioTypePrimitiveCharacter source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypePrimitiveCharacter.EXAMPLE })
		char getDerived();

	}

	public interface ScenarioTypePrimitiveByte extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		byte getExample();

		void setExample(byte value);

	}

	public interface ScenarioTypeDerivedPrimitiveByte extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypePrimitiveByte getSource();

		void setSource(ScenarioTypePrimitiveByte source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypePrimitiveByte.EXAMPLE })
		byte getDerived();

	}

	public interface ScenarioTypePrimitiveShort extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		short getExample();

		void setExample(short value);

	}

	public interface ScenarioTypeDerivedPrimitiveShort extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypePrimitiveShort getSource();

		void setSource(ScenarioTypePrimitiveShort source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypePrimitiveShort.EXAMPLE })
		short getDerived();

	}

	public interface ScenarioTypePrimitiveInteger extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		int getExample();

		void setExample(int value);

	}

	public interface ScenarioTypeDerivedPrimitiveInteger extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypePrimitiveInteger getSource();

		void setSource(ScenarioTypePrimitiveInteger source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypePrimitiveInteger.EXAMPLE })
		int getDerived();

	}

	public interface ScenarioTypePrimitiveLong extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		long getExample();

		void setExample(long value);

	}

	public interface ScenarioTypeDerivedPrimitiveLong extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypePrimitiveLong getSource();

		void setSource(ScenarioTypePrimitiveLong source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypePrimitiveLong.EXAMPLE })
		long getDerived();

	}

	public interface ScenarioTypePrimitiveFloat extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		float getExample();

		void setExample(float value);

	}

	public interface ScenarioTypeDerivedPrimitiveFloat extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypePrimitiveFloat getSource();

		void setSource(ScenarioTypePrimitiveFloat source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypePrimitiveFloat.EXAMPLE })
		float getDerived();

	}

	public interface ScenarioTypePrimitiveDouble extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		double getExample();

		void setExample(double value);

	}

	public interface ScenarioTypeDerivedPrimitiveDouble extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypePrimitiveDouble getSource();

		void setSource(ScenarioTypePrimitiveDouble source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypePrimitiveDouble.EXAMPLE })
		double getDerived();

	}

	public interface ScenarioTypeObjectBoolean extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Boolean getExample();

		void setExample(Boolean value);

	}

	public interface ScenarioTypeDerivedObjectBoolean extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeObjectBoolean getSource();

		void setSource(ScenarioTypeObjectBoolean source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeObjectBoolean.EXAMPLE })
		Boolean getDerived();

	}

	public interface ScenarioTypeObjectCharacter extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Character getExample();

		void setExample(Character value);

	}

	public interface ScenarioTypeDerivedObjectCharacter extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeObjectCharacter getSource();

		void setSource(ScenarioTypeObjectCharacter source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeObjectCharacter.EXAMPLE })
		Character getDerived();

	}

	public interface ScenarioTypeObjectByte extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Byte getExample();

		void setExample(Byte value);

	}

	public interface ScenarioTypeDerivedObjectByte extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeObjectByte getSource();

		void setSource(ScenarioTypeObjectByte source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeObjectByte.EXAMPLE })
		Byte getDerived();

	}

	public interface ScenarioTypeObjectShort extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Short getExample();

		void setExample(Short value);

	}

	public interface ScenarioTypeDerivedObjectShort extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeObjectShort getSource();

		void setSource(ScenarioTypeObjectShort source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeObjectShort.EXAMPLE })
		Short getDerived();

	}

	public interface ScenarioTypeObjectInteger extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Integer getExample();

		void setExample(Integer value);

	}

	public interface ScenarioTypeDerivedObjectInteger extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeObjectInteger getSource();

		void setSource(ScenarioTypeObjectInteger source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeObjectInteger.EXAMPLE })
		Integer getDerived();

	}

	public interface ScenarioTypeObjectLong extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Long getExample();

		void setExample(Long value);

	}

	public interface ScenarioTypeDerivedObjectLong extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeObjectLong getSource();

		void setSource(ScenarioTypeObjectLong source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeObjectLong.EXAMPLE })
		Long getDerived();

	}

	public interface ScenarioTypeObjectFloat extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Float getExample();

		void setExample(Float value);

	}

	public interface ScenarioTypeDerivedObjectFloat extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeObjectFloat getSource();

		void setSource(ScenarioTypeObjectFloat source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeObjectFloat.EXAMPLE })
		Float getDerived();

	}

	public interface ScenarioTypeObjectDouble extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Double getExample();

		void setExample(Double value);

	}

	public interface ScenarioTypeDerivedObjectDouble extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeObjectDouble getSource();

		void setSource(ScenarioTypeObjectDouble source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeObjectDouble.EXAMPLE })
		Double getDerived();

	}

	public interface ScenarioTypeString extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		String getExample();

		void setExample(String value);

	}

	public interface ScenarioTypeDerivedString extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeString getSource();

		void setSource(ScenarioTypeString source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeString.EXAMPLE })
		String getDerived();

	}

	public interface ScenarioTypeClass extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Class<?> getExample();

		void setExample(Class<?> value);

	}

	public interface ScenarioTypeDerivedClass extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeClass getSource();

		void setSource(ScenarioTypeClass source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeClass.EXAMPLE })
		Class<?> getDerived();

	}

	public interface ScenarioTypeDate extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Date getExample();

		void setExample(Date value);

	}

	public interface ScenarioTypeDerivedDate extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeDate getSource();

		void setSource(ScenarioTypeDate source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeDate.EXAMPLE })
		Date getDerived();

	}

	public interface ScenarioTypeEnum extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		Enum<?> getExample();

		void setExample(Enum<?> value);

	}

	public interface ScenarioTypeDerivedEnum extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeEnum getSource();

		void setSource(ScenarioTypeEnum source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeEnum.EXAMPLE })
		Enum<?> getDerived();

	}

	public interface ScenarioTypeEnumSubtype extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		ExampleEnum getExample();

		void setExample(ExampleEnum value);

	}

	public interface ScenarioTypeDerivedEnumSubtype extends ConfigurationItem {

		String SOURCE = "source";

		String DERIVED = "derived";

		@Name(SOURCE)
		ScenarioTypeEnumSubtype getSource();

		void setSource(ScenarioTypeEnumSubtype source);

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeEnumSubtype.EXAMPLE })
		ExampleEnum getDerived();

	}

	// For testing the type-checking of the paths:

	public interface ScenarioTypeExampleSupertype extends ConfigurationItem {
		// Nothing needed but the type itself
	}

	public interface ScenarioTypeExampleType extends ScenarioTypeExampleSupertype {
		// Nothing needed but the type itself
	}

	public interface ScenarioTypeExampleSubtype extends ScenarioTypeExampleType {
		// Nothing needed but the type itself
	}

	public interface ScenarioTypeExampleIndirectSubtype extends ScenarioTypeExampleSubtype {
		// Nothing needed but the type itself
	}

	@NoImplementationClassGeneration
	public interface ScenarioTypePrimitiveFromObject extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@DerivedRef(SOURCE)
		int getDerived();

		@Name(SOURCE)
		Integer getSource();

	}

	public interface ScenarioTypeObjectFromPrimitive extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@DerivedRef(SOURCE)
		Integer getDerived();

		@Name(SOURCE)
		int getSource();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeFromWrongType extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@DerivedRef(SOURCE)
		int getDerived();

		@Name(SOURCE)
		boolean getSource();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeFromSupertype extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@DerivedRef(SOURCE)
		@Name(DERIVED)
		ScenarioTypeExampleType getDerived();

		@Name(SOURCE)
		ScenarioTypeExampleSupertype getSource();

	}

	public interface ScenarioTypeFromSubtype extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@DerivedRef(SOURCE)
		@Name(DERIVED)
		ScenarioTypeExampleType getDerived();

		@Name(SOURCE)
		ScenarioTypeExampleSubtype getSource();

	}

	public interface ScenarioTypeFromIndirectSubtype extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@DerivedRef(SOURCE)
		@Name(DERIVED)
		ScenarioTypeExampleType getDerived();

		@Name(SOURCE)
		ScenarioTypeExampleIndirectSubtype getSource();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeViaNonConfigItem extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@DerivedRef({ SOURCE, "i-dont-exist" })
		@Name(DERIVED)
		int getDerived();

		@InstanceFormat
		@Name(SOURCE)
		@Subtypes({})
		Object getSource();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeViaNonExistingProperty extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@DerivedRef({ SOURCE, "fubar" })
		@Name(DERIVED)
		int getDerived();

		@Name(SOURCE)
		@Subtypes({})
		ConfigurationItem getSource();

	}

	public interface ScenarioTypeMutualDependenciesReferenceLeft extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		String EXAMPLE = "example";

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeMutualDependenciesReferenceRight.EXAMPLE })
		String getDerived();

		@Name(SOURCE)
		ScenarioTypeMutualDependenciesReferenceRight getSource();

		void setSource(ScenarioTypeMutualDependenciesReferenceRight source);

		@Name(EXAMPLE)
		String getExample();

		void setExample(String example);

	}

	public interface ScenarioTypeMutualDependenciesReferenceRight extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		String EXAMPLE = "example";

		@Name(DERIVED)
		@DerivedRef({ SOURCE, ScenarioTypeMutualDependenciesReferenceLeft.EXAMPLE })
		String getDerived();

		@Name(SOURCE)
		ScenarioTypeMutualDependenciesReferenceLeft getSource();

		void setSource(ScenarioTypeMutualDependenciesReferenceLeft source);

		@Name(EXAMPLE)
		String getExample();

		void setExample(String example);

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeFunctionResultOfWrongType extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@Derived(fun = IntegerToString.class, args = @Ref(SOURCE))
		int getDerived();

		@Name(SOURCE)
		int getSource();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeFunctionParameterFromWrongType extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@Derived(fun = IntegerToString.class, args = @Ref(SOURCE))
		String getDerived();

		@Name(SOURCE)
		boolean getSource();

	}

	public interface ScenarioTypeFunctionParameterFromCorrectType extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@Derived(fun = IntegerToString.class, args = @Ref(SOURCE))
		String getDerived();

		@Name(SOURCE)
		int getSource();

		void setSource(int source);

	}

	public interface ScenarioTypeIntProperty extends ConfigurationItem {

		String EXAMPLE = "example";
		
		@Name(EXAMPLE)
		int getExample();

		void setExample(int example);

	}

	public interface ScenarioTypeFunctionParameterViaOtherItem extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@Derived(fun = IntegerToString.class, args = @Ref({ SOURCE, ScenarioTypeIntProperty.EXAMPLE }))
		String getDerived();

		@Name(SOURCE)
		ScenarioTypeIntProperty getSource();

		void setSource(ScenarioTypeIntProperty source);

	}

	public interface ScenarioTypeFunctionMultipleParameters extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE_A = "source-a";

		String SOURCE_B = "source-b";

		@Name(DERIVED)
		@Derived(fun = M.Add.class, args = { @Ref(SOURCE_A), @Ref(SOURCE_B) })
		int getDerived();

		@Name(SOURCE_A)
		int getSourceA();

		void setSourceA(int sourceA);

		@Name(SOURCE_B)
		int getSourceB();

		void setSourceB(int sourceB);

	}

	public interface ScenarioTypePrimitiveLongFromInt extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@DerivedRef(SOURCE)
		long getDerived();

		@Name(SOURCE)
		int getSource();

		void setSource(int source);

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeObjectLongFromInt extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Name(DERIVED)
		@DerivedRef(SOURCE)
		Long getDerived();

		@Name(SOURCE)
		Integer getSource();

		void setSource(Integer source);

	}

	public static class ExampleIntegerValueProvider extends AbstractConfigurationValueProvider<Integer> {

		public static final ExampleIntegerValueProvider INSTANCE = new ExampleIntegerValueProvider();

		public ExampleIntegerValueProvider() {
			super(Integer.class);
		}

		@Override
		protected Integer getValueNonEmpty(String propertyName, CharSequence propertyValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected String getSpecificationNonNull(Integer configValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Integer defaultValue() {
			return 42;
		}

	}

	public interface ScenarioTypeDefaultFromFormat extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@Format(ExampleIntegerValueProvider.class)
		@DerivedRef(SOURCE)
		@Name(DERIVED)
		Integer getDerived();

		@Name(SOURCE)
		Integer getSource();

	}

	public interface ScenarioTypeDefaultFromValueBinding extends ConfigurationItem {

		String DERIVED = "derived";

		String SOURCE = "source";

		@ListBinding()
		@DerivedRef({ SOURCE, ScenarioTypeDefaultFromFormatSource.SOURCE })
		@Name(DERIVED)
		List<Integer> getDerived();

		@Name(SOURCE)
		ScenarioTypeDefaultFromFormatSource getSource();

	}

	public interface ScenarioTypeDefaultFromFormatSource extends ConfigurationItem {

		String SOURCE = "source";

		@ListBinding
		@Name(SOURCE)
		List<Integer> getSource();

	}

	public interface BaseConditionalPath extends ConfigurationItem {

		String CONDITIONAL_FOO = "conditional-foo";
		String OTHER = "other";

		@Name(OTHER)
		@Mandatory
		Other getOther();

		void setOther(Other value);

		@Name(CONDITIONAL_FOO)
		@DerivedRef(steps = { @Step(OTHER), @Step(type = OtherWithFoo.class, value = OtherWithFoo.FOO) })
		String getConditionalFoo();

		@Abstract
		interface Other extends ConfigurationItem {
			// Pure base interface.
		}

		interface OtherWithFoo extends Other {
			String FOO = "foo";

			@Name(FOO)
			@Mandatory
			String getFoo();

			void setFoo(String value);
		}

		interface OtherWithoutFoo extends Other {
			String BAR = "bar";

			@Name(BAR)
			int getBar();
		}

	}

	public interface DerivedUsingCollectionProps extends ConfigurationItem {

		public interface Other1 extends ConfigurationItem {

			String VALUE = "value";

			@Name(VALUE)
			int getValue();
			
			void setValue(int val);

		}

		public interface Other2 extends NamedConfigMandatory {

			String OTHER1 = "other1";

			@Name(OTHER1)
			List<Other1> getOther1();
		}

		String OTHER = "other";

		String OTHERS_LIST = "others-list";

		String OTHERS_MAP = "others-map";

		String OTHERS_ARRAY = "others-array";

		@Derived(fun = Sum.class, args = {
			@Ref({ OTHER, Other1.VALUE }),
			@Ref({ OTHERS_LIST, Other1.VALUE }),
			@Ref({ OTHERS_MAP, Other2.OTHER1, Other1.VALUE }),
			@Ref({ OTHERS_ARRAY, Other1.VALUE }),
		})
		int getSum();

		@Name(OTHER)
		Other1 getOther();

		void setOther(Other1 val);

		@Name(OTHERS_LIST)
		List<Other1> getOthersList();

		@Name(OTHERS_MAP)
		@Key(Other2.NAME_ATTRIBUTE)
		Map<String, Other2> getOthersMap();

		@Name(OTHERS_ARRAY)
		Other1[] getOthersArray();

		void setOthersArray(Other1[] val);

		public static class Sum extends Function4<Integer, Integer, List<Integer>, List<List<Integer>>, List<Integer>> {

			@Override
			public Integer apply(Integer arg1, List<Integer> arg2, List<List<Integer>> arg3, List<Integer> arg4) {
				int result = 0;
				if (arg1 != null) {
					result += arg1.intValue();
				}
				for (Integer t : arg2) {
					result += t.intValue();
				}
				for (List<Integer> lt : arg3) {
					for (Integer t : lt) {
						result += t.intValue();
					}
				}
				for (Integer t : arg4) {
					result += t.intValue();
				}
				return result;
			}

		}
	}

}
