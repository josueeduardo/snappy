import React from "react";
import {inject, observer} from "mobx-react";
import HighchartReact from "../charts/highcharts/HighchartReact";


@inject("metricsStore", "stateStore") @observer
export default class ResourceUsage extends React.Component {

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

    getTotalErrors(resources) {
        return resources.reduce((prev, curr) => {
            let errors = this.getErrorsForEndpoint(curr);
            return errors + prev;
        }, 0);
    }

    getErrorsForEndpoint(resource) {

        return Object.keys(resource.metrics.responses).map((key, index) => {
            let code = parseInt(key);
            return {code: code, count: resource.metrics.responses[key]}
        }).filter((v) => {
            return !(v.code >= 200 && v.code < 300 || v.code === 304);
        }).reduce((prev, curr) => {
            return prev + curr.count;
        }, 0);

    }


    getMainResources(resources, totalErrors) {
        const result = resources.map((res) => {
            let baseUrl = res.url.split("/")[1];
            return {base: baseUrl, resource: res};
        }).reduce((prev, curr) => {
            if (!prev[curr.base]) {
                prev[curr.base] = [];
            }
            prev[curr.base].push(curr.resource);
            return prev;
        }, {});

        let copyResult = Object.assign({}, result);

        let totalPerBaseEndpoint = Object.keys(copyResult).map((key, index) => {
            let resources = copyResult[key];
            return this.usagePercentageForBase(resources, totalErrors, key)
        });

        let drilldown = Object.keys(copyResult).map((key, index) => {
            let resources = copyResult[key];
            return this.endpointsDrillDown(resources, totalErrors, key)
        });


        return {
            series: [{
                name: 'Resources',
                colorByPoint: true,
                data: totalPerBaseEndpoint
            }],
            drilldown: {
                animation: false,
                series: drilldown
            }
        }
    }

    endpointsDrillDown(resources, totalErrors, baseUrl) {

        const drillDownData = resources.map((resource) => {
            let usageData = 0;
            if (totalErrors > 0) {
                let errorForThisResource = this.getErrorsForEndpoint(resource);
                usageData = (errorForThisResource / totalErrors) * 100;
            }
            return [resource.method + " " + resource.url, usageData]
        });


        return {
            name: baseUrl,
            id: baseUrl,
            data: drillDownData
        };
    }

    usagePercentageForBase(resources, totalErrors, baseUrl) {
        let errorsPercent = 0;
        if (totalErrors > 0) {
            const totalErrorForBaseUrl = resources.map((resource) => {
                return this.getErrorsForEndpoint(resource);
            }).reduce((prev, curr) => {
                return prev + curr;
            }, 0);
            errorsPercent = (totalErrorForBaseUrl / totalErrors) * 100;
        }

        return {
            name: baseUrl,
            y: errorsPercent,
            drilldown: baseUrl
        }
    };

    getConfig(data, totalErrors) {
        let config = {
            chart: {
                type: 'column'
            },
            title: {
                text: 'Errors per endpoint (' + totalErrors + ' total)'
            },
            subtitle: {
                text: 'Click the columns to more details.'
            },
            xAxis: {
                useHTML: true,//Set to true
                style: {
                    width: '50px',
                    whiteSpace: 'normal'//set to normal
                },
                type: 'category'
            },
            yAxis: {
                max: 100,
                title: {
                    text: 'Resource errors'
                }
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                series: {
                    borderWidth: 0,
                    dataLabels: {
                        enabled: true,
                        format: '{point.y:.1f}%'
                    }
                }
            },

            tooltip: {
                headerFormat: '<span style="font-size:11px">{series.name}</span><br>',
                pointFormat: '<span style="color:{point.color}">{point.name}</span>: <b>{point.y:.2f}%</b> of total<br/>'
            },
        };

        return Object.assign(config, data);
    }

    render() {

        const {resources} = this.props;
        if (!resources || resources.length === 0) {
            return <h3>No data</h3>
        }

        const totalErrors = this.getTotalErrors(resources);
        const series = this.getMainResources(resources, totalErrors);
        const config = this.getConfig(series, totalErrors);


        return (
            <div>
                <HighchartReact container={'errors_drilldown'} options={config}/>
            </div>
        )
    }
}