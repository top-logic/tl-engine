<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config//metaobject[@object_name='MetaAttribute']//mo_attribute[@att_name='navigate']">
		<xsl:copy-of select="."/>

		<mo_attribute
			att_name="historyType"
			att_type="String"
			mandatory="false"
		>
			<storage class="com.top_logic.dob.attr.storage.EnumAttributeStorage"
				enum="com.top_logic.dob.meta.MOReference$HistoryType"
			/>
		</mo_attribute>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>