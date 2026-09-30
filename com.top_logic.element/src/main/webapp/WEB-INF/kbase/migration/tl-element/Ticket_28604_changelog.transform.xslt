<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<!-- Replace key-attributes -->
	<xsl:template match="/config/meta-objects/metaobject[@object_name='I18NAttributeStorage']/annotations/key-attributes">
		<key-attributes attributes="object, metaAttribute"/>
	</xsl:template>

	<!-- Add key-attributes at the end of annotations -->
	<xsl:template match="/config/meta-objects/association[@object_name='MetaElement_generalizations']/annotations">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
			<key-attributes attributes="source,dest"/>
		</xsl:copy>
	</xsl:template>

	<!-- Add key-attributes at the end of annotations -->
	<xsl:template match="/config/meta-objects/metaobject[@object_name='hasHistoricValue']/annotations">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
			<key-attributes attributes="source,dest,metaAttribute"/>
		</xsl:copy>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>