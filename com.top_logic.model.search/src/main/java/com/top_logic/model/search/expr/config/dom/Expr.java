/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.dom;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.CssClass;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.search.expr.CompareKind;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.operations.Count;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.TLScriptCodeEditorControl;
import com.top_logic.model.search.ui.TLScriptPropertyEditor;

/**
 * Configuration describing a {@link SearchExpression}.
 * 
 * <p>
 * An {@link Expr} has either its canonical XML representation, or a textual DSL representation
 * defined by {@link ExprFormat}:
 * </p>
 * 
 * <dl>
 * <dt>null</dt>
 * <dd>Literal evaluating to the constant value <code>null</code>.</dd>
 * 
 * <dt>true</dt>
 * <dd>Boolean literal evaluating to the constant value <code>true</code>.</dd>
 * 
 * <dt>false</dt>
 * <dd>Boolean literal evaluating to the constant value <code>false</code>.</dd>
 * 
 * <dt>"some string"</dt>
 * <dt>'some other string'</dt>
 * <dd>The string literal <code>some string</code> or <code>some other string</code>.</dd>
 * 
 * <dt>#"my.res.key"</dt>
 * <dt>#'my.res.key'</dt>
 * <dd>Reference to the (application-defined) {@link ResKey} with the given key.</dd>
 * 
 * <dt>#("Ein Text"@de, "Some text"@en, "Un texto"@es)</dt>
 * <dt>#('Ein Text'@de, 'Some text'@en, 'Un texto'@es)</dt>
 * <dd>The literal {@link ResKey} translating to the given texts in the annotated languages.</dd>
 * 
 * <dt>"some string"@de</dt>
 * <dt>'some other string'@en</dt>
 * <dd>The literal {@link ResKey} evaluating to the given text in the annotated language. Since only
 * a single translation is given, this syntax is only valid for single-language applications.</dd>
 * 
 * <dt>[resKey].fill([expr1],... [exprn])</dt>
 * <dd>Builds a message with dynamic arguments from a {@link ResKey}. The expression
 * <code>[resKey]</code> is evaluated and a {@link ResKey} value is expected as result. With this
 * {@link ResKey}, a dynamic message key is build with the evaluation result of
 * <code>[arg1]</code>,... <code>[argn]</code> as arguments.</dd>
 * 
 * <dt>`[fully qualified name of a model part]`</dt>
 * <dd>The literal model part, may be a type (e.g. <code>`my.module:MyType`</code>), a structured
 * type part (<code>`my.module:MyType#myProperty`</code>), or a classifier
 * (<code>(`my.module:MyEnum#MY_CLA`)</code>.</dd>
 * 
 * <dt>42</dt>
 * <dt>13.42</dt>
 * <dd>The numeric literals <code>42</code> and <code>13.42</code>.</dd>
 * 
 * <dt>[e1] + [e2]</dt>
 * <dd>The sum of <code>[e1]</code> and <code>[e2]</code>.</dd>
 * 
 * <dt>[e1] - [e2]</dt>
 * <dd>The difference of <code>[e1]</code> and <code>[e2]</code>.</dd>
 * 
 * <dt>[e1] * [e2]</dt>
 * <dd>The product of <code>[e1]</code> and <code>[e2]</code>.</dd>
 * 
 * <dt>[e1] / [e2]</dt>
 * <dd>The division of <code>[e1]</code> by <code>[e2]</code>.</dd>
 * 
 * <dt>[e1] % [e2]</dt>
 * <dd>The modulo of the division of <code>[e1]</code> by <code>[e2]</code>.</dd>
 * 
 * <dt>[e1] &amp;&amp; [e2]</dt>
 * <dt>[e1] and [e2]</dt>
 * <dd>Combination of the boolean expressions <code>[e1]</code> and <code>[e2]</code> evaluating to
 * <code>true</code>, if <code>[e1]</code> and <code>[e2]</code> evaluate to <code>true</code>.</dd>
 * 
 * <dt>[e1] || [e2]</dt>
 * <dt>[e1] or [e2]</dt>
 * <dd>Combination of the boolean expressions <code>[e1]</code> and <code>[e2]</code> evaluating to
 * <code>true</code>, if either <code>[e1]</code> or <code>[e2]</code> evaluates to
 * <code>true</code>.</dd>
 * 
 * <dt>![expr]</dt>
 * <dd>Boolean expressions evaluating to <code>true</code>, if <code>[expr]</code> evaluates to
 * <code>false</code>.</dd>
 *
 * <dt>[e1] == [e2]</dt>
 * <dt>[e1].isEqual([e2])</dt>
 * <dd>Boolean expression evaluating to <code>true</code>, if <code>[e1]</code> and
 * <code>[e2]</code> evaluate to the same value.</dd>
 * 
 * <dt>[e1] != [e2]</dt>
 * <dd>Boolean expression evaluating to <code>true</code>, if <code>[e1]</code> and
 * <code>[e2]</code> do not evaluate to the same value.</dd>
 * 
 * <dt>[e1] &gt; [e2]</dt>
 * <dt>[e1].isGreater([e2])</dt>
 * <dt>[e1].gt([e2])</dt>
 * <dd>Boolean expression evaluating to <code>true</code>, if the evaluation result of
 * <code>[e1]</code> is greater than the evaluation result of <code>[e2]</code>.</dd>
 * 
 * <dt>[e1] &lt; [e2]</dt>
 * <dt>[e1].lt([e2])</dt>
 * <dd>Boolean expression evaluating to <code>true</code>, if the evaluation result of
 * <code>[e1]</code> is less than the evaluation result of <code>[e2]</code>.</dd>
 * 
 * <dt>[e1] &gt;= [e2]</dt>
 * <dt>[e1].ge([e2])</dt>
 * <dd>Boolean expression evaluating to <code>true</code>, if the evaluation result of
 * <code>[e1]</code> is greater than or equal to the evaluation result of <code>[e2]</code>.</dd>
 * 
 * <dt>[e1] &lt;= [e2]</dt>
 * <dt>[e1].lt([e2])</dt>
 * <dd>Boolean expression evaluating to <code>true</code>, if the evaluation result of
 * <code>[e1]</code> is less than or equal to the evaluation result of <code>[e2]</code>.</dd>
 * 
 * <dt>compare([e1], [e2])</dt>
 * <dd>The comparator comparison value of <code>[e1]</code> and <code>[e2]</code>. The result is
 * less than zero, equal to zero, greater than zero, if <code>[e1]</code> is less than, equal to,
 * greater than <code>[e2]</code> respectively.</dd>
 * 
 * <dt>desc([e1])</dt>
 * <dd>The inversion of the compare result of <code>[e1]</code>. The following holds:
 * <code>compar(desc([e1]), desc([e2])) = -compare([e1], [e2])</code>. Care must be taken that
 * <code>desc(...)</code> values are only compared with other <code>desc(...)</code> values of
 * comparable contents.</dd>
 * 
 * <dt>x -> [expr]</dt>
 * <dd>Defines a function taking an argument <code>x</code> and creating a value by evaluating the
 * expression <code>[expr]</code>. In the expression, the value of the function argument can be
 * accessed using the syntax <code>$x</code>. A function taking multiple arguments can be
 * constructed in the following way: <code>x -> y -> [expr using $x and $y]</code>.</dd>
 * 
 * <dt>$x</dt>
 * <dd>Access to a function argument <code>x</code> defined in the context.</dd>
 * 
 * <dt>[set].filter([f])</dt>
 * <dd>Filters the collection result of evaluating <code>[set]</code> by applying the predicate
 * function <code>[f]</code> to each of its elements. In the resulting collection, only those
 * elements are contained that evaluated the predicate function <code>[f]</code> to
 * <code>true</code>.</dd>
 * 
 * <dt>[set].map([f])</dt>
 * <dt>[set].foreach([f])</dt>
 * <dd>Applies the function <code>[f]</code> to each element of the collection <code>[set]</code>
 * and returns the collection of evaluation results. The first syntax is preferred for functional
 * expressions, the second for applying functions with side-effects to collections.</dd>
 * 
 * <dt>[collection].reduce([identity], [fun])</dt>
 * <dd>Reduces the collection to a single value by applying an associative combinator function
 * <code>[fun]</code>. The first argument is the neutral element with respect to the combinator
 * function, meaning that <code>fun(identity, x)</code> is equal to <code>x</code> for any
 * <code>x</code>. The expression <code>list(v1, v2, ..., vn).reduce(i, fun)</code> is equivalent to
 * the expression <code>fun(... fun(fun(i, v1), v2)...), vn)</code>.</dd>
 * 
 * <dt>[set1].containsAll([set2])</dt>
 * <dd>Computes whether the set <code>[set1]</code> contains all elements form the set
 * <code>[set2]</code>.</dd>
 * 
 * <dt>[set].containsElement([expr])</dt>
 * <dd>Computes whether the set <code>[set]</code> contains the element retrieved from the
 * evaluation of <code>[expr]</code>.</dd>
 * 
 * <dt>[set1].containsSome([set2])</dt>
 * <dd>Computes whether the set <code>[set1]</code> contains at least one element from the set
 * <code>[set2]</code>.</dd>
 * 
 * <dt>[setOfSets].flatten()</dt>
 * <dd>Computes a collection that contains all element form all collections in the collection
 * <code>[setOfSets]</code>. Since collections of collections are not supported as end results of
 * expressions, <code>flatten</code> is required to fix the type of expressions
 * <code>[set].map([set-valued-fun])</code>, where a mapping with a set-valued function was computed
 * over a set of elements.</dd>
 * 
 * <dt>[set1].intersection([set2])</dt>
 * <dd>Computes the intersection of the two sets <code>[set1]</code> and <code>[set2]</code>.</dd>
 * 
 * <dt>union([set1],..., [setn])</dt>
 * <dd>Computes the union of the sets <code>[set1]</code>,... <code>[setn]</code>.</dd>
 * 
 * <dt>[expr].round([digits])</dt>
 * <dd>Rounds the floating point value <code>[expr]</code> to the given number of digits. The
 * expression <code>3.141.round(2)</code> results in <code>3.14</code>. The digit argument is
 * optional and defaults to <code>0</code>.</dd>
 * 
 * <dt>[e1].isDateEqual([e2])</dt>
 * <dd>Equivalent to <code>[e1].day() == [e2].day()</code>.</dd>
 * 
 * <dt>[e1].isDateGreater([e2])</dt>
 * <dd>Equivalent to <code>[e1].day() &gt; [e2].day()</code>.</dd>
 * 
 * <dt>[expr].day()</dt>
 * <dd>Converts a timestamp value to an abstract date value (without time and timezone).</dd>
 * 
 * <dt>[e1].isFloatEqual([e2], [precision])</dt>
 * <dd>Equivalent to <code>[e1].round([precision]) == [e2].round([precision])</code>.</dd>
 * 
 * <dt>[e1].isFloatGreater([e2])</dt>
 * <dd>Equivalent to <code>[e1].round([precision]) &gt; [e2].round([precision])</code>.</dd>
 * 
 * <dt>[e1].length()</dt>
 * <dd>The number of characters in the string <code>[e1]</code>.</dd>
 * 
 * <dt>[e1].isStringEqual([e2], [case-sensitive])</dt>
 * <dd>Compares the strings <code>[e1]</code> and <code>[e2]</code> for equality. The optional
 * boolean <code>[case-sensitive]</code> argument decides, whether comparison should be
 * case-sensitive. The default is to compare case-insensitive.</dd>
 * 
 * <dt>[e1].isStringGreater([e2], [case-sensitive])</dt>
 * <dd>Compares the strings <code>[e1]</code> and <code>[e2]</code>. The optional boolean
 * <code>[case-sensitive]</code> argument decides, whether comparison should be case-sensitive. The
 * default is to compare case-insensitive.</dd>
 * 
 * <dt>[e1].stringStartsWith([e2], [case-sensitive])</dt>
 * <dd>Checks, whether <code>[e1]</code> starts with the prefix <code>[e2]</code>. The optional
 * boolean <code>[case-sensitive]</code> argument decides, whether the check is performed
 * case-sensitive. The default is to compare case-insensitive.</dd>
 * 
 * <dt>[e1].stringContains([e2], [case-sensitive])</dt>
 * <dd>Checks, whether <code>[e1]</code> contains the sub-string <code>[e2]</code>. The optional
 * boolean <code>[case-sensitive]</code> argument decides, whether the check is performed
 * case-sensitive. The default is to compare case-insensitive.</dd>
 * 
 * <dt>[e1].stringEndsWith([e2], [case-sensitive])</dt>
 * <dd>Checks, whether <code>[e1]</code> ends with the suffix <code>[e2]</code>. The optional
 * boolean <code>[case-sensitive]</code> argument decides, whether the check is performed
 * case-sensitive. The default is to compare case-insensitive.</dd>
 * 
 * <dt>if([test], [then], [else])</dt>
 * <dt>[test].ifElse([then], [else])</dt>
 * <dd>Results in the evaluation of <code>[then]</code>, or <code>[else]</code> depending on the
 * result of the evaluation of <code>[test]</code>. If <code>[test]</code> evaluates to
 * <code>true</code>, the result of <code>[then]</code> is returned, <code>[else]</code>
 * otherwise.</dd>
 * 
 * <dt>[expr].isEmpty()</dt>
 * <dd>Checks, whether <code>[expr]</code> evaluates to the empty string, empty collection, or
 * <code>null</code>.</dd>
 * 
 * <dt>[expr].get(`part-literal`)</dt>
 * <dd>Accesses the attribute <code>part-literal</code> on the result object of
 * <code>[expr]</code>.</dd>
 * 
 * <dt>[expr].referers(`ref-literal`)</dt>
 * <dd>Retrieves the set of objects that have the result of <code>[expr]</code> assigned to their
 * reference <code>ref-literal</code>.</dd>
 * 
 * <dt>[expr].set(`part-literal`, [value])</dt>
 * <dd>Updates the attribute <code>part-literal</code> of the result object of <code>[expr]</code>
 * to the value of <code>[value]</code>.</dd>
 * 
 * <dt>obj.add(ref, value)</dt>
 * <dd>Appends the given value to the given multi-reference of the given object. Equivalent to
 * <code>obj.add(ref, obj.get(ref).size(), value)</code>.</dd>
 * 
 * <dt>obj.add(ref, index, value)</dt>
 * <dd>Adds the given value to the given multi-reference of the given object. The new value is
 * inserted at the given index (if the given reference is ordered). Equivalent to
 * <code>obj.set(ref, concat(obj.get(ref).subList(0, index), value, obj.get(ref).subList(index)))</code></dd>
 * 
 * <dt>new(`type-literal`)</dt>
 * <dd>Allocates a new object of the given type and returns it as result.</dd>
 * 
 * <dt>[fun]([arg])</dt>
 * <dt>[fun]([arg1], ..., [argn])</dt>
 * <dd>Evaluates the function expression <code>[fun]</code> with the result of <code>[arg]</code> as
 * argument.</dd>
 * 
 * <dt>{[var] = [expr]; ...; [result-expression-using-$var]; }</dt>
 * <dd>Assigns the result of evaluating expression <code>[expr]</code> to the name
 * <code>[var]</code> and returns the evaluation of the expression
 * <code>[result-expression-using-$var]</code>.</dd>
 * 
 * <dt>[expr].instanceOf(`class-literal`)</dt>
 * <dd>Checks, whether the result of <code>[expr]</code> is an instance of the class
 * <code>class-literal</code>.</dd>
 * 
 * <dt>[expr].singleElement()</dt>
 * <dd>Unwraps the collection <code>[expr]</code> that must have at least one argument. If the
 * collection contains an element, it is returned. An empty collection evaluates to
 * <code>null</code>.</dd>
 * 
 * <dt>[expr].singleton()</dt>
 * <dd>Wraps the result of <code>[expr]</code> into a collection. A non-null value results in a
 * one-element collection. A <code>null</code> value results in the empty collection.</dd>
 * 
 * <dt>list([e1],... [en])</dt>
 * <dd>Creates a fixed-length list from the evaluation results of <code>[e1]</code>,...
 * <code>[en]</code>.</dd>
 * 
 * <dt>count([start], [stop], [step])</dt>
 * <dd>See {@link Count}.</dd>
 * 
 * <dt>concat(l1, l2, ..., ln)</dt>
 * <dd>Concatenates the given lists. Equivalent to <code>list(l1, l2, ..., ln).flatten()</code>. The
 * given parts <code>l1</code>,... <code>ln</code> may either be collections or single values. If
 * all parts are single values, the expression is equivalent to
 * <code>list(l1, l2, ..., ln)</code>.</dd>
 * 
 * <dt>list.subList(startIndex)</dt>
 * <dd>The part of the given list starting from the given index and ending at the end of the list.
 * Equivalent to <code>list.subList(startIndex, list.size())</code>.</dd>
 * 
 * <dt>list.subList(startIndex, stopIndex)</dt>
 * <dd>The part of the list starting at the given start index (inclusive) and ending at the given
 * stop index (exclusive).</dd>
 * 
 * <dt>[e1].size()</dt>
 * <dd>The length of the list <code>[e1]</code>.</dd>
 * 
 * <dt>[e1].sort([comparator])</dt>
 * <dd>
 * <p>
 * Sorts the list <code>[e1]</code> using the comparator function <code>[comparator]</code>.
 * </p>
 * <p>
 * A comparator function is expected to take two arguments <code>x</code> and <code>y</code> and
 * return a value less than, equal to, greater than zero, if <code>x</code> is less than, equal to,
 * greater than <code>y</code>.
 * </p>
 * 
 * <p>
 * The comparator argument is optional. If not given, the values in the list are compared directly.
 * Note: This is only useful for lists of primitive values, since objects are not comparable.
 * </p>
 * </dd>
 * 
 * <dt>comparator([fun])</dt>
 * <dd>Builds a comparator function for <code>sort(...)</code> from an accessor function that
 * retrieves a compare key from a list element. Sorting a list with such comparator establishes an
 * order on the values retrieved by the accessor function.</dd>
 * 
 * <dt>[expr].associationNavigation(`source-end`, `target-end`)</dt>
 * <dd>Navigates the association defining <code>source-end</code> from the result of
 * <code>[expr]</code> to the end <code>target-end</code>.</dd>
 * 
 * <dt>all(`class-literal`)</dt>
 * <dd>Retrieves all instances of the given class.</dd>
 * 
 * <dt>none()</dt>
 * <dd>The empty set.</dd>
 * 
 * <dt>[start].recursion([fun], [min-depth], [max-depth])</dt>
 * <dd>
 * <p>
 * Builds a list by applying the function <code>[fun]</code> to each element of the
 * <code>[start]</code> collection and recursively to all result elements produced by former
 * function evaluations. If the recursion produces the same object twice, only the first one is kept
 * and the recursion terminates. In the evaluation result, only those elements are returned that
 * resulted from recursion depths between <code>[min-depth]</code> (inclusive) and
 * <code>[max-depth]</code> (exclusive).
 * </p>
 * 
 * <p>
 * Both depth arguments are optional. <code>[min-depth]</code> defaults to <code>0</code> and
 * <code>[max-depth]</code> to <code>-1</code> meaning infinite depth.
 * </p>
 * 
 * <p>
 * The expression <code>$x.recursion(node -> $node.get(`my:Type#parent`), 1)</code> builds a path
 * from the element in variable <code>x</code> to the root of a tree following the parrent attribute
 * `my:Type#parent`. The path starts with the direct parent of <code>x</code>, since <code>x</code>
 * itself is not included in the path, because the minimum depth (number of evaluations) is
 * <code>1</code>.
 * </p>
 * </dd>
 * 
 * <dt>tuple(coord1 -> [expr1], coord2 -> [expr2],..., coordn -> [exprn])</dt>
 * <dd>
 * <p>
 * Correlation constructor. The expression evaluates to a collection of transient tuple objects with
 * the properties <code>coord1</code> to <code>coordn</code>.
 * </p>
 * <p>
 * The objects are constructed as follows: The expression <code>expr1</code> is evaluated and for
 * each element of the result collection, the expression <code>expr2</code> is evaluated with the
 * variable <code>coord1</code> assigned to the current element of the result of the evaluation of
 * <code>expr1</code>. The process continues with all expressions up to <code>exprn</code>. From
 * each complete set of variable assignments <code>coord1</code> to <code>coordn</code>, a transient
 * tuple object is created and returned as result of the expression.
 * </p>
 * <p>
 * Note: The <code>tuple(...)</code> constructor can currently only be used as direct result, since
 * the resulting transient tuple objects cannot be further accessed with the expression language,
 * since their properties have no qualified name and the <code>get(...)</code> function requires a
 * fully qualified type part literal to work.
 * </p>
 * </dd>
 * 
 * <dt>[fun]([a0], [a1], ..., [an])</dt>
 * <dd>Evaluates the function <code>[fun]</code> passing the result of the evaluations of the
 * expressions <code>[a1]</code>, ..., <code>[an]</code> as arguments. The function
 * <code>[fun]</code> may only be a globally defined function name. To apply a function variable,
 * use <code>[f].apply([a1], ..., [an])</code> with the function-typed expression <code>[f]</code>.
 * An alternative syntax with exactly the same semantics is <code>[a0].fun([a1], ..., [an])</code>.
 * Custom functions can be defined by adding builders to the
 * {@link com.top_logic.model.search.expr.config.SearchBuilder.Config#getMethods()} configuration
 * list.</dd>
 * 
 * <dt>[expr].[fun]([a1], [a2], ..., [an])</dt>
 * <dd>Evaluates the function <code>[fun]</code> passing the result of the evaluations of the
 * expressions <code>[expr]</code>, <code>[a1]</code>, ..., <code>[an]</code> as arguments. The
 * function <code>[fun]</code> can only be a globally defined name. An alternative syntax with
 * exactly the same semantics is <code>fun([expr], [a1], ..., [an])</code>. Custom functions can be
 * defined by adding builders to the
 * {@link com.top_logic.model.search.expr.config.SearchBuilder.Config#getMethods()} configuration
 * list.</dd>
 * 
 * <dt>{ [e1]; [e2]; ...; [en] }</dt>
 * <dd>Evaluates the side-effect expressions <code>[e1]</code>, ..., <code>[en]</code> in the given
 * order and yields the result of the evaluation of <code>[en]</code>.</dd>
 * 
 * <dt>toString([value1], [value2], ...)</dt>
 * <dd>Converts and concatenates all given values using their corresponding label provider. List
 * values are automatically flattened.</dd>
 * 
 * <dt>numberFormat([pattern])</dt>
 * <dd>Constructs a number format with the given pattern for either <code>parse()</code> or
 * <code>format()</code> calls. See {@link DecimalFormat}.</dd>
 * 
 * <dt>dateFormat([pattern])</dt>
 * <dd>Constructs a date format with the given pattern for either <code>parse()</code> or
 * <code>format()</code> calls. See {@link SimpleDateFormat}.</dd>
 * 
 * <dt>messageFormat([pattern])</dt>
 * <dd>Constructs a message format with the given pattern for either <code>parse()</code> or
 * <code>format()</code> calls. See {@link MessageFormat}.</dd>
 * 
 * <dt>[format].format([value1],...)</dt>
 * <dd>Uses the given format to format the given value(s). If more than one value is given, the
 * format is called with an array of those values (useful for <code>messageFormat()</code>).</dd>
 * 
 * <dt>[format].parse([string])</dt>
 * <dd>Uses the given format to parse the given text and returns the retrieved value.</dd>
 * 
 * <dt>date([year], [month], [day])</dt>
 * <dd>Constructs a date value representing a day of a year (in system calendar) compatible with
 * <code>tl.core:Date</code> values. Note: The month is zero-based (January is zero) but the day is
 * one-based (the first of a month is 1).</dd>
 * 
 * <dt>dateTime([year], [month], [day], [hour], [minute], [second], [millisecond])</dt>
 * <dd>Constructs a date value representing a point in time (in user calendar) compatible with
 * <code>tl.core:DateTime</code> values. Note: The month is zero-based (January is zero) but the day
 * is one-based (the first of a month is 1). The hour, minute, second and millisecond values are
 * optional.</dd>
 * 
 * <dt>now()</dt>
 * <dd>Constructs a date value representing the current point in time (in user calendar) compatible
 * with <code>tl.core:DateTime</code> values.</dd>
 * 
 * <dt>today()</dt>
 * <dd>Constructs a date value representing the current day of the year as seen by the user calendar
 * represented as <code>tl.core:Date</code> value (in system calendar).</dd>
 * 
 * <dt>[date].toSystemCalendar()</dt>
 * <dd>Promotes a date value to a (system) calendar to access fields such as year, month and so on.
 * The date value is expected to be of type <code>tl.core:Date</code>.</dd>
 * 
 * <dt>[date].toUserCalendar()</dt>
 * <dd>Promotes a date value to a (user) calendar to access fields such as year, month and so on.
 * The date value is expected to be of type <code>tl.core:DateTime</code>.</dd>
 * 
 * <dt>[calendar].toDate()</dt>
 * <dd>Unwraps the date represented by a given calendar. Depending on the calendar
 * (<code>systemCalendar()</code> or <code>userCalendar()</code>) the value is compatible with
 * <code>tl.core:Date</code> or <code>tl.core:DateTime</code>.</dd>
 * 
 * <dt>[string].toDate()</dt>
 * <dd>Parses a date represented in XML date time format, see {@link XmlDateTimeFormat}.</dd>
 * 
 * <dt>[number].toDate()</dt>
 * <dd>Constructs a date value by converting a system millisecond value (since
 * 1970-01-01:00:00:00.000).</dd>
 * 
 * <dt>[date].toMillis()</dt>
 * <dd>Constructs a millisecond value (since 1970-01-01:00:00:00.000) from the given date
 * value.</dd>
 * 
 * <dt>[calendar].year()</dt>
 * <dt>[calendar].month()</dt>
 * <dt>[calendar].day()</dt>
 * <dt>[calendar].hour()</dt>
 * <dt>[calendar].minute()</dt>
 * <dt>[calendar].second()</dt>
 * <dt>[calendar].millisecond()</dt>
 * <dt>[calendar].era()</dt>
 * <dt>[calendar].week()</dt>
 * <dt>[calendar].weekOfMonth()</dt>
 * <dt>[calendar].dayOfWeek()</dt>
 * <dt>[calendar].dayOfWeekInMonth()</dt>
 * <dt>[calendar].dayOfYear()</dt>
 * <dt>[calendar].amPm()</dt>
 * <dt>[calendar].hour12()</dt>
 * <dt>[calendar].zoneOffset()</dt>
 * <dt>[calendar].dstOffset()</dt>
 * <dd>The corresponding field of the given calendar (see <code>systemCalendar()</code> and
 * <code>userCalendar()</code>). See
 * {@link com.top_logic.model.search.expr.CalendarField.Field}.</dd>
 * 
 * <dt>[calendar].withYear([value])</dt>
 * <dt>[calendar].withMonth([value])</dt>
 * <dt>[calendar].withDay([value])</dt>
 * <dt>[calendar].withHour([value])</dt>
 * <dt>[calendar].withMinute([value])</dt>
 * <dt>[calendar].withSecond([value])</dt>
 * <dt>[calendar].withMillisecond([value])</dt>
 * <dt>[calendar].withEra([value])</dt>
 * <dt>[calendar].withWeek([value])</dt>
 * <dt>[calendar].withWeekOfMonth([value])</dt>
 * <dt>[calendar].withDayOfWeek([value])</dt>
 * <dt>[calendar].withDayOfWeekInMonth([value])</dt>
 * <dt>[calendar].withDayOfYear([value])</dt>
 * <dt>[calendar].withAmPm([value])</dt>
 * <dt>[calendar].withHour12([value])</dt>
 * <dt>[calendar].withZoneOffset([value])</dt>
 * <dt>[calendar].withSstOffset([value])</dt>
 * <dd>Creates a new calendar from the given one with the corresponding field set to the given value
 * (see <code>systemCalendar()</code> and <code>userCalendar()</code>). See
 * {@link com.top_logic.model.search.expr.CalendarField.Field}.</dd>
 * 
 * <dt>[calendar].withYearAdded([value])</dt>
 * <dt>[calendar].withMonthAdded([value])</dt>
 * <dt>[calendar].withDayAdded([value])</dt>
 * <dt>[calendar].withHourAdded([value])</dt>
 * <dt>[calendar].withMinuteAdded([value])</dt>
 * <dt>[calendar].withSecondAdded([value])</dt>
 * <dt>[calendar].withMillisecondAdded([value])</dt>
 * <dt>[calendar].withEraAdded([value])</dt>
 * <dt>[calendar].withWeekAdded([value])</dt>
 * <dt>[calendar].withWeekOfMonthAdded([value])</dt>
 * <dt>[calendar].withDayOfWeekAdded([value])</dt>
 * <dt>[calendar].withDayOfWeekInMonthAdded([value])</dt>
 * <dt>[calendar].withDayOfYearAdded([value])</dt>
 * <dt>[calendar].withAmPmAdded([value])</dt>
 * <dt>[calendar].withHour12Added([value])</dt>
 * <dt>[calendar].withZoneOffsetAdded([value])</dt>
 * <dt>[calendar].withSstOffsetAdded([value])</dt>
 * <dd>Creates a new calendar from the given one with the corresponding field incremented by the
 * given value (see <code>systemCalendar()</code> and <code>userCalendar()</code>). See
 * {@link com.top_logic.model.search.expr.CalendarField.Field}.</dd>
 * 
 * </dl>
 * 
 * @see QueryExecutor#compile(Expr)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
@Format(ExprFormat.class)
@ItemDisplay(ItemDisplayType.VALUE)
@PropertyEditor(TLScriptPropertyEditor.class)
@ControlProvider(TLScriptCodeEditorControl.PlainCP.class)
@CssClass("tlscript")
@Label("Expression")
@RenderWholeLine
public interface Expr extends ConfigurationItem {

	/**
	 * Accepts the given {@link ExprVisitor}.
	 */
	<R, A, E extends Throwable> R visit(ExprVisitor<R, A, E> v, A arg) throws E;

	/**
	 * A literal value.
	 */
	@Abstract
	public interface Literal extends Expr {
		// Pure marker interface.
	}

	/**
	 * The <code>null</code> literal.
	 */
	@TagName("null")
	public interface Null extends Literal {
		// Pure marker interface.
	}

	/**
	 * The boolean <code>true</code> literal.
	 */
	@TagName("true")
	public interface True extends Literal {
		// Pure marker interface.
	}

	/**
	 * The boolean <code>false</code> literal.
	 */
	@TagName("false")
	public interface False extends Literal {
		// Pure marker interface.
	}

	/**
	 * A {@link Literal} that has its value given in the configuration.
	 */
	@Abstract
	public interface ValueLiteral<T> extends Literal {
		// Pure marker interface.
	}

	/**
	 * A {@link String} literal.
	 */
	@TagName("string")
	public interface StringLiteral extends ValueLiteral<String> {
		/**
		 * The literal value.
		 */
		String getValue();

		/** @see #getValue() */
		void setValue(String value);
	}

	/**
	 * A HTML macro with potentially {@link EmbeddedExpression}s.
	 */
	@TagName("html")
	public interface Html extends Expr {
		/**
		 * The HTML contents.
		 */
		@DefaultContainer
		List<HtmlContent> getContents();
	}

	/**
	 * Marker for possible {@link Html#getContents()}.
	 */
	@Abstract
	public interface HtmlContent extends Expr {
		// Marker.
	}

	/**
	 * Textual content of a {@link Html} macro.
	 */
	@TagName("text")
	public interface TextContent extends HtmlContent, AttributeContent {

		/**
		 * The literal text.
		 */
		String getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(String text);
	}

	/**
	 * Base interface for HTML tags.
	 */
	@Abstract
	public interface AbstractTag extends HtmlContent {
		/**
		 * The tag name.
		 */
		String getTag();

		/**
		 * @see #getTag()
		 */
		void setTag(String name);

	}

	/**
	 * A HTML start tag.
	 */
	@TagName("start")
	public interface StartTag extends AbstractTag {
		/**
		 * @see #isEmpty()
		 */
		String EMPTY = "empty";

		/**
		 * Attributes of {@link #getTag()}
		 */
		@DefaultContainer
		List<Attribute> getAttributes();

		/**
		 * Whether the tag is empty (has no explicit end tag).
		 */
		@Name(EMPTY)
		boolean isEmpty();

		/**
		 * @see #isEmpty()
		 */
		void setEmpty(boolean value);

		/**
		 * Looks up an attribute by name.
		 */
		default AttributeContent getAttribute(String typeAttr) {
			for (Attribute attr : getAttributes()) {
				if (typeAttr.equals(attr.getName())) {
					return attr.getValue();
				}
			}
			return null;
		}
	}

	/**
	 * A HTML end tag.
	 */
	@TagName("end")
	public interface EndTag extends AbstractTag {
		// Pure marker.
	}

	/**
	 * An attribute of a {@link StartTag}.
	 */
	@TagName("attribute")
	public interface Attribute extends Expr {
		/**
		 * The attribute name.
		 */
		String getName();

		/**
		 * @see #getName()
		 */
		void setName(String name);

		/**
		 * The attribute value.
		 */
		@DefaultContainer
		AttributeContent getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(AttributeContent value);

	}

	/**
	 * Marker for all valid contents of {@link Attribute#getValue()}.
	 */
	@Abstract
	public interface AttributeContent extends Expr {
		// Marker.
	}

	/**
	 * Collection of {@link AttributeContent} for attributes that are composed of multiple parts.
	 */
	@TagName("sequence")
	public interface AttributeContents extends AttributeContent {
		/**
		 * Sequence of {@link AttributeContent} values to be expanded within the attribute.
		 */
		@DefaultContainer
		List<AttributeContent> getValues();
	}

	/**
	 * An {@link Expr} embedded within a {@link Html} macro.
	 */
	@TagName("embed")
	public interface EmbeddedExpression extends HtmlContent, AttributeContent {
		/**
		 * The expression to be evaluated.
		 */
		@DefaultContainer
		Expr getExpr();

		/**
		 * @see #getExpr()
		 */
		void setExpr(Expr expr);
	}

	/**
	 * A reference to a {@link ResKey}.
	 */
	@TagName("reskey")
	public interface ResKeyReference extends ValueLiteral<String> {
		/**
		 * The key.
		 */
		String getKey();

		/** @see #getKey() */
		void setKey(String value);
	}

	/**
	 * A string literal in multiple languages.
	 */
	@TagName("i18n")
	public interface ResKeyLiteral extends ValueLiteral<String> {

		/** Configuration name of the value {@link #getKey()}. */
		String KEY = "key";

		/**
		 * Name of the key relative to the surrounding {@link ResKeyLiteral}.
		 * 
		 * @return May be <code>null</code> when no key is given.
		 */
		@Name(KEY)
		@Nullable
		String getKey();

		/** Setter for {@link #getKey()}. */
		void setKey(String key);

		/**
		 * Translations in different languages.
		 */
		@DefaultContainer
		@Key(LangStringConfig.LANG)
		Map<Locale, LangStringConfig> getValues();

		/**
		 * Suffix {@link ResKeyLiteral} indexed by their {@link #getKey()}.
		 */
		@Key(KEY)
		Map<String, ResKeyLiteral> getSuffixes();


		/**
		 * A language tagged string.
		 */
		interface LangStringConfig extends ConfigurationItem, ResKey.LangString {

			/**
			 * @see #getLang()
			 */
			void setLang(Locale value);

			/**
			 * @see #getText()
			 */
			void setText(String value);
		}

	}

	/**
	 * A {@link Number} literal.
	 */
	@TagName("number")
	public interface NumberLiteral extends ValueLiteral<Number> {
		/**
		 * The literal value.
		 */
		double getValue();

		/** @see #getValue() */
		void setValue(double value);
	}

	/**
	 * A {@link Literal} referencing the application model.
	 */
	@Abstract
	public interface ModelLiteral extends Literal {
		// Pure marker interface.
	}

	/**
	 * {@link ModelLiteral} referencing a {@link TLModule}.
	 */
	@TagName("module")
	public interface ModuleLiteral extends ModelLiteral {

		/**
		 * The module name.
		 */
		String getName();

		/** @see #getName() */
		void setName(String value);

	}

	/**
	 * {@link ModelLiteral} referencing a {@link TLModule#getSingleton(String) singleton}.
	 */
	@TagName("singleton")
	public interface SingletonLiteral extends ModelLiteral {

		/**
		 * The module name.
		 */
		@Mandatory
		String getModule();

		/** @see #getModule() */
		void setModule(String value);

		/**
		 * The singleton name.
		 */
		@StringDefault(TLModule.DEFAULT_SINGLETON_NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);

	}

	/**
	 * {@link ModelLiteral} referencing a type or part thereof.
	 */
	@Abstract
	public interface AbstractTypeLiteral extends ModelLiteral {

		/**
		 * The owning {@link TLModule} of the referenced model part.
		 */
		String getModule();
	
		/** @see #getModule() */
		void setModule(String value);
	
	}

	/**
	 * {@link ModelLiteral} referencing a {@link TLType}.
	 */
	@TagName("type")
	public interface TypeLiteral extends AbstractTypeLiteral {

		/**
		 * The type name.
		 */
		String getName();

		/** @see #getName() */
		void setName(String value);
	
	}

	/**
	 * {@link ModelLiteral} referencig a {@link TLTypePart}.
	 */
	@TagName("part")
	public interface PartLiteral extends AbstractTypeLiteral {

		/**
		 * The owning type name.
		 */
		String getType();

		/** @see #getType() */
		void setType(String value);

		/**
		 * The name of the referenced {@link TLTypePart}.
		 */
		String getName();

		/** @see #getName() */
		void setName(String value);

	}

	/**
	 * A variable reference.
	 * 
	 * @see Define#getName()
	 */
	@TagName("var")
	public interface Var extends Expr {

		/**
		 * Name of the referenced context variable.
		 */
		String getName();
	
		/** @see #getName() */
		void setName(String value);
	
	}

	/**
	 * {@link Expr} wrapper.
	 * 
	 * <p>
	 * Useful for the top-level property of a configuration item to allow using custom tag names for
	 * selecting the top-level type or as result from a parser to keep the originally formatted
	 * source code.
	 * </p>
	 */
	public interface Wrapped extends Expr {

		/** @see #getSrc() */
		String SRC = "src";

		/**
		 * The original source code of {@link #getExpr()}, when created from a parser,
		 * <code>null</code> otherwise.
		 */
		@Nullable
		@Name(SRC)
		String getSrc();

		/** @see #getSrc() */
		void setSrc(String value);

		/**
		 * The wrapped {@link Expr}.
		 */
		@DefaultContainer
		@Mandatory
		Expr getExpr();

		/** @see #getExpr() */
		void setExpr(Expr result);

	}

	/**
	 * {@link Expr} taking two arguments.
	 */
	@Abstract
	public interface Operation extends Expr {

		/**
		 * The operands to be combined in this {@link Operation}.
		 */
		@DefaultContainer
		List<Expr> getOperands();

	}

	/**
	 * Addition.
	 */
	@TagName("add")
	public interface Add extends Operation {
		// Marker interface.
	}

	/**
	 * Subtraction.
	 */
	@TagName("sub")
	public interface Sub extends Operation {
		// Marker interface.
	}

	/**
	 * Multiplication.
	 */
	@TagName("mul")
	public interface Mul extends Operation {
		// Marker interface.
	}

	/**
	 * Division.
	 */
	@TagName("div")
	public interface Div extends Operation {
		// Marker interface.
	}

	/**
	 * Modulo operation.
	 */
	@TagName("mod")
	public interface Mod extends Operation {
		// Marker interface.
	}

	/**
	 * Compare operator.
	 */
	@TagName("cmp")
	public interface Cmp extends Operation {

		/**
		 * The comparison to perform.
		 */
		CompareKind getKind();

		/**
		 * See {@link #getKind()}
		 */
		void setKind(CompareKind value);

	}

	/**
	 * Boolean and operator.
	 */
	@TagName("and")
	public interface And extends Operation {
		// Pure marker interface.
	}

	/**
	 * Boolean or operator.
	 */
	@TagName("or")
	public interface Or extends Operation {
		// Pure marker interface.
	}

	/**
	 * Boolean not operator.
	 */
	@TagName("not")
	public interface Not extends Expr {
	
		/**
		 * The negated boolean value.
		 */
		@DefaultContainer
		@Mandatory
		Expr getExpr();
	
		/** @see #getExpr() */
		void setExpr(Expr value);
	
	}

	/**
	 * Equality comparison.
	 */
	@TagName("eq")
	public interface Eq extends Operation {
		// Pure marker interface.
	}

	/**
	 * Binding of a value to a context variable.
	 */
	@TagName("define")
	public interface Define extends Expr {

		/**
		 * The variable name to which the context value is bound.
		 */
		String getName();

		/** @see #getName() */
		void setName(String value);

		/**
		 * The expression to evaluate with the {@link #getName() variable binding} in scope.
		 */
		@DefaultContainer
		@Mandatory
		Expr getExpr();

		/** @see #getExpr() */
		void setExpr(Expr value);

	}

	/**
	 * Binding of the value of an expression to a local variable.
	 */
	@TagName("assign")
	public interface Assign extends Expr {

		/**
		 * The name of the variable that is bound.
		 */
		String getName();

		/** @see #getName() */
		void setName(String value);

		/**
		 * The expression delivering the value that is bound to the variable with
		 * {@link #getName()}.
		 */
		@DefaultContainer
		@Mandatory
		Expr getExpr();

		/** @see #getExpr() */
		void setExpr(Expr value);

	}

	/**
	 * Index access to a collection value.
	 */
	@TagName("at")
	public interface At extends Expr {

		/**
		 * The collection being accessed.
		 */
		@DefaultContainer
		@Mandatory
		Expr getSelf();

		/** @see #getSelf() */
		void setSelf(Expr value);

		/**
		 * The index expression.
		 */
		@Mandatory
		Expr getIndex();

		/** @see #getIndex() */
		void setIndex(Expr value);

	}

	/**
	 * Function application.
	 */
	@TagName("apply")
	public interface Apply extends Expr {

		/**
		 * The function to invoke with {@link #getArg()}.
		 */
		@Mandatory
		Expr getFun();

		/** @see #getFun() */
		void setFun(Expr value);

		/**
		 * The argument to pass to {@link #getFun()}.
		 */
		@Mandatory
		Expr getArg();

		/** @see #getArg() */
		void setArg(Expr value);

	}

	/**
	 * Cross product of set expressions creating tuple lists.
	 */
	@TagName("tuple")
	public interface Tuple extends Expr {

		/**
		 * The coordinates the resulting tuples are composed of.
		 */
		@DefaultContainer
		List<Coord> getCoords();

		/**
		 * A single coordinate producing values in a {@link Tuple}.
		 */
		public interface Coord extends ConfigurationItem {

			/**
			 * The name of the coordinate.
			 */
			String getName();

			/**
			 * @see #getName()
			 */
			void setName(String value);

			/**
			 * Whether the <code>null</code> value is produced, if {@link #getExpr()} generates the
			 * empty list.
			 * 
			 * <p>
			 * If a {@link Tuple} has a non-optional coordinate whose {@link #getExpr()} generates
			 * the empty list, the result of the {@link Tuple} expression is empty.
			 * </p>
			 */
			boolean isOptional();

			/**
			 * @see #isOptional()
			 */
			void setOptional(boolean value);

			/**
			 * The {@link Expr} producing the set of values for this coordinate.
			 * 
			 * <p>
			 * The expression may contain {@link Var free variables} referring to the
			 * {@link #getName() names} of {@link Tuple#getCoords() preceding coordinates} of the
			 * surrounding {@link Tuple}. During execution, these variables are bound to the actual
			 * values of the preceding coordinates to the produce concrete tuple value.
			 * </p>
			 */
			@DefaultContainer
			Expr getExpr();

			/**
			 * @see #getExpr()
			 */
			void setExpr(Expr value);
		}

	}

	/**
	 * Direct access to the context value.
	 * 
	 * @see Define#getName()
	 */
	@TagName("block")
	public interface Block extends Expr {
		/**
		 * The contents to be executed in sequence.
		 */
		@DefaultContainer
		List<Expr> getContents();

		/**
		 * @see #getContents()
		 */
		void setContents(List<Expr> contents);
	}

	/**
	 * {@link Expr} representing a globally defined function.
	 */
	@Abstract
	public interface AbstractMethod extends Expr {

		/**
		 * The name of the method to call.
		 */
		String getName();

		/** @see #getName() */
		void setName(String value);

		/**
		 * The arguments to pass to the invokation.
		 */
		List<Arg> getArgs();

		/** @see #getArgs() */
		void setArgs(List<Arg> value);

	}

	/**
	 * An argument to a (built-in) method call.
	 */
	public interface Arg extends ConfigurationItem {
		/**
		 * An optional name in case of a named argument.
		 * 
		 * <p>
		 * If the name is <code>null</code>, a positional argument was given.
		 * </p>
		 */
		@Nullable
		String getName();

		/**
		 * @see #getValue()
		 */
		void setName(String name);

		/**
		 * The argument value.
		 */
		@DefaultContainer
		Expr getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(Expr expr);
	}

	/**
	 * A method invocation with instance syntax <code>self.method(...)</code>.
	 */
	@TagName("method")
	public interface Method extends AbstractMethod {

		/**
		 * The value to invoke the method on.
		 */
		@DefaultContainer
		@Mandatory
		Expr getSelf();

		/** @see #getSelf() */
		void setSelf(Expr value);

	}

	/**
	 * A method invocation with static syntax <code>method(...)</code>.
	 */
	@TagName("staticMethod")
	public interface StaticMethod extends AbstractMethod {
		// Pure marker interface.
	}

	/**
	 * A qualified (dot-separated) name.
	 */
	public static class QName extends ArrayList<String> {

		@Override
		public String toString() {
			return StringServices.join(this, ".");
		}

	}

	/**
	 * Method argument list.
	 */
	public static class Args extends ArrayList<Arg> {
		// Pure marker class.
	}

}
