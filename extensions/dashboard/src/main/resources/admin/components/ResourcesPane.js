import React from "react";
import {observer} from "mobx-react";


@observer
export default class Resources extends React.Component {

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

    resourceInfo(resource) {
        const {url} = resource;
        const {method} = resource;
        const {metrics} = resource;

        let methodSpan = this.methodLabel(method);

        return (
            <tr key={url} class="status-pending">
                <td class="icon"><i class="icon-exchange"></i></td>
                <td class="icon">{methodSpan}</td>
                <td><a href="#">{url}</a></td>
                <td><b>{metrics.totalRequestTime}</b></td>
            </tr>
        )
    }

    render() {

        let element = <h3>No resource available</h3>
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
                    <span class="title">Resources</span>
                </div>
                {element}

            </div>
        )
    }
}