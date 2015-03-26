## Code Status

[![Build Status](https://travis-ci.org/scheerer/stash-merge-rules-plugin.svg?branch=master)](https://travis-ci.org/scheerer/stash-merge-rules-plugin)

## License

Released under the [GPLv3](http://opensource.org/licenses/GPL-3.0).

## Atlassian Stash Extra Merge Rules

An Atlassian Stash plugin for preventing pull requests from being merged without meeting some criteria.

- Optionally checks the build status for source and/or target branches. Only allowing branches with a successful build for the latest commit.
- Optionally requiring title being editing to include the text [DONE].
- Allowing rules to be overridden via a configurable bypass keyword in the pull request title.
- Allows for configuration of HipChat to send notifications to a specified room to alert people of a pull request being merged via the bypass keyword.


Here are the SDK commands you'll use immediately:

* atlas-run   -- installs this plugin into the product and starts it on localhost
* atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
* atlas-cli   -- after atlas-run or atlas-debug, opens a Maven command line window:
                 - 'pi' reinstalls the plugin into the running product instance
* atlas-help  -- prints description for all commands in the SDK

Full documentation is always available at:

https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK
