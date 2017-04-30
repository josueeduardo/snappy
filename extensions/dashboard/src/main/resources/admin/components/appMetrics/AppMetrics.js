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
        return (
            <div>
                <div class="row">
                    <div class="col-md-6">
                        <AppMetricsTable appMetrics={metrics.appMetrics}/>
                    </div>
                    <div class="col-md-6">

                    </div>
                </div>
            </div>
        )
    }


}