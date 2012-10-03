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
package org.openmrs.module.dictionarypublishing.api.db;

import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.module.dictionarypublishing.api.DictionaryPublishingService;

/**
 * Database methods for {@link DictionaryPublishingService}.
 */
public interface DictionaryPublishingDAO {
	
	/**
	 * Gets concepts to be published, if this is not the first publish after the dictionary was
	 * created, only concepts that were created or modified after the last full subscription date
	 * will be included otherwise all, the lastPublishDate
	 * 
	 * @param fromDate the date when the last publish was done
	 * @return a list of concepts
	 * @should return all concept modified or created after the specified date
	 */
	public List<Concept> getConceptsToExport(Date fromDate);
}
