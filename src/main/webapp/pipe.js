const StatusMapping = {
    SUCCESS: 'SUCCESS',
    FAILURE: 'FAILURE',
    UNSTABLE: 'UNSTABLE',    
    NOT_BUILT: 'NOT_BUILT',
    ABORT: 'ABORT',
    BUILDING: 'BUILDING',
    PENDING: 'PENDING'
}
let lastUpdate;


function drawArrow() {
    const td = '<td class="next">' +
        '<span class="status next">' +
        '<img src="/jenkins/images/24x24/next.png">' +
        '</img>' +
        '</span>' +
        '</td>';

    return td;
}

function  rerunBuild(urlJob) {
    Q.ajax({
        url: rootURL + '/' + urlJob + 'replay/run',
        type: 'POST',
        //beforeSend: before,
        timeout: 20000,
        success: function (data, textStatus, jqXHR) {
            console.info('Triggered build of ' + taskId + ' successfully!')
        },
        error: function (jqXHR, textStatus, errorThrown) {
            window.alert('Could not trigger build! error: ' + errorThrown + ' status: ' + textStatus)
        }
    });
}    

function drawCellData(cell, data) {
    let status = StatusMapping[cell.status];
    
    let dateFormat =  formatAbsoluteDate(cell.startTimeMillis, false);
    if (data.showAbsoluteDateTime){
        dateFormat =  formatAbsoluteDate(cell.startTimeMillis, true);
    }
    
    let buildInfo = '<div class="build-info"><ul>' +        
        `<li class="build-time" title="Start date: ${dateFormat}">${dateFormat}</li>` +
        `<li class="build-duration" title="Build duration: ${cell.duration} ms">${cell.duration} ms</li>` +        
        '</ul></div>' +
        `<div class="build-actions"><div class="status-bar"></div><div class="icons">`;
    if (data.linkToConsoleLog)
        buildInfo +=`<a href="${rootURL}/${cell.url}console"><img src="/jenkins/images/16x16/terminal.png" alt="console" title="console"></a>`;

    if (data.allowPipelineStart)
        //buildInfo +=`<a href="${rootURL}/${cell.url}replay/run"><img src="/jenkins/images/16x16/redo.png" alt="re-run" title="re-run"></a>`;
        buildInfo += `<span onclick="rerunBuild('${cell.url}')">`+
                        `<img title="re-run" alt="re-run" src="${rootURL}/images/16x16/redo.png" />`+
                    `</span>`;
    
    buildInfo +='</div>';

    const td = '<td id="">' +
        `<div class="build-card rounded ${status}">` +
        '<div class="header">' +
        `<a href="${rootURL}/${cell.url}" title="${cell.parent}">${cell.parent} ${cell.displayName}</a>` +
        '</div>' +
        buildInfo +
        '</div>' +
        '</td>';

    return td;
}

function drawRowHeader(pipeline) {
    let envVars = '';
    Object.keys(pipeline.envVars).forEach(key => {
        envVars += `<li class="rowHeaderEnv" title="${pipeline.envVars[key]}">${key}:<br/>${pipeline.envVars[key].substr(0,18)}</li>`;
    });
    const div = '<div class="pipeline-info">' +
        '<div class="revision rounded">' +
        `<div class="title">Pipeline ${pipeline.runs[0][0].displayName}</div>` +
        `<div class="description"><ul>` +
        envVars +
        `</ul></div>` +
        '</div>' +
        '</div>';    
    return div;
}

function fillRow(x,data) {    
    let html = [];
    for (let i=(x+1)/2;i< Object.keys(data.projects[0].pipelines[0].runs[0]).length; i++) {
        html.push('<td class="next"></td><td><div class="build-card rounded ABORT"></div></td>');
        //html.push('<td></td>');     
    }
    return html;
}

function renderPipelineExecution(pipeline, data) {
    let html = [];
    const matrix = pipeline.runs;

    html.push('<div class="pipeline-wrapper">');
    if (matrix.isEmpty) return html;

    html.push(drawRowHeader(pipeline));

    html.push('<div class="pipeline">');

    html.push('<table class="pipelines">');
    html.push('<tbody>');
    let x = 0, y = 0;
    matrix.forEach(row => {
        x = 0;
        html.push('<tr>');
        row.forEach(cell => {
            if (cell && cell.arrow) {
                html.push(drawArrow())
                x++;
            }
            if (cell && cell.displayName) {
                html.push(drawCellData(cell, data))
                x++;
            }
            if (cell == null) {
                if (x == 0) {
                    html.push('<td></td>');
                } else {
                    html.push('<td class="next"></td><td></td>');
                }
                x++;
            }
        })
        if (y==0)
            html = html.concat(fillRow(x,data));
        y++;        
        html.push('</tr>');
    });
    html.push('</tbody>');
    html.push('</table>');
    html.push('</div>');
    html.push('</div>');
    return html;
}

function drawColumnDataHeader(project) {
    let html = [];
    const pipeline = project.pipelines[0];
    const matrix = pipeline.runs;

    html.push('<div class="pipeline-wrapper header">');
    html.push('<div class="pipeline-info"></div>');
    html.push('<div class="pipeline">');

    html.push('<table class="pipelines">');
    html.push('<tbody>');
    let x = 0, y = 0;
    matrix.forEach(row => {
        html.push('<tr class="project-pipeline">');
        x = 0;
        row.forEach(cell => {
            if (cell && cell.arrow) {
                html.push(drawArrow())
                x++;
            }
            if (cell && cell.displayName) {
                html.push('<td id="">');
                html.push('<div class="build-card rounded">');
                html.push('<div class="header">');
                html.push(`<a href="" title="${cell.parent}">${cell.parent}</a>`);
                html.push('</div>');
                html.push('</div>');
                html.push('</td>');
                x++;
            }
            if (cell == null) {
                if (x == 0) {
                    html.push('<td></td>');
                } else {
                    html.push('<td class="next"></td><td></td>');
                }
                x++;
            }
        })
        y++;
        html.push('</tr>');
    });
    html.push('</tbody>');
    html.push('</table>');
    html.push('</div>');
    html.push('</div>');
    return html;
}

function drawColumnDataHeader1(project) {
    let html = [];
    const pipeline = project.pipelines[0];
    const matrix = pipeline.runs;

    html.push('<div class="pipeline-wrapper header">');
    html.push('<div class="pipeline-info"></div>');
    html.push('<div class="pipeline">');

    html.push('<table class="pipelines">');
    html.push('<tbody>');
    let x = 0, y = 0;
    if (Object.keys(project.columnHeaders).length>0) {
        matrix.forEach(row => {
            html.push('<tr class="project-pipeline">');
            x = 0;
            row.forEach(cell => {
                if (cell && cell.arrow) {
                    html.push(drawArrow())
                    x++;
                }
                if (cell && cell.displayName) {
                    let envVars='';           
                    if (project.columnHeaders[cell.parent]) {
                        Object.keys(project.columnHeaders[cell.parent]).forEach(key => {
                            if (key !== 'title')
                                envVars += `<li>${key}:${project.columnHeaders[cell.parent][key]}</li>`;
                        });
                    }
                    html.push('<td id="">');
                    html.push('<div class="build-card rounded">');
                    html.push('<div class="header">');
                    html.push(`<a href="${rootURL}/job/${cell.parent}/" title="${cell.parent}">${cell.parent}</a>`);                
                    html.push('</div>');
                    if (envVars) {
                        html.push(`<div class="build-info"><ul>` + envVars + `</ul></div>`); 
                    }
                    html.push('</div>');
                    html.push('</td>');
                    x++;
                }
                if (cell == null) {
                    if (x == 0) {
                        html.push('<td></td>');
                    } else {
                        html.push('<td class="next"></td><td></td>');
                    }
                    x++;
                }
            })
            if (y==0)
                html = html.concat(fillRow(x,{projects: [project]}));
            y++;
            html.push('</tr>');
        });
    }
    html.push('</tbody>');
    html.push('</table>');
    html.push('</div>');
    html.push('</div>');
    return html;
}

function renderProject(project, data) {
    let html = [];

    html.push('<div id="build-pipeline-plugin-content">');

    if (project.pipelines.length === 0) {
        html.push('No builds done yet.');
    }

    html = html.concat(drawColumnDataHeader1(project));

    for (var i = 0; i < project.pipelines.length; i++) {
        html = html.concat(renderPipelineExecution(project.pipelines[i], data));
    }
    return html;
}

function pipelineUtils() {
    var self = this;
    this.updatePipelines = function (divNames, errorDiv, view, fullscreen, page, component, showChanges, aggregatedChangesGroupingPattern, timeout, pipelineid, jsplumb) {

        // JENKINS-46160 Don't refresh pipelines if the tab/window is not active
        if (document.hidden) {
            setTimeout(function () {
                self.updatePipelines(divNames, errorDiv, view, fullscreen, page, component, showChanges, aggregatedChangesGroupingPattern, timeout, pipelineid, jsplumb);
            }, timeout);
            return;
        }

        Q.ajax({
            url: rootURL + '/' + view.viewUrl + 'api/json' + '?page=' + page + '&component=' + component + '&fullscreen=' + fullscreen,
            dataType: 'json',
            async: true,
            cache: false,
            timeout: 20000,
            success: function (data) {
                self.refreshPipelines(data, divNames, errorDiv, view, fullscreen, showChanges, aggregatedChangesGroupingPattern, pipelineid, jsplumb);
                setTimeout(function () {
                    self.updatePipelines(divNames, errorDiv, view, fullscreen, page, component, showChanges, aggregatedChangesGroupingPattern, timeout, pipelineid, jsplumb);
                }, timeout);
            },
            error: function (xhr, status, error) {
                Q('#' + errorDiv).html('Error communicating to server! ' + htmlEncode(error)).show();
               // jsplumb.repaintEverything();
                setTimeout(function () {
                    self.updatePipelines(divNames, errorDiv, view, fullscreen, page, component, showChanges, aggregatedChangesGroupingPattern, timeout, pipelineid, jsplumb);
                }, timeout);
            }
        });
    }

    var lastResponse = null;

    this.refreshPipelines = function (data, divNames, errorDiv, view, showAvatars, showChanges, aggregatedChangesGroupingPattern, pipelineid, jsplumb) {
         lastUpdate = data.lastUpdated;
        var showAbsoluteDateTime = data.showAbsoluteDateTime;

        displayErrorIfAvailable(data, errorDiv);

        if (lastResponse === null || JSON.stringify(data.projects) !== JSON.stringify(lastResponse.projects)) {
            for (var z = 0; z < divNames.length; z++) {
                Q('#' + divNames[z]).html('');
            }

            if (!data.projects || data.projects.length === 0) {
                Q('#pipeline-message-' + pipelineid).html('No pipelines configured or found. Please review the <a href="configure">configuration</a>')
            }

            //jsplumb.reset();

            for (var c = 0; c < data.projects.length; c++) {
                let html = renderProject(data.projects[c], data);
                Q('#' + divNames[c % divNames.length]).append(html.join(''));
                Q('#pipeline-message-' + pipelineid).html('');
            }
        } else {
           /* var proj;
            var pipe;
            var head;
            var st;
            var ta;
            var time;

            for (var p = 0; p < data.projects.length; p++) {
                proj = data.projects[p];
                for (var d = 0; d < proj.pipelines.length; d++) {
                    pipe = proj.pipelines[d];
                    head = document.getElementById(pipe.id);
                    if (head) {
                        head.innerHTML = formatDate(pipe.timestamp, lastUpdate, showAbsoluteDateTime)
                    }

                    for (var l = 0; l < pipe.stages.length; l++) {
                        st = pipe.stages[l];
                        for (var m = 0; m < st.tasks.length; m++) {
                            ta = st.tasks[m];
                            time = document.getElementById(getTaskId(ta.id, d) + '.timestamp');
                            if (time) {
                                time.innerHTML = formatDate(ta.status.timestamp, lastUpdate, showAbsoluteDateTime);
                            }
                        }
                    }
                }
            }*/
        }
        //jsplumb.repaintEverything();

    }
}



/*
function div(class, id, inner) {
return `<div class="${class}">${inner}</div>`;
}*/
/*
function TableRow(class, id) {
    const column = [];

    addColumn() {

    }

    getColumn() {

    }

    build() {

    }

    return{
        addColumn,
        getColumn,
        build
    }
}
function Table(class, id) {
    const rows = [];

    addRow(row) {
        rows.push(row);
    }

    getRow(index) {
        return rows[i];
    }

    build() {

    }

    return {
        addRow,
        getRow,
        build
    }

}
*/
function displayErrorIfAvailable(data, errrorDivId) {
    var cErrorDiv = Q('#' + errrorDivId);
    if (data.error) {
        cErrorDiv.html('Error: ' + data.error).show();
    } else {
        cErrorDiv.hide().html('');
    }
}

function formatAbsoluteDate(date, showAbsoluteDateTime) {
    if (showAbsoluteDateTime) {
        return date !== null ? moment(date).format('YYYY-MM-DD HH:mm:ss') : '';
    } else {
        return date !== null ? moment(date).from(moment()) : '';
    }
}

function formatDuration(millis) {
    if (millis === 0) {
        return '0 sec';
    }

    var seconds = Math.floor(millis / 1000);
    var minutes = Math.floor(seconds / 60);
    var minstr;
    var secstr;

    seconds = seconds % 60;
    minstr = minutes === 0 ? '' : minutes + ' min ';
    secstr = '' + seconds + ' sec';

    return minstr + secstr;
}

function htmlEncode(html) {
    return document.createElement('a')
        .appendChild(document.createTextNode(html))
        .parentNode.innerHTML
        .replace(/\n/g, '<br/>');
}
