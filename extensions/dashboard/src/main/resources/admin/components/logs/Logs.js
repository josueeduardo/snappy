import React from "react";
import {inject, observer} from "mobx-react";


@inject("metricsStore", "stateStore") @observer
export default class Logs extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Logs";
        this.props.stateStore.pageDescription = "View system logs";
        this.props.stateStore.headerIcon = "icon-file-alt";
    }

    render() {
        return (
            <div>

            </div>
        )
    }
}