<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="@kind">
		<xsl:attribute name="kind">
			<xsl:choose>
				<xsl:when test="starts-with(string(ancestor::*/@class), 'com.top_logic.model.search.ui.model.operator.') and contains(string(.), ':')">
					<xsl:value-of select="substring-after(string(.), ':')"/>
				</xsl:when>
				
				<xsl:otherwise>
					<xsl:value-of select="."/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	
	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>