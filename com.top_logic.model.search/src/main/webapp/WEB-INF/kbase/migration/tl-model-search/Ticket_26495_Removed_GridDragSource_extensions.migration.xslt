<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="@class[.='com.top_logic.model.search.providers.TableGridDragSourceByExpression']|@class[.='com.top_logic.model.search.providers.TreeGridDragSourceByExpression']">
		<xsl:attribute name="class">
			<xsl:value-of select="'com.top_logic.model.search.providers.GridDragSourceByExpression'"/>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>