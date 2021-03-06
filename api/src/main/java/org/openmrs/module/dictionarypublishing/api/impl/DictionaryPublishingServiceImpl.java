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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dictionarypublishing.DictionaryPublishingConstants;
import org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService;
import org.openmrs.module.dictionarypublishing.api.db.DictionaryPublishingDAO;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.metadatasharing.ExportedPackage;
import org.openmrs.module.metadatasharing.MetadataSharing;
import org.openmrs.module.metadatasharing.MetadataSharingConsts;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.openmrs.module.metadatasharing.task.impl.ExportPackageTask;
import org.openmrs.module.metadatasharing.wrapper.PackageExporter;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of {@link DictionaryPublishingService}.
 */
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
	
	public AdministrationService getAdministrationService() {
		return Context.getAdministrationService();
	}
	
	public MetadataMappingService getMetadataMappingService() {
		return Context.getService(MetadataMappingService.class);
	}
	
	public MetadataSharingService getMDSService() {
		return Context.getService(MetadataSharingService.class);
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#publishNewVersion()
	 */
	@Transactional
	@Override
	public void publishNewVersion() throws APIException {
		GlobalProperty lastPublishDateGP = Context.getAdministrationService().getGlobalPropertyObject(
		    DictionaryPublishingConstants.GP_LAST_PUBLISH_DATE);
		
		final Date sinceDate;
		if (hasDictionaryBeenEverPublished()) {
			sinceDate = getLastPublishedPackage().getDateCreated();
		} else {
			sinceDate = getLastPublishDate();
		}
		
		final boolean isInitialExport;
		String groupUuid = Context.getAdministrationService().getGlobalProperty(
		    DictionaryPublishingConstants.GP_PACKAGE_GROUP_UUID);
		
		final ExportedPackage expPackage;
		
		if (StringUtils.isBlank(groupUuid)) {
			isInitialExport = true;
			
			expPackage = null;
		} else {
			isInitialExport = !hasDictionaryBeenEverPublished();
			if (!isInitialExport) {
				expPackage = getLastPublishedPackage();
			} else {
				expPackage = null;
			}
		}
		
		final List<Concept> concepts = dao.getConceptsToExport(sinceDate);
		
		for (Concept concept : concepts) {
			getMetadataMappingService().addLocalMappingToConcept(concept);
        }
		
		//get the date as early as possible to minimize the chance of having an update
		//or insert of a concept between now until when we actually save the export
		final Date dateCreated = new Date();
		if (concepts.size() > 0 && (isInitialExport || expPackage != null)) {
			PackageExporter exporter = MetadataSharing.getInstance().newPackageExporter();
			exporter.getPackage().setDescription(
			    "Contains " + concepts.size() + " concepts modified or created since "
			            + new SimpleDateFormat(MetadataSharingConsts.DATE_FORMAT).format(sinceDate));
			exporter.getPackage().setDateCreated(dateCreated);
			if (isInitialExport) {
				exporter.getPackage().setName(getMetadataMappingService().getLocalSource().getName() + " Concept Dictionary");
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
				if (lastPublishDateGP != null) {
					lastPublishDateGP.setPropertyValue("");
					as.saveGlobalProperty(lastPublishDateGP);
				}
				GlobalProperty groupUuidGP = as
				        .getGlobalPropertyObject(DictionaryPublishingConstants.GP_PACKAGE_GROUP_UUID);
				if (groupUuidGP == null) {
					groupUuidGP = new GlobalProperty(DictionaryPublishingConstants.GP_PACKAGE_GROUP_UUID,
					        exporter.getExportedPackage().getGroupUuid(),
					        "The group uuid of the packages exported from this dictionary");
				} else {
					groupUuidGP.setPropertyValue(exporter.getExportedPackage().getGroupUuid());
				}
				as.saveGlobalProperty(groupUuidGP);
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#disablePublishingDictionary()
	 */
	@Transactional
	@Override
	public void disablePublishingDictionary() throws APIException {
		AdministrationService as = Context.getAdministrationService();
		String groupUuid = as.getGlobalProperty(DictionaryPublishingConstants.GP_PACKAGE_GROUP_UUID);
		if (StringUtils.isBlank(groupUuid)) {
			log.warn("The dictionary is not yet published");
			return;
		}
		
		MetadataSharingService mds = Context.getService(MetadataSharingService.class);
		for (ExportedPackage exportedPackage : mds.getExportedPackagesByGroup(groupUuid)) {
			if (exportedPackage.isPublished()) {
				exportedPackage.setPublished(false);
				mds.saveExportedPackage(exportedPackage);
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#getLastPublishDate()
	 */
	@Transactional(readOnly = true)
	@Override
	public Date getLastPublishDate() throws APIException {
		String gp = getAdministrationService().getGlobalProperty(
		    DictionaryPublishingConstants.GP_LAST_PUBLISH_DATE, "");
		if (StringUtils.isBlank(gp)) {
			return new Date(0);
		}
		try {
			return new SimpleDateFormat(MetadataSharingConsts.DATE_FORMAT).parse(gp);
		}
		catch (ParseException e) {
			throw new APIException("The " + DictionaryPublishingConstants.GP_LAST_PUBLISH_DATE
			        + " gp has a wrong format, expected: " + MetadataSharingConsts.DATE_FORMAT + ", found: " + gp, e);
		}
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#getConceptsCountSinceLastDatePublished()
	 */
	@Transactional(readOnly = true)
	@Override
	public long getConceptsCountSinceLastDatePublished() throws APIException {
		long conceptsCount;
		if (hasDictionaryBeenEverPublished()) {
			conceptsCount = dao.getConceptsToExportCount(getLastPublishedPackage().getDateCreated());
		} else {
			conceptsCount = dao.getConceptsToExportCount(getLastPublishDate());
		}
		
		return conceptsCount;
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#isDictionaryPublished()
	 */
	@Transactional(readOnly = true)
	@Override
	public boolean isDictionaryPublished() throws APIException {
		String uuid = getDictionaryPackageUuid();
		if (!StringUtils.isBlank(uuid)) {
			ExportedPackage exportedPackage = getMDSService().getLatestExportedPackageByGroup(uuid);
			return (exportedPackage != null) ? exportedPackage.isPublished() : false;
		} else {
			return false;
		}
	}
	
	private ExportedPackage getLastPublishedPackage() throws APIException {
		String uuid = getDictionaryPackageUuid();
		if (!StringUtils.isBlank(uuid)) {
			ExportedPackage exportedPackage = getMDSService().getLatestExportedPackageByGroup(uuid);
			if (exportedPackage != null) {
				return exportedPackage;
			} else {
				throw new APIException("The " + DictionaryPublishingConstants.GP_PACKAGE_GROUP_UUID
				        + " go points to an unexisting package: " + uuid);
			}
		} else {
			throw new APIException("The dictionary has been never published.");
		}
	}
	
	private String getDictionaryPackageUuid() {
		return getAdministrationService().getGlobalProperty(DictionaryPublishingConstants.GP_PACKAGE_GROUP_UUID,
		    "");
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#hasDictionaryBeenEverPublished()
	 */
	@Transactional(readOnly = true)
	@Override
	public boolean hasDictionaryBeenEverPublished() throws APIException {
		String uuid = getDictionaryPackageUuid();
		if (!StringUtils.isBlank(uuid)) {
			ExportedPackage exportedPackage = getMDSService().getLatestExportedPackageByGroup(uuid);
			return (exportedPackage != null);
		} else {
			return false;
		}
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#getLastPublishedVersion()
	 */
	@Transactional(readOnly = true)
	@Override
	public int getLastPublishedVersion() throws APIException {
		return getLastPublishedPackage().getVersion();
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#getPublishedUrl()
	 */
	@Transactional(readOnly = true)
	@Override
	public String getPublishedUrl() throws APIException {
		String gp = Context.getAdministrationService().getGlobalProperty(MetadataSharingConsts.GP_URL_PREFIX, "");
		
		if (StringUtils.isBlank(gp)) {
			return null;
		} else {
			return gp + "/concept-dictionary.form";
		}
	}
	
	/**
	 * @see org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService#enablePublishingDictionary()
	 */
	@Transactional
	@Override
	public void enablePublishingDictionary() throws APIException {
		String groupUuid = Context.getAdministrationService().getGlobalProperty(
		    DictionaryPublishingConstants.GP_PACKAGE_GROUP_UUID, "");
		if (!StringUtils.isBlank(groupUuid)) {
			List<ExportedPackage> packages = getMDSService().getExportedPackagesByGroup(groupUuid);
			for (ExportedPackage pack : packages) {
				pack.setPublished(true);
				getMDSService().saveExportedPackage(pack);
			}
		}
	}
	
}
