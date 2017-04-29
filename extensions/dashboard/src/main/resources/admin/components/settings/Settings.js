import React from "react";
import {inject, observer} from "mobx-react";


@inject("metricsStore", "stateStore") @observer
export default class Settings extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Settings";
        this.props.stateStore.pageDescription = "Configure system";
        this.props.stateStore.headerIcon = "icon-wrench";
    }

    render() {
        return (
            <div>

            </div>
        )
    }
}