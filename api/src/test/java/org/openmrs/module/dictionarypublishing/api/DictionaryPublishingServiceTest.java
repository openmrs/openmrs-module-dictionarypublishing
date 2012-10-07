/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dictionarypublishing.api;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.conceptpubsub.api.ConceptPubSubService;
import org.openmrs.module.dictionarypublishing.DictionaryPublishingConstants;
import org.openmrs.module.metadatasharing.ExportedPackage;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests {@link $ DictionaryPublishingService} .
 */
public class DictionaryPublishingServiceTest extends BaseModuleContextSensitiveTest {
	
	private static final long DELAY = 200;
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(DictionaryPublishingService.class));
	}
	
	/**
	 * @see {@link DictionaryPublishingService#publishNewVersion()}
	 */
	@Test
	@Verifies(value = "should create a package since the last full publish date", method = "publishNewVersion()")
	public void publishNewVersion_shouldCreateAPackageSinceTheLastFullPublishDate() throws Exception {
		AdministrationService as = Context.getAdministrationService();
		//Perform the full publish for test purposes
		publishInitialVersion(as);
		
		List<ExportedPackage> packages = Context.getService(MetadataSharingService.class).getAllExportedPackages();
		ExportedPackage p = packages.get(0);
		Assert.assertEquals(false, p.isIncrementalVersion());
		Assert.assertEquals(true, p.isPublished());
		Assert.assertEquals(6, p.getItems().size());
		//should have cleared the GP since this is the initial export
		Assert.assertNull(as.getGlobalProperty(DictionaryPublishingConstants.GP_NEXT_DICTIONARY_PUBLISH_DATE));
		Assert.assertEquals(p.getGroupUuid(),
		    as.getGlobalProperty(DictionaryPublishingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID));
	}
	
	/**
	 * @see {@link DictionaryPublishingService#publishNewVersion()}
	 */
	@Test
	@Verifies(value = "should create a package with only changes since the last full publish date", method = "publishNewVersion()")
	public void publishNewVersion_shouldCreateAPackageWithOnlyChangesSinceTheLastFullPublishDate() throws Exception {
		AdministrationService as = Context.getAdministrationService();
		publishInitialVersion(as);
		
		//edit a concept for testing purposes so that we have something to publish
		ConceptService cs = Context.getConceptService();
		Concept concept = cs.getConcept(5089);
		Assert.assertFalse("1.1".equals(concept.getVersion()));
		concept.setVersion("1.1");
		cs.saveConcept(concept);
		
		Context.getService(DictionaryPublishingService.class).publishNewVersion();
		Thread.sleep(DELAY);
		MetadataSharingService mss = Context.getService(MetadataSharingService.class);
		List<ExportedPackage> packages = mss.getAllExportedPackages();
		Assert.assertEquals(2, packages.size());
		Assert.assertEquals(
		    1,
		    mss.getLatestExportedPackageByGroup(
		        as.getGlobalProperty(DictionaryPublishingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID)).getItems().size());
	}
	
	/**
	 * @see {@link DictionaryPublishingService#publishNewVersion()}
	 */
	@Test
	@Verifies(value = "should not create no package if there are no changes since the last full publish date", method = "publishNewVersion()")
	public void publishNewVersion_shouldNotCreateNoPackageIfThereAreNoChangesSinceTheLastFullPublishDate() throws Exception {
		publishInitialVersion(Context.getAdministrationService());
		
		Context.getService(DictionaryPublishingService.class).publishNewVersion();
		Thread.sleep(DELAY);
		Assert.assertEquals(1, Context.getService(MetadataSharingService.class).getAllExportedPackages().size());
	}
	
	/**
	 * @see {@link DictionaryPublishingService#disablePublishingDictionary()}
	 */
	@Test
	@Verifies(value = "should un publish the matching exported packages in this dictionary", method = "unpublishDictionary()")
	public void unpublishDictionary_shouldUnPublishTheMatchingExportedPackagesInThisDictionary() throws Exception {
		AdministrationService as = Context.getAdministrationService();
		publishInitialVersion(as);
		String groupUuid = as.getGlobalProperty(DictionaryPublishingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
		Assert.assertNotNull(groupUuid);
		MetadataSharingService mss = Context.getService(MetadataSharingService.class);
		List<ExportedPackage> packages = mss.getExportedPackagesByGroup(groupUuid);
		Assert.assertEquals(1, packages.size());
		Assert.assertEquals(true, packages.get(0).isPublished());
		
		Context.getService(DictionaryPublishingService.class).disablePublishingDictionary();
		packages = mss.getExportedPackagesByGroup(groupUuid);
		Assert.assertEquals(1, packages.size());
		Assert.assertEquals(false, packages.get(0).isPublished());
	}
	
	/**
	 * Performs the first full publish
	 * 
	 * @param as {@link AdministrationService} object
	 * @throws InterruptedException
	 */
	private static void publishInitialVersion(AdministrationService as) throws Exception {
		Context.getService(ConceptPubSubService.class).setLocalConceptSource(Context.getConceptService().getConceptSource(2));
		
		GlobalProperty gp = new GlobalProperty(DictionaryPublishingConstants.GP_NEXT_DICTIONARY_PUBLISH_DATE,
		        "2008-08-18 00:00:00");
		as.saveGlobalProperty(gp);
		MetadataSharingService mss = Context.getService(MetadataSharingService.class);
		Context.getService(DictionaryPublishingService.class).publishNewVersion();
		Thread.sleep(DELAY);
		Assert.assertEquals(1, mss.getAllExportedPackages().size());
		Assert.assertNotNull(mss.getAllExportedPackages().get(0).getOpenmrsVersion());
	}
}
