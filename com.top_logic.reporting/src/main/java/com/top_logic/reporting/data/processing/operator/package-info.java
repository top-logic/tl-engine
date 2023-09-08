/**
 * Operators for processing data on {@link com.top_logic.reporting.data.base.value.Value}s.
 * <p>
 * This package contains some default operators for different basic value types (such as
 * {@link com.top_logic.reporting.data.base.value.common.NumericValue number},
 * {@link com.top_logic.reporting.data.base.value.common.DateValue date}). The operators can be
 * located anywhere in the system, but when they should be known by the
 * {@link com.top_logic.reporting.data.processing.operator.OperatorFactory}, they have to be
 * specified in the <i>TopLogic</i> configuration file.
 * </p>
 * <p>
 * The operator factory is able to find as much operators for a type of values as possible.
 * Therefore it inspects the class structure and tries to find also the operators for the super
 * classes.
 * </p>
 */
package com.top_logic.reporting.data.processing.operator;
