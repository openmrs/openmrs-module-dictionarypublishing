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
package org.openmrs.module.dictionarypublishing.api.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dictionarypublishing.DictionaryPublishingConstants;
import org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService;
import org.openmrs.module.dictionarypublishing.api.db.DictionaryPublishingDAO;
import org.openmrs.module.metadatasharing.ExportedPackage;
import org.openmrs.module.metadatasharing.MetadataSharing;
import org.openmrs.module.metadatasharing.MetadataSharingConsts;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.openmrs.module.metadatasharing.task.impl.ExportPackageTask;
import org.openmrs.module.metadatasharing.wrapper.PackageExporter;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of {@link DictionaryPublishingService}.
 */
@Transactional
public class DictionaryPublishingServiceImpl extends BaseOpenmrsService implements DictionaryPublishingService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private DictionaryPublishingDAO dao;
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(DictionaryPublishingDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @return the dao
	 */
	public DictionaryPublishingDAO getDao() {
		return dao;
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#publishNewVersion()
	 */
	@Override
	public void publishNewVersion() throws Exception {
		GlobalProperty lastFullPublishDateGP = Context.getAdministrationService().getGlobalPropertyObject(
		    DictionaryPublishingConstants.GP_LAST_FULL_DICTIONARY_PUBLISH_DATE);
		Date fromDate = null;
		boolean isInitialExport = false;
		MetadataSharingService mds = Context.getService(MetadataSharingService.class);
		ExportedPackage expPackage = null;
		if (lastFullPublishDateGP != null && StringUtils.isNotBlank(lastFullPublishDateGP.getPropertyValue())) {
			fromDate = new SimpleDateFormat(MetadataSharingConsts.DATE_FORMAT).parse(lastFullPublishDateGP
			        .getPropertyValue());
			isInitialExport = true;
		} else {
			String groupUuid = Context.getAdministrationService().getGlobalProperty(
			    DictionaryPublishingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
			if (StringUtils.isNotBlank(groupUuid)) {
				expPackage = mds.getLatestExportedPackageByGroup(groupUuid);
				fromDate = expPackage.getDateCreated();
			}
		}
		
		List<Concept> concepts = dao.getConceptsToExport(fromDate);
		//get the date as early as possible to minimize the chance of having an update
		//or insert of a concept between now until when we actually save the export
		Date dateCreated = new Date();
		if (concepts.size() > 0 && (isInitialExport || expPackage != null)) {
			PackageExporter exporter = MetadataSharing.getInstance().newPackageExporter();
			exporter.getPackage().setDescription("Contains " + concepts.size() + " concepts ");
			exporter.getPackage().setDateCreated(dateCreated);
			exporter.getPackage().setOpenmrsVersion(OpenmrsConstants.OPENMRS_VERSION_SHORT);
			if (isInitialExport) {
				exporter.getPackage().setName("Package");
				exporter.getExportedPackage().setIncrementalVersion(false);
				exporter.getExportedPackage().setPublished(true);
			} else {
				exporter.getPackage().setName(expPackage.getName());
				exporter.getExportedPackage().setIncrementalVersion(true);
				exporter.getPackage().setGroupUuid(expPackage.getGroupUuid());
				exporter.getPackage().setVersion(expPackage.getVersion() + 1);
				exporter.getExportedPackage().setPublished(expPackage.isPublished());
			}
			
			for (Concept concept : concepts) {
				exporter.addItem(concept);
			}
			
			ExportPackageTask task = new ExportPackageTask(exporter.getExportedPackage(), true);
			task.execute();
			if (isInitialExport) {
				AdministrationService as = Context.getAdministrationService();
				as.purgeGlobalProperty(lastFullPublishDateGP);
				GlobalProperty groupUuidGP = as
				        .getGlobalPropertyObject(DictionaryPublishingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID);
				if (groupUuidGP == null) {
					groupUuidGP = new GlobalProperty(DictionaryPublishingConstants.GP_DICTIONARY_PACKAGE_GROUP_UUID,
					        exporter.getExportedPackage().getGroupUuid(),
					        "The group uuid of the packages exported from this dictionary");
				} else {
					groupUuidGP.setPropertyValue(exporter.getExportedPackage().getGroupUuid());
				}
				as.saveGlobalProperty(groupUuidGP);
			}
		}
	}
}
