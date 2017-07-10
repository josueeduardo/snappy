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

    getTotalRequests(resourceSummaries) {
        return resourceSummaries.map((resource) => {
            return resource.metrics.totalRequests;
        }).reduce((prev, current) => {
            return prev + current;
        }, 0);
    }

    getMainResources(resourceSummaries) {
        const result = resourceSummaries.map((res) => {
            let baseUrl = res.url.split("/")[1];
            return {base: baseUrl, resource: res};
        }).reduce((prev, curr) => {
            if (!prev[curr.base]) {
                prev[curr.base] = [];
            }
            prev[curr.base].push(curr.resource);
            return prev;
        }, {});

        const totalRequests = this.getTotalRequests(resourceSummaries);
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
                text: 'Errors'
            },
            subtitle: {
                text: 'Click the columns to more details.'
            },
            xAxis: {
                useHTML:true,//Set to true
                style:{
                    width:'50px',
                    whiteSpace:'normal'//set to normal
                },
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

        const {resourceSummaries} = this.props;
        if (!resourceSummaries || resourceSummaries.length === 0) {
            return <h3>No data</h3>
        }

        let series = this.getMainResources(resourceSummaries);
        // const config = this.getConfig(series);
        const config = {
            chart: {
                type: 'column'
            },
            title: {
                text: 'Browser market shares. January, 2015 to May, 2015'
            },
            subtitle: {
                text: 'Click the columns to view versions. Source: <a href="http://netmarketshare.com">netmarketshare.com</a>.'
            },
            xAxis: {
                type: 'category'
            },
            yAxis: {
                title: {
                    text: 'Total percent market share'
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

            series: [{
                name: 'Brands',
                animation: false,
                colorByPoint: true,
                data: [{
                    name: 'Microsoft Internet Explorer',
                    y: 56.33,
                    drilldown: 'Microsoft Internet Explorer'
                }, {
                    name: 'Chrome',
                    y: 24.03,
                    drilldown: 'Chrome'
                }, {
                    name: 'Firefox',
                    y: 10.38,
                    drilldown: 'Firefox'
                }, {
                    name: 'Safari',
                    y: 4.77,
                    drilldown: 'Safari'
                }, {
                    name: 'Opera',
                    y: 0.91,
                    drilldown: 'Opera'
                }, {
                    name: 'Proprietary or Undetectable',
                    y: 0.2,
                    drilldown: null
                }]
            }],
            drilldown: {
                animation: false,
                series: [{
                    name: 'Microsoft Internet Explorer',
                    id: 'Microsoft Internet Explorer',
                    data: [
                        [
                            'v11.0',
                            24.13
                        ],
                        [
                            'v8.0',
                            17.2
                        ],
                        [
                            'v9.0',
                            8.11
                        ],
                        [
                            'v10.0',
                            5.33
                        ],
                        [
                            'v6.0',
                            1.06
                        ],
                        [
                            'v7.0',
                            0.5
                        ]
                    ]
                }, {
                    name: 'Chrome',
                    id: 'Chrome',
                    data: [
                        [
                            'v40.0',
                            5
                        ],
                        [
                            'v41.0',
                            4.32
                        ],
                        [
                            'v42.0',
                            3.68
                        ],
                        [
                            'v39.0',
                            2.96
                        ],
                        [
                            'v36.0',
                            2.53
                        ],
                        [
                            'v43.0',
                            1.45
                        ],
                        [
                            'v31.0',
                            1.24
                        ],
                        [
                            'v35.0',
                            0.85
                        ],
                        [
                            'v38.0',
                            0.6
                        ],
                        [
                            'v32.0',
                            0.55
                        ],
                        [
                            'v37.0',
                            0.38
                        ],
                        [
                            'v33.0',
                            0.19
                        ],
                        [
                            'v34.0',
                            0.14
                        ],
                        [
                            'v30.0',
                            0.14
                        ]
                    ]
                }, {
                    name: 'Firefox',
                    id: 'Firefox',
                    data: [
                        [
                            'v35',
                            2.76
                        ],
                        [
                            'v36',
                            2.32
                        ],
                        [
                            'v37',
                            2.31
                        ],
                        [
                            'v34',
                            1.27
                        ],
                        [
                            'v38',
                            1.02
                        ],
                        [
                            'v31',
                            0.33
                        ],
                        [
                            'v33',
                            0.22
                        ],
                        [
                            'v32',
                            0.15
                        ]
                    ]
                }, {
                    name: 'Safari',
                    id: 'Safari',
                    data: [
                        [
                            'v8.0',
                            2.56
                        ],
                        [
                            'v7.1',
                            0.77
                        ],
                        [
                            'v5.1',
                            0.42
                        ],
                        [
                            'v5.0',
                            0.3
                        ],
                        [
                            'v6.1',
                            0.29
                        ],
                        [
                            'v7.0',
                            0.26
                        ],
                        [
                            'v6.2',
                            0.17
                        ]
                    ]
                }, {
                    name: 'Opera',
                    id: 'Opera',
                    data: [
                        [
                            'v12.x',
                            0.34
                        ],
                        [
                            'v28',
                            0.24
                        ],
                        [
                            'v27',
                            0.17
                        ],
                        [
                            'v29',
                            0.16
                        ]
                    ]
                }]
            }
        };

        return (
            <div>
                <HighchartReact  container={'error_drilldown'} options={config}/>
            </div>
        )
    }
}