<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config/meta-objects/metaobject[@object_name='FlexReport']/annotations">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates select="node()"/>
			<key-attributes attributes="tType"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/config/meta-objects/metaobject[@object_name='FlexReport']/mo_attribute[@att_name='report']">
		<xsl:copy-of select="."/>
		<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
			att_name="tType"
			att_type="MetaElement"
			mandatory="true"
		>
			<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
				<value-factory class="com.top_logic.element.meta.kbbased.TLTypeCacheFactory"
					type="tl.reporting:FlexReport"
				/>
			</storage>
		</mo_attribute>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>