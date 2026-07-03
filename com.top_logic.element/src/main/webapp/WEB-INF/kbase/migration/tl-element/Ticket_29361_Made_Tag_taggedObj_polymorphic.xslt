<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<!-- made reference polymorphic -->
	<xsl:template match="metaobject[@object_name='Tag']/attributes/reference[@att_name='taggedObj'][not(@monomorphic)]">
		<xsl:copy>
			<xsl:attribute name="monomorphic">false</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- don't know whether attributes tag exists -->
	<xsl:template match="metaobject[@object_name='Tag']/reference[@att_name='taggedObj'][not(@monomorphic)]">
		<xsl:copy>
			<xsl:attribute name="monomorphic">false</xsl:attribute>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>