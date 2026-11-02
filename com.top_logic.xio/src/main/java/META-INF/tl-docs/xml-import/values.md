---
description: Read before storing values in an XML import definition (com.top_logic.xio) - <property> (PropertyImport) with xml-attribute and format, <text-content> with assign-id, <xml-content-property>, <reference> and <multi-reference> (ReferenceImport, MultiReferenceImport) with id-attribute, format and ignore-unresolved, <set-translation> (ResKeyUpdate) for I18N attributes with language, language-attribute, language-var, default-language and fill-fallback, <assign> computing a value with TL-Script, and the target-var of all of them.
order: 30
---

# Values

The value handlers store a value from the current element into an attribute of an object. They name the attribute with `name` (`property` for `<assign>`), the plain attribute name of the object's type (`name`, not `my.module:Item#name`), and store into the object in the variable `target-var`, which defaults to `this`. A value for an object that is still a [forward reference](doc:xml-import/objects#forward-references) is stored when the object is defined.

## `<property>`

`<property name="..." xml-attribute="..."/>` (`PropertyImport`) stores the value of an XML attribute of the current element. An element without the attribute leaves the property unchanged. The text is converted by `format`, a `ConfigurationValueProvider` (default: the string itself), given either as class name in the attribute or as nested element:

```xml
<property name="x" xml-attribute="x" format="com.top_logic.basic.config.format.IntegerWrapperFormat"/>

<property name="isExecutable" xml-attribute="isExecutable" format="com.top_logic.basic.config.format.PrimitiveBooleanFormat"/>

<property name="name" xml-attribute="name">
	<format class="com.top_logic.basic.config.StringValueProvider"/>
</property>
```

A value the format rejects is reported as error and not stored. `<property>` does not consume the element.

## `<text-content>`

`<text-content name="..."/>` (`TextContentImport`) stores the text content of the current element, converted by `format` like `<property>`. It consumes the element; an element that has child elements is an error. With `assign-id="true"`, the text also becomes an ID of the target object (see [IDs of an import](doc:xml-import/objects#ids-of-an-import)):

```xml
<object model-type="test.com.top_logic.xio.importer.test3:A">
	<dispatch>
		<tag name="id">
			<text-content name="name" assign-id="true"/>
		</tag>
	</dispatch>
</object>
```

## `<xml-content-property>`

`<xml-content-property name="..."/>` (`XMLPropertyImport`) stores the current element itself, serialized as XML text, e.g. a diagram that is kept as XML. With `content="true"` only the content of the element is stored, without the element. It consumes the element; the default `target-var` is `this`.

```xml
<tag name="BPMNDiagram">
	<xml-content-property name="diagram"/>
</tag>
```

## `<reference>` and `<multi-reference>`

`<reference name="..." id-attribute="..."/>` (`ReferenceImport`) stores the object whose ID is in the XML attribute `id-attribute` into a reference attribute. `<multi-reference>` (`MultiReferenceImport`) stores a list of objects whose IDs are separated by spaces. Both resolve forward references, so the referenced objects may be defined anywhere in the document; a multi-reference is stored when all its objects are defined, in the order of the IDs.

| Attribute | Meaning |
| --- | --- |
| `name` | The reference attribute to set. |
| `id-attribute` | The XML attribute holding the ID(s). An element without it leaves the reference unchanged. |
| `format` | For `<reference>`: converts the attribute value to the ID (default: the value itself). For `<multi-reference>`: splits it into IDs (default `com.top_logic.basic.config.SpaceSeparatedIds`). |
| `ignore-unresolved` | If `true`, an ID that is never defined is not reported. A `<reference>` is then left unset, a `<multi-reference>` leaves the ID out. |

```xml
<object model-type="test.com.top_logic.xio.importer.test9:Item" id-attribute="id">
	<property name="extId" xml-attribute="id"/>
	<reference name="other" id-attribute="other" ignore-unresolved="true"/>
	<multi-reference name="others" id-attribute="others" ignore-unresolved="true"/>
	<dispatch/>
</object>
```

Neither consumes the element. A reference that is built from the document structure instead of an ID attribute is a [linking](doc:xml-import/linking).

## `<set-translation>`

`<set-translation>` (`ResKeyUpdate`) adds a translation in one language to an internationalized attribute (a `ResKey` value, e.g. of type `tl.model.i18n:I18NString`). The translations the attribute has already are kept, a translation of the same language is replaced. So importing the same object twice in two languages, or reading several language variants of one element, builds up a multilingual value.

The value is the XML attribute `xml-attribute`; without `xml-attribute`, the text content of the current element, which consumes the element. An empty or blank value is not stored.

The language is given by exactly one of:

| Attribute | Language |
| --- | --- |
| `language` | A fixed language tag, e.g. `en`. |
| `language-attribute` | The XML attribute of the current element holding the language tag, e.g. `lang`. |
| `language-var` | The variable holding a language tag or a `Locale`, typically bound by [`<with-var>`](doc:xml-import/variables#with-var) to an attribute of an outer element. |

Only the language part of a tag is used: a translation for `de-DE` is stored for `de`.

`default-language` is the language used if the `language-attribute` or `language-var` gives none (attribute missing or empty, variable unbound or empty). Without it, a value without language is reported as error `No language given for the translation` and not stored. It cannot be combined with a fixed `language`.

`fill-fallback="true"` also stores the value for the fallback language of the application, if the attribute has no translation in that language yet - so the first imported language also fills the fallback.

```xml
<tag name="root">
	<with-var var="language" xml-attribute="DefaultLanguage">
		<object model-type="test.com.top_logic.xio.importer.test10:Root">
			<dispatch>
				<tag name="a">
					<with-var var="language" xml-attribute="DefaultLanguage">
						<object model-type="test.com.top_logic.xio.importer.test10:A">
							<property name="name" xml-attribute="name"/>
							<set-translation name="label" xml-attribute="label" language-var="language" default-language="de-DE"/>
							<set-translation name="fixed" xml-attribute="fixed" language="en" fill-fallback="true"/>
							<set-translation name="other" xml-attribute="otherEn" language="en"/>
							<set-translation name="other" xml-attribute="other" language-var="language" fill-fallback="true"/>
							<linking>
								<list-linking name="test.com.top_logic.xio.importer.test10:Root#as"/>
							</linking>
							<dispatch>
								<tag name="text">
									<set-translation name="text" language-attribute="lang" default-language="fr"/>
								</tag>
							</dispatch>
						</object>
					</with-var>
				</tag>
			</dispatch>
		</object>
	</with-var>
</tag>
```

Here an `<a>` takes its language from its own `DefaultLanguage` attribute, or else from the one of `<root>`; `<text lang="...">` elements each add one translation of `text`.

Translations that are given as separate elements per language are selected with a [`<switch>`](doc:xml-import/structure#switch-by-attribute-value):

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

## `<assign>`

`<assign property="..." value="..."/>` (`AssignHandler`) stores the result of a TL-Script function. `value` is a function of one argument, the value of the variable `argument-var` (default `scope`, the parent object); `target-var` (default `this`) is the object to store into. The result of `<assign>` is the computed value. It does not read or consume the element, so an `<assign>` that depends on the input is placed into a [`<switch>`](doc:xml-import/structure#switch-by-attribute-value) case:

```xml
<switch xml-attribute="flag">
	<case value="1, true">
		<assign property="state" value="x -> 'on'"/>
	</case>
	<default>
		<assign property="state" value="x -> 'unknown'"/>
	</default>
</switch>
```

For more on the expressions of a definition and the variables they can access, see [Variables and scripts](doc:xml-import/variables).
