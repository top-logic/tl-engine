---
description: Read before writing an XML import definition (com.top_logic.xio) that imports into data that already exists or imports the same document again - <resolve-existing> (ResolveExisting) with objects, key, argument-var and partition, <unless-known> (UnlessKnown) with <update> and its var, condition and arguments, sharing the attribute handlers of creation and update with <group> and <ref handler-id>, and <update-known> (UpdateKnown) with <tag name id-attribute var> updating known descendants at any depth - with a worked example adding a second language to imported objects and their nested descendants.
order: 60
---

# Importing into existing data

By default, an import creates every object it reads. Importing the same document twice creates everything twice. The handlers of this article match the IDs of the document against objects that exist before the import, and decide per element whether to create an object, update a known one, or skip it.

## `<resolve-existing>`

`<resolve-existing>` (`ResolveExisting`) makes objects that exist before the import known by an ID within its content. An ID that is not known to the import is looked up among the existing objects by their key; if one is found, it is the object of the ID from then on. Such an ID resolves `<object-ref>`, `<reference>` and `<multi-reference>`, makes an `<object join-duplicates="true">` update the existing object instead of creating one, and makes an element known to `<unless-known>` and `<update-known>`.

| Attribute | Meaning |
| --- | --- |
| `objects` | TL-Script computing the existing objects. It receives the value of `argument-var`, e.g. `` c -> $c.get(`my.module:Container#elements`) ``. |
| `key` | TL-Script function computing the key of an existing object, against which the IDs are matched, e.g. `` x -> $x.get(`my.module:Item#extId`) ``. A key that is not a string is converted to one; an object with an empty key is never found. If two objects have the same key, the first is used and a warning `Ambiguous key` is reported. |
| `argument-var` | The variable passed to `objects`, default `this` (at the top level the context object of the import). |
| `partition` | Optional function computing a partition key from an ID that is not known, see [Partitions](#partitions). |

The existing objects are computed and indexed lazily, once per import: when an ID within the content is not found for the first time. A `<resolve-existing>` that is never asked costs nothing. If they are nested, the innermost one is asked first.

```xml
<tag name="item">
	<resolve-existing
		key="x -> $x.get(`test.com.top_logic.xio.importer.test8:Item#extId`)"
		objects="c -> $c.get(`test.com.top_logic.xio.importer.test8:Container#elements`)"
	>
		<object model-type="test.com.top_logic.xio.importer.test8:Item" id-attribute="id" join-duplicates="true">
			<property name="extId" xml-attribute="id"/>
			<property name="name" xml-attribute="name"/>
			<reference name="other" id-attribute="other"/>
			<linking>
				<list-linking name="test.com.top_logic.xio.importer.test8:Container#elements"/>
			</linking>
			<dispatch/>
		</object>
	</resolve-existing>
</tag>
```

An `<item id="...">` whose ID is the `extId` of an element of the container updates that element (and is not linked a second time); every other `<item>` creates a new one. The imported ID is stored in `extId`, so the next import finds the object again. Here `this` is the container, so the expression gets it as `c`.

### Partitions

Without `partition`, all existing objects form one index. For a large data set of which an import touches a small part, `partition` splits the lookup: it computes a partition key from an unknown ID, and `objects` receives the partition key as second argument and computes only the objects of that partition. Each partition is computed once per import, when an ID of it is not found for the first time; partitions the import does not touch are never computed. An ID with an empty partition key is not found.

```xml
<resolve-existing
	key="x -> $x.get(`test.com.top_logic.xio.importer.test15:Item#extId`)"
	objects="c -> p -> $c.get(`test.com.top_logic.xio.importer.test15:Container#elements`).filter(x -> $x.get(`test.com.top_logic.xio.importer.test15:Item#extId`).split('_', limit: 2).firstElement() == $p)"
	partition="id -> $id.split('_', limit: 2).firstElement()"
>
	...
</resolve-existing>
```

for IDs of the form `<partition>_<local-id>`.

## `<unless-known>`

`<unless-known id-attribute="...">` (`UnlessKnown`) applies its content handlers only to an element whose ID is not known yet. An ID is known if an object with this ID was imported before in the same run (further up in the document), or if an enclosing `<resolve-existing>` finds it. An element without the attribute, or with an empty one, is imported.

For an element with a known ID:

- without `<update>`, the element is skipped with all its content;
- with `<update>`, the handlers of the `<update>` are applied to the element, with the known object bound to `this` and to the `var` of the `<update>`, if its `condition` holds; otherwise the element is skipped.

The `<update>` creates and links nothing, unless its handlers do. Content of the element that its handlers do not consume is skipped.

| Attribute of `<update>` | Meaning |
| --- | --- |
| `var` | An additional variable for the known object. |
| `condition` | Function deciding whether the known object is updated. It receives the known object, followed by the values of the variables in `arguments`. Without it, a known object is always updated. |
| `arguments` | Comma-separated names of variables passed to `condition` after the known object. |

```xml
<tag name="data">
	<with-var var="lang" xml-attribute="lang">
		<resolve-existing
			key="x -> $x.get(`test.com.top_logic.xio.importer.test14:Item#extId`)"
			objects="c -> $c.get(`test.com.top_logic.xio.importer.test14:Container#elements`)"
		>
			<dispatch>
				<tag name="item">
					<unless-known id-attribute="id">
						<object model-type="test.com.top_logic.xio.importer.test14:Item" id-attribute="id">
							<property name="extId" xml-attribute="id"/>
							<set-translation name="name" xml-attribute="name" language-var="lang"/>
							<linking>
								<list-linking name="test.com.top_logic.xio.importer.test14:Container#elements"/>
							</linking>
							<dispatch/>
						</object>
						<update var="item"
							arguments="lang"
							condition="item -> lang -> !$item.get(`test.com.top_logic.xio.importer.test14:Item#languages`).stringContains($lang)"
						>
							<set-translation name="name" target-var="item" xml-attribute="name" language-var="lang"/>
						</update>
					</unless-known>
				</tag>
			</dispatch>
		</resolve-existing>
	</with-var>
</tag>
```

A new item is created with its name in the language of the document; an existing one only gets the translation of this language, and only if it does not have this language yet.

Compared with `<object join-duplicates="true">`, which applies the same handlers to a new and to a known object, `<unless-known>` separates the two: a known object can be left alone, or updated by different handlers - without creating its children again or linking it a second time.

### Children of a known object

The `<update>` handles only the element of the known object. Its child elements are handled by handlers inside the `<update>`. If the children are direct children, an `<object-ref>` per child tag does it:

```xml
<update>
	<set-translation name="name" xml-attribute="name" language-var="lang"/>
	<dispatch>
		<tag name="child">
			<object-ref id-attribute="id" ignore-unresolved="true">
				<set-translation name="text" xml-attribute="text" language-var="lang"/>
				<dispatch/>
			</object-ref>
		</tag>
	</dispatch>
</update>
```

For deeper structures, this restates the whole nesting of the creation in the update. `<group>` and `<update-known>` avoid that.

## Sharing handlers with `<group>`

The handlers that set the attributes of an object are often the same on creation and on update. Wrapped in a [`<group>`](doc:xml-import#handler-ids-ref-and-group) with an `id` in the creation, they are applied in the update by `<ref handler-id="..."/>`:

```xml
<unless-known id-attribute="id">
	<object model-type="test.com.top_logic.xio.importer.test16:Item" id-attribute="id">
		<property name="extId" xml-attribute="id"/>
		<group id="item-texts">
			<set-translation name="name" xml-attribute="name" language-var="lang"/>
		</group>
		...
	</object>
	<update>
		<ref handler-id="item-texts"/>
	</update>
</unless-known>
```

The `<group>` does not consume the element, so the `<ref>` to it fits in front of the handler that does.

## `<update-known>`

`<update-known>` (`UpdateKnown`) walks the whole subtree of the current element and updates the known object of each descendant element, at any depth, by its ID - without restating the nesting. It has one `<tag>` per tag name of the descendants to update:

| Attribute of `<tag>` | Meaning |
| --- | --- |
| `name` | The tag name of the descendant elements. |
| `id-attribute` | The XML attribute holding the ID of the element's known object (mandatory, it may differ per tag). |
| `var` | An additional variable for the known object. |

The content of a `<tag>` is the handlers applied to an element whose ID is known, with the known object bound to `this` and `var`. If they consume the element, its children are not walked any more; otherwise they are. An element is not updated, but its children are walked, if its tag name has no `<tag>`, if it has no ID (attribute missing or empty), or if its ID is not known. The current element itself is not updated - the handlers next to `<update-known>` do that - and nothing is created or linked, unless the handlers do so. `<update-known>` consumes the current element.

Typically `<update-known>` is the last handler of an `<update>`, next to the `<ref>` that updates the known object itself.

## Worked example: adding a language

The first import creates items with nested children in German; a second import of the same structure in English adds the English texts to the existing objects and their descendants, at any depth and below wrapper elements, without creating anything. The definition (from `testXmlImporter16-UpdateKnown.importer.xml`):

```xml
<handler>
	<tag name="data">
		<with-var var="lang" xml-attribute="lang">
			<resolve-existing
				key="x -> $x.get(`test.com.top_logic.xio.importer.test16:Item#extId`)"
				objects="c -> $c.get(`test.com.top_logic.xio.importer.test16:Container#elements`).recursion(x -> $x.get(`test.com.top_logic.xio.importer.test16:Item#children`))"
			>
				<dispatch>
					<tag name="item">
						<unless-known id-attribute="id">
							<object model-type="test.com.top_logic.xio.importer.test16:Item" id-attribute="id">
								<property name="extId" xml-attribute="id"/>
								<group id="item-texts">
									<set-translation name="name" xml-attribute="name" language-var="lang"/>
								</group>
								<linking>
									<list-linking name="test.com.top_logic.xio.importer.test16:Container#elements"/>
								</linking>
								<dispatch id="item-content">
									<tag name="static">
										<ref handler-id="item-content"/>
									</tag>
									<tag name="children">
										<ref handler-id="item-content"/>
									</tag>
									<tag name="details">
										<ref handler-id="item-content"/>
									</tag>
									<tag name="child">
										<object model-type="test.com.top_logic.xio.importer.test16:Item" id-attribute="id">
											<property name="extId" xml-attribute="id"/>
											<group id="child-texts">
												<set-translation name="name" xml-attribute="name" language-var="lang"/>
											</group>
											<linking>
												<list-linking name="test.com.top_logic.xio.importer.test16:Item#children"/>
											</linking>
											<ref handler-id="item-content"/>
										</object>
									</tag>
									<tag name="sub">
										<object model-type="test.com.top_logic.xio.importer.test16:Item" id-attribute="key">
											<property name="extId" xml-attribute="key"/>
											<group id="sub-texts">
												<set-translation name="name" xml-attribute="label" language-var="lang"/>
											</group>
											<linking>
												<list-linking name="test.com.top_logic.xio.importer.test16:Item#children"/>
											</linking>
											<dispatch/>
										</object>
									</tag>
								</dispatch>
							</object>
							<update>
								<ref handler-id="item-texts"/>
								<update-known>
									<tag name="child" id-attribute="id">
										<ref handler-id="child-texts"/>
									</tag>
									<tag name="sub" id-attribute="key">
										<ref handler-id="sub-texts"/>
									</tag>
								</update-known>
							</update>
						</unless-known>
					</tag>
				</dispatch>
			</resolve-existing>
		</with-var>
	</tag>
</handler>
```

The model: a `Container` with a composite `elements` of `Item`s; an `Item` has `extId` (string), `name` (`tl.model.i18n:I18NString`) and a composite `children` of `Item`s. The container is the context of both imports.

The first import reads

```xml
<data lang="de">
	<item id="i1" name="Eins">
		<static>
			<children>
				<child id="c1" name="Kind eins">
					<child id="c11" name="Enkel eins">
						<details>
							<sub key="s111" label="Urenkel eins"/>
						</details>
					</child>
				</child>
				<child id="c2" name="Kind zwei">
					<details>
						<sub key="s21" label="Teil zwei"/>
					</details>
				</child>
				<child id="c3" name="Kind drei"/>
			</children>
		</static>
	</item>
	<item id="i2" name="Zwei"/>
</data>
```

Nothing is known yet: `<resolve-existing>` finds no objects, so every `<item>` passes `<unless-known>` and creates the tree `i1` (children `c1`, `c2`, `c3`; `c1` has `c11`, which has `s111`; `c2` has `s21`) and `i2`, all with German names. The wrapper elements `<static>`, `<children>` and `<details>` create nothing, they only continue the recursive `item-content` dispatch. The `<sub>` elements carry their ID in `key` and their name in `label`.

The second import reads

```xml
<data lang="en">
	<item id="i1" name="One">
		<static>
			<children>
				<child id="c1" name="Child one">
					<child id="c11" name="Grandchild one">
						<details>
							<sub key="s111" label="Great-grandchild one"/>
						</details>
					</child>
				</child>
				<child id="c9" name="Unknown child">
					<details>
						<sub key="s21" label="Part two"/>
						<sub key="s99" label="Unknown part"/>
					</details>
				</child>
				<other id="c3" name="Not updated: unlisted tag"/>
				<child key="c3" name="Not updated: no ID attribute of the tag"/>
				<sub id="c3" label="Not updated: no ID attribute of the tag"/>
				<child id="" name="Not updated: empty ID"/>
			</children>
		</static>
	</item>
</data>
```

Now `<resolve-existing>` indexes all items of the container recursively by `extId`. `i1` is known, so `<unless-known>` applies the `<update>`: `<ref handler-id="item-texts"/>` adds the English name to `i1` (the language comes from the `lang` attribute of `<data>`), and `<update-known>` walks everything below `<item>`:

- `c1`, `c11` (`<child>`, ID in `id`) get their English name by the `child-texts` group, `s111` (`<sub>`, ID in `key`) by `sub-texts` - at their different depths and below `<static>`, `<children>` and `<details>`, which have no `<tag>` and are only walked.
- `c9` is not known and is not created, but its children are walked, so the known `s21` below it gets its English name, while the unknown `s99` is ignored.
- `<other id="c3">` has no `<tag>`; `<child key="c3">` and `<sub id="c3">` do not carry the ID in the `id-attribute` of their tag; `<child id="">` has an empty ID. None of them updates `c3`.
- `i2` is not in the document and stays untouched.

After the second import, `i1`, `c1`, `c11`, `s111` and `s21` have a German and an English name, `c3` and `i2` only the German one, and no object was created or linked.
