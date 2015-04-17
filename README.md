# AppDynamics Apache Log Monitoring Extension


##Use Case
TODO:


##Installation

1. Run 'mvn clean install' from apache-log-monitoring-extension directory
2. Copy and unzip ApacheLogMonitor-\<version\>.zip from 'target' directory into \<machine_agent_dir\>/monitors/
3. Edit config.yaml file in ApacheLogMonitor/conf and provide the required configuration (see Configuration section)
4. Restart the Machine Agent.

##Configuration

###config.yaml

**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

TODO:


##Metrics
Metric path is typically: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Apache Log Monitor|** followed by the individual categories/metrics below:

##Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/apache-log-monitoring-extension).

##Community

Find out more in the [AppSphere] community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).

