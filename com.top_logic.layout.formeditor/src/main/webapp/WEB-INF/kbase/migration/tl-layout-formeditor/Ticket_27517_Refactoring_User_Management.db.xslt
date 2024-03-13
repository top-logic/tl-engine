<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	version="1.0"
>
	<xsl:template match="/config/meta-objects/association[@object_name='definesGroup']/source">
		<xsl:copy>
			<xsl:attribute name="deletion-policy">delete-referer</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="/config/meta-objects/association[@object_name='definesGroup']/destination">
		<xsl:copy>
			<xsl:attribute name="is-container">true</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/config/meta-objects/association[@object_name='hasPersonalConfiguration']/source">
		<xsl:copy>
			<xsl:attribute name="deletion-policy">delete-referer</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="/config/meta-objects/association[@object_name='hasPersonalConfiguration']/destination">
		<xsl:copy>
			<xsl:attribute name="is-container">true</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='Person']//annotation[@config:interface='com.top_logic.knowledge.service.xml.annotation.ImplementationBindingAnnotation']/binding">
		<binding application-type="com.top_logic.contact.business.Account"/>
	</xsl:template>

	<xsl:template match="/config/meta-objects/association[@object_name='hasOwner']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:if test="not(source)">
				<source
					is-container="true"
					override="true"
					target-type="Object"
				/>
			</xsl:if>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/config/schemas/schema[@name='dataManager']"/>
	<xsl:template match="/config/data/definition[@resource='webinf://kbase/KBDataRoot.xml']"/>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>