import React from "react";
import {inject, observer} from "mobx-react";
import ResourcesPane from "../ResourcesPane";
import Pie from "../charts/highcharts/Pie";


@inject("metricsStore", "stateStore") @observer
export default class Resources extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Resources";
        this.props.stateStore.pageDescription = "Resources metrics";
        this.props.stateStore.headerIcon = "icon-exchange";
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

        const {metrics} = this.props.metricsStore;
        if (!metrics ||
            !metrics.resources ||
            metrics.resources.length === 0 ||
            !metrics.resources[0].metrics ||
            !metrics.resources[0].metrics.metricsStartDate) {

            return <h3>No data</h3>
        }

        let totalRequest = metrics.resources.map((resource) => {
            return resource.metrics.totalRequests;
        }).reduce((prev, current) => {
            return prev + current;
        }, 0);

        const usageData = this.usagePercentage(metrics.resources, totalRequest);
        const responseStatusesData = this.responseStatuses(metrics.resources, totalRequest);
        const errorPercent = this.errorPercentage(metrics.resources);


        return (
            <div>
                <div class="row">
                    <div class="col-md-6">
                        <ResourcesPane resources={metrics.resources}/>
                    </div>
                    <div class="col-md-6">

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
            </div>
        )
    }

}

