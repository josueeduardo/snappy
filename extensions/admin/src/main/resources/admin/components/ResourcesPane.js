import React from "react";
import {observer} from "mobx-react";


@observer
export default class ResourcesPanel extends React.Component {

    methodLabel(method) {
        let color = 'label label-blue';
        if (method === 'GET') {
            color = 'label label-green'
        } else if (method === 'POST') {
            color = 'label label-gray'
        } else if (method === 'PUT') {
            color = 'label label-blue'
        } else if (method === 'DELETE') {
            color = 'label label-red'
        }

        return (<span style={{textAlign: 'center'}} class={color}>{method}</span>)
    }

    errorPercent(metric) {
        if(metric.totalRequests === 0) {
            return "-";
        }
        let errorCount = 0;
        for (var key in metric.responses) {
            let status = parseInt(key);
            var count = metric.responses[key];
            if(status < 200 || status > 299) {
                errorCount += count;
            }
        }
        if(errorCount === 0) {
            return 0;
        }
        return parseInt((errorCount / metric.totalRequests) * 100);
    }

    resourceInfo(resource) {
        const {url,method, metrics} = resource;

        let methodSpan = this.methodLabel(method);
        // let errorsPercent = this.errorPercent(metrics);

        let urlKey = method + url.replace(/\//g, '').replace(/{/g, '').replace(/}/g, '');

        return (
            <tr key={urlKey} class="status-pending">
                <td class="icon"><i class="icon-exchange"></i></td>
                <td class="icon">{methodSpan}</td>
                <td><b>{url}</b></td>
                <td style={{width: '100px'}}><b>{metrics.totalRequests}</b></td>
                <td style={{width: '150px'}}><b>{metrics.maxRequestTime}</b></td>
                {/*<td style={{width: '100px'}}><b>{errorsPercent}</b></td>*/}
            </tr>
        )
    }

    render() {

        let element = <div class="text-center padded"><h3>No resource available</h3></div>;
        if (this.props.resources && this.props.resources.length > 0) {
            const resources = this.props.resources.map((re) => {
                return this.resourceInfo(re);
            });
            element = (
                <div class="box-content">
                    <table class="table table-normal">
                        <thead>
                        <tr>
                            <td></td>
                            <td>Method</td>
                            <td>URL</td>
                            <td>Requests</td>
                            <td>Max response time (ms)</td>
                        </tr>
                        </thead>

                        <tbody>
                        {resources}
                        </tbody>
                    </table>
                </div>
            )
        }


        return (
            <div class="box">
                <div class="box-header">
                    <span class="title">{this.props.title}</span>
                </div>
                {element}

            </div>
        )
    }
}