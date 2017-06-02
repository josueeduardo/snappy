import React from "react";
import {inject, observer} from "mobx-react";
import AppMetricsTable from "./AppMetricsTable";

@inject("metricsStore", "stateStore") @observer
export default class AppMetrics extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Application metrics";
        this.props.stateStore.pageDescription = "Manage metrics defined in your application";
        this.props.stateStore.headerIcon = "icon-bar-chart";
    }

    render() {
        const {metrics} = this.props.metricsStore;


        let timed = {};
        let nonTimed = {};
        for (let key in metrics.appMetrics) {
            let item = metrics.appMetrics[key];
            if (Object.prototype.toString.call(item) === '[object Array]') {
                timed[key] = item;
            } else {
                nonTimed[key] = item;
            }
        }

        return (
            <div>
                <div class="row">
                    <div class="col-md-6">
                        <AppMetricsTable appMetrics={nonTimed}/>
                    </div>
                    <div class="col-md-6">

                    </div>
                </div>
            </div>
        )
    }


}