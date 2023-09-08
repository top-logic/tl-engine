<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns="http://www.top-logic.com/ns/dynamic-types/6.0"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	xmlns:ext="xalan://com.top_logic.element.config.ConfigTypeResolver"
	xmlns:exsl="http://exslt.org/common"
	exclude-result-prefixes="#default ext exsl" 
>
	<xsl:variable name="propertyTag">
		<xsl:value-of select="'property'"></xsl:value-of>
	</xsl:variable>
	<xsl:variable name="referenceTag">
		<xsl:value-of select="'reference'"></xsl:value-of>
	</xsl:variable>

	<xsl:template match="metaattribute[@impl='STRING' or @impl='DATE' or @impl='BINARY' or @impl='LONG' or @impl='BOOLEAN' or @impl='FLOAT']">
		<xsl:element name="{$propertyTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeTypeProperties"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="toType">
		<xsl:param name="impl" select="@impl"/>
		<xsl:param name="mandatory" select="@mandatory"/>
		<xsl:param name="nullable" select="@nullable"/>
		
		<xsl:variable name="legacyTableTypesPrefix" select="'tl.legacy.tabletypes:'"/>
		<xsl:choose>
			<xsl:when test="@persistencyConversion">
				<xsl:variable name="typeName" select="parent::metaattributes/parent::metaelement/@type"/>
				<xsl:value-of select="concat(@persistencyConversion, '.', $typeName, '.', @name)"/>
			</xsl:when>
			<xsl:when test="$impl = 'STRING'">
				<xsl:value-of select="'tl.core:String'"/>
			</xsl:when>
			<xsl:when test="$impl = 'DATE'">
				<xsl:value-of select="'tl.core:Date'"/>
			</xsl:when>
			<xsl:when test="$impl = 'BINARY'">
				<xsl:value-of select="'tl.core:Binary'"/>
			</xsl:when>
			<xsl:when test="$impl = 'LONG'">
				<xsl:value-of select="'tl.core:Long'"/>
			</xsl:when>
			<xsl:when test="$impl = 'BOOLEAN'">
				<xsl:choose>
					<xsl:when test="$mandatory = 'true' or $nullable = 'true'">
						<xsl:value-of select="'tl.core:Tristate'"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'tl.core:Boolean'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$impl = 'FLOAT'">
				<xsl:value-of select="'tl.core:Double'"/>
			</xsl:when>
			<xsl:when test="$impl='DAP' or $impl='DAP_COLLECTION' or $impl='DAP_FALLB'">
				<xsl:value-of select="'tl.util:Any'"/>
			</xsl:when>
			<xsl:when test="$impl='COLOR'">
				<xsl:value-of select="'tl.util:Color'"/>
			</xsl:when>
			<xsl:when test="$impl='SINGLE_EXTERNAL_CONTACT'">
				<xsl:value-of select="'tl.contact.external:ExternalContact'"/>
			</xsl:when>
			<xsl:when test="$impl='EXTERNAL_CONTACT'">
				<xsl:value-of select="'tl.contact.external:ExternalContactSet'"/>
			</xsl:when>
			<xsl:when test="$impl='WEBFOLDER'">
				<xsl:value-of select="concat($legacyTableTypesPrefix, 'WebFolderTable')"/>
			</xsl:when>
			<xsl:when test="$impl='DOCUMENT'">
				<xsl:value-of select="concat($legacyTableTypesPrefix, 'DocumentTable')"/>
			</xsl:when>
			<xsl:when test="$impl='STRING_SET'">
				<xsl:value-of select="'tl.core:String'"/>
			</xsl:when>
			<xsl:when test="$impl='CALCULATED'">
				<xsl:choose>
					<xsl:when test="@algorithmClass = 'com.top_logic.project.template.project.template.ItemCreatedValue'">
						<xsl:value-of select="'enum:risk.itemstates.activity'"/>
					</xsl:when>
					<xsl:when test="@algorithmClass = 'com.top_logic.project.template.project.template.NowValue'">
						<xsl:value-of select="'tl.core:Date'"/>
					</xsl:when>
					<xsl:when test="@algorithmClass = 'com.top_logic.project.template.project.template.ProjectStartedValue'">
						<xsl:value-of select="'enum:project.state'"/>
					</xsl:when>
					<xsl:when test="boolean(@calculatedType) and not(@calculatedType = 'CALCULATED')">
						<xsl:call-template name="toType">
							<xsl:with-param name="impl" select="@calculatedType"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'tl.util:Any'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$impl='COMPLEX'">
				<xsl:choose>
					<xsl:when test="@providerClass = 'com.top_logic.element.meta.complex.CountryValueProvider'">
						<xsl:value-of select="'tl.util:Country'"/>
					</xsl:when>
					<xsl:when test="@providerClass = 'com.top_logic.element.meta.complex.LanguageValueProvider'">
						<xsl:value-of select="'tl.util:Language'"/>
					</xsl:when>
					<xsl:when test="@providerClass = 'com.top_logic.element.unit.UnitValueProvider'">
						<xsl:value-of select="ext:interfaceType('AbstractUnit')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="typeName" select="parent::metaattributes/parent::metaelement/@type"/>
						<xsl:value-of select="concat(@providerClass, '.', $typeName, '.', @name)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$impl='CLASSIFICATION' or $impl='CHECKLIST'">
				<xsl:value-of select="concat('enum:', @classification)"/>
			</xsl:when>
			<xsl:when test="$impl = 'HISTORIC_WRAPPER' or $impl = 'SINGLE_REFERENCE' or $impl = 'SINGLEWRAPPER' or $impl = 'WRAPPER' or $impl = 'SINGLE_STRUCTURE' or $impl = 'STRUCTURE' or $impl = 'LIST' or $impl = 'TYPEDSET' or $impl = 'COLLECTION'">
				<xsl:choose>
					<xsl:when test="@metaobject">
						<xsl:choose>
							<xsl:when test="@metaobject = 'Currency'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'CurrencyTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'Unit'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'UnitTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'AbstractUnit'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'AbstractUnitTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'Address'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'AddressTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'Person'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'PersonTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'Document'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'DocumentTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'WebFolder'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'WebFolderTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'DocumentVersion'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'DocumentVersionTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'MailFolder'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'MailFolderTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'Mail'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'MailTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'StoredReport'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'StoredReportTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'FlexReport'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'FlexReportTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'StoredQuery'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'StoredQueryTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'Group'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'GroupTable')"/>
							</xsl:when>
							<xsl:when test="@metaobject = 'Comment'">
								<xsl:value-of select="concat($legacyTableTypesPrefix, 'CommentTable')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="ext:interfaceType(@metaobject)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="@structure">
						<xsl:variable name="nodesSuffix">
							<xsl:choose>
								<xsl:when test="@types">
									<xsl:value-of select="concat(':', @types)"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="''"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:value-of select="concat(@structure, $nodesSuffix)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="ext:interfaceType('Object')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$impl = 'GALLERY'">
				<xsl:value-of select="'tl.imagegallery:GalleryImage'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'tl.util:Any'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="makeTypeProperties">
		<xsl:param name="impl" select="@impl"/>
		
		<xsl:attribute name="type">
			<xsl:call-template name="toType">
				<xsl:with-param name="impl" select="$impl"/>
			</xsl:call-template>
		</xsl:attribute>
		
		<xsl:choose>
			<xsl:when test="$impl = 'CALCULATED'">
				<xsl:call-template name="makeCollectionProperties">
					<xsl:with-param name="impl" select="@calculatedType"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="makeCollectionProperties"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
		
	<xsl:template name="makeCollectionProperties">
		<xsl:param name="impl" select="@impl"/>
		
		<xsl:if test="$impl='CLASSIFICATION'">
			<xsl:attribute name="multiple">
				<xsl:choose>
					<xsl:when test="@multiple">
						<xsl:value-of select="@multiple"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="ext:isMultiSelect(@classification)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
		</xsl:if>
		
		<xsl:if test="$impl='TYPEDSET' or $impl='COLLECTION' or $impl='LIST' or $impl='GALLERY' or $impl='STRUCTURE' or $impl='CHECKLIST' or $impl='STRING_SET'">
			<xsl:attribute name="multiple">
				<xsl:value-of select="'true'"/>
			</xsl:attribute>
		</xsl:if>
		
		<xsl:if test="$impl='LIST' or $impl='GALLERY'">
			<xsl:attribute name="ordered">
				<xsl:value-of select="'true'"/>
			</xsl:attribute>
		</xsl:if>
		
		<xsl:if test="$impl='LIST' or $impl='GALLERY'">
			<xsl:attribute name="bag">
				<xsl:value-of select="'true'"/>
			</xsl:attribute>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='TYPEDSET']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='COLLECTION']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='LIST']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='GALLERY']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='STRUCTURE']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='SINGLE_STRUCTURE']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='WRAPPER']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='SINGLEWRAPPER']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='SINGLE_REFERENCE']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='HISTORIC_WRAPPER']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='COMPLEX']">
		<xsl:variable name="typeSpec">
			<xsl:call-template name="toType"/>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="starts-with($typeSpec, 'tl.tables:')">
				<xsl:element name="{$referenceTag}">
					<xsl:apply-templates select="@name"/>
					<xsl:call-template name="makeTypeProperties"/>
					<xsl:call-template name="otherAttributeProperties"/>
					<xsl:call-template name="commonProperties"/>
				</xsl:element>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:element name="{$propertyTag}">
					<xsl:apply-templates select="@name"/>
					<xsl:call-template name="makeTypeProperties"/>
					<xsl:call-template name="otherAttributeProperties"/>
					<xsl:call-template name="commonProperties"/>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='STRING_SET']">
		<xsl:element name="{$propertyTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeTypeProperties"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='COLOR']">
		<xsl:element name="{$propertyTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeTypeProperties"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='DAP' or @impl='DAP_COLLECTION' or @impl='DAP_FALLB']">
		<xsl:element name="{$propertyTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeTypeProperties"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='CALCULATED']" name="otherCalculated">
		<xsl:element name="{$propertyTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeTypeProperties"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='CALCULATED' and @algorithmClass = 'com.top_logic.element.meta.algorithm.ValueLocatorAlgorithm']">
		<xsl:choose>
			<xsl:when test="starts-with(@locator, 'ReverseMetaAttribute(')">
				<xsl:call-template name="reverseCalculated">
					<xsl:with-param name="locator" select="@locator"/>		
					<xsl:with-param name="multiple" select="'true'"/>		
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@locator, 'SingleValueReverseMetaAttribute(')">
				<xsl:call-template name="reverseCalculated">
					<xsl:with-param name="locator" select="@locator"/>		
					<xsl:with-param name="multiple" select="'false'"/>		
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="otherCalculated"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='CALCULATED' and string-length(@expr) &gt; 0 and ext:locatorIsReference(@expr)]">
		<xsl:variable name="typeName">
			<xsl:value-of select="ext:locatorType(@expr)"/>
		</xsl:variable>
		
		<xsl:variable name="multiple">
			<xsl:value-of select="ext:locatorIsMultiple(@expr)"/>
		</xsl:variable>
		
		<xsl:variable name="otherEndName">
			<xsl:value-of select="ext:locatorReverseEnd(@expr)"/>
		</xsl:variable>
		
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:attribute name="type">
				<xsl:value-of select="$typeName"/>
			</xsl:attribute>
			<xsl:if test="$otherEndName">
				<xsl:attribute name="inverse-reference">
					<xsl:value-of select="$otherEndName"/>
				</xsl:attribute>
				<xsl:attribute name="kind">
					<xsl:value-of select="'backwards'"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:attribute name="multiple">
				<xsl:value-of select="$multiple"/>
			</xsl:attribute>
			
			<xsl:call-template name="otherAttributeProperties"/>
			
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="reverseCalculated">
		<xsl:param name="locator"/>
		<xsl:param name="multiple"/>
		
		<xsl:variable name="qualifiedEndQuoted">
			<xsl:value-of select="substring-before(substring-after($locator, '('), ')')"/>
		</xsl:variable>
		
		<xsl:variable name="qualifiedEnd">
			<xsl:value-of select="substring($qualifiedEndQuoted, 2, string-length($qualifiedEndQuoted) - 2)"/>
		</xsl:variable>
		
		<xsl:variable name="typeName">
			<xsl:value-of select="substring-before($qualifiedEnd, '#')"/>
		</xsl:variable>
		
		<xsl:variable name="otherEndName">
			<xsl:value-of select="substring-after($qualifiedEnd, '#')"/>
		</xsl:variable>
		
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:attribute name="type">
				<xsl:value-of select="concat('me:', $typeName)"/>
			</xsl:attribute>
			<xsl:attribute name="inverse-reference">
				<xsl:value-of select="$otherEndName"/>
			</xsl:attribute>
			<xsl:attribute name="kind">
				<xsl:value-of select="'backwards'"/>
			</xsl:attribute>
			<xsl:attribute name="multiple">
				<xsl:value-of select="$multiple"/>
			</xsl:attribute>
			
			<xsl:call-template name="otherAttributeProperties"/>
			
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='SINGLE_EXTERNAL_CONTACT']">
		<xsl:element name="{$propertyTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeTypeProperties"/>
			<xsl:call-template name="otherAttributeProperties"/>
			
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='EXTERNAL_CONTACT']">
		<xsl:element name="{$propertyTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeTypeProperties"/>
			<xsl:call-template name="otherAttributeProperties"/>
			
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="makeReferenceType">
		<xsl:call-template name="makeTypeProperties"/>
		<xsl:attribute name="kind">
			<xsl:value-of select="'forwards'"/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='WEBFOLDER']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="setDefaultProvider">
		<xsl:if test="@string-default">
			<default-value>
				<string value="{@string-default}"/>
			</default-value>
		</xsl:if>
		<xsl:if test="@boolean-default">
			<default-value>
			  	<boolean value="{@boolean-default}"/>
			</default-value>
		</xsl:if>
		<xsl:if test="@date-default">
			<default-value>
			  	<date value="{@date-default}"/>
			</default-value>
		</xsl:if>
		<xsl:if test="@long-default">
			<default-value>
			  	<long value="{@long-default}"/>
			</default-value>
		</xsl:if>
		<xsl:if test="@double-default">
			<default-value>
			  	<double value="{@double-default}"/>
			</default-value>
		</xsl:if>
		<xsl:if test="@color-default">
			<default-value>
			  	<color value="{@color-default}"/>
			</default-value>
		</xsl:if>
		<xsl:if test="@null-default">
			<default-value>
			  	<null />
			</default-value>
		</xsl:if>
		<xsl:if test="@defaultProvider">
			<default-value provider="{@defaultProvider}"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="addStorageImplementation">
		<xsl:param name="class"/>
		<xsl:if test="@referenceBuilder or @association">
			<storage-algorithm>
				<xsl:element name="implementation">
					<xsl:attribute name="class">
						<xsl:value-of select="$class"/>
					</xsl:attribute>
					<xsl:if test="@referenceBuilder">
						<xsl:attribute name="reference-builder">
							<xsl:value-of select="@referenceBuilder"/>
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="@association">
						<xsl:attribute name="table">
							<xsl:value-of select="@association"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:element>
			</storage-algorithm>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template match="metaattribute[@impl='DOCUMENT']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='CLASSIFICATION']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='CHECKLIST']">
		<xsl:element name="{$referenceTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeReferenceType"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='CHECKLIST']" mode="annotations">
		<classification-display value="checklist"/>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='CLASSIFICATION']" mode="annotations">
		<xsl:if test="ext:isUnordered(@classification)">
			<unordered-enum/>
		</xsl:if>
		<xsl:choose>
			<xsl:when test="@pop-up='true'">
				<classification-display value="pop-up"/>
					</xsl:when>
			<xsl:when test="@pop-up='false'">
				<classification-display value="drop-down"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="metaattribute" mode="annotations">
		<!-- No annotations by default. -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@impl[.='CHECKLIST']">
		<xsl:attribute name="impl">
			<xsl:value-of select="'CLASSIFICATION'"/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="metaattribute">
		<xsl:element name="{$propertyTag}">
			<xsl:apply-templates select="@name"/>
			<xsl:call-template name="makeTypeProperties"/>
			<xsl:call-template name="otherAttributeProperties"/>
			<xsl:call-template name="commonProperties"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="otherAttributeProperties">
		<xsl:apply-templates select="@*[local-name(.) != 'name']"/>
	</xsl:template>
	
	<xsl:template name="commonProperties">
		<xsl:variable name="annotationNodes">
			<xsl:apply-templates mode="annotations" select="."/>
			
			<xsl:if test="@filter">
				<constraint>
					<filter class="com.top_logic.element.meta.kbbased.NamedConstraint" 
						name="{@filter}"
					/>
				</constraint>
			</xsl:if>
			<xsl:if test="@impl='STRING_SET' or @calculatedType='STRING_SET'">
				<config-type value="STRING_SET"/>
			</xsl:if>
			<xsl:if test="@impl='STRING_SET'">
				<xsl:if test="@locator">
					<options>
						<generator class="com.top_logic.element.meta.kbbased.StringSetOptions"
							locator="{@locator}"
						/>
					</options>
				</xsl:if>
			</xsl:if>
			<xsl:if test="@generator">
				<options>
					<generator class="com.top_logic.element.meta.kbbased.NamedOptions" name="{@generator}"/>
				</options>
			</xsl:if>
			<xsl:if test="@validityCheck">
				<validity
					value="{@validityCheck}"/>
			</xsl:if>
			<xsl:if test="@display">
				<boolean-display presentation="{@display}"/>
			</xsl:if>
			<xsl:if test="@folderType">
				<folder-type value="{@folderType}"/>
			</xsl:if>
			<xsl:if test="@format">
				<format format="{@format}"/>
			</xsl:if>
			<xsl:if test="@format-ref">
				<format format-ref="{@format-ref}"/>
			</xsl:if>
			<xsl:if test="@max or @min">
				<value-range max="{@max}" min="{@min}"/>
			</xsl:if>
			<xsl:if test="@presentation">
				<reference-display value="{@presentation}"/>
			</xsl:if>
			<xsl:if test="@mainProperties">
				<main-properties properties="{@mainProperties}"/>
			</xsl:if>
			<xsl:if test="@textarea">
				<xsl:element name="multi-line">
					<xsl:attribute name="value">
						<xsl:value-of select="@textarea"/>
					</xsl:attribute>
					
					<xsl:if test="@rows">
						<xsl:attribute name="rows">
							<xsl:value-of select="@rows"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:element>
			</xsl:if>
			<xsl:if test="@cssClass">
				<xsl:element name="css-class">
					<xsl:attribute name="value">
						<xsl:value-of select="@cssClass"/>
					</xsl:attribute>
				</xsl:element>
			</xsl:if>
			<xsl:if test="@length">
				<size-constraint upper-bound="{@length}"/>
			</xsl:if>
			<xsl:if test="@templatePath">
				<template-locator value="{@templatePath}"/>
			</xsl:if>
			<xsl:if test="@allowsSearchRange">
				<search-range value="{@allowsSearchRange}"/>
			</xsl:if>
			<xsl:if test="@fullTextRelevant">
				<fulltext-relevant value="{@fullTextRelevant}"/>
			</xsl:if>
			<xsl:if test="@sortOrder">
				<sort-order value="{@sortOrder}"/>
			</xsl:if>
			<xsl:if test="@deleteProtected">
				<delete-protected/>
			</xsl:if>
			<xsl:call-template name="setDefaultProvider" />
			<xsl:if test="@createVisibility">
				<create-visibility value="{@createVisibility}"/>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="@excludeFromGUIContext">
					<visibility value="hidden"/>
				</xsl:when>
				<xsl:when test="@editable = 'false'">
					<visibility value="read-only"/>
				</xsl:when>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="@impl='CLASSIFICATION'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SetStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='CHECKLIST'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SetStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='TYPEDSET'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SetStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='COLLECTION'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SetStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='LIST'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.ListStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='GALLERY'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.GalleryStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='STRUCTURE'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SetStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='SINGLE_STRUCTURE'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SingletonLinkStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='WRAPPER'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SingletonLinkStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='SINGLEWRAPPER'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SingletonLinkStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='WEBFOLDER'">
					<xsl:call-template name="addStorageImplementation">
						<xsl:with-param name="class" select="'com.top_logic.element.meta.kbbased.storage.SingletonLinkStorage'"/>				
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="@impl='SINGLE_REFERENCE'">
					<storage-algorithm>
						<implementation class="com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage" storage-type="{@storage-type}" storage-attribute="{@storage-attribute}"/>
					</storage-algorithm>					
				</xsl:when>
				<xsl:when test="@impl='HISTORIC_WRAPPER'">
					<storage-algorithm>
						<implementation class="com.top_logic.element.meta.kbbased.storage.HistoricStorage"/>
					</storage-algorithm>					
				</xsl:when>
				<xsl:when test="@impl='DOCUMENT'">
					<storage-algorithm>
						<xsl:element name="implementation">
							<xsl:attribute name="class">
								<xsl:value-of select="'com.top_logic.element.meta.kbbased.storage.DocumentStorage'"/>
							</xsl:attribute>
							<xsl:attribute name="folder-path">
								<xsl:value-of select="@folderPath"/>
							</xsl:attribute>
							<xsl:if test="@referenceBuilder">
								<xsl:attribute name="reference-builder">
									<xsl:value-of select="@referenceBuilder"/>
								</xsl:attribute>
							</xsl:if>
						</xsl:element>
					</storage-algorithm>					
				</xsl:when>
				<xsl:when test="@impl='STRING_SET'">
					<storage-algorithm>
						<xsl:element name="implementation">
							<xsl:attribute name="class">
								<xsl:value-of select="'com.top_logic.element.meta.kbbased.storage.StringSetStorage'"/>
							</xsl:attribute>
							<xsl:if test="@result">
								<xsl:attribute name="result">
									<xsl:value-of select="@result"/>
								</xsl:attribute>
							</xsl:if>
						</xsl:element>
					</storage-algorithm>
				</xsl:when>
				<xsl:when test="@impl='CALCULATED'">
					<storage-algorithm>
						<implementation>
							<xsl:element name="algorithm">
								<xsl:if test="@algorithmClass">
									<xsl:attribute name="class">
										<xsl:value-of select="@algorithmClass"/>
									</xsl:attribute>
								</xsl:if>
								<xsl:if test="@locator">
									<xsl:attribute name="locator">
										<xsl:choose>
											<xsl:when test="starts-with(@locator, 'PathInverted(')">
												<xsl:value-of select="ext:transformPathAttributeValueLocaterInvertable(@locator)"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="@locator"/>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:attribute>
								</xsl:if>
								<xsl:if test="@expr">
									<xsl:attribute name="expr">
										<xsl:value-of select="@expr"/>
									</xsl:attribute>
								</xsl:if>
								<xsl:if test="@methodName">
									<xsl:attribute name="methodName">
										<xsl:value-of select="@methodName"/>
									</xsl:attribute>
								</xsl:if>
							</xsl:element>
						</implementation>
					</storage-algorithm>
				</xsl:when>
				<xsl:when test="@impl='DAP' or @impl='DAP_COLLECTION' or @impl='DAP_FALLB'">
					<storage-algorithm>
						<xsl:element name="implementation">
							<xsl:attribute name="class">
								<xsl:choose>
									<xsl:when test="@impl='DAP'">
										<xsl:value-of select="'com.top_logic.element.meta.kbbased.storage.ExternalStorage'"/>
									</xsl:when>
									<xsl:when test="@impl='DAP_FALLB'">
										<xsl:value-of select="'com.top_logic.element.meta.kbbased.storage.FallbackExternalStorage'"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="'com.top_logic.element.meta.kbbased.storage.ExternalCollectionStorage'"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:attribute>
							<xsl:if test="@query">
								<xsl:attribute name="query">
									<xsl:value-of select="@query"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="@parameters">
								<xsl:attribute name="parameters">
									<xsl:value-of select="@parameters"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="@locator">
								<xsl:attribute name="locator">
									<xsl:value-of select="@locator"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="@result">
								<xsl:attribute name="result">
									<xsl:value-of select="@result"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="@iterated">
								<xsl:attribute name="iterated">
									<xsl:value-of select="@iterated"/>
								</xsl:attribute>
							</xsl:if>
						</xsl:element>
					</storage-algorithm>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:if test="count(exsl:node-set($annotationNodes)/node()) &gt; 0">
			<annotations xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<xsl:copy-of select="$annotationNodes"/>
			</annotations>
		</xsl:if>
		
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="metaattribute/@pop-up">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@impl">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@referenceBuilder">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@association">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@folderPath">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@metaobject">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@structure">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@types">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@classification">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@algorithmClass">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@allowsSearchRange">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@fullTextRelevant">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@sortOrder">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@deleteProtected">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@calculatedType">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@locator">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@expr">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@result">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@parameters">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@iterated">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@methodName">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@providerClass">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@filter">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@generator">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@validityCheck">
		<!-- Drop -->
	</xsl:template>

	<xsl:template match="metaattribute/@display">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@folderType">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@format">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@format-ref">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@max">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@min">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@presentation">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@mainProperties">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@textarea">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@rows">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@cssClass">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@length">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@templatePath">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@persistencyConversion">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@nullable">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@excludeFromGUIContext">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@editable">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@createVisibility">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@storage-attribute">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@storage-type">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@defaultProvider">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@string-default">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@boolean-default">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@date-default">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@long-default">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@double-default">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="metaattribute/@null-default">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="element/metaelementdefinition">
		<generalizations>
			<xsl:call-template name="makeExtends">
				<xsl:with-param name="type" select="@meType"/>
				<xsl:with-param name="scope" select="@holderType"/>
			</xsl:call-template>
		</generalizations>
		
		<xsl:if test="metaelements/metaelement">
			<xsl:apply-templates/>			
		</xsl:if>
	</xsl:template>

	<xsl:template match="structuredElements/metaelementdefinition">
		<!-- Drop global definitions. -->
	</xsl:template>
	
	<xsl:template name="makeExtends">
		<xsl:param name="type"/>
		<xsl:param name="scope"/>
		
		<xsl:element name="generalization">
			<xsl:attribute name="type">
				<xsl:value-of select="$type"/>
			</xsl:attribute>
			<xsl:if test="$scope and not($scope='global')">
				<xsl:attribute name="scope">
					<xsl:value-of select="$scope"/>
				</xsl:attribute>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="structuredElements">
		<xsl:element name="model">
			<xsl:apply-templates select="@*"/>
			
			<modules>
				<xsl:apply-templates/>
			</modules>
		</xsl:element>
	</xsl:template>

	<xsl:template match="structure">
		<xsl:element name="module">
			<xsl:apply-templates select="@*"/>
			
			<xsl:call-template name="setModuleAnnotations" />

			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="setModuleAnnotations">
		<xsl:variable name="annotationNodes">
			<xsl:if test="@interface-package != '' or @implementation-package != ''">
				<xsl:element name="annotation">
					<xsl:attribute name="config:interface">
						<xsl:value-of select="'com.top_logic.model.config.JavaPackage'"/>
					</xsl:attribute>
					<xsl:if test="@interface-package != ''">
						<xsl:attribute name="interface-package">
							<xsl:value-of select="@interface-package"/>
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="@implementation-package != ''">
						<xsl:attribute name="implementation-package">
							<xsl:value-of select="@implementation-package"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:element>
			</xsl:if>
			<xsl:if test="@factory-class != ''">
				<annotation config:interface="com.top_logic.model.config.FactoryClass" value="{@factory-class}"/>
			</xsl:if>
			<xsl:if test="@rootType">
				<singletons>
					<singleton type="{@rootType}"/>
				</singletons>
			</xsl:if>
		</xsl:variable>
		<xsl:if test="count(exsl:node-set($annotationNodes)/node()) &gt; 0">
			<annotations xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<xsl:copy-of select="$annotationNodes"/>
			</annotations>
		</xsl:if>
	</xsl:template>
		
	<xsl:template match="structure/@interface-package">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="structure/@implementation-package">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="structure/@factory-class">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="structure/@rootType">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="structure/elements">
		<xsl:variable name="preceedingGlobals" 
			select="parent::structure/preceding-sibling::*[self::structure or self::metaelementdefinition][position() = 1][self::metaelementdefinition]"/>

		<xsl:element name="types">
			<xsl:comment>structure/elements</xsl:comment>
			<xsl:apply-templates select="$preceedingGlobals/metaelements/metaelement/metaattributes/metaattribute" mode="datatype"/>
			<xsl:apply-templates select="$preceedingGlobals/metaelements/metaelement"/>
		
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaattribute[@impl='COMPLEX']" mode="datatype">
		<xsl:choose>
			<xsl:when test="@providerClass = 'com.top_logic.element.meta.complex.CountryValueProvider'">
				<!-- Well known type, handled specially. -->
			</xsl:when>
			<xsl:when test="@providerClass = 'com.top_logic.element.meta.complex.LanguageValueProvider'">
				<!-- Well known type, handled specially. -->
			</xsl:when>
			<xsl:when test="@providerClass = 'com.top_logic.element.unit.UnitValueProvider'">
				<!-- Well known type, handled specially. -->
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="typeName" select="parent::metaattributes/parent::metaelement/@type"/>
				<datatype name="{@providerClass}.{$typeName}.{@name}" db_type="varchar" db_size="255" kind="Custom">
					<storage-mapping class="{@providerClass}"/>
					<annotations>
						<config-type value="cfg.{@providerClass}"/>
					</annotations>
				</datatype>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="metaattribute[@persistencyConversion]" mode="datatype">
		<xsl:variable name="typeName" select="parent::metaattributes/parent::metaelement/@type"/>
		<xsl:variable name="conf">
			<xsl:choose>
				<xsl:when test="@impl='CALCULATED'">
					<xsl:value-of select="@calculatedType"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@impl"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<datatype name="{@persistencyConversion}.{$typeName}.{@name}" db_type="varchar" db_size="255" kind="Custom">
			<storage-mapping class="{@persistencyConversion}"/>
			<annotations>
				<config-type value="{$conf}"/>
				<xsl:if test="$conf='LONG' or $conf='FLOAT' or $conf='DATE'">
					<search-range value="true"/>
				</xsl:if>
			</annotations>
		</datatype>
	</xsl:template>
	
	<xsl:template match="structure/elements/element">
		<xsl:element name="class">
			<!-- Make sure, the name attribute appears first in the output. -->
			<xsl:attribute name="name">
				<xsl:value-of select="@type"/>
			</xsl:attribute>
			
			<xsl:apply-templates select="@*[local-name(.) != 'type']"/>
			<xsl:apply-templates/>
			
			<xsl:call-template name="setClassAnnotations" />
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="setClassAnnotations">
		<annotations xmlns:config="http://www.top-logic.com/ns/config/6.0">
			<xsl:element name="table">
				<xsl:attribute name="name">
					<xsl:value-of select="string(koType)"/>
				</xsl:attribute>
			</xsl:element>
			
			<xsl:variable name="java-impl-class" select="string(wrapperClass)"/>
			<xsl:variable name="java-intf-class" select="string(interfaceClass)"/>
			<xsl:element name="annotation">
				<xsl:attribute name="config:interface">
					<xsl:value-of select="'com.top_logic.model.config.JavaClass'"/>
				</xsl:attribute>
				<xsl:if test="$java-impl-class != ''">
					<xsl:attribute name="class-name">
						<xsl:value-of select="$java-impl-class"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="$java-intf-class != ''">
					<xsl:attribute name="interface-name">
						<xsl:value-of select="$java-intf-class"/>
					</xsl:attribute>
				</xsl:if>
			</xsl:element>
			
			<xsl:variable name="meName" select="metaelementdefinition/@meType"/>
			<xsl:variable name="usages" select="//element[metaelementdefinition/@meType = $meName]"/>
			<xsl:if test="count($usages) &gt; 1">
				<!-- 
					Only annotate images to the concrete type, if the image specification 
					is not moved to the unique usage of the interface type, see above.
				-->
				<xsl:call-template name="copyImages"/>
			</xsl:if>
		</annotations>
	</xsl:template>
		
	<xsl:template name="setInterfaceAnnotations">
		<xsl:variable name="annotationNodes">
			<xsl:variable name="meName" select="@type"/>
			<xsl:variable name="usages" select="//element[metaelementdefinition/@meType = $meName]"/>
			<xsl:if test="count($usages) = 1">
				<xsl:for-each select="$usages">
					<xsl:call-template name="copyImages"/>
				</xsl:for-each>
			</xsl:if>
		</xsl:variable>
		<xsl:if test="count(exsl:node-set($annotationNodes)/node()) &gt; 0">
			<annotations xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<xsl:copy-of select="$annotationNodes"/>
			</annotations>
		</xsl:if>
	</xsl:template>
		
	<xsl:template name="copyImages">
		<xsl:if test="defaultIcon or largeIcon or openIcon">
			<xsl:element name="instance-presentation">
				<xsl:if test="defaultIcon">
					<xsl:attribute name="icon">
						<xsl:value-of select="string(defaultIcon)"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="largeIcon">
					<xsl:attribute name="large-icon">
						<xsl:value-of select="string(largeIcon)"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="openIcon">
					<xsl:attribute name="expanded-icon">
						<xsl:value-of select="string(openIcon)"/>
					</xsl:attribute>
				</xsl:if>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="element/defaultIcon">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="element/largeIcon">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="element/openIcon">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="element/koType">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="element/wrapperClass">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="element/interfaceClass">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="element/children">
		<xsl:element name="attributes">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="element/children/child">
		<xsl:variable name="typeRef">
			<xsl:value-of select="@name"/>
		</xsl:variable>
		<xsl:variable name="scope">
			<xsl:value-of select="//element[@type=$typeRef]/metaelementdefinition/@holderType"/>
		</xsl:variable>
		
		<xsl:element name="reference">
			<xsl:attribute name="name">
				<xsl:value-of select="concat(@name, 'Children')"/>
			</xsl:attribute>
			<xsl:attribute name="multiple">
				<xsl:value-of select="'true'"/>
			</xsl:attribute>
			<xsl:attribute name="composite">
				<xsl:value-of select="'true'"/>
			</xsl:attribute>
			<xsl:attribute name="kind">
				<xsl:value-of select="'forwards'"/>
			</xsl:attribute>
			<xsl:apply-templates select="@*"/>
			<annotations xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<xsl:if test="$scope and not($scope='global')">
					<create-scope scope-ref="{$scope}" create-type="{$typeRef}"/>
				</xsl:if>
				<visibility value="hidden"/>
			</annotations>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="element/children/child/@name">
		<xsl:attribute name="type">
			<xsl:value-of select="concat(ancestor::structure/@name, ':', .)"/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="metaelements">
		<xsl:element name="types">
			<xsl:comment>metaelements</xsl:comment>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaelement">
		<xsl:element name="interface">
			<xsl:apply-templates select="@*"/>
			
			<xsl:if test="@superType">
				<generalizations>
					<xsl:call-template name="makeExtends">
						<xsl:with-param name="type" select="@superType"/>
						<xsl:with-param name="scope" select="@superHolderType"/>
					</xsl:call-template>
				</generalizations>
			</xsl:if>
			
			<xsl:apply-templates/>
			
			<xsl:call-template name="setInterfaceAnnotations"/>
		</xsl:element>
		
		<xsl:variable name="interfaceName">
			<xsl:value-of select="@type"/>
		</xsl:variable>
	</xsl:template>

	<xsl:template match="metaelement/@type">
		<xsl:attribute name="name">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="metaelement/@superType">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="metaelement/@superHolderType">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="metaelement/metaattributes|workflow/metaattributes">
		<xsl:element name="attributes">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<!-- standard copy template -->
	<xsl:template match="/">
		<xsl:text>&#10;</xsl:text>
		<xsl:apply-templates select="child::node()"/>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>