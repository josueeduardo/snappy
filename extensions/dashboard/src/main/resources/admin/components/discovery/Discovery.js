import React from "react";
import {inject, observer} from "mobx-react";

@inject("metricsStore", "stateStore") @observer
export default class Discovery extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Discovery";
        this.props.stateStore.pageDescription = "Manage services interaction";
        this.props.stateStore.headerIcon = "icon-globe";
    }

    render() {
        return (
            <div>

            </div>
        )
    }
}