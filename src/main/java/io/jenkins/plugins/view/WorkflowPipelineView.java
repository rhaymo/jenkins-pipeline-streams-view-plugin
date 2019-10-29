package io.jenkins.plugins.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import com.google.common.collect.Sets;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Api;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.model.ViewGroup;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.view.extension.PipelineHeaderExtension;
import io.jenkins.plugins.view.extension.PipelineHeaderExtensionDescriptor;
import io.jenkins.plugins.view.model.MyRun;
import io.jenkins.plugins.view.model.PipelineExecution;
import io.jenkins.plugins.view.model.Project;
import io.jenkins.plugins.view.util.BuildFlowAction;
import io.jenkins.plugins.view.util.JenkinsUtil;
import io.jenkins.plugins.view.util.Matrix;
import io.jenkins.plugins.view.util.PipelineException;
import io.jenkins.plugins.view.util.ProjectUtil;
import io.jenkins.plugins.view.util.Matrix.Entry;
import jenkins.model.Jenkins;

public class WorkflowPipelineView extends View {

    private static final Logger LOG = Logger.getLogger(WorkflowPipelineView.class.getName());

    public static final int DEFAULT_INTERVAL = 2;

    public static final int DEFAULT_NO_OF_PIPELINES = 3;
    private static final int MAX_NO_OF_PIPELINES = 50;
    private static final String NONE_SORTER = "none";

    private int updateInterval = DEFAULT_INTERVAL;
    private int noOfPipelines = DEFAULT_NO_OF_PIPELINES;
    private int noOfColumns = 1;
    // private String sorting = NONE_SORTER;
    private boolean allowPipelineStart = false;
    private boolean allowAbort = false;
    private boolean showChanges = false;
    private boolean showAbsoluteDateTime = false;
    private int maxNumberOfVisiblePipelines = -1;
    private List<ComponentSpec> componentSpecs;
    private boolean linkToConsoleLog = false;
    private String description = null;

    /** URL for custom CSS file */
    private String cssUrl = "";

    private transient String error;

    /**
     * What to show as a row header for pipelines
     */
    private PipelineHeaderExtension rowHeaders;

    /**
     * What to show as a column headers for jobs in the pipeline
     */
    private PipelineHeaderExtension columnHeaders;

    @DataBoundConstructor
    public WorkflowPipelineView(String name) {
        super(name);
    }

    public WorkflowPipelineView(String name, ViewGroup owner) {
        super(name, owner);
    }

    public int getNoOfColumns() {
        return noOfColumns;
    }

    public void setNoOfColumns(int noOfColumns) {
        this.noOfColumns = noOfColumns;
    }

    public String getCssUrl() {
        return cssUrl;
    }

    public void setCssUrl(final String cssUrl) {
        this.cssUrl = cssUrl;
    }

    public int getUpdateInterval() {
        // This occurs when the plugin has been updated and as long as the view has not
        // been updated
        // Jenkins will set the default value to 0
        if (updateInterval == 0) {
            updateInterval = DEFAULT_INTERVAL;
        }

        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public int getNoOfPipelines() {
        return noOfPipelines;
    }

    public void setNoOfPipelines(int noOfPipelines) {
        this.noOfPipelines = noOfPipelines;
    }

    public PipelineHeaderExtension getColumnHeaders() {
        return columnHeaders;
    }

    public void setColumnHeaders(PipelineHeaderExtension columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public PipelineHeaderExtension getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(PipelineHeaderExtension rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    @Exported
    public boolean isAllowPipelineStart() {
        return allowPipelineStart;
    }

    public void setAllowPipelineStart(boolean allowPipelineStart) {
        this.allowPipelineStart = allowPipelineStart;
    }

    @Exported
    public boolean isAllowAbort() {
        return allowAbort;
    }

    public void setAllowAbort(boolean allowAbort) {
        this.allowAbort = allowAbort;
    }

    public boolean isShowChanges() {
        return showChanges;
    }

    public void setShowChanges(boolean showChanges) {
        this.showChanges = showChanges;
    }

    @Exported
    public boolean isShowAbsoluteDateTime() {
        return showAbsoluteDateTime;
    }

    public void setShowAbsoluteDateTime(boolean showAbsoluteDateTime) {
        this.showAbsoluteDateTime = showAbsoluteDateTime;
    }

    public int getMaxNumberOfVisiblePipelines() {
        return maxNumberOfVisiblePipelines;
    }

    public void setMaxNumberOfVisiblePipelines(int maxNumberOfVisiblePipelines) {
        this.maxNumberOfVisiblePipelines = maxNumberOfVisiblePipelines;
    }

    public List<ComponentSpec> getComponentSpecs() {
        if (componentSpecs == null) {
            componentSpecs = new ArrayList<>();
        }
        return componentSpecs;
    }

    public void setComponentSpecs(List<ComponentSpec> componentSpecs) {
        this.componentSpecs = componentSpecs;
    }

    @Exported
    public String getLastUpdated() {
        // return TimestampFormat.formatTimestamp(System.currentTimeMillis());
        return "";
    }

    @Exported
    public boolean isLinkToConsoleLog() {
        return linkToConsoleLog;
    }

    public void setLinkToConsoleLog(boolean linkToConsoleLog) {
        this.linkToConsoleLog = linkToConsoleLog;
    }

    @Exported
    @Override
    public String getDescription() {
        if (super.description == null) {
            setDescription(this.description);
        }
        return super.description;
    }

    public void setDescription(String description) {
        super.description = description;
        this.description = description;
    }

    @Exported
    public String getError() {
        return error;
    }

    @Override
    @Exported
    public String getViewUrl() {
        return super.getViewUrl();
    }

    @Override
    public Api getApi() {
        return new Api(this);
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        if (!isDefault()) {
            return getOwner().getPrimaryView().doCreateItem(req, rsp);
        } else {
            return jenkins().doCreateItem(req, rsp);
        }
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        Set<TopLevelItem> jobs = Sets.newHashSet();
        addJobsFromComponentSpecs(jobs);
        return jobs;
    }

    private void addJobsFromComponentSpecs(Set<TopLevelItem> jobs) {
        if (componentSpecs == null) {
            return;
        }
        for (ComponentSpec spec : componentSpecs) {
            try {
                WorkflowJob job = getWorkflowJob(spec.job);
                jobs.add(job);
            } catch (PipelineException e) {
                LOG.log(Level.SEVERE, "Failed to resolve WorkflowJob for configured job name: " + spec.job, e);
            }
        }
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return getItems().contains(item);
    }

    @Override
    public ItemGroup<? extends TopLevelItem> getOwnerItemGroup() {
        if (getOwner() == null) {
            return null;
        }
        return super.getOwnerItemGroup();
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
        req.bindJSON(this, req.getSubmittedForm());
        componentSpecs = req.bindJSONToList(ComponentSpec.class, req.getSubmittedForm().get("componentSpecs"));
    }

    private Map<String, Map<String, String>> getHeadersForRuns(WorkflowJob job) {
        Map<String, Map<String, String>> headersForRuns = new HashMap<>();
        if (job.getBuilds().size() > 0) {

            BuildFlowAction bfa = new BuildFlowAction(job.getBuilds().get(0));
            Matrix<Object> m = bfa.buildMatrix();
            m.get().forEach(row -> {
                row.forEach(cell -> {
                    Entry currentCell = (Entry) cell;
                    if (cell != null && cell.getData() != null) {
                        String key;
                        Map<String, String> value = null;
                        if (cell.getData() instanceof Run) {
                            Run currentCellData = (Run) cell.getData();
                            key = currentCellData.getParent().getDisplayName();
                            value = columnHeaders.getParameters(currentCellData);
                            // headersForRuns.put(currentCellData.getParent().getDisplayName(),
                            // columnHeaders.getParameters(currentCellData));
                        } else {
                            Queue.Item currentCellData = (Queue.Item) cell.getData();
                            key = currentCellData.getDisplayName();
                            // headersForRuns.put(currentCellData.getDisplayName(), null);
                        }
                        if (value != null && !value.isEmpty())
                            headersForRuns.put(key, value);
                    }
                });
            });
        }
        return headersForRuns;
    }

    @Exported
    public List<Project> getProjects() {
        try {
            LOG.fine("Getting pipelines");
            List<Project> projects = new ArrayList<>();
            for (ComponentSpec componentSpec : getComponentSpecs()) {
                WorkflowJob job = getWorkflowJob(componentSpec.job);
                List<PipelineExecution> pipelinesExecutions = resolvePipelinesExecutionsForJob(job);
                Project component = new Project(componentSpec.name, job, pipelinesExecutions, getHeadersForRuns(job));
                this.error = null;
                projects.add(component);
            }
            /*
             * if (sortingConfigured()) { sort(components); }
             */
            if (maxNumberOfVisiblePipelines > 0) {
                LOG.fine("Limiting number of jobs to: " + maxNumberOfVisiblePipelines);
                projects = projects.subList(0, Math.min(projects.size(), maxNumberOfVisiblePipelines));
            }
            return projects;
        } catch (PipelineException e) {
            error = e.getMessage();
            return Collections.emptyList();
        }
    }

    private List<PipelineExecution> resolvePipelinesExecutionsForJob(WorkflowJob job) throws PipelineException {
        List<PipelineExecution> pipelinesExecution = new ArrayList<>();
        if (job.getBuilds() == null) {
            return pipelinesExecution;
        }
        Iterator<WorkflowRun> it = job.getBuilds().iterator();
        for (int i = 0; i < noOfPipelines && it.hasNext(); i++) {
            WorkflowRun build = it.next();
            PipelineExecution pipeline = resolvePipelineExecutionFromJobBuild(job, build);
            pipelinesExecution.add(pipeline);
        }
        return pipelinesExecution;
    }

    private PipelineExecution resolvePipelineExecutionFromJobBuild(WorkflowJob job, WorkflowRun build)
            throws PipelineException {
        BuildFlowAction bfa = new BuildFlowAction(build);
        Matrix<Object> m = bfa.buildMatrix();
        List<List<MyRun>> rowList = convertMatrixToListOfLists(m);
        Map<String, String> envVars = rowHeaders.getParameters(build);
        return new PipelineExecution("test", null, null, null, rowList, envVars);
    }

    private List<List<MyRun>> convertMatrixToListOfLists(Matrix<Object> m) {
        List<List<MyRun>> rowList = new ArrayList<List<MyRun>>();
        m.get().forEach(row -> {
            List<MyRun> colList = new ArrayList<MyRun>();
            row.forEach(cell -> {
                Entry currentCell = (Entry) cell;
                if (cell != null && cell.getData() != null) {
                    if (cell.getData() instanceof Run) {
                        Run currentCellData = (Run) cell.getData();
                        colList.add(new MyRun(currentCellData.getDisplayName(),
                                currentCellData.getParent().getDisplayName(), currentCellData.getUrl(),
                                convertResultToStatus(currentCellData.getResult()), cell.getArrow(),
                                currentCellData.getDuration(), currentCellData.getStartTimeInMillis()));
                    } else {
                        Queue.Item currentCellData = (Queue.Item) cell.getData();
                        colList.add(new MyRun(null, currentCellData.getDisplayName(), currentCellData.getUrl(),
                                MyRun.Status.BUILDING, cell.getArrow(), 0, 0));
                    }
                } else {
                    colList.add(null);
                }
            });
            rowList.add(colList);
        });
        return rowList;
    }

    private MyRun.Status convertResultToStatus(Result result) {
        if (result != null)
            return MyRun.Status.values()[result.ordinal];
        else
            return MyRun.Status.BUILDING;

    }

    private WorkflowJob getWorkflowJob(final String projectName) throws PipelineException {
        WorkflowJob job = ProjectUtil.getWorkflowJob(projectName, getOwnerItemGroup());
       /* if (job == null) {
            throw new PipelineException("Failed to resolve job with name: " + projectName);
        }*/
        return job;
    }

    @Extension
    public static class DescriptorImpl extends ViewDescriptor {
        public ListBoxModel doFillNoOfColumnsItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            options.add("1", "1");
            options.add("2", "2");
            options.add("3", "3");
            return options;
        }

        public ListBoxModel doFillProjectsItems(@AncestorInPath ItemGroup<?> context) {
            return ProjectUtil.fillAllProjects(context, WorkflowJob.class);
        }

        public ListBoxModel doFillNoOfPipelinesItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            for (int i = 1; i <= MAX_NO_OF_PIPELINES; i++) {
                String opt = String.valueOf(i);
                options.add(opt, opt);
            }
            return options;
        }

        public ListBoxModel doFillSortingItems() {
            /*
             * DescriptorExtensionList<GenericComponentComparator,
             * ComponentComparatorDescriptor> descriptors =
             * GenericComponentComparator.all();
             */
            ListBoxModel options = new ListBoxModel();
            options.add("None", NONE_SORTER);
            /*
             * for (ComponentComparatorDescriptor descriptor : descriptors) {
             * options.add(descriptor.getDisplayName(), descriptor.getId()); }
             */
            return options;
        }

        public FormValidation doCheckUpdateInterval(@QueryParameter String value) {
            int valueAsInt;
            try {
                valueAsInt = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return FormValidation.error(e, "Value must be an integer");
            }
            if (valueAsInt <= 0) {
                return FormValidation.error("Value must be greater than 0");
            }
            return FormValidation.ok();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Delivery Pipeline View for Jenkins Pipelines";
        }

        /**
         * @param condition if true, return it as part of the returned list
         * @return a filtered and ordered list of descriptors matching the condition
         */
        public List<PipelineHeaderExtensionDescriptor> filter(
                Function<PipelineHeaderExtensionDescriptor, Boolean> condition) {
            final List<PipelineHeaderExtensionDescriptor> result = new ArrayList<PipelineHeaderExtensionDescriptor>();
            //final List<PipelineHeaderExtension> applicableExtensions = new ArrayList<PipelineHeaderExtension>();
            for (PipelineHeaderExtensionDescriptor descriptor : PipelineHeaderExtensionDescriptor.all()) {
                if (condition.apply(descriptor)) {
                    result.add(descriptor);
                }
            }
            Collections.sort(result);
            return result;
        }

        /**
         * @return a list of PipelineHeaderExtension descriptors which can be used as a
         *         row header
         */
        public List<PipelineHeaderExtensionDescriptor> getRowHeaderDescriptors() {
            return filter(new Function<PipelineHeaderExtensionDescriptor, Boolean>() {
                @Override
                public Boolean apply(PipelineHeaderExtensionDescriptor extension) {
                    return extension.appliesToRows();
                }
            });
        }

        /**
         * @return a list of PipelineHeaderExtension descriptors which can be used as
         *         column headers
         */
        public List<PipelineHeaderExtensionDescriptor> getColumnHeaderDescriptors() {
            return filter(new Function<PipelineHeaderExtensionDescriptor, Boolean>() {

                @Override
                public Boolean apply(PipelineHeaderExtensionDescriptor extension) {
                    return extension.appliesToColumns();
                }
            });
        }
    }

    public static class ComponentSpec extends AbstractDescribableImpl<ComponentSpec> {
        private String name;
        private String job;

        @DataBoundConstructor
        public ComponentSpec(String name, String job) {
            this.name = name;
            this.job = job;
        }

        public String getName() {
            return name;
        }

        public String getJob() {
            return job;
        }

        public void setJob(String job) {
            this.job = job;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<WorkflowPipelineView.ComponentSpec> {

            @Nonnull
            @Override
            public String getDisplayName() {
                return "";
            }

            public ListBoxModel doFillJobItems(@AncestorInPath ItemGroup<?> context) {
                return ProjectUtil.fillAllProjects(context, WorkflowJob.class);
            }

            public FormValidation doCheckName(@QueryParameter String value) {
                if (value != null && !"".equals(value.trim())) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Please supply a title");
                }
            }
        }
    }

    private static Jenkins jenkins() {
        return JenkinsUtil.getInstance();
    }

}