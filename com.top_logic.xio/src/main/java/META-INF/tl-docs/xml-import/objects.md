---
description: Read before creating or referencing objects in an XML import definition (com.top_logic.xio) - <object> (ObjectCreate) with model-type, id-attribute (also text()), var, join-duplicates and fill-forward-var, <object-ref> (ObjectRef) with mandatory, multiple and ignore-unresolved, <forward>, <with-result>, <alternative-id>, how IDs are assigned and resolved, forward references to objects defined later, and duplicate or unresolved IDs.
order: 20
---

# Objects and IDs

## IDs of an import

An import has one namespace of IDs. An object gets an ID when it is created with an `id-attribute`, or later by `<alternative-id>`, `<text-content assign-id="true">` or `<with-result>`. Everything that refers to objects - `<object-ref>`, `<reference>`, `<multi-reference>` - names them by such an ID. IDs are strings from the imported document; they are trimmed, and an empty ID counts as no ID. The namespace lives for one run of the import; objects that exist before the import get IDs only through [`<resolve-existing>`](doc:xml-import/existing-data#resolve-existing).

### Forward references

An ID may be used before the object with this ID is imported. The reference is then a placeholder, and everything that is applied to it - storing it in a reference, setting a property of the referenced object, linking it - is deferred until the ID is defined, with the variables of the place where it was requested. A document may therefore reference objects in any order:

```xml
<object model-type="test.com.top_logic.xio.importer.test1:B" id-attribute="name">
	<property name="name" xml-attribute="name"/>
	<multi-reference name="next" id-attribute="other"/>
	<dispatch/>
</object>
```

An ID that is still not defined at the end of the import is reported as error `Identifier "<id>" not defined`, unless every reference to it sets `ignore-unresolved="true"` (`UnresolvedIdConfig`). An ignored ID leaves the reference unset, and everything applied to the missing object is dropped.

## `<object>`: create an object

`<object>` (`ObjectCreate`) creates an instance of `model-type` (a qualified type name `module:Type`) and applies its content handlers with `this` bound to the new object and `scope` bound to the previous `this`. Its result is the new object.

| Attribute | Meaning |
| --- | --- |
| `model-type` | The type to instantiate. |
| `id-attribute` | The XML attribute of the current element whose value becomes the ID of the new object. `text()` takes the text content of the element instead (and consumes it). Without it, the object is anonymous. |
| `var` | An additional variable bound to the new object for the content, so that deeper handlers can still address it when `this` is rebound. |
| `join-duplicates` | If `true` and the ID is already known, the known object is used instead of creating a new one, and the content handlers are applied to it again. |
| `fill-forward-var` | A variable holding an unfilled reference from [`<forward>`](#forward-and-fill-forward-var), which is filled with the new object. |

The object is not connected to anything by itself; a [`<linking>`](doc:xml-import/linking) in its content adds it to its parent. Creating a second object with an ID that is already assigned in this import is an error `Duplicate identifier "<id>"` (the known object is used), unless `join-duplicates="true"`.

```xml
<object model-type="test.com.top_logic.xio.importer.test3:A" id-attribute="name">
	<property name="name" xml-attribute="name"/>
	<linking>
		<list-linking name="test.com.top_logic.xio.importer.test3:R#all"/>
	</linking>
	<dispatch/>
</object>
```

### `join-duplicates`

With `join-duplicates`, an element that occurs several times in the document with the same ID imports one object. The content handlers are applied for each occurrence, so later occurrences overwrite the properties, and a `<list-linking>` adds the object only once:

```xml
<object model-type="test.com.top_logic.xio.importer.test7:A" id-attribute="name" join-duplicates="true">
	<property name="name" xml-attribute="name"/>
	<property name="value" xml-attribute="value"/>
	<linking>
		<list-linking name="test.com.top_logic.xio.importer.test7:R#set"/>
	</linking>
	<dispatch/>
</object>
```

Within a [`<resolve-existing>`](doc:xml-import/existing-data#resolve-existing), an ID is also known if it belongs to an object that existed before the import, so `join-duplicates` then updates the existing object instead of creating one.

## `<object-ref>`: continue an object by its ID

`<object-ref>` (`ObjectRef`) resolves the ID in its mandatory `id-attribute` and applies its content handlers to the referenced object, with `this` bound to it and `scope` to the previous `this`. It creates nothing. The ID may be defined anywhere in the document, also further below ([forward references](#forward-references)).

| Attribute | Meaning |
| --- | --- |
| `id-attribute` | The XML attribute holding the ID, or `text()` for the text content of the element. |
| `var` | An additional variable bound to the referenced object. |
| `mandatory` | If `true`, an element without the attribute (or with an empty one) is an error. Otherwise it is ignored, with its content. |
| `multiple` | If `true`, the attribute holds several IDs separated by spaces (like `IDREFS`), and the content is applied to each of them. |
| `ignore-unresolved` | If `true`, an ID that is never defined is not reported. |

Typical uses are linking an object to objects it refers to, and adding data to an object that was created elsewhere:

```xml
<object model-type="test.com.top_logic.xio.importer.test3:A" id-attribute="name">
	<object-ref id-attribute="others" multiple="true">
		<linking>
			<list-linking name="test.com.top_logic.xio.importer.test3:A#others"/>
		</linking>
	</object-ref>
	<dispatch/>
</object>
```

```xml
<tag name="update">
	<object-ref id-attribute="ref" ignore-unresolved="true">
		<property name="note" xml-attribute="note"/>
		<dispatch/>
	</object-ref>
</tag>
```

Nested `<object-ref>`s with `var` make two referenced objects available at once (from the BPMN import):

```xml
<object model-type="tl.bpe.bpml:AnnotationAssociation" id-attribute="id" var="association-link">
	<object-ref id-attribute="sourceRef" var="src" mandatory="true">
		<object-ref id-attribute="targetRef" var="dst" mandatory="true">
			<linking target-var="association-link" value-var="dst">
				<ref-linking name="tl.bpe.bpml:AnnotationAssociation#target"/>
			</linking>
			<linking target-var="association-link" value-var="src">
				<ref-linking name="tl.bpe.bpml:AnnotationAssociation#source"/>
			</linking>
		</object-ref>
	</object-ref>
</object>
```

An ID given as text content of a child element is read with `id-attribute="text()"`:

```xml
<tag name="others">
	<dispatch>
		<tag name="id">
			<object-ref id-attribute="text()">
				<linking>
					<list-linking name="test.com.top_logic.xio.importer.test3:A#others"/>
				</linking>
			</object-ref>
		</tag>
	</dispatch>
</tag>
```

## `<forward>` and `fill-forward-var`

Sometimes the ID of an object is given on an outer element, while the type of the object is only decided by an inner element. `<forward>` (`ObjectForward`) reads the ID from `id-attribute` and binds `this` and `var` to an unfilled reference for it, which an inner `<object fill-forward-var="...">` fills with the object it creates. The object gets the ID, and all references to the ID resolve to it. The ID must not be defined yet; filling the same reference twice is an error.

```xml
<tag name="def">
	<forward id-attribute="id" var="content">
		<dispatch>
			<tag name="a">
				<object model-type="test.com.top_logic.xio.importer.test2:A" fill-forward-var="content">
					<property name="name" xml-attribute="name"/>
				</object>
				<dispatch/>
			</tag>
			<tag name="b">
				<object model-type="test.com.top_logic.xio.importer.test2:B" fill-forward-var="content">
					<property name="x" xml-attribute="x" format="com.top_logic.basic.config.format.IntegerWrapperFormat"/>
				</object>
				<dispatch/>
			</tag>
		</dispatch>
	</forward>
</tag>
```

for a document like `<def id="d1"><a name="..."/></def>`.

## `<with-result>`

`<with-result>` (`WithResult`) is an `<object-ref>` that, after applying its content, assigns the ID of its `id-attribute` to the *result* of its content handlers - e.g. to the object a nested `<dispatch>` created for a child element. Like with `<forward>`, the ID of an outer element thus names an object created for an inner one. It takes the attributes of `<object-ref>`.

## `<alternative-id>`

`<alternative-id id-attribute="..."/>` (`AlternativeIdHandler`) assigns the value of the given XML attribute as an additional ID to `this`, if the attribute is present. It does not consume the element. The BPMN import creates a collaboration for the root element and assigns the ID of a `<collaboration>` child to it later:

```xml
<tag name="definitions">
	<object model-type="tl.bpe.bpml:Collaboration">
		<dispatch>
			<tag name="collaboration">
				<alternative-id id-attribute="id"/>
				<property name="extId" xml-attribute="id"/>
				<dispatch>
					...
				</dispatch>
			</tag>
		</dispatch>
	</object>
</tag>
```

The text content of an element becomes an additional ID of `this` with `<text-content name="..." assign-id="true"/>`, see [Values](doc:xml-import/values#text-content).
