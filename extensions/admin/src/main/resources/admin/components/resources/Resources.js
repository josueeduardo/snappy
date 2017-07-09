import React from "react";
import {inject, observer} from "mobx-react";
import ResourcesPane from "../ResourcesPane";
import Pie from "../charts/highcharts/Pie";
import HighchartReact from "../charts/highcharts/HighchartReact";


@inject("metricsStore", "stateStore") @observer
export default class Resources extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Resources";
        this.props.stateStore.pageDescription = "Resources metrics";
        this.props.stateStore.headerIcon = "icon-exchange";

        this.props.metricsStore.fetchResources();
        this.timer = setInterval(() => {
            this.props.metricsStore.fetchResources();
        }, 60000);

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

    responseStatuses(resources, totalRequest) {
        const vals = resources.map((resource) => {
            return resource.metrics.responses;
        }).reduce((prev, curr) => {
            for (let key in curr) {
                if (prev[key]) {
                    prev[key] += curr[key];
                } else {
                    prev[key] = curr[key];
                }
            }
            return Object.assign({}, prev);
        }, {});

        let data = [];
        for (let key in vals) {
            let percentage = (vals[key] / totalRequest) * 100;
            data.push({name: key, y: percentage})
        }
        return {
            title: {
                text: 'Response statuses'
            },
            series: [{
                name: 'Status code',
                colorByPoint: true,
                data: data
            }]
        };
    }

    errorPercentage(resources) {
        const errors = resources.map((resource) => {

            let responses = resource.metrics.responses;
            let numError = 0;
            for (let key in responses) {
                let code = parseInt(key);
                if (code < 200 || code > 299) {
                    numError += responses[key];
                }
            }

            return {name: resource.url, y: numError};

        });

        let totalErrors = errors.reduce((prev, curr) => {
            return prev + curr.y;
        }, 0);

        let errorPercentages = errors.map((error) => {
            error.y = (error.y / totalErrors) * 100;
            return error;
        });


        return {
            title: {
                text: 'Errors per endpoint'
            },
            series: [{
                name: 'Error code',
                colorByPoint: true,
                data: errorPercentages
            }]
        };
    }


    render() {

        const {resourceSummaries, resources} = this.props.metricsStore;
        if (!resourceSummaries || resourceSummaries.length === 0) {
            return <h3>No data</h3>
        }

        let totalRequest = resourceSummaries.map((resource) => {
            return resource.metrics.totalRequests;
        }).reduce((prev, current) => {
            return prev + current;
        }, 0);

        const usageData = this.usagePercentage(resourceSummaries, totalRequest);
        const responseStatusesData = this.responseStatuses(resourceSummaries, totalRequest);
        const errorPercent = this.errorPercentage(resourceSummaries);

        const resourceGraphs = resources.map((res) => {
            return <ResourceGraphs resource={res} />
        });

        return (
            <div>
                <div class="row">
                    <div class="col-md-6 col-md-offset-3">
                        <ResourcesPane resources={resourceSummaries}/>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-4">
                        <div class="box">
                            <div class="box-header">
                                <span class="title">Usage</span>
                            </div>
                            <Pie name={'usage'} data={usageData}/>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="box">
                            <div class="box-header">
                                <span class="title">Response Statuses</span>
                            </div>
                            <Pie name={'responseStatuses'} data={responseStatusesData}/>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="box">
                            <div class="box-header">
                                <span class="title">Errors per endpoint</span>
                            </div>
                            <div class="box-content">
                                <Pie name={'statuses'} data={errorPercent}/>
                            </div>
                        </div>
                    </div>
                </div>

                {resourceGraphs}

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