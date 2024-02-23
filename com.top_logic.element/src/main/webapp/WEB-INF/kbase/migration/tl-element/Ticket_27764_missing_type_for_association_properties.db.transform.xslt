<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config//metaobject[@object_name='MetaAttribute']//annotation[@config:interface='com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation']">
		<annotation config:interface="com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation">
			<builder class="com.top_logic.knowledge.search.QNameQueryBuilder"
				type-reference-column="impl">
				<type-value-mapping>
					<entry key="tl.model:TLProperty" value="property,association-property" />
					<entry key="tl.model:TLReference" value="reference" />
					<entry key="tl.model:TLAssociationEnd" value="association-end" />
				</type-value-mapping>
			</builder>
		</annotation>
	</xsl:template>

	<xsl:template match="/config//metaobject[@object_name='MetaAttribute']//mo_attribute[@att_name='tType']/storage">
		<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
			<value-factory class="com.top_logic.element.meta.kbbased.PolymorphicTypeCacheFactory"
				key-attribute="impl"
			>
				<value-type-mapping>
					<entry type="tl.model:TLProperty" value="property" />
					<entry type="tl.model:TLReference" value="reference" />
					<entry type="tl.model:TLAssociationEnd" value="association-end" />
					<entry type="tl.model:TLProperty" value="association-property" />
				</value-type-mapping>
			</value-factory>
		</storage>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>