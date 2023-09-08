<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<!-- Merge width and widthUnit attributes. -->
	<xsl:template match="arguments/@width[../@widthUnit]">
	    <xsl:attribute name="width">
	        <xsl:value-of select="."/>
	        <xsl:value-of select="../@widthUnit"/>
	    </xsl:attribute>
	</xsl:template>
	
	<!-- Remove widthUnit attribute. -->
	<xsl:template match="arguments/@widthUnit"/>
	
	<!-- Merge height and heightUnit attributes. -->
	<xsl:template match="arguments/@height[../@heightUnit]">
	    <xsl:attribute name="height">
	        <xsl:value-of select="."/>
	        <xsl:value-of select="../@heightUnit"/>
	    </xsl:attribute>
	</xsl:template>
	
	<!-- Remove heightUnit attribute. -->
	<xsl:template match="arguments/@heightUnit"/>

	<!-- Standard copy template. -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>