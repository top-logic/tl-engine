<?xml version="1.0" encoding="UTF-8"?>

<xsl:transform version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="knowledgeobject/ko_attribute[@att_name='creatorid']">
	</xsl:template>

	<xsl:template match="knowledgeobject/ko_attribute[@att_name='modifier']">
	</xsl:template>

	<xsl:template match="knowledgeobject/ko_attribute[@att_name='isRemoved']">
	</xsl:template>

	<xsl:template match="knowledgeobject/ko_attribute[@att_name='created']">
	</xsl:template>

	<xsl:template match="knowledgeobject/ko_attribute[@att_name='modified']">
	</xsl:template>

	<xsl:template match="knowledgeobject/ko_attribute[@att_name='removed']">
	</xsl:template>

	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>

</xsl:transform> 
