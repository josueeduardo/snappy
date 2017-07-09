import React from "react";
import Highcharts from "highcharts";
import hichartsmore from "highcharts-more";
import HighchartReact from "./HighchartReact";

export default class Pie extends React.Component {

    constructor(props) {
        super(props);
        this.basePie = {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie'
            },
            title: {
                text: 'CHANGE_ME'
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false,
                        // format: '<b>{point.name}</b>: {point.percentage:.0f} %',
                        // style: {
                        //     color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                        // }
                    },
                    showInLegend: true
                }
            },
            series: [{
                name: 'CHANGE_ME',
                colorByPoint: true,
                data: []
            }]
        }

    }

    render() {
        const {name} = this.props;
        let props = Highcharts.merge(this.basePie, this.props.data);
        return (
            <HighchartReact container={name} options={props}/>
        )
    }
}