import React from "react";
import ReactDOM from "react-dom";
import {observer} from "mobx-react";
import JustGage from "./justgage";

const mb = 1048576;

@observer
export default class Gauge extends React.Component {
    componentDidMount() {
        this.node = ReactDOM.findDOMNode(this);

        this.guage = new JustGage({
            id: this.props.name,
            value: this.toMb(this.props.value),
            min: this.toMb(this.props.min),
            max: this.toMb(this.props.max),
            title: this.props.title,
            label: this.props.label
        });
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.value !== this.props.value) {
            let value = this.toMb(this.props.value);
            this.guage.refresh(value);
        }
        if (nextProps.max !== this.props.max) {
            let max = this.toMb(nextProps.max);
            let value = this.toMb(nextProps.value);
            this.guage.refresh(value, max);
        }
    }

    toMb(val) {
        return  parseInt(parseInt(val) / mb);
    }

    componentWillUnmount() {
        this.guage = null;
        ReactDOM.unmountComponentAtNode(this.node);
    }

    render() {
        return (
            <div id={this.props.name} class="justgage" />
        );
    }
}

// gauges = [];
// $(".justgage.js").each(function() {
//     var gaugeWidthScale, refreshAnimationType, showMinMax;
//     showMinMax = $(this).attr("data-labels") || true;
//     gaugeWidthScale = $(this).attr("data-gauge-width-scale") || 1;
//     refreshAnimationType = $(this).attr("data-animation-type") || "linear";
//     return gauges.push(new JustGage({
//         id: $(this).attr("id"),
//         min: 0,
//         max: 100,
//         title: $(this).attr("data-title"),
//         value: getRandomInt(1, 80),
//         label: "",
//         levelColorsGradient: false,
//         showMinMax: showMinMax,
//         gaugeWidthScale: gaugeWidthScale,
//         startAnimationTime: 1000,
//         startAnimationType: ">",
//         refreshAnimationTime: 1000,
//         refreshAnimationType: refreshAnimationType,
//         levelColors: [Theme.colors.green, Theme.colors.orange, Theme.colors.red]
//     }));
// });
// setInterval(function() {
//     return $(gauges).each(function() {
//         return this.refresh(getRandomInt(0, 80));
//     });
// }, 2500);