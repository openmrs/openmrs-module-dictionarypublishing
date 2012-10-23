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
package org.openmrs.module.dictionarypublishing.web.controller;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.conceptpubsub.api.ConceptPubSubService;
import org.openmrs.module.dictionarypublishing.DictionaryPublishingConstants;
import org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService;
import org.openmrs.module.metadatasharing.MetadataSharing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The controller to process requests for concept dictionary packages
 */
@Controller
public class DictionaryPublishingController {
	
	public static final String MODULE_URL = "/module/dictionarypublishing/";
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	private DictionaryPublishingService service;
	
	@Autowired
	private ConceptPubSubService mappingService;
	
	@RequestMapping(value = "concept-dictionary", method = RequestMethod.GET)
	public String getConceptDictionary(ModelMap model) {
		String groupUuid = Context.getAdministrationService().getGlobalProperty(
		    DictionaryPublishingConstants.GP_PACKAGE_GROUP_UUID);
		if (StringUtils.isBlank(groupUuid)) {
			throw new APIException("The requested dictionary hasn't yet been published");
		}
		
		return "redirect:/ws/rest/metadatasharing/package/" + groupUuid + "/latest.form";
	}
	
	@RequestMapping(value = MODULE_URL + "publish")
	public String configurePublish(ModelMap model) throws Exception {
		if (!mappingService.isAddLocalMappingOnExport() || !mappingService.isLocalSourceConfigured()) {
			return MODULE_URL + "localSourceNotConfigured";
		}
		
		if (!MetadataSharing.getInstance().isPublishConfigured()) {
			return MODULE_URL + "publishNotConfigured";
		}
		
		if (!service.hasDictionaryBeenEverPublished()) {
			Date nextPublishDate = service.getLastPublishDate();
			model.addAttribute("nextPublishDate", nextPublishDate);
		}
		
		long conceptsCount = service.getConceptsCountSinceLastDatePublished();
		model.addAttribute("conceptsCount", conceptsCount);
		
		ConceptSource localSource = mappingService.getLocalSource();
		model.addAttribute("localSource", localSource);
		
		boolean hasEverBeenPublished = service.hasDictionaryBeenEverPublished();
		model.addAttribute("hasEverBeenPublished", hasEverBeenPublished);
		
		String publishedUrl = service.getPublishedUrl();
		model.addAttribute("publishedUrl", publishedUrl);
		
		if (hasEverBeenPublished) {
			boolean isPublished = service.isDictionaryPublished();
			model.addAttribute("isPublished", isPublished);
			
			int lastPublishedVersion = service.getLastPublishedVersion();
			model.addAttribute("lastPublishedVersion", lastPublishedVersion);
		}
		
		return null;
	}
	
	@RequestMapping(value = MODULE_URL + "publishNewVersion", method = RequestMethod.POST)
	public String publishNewVersion(ModelMap model) throws Exception {
		service.publishNewVersion();
		return "redirect:publish.form";
	}
	
	@RequestMapping(value = MODULE_URL + "disablePublishing", method = RequestMethod.POST)
	public String disablePublishing() throws Exception {
		service.disablePublishingDictionary();
		return "redirect:publish.form";
	}
	
	@RequestMapping(value = MODULE_URL + "enablePublishing", method = RequestMethod.POST)
	public String enablePublishing() {
		service.enablePublishingDictionary();
		return "redirect:publish.form";
	}
}
