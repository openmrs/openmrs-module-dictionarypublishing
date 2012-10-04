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

import org.openmrs.api.OpenmrsService;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured
 * in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(DictionaryPublishingService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
public interface DictionaryPublishingService extends OpenmrsService {
	
	/**
	 * Creates a package and publish all concepts modified since the last published version of a
	 * dictionary
	 * 
	 * @should create a package since the last full publish date
	 * @should create a package with only changes since the last full publish date
	 * @should not create no package if there are no changes since the last full publish date
	 * @throws Exception TODO
	 */
	public void publishNewVersion() throws Exception;
}
