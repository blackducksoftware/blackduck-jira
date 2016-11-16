/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.config;

import java.util.Comparator;

public class IdToNameMappingByNameComparator implements Comparator<IdToNameMapping> {

    @Override
    public int compare(IdToNameMapping o1, IdToNameMapping o2) {
        if (o1 == o2) {
            return 0;
        }
        if ((o1 == null) || (o1.getName() == null)) {
            return -1;
        }
        if ((o2 == null) || (o2.getName() == null)) {
            return 1;
        }
        return o1.getName().compareTo(o2.getName());
    }

}
