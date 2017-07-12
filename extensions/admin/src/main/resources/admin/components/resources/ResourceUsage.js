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

    getTotalRequests(resources) {
        return resources.map((resource) => {
            return resource.metrics.totalRequests;
        }).reduce((prev, current) => {
            return prev + current;
        }, 0);
    }

    getMainResources(resources) {
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

        const totalRequests = this.getTotalRequests(resources);
        let copyResult = Object.assign({}, result);


        let totalPerBaseEndpoint = Object.keys(copyResult).map((key, index) => {
            let resources = copyResult[key];
            return this.usagePercentageForBase(resources, totalRequests, key)
        });

        let drilldown = Object.keys(copyResult).map((key, index) => {
            let resources = copyResult[key];
            return this.endpointsDrillDown(resources, totalRequests, key)
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

    endpointsDrillDown(resources, totalRequest, baseUrl) {

        const drillDownData = resources.map((resource) => {
            let usageData = 0;
            if (totalRequest > 0) {
                usageData = (resource.metrics.totalRequests / totalRequest) * 100;
            }
            return [resource.method + " " + resource.url, usageData]
        });


        return {
            name: baseUrl,
            id: baseUrl,
            data: drillDownData
        };
    }

    usagePercentageForBase(resources, totalRequest, baseUrl) {
        let usageData = 0;
        if (totalRequest > 0) {
            const totalReqForBaseUrl = resources.map((resource) => {
                return resource.metrics.totalRequests;
            }).reduce((prev, curr) => {
                return prev + curr;
            }, 0);
            usageData = (totalReqForBaseUrl / totalRequest) * 100;
        }

        return {
            name: baseUrl,
            y: usageData,
            drilldown: baseUrl
        }
    };


// usagePercentageAllDRAFT(resources, totalRequest, baseUrl) {
//     const usageData = resources.map((resource) => {
//         return {name: resource.url, y: (resource.metrics.totalRequests / totalRequest) * 100};
//     });
//
//     return {
//         title: {
//             text: 'Resource usage'
//         },
//         series: [{
//             name: baseUrl,
//             colorByPoint: true,
//             data: usageData
//         }]
//     };
// }


    getConfig(data) {
        let config = {
            chart: {
                type: 'column'
            },
            title: {
                text: 'Resource usage'
            },
            subtitle: {
                text: 'Click the columns to more details.'
            },
            xAxis: {
                type: 'category'
            },
            yAxis: {
                title: {
                    text: 'Total percent resource usage'
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

        let series = this.getMainResources(resources);
        const config = this.getConfig(series);

        return (
            <div>
                <HighchartReact container={'usage_drilldown'} options={config}/>
            </div>
        )
    }
}