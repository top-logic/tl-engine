<?xml version="1.0" encoding="Big5"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">

	<xsl:output method="text" encoding="utf-16" />
	
	<xsl:template match="/">
		<xsl:text>速比:</xsl:text>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="text">
		<xsl:text>(</xsl:text>
		<xsl:value-of select="cn"/>
		<xsl:text>:</xsl:text>
		<xsl:value-of select="de"/>
		<xsl:text>)</xsl:text>
	</xsl:template>

</xsl:transform>
