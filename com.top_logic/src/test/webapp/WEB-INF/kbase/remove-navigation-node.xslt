<?xml version="1.0" encoding="UTF-8"?>

<xsl:transform version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template
	  match="knowledgeobject[@object_type='NavigationNode']">
	</xsl:template>

	<xsl:template
	  match="knowledgeassociation[boolean(ka_src/@object_type='NavigationNode') or boolean(ka_dest/@object_type='NavigationNode')]">
	</xsl:template>

	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>

</xsl:transform> 
