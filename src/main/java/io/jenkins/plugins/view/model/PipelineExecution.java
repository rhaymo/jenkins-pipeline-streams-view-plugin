/*
This file is part of Delivery Pipeline Plugin.

Delivery Pipeline Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Delivery Pipeline Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Delivery Pipeline Plugin.
If not, see <http://www.gnu.org/licenses/>.
*/
package io.jenkins.plugins.view.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
//import se.diabol.jenkins.core.TimestampFormat;
@ExportedBean(defaultVisibility = 100)
public class PipelineExecution  {

    
    public static class TriggerCause {}
    public static class UserInfo {}
    public static class Change {}
    public static class Task {}

    //private List<MyRun> stages;

    //private String version;

    private List<TriggerCause> triggeredBy;

    private Set<UserInfo> contributors;

    private String timestamp;

    private List<Change> changes;

    private long totalBuildTime;

    List<List<MyRun>> runs;

    Map<String,String> envVars;

    
    public PipelineExecution(String name, List<Change> changes,
                    List<TriggerCause> triggeredBy, String timestamp, List<List<MyRun>> runs, Map<String,String> envVars) {

        this.changes = changes;
        this.triggeredBy = triggeredBy;
        this.timestamp = timestamp;
        this.runs = runs;
        this.envVars = envVars;      
        this.contributors = null;
    }

    @Exported
    public String getTimestamp() {
        return timestamp;
    }

    @Exported
    public Set<UserInfo> getContributors() {
        return contributors;
    }

    @Exported
    public int getId() {
        return hashCode();
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    @Exported
    public List<Change> getChanges() {
        return changes;
    }

    @Exported
    public long getTotalBuildTime() {
        return totalBuildTime;
    }

    @Exported
    public List<TriggerCause> getTriggeredBy() {
        return triggeredBy;
    }

    @Exported
    public List<List<MyRun>> getRuns() {
        return runs;
    }

    public void setRuns(List<List<MyRun>> runs) {
        this.runs = runs;
    }   

    @Exported
    public Map<String,String>  getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map<String,String>  envVars) {
        this.envVars = envVars;
    }

}