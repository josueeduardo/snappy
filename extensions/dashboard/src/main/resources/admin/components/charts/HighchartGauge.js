import React from "react";
import Highcharts from "highcharts";
import hichartsmore from "highcharts-more";
import solidgauge from "highcharts/modules/solid-gauge";

export default class HighchartGauge extends React.Component {

    componentDidMount() {
        // Extend Highcharts with modules
        solidgauge(Highcharts);
        hichartsmore(Highcharts);
        // if (this.props.modules) {
        //     this.props.modules.forEach(function (module) {
        //         solidgauge(Highcharts);
        //     });
        // }
        // Set container which the chart should render to.
        this.chart = new Highcharts[this.props.type || "Chart"](
            this.props.container,
            this.props.options
        );
    }

    componentWillReceiveProps(nextProps) {
        // // if (nextProps.value !== this.props.value) {
        // //     let value = parseInt(parseInt(this.props.value) / mb);
        // //     this.guage.refresh(value);
        // // }
        // // if (nextProps.max !== this.props.max) {
        // let max = parseInt(parseInt(nextProps.max) / mb);
        // let value = parseInt(parseInt(nextProps.value) / mb);
        // this.guage.refresh(value, max);
        // }

        let point = this.chart.series[0].points[0];
        let inc = Math.round((Math.random() - 0.5) * 100);
        let newVal = point.y + inc;

        if (newVal < 0 || newVal > 200) {
            newVal = point.y - inc;
        }

        point.update(newVal);

    }

    componentWillUnmount() {
        this.chart.destroy();
    }

    render() {
        solidgauge(Highcharts);
        hichartsmore(Highcharts);
        return (
            <div>
                <div id={this.props.container}>

                </div>
            </div>
        )
    }
}