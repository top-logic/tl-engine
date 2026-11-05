---
description: Read before writing or changing an XML import definition (the <handler> / <dispatch> declaration interpreted by com.top_logic.xio.importer.XmlImporter, e.g. a *.importer.xml, the import-definition of an XMLImportCommand or the handler of a ParseXml TL-Script function) - how a definition is structured and run, the reader-position contract every handler follows, the variables this and scope, handler IDs with <ref handler-id> and <group>, and which article of the chapter describes which tag.
order: 70
---

# XML import

The module `com.top_logic.xio` imports arbitrary XML documents into a TopLogic model. What the import does is not programmed but declared: an *import definition* is an XML configuration of `Handler` implementations (package `com.top_logic.xio.importer.handlers`). Each handler is a small building block - dispatch on a tag name, create an object, read an attribute into a property, link an object to its parent - and the nesting of the handlers in the definition mirrors the nesting of the elements in the imported document.

The articles of this chapter describe the syntax of an import definition:

| Article | Tags |
| --- | --- |
| [Dispatch and control flow](doc:xml-import/structure) | `<handler>`, `<dispatch>`, `<tag>`, `<default>`, `unknown`, `<skip-deep>`, `<no-op>`, `<log>`, `<switch>`, `<switch-block>`, `<if>`, `<chain>`, `<attribute-check>`, custom handlers |
| [Objects and IDs](doc:xml-import/objects) | `<object>`, `<object-ref>`, `<forward>`, `<with-result>`, `<alternative-id>`, forward references, `join-duplicates`, `ignore-unresolved` |
| [Values](doc:xml-import/values) | `<property>`, `<text-content>`, `<xml-content-property>`, `<reference>`, `<multi-reference>`, `<set-translation>`, `<assign>` |
| [Linking](doc:xml-import/linking) | `<linking>`, `<list-linking>`, `<ref-linking>`, `<custom-linking>` |
| [Variables and scripts](doc:xml-import/variables) | `this`, `scope`, `var`, `<with-var>`, `<in-scope>`, the TL-Script expressions of a definition and their arguments |
| [Importing into existing data](doc:xml-import/existing-data) | `<resolve-existing>`, `<unless-known>` with `<update>`, `<group>` reuse, `<update-known>` |

All tag and attribute names in these articles are the configuration names of the handler config interfaces (`@TagName`, `@Name`, or the getter name: `getIdAttribute()` is `id-attribute`). The test definitions `test/com/top_logic/xio/importer/*.importer.xml` in the module's test sources and the BPMN import `com/top_logic/bpe/bpml/importer/importBpml.xml` are complete working examples.

## A first definition

The document

```xml
<data>
	<item name="first"/>
	<item name="second"/>
</data>
```

is imported into the `elements` of a container object by

```xml
<handler>
	<tag name="data">
		<dispatch>
			<tag name="item">
				<object model-type="my.module:Item">
					<property name="name" xml-attribute="name"/>
					<linking>
						<list-linking name="my.module:Container#elements"/>
					</linking>
					<dispatch/>
				</object>
			</tag>
		</dispatch>
	</tag>
</handler>
```

The root `<handler>` is a `<dispatch>` (`DispatchingImporter`) over the root element of the document. For `<data>` it applies the inner `<dispatch>`, which applies the `<object>` to each `<item>`. The `<object>` creates an `my.module:Item`, sets its `name` from the XML attribute `name`, adds it to the `elements` of the container, and skips the (empty) content of `<item>` with the trailing `<dispatch/>`. The container is passed as context of the import, see [Running an import](#running-an-import).

## The reader-position contract

A handler works on a `javax.xml.stream.XMLStreamReader`. Every handler follows the same contract:

- A handler is called with the reader at the `START_ELEMENT` of the *current element* (only the root handler starts at the beginning of the document).
- A handler either *consumes* the element - it returns with the reader at the matching `END_ELEMENT` - or it leaves the reader where it is, at the `START_ELEMENT`.
- Where a definition lists several handlers in a row (the content of a `<tag>`, of an `<object>`, of a `<group>`), they are applied one after the other *to the same element*. The non-consuming handlers therefore come first, and the last handler consumes the element.

Consuming handlers are `<dispatch>`, `<skip-deep>`, `<text-content>`, `<xml-content-property>`, `<update-known>`, `<set-translation>` without `xml-attribute`, and an object handler with `id-attribute="text()"`. Handlers that only read attributes of the current element (`<property>`, `<reference>`, `<multi-reference>`, `<set-translation xml-attribute="...">`, `<alternative-id>`, `<with-var>` and the like) or that read nothing at all (`<linking>`, `<assign>`, `<log>`, `<no-op>`) do not consume it. A handler with content (`<object>`, `<object-ref>`, `<group>`, `<with-var>`, `<in-scope>`, ...) consumes the element if its own content does.

That is why the `<object>` above ends with `<dispatch/>`: an empty `<dispatch/>` skips all child elements of the current element and consumes it. The `<dispatch>` checks after the handlers of a `<tag>` that the element was consumed; if not, it reports `Expected to have consumed all data up to the matching end element of '<tag>'.` An `<object>` without any content handler consumes its element as well, because an empty content list is an empty `<dispatch/>`.

The handlers of a `<tag>` may also stand side by side instead of nested:

```xml
<tag name="a">
	<object model-type="my.module:A" fill-forward-var="content">
		<property name="name" xml-attribute="name"/>
	</object>
	<dispatch/>
</tag>
```

Here the `<object>` does not consume `<a>` (its only content handler reads an attribute) and the `<dispatch/>` next to it does.

## The variables `this` and `scope`

Handlers communicate through variables of the import context (`ImportContext`). Two of them are always involved:

- `this` - the object the current element is about. An object handler (`<object>`, `<object-ref>`, ...) binds `this` to its object for its content.
- `scope` - the object of the enclosing element. An object handler binds `scope` to the `this` of its caller before it creates or resolves its own object.

So within the content of an `<object>`, `this` is the new object and `scope` its parent. The handlers that store values default to `this` (`target-var="this"` of `<property>`, `<reference>`, `<set-translation>`, ...), and `<linking>` links `this` into `scope`. Further variables are introduced with `var` on an object handler, with `<with-var>` and with `<in-scope>`, see [Variables and scripts](doc:xml-import/variables). A variable binding holds for the content of the handler that binds it, so an inner binding hides an outer one of the same name.

## Handler IDs, `<ref>` and `<group>`

Every handler may carry an `id`. A `<ref handler-id="..."/>` anywhere in the same definition applies the handler with this ID at its own position, with the variables in effect there. IDs are unique within a definition, and a `<ref>` may name a handler that is defined further below.

A `<ref>` to an enclosing `<dispatch>` imports a recursive structure of any depth:

```xml
<dispatch id="tree">
	<tag name="node">
		<object model-type="test.tree:Node">
			<property name="name" xml-attribute="name"/>
			<linking>
				<list-linking name="test.tree:Node#children"/>
			</linking>
			<ref handler-id="tree"/>
		</object>
	</tag>
</dispatch>
```

A `<group>` (`GroupHandler`) bundles a list of handlers under one ID without doing anything else: it applies its content to the current element, neither changes a variable nor consumes more input than its content does. It exists to share a set of handlers between places of the definition - typically the attribute handlers of an object, shared between its creation and the update of an already known object (see [Sharing handlers with group](doc:xml-import/existing-data#sharing-handlers-with-group)):

```xml
<object model-type="test.com.top_logic.xio.importer.test16:Item" id-attribute="id">
	<property name="extId" xml-attribute="id"/>
	<group id="item-texts">
		<set-translation name="name" xml-attribute="name" language-var="lang"/>
	</group>
	...
</object>

<ref handler-id="item-texts"/>
```

A `<group>` is also the way to put several handlers where the configuration accepts only one, e.g. into the `<default>` of a `<dispatch>` or a `<switch>`.

## Running an import

An import definition is used in one of three ways:

- **The `XMLImportCommand`** (`com.top_logic.xio.command.XMLImportCommand`) is a button that uploads an XML file and imports it in a transaction. The definition is its `<import-definition>`; the target model of the command is the context of the import (`this` at the top level). `transient="true"` creates transient objects, which the `postProcessing` function (`result -> model -> ...`) has to turn into persistent ones; `logging="true"` writes every created object to the application log.

  ```xml
  <button class="com.top_logic.xio.command.XMLImportCommand" logging="true">
  	<import-definition>
  		<dispatch>
  			<tag name="items">
  				...
  			</tag>
  		</dispatch>
  	</import-definition>
  </button>
  ```

- **A TL-Script function** built by `com.top_logic.xio.importer.expr.ParseXml$Builder` and registered as a `<method>` of the `SearchBuilder`. The definition is the content of the `<method>`. The function takes the arguments `data` (a binary value with the XML), `context` (the context object, optional), `transient` (default `false`) and `logCreations` (default `true`), returns the result of the import and fails with the collected errors. It runs with the access rights of the calling script.

  ```xml
  <method name="parseLibrary" class="com.top_logic.xio.importer.expr.ParseXml$Builder">
  	<dispatch>
  		...
  	</dispatch>
  </method>
  ```

- **From Java**: `XmlImporter.newInstance(log, content)` reads a definition (`XmlImporter.readImportConfig(...)` reads it alone), `importModel(binding, source, context)` runs it. The `ModelBinding` decides where the objects are created: `ApplicationModelBinding` creates persistent objects in a knowledge base (within a transaction of the caller), `TransientModelBinding` transient ones. Its `usesSecurity` flag decides whether the TL-Script expressions of the definition run with the access rights of the current user. An import passes problems to the `I18NLog` and continues; the caller decides whether errors abort it.

  ```java
  XmlImporter importer = XmlImporter.newInstance(log, definition);
  Object result = importer.importModel(new TransientModelBinding(model, false), new StreamSource(in), container);
  ```

The result of an import is the last object the root handler produced, e.g. the object created for the root element. The `context` object is bound to `this` at the top level, so a top-level `<object>` gets it as its `scope` and a `<linking>` links into it. Only `this` is bound at the top level, `scope` is not.

The IDs of an import (see [Objects and IDs](doc:xml-import/objects)) are valid for one run of `importModel()`. References to IDs that are never defined are reported at its end.
