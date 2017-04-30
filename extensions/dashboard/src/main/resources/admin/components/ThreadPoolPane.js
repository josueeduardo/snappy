import React from "react";
import {inject, observer} from "mobx-react";


@observer
export default class ThreadPoolPane extends React.Component {

    threadPoolInfo(threadPool) {
        const {poolName} = threadPool;
        const {poolSize} = threadPool;
        const {activeCount} = threadPool;
        const {shutdown} = threadPool;

        const label = shutdown ? 'label label-red' : 'label label-green';
        const statusText = shutdown ? 'Shutdown' : 'Active';
        return (
            <tr key={poolName} class="status-pending">
                <td class="icon"><i class="icon-cogs"></i></td>
                <td><b>{poolName}</b>b></td>
                <td><b>{activeCount} / {poolSize}</b></td>
                <td><span style={{textAlign: 'center'}} class={label}>{statusText}</span></td>
            </tr>
        )
    }

    render() {

        let element = <h3>No thread pool available</h3>
        if (this.props.threadPools && this.props.threadPools.length > 0) {
            const threadPools = this.props.threadPools.map((tp) => {
                return this.threadPoolInfo(tp);
            });
            element = (
                <div class="box-content">
                    <table class="table table-normal">
                        <thead>
                        <tr>
                            <td></td>
                            <td>Name</td>
                            <td>Usage</td>
                            <td>Status</td>
                        </tr>
                        </thead>

                        <tbody>
                        {threadPools}
                        </tbody>
                    </table>
                </div>
            )
        }


        return (
            <div class="box">
                <div class="box-header">
                    <span class="title">Thread pools</span>
                </div>
                {element}

            </div>
        )
    }
}