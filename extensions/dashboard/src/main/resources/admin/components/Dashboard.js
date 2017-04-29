import React from "react";
import Gauge from "./charts/Gauge";
import {inject, observer} from "mobx-react";
import ThreadPoolPane from "./ThreadPoolPane";
import ResourcesPane from "./ResourcesPane";


@inject("metricsStore", "stateStore") @observer
export default class Dashboard extends React.Component {

    componentWillMount() {
        this.props.stateStore.pageTitle = "Dashboard";
        this.props.stateStore.pageDescription = "System overview";
        this.props.stateStore.headerIcon = "icon-dashboard";
    }

    render() {

        const {metrics} = this.props.metricsStore;

        return (
            <div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="box">
                            <div class="box-header"><span class="title">Memory</span></div>
                            <div class="box-content padded" style={{textAlign: 'center'}}>
                                <Gauge key={'heapUsage'}
                                       name={'heapUsage'}
                                       value={metrics.usedMemory}
                                       min={0}
                                       max={metrics.totalMemory}
                                       title={'Heap Usage'}
                                       label={'MB'}/>

                                <Gauge key={'heapSize'}
                                       name={'heapSize'}
                                       value={metrics.totalMemory}
                                       min={0}
                                       max={metrics.maxMemory}
                                       title={'Heap Size'}
                                       label={'MB'}/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <ResourcesPane resources={metrics.resources}/>
                    </div>
                    <div class="col-md-6">
                        <ThreadPoolPane threadPools={metrics.threadPools}/>
                    </div>
                </div>
            </div>
        )
    }
}