import React from "react";
import {observer} from "mobx-react";


@observer
export default class AppMetricsTable extends React.Component {

    render() {

        let metricElements = [];
        if (this.props.appMetrics) {
            for (let metricKey in this.props.appMetrics) {
                metricElements.push((
                    <tr key={metricKey} class="status-pending">
                        <td><b>{metricKey}</b></td>
                        <td><b>{new String(this.props.appMetrics[metricKey])}</b></td>
                    </tr>
                ))
            }
        }

        if (metricElements.length === 0) {
            return <h3>No data available</h3>
        }


        return (
            <div class="box">
                <div class="box-header">
                    <span class="title">App metrics</span>
                </div>
                <div class="box-content">
                    <table class="table table-normal">
                        <thead>
                        <tr>
                            <td>Name</td>
                            <td>Value</td>
                        </tr>
                        </thead>
                        <tbody>
                        {metricElements}
                        </tbody>
                    </table>
                </div>

            </div>
        )
    }
}