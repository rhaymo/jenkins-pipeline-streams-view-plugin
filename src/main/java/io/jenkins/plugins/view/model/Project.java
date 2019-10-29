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

import com.google.common.collect.ImmutableList;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExportedBean(defaultVisibility = 100)
public class Project {
    private final List<PipelineExecution> pipelines;
    private final WorkflowJob workflowJob;
    private Map<String, Map<String,String>> columnHeaders;

    public Project(String name, WorkflowJob job, List<PipelineExecution> pipelines, Map<String, Map<String,String>> columnHeaders) {
        //super(name);
        this.workflowJob = job;
        if (pipelines != null) {
            this.pipelines = ImmutableList.copyOf(pipelines);
        } else {
            this.pipelines = Collections.emptyList();
        }
        this.columnHeaders = columnHeaders;
    }

    @Exported
    public boolean isWorkflowComponent() {
        return true;
    }

    @Exported
    public String getWorkflowUrl() {
        return workflowJob.getUrl();
    }

    @Exported
    public WorkflowJob getWorkflowJob() {
        return workflowJob;
    }

    @Exported
    public String getFullJobName() {
        return workflowJob.getFullName();
    }

    @Exported
    public List<PipelineExecution> getPipelines() {
        return pipelines;
    }

    @Exported
    public Map<String, Map<String, String>> getColumnHeaders() {
        return columnHeaders;
    }

    public void setColumnHeaders(Map<String, Map<String, String>> columnHeaders) {
        this.columnHeaders = columnHeaders;
    }
}