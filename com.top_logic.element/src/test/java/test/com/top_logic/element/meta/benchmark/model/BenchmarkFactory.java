/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.benchmark.model;

/**
 * Factory for <code>Benchmark</code> objects.
 * 
 * <p>
 * Note: this is generated code. Do not modify. Instead, create a subclass and register this in the module system.
 * </p>
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.FactoryGenerator}
 */
public class BenchmarkFactory extends com.top_logic.element.meta.kbbased.AbstractElementFactory {

	/**
	 * Name of the structure <code>Benchmark</code> defined by {@link BenchmarkFactory}.
	 */
	public static final String BENCHMARK_STRUCTURE = "Benchmark";

	/**
	 * Lookup {@link BenchmarkA} type.
	 */
	public static com.top_logic.model.TLClass getBenchmarkAType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(BENCHMARK_STRUCTURE).getType(BenchmarkA.BENCHMARK_A_TYPE);
	}

	/**
	 * Lookup {@link BenchmarkA#BACK_REF_ATTR} of {@link BenchmarkA}.
	 */
	public static com.top_logic.model.TLReference getBackRefBenchmarkAAttr() {
		return (com.top_logic.model.TLReference) getBenchmarkAType().getPart(BenchmarkA.BACK_REF_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkA#REF_ATTR} of {@link BenchmarkA}.
	 */
	public static com.top_logic.model.TLReference getRefBenchmarkAAttr() {
		return (com.top_logic.model.TLReference) getBenchmarkAType().getPart(BenchmarkA.REF_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkA#REF_LIST_ATTR} of {@link BenchmarkA}.
	 */
	public static com.top_logic.model.TLReference getRefListBenchmarkAAttr() {
		return (com.top_logic.model.TLReference) getBenchmarkAType().getPart(BenchmarkA.REF_LIST_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkA#REF_SET_ATTR} of {@link BenchmarkA}.
	 */
	public static com.top_logic.model.TLReference getRefSetBenchmarkAAttr() {
		return (com.top_logic.model.TLReference) getBenchmarkAType().getPart(BenchmarkA.REF_SET_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkA#S1_ATTR} of {@link BenchmarkA}.
	 */
	public static com.top_logic.model.TLProperty getS1BenchmarkAAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkAType().getPart(BenchmarkA.S1_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkA#S2_ATTR} of {@link BenchmarkA}.
	 */
	public static com.top_logic.model.TLProperty getS2BenchmarkAAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkAType().getPart(BenchmarkA.S2_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkA#X_ATTR} of {@link BenchmarkA}.
	 */
	public static com.top_logic.model.TLProperty getXBenchmarkAAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkAType().getPart(BenchmarkA.X_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkA#Y_ATTR} of {@link BenchmarkA}.
	 */
	public static com.top_logic.model.TLProperty getYBenchmarkAAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkAType().getPart(BenchmarkA.Y_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkB} type.
	 */
	public static com.top_logic.model.TLClass getBenchmarkBType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(BENCHMARK_STRUCTURE).getType(BenchmarkB.BENCHMARK_B_TYPE);
	}

	/**
	 * Lookup {@link BenchmarkB#DYNAMIC_REF_ATTR} of {@link BenchmarkB}.
	 */
	public static com.top_logic.model.TLReference getDynamicRefBenchmarkBAttr() {
		return (com.top_logic.model.TLReference) getBenchmarkBType().getPart(BenchmarkB.DYNAMIC_REF_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkB#DYNAMIC_REF_LIST_ATTR} of {@link BenchmarkB}.
	 */
	public static com.top_logic.model.TLReference getDynamicRefListBenchmarkBAttr() {
		return (com.top_logic.model.TLReference) getBenchmarkBType().getPart(BenchmarkB.DYNAMIC_REF_LIST_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkB#DYNAMIC_REF_SET_ATTR} of {@link BenchmarkB}.
	 */
	public static com.top_logic.model.TLReference getDynamicRefSetBenchmarkBAttr() {
		return (com.top_logic.model.TLReference) getBenchmarkBType().getPart(BenchmarkB.DYNAMIC_REF_SET_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkB#DYNAMIC_S1_ATTR} of {@link BenchmarkB}.
	 */
	public static com.top_logic.model.TLProperty getDynamicS1BenchmarkBAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkBType().getPart(BenchmarkB.DYNAMIC_S1_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkB#DYNAMIC_S2_ATTR} of {@link BenchmarkB}.
	 */
	public static com.top_logic.model.TLProperty getDynamicS2BenchmarkBAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkBType().getPart(BenchmarkB.DYNAMIC_S2_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkB#DYNAMIC_X_ATTR} of {@link BenchmarkB}.
	 */
	public static com.top_logic.model.TLProperty getDynamicXBenchmarkBAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkBType().getPart(BenchmarkB.DYNAMIC_X_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkB#DYNAMIC_Y_ATTR} of {@link BenchmarkB}.
	 */
	public static com.top_logic.model.TLProperty getDynamicYBenchmarkBAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkBType().getPart(BenchmarkB.DYNAMIC_Y_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkResult} type.
	 */
	public static com.top_logic.model.TLClass getBenchmarkResultType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(BENCHMARK_STRUCTURE).getType(BenchmarkResult.BENCHMARK_RESULT_TYPE);
	}

	/**
	 * Lookup {@link BenchmarkResult#MILLIS_PER_OPERATION_ATTR} of {@link BenchmarkResult}.
	 */
	public static com.top_logic.model.TLProperty getMillisPerOperationBenchmarkResultAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkResultType().getPart(BenchmarkResult.MILLIS_PER_OPERATION_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkResult#OBJECT_CNT_ATTR} of {@link BenchmarkResult}.
	 */
	public static com.top_logic.model.TLProperty getObjectCntBenchmarkResultAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkResultType().getPart(BenchmarkResult.OBJECT_CNT_ATTR);
	}

	/**
	 * Lookup {@link BenchmarkResult#TEST_NAME_ATTR} of {@link BenchmarkResult}.
	 */
	public static com.top_logic.model.TLProperty getTestNameBenchmarkResultAttr() {
		return (com.top_logic.model.TLProperty) getBenchmarkResultType().getPart(BenchmarkResult.TEST_NAME_ATTR);
	}

	/**
	 * Lookup {@link A} type.
	 */
	public static com.top_logic.model.TLClass getAType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(BENCHMARK_STRUCTURE).getType(A.A_TYPE);
	}

	/**
	 * Lookup {@link B} type.
	 */
	public static com.top_logic.model.TLClass getBType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(BENCHMARK_STRUCTURE).getType(B.B_TYPE);
	}

	/**
	 * Lookup {@link Result} type.
	 */
	public static com.top_logic.model.TLClass getResultType() {
		return (com.top_logic.model.TLClass) com.top_logic.util.model.ModelService.getApplicationModel().getModule(BENCHMARK_STRUCTURE).getType(Result.RESULT_TYPE);
	}

	/**
	 * Name of type <code>BenchmarkA</code> in structure {@link #BENCHMARK_STRUCTURE}.
	 * 
	 * @deprecated Use {@link BenchmarkA#BENCHMARK_A_TYPE}.
	 */
	@Deprecated
	public static final String BENCHMARK_A_NODE = BenchmarkA.BENCHMARK_A_TYPE;

	/**
	 * Name of type <code>BenchmarkB</code> in structure {@link #BENCHMARK_STRUCTURE}.
	 * 
	 * @deprecated Use {@link BenchmarkB#BENCHMARK_B_TYPE}.
	 */
	@Deprecated
	public static final String BENCHMARK_B_NODE = BenchmarkB.BENCHMARK_B_TYPE;

	/**
	 * Name of type <code>BenchmarkResult</code> in structure {@link #BENCHMARK_STRUCTURE}.
	 * 
	 * @deprecated Use {@link BenchmarkResult#BENCHMARK_RESULT_TYPE}.
	 */
	@Deprecated
	public static final String BENCHMARK_RESULT_NODE = BenchmarkResult.BENCHMARK_RESULT_TYPE;

	/**
	 * Name of type <code>A</code> in structure {@link #BENCHMARK_STRUCTURE}.
	 * 
	 * @deprecated Use {@link A#A_TYPE}.
	 */
	@Deprecated
	public static final String A_NODE = A.A_TYPE;

	/**
	 * Storage table name of {@link #A_NODE} objects.
	 */
	public static final String KO_NAME_A = "Benchmark";

	/**
	 * Name of type <code>B</code> in structure {@link #BENCHMARK_STRUCTURE}.
	 * 
	 * @deprecated Use {@link B#B_TYPE}.
	 */
	@Deprecated
	public static final String B_NODE = B.B_TYPE;

	/**
	 * Storage table name of {@link #B_NODE} objects.
	 */
	public static final String KO_NAME_B = "Benchmark";

	/**
	 * Name of type <code>Result</code> in structure {@link #BENCHMARK_STRUCTURE}.
	 * 
	 * @deprecated Use {@link Result#RESULT_TYPE}.
	 */
	@Deprecated
	public static final String RESULT_NODE = Result.RESULT_TYPE;

	/**
	 * Storage table name of {@link #RESULT_NODE} objects.
	 */
	public static final String KO_NAME_RESULT = "BenchmarkResult";


	/**
	 * Create an instance of {@link A} type.
	 */
	public final A createA(com.top_logic.model.TLObject context) {
		return (A) createObject(getAType(), context);
	}

	/**
	 * Create an instance of {@link A} type.
	 */
	public final A createA() {
		return createA(null);
	}

	/**
	 * Create an instance of {@link B} type.
	 */
	public final B createB(com.top_logic.model.TLObject context) {
		return (B) createObject(getBType(), context);
	}

	/**
	 * Create an instance of {@link B} type.
	 */
	public final B createB() {
		return createB(null);
	}

	/**
	 * Create an instance of {@link Result} type.
	 */
	public final Result createResult(com.top_logic.model.TLObject context) {
		return (Result) createObject(getResultType(), context);
	}

	/**
	 * Create an instance of {@link Result} type.
	 */
	public final Result createResult() {
		return createResult(null);
	}

	/**
	 * The singleton instance of {@link BenchmarkFactory}.
	 */
	public static BenchmarkFactory getInstance() {
		return (BenchmarkFactory) com.top_logic.element.model.DynamicModelService.getFactoryFor(BENCHMARK_STRUCTURE);
	}
}
