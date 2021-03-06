import React from "react";
import {inject, observer} from "mobx-react";
import ResourcesPane from "../ResourcesPane";
import HighchartReact from "../charts/highcharts/HighchartReact";
import ResourceUsage from "./ResourceUsage";
import ResourceError from "./ResourceError";


@inject("metricsStore", "stateStore") @observer
export default class Resources extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Resources";
        this.props.stateStore.pageDescription = "Resources metrics";
        this.props.stateStore.headerIcon = "icon-exchange";

        this.props.metricsStore.fetchResources();
        this.props.metricsStore.fetchResourceMetricsStatus();
    }

    usagePercentage(resources, totalRequest) {
        const usageData = resources.map((resource) => {
            return {name: resource.url, y: (resource.metrics.totalRequests / totalRequest) * 100};
        });

        return {
            title: {
                text: 'Resource usage'
            },
            series: [{
                name: 'Usage',
                colorByPoint: true,
                data: usageData
            }]
        };
    }


    getTotalRequests(resourceSummaries) {
        return resourceSummaries.map((resource) => {
            return resource.metrics.totalRequests;
        }).reduce((prev, current) => {
            return prev + current;
        }, 0);
    }

    getGroupedResources(resourceSummaries) {
        return resourceSummaries.map((res) => {
            let baseUrl = res.url.split("/")[1];
            return {base: baseUrl, resource: res};
        }).reduce((prev, curr) => {
            if (!prev[curr.base]) {
                prev[curr.base] = [];
            }
            prev[curr.base].push(curr.resource);
            return prev;
        }, {});
    }

    buildGroupedResourceGroupPanel(resourceSummaries) {
        const resources = this.getGroupedResources(resourceSummaries);
        const size = Object.keys(resources).length;
        if (size === 0) {
            return <div></div>
        }

        let items = [];
        Object.keys(resources).map((key) => {
            items.push(
                <div key={'resource_pane_' + key} class="row">
                    <div class="col-md-6 col-md-offset-3">
                        <ResourcesPane title={key} resources={resources[key]}/>
                    </div>
                </div>
            );
        });
        return items;
    }

    setResourceMetricsEnabled(e, enabled) {
        e.preventDefault();
        this.props.metricsStore.setResourceMetricEnabled(enabled);
    }

    getEnableButton() {
        const {resourcesEnabled} = this.props.metricsStore;

        const btn = resourcesEnabled ?
            <a href="#" onClick={(e) => this.setResourceMetricsEnabled(e, false)} class="btn btn-red">Disable</a> :
            <a href="#" onClick={(e) => this.setResourceMetricsEnabled(e, true)} class="btn btn-green">Enable</a>;

        return (
            <div class="row">
                <div class="col-md-12">
                    <div class="padded pull-right">
                        {btn}
                    </div>
                </div>
            </div>
        )

    }

    render() {
        const {resources, resourcesEnabled} = this.props.metricsStore;
        const enableButton = this.getEnableButton();

        if (!resources || resources.length === 0) {
            return (
                <div>
                    {enableButton}
                    <h3 class="padded">No data</h3>
                </div>
        )
        }

        // let totalRequest = this.getTotalRequests(resources);

        // const usageData = this.usagePercentage(resources, totalRequest);
        // const responseStatusesData = this.responseStatuses(resources, totalRequest);
        // const errorPercent = this.errorPercentage(resources);

        const groupedResources = this.buildGroupedResourceGroupPanel(resources);

        // const resourceGraphs = resources.map((res, idx) => {
        //     return <ResourceGraphs key={'resource_graph_' + idx} resource={res}/>
        // });

        return (
            <div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="pull-right">
                            {this.getEnableButton()}
                        </div>
                    </div>
                </div>

                {groupedResources}

                <div class="row">
                    <div class="col-md-12">
                        <div class="box">
                            <div class="box-header">
                                <span class="title">Usage</span>
                            </div>
                            {resourcesEnabled ? <ResourceUsage resources={resources}/> : <h3 class="text-center padded">Disabled</h3>}
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="box">
                            <div class="box-header">
                                <span class="title">Errors per endpoint</span>
                            </div>
                            {resourcesEnabled ? <ResourceError resources={resources}/> : <h3 class="text-center padded">Disabled</h3>}

                        </div>
                    </div>
                </div>

                {/*{resourceGraphs}*/}

            </div>
        )
    }
}

class ResourceGraphs extends React.Component {
    render() {
        const {resource} = this.props;
        return (
            <div class="row">
                <div class="col-md-12">
                    <div class="box">
                        <div class="box-header">
                            <span class="title">{resource.method} - {resource.url}</span>
                        </div>
                        <div class="row padded">
                            <div class="col-md-6">
                                <div class="box">
                                    <div class="box-header">
                                        <span class="title">Response Statuses</span>
                                    </div>
                                    <ResponseCodeGraph resource={resource}/>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="box ">
                                    <div class="box-header">
                                        <span class="title">Request processing time</span>
                                    </div>
                                    <ResponseTimeGraph resource={resource}/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

class ResponseTimeGraph extends React.Component {

    getGraphOptions(resource) {
        let avgReqTimes = resource.metrics.map((metric) => {
            let avg = metric.data.totalRequestTime / metric.data.totalRequests;
            avg = parseFloat(avg.toFixed(2));
            return [metric.timestamp, avg];
        });

        return {
            title: {
                text: 'Request processing time'
            },
            yAxis: {
                labels: {
                    formatter: function () {
                        return this.value;
                    }
                },
                allowDecimals: false
            },
            xAxis: {
                type: 'datetime',
            },

            plotOptions: {
                series: {
                    // compare: 'percent',
                    showInNavigator: true
                }
            },
            series: [{
                name: 'Avg. Response time',
                color: 'black',
                data: avgReqTimes
            }]
        }
    }

    render() {
        const {resource} = this.props;
        const options = this.getGraphOptions(resource);
        return (
            <div>
                <HighchartReact container={resource.id + '_avgTime'} options={options}/>
            </div>
        )
    }
}

class ResponseCodeGraph extends React.Component {

    getResponseByStatus(metric, filter) {
        let responses = metric.data.responses;
        let sum = 0;
        for (let key in responses) {
            let code = parseInt(key);
            if (filter(code)) {
                sum += responses[key];
            }
        }
        return [metric.timestamp, sum];
    }

    getGraphOptions(resource) {
        let requests = resource.metrics.map((metric) => {
            return [metric.timestamp, metric.data.totalRequests];
        });

        const errors = resource.metrics.map((metric) => {
            return this.getResponseByStatus(metric, (status) => {
                return (status < 200 || status > 299);
            })
        });

        const success = resource.metrics.map((metric) => {
            return this.getResponseByStatus(metric, (status) => {
                return (status >= 200 && status < 300);
            })
        });


        return {
            title: {
                text: 'Response code'
            },
            yAxis: {
                labels: {
                    formatter: function () {
                        return this.value;
                    }
                },
                allowDecimals: false
            },
            xAxis: {
                type: 'datetime',
            },

            plotOptions: {
                series: {
                    // compare: 'percent',
                    showInNavigator: true
                }
            },
            series: [{
                name: 'Requests',
                color: 'blue',
                data: requests
            }, {
                name: 'Errors',
                color: 'red',
                data: errors
            }, {
                name: 'Success',
                color: 'green',
                data: success
            }]
        }
    }

    render() {
        const {resource} = this.props;
        const options = this.getGraphOptions(resource);
        return (
            <div>
                <HighchartReact container={resource.id + '_responseCodes'} options={options}/>
            </div>
        )
    }
}