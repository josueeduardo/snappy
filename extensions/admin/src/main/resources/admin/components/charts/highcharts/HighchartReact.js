import React from "react";
import Highcharts from "highcharts";
import hichartsmore from "highcharts-more";

export default class HighchartReact extends React.Component {

    componentDidMount() {
        // Set container which the chart should render to.
        this.chart = new Highcharts[this.props.type || "Chart"](
            this.props.container,
            this.props.options
        );
    }

    componentWillReceiveProps(nextProps) {
        if(this.props !== nextProps)
            this.chart.series = nextProps;
    }

    componentWillUnmount() {
        this.chart.destroy();
    }

    render() {
        return (
            <div>
                <div id={this.props.container}>

                </div>
            </div>
        )
    }
}