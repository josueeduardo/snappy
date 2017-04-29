import React from "react";
import {inject, observer} from "mobx-react";


@inject("metricsStore", "stateStore") @observer
export default class Resources extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Resources";
        this.props.stateStore.pageDescription = "Resources metrics";
        this.props.stateStore.headerIcon = "icon-exchange";
    }

    render() {
        return (
            <div>

            </div>
        )
    }
}