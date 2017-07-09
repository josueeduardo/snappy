import React from "react";
import {inject, observer} from "mobx-react";


@observer
export default class ThreadPoolDetails extends React.Component {

    threadPoolInfo(threadPool) {
        const {poolName} = threadPool;
        const {poolSize} = threadPool;
        const {corePoolSize} = threadPool;
        const {maximumPoolSize} = threadPool;
        const {activeCount} = threadPool;
        const {shutdown} = threadPool;
        const {completedTaskCount} = threadPool;
        const {largestPoolSize} = threadPool;

        const {queueCapacity} = threadPool;
        const {queuedTasks} = threadPool;

        const label = shutdown ? 'label label-red' : 'label label-green';
        const statusText = shutdown ? 'Shutdown' : 'Active';

        const usagePercent = activeCount === 0 ? 0 : parseInt((activeCount / poolSize) * 100);
        const poolSizePercent = poolSize === 0 ? 0 : parseInt((poolSize / maximumPoolSize) * 100);
        const queuedTasksPercent = queuedTasks === 0 ? 0 : parseInt((queuedTasks / queueCapacity) * 100);

        return (
            <tr key={poolName} class="status-pending">
                <td class="icon"><i class="icon-cogs"></i></td>
                <td><b>{poolName}</b></td>
                <td>
                    <div class="progress progress-striped active" style={{marginBottom: 0}}>
                        <div class="progress-bar progress-blue tip" title="" style={{width: poolSizePercent + '%'}}></div>
                    </div>
                    <span style={{textAlign: 'center'}}><b>{poolSize} / {maximumPoolSize}</b></span>
                </td>
                <td>
                    <div class="progress progress-striped active" style={{marginBottom: 0}}>
                        <div class="progress-bar progress-blue tip" title="" style={{width: usagePercent + '%'}}></div>
                    </div>
                    <span style={{textAlign: 'center'}}><b>{activeCount} / {poolSize}</b></span>
                </td>
                <td>
                    <div class="progress progress-striped active" style={{marginBottom: 0}}>
                        <div class="progress-bar progress-blue tip" title="" style={{width: queuedTasksPercent + '%'}}></div>
                    </div>
                    <span style={{textAlign: 'center'}}><b>{queuedTasks} / {queueCapacity}</b></span>
                </td>
                <td><b>{completedTaskCount}</b></td>
                <td><b>{largestPoolSize}</b></td>
                <td><span style={{textAlign: 'center'}} class={label}><b>{statusText}</b></span></td>
            </tr>
        )
    }

    render() {

        let element = <div class="text-center padded"><h3>No thread pool available</h3></div>
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
                            <td>Pool size</td>
                            <td>Active taks</td>
                            <td>Queued tasks</td>
                            <td>Completed tasks</td>
                            <td>Largest pool size</td>
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