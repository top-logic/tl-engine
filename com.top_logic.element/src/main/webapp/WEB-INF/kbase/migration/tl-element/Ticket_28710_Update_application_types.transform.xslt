<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config/meta-objects/association[@object_name='hasRole']">
		<metaobject
			object_name="hasRole"
			object_type="MOKnowledgeObject"
			super_class="Object"
		>
			<annotations>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.KnowledgeItemFactoryAnnotation">
					<implementation-factory class="com.top_logic.knowledge.service.db2.KnowledgeObjectFactory"/>
				</annotation>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.FullLoadAnnotation"
					full-load="true"
				/>
				<key-attributes attributes="dest,tType,source"/>
				<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation">
					<binding application-type="com.top_logic.element.meta.kbbased.AttributedWrapper"/>
				</annotation>
			</annotations>
			<reference
				att_name="source"
				deletion-policy="delete-referer"
				mandatory="true"
				monomorphic="false"
				target-type="Object"
			/>
			<reference
				att_name="dest"
				deletion-policy="delete-referer"
				mandatory="true"
				monomorphic="false"
				target-type="BoundedRole"
			/>
			<reference
				att_name="owner"
				deletion-policy="delete-referer"
				mandatory="true"
				monomorphic="true"
				target-type="Group"
			/>
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
				att_name="tType"
				att_type="MetaElement"
				mandatory="true"
			>
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
						type="tl.accounts:RoleAssignment"
					/>
				</storage>
			</mo_attribute>
		</metaobject>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='StoredQuery']/mo_attribute[@att_name='sortOrder']">
		<xsl:copy-of select="."/>
		<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
			att_name="tType"
			att_type="MetaElement"
			mandatory="true"
		>
			<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
				<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
					type="tl.search:StoredQuery"
				/>
			</storage>
		</mo_attribute>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>