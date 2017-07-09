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

        this.props.metricsStore.fetchStats();
        this.timer = setInterval(() => {
            this.props.metricsStore.fetchStats();
        }, 10000);

        this.props.metricsStore.fetchResources();
    }

    componentWillUnmount() {
        clearInterval(this.timer);
    }

    render() {

        const {stats, resourceSummaries} = this.props.metricsStore;
        const {usedMemory, totalMemory, maxMemory} = stats.memory;

        return (
            <div>
                <div class="row">
                    <div class="col-md-6 col-md-offset-3">
                        <div class="box">
                            <div class="box-header"><span class="title">Memory</span></div>
                            <div class="box-content padded" style={{textAlign: 'center'}}>
                                <Gauge key={'heapUsage'}
                                       name={'heapUsage'}
                                       value={usedMemory}
                                       min={0}
                                       max={totalMemory}
                                       title={'Heap Usage'}
                                       label={'MB'}/>

                                <Gauge key={'heapSize'}
                                       name={'heapSize'}
                                       value={totalMemory}
                                       min={0}
                                       max={maxMemory}
                                       title={'Heap Size'}
                                       label={'MB'}/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <ResourcesPane resources={resourceSummaries}/>
                    </div>
                    <div class="col-md-6">
                        <ThreadPoolPane threadPools={stats.threadPools}/>
                    </div>
                </div>
            </div>
    )
    }
    }