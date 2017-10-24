# GemFire Function example to join two values from different regions and store the result

This is an example to join two values from different regions if each keys are same and store the result to other region.

## Target GemFire version
9.0 or later based on Apache Geode

## Usage
* modify cache.xml files and gemfire.properties file according to your environment.
* start a locator with using the script - startLocator.sh
* start two cache servers with using the sctipts - startServer1.sh and startServer2.sh: need to modify --classpath to refer to compiled classes (especially for Function class) according to your environment
* execute FunctionClient to initiate a function to join two values from two regions and get actual results for confirmation.

## Explanation for each source code
* FunctionClient.java - initiate the Function to execute join
* JoinRegionsFunction.java - server side Function logic to execute join values from different two regions and store the result to other region
* TestQueryClient.java - just test logic to test query without calling Function