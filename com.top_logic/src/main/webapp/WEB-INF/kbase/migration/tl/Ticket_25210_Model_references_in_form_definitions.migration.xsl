<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.top-logic.com/ns/config/6.0" xmlns:config="http://www.top-logic.com/ns/config/6.0">

	<xsl:template match="//entry[@class='com.top_logic.layout.formeditor.parts.ForeignAttributeTemplateProvider']">
		<xsl:element name="attribute-reference">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="//entry/@class[.='com.top_logic.layout.formeditor.parts.ForeignAttributeTemplateProvider']">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="//formTable/@typeSpec|//foreign-objects/@typeSpec|//entry[@class='com.top_logic.layout.formeditor.parts.ForeignAttributeTemplateProvider']/@typeSpec">
		<xsl:attribute name="type">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>