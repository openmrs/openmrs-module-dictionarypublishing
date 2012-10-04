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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.dictionarypublishing.DictionaryPublishingConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The controller to process requests for concept dictionary packages
 */
@Controller
public class ConceptDictionaryController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/dictionarypublishing/concept-dictionary", method = RequestMethod.GET)
	public String getConceptDictionary(ModelMap model) {
		String groupUuid = Context.getAdministrationService().getGlobalProperty(
		    DictionaryPublishingConstants.EXPORTED_PACKAGES_GROUP_UUID);
		if (StringUtils.isBlank(groupUuid)) {
			throw new APIException("The rquested dictionary hasn't yet been published");
		}
		
		return "redirect:/module/metadatasharing/package/" + groupUuid;
	}
}
