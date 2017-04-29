import React from "react";
import {inject, observer} from "mobx-react";


@inject("metricsStore", "stateStore") @observer
export default class ThreadPool extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Thread pools";
        this.props.stateStore.pageDescription = "View system thread pools";
        this.props.stateStore.headerIcon = "icon-file-alt";
    }

    render() {
        return (
            <div>

            </div>
        )
    }
}