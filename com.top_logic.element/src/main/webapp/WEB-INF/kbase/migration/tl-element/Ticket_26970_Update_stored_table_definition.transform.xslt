<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config/meta-objects/metaobject[@object_name='Item']/annotations">
		<annotations>
			<annotation config:interface="com.top_logic.dob.schema.config.annotation.IndexColumnsStrategyAnnotation"
				strategy="com.top_logic.knowledge.service.db2.DefaultIndexColumnsStrategy"
			/>
			<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation"
				builder="com.top_logic.knowledge.search.MonomorphicQueryBuilder"
			/>
		</annotations>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='TLObject']/annotations">
		<annotations>
			<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
				<binding class="com.top_logic.knowledge.wrap.binding.DynamicBinding"
					default-application-type="com.top_logic.element.meta.kbbased.AttributedWrapper"
					type-ref="tType"
				/>
			</annotation>
			<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation">
				<builder class="com.top_logic.knowledge.search.TypeReferenceQueryBuilder"
					type-reference-column="tType"
				/>
			</annotation>
			<key-attributes attributes="tType"/>
		</annotations>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='MetaAttribute']/reference[@att_name='type']">
		<reference
			att_name="type"
			branch-global="false"
			by-value="true"
			deletion-policy="delete-referer"
			history-type="current"
			initial="false"
			is-container="false"
			mandatory="false"
			monomorphic="false"
			target-type="TLType"
		/>
	</xsl:template>

	<xsl:variable name="metaattribute_type"
		select="/config/meta-objects/metaobject[@object_name='MetaAttribute']/mo_attribute[@att_name='tType']"
	/>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='MetaAttribute']/mo_attribute[@att_name='defaultProvider']">
		<xsl:copy-of select="."/>
		<xsl:if test="count($metaattribute_type) = 0">
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.PolymorphicTypeCacheFactory"
						key-attribute="impl"
					>
						<value-type-mapping>
							<entry
								type="tl.model:TLProperty"
								value="property"
							/>
							<entry
								type="tl.model:TLReference"
								value="reference"
							/>
							<entry
								type="tl.model:TLAssociationEnd"
								value="association-end"
							/>
						</value-type-mapping>
					</value-factory>
				</storage>
			</mo_attribute>
		</xsl:if>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='MetaAttribute']/annotations">
		<annotations>
			<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
				<binding class="com.top_logic.knowledge.wrap.binding.PolymorphicBinding"
					key-attribute="impl"
				>
					<bindings>
						<binding key="property"
							application-type="com.top_logic.element.meta.kbbased.PersistentClassProperty"
						/>
						<binding key="association-property"
							application-type="com.top_logic.element.meta.kbbased.PersistentAssociationProperty"
						/>
						<binding key="association-end"
							application-type="com.top_logic.element.meta.kbbased.PersistentEnd"
						/>
						<binding key="reference"
							application-type="com.top_logic.element.meta.kbbased.PersistentReference"
						/>
					</bindings>
				</binding>
			</annotation>
			<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation">
				<builder class="com.top_logic.knowledge.search.QNameQueryBuilder"
					type-reference-column="impl"
				>
					<type-value-mapping>
						<entry key="tl.model:TLProperty"
							value="property"
						/>
						<entry key="tl.model:TLReference"
							value="reference"
						/>
						<entry key="tl.model:TLAssociationEnd"
							value="association-end"
						/>
					</type-value-mapping>
				</builder>
			</annotation>
		</annotations>
	</xsl:template>

	<xsl:variable name="module_type"
		select="/config/meta-objects/metaobject[@object_name='TLModule']/mo_attribute[@att_name='tType']"
	/>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='TLModule']/reference[@att_name='model']">
		<xsl:copy-of select="."/>
		<xsl:if test="count($module_type) = 0">
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.model:TLModule"
					/>
				</storage>
			</mo_attribute>
		</xsl:if>
	</xsl:template>

	<xsl:variable name="datatype_type"
		select="/config/meta-objects/metaobject[@object_name='TLDatatype']/mo_attribute[@att_name='tType']"
	/>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='TLDatatype']/mo_attribute[@att_name='storageMapping']">
		<xsl:copy-of select="."/>
		<xsl:if test="count($datatype_type) = 0">
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.model:TLPrimitive"
					/>
				</storage>
			</mo_attribute>
		</xsl:if>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='TLModel']">
		<metaobject
			object_name="TLModel"
			super_class="TLModelPart"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.model.PersistentTLModel"/>
				</annotation>
			</annotations>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.model:TLModel"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='TLTypeBase']">
		<!-- Remove -->
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='TLType']">
		<metaobject
			abstract="true"
			object_name="TLType"
			object_type="MOKnowledgeObject"
			super_class="TLModelPart"
		>
			<mo_attribute
				att_name="name"
				att_type="String"
				binary="true"
				mandatory="true"
			/>
			<reference
				att_name="module"
				branch-global="false"
				deletion-policy="delete-referer"
				history-type="current"
				is-container="false"
				mandatory="true"
				monomorphic="true"
				target-type="TLModule"
			/>
			<reference
				att_name="scope"
				branch-global="false"
				by-value="false"
				deletion-policy="delete-referer"
				history-type="current"
				initial="false"
				is-container="false"
				mandatory="true"
				monomorphic="false"
				target-type="TLScope"
			/>
			<index>
				<mo_index name="nameIdx"
					unique="true"
				>
					<index-parts>
						<index_part name="scope"
							part="name"
						/>
						<index_part name="name"/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='FastListElt']">
		<metaobject
			db_name="FAST_LIST_ELT"
			object_name="FastListElt"
			object_type="MOKnowledgeObject"
			super_class="TLModelPart"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.PersistentClassifier"/>
				</annotation>
			</annotations>
			<reference
				att_name="owner"
				branch-global="false"
				by-value="true"
				deletion-policy="delete-referer"
				history-type="current"
				is-container="false"
				mandatory="true"
				monomorphic="true"
				target-type="FastList"
			/>
			<mo_attribute
				att_name="name"
				att_type="String"
				db_size="72"
				mandatory="true"
			/>
			<mo_attribute
				att_name="order"
				att_type="Integer"
				db_name="SORT_ORDER"
				mandatory="true"
			/>
			<mo_attribute
				att_name="descr"
				att_type="String"
				db_size="128"
				mandatory="false"
			/>
			<mo_attribute
				att_name="flags"
				att_type="Integer"
				hidden="true"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.model:TLClassifier"
					/>
				</storage>
			</mo_attribute>
			<index>
				<mo_index name="FastListEltOwnerNameIdx"
					unique="true"
				>
					<index-parts>
						<index_part name="owner"
							part="name"
						/>
						<index_part name="name"/>
					</index-parts>
				</mo_index>
				<mo_index name="FastListEltOwnerOrderIdx">
					<index-parts>
						<index_part name="owner"
							part="name"
						/>
						<index_part name="order"/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Person']">
		<metaobject
			db_name="PERSON"
			object_name="Person"
			object_type="MOKnowledgeObject"
			super_class="KnowledgeObject"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedPerson"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<mo_attribute
				att_name="name"
				att_type="String"
				mandatory="true"
			/>
			<mo_attribute
				att_name="locale"
				att_type="String"
				mandatory="false"
			/>
			<mo_attribute
				att_name="pwdhistory"
				att_type="String"
				db_size="1333"
				hidden="true"
				mandatory="false"
			/>
			<mo_attribute
				att_name="lastPwdChange"
				att_type="Timestamp"
				hidden="true"
				mandatory="false"
			/>
			<mo_attribute
				att_name="unusedNotified"
				att_type="Boolean"
				mandatory="false"
			/>
			<mo_attribute
				att_name="wasAlive"
				att_type="Boolean"
				mandatory="false"
			/>
			<mo_attribute
				att_name="dataDeviceID"
				att_type="String"
				mandatory="false"
			/>
			<mo_attribute
				att_name="authDeviceID"
				att_type="String"
				mandatory="false"
			/>
			<mo_attribute
				att_name="restrictedUser"
				att_type="Boolean"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.accounts:Person"
					/>
				</storage>
			</mo_attribute>
			<index>
				<mo_index name="name"
					unique="true"
				>
					<index-parts>
						<index_part name="name"/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Address']">
		<metaobject
			object_name="Address"
			object_type="MOKnowledgeObject"
			super_class="KnowledgeObject"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedAddress"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<mo_attribute
				att_name="street1"
				att_type="String"
				db_size="254"
				mandatory="false"
			/>
			<mo_attribute
				att_name="street2"
				att_type="String"
				db_size="254"
				mandatory="false"
			/>
			<mo_attribute
				att_name="zip"
				att_type="String"
				db_size="32"
				mandatory="false"
			/>
			<mo_attribute
				att_name="city"
				att_type="String"
				db_size="254"
				mandatory="false"
			/>
			<mo_attribute
				att_name="country"
				att_type="String"
				db_size="254"
				mandatory="false"
			/>
			<mo_attribute
				att_name="state"
				att_type="String"
				db_size="254"
				mandatory="false"
			/>
			<mo_attribute
				att_name="telephone1"
				att_type="String"
				db_size="64"
				mandatory="false"
			/>
			<mo_attribute
				att_name="telephone2"
				att_type="String"
				db_size="64"
				mandatory="false"
			/>
			<mo_attribute
				att_name="mobile"
				att_type="String"
				db_size="64"
				mandatory="false"
			/>
			<mo_attribute
				att_name="fax1"
				att_type="String"
				db_size="64"
				mandatory="false"
			/>
			<mo_attribute
				att_name="fax2"
				att_type="String"
				db_size="64"
				mandatory="false"
			/>
			<mo_attribute
				att_name="eMail"
				att_type="String"
				db_size="254"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.accounts:Address"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Unit']">
		<metaobject
			object_name="Unit"
			object_type="MOKnowledgeObject"
			super_class="AbstractUnit"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedUnit"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<reference
				att_name="baseUnit"
				branch-global="false"
				deletion-policy="veto"
				history-type="current"
				is-container="false"
				mandatory="false"
				monomorphic="true"
				target-type="Unit"
			/>
			<mo_attribute
				att_name="factor"
				att_type="Double"
				db_prec="4"
				db_size="10"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.units:Unit"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Currency']">
		<metaobject
			object_name="Currency"
			object_type="MOKnowledgeObject"
			super_class="AbstractUnit"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedCurrency"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<reference
				att_name="baseUnit"
				branch-global="false"
				deletion-policy="veto"
				history-type="current"
				is-container="false"
				mandatory="false"
				monomorphic="true"
				target-type="Currency"
			/>
			<mo_attribute
				att_name="factor"
				att_type="Double"
				db_prec="4"
				db_size="10"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.units:Currency"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Group']">
		<metaobject
			db_name="TLGROUP"
			object_name="Group"
			object_type="MOKnowledgeObject"
			super_class="KnowledgeObject"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedGroup"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<mo_attribute
				att_name="name"
				att_type="String"
				mandatory="true"
			/>
			<mo_attribute
				att_name="isSystem"
				att_type="Boolean"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.accounts:Group"
					/>
				</storage>
			</mo_attribute>
			<index>
				<mo_index name="name"
					unique="true"
				>
					<index-parts>
						<index_part name="name"/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>
	</xsl:template>
	<xsl:template match="/config/meta-objects/metaobject[@object_name='BoundedRole']">
		<metaobject
			object_name="BoundedRole"
			object_type="MOKnowledgeObject"
			super_class="KnowledgeObject"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedRole"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<mo_attribute
				att_name="name"
				att_type="String"
				mandatory="true"
			/>
			<mo_attribute
				att_name="isSystem"
				att_type="Boolean"
				mandatory="false"
			/>
			<mo_attribute
				att_name="description"
				att_type="String"
				db_size="10000"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.accounts:Role"
					/>
				</storage>
			</mo_attribute>
			<index>
				<mo_index name="name"
					unique="true"
				>
					<index-parts>
						<index_part name="name"/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Comment']">
		<metaobject
			db_name="TLCOMMENT"
			object_name="Comment"
			object_type="MOKnowledgeObject"
			super_class="KnowledgeObject"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.comment.wrap.Comment"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<mo_attribute
				att_name="content"
				att_type="String"
				db_size="10000"
				mandatory="true"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.comments:Comment"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='FastList']">
		<metaobject
			object_name="FastList"
			object_type="MOKnowledgeObject"
			super_class="TLType"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.PersistentEnumeration"/>
				</annotation>
			</annotations>
			<reference
				att_name="default"
				branch-global="false"
				deletion-policy="clear-reference"
				history-type="current"
				is-container="true"
				mandatory="false"
				monomorphic="true"
				target-type="FastListElt"
			/>
			<mo_attribute
				att_name="descr"
				att_type="String"
				db_size="128"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.model:TLEnumeration"
					/>
				</storage>
			</mo_attribute>
			<index>
				<mo_index name="FastListNameIdx"
					unique="true"
				>
					<index-parts>
						<index_part name="name"/>
						<index_part name="module"
							part="name"
						/>
					</index-parts>
				</mo_index>
			</index>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='MetaElement']">
		<metaobject
			object_name="MetaElement"
			object_type="MOKnowledgeObject"
			super_class="TLType"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding class="com.top_logic.knowledge.wrap.binding.PolymorphicBinding"
						key-attribute="impl"
					>
						<bindings>
							<binding key="association"
								application-type="com.top_logic.element.meta.kbbased.PersistentAssociation"
							/>
							<binding key="class"
								application-type="com.top_logic.element.meta.PersistentClass"
							/>
						</bindings>
					</binding>
				</annotation>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation">
					<builder class="com.top_logic.knowledge.search.QNameQueryBuilder"
						type-reference-column="impl"
					>
						<type-value-mapping>
							<entry key="tl.model:TLAssociation"
								value="association"
							/>
							<entry key="tl.model:TLClass"
								value="class"
							/>
						</type-value-mapping>
					</builder>
				</annotation>
			</annotations>
			<mo_attribute
				att_name="impl"
				att_type="String"
				binary="true"
				initial="true"
				mandatory="true"
			/>
			<mo_attribute
				att_name="abstract"
				att_type="Boolean"
				mandatory="false"
			/>
			<mo_attribute
				att_name="final"
				att_type="Boolean"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.PolymorphicTypeCacheFactory"
						key-attribute="impl"
					>
						<value-type-mapping>
							<entry
								type="tl.model:TLAssociation"
								value="association"
							/>
							<entry
								type="tl.model:TLClass"
								value="class"
							/>
						</value-type-mapping>
					</value-factory>
				</storage>
			</mo_attribute>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="defaultProvider"
				att_type="Void"
				mandatory="false"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLDefaultProviderFactory"/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='WebFolder']">
		<metaobject
			db_name="WEB_FOLDER"
			object_name="WebFolder"
			object_type="MOKnowledgeObject"
			super_class="DublinCore"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedWebFolder"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<mo_attribute
				att_name="name"
				att_type="String"
				mandatory="true"
			/>
			<mo_attribute
				att_name="description"
				att_type="String"
				mandatory="false"
			/>
			<mo_attribute
				att_name="folderType"
				att_type="String"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.folder:WebFolder"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='DocumentVersion']">
		<metaobject
			db_name="DOCUMENT_VERSION"
			object_name="DocumentVersion"
			object_type="MOKnowledgeObject"
			super_class="DublinCore"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedDocumentVersion"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<mo_attribute
				att_name="revision"
				att_type="Integer"
				mandatory="true"
			/>
			<reference
				att_name="doc"
				history-type="historic"
				mandatory="true"
				target-type="Document"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.folder:DocumentVersion"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Document']">
		<metaobject
			db_name="DOCUMENT"
			object_name="Document"
			object_type="MOKnowledgeObject"
			super_class="DublinCore"
			versioned="true"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedDocument"/>
				</annotation>
				<key-attributes attributes="tType"/>
			</annotations>
			<mo_attribute
				att_name="name"
				att_type="String"
				mandatory="true"
			/>
			<mo_attribute
				att_name="size"
				att_type="Long"
				mandatory="true"
			/>
			<mo_attribute
				att_name="updateRevisionNumber"
				att_type="Long"
				mandatory="true"
			/>
			<mo_attribute
				att_name="versionNumber"
				att_type="Integer"
				mandatory="false"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.folder:Document"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>