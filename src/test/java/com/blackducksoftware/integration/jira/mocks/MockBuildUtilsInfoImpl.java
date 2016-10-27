package com.blackducksoftware.integration.jira.mocks;

import com.atlassian.jira.util.BuildUtilsInfoImpl;

public class MockBuildUtilsInfoImpl extends BuildUtilsInfoImpl {

    private int[] versionNumbers;

    private String version;

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int[] getVersionNumbers() {
        return versionNumbers;
    }

    public void setVersionNumbers(final int[] versionNumbers) {
        this.versionNumbers = versionNumbers;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

}
