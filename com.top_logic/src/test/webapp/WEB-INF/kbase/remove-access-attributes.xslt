<?xml version="1.0" encoding="UTF-8"?>

<xsl:transform version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="knowledgeobject/ko_attribute[@att_name='access_read']">
	</xsl:template>

	<xsl:template match="knowledgeobject/ko_attribute[@att_name='access_write']">
	</xsl:template>

	<xsl:template match="knowledgeobject/ko_attribute[@att_name='access_delete']">
	</xsl:template>

	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>

</xsl:transform> 
