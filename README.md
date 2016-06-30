# hub-jira
Jira Hub Plugin

Here are the SDK commands you'll use immediately:

* atlas-run   -- installs this plugin into the product and starts it on localhost
* atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-cli   -- after atlas-run or atlas-debug, opens a Maven command line window:
                 - 'pi' reinstalls the plugin into the running product instance
* atlas-help  -- prints description for all commands in the SDK

Full documentation is always available at:

https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK


## Overview ##
Hub plugin for Atlassian Jira.  This plugin provides the ability to create Jira issues based on Policy violations in the Hub, and close them based on violation overrides.  Tickets will only be created for the Hub projects that are mapped to Jira projects and only for the violations that are selected.

## Where can I get the latest release? ##
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-jira. 

You can download the latest release artifacts from GitHub: https://github.com/blackducksoftware/hub-jira/releases

## Documentation ##
All documentation for hub-jira can be found on the base project:  https://github.com/blackducksoftware/hub-jira/wiki

## License ##
Apache License 2.0