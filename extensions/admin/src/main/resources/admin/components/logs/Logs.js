import React from "react";
import {inject, observer} from "mobx-react";


@inject("logStore", "stateStore") @observer
export default class Logs extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Logs";
        this.props.stateStore.pageDescription = "View system logs";
        this.props.stateStore.headerIcon = "icon-file-alt";
    }

    componentDidMount() {
        let tailf = this.props.stateStore.logMode === 'tailf';
        this.props.logStore.connect(document.getElementById("log"), tailf);
    }

    componentWillUnmount() {
        this.props.logStore.disconnect();
        document.getElementById("log").innerHTML = '';
    }

    switchTab(e, name) {
        e.preventDefault();
        const {logMode} = this.props.stateStore;
        if(logMode !== name) {
            this.props.stateStore.logMode = name;
            let tailf = name === 'tailf';
            this.props.logStore.connect(document.getElementById("log"), tailf)
        }

    }

    getPanelHeader() {
            const {logMode} = this.props.stateStore;
        return (
            <ul class="nav nav-tabs nav-tabs-right">
                <li class={logMode === 'tailf' ? 'active' : ''}>
                    <a href="#" data-toggle="tab" onClick={(e) => this.switchTab(e, 'tailf')}>
                        <i class="icon-refresh"></i><span>Tailf</span>
                    </a>
                </li>
                <li class={logMode === 'full' ? 'active' : ''}>
                    <a href="#" data-toggle="tab" onClick={(e) => this.switchTab(e, 'full')}>
                        <i class="icon-file-alt"></i><span>Full log</span>
                    </a>
                </li>
            </ul>
        )
    }


    render() {
        // const parsed = this.props.logStore.logLines.map((line, i) => {
        //     return (<span key={i}>{line}<br/></span>);
        // });
        const panelTab = this.getPanelHeader();
        return (
            <div class="row">
                <div class="col-md-12">
                    <div class="box">
                        <div class="box-header">
                            <span class="title">Log</span>
                            {panelTab}
                        </div>
                        <div class="box-content">
                            <div id="log" style={{marginLeft: '20px', minHeight: '500px'}}>

                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}