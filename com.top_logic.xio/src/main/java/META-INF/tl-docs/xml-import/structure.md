---
description: Read before structuring an XML import definition (com.top_logic.xio) by tag names and conditions - <handler> and <dispatch> with <tag> and <default>, the unknown option (skip / warn / error, UnknownElements), <skip-deep>, <no-op>, <log>, the attribute-value switch <switch> with <case value> and <default>, the predicate switch <switch-block> with <case> / <if> and <else>, <chain>, <attribute-check>, and plugging in an own Handler class with <entry class>.
order: 10
---

# Dispatch and control flow

These handlers decide which handlers an element gets, without creating or changing objects themselves. For the reader-position contract they follow, see [the chapter index](doc:xml-import#the-reader-position-contract).

## `<dispatch>` and `<tag>`

`<dispatch>` (`DispatchingImporter`) reads the children of the current element one after the other and applies to each child the handlers of the `<tag>` whose `name` is the child's local name (namespaces are not considered). It consumes the current element. The root of a definition, `<handler>`, is a `<dispatch>` over the root element of the document; a definition may also start with `<dispatch>` directly.

```xml
<dispatch>
	<tag name="foo">
		...
	</tag>
	<tag name="bar">
		...
	</tag>
</dispatch>
```

A `<tag>` holds any number of handlers, applied in sequence to the child element; the last one has to consume it (see [the reader-position contract](doc:xml-import#the-reader-position-contract)). An empty `<tag name="note"/>` skips the element like an empty `<dispatch/>`, i.e. it skips its children according to the [unknown](#unknown-elements) setting.

The result of a `<dispatch>` is the last non-null result of its children's handlers, e.g. the last created object.

### Default handler

A child without a matching `<tag>` gets the handler in `<default>`, if there is one. `<default>` holds a single handler; several are wrapped in a [`<group>`](doc:xml-import#handler-ids-ref-and-group).

```xml
<dispatch>
	<default>
		<object model-type="test.com.top_logic.xio.importer.test12:Item">
			<property name="name" xml-attribute="name"/>
			<linking>
				<list-linking name="test.com.top_logic.xio.importer.test12:Item#children"/>
			</linking>
			<dispatch/>
		</object>
	</default>
</dispatch>
```

### Unknown elements

A child that has neither a `<tag>` nor a `<default>` is skipped with its whole subtree. The option `unknown` (`UnknownElements`) decides whether this is reported:

| `unknown` | Effect |
| --- | --- |
| `skip` | skipped silently (the default at the top level) |
| `warn` | a warning `Unknown element "<tag>" skipped.` with the document location |
| `error` | an error `Unknown element "<tag>".` with the document location |

The setting holds for the whole subtree of the `<dispatch>` that sets it, i.e. for every nested `<dispatch>` (also one applied through a `<ref>`) that does not set it again. Set on the root `<handler>`, it holds for the whole definition:

```xml
<handler unknown="warn">
	<tag name="data">
		<dispatch>
			<tag name="item">
				<object model-type="test.com.top_logic.xio.importer.test12:Item">
					<property name="name" xml-attribute="name"/>
					<dispatch>
						<tag name="note"/>
					</dispatch>
				</object>
			</tag>
			<tag name="strict">
				<object model-type="test.com.top_logic.xio.importer.test12:Item">
					<dispatch unknown="error">
						<tag name="child">
							...
						</tag>
					</dispatch>
				</object>
			</tag>
		</dispatch>
	</tag>
</handler>
```

An element that has a `<default>` is never unknown, so `unknown` has no effect on a `<dispatch>` with a `<default>` itself (but still on the nested ones).

## `<skip-deep>`, `<no-op>`, `<log>`

- `<skip-deep>` (`SkipDeep`) skips the current element with its whole subtree and consumes it. With `warn="true"` it writes an info message naming the skipped tag. Unlike an empty `<dispatch/>`, it ignores the `unknown` setting.
- `<no-op>` (`NoOp`) does nothing and does not consume the element. It fills a place that needs a handler, e.g. a `<case>` of a `<switch>` whose element is consumed by a handler next to the switch.
- `<log message="..."/>` (`LogHandler`) writes the internationalized `message` (a resource key) as info to the import log, together with the document location. It does not consume the element.

## `<switch>`: by attribute value

`<switch>` (`PropertySwitch`) reads the XML attribute `xml-attribute` of the current element and applies the handler of the `<case>` whose `value` lists the attribute value. `value` is a comma-separated list (`CaseValues`): each entry selects the case, entries are trimmed, and an empty entry (or `value=""`) stands for the empty attribute value - `<case value="0, false,">` also matches `flag=""`. A value must not appear in two cases of the same switch (a configuration error). A value without a case gets the handler of `<default>`; without `<default>`, nothing is done. Like `<default>`, a `<case>` holds a single handler.

```xml
<object model-type="test.com.top_logic.xio.importer.test13:Item">
	<property name="name" xml-attribute="name"/>
	<switch xml-attribute="flag">
		<case value="1, true">
			<assign property="state" value="x -> 'on'"/>
		</case>
		<case value="0, false,">
			<assign property="state" value="x -> 'off'"/>
		</case>
		<default>
			<assign property="state" value="x -> 'unknown'"/>
		</default>
	</switch>
	<dispatch/>
</object>
```

If the element has no such attribute at all, no handler is applied, not even the default. The switch then does not consume the element, so a switch whose cases consume the element must be followed by a consuming handler for that case, or it leaves the element unconsumed:

```xml
<tag name="property">
	<switch xml-attribute="name">
		<case value="label_en">
			<set-translation name="label" language="en"/>
		</case>
		<case value="label_de">
			<set-translation name="label" language="de"/>
		</case>
		<default>
			<dispatch/>
		</default>
	</switch>
</tag>
```

## `<switch-block>`: by predicate

`<switch-block>` (`SwitchImportHandler`) tests the TL-Script `predicate` of each `<case>` in order and applies the handlers of the first case that evaluates to `true`; if none does, the handlers in `<else>`. A `<case>` is a `ConditionalHandler`, whose own tag name `<if>` can be used instead of `<case>`. The predicate is a function receiving the values of the variables listed in `arguments` (comma-separated, default `this`). A case and `<else>` hold any number of handlers.

```xml
<object-ref id-attribute="id">
	<switch-block>
		<case predicate="x -> $x.instanceOf(`test.com.top_logic.xio.importer.test2:A`)">
			<linking>
				<list-linking name="test.com.top_logic.xio.importer.test2:V#as"/>
			</linking>
		</case>
		<case predicate="x -> $x.instanceOf(`test.com.top_logic.xio.importer.test2:B`)">
			<linking>
				<list-linking name="test.com.top_logic.xio.importer.test2:V#bs"/>
			</linking>
		</case>
		<else>
			<linking>
				<list-linking name="test.com.top_logic.xio.importer.test2:V#others"/>
			</linking>
		</else>
	</switch-block>
</object-ref>
```

`<switch-block>` itself does not consume the element; its result is `null`. An `<if predicate="...">` used on its own applies its handlers if the predicate holds and does nothing otherwise.

An argument that is a forward reference to an object not yet imported (see [Forward references](doc:xml-import/objects#forward-references)) is passed when the object is defined. The predicate and the handlers of the selected case then run later, when the reader is no longer at the current element. In such a switch, use only handlers that do not read the input, e.g. `<linking>` and `<assign>` - as in the example, where `<object-ref>` may refer to an object defined further below.

## `<chain>`

`<chain>` (`ChainHandler`) applies its handlers in sequence to the current element, each with the variable `chain-var` bound to the result of the previous handler. The first handler gets the value of the variable `input-var`. Both default to `scope`. The result of the chain is the result of the last handler. As in any handler list, only the last handler may consume the element.

## `<attribute-check>`

`<attribute-check xml-attribute="version" value="2\..*"/>` (`AttributeCheck`) reports an error if the value of the XML attribute does not match the regular expression `value`. It does not consume the element. The attribute is expected to be present.

## Own handlers

A `Handler` implementation of another module is used with the generic entry syntax and its class name, e.g. in the BPMN import:

```xml
<tag name="extensionElements">
	<entry class="com.top_logic.bpe.bpml.importer.HandleTLExtensions"/>
</tag>
```

A handler whose config interface has a `@TagName` can be used by that tag name in every definition, because `XmlImporter` registers all `Handler` implementations found by the `TypeIndex` with their tag names. An own handler follows the [reader-position contract](doc:xml-import#the-reader-position-contract) and accesses the model only through the `ImportContext` it is passed, so that forward references and the `ModelBinding` keep working.
