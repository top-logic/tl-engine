<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="metaobject[@object_name='MetaElement']/mo_attribute[@att_name='defaultProvider']">
		<!-- Remove, might happen if the migration is applied multiple times (first boot attempt failed). -->
	</xsl:template>
	
	<xsl:template match="metaobject[@object_name='MetaElement']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
			
			<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute" att_name="defaultProvider" att_type="Void"  mandatory="false">
				<storage class="com.top_logic.dob.attr.storage.CachedComputedAttributeStorage">
					<value-factory class="com.top_logic.element.meta.kbbased.TLDefaultProviderFactory" />
				</storage>
			</mo_attribute>
		</xsl:copy>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>