import React from "react";
import {inject, observer} from "mobx-react";

@inject("metricsStore", "stateStore") @observer
export default class AppMetrics extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Application metrics";
        this.props.stateStore.pageDescription = "Manage metrics defined in your application";
        this.props.stateStore.headerIcon = "icon-bar-chart";
    }

    render() {
        return (
            <div>

            </div>
        )
    }
}