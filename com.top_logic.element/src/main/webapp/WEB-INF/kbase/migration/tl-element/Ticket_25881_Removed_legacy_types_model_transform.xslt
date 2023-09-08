<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="/config/module[@name='tl.model']">
		<module name="tl.model">
			<annotations>
				<package-binding interface-package="com.top_logic.model.model"/>
			</annotations>
			<types>
				<datatype name="TLAnnotation"
					db_type="clob"
					kind="Custom"
				>
					<storage-mapping class="com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping"
						application-type="com.top_logic.model.internal.PersistentModelPart$AnnotationConfigs"
					/>
				</datatype>

				<interface name="TLObject">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.TLObject"/>
					</annotations>
					<attributes>
						<reference name="tType"
							kind="forwards"
							type="TLType"
						>
							<annotations>
								<storage-algorithm>
									<implementation class="com.top_logic.element.meta.kbbased.storage.ReadOnlyForeignKeyStorage"
										storage-attribute="tType"
										storage-type="TLObject"
									/>
								</storage-algorithm>
								<visibility value="hidden"/>
							</annotations>
						</reference>
					</attributes>
				</interface>

				<interface name="TLModelPart">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.TLModelPart"/>
					</annotations>
					<generalizations>
						<generalization type="TLObject"/>
					</generalizations>
					<attributes>
						<property name="annotations"
							bag="false"
							multiple="true"
							type="TLAnnotation"
						/>
						<reference name="model"
							type="TLModel"
						/>
					</attributes>
				</interface>

				<interface name="TLNamedPart">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.TLNamedPart"/>
						<main-properties properties="name"/>
					</annotations>
					<generalizations>
						<generalization type="TLModelPart"/>
					</generalizations>
					<attributes>
						<property name="name"
							type="tl.core:String"
						/>
					</attributes>
				</interface>

				<class name="TLModel">
					<annotations>
						<table name="TLModel"/>
						<implementation-binding interface-name="com.top_logic.model.TLModel"/>
					</annotations>
					<generalizations>
						<generalization type="TLModelPart"/>
					</generalizations>
					<attributes>
						<reference name="model"
							override="true"
							type="TLModel"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<!-- The model of a TLModel is the element itself. -->
											<!-- Empty chain results in return of the object itself. -->
											<chain/>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</reference>
						<reference name="modules"
							inverse-reference="model"
							kind="backwards"
							multiple="true"
							type="TLModule"
						/>
					</attributes>
				</class>

				<interface name="TLScope">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.TLScope"/>
					</annotations>
					<generalizations>
						<generalization type="TLObject"/>
					</generalizations>
					<attributes>
						<reference name="types"
							inverse-reference="scope"
							kind="backwards"
							multiple="true"
							type="TLType"
						/>
					</attributes>
				</interface>

				<class name="TLModule">
					<annotations>
						<table name="TLModule"/>
						<implementation-binding interface-name="com.top_logic.model.TLModule"/>
						<instance-presentation icon="theme:mime.TLModule"/>
						<main-properties properties="name,types"/>
					</annotations>
					<generalizations>
						<generalization type="TLScope"/>
						<generalization type="TLNamedPart"/>
					</generalizations>
					<attributes>
						<reference name="singletons"
							inverse-reference="module"
							kind="backwards"
							multiple="true"
							type="TLModuleSingletons"
						/>
						<reference name="model"
							override="true"
							type="TLModel"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="model"
										storage-type="TLModule"
									/>
								</storage-algorithm>
							</annotations>
						</reference>
					</attributes>
				</class>

				<!-- Types -->

				<interface name="TLType">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.TLType"/>
						<main-properties properties="name,module"/>
					</annotations>
					<generalizations>
						<generalization type="TLNamedPart"/>
					</generalizations>
					<attributes>
						<reference name="module"
							type="TLModule"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="module"
										storage-type="TLType"
									/>
								</storage-algorithm>
							</annotations>
						</reference>

						<reference name="scope"
							type="TLScope"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="scope"
										storage-type="TLType"
									/>
								</storage-algorithm>
							</annotations>
						</reference>
						<reference name="model"
							override="true"
							type="TLModel"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<chain>
												<get-value attribute="module"/>
												<get-value attribute="model"/>
											</chain>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</reference>
					</attributes>
				</interface>

				<interface name="TLStructuredType">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.TLStructuredType"/>
					</annotations>
					<generalizations>
						<generalization type="TLType"/>
					</generalizations>
					<attributes>
						<reference name="localParts"
							inverse-reference="owner"
							kind="backwards"
							multiple="true"
							ordered="true"
							type="TLStructuredTypePart"
						/>
					</attributes>
				</interface>

				<class name="TLEnumeration">
					<annotations>
						<export-binding impl="com.top_logic.model.io.bindings.ModelPartReferenceValueBinding"/>
						<implementation-binding interface-name="com.top_logic.model.TLEnumeration"/>
						<instance-presentation icon="theme:mime.TLEnumeration"/>
						<table name="FastList"/>
					</annotations>
					<generalizations>
						<generalization type="TLStructuredType"/>
					</generalizations>
					<attributes>
						<reference name="classifiers"
							inverse-reference="owner"
							kind="backwards"
							multiple="true"
							type="TLClassifier"
						/>
						<reference name="scope"
							override="true"
							type="TLModule"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="module"
										storage-type="TLType"
									/>
								</storage-algorithm>
							</annotations>
						</reference>
					</attributes>
				</class>

				<class name="TLPrimitive">
					<annotations>
						<export-binding impl="com.top_logic.model.io.bindings.ModelPartReferenceValueBinding"/>
						<implementation-binding interface-name="com.top_logic.model.TLPrimitive"/>
						<instance-presentation icon="theme:mime.TLPrimitive"/>
						<table name="TLDatatype"/>
					</annotations>
					<generalizations>
						<generalization type="TLType"/>
					</generalizations>
					<attributes/>
				</class>

				<class name="TLAssociation">
					<annotations>
						<export-binding impl="com.top_logic.model.io.bindings.ModelPartReferenceValueBinding"/>
						<implementation-binding interface-name="com.top_logic.model.TLAssociation"/>
						<instance-presentation icon="theme:mime.TLAssociation"/>
						<main-properties properties="_self, module"/>
						<table name="MetaElement"/>
					</annotations>
					<generalizations>
						<generalization type="TLStructuredType"/>
					</generalizations>
					<attributes>
						<reference name="union"
							multiple="true"
							type="TLAssociation"
						/>
						<reference name="subsets"
							inverse-reference="union"
							kind="backwards"
							multiple="true"
							type="TLAssociation"
						/>
					</attributes>
				</class>

				<class name="TLClass">
					<annotations>
						<export-binding impl="com.top_logic.model.io.bindings.ModelPartReferenceValueBinding"/>
						<implementation-binding interface-name="com.top_logic.model.TLClass"/>
						<instance-presentation icon="theme:mime.TLClass"/>
						<main-properties properties="_self, module"/>
						<table name="MetaElement"/>
					</annotations>
					<generalizations>
						<generalization type="TLStructuredType"/>
					</generalizations>
					<attributes>
						<property name="abstract"
							type="tl.core:Boolean"
						/>
						<property name="final"
							type="tl.core:Boolean"
						/>
						<reference name="generalizations"
							multiple="true"
							ordered="true"
							type="TLClass"
						>
							<annotations>
								<storage-algorithm>
									<list-storage
										monomorphic-table="true"
										order-attribute="order"
										table="MetaElement_generalizations"
									/>
								</storage-algorithm>
								<delete-protected/>
							</annotations>
						</reference>
						<reference name="specializations"
							inverse-reference="generalizations"
							kind="backwards"
							multiple="true"
							type="TLClass"
						/>
					</attributes>
				</class>

				<!-- Parts -->

				<interface name="TLTypePart">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.TLTypePart"/>
						<main-properties properties="name,owner,type"/>
					</annotations>
					<generalizations>
						<generalization type="TLNamedPart"/>
					</generalizations>
					<attributes>
						<reference name="type"
							mandatory="true"
							type="TLType"
						/>
						<reference name="model"
							override="true"
							type="TLModel"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<chain>
												<get-value attribute="owner"/>
												<get-value attribute="model"/>
											</chain>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</reference>
						<reference name="owner"
							type="TLType"
						/>
					</attributes>
				</interface>

				<interface name="DerivedTLTypePart">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.DerivedTLTypePart"/>
					</annotations>
					<generalizations>
						<generalization type="TLTypePart"/>
					</generalizations>
					<attributes>
						<property name="derived"
							type="tl.core:Boolean"
						/>
					</attributes>
				</interface>

				<interface name="TLStructuredTypePart">
					<annotations>
						<implementation-binding interface-name="com.top_logic.model.TLStructuredTypePart"/>
					</annotations>
					<generalizations>
						<generalization type="TLTypePart"/>
					</generalizations>
					<attributes>
						<reference name="owner"
							override="true"
							type="TLStructuredType"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="owner"
										storage-type="MetaAttribute"
									/>
								</storage-algorithm>
							</annotations>
						</reference>
						<reference name="type"
							override="true"
							type="TLType"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="type"
										storage-type="MetaAttribute"
									/>
								</storage-algorithm>
							</annotations>
						</reference>

						<property name="mandatory"
							type="tl.core:Boolean"
						/>
						<property name="multiple"
							type="tl.core:Boolean"
						/>
						<property name="bag"
							type="tl.core:Boolean"
						/>
						<property name="ordered"
							type="tl.core:Boolean"
						/>
						<reference name="classifiedBy"
							multiple="true"
							type="tl.model:TLClassifier"
						>
							<annotations>
								<storage-algorithm>
									<set-storage
										monomorphic-table="true"
										table="classifiedBy"
									/>
								</storage-algorithm>
								<delete-protected/>
							</annotations>
						</reference>
					</attributes>
				</interface>

				<class name="TLAssociationEnd">
					<annotations>
						<export-binding impl="com.top_logic.model.io.bindings.ModelPartReferenceValueBinding"/>
						<implementation-binding interface-name="com.top_logic.model.TLAssociationEnd"/>
						<instance-presentation icon="theme:mime.TLAssociationEnd"/>
						<main-properties properties="_self, owner, type"/>
						<table name="MetaAttribute"/>
					</annotations>
					<generalizations>
						<generalization type="TLStructuredTypePart"/>
					</generalizations>
					<attributes>
						<reference name="reference"
							inverse-reference="end"
							kind="backwards"
							type="TLReference"
						/>
						<property name="composite"
							type="tl.core:Boolean"
						/>
						<property name="aggregate"
							type="tl.core:Boolean"
						/>
						<property name="navigate"
							type="tl.core:Boolean"
						/>
					</attributes>
				</class>

				<class name="TLReference">
					<annotations>
						<export-binding impl="com.top_logic.model.io.bindings.ModelPartReferenceValueBinding"/>
						<implementation-binding interface-name="com.top_logic.model.TLReference"/>
						<instance-presentation icon="theme:mime.TLReference"/>
						<main-properties properties="_self, owner, type"/>
						<table name="MetaAttribute"/>
					</annotations>
					<generalizations>
						<generalization type="TLStructuredTypePart"/>
						<generalization type="DerivedTLTypePart"/>
					</generalizations>
					<attributes>
						<reference name="end"
							mandatory="true"
							type="TLAssociationEnd"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="end"
										storage-type="MetaAttribute"
									/>
								</storage-algorithm>
							</annotations>
						</reference>
						<reference name="type"
							override="true"
							type="TLType"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<chain>
												<get-value attribute="end"/>
												<get-value attribute="type"/>
											</chain>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</reference>
						<property name="mandatory"
							override="true"
							type="tl.core:Boolean"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<chain>
												<get-value attribute="end"/>
												<get-value attribute="mandatory"/>
											</chain>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</property>
						<property name="multiple"
							override="true"
							type="tl.core:Boolean"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<chain>
												<get-value attribute="end"/>
												<get-value attribute="multiple"/>
											</chain>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</property>
						<property name="bag"
							override="true"
							type="tl.core:Boolean"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<chain>
												<get-value attribute="end"/>
												<get-value attribute="bag"/>
											</chain>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</property>
						<property name="ordered"
							override="true"
							type="tl.core:Boolean"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<chain>
												<get-value attribute="end"/>
												<get-value attribute="ordered"/>
											</chain>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</property>
					</attributes>
				</class>

				<class name="TLProperty">
					<annotations>
						<export-binding impl="com.top_logic.model.io.bindings.ModelPartReferenceValueBinding"/>
						<implementation-binding interface-name="com.top_logic.model.TLProperty"/>
						<instance-presentation icon="theme:mime.TLProperty"/>
						<main-properties properties="_self, owner, type"/>
						<table name="MetaAttribute"/>
					</annotations>
					<generalizations>
						<generalization type="TLStructuredTypePart"/>
						<generalization type="DerivedTLTypePart"/>
					</generalizations>
				</class>

				<class name="TLClassifier">
					<annotations>
						<export-binding impl="com.top_logic.model.io.bindings.ModelPartReferenceValueBinding"/>
						<implementation-binding interface-name="com.top_logic.model.TLClassifier"/>
						<instance-presentation icon="theme:mime.TLClassifier"/>
						<main-properties properties="_self, owner"/>
						<table name="FastListElt"/>
					</annotations>
					<generalizations>
						<generalization type="TLTypePart"/>
					</generalizations>
					<attributes>
						<property name="default"
							type="tl.core:Boolean"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<!-- FastListElement implements the method by fetching default from FastList and check for equality. This can not be described in the model. -->
											<method-call method="com.top_logic.knowledge.wrap.list.FastListElement#isDefault()"/>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</property>
						<reference name="owner"
							override="true"
							type="TLEnumeration"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="owner"
										storage-type="FastListElt"
									/>
								</storage-algorithm>
							</annotations>
						</reference>
						<reference name="type"
							override="true"
							type="TLEnumeration"
						>
							<annotations>
								<storage-algorithm>
									<derived-storage>
										<expression-evaluation>
											<chain>
												<get-value attribute="owner"/>
											</chain>
										</expression-evaluation>
									</derived-storage>
								</storage-algorithm>
							</annotations>
						</reference>
					</attributes>
				</class>

				<class name="TLModuleSingletons">
					<annotations>
						<table name="TLModule_singletons"/>
						<implementation-binding interface-name="com.top_logic.model.TLModuleSingleton"/>
					</annotations>
					<attributes>
						<property name="name"
							type="tl.core:String"
						/>
						<reference name="module"
							type="TLModule"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="module"
										storage-type="TLModule_singletons"
									/>
								</storage-algorithm>
							</annotations>
						</reference>
						<reference name="singleton"
							navigate="true"
							type="TLObject"
						>
							<annotations>
								<storage-algorithm>
									<foreign-key-storage
										storage-attribute="singleton"
										storage-type="TLModule_singletons"
									/>
								</storage-algorithm>
							</annotations>
						</reference>
					</attributes>
				</class>
			</types>
		</module>
		
		<module name="tl.accounts">
			<class name="Person">
				<annotations>
					<implementation-binding class-name="com.top_logic.knowledge.wrap.person.Person"/>
					<instance-presentation
						icon="theme:mime.tl.accounts.Person"
						large-icon="theme:mime.tl.accounts.Person.large"
					/>
					<main-properties properties="_self, name, locale"/>
					<table name="Person"/>
				</annotations>
				<attributes>
					<property name="name"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="locale"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="pwdhistory"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
							<visibility value="hidden"/>
						</annotations>
					</property>
					<property name="lastPwdChange"
						type="tl.core:Date"
					>
						<annotations>
							<delete-protected/>
							<visibility value="hidden"/>
						</annotations>
					</property>
					<property name="unusedNotified"
						type="tl.core:Boolean"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="wasAlive"
						type="tl.core:Boolean"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="dataDeviceID"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="authDeviceID"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="restrictedUser"
						type="tl.core:Boolean"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<reference name="representativeGroup"
						multiple="false"
						type="tl.accounts:Group"
					>
						<annotations>
							<storage-algorithm>
								<singleton-link-storage
									monomorphic-table="true"
									table="definesGroup"
								/>
							</storage-algorithm>
							<delete-protected/>
						</annotations>
					</reference>
				</attributes>
			</class>
			<class name="Group">
				<annotations>
					<table name="Group"/>
					<implementation-binding class-name="com.top_logic.tool.boundsec.wrap.Group"/>
					<instance-presentation
						icon="theme:mime.tl.accounts.Group"
						large-icon="theme:mime.tl.accounts.Group.large"
					/>
				</annotations>
				<attributes>
					<property name="name"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="isSystem"
						type="tl.core:Boolean"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
				</attributes>
			</class>
			<class name="Address">
				<annotations>
					<implementation-binding class-name="com.top_logic.knowledge.wrap.Address"/>
					<main-properties properties="_self, zip, city, state, country"/>
					<table name="Address"/>
				</annotations>
				<attributes>
					<property name="street1"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="street2"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="zip"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="city"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="country"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="state"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="telephone1"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="telephone2"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="mobile"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="fax1"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="fax2"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="eMail"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
				</attributes>
			</class>
			<class name="Role">
				<annotations>
					<table name="BoundedRole"/>
					<implementation-binding class-name="com.top_logic.tool.boundsec.wrap.BoundedRole"/>
					<instance-presentation icon="theme:mime.tl.accounts.Role"/>
				</annotations>
				<attributes>
					<property name="name"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="isSystem"
						type="tl.core:Boolean"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="description"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
				</attributes>
			</class>
		</module>

		<module name="tl.folder">
			<interface name="WithResource">
				<attributes>
					<property name="physicalResource"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
							<visibility value="hidden"/>
							<fulltext-relevant value="false"/>
						</annotations>
					</property>
				</attributes>
			</interface>
			<class name="WebFolder">
				<generalizations>
					<generalization type="WithResource"/>
				</generalizations>
				<annotations>
					<table name="WebFolder"/>
					<config-type value="WEBFOLDER"/>
					<implementation-binding class-name="com.top_logic.knowledge.wrap.WebFolder"/>
					<instance-presentation
						icon="theme:tl.folder.WebFolder"
						large-icon="theme:tl.folder.WebFolder.large"
					/>
				</annotations>
				<attributes>
					<property name="name"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="description"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="folderType"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
				</attributes>
			</class>
			<class name="Document">
				<generalizations>
					<generalization type="WithResource"/>
				</generalizations>
				<annotations>
					<table name="Document"/>
					<config-type value="DOCUMENT"/>
					<implementation-binding class-name="com.top_logic.knowledge.wrap.Document"/>
					<instance-presentation icon="theme:tl.folder.Document"/>
					<main-properties properties="_self, size, versionNumber"/>
				</annotations>
				<attributes>
					<property name="name"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="size"
						mandatory="true"
						type="tl.core:Long"
					>
						<annotations>
							<delete-protected/>
							<fulltext-relevant value="false"/>
						</annotations>
					</property>
					<property name="versionNumber"
						type="tl.core:Integer"
					>
						<annotations>
							<delete-protected/>
							<fulltext-relevant value="false"/>
						</annotations>
					</property>
				</attributes>
			</class>
			<class name="DocumentVersion">
				<annotations>
					<table name="DocumentVersion"/>
					<implementation-binding class-name="com.top_logic.knowledge.wrap.DocumentVersion"/>
					<instance-presentation icon="theme:tl.folder.DocumentVersion"/>
				</annotations>
				<attributes>
					<property name="revision"
						mandatory="true"
						type="tl.core:Integer"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<reference name="doc"
						kind="forwards"
						navigate="true"
						type="Document"
					>
						<annotations>
							<storage-algorithm>
								<foreign-key-storage
									storage-attribute="doc"
									storage-type="DocumentVersion"
								/>
							</storage-algorithm>
						</annotations>
					</reference>
				</attributes>
			</class>
		</module>

		<module name="tl.units">
			<interface name="AbstractUnit">
				<attributes>
					<property name="name"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="format"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
					<property name="sortOrder"
						mandatory="true"
						type="tl.core:Integer"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
				</attributes>
			</interface>
			<class name="Currency">
				<generalizations>
					<generalization type="AbstractUnit"/>
				</generalizations>
				<annotations>
					<implementation-binding class-name="com.top_logic.knowledge.wrap.currency.Currency"/>
					<main-properties properties="_self, factor"/>
					<table name="Currency"/>
				</annotations>
				<attributes>
					<reference name="baseUnit"
						kind="forwards"
						navigate="true"
						type="Currency"
					>
						<annotations>
							<storage-algorithm>
								<foreign-key-storage
									storage-attribute="baseUnit"
									storage-type="Currency"
								/>
							</storage-algorithm>
						</annotations>
					</reference>
					<property name="factor"
						type="tl.core:Double"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
				</attributes>
			</class>
			<class name="Unit">
				<generalizations>
					<generalization type="AbstractUnit"/>
				</generalizations>
				<annotations>
					<implementation-binding
						class-name="com.top_logic.knowledge.wrap.unit.UnitWrapper"
						interface-name="com.top_logic.knowledge.wrap.unit.Unit"
					/>
					<instance-presentation
						icon="theme:mime.tl.units.Unit"
						large-icon="theme:mime.tl.units.Unit.large"
					/>
					<main-properties properties="_self, format"/>
					<table name="Unit"/>
				</annotations>
				<attributes>
					<reference name="baseUnit"
						kind="forwards"
						navigate="true"
						type="Unit"
					>
						<annotations>
							<storage-algorithm>
								<foreign-key-storage
									storage-attribute="baseUnit"
									storage-type="Unit"
								/>
							</storage-algorithm>
						</annotations>
					</reference>
					<property name="factor"
						type="tl.core:Double"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
				</attributes>
			</class>
		</module>

		<module name="tl.comments">
			<class name="Comment">
				<annotations>
					<table name="Comment"/>
					<implementation-binding class-name="com.top_logic.element.comment.wrap.Comment"/>
					<instance-presentation
						icon="theme:mime.tl.comments.Comment"
					/>
				</annotations>
				<attributes>
					<property name="content"
						mandatory="true"
						type="tl.core:String"
					>
						<annotations>
							<delete-protected/>
						</annotations>
					</property>
				</attributes>
			</class>
		</module>
	</xsl:template>
	
	<xsl:template match="@type">
		<xsl:choose>
			<xsl:when test="string(.) = 'tl.tables:ExternalReferenceTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Ticket:ExternalReference'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.legacy.tabletypes:ExternalReferenceTable'"><xsl:attribute name="type"><xsl:value-of select="'Ticket:ExternalReference'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ActivityTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'RiskItems:Activity'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ApproachingCurveTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'ApproachingCurveStructure:ApproachingCurveType'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:AssignedGoalTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'AssignedGoals:AssignedGoal'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:CommitteeTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'CommitteeStruct:Committee'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:CostTypeTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'costStructure:CostAreaChild'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:CostUnitTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'costStructure:CostUnit.internal'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:DecisionMemoTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Decisions:DecisionMemo'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ExportTemplatePlaceholderTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'posElement:ExportTemplatePlaceholder'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ExportTemplateTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'posElement:ExportTemplate'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:FinalReportTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'FinalReports:FinalReport'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:GovernedTaskTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'GovernedObjects:GovernedTask'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:KPGroupTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'KPI:KPI.Group'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:MeetingTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Meetings:Meetings.all'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:MilestoneTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Milestones:Milestones.all'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:OrgUnitTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'orgStructure:OrgUnit.base'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ProgramTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Programs:Program'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ProjectResultTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'FinalReports:ProjectResult'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ProjectTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projElement:Project'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ScenarioTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'ScenarioStruct:Scenario.all'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:StatusReportDetailTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'StatusReports:StatusReportDetail'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:StatusReportTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'StatusReports:StatusReport'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:SubprojectTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projElement:Subproject'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:TagTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Tag:Tag'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:TargetProductTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Programs:TargetProduct'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:TemplateActivityTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projectTemplate:Activity'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:TemplateMilestoneTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projectTemplate:projectTemplate.Milestone'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:WorkpackageTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'projElement:Workpackage'"/></xsl:attribute></xsl:when>
		    <xsl:when test="string(.) = 'tl.tables:ObjectTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLObject'"/></xsl:attribute></xsl:when>

			<xsl:when test="string(.) = 'tl.legacy.tabletypes:PersonTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Person'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.tables:PersonTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Person'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:GroupTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Group'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:WebFolderTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.folder:WebFolder'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:DocumentTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.folder:Document'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:DocumentVersionTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.folder:DocumentVersion'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.tables:AbstractUnitTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'tl.units:AbstractUnit'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:CurrencyTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.units:Currency'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:UnitTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.units:Unit'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:CommentTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.comments:Comment'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:MetaElementTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLClass'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.tables:FastListEltTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLClassifier'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:FastListEltTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLClassifier'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:MetaAttributeTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLStructuredTypePart'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:FastListTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLEnumeration'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:AddressTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Address'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:BoundedRoleTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.accounts:Role'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.tables:UsedSkillProfileTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Resources:UsedSkillProfile'"/></xsl:attribute></xsl:when> 
			<xsl:when test="string(.) = 'tl.tables:ContactTableInterface'"><xsl:attribute name="type"><xsl:value-of select="'Contacts:Contact.Person'"/></xsl:attribute></xsl:when>
    
		    <xsl:when test="starts-with(string(.), 'tl.tables:')"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLObject'"/></xsl:attribute></xsl:when>
		    <xsl:when test="starts-with(string(.), 'tl.legacy.tabletypes:')"><xsl:attribute name="type"><xsl:value-of select="'tl.model:TLObject'"/></xsl:attribute></xsl:when>

			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="reference-builder">
		<!-- Remove. -->
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>