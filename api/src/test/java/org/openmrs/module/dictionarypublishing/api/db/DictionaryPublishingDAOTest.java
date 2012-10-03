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

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.module.metadatasharing.MetadataSharingConsts;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;

public class DictionaryPublishingDAOTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private DictionaryPublishingDAO dao;
	
	/**
	 * @see {@link DictionaryPublishingDAO#getConceptsToExport(Date)}
	 */
	@Test
	@Verifies(value = "should return all concept modified or created after the specified date", method = "getConceptsToExport(Date)")
	public void getConceptsToExport_shouldReturnAllConceptModifiedOrCreatedAfterTheSpecifiedDate() throws Exception {
		Assert.assertEquals(22,
		    dao.getConceptsToExport(new SimpleDateFormat(MetadataSharingConsts.DATE_FORMAT).parse("2008-08-15 00:00:00"))
		            .size());
	}
}
