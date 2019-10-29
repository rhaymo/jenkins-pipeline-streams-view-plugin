Pipelines Stream View
=====================

The purpose of the Pipelines Stream View plugin is to provide visualisation of downstream/upstream relations of Jenkins Pipeline jobs (that's defined using a Jenkinsfile). The plugin is perfect for Continuous Delivery pipeline visualisation on information radiators.

Why another view plugin?
---
yet-another-build-visualizer-plugin: It shows the upstream/downstream relation for a single job execution. You have to navigate to the job execution page to see the relations of that job build.

delivery-pipeline-plugin: It shows the upstream/downstream relation for Jenkins freestyle projects. For Jenkins Pipeline projects, you can only view the relations between the stages in the Jenkins pipeline project, but not between Jenkins Pipeline projects.

build-pipeline-plugin: It shows the upstream/downstream relation for Jenkins freestyle projects. Jenkins Pipeline projects are not supported.

pipeline-stream-view: if you have a jenkins Pipeline for build, another Pipeline for deploy to Qa, another Pipeline for deploy to pre-prod and another for prod, you can view the upstream/downstream relations of the last N execution in a single view.

## Screenshots

![Screenshot](https://github.com/rhaymo/jenkins-pipeline-streams-view-plugin/blob/master/static/pipeline-streams-view.png)

Requirements
---
Pipelines Stream View plugin 1.0.0 and later requires Java 8 and Jenkins core 2.164 or later.

Usage
---
After installing the plugin, create a new view, selecting "Pipeline Streams View". Select the starting Jenkins Pipeline, the number of execution to show, and the column and row header.


Building the Project
--------------------
Requires Java 11, Apache Maven 3.3.x or later.

    mvn clean install

Run locally
---
During development you can easily start a local Jenkins instance with the Pipeline Streams View plugin installed based on your current source code revision.
Build the project using the step mentioned above and run:

    mvn hpi:run

This will start a local Jenkins with the Pipeline Streams View plugin installed. It will by default be available at http://localhost:8080/jenkins.


Create Jenkins plugin artifact
---
To create a Jenkins plugin artifact, build the project and run:

    mvn package

This creates a pipeline-streams-view.hpi file in the target directory.
This file can be manually uploaded through the Jenkins plugin management console (under the Advanced tab) to load the built plugin into Jenkins.
