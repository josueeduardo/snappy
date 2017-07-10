import React from "react";
import ReactDOM from "react-dom";
import {Router, Route, IndexRoute, hashHistory, browserHistory} from "react-router";
import {Provider} from "mobx-react";
import App from "./components/App.js";
import Dashboard from "./components/Dashboard";
import Resources from "./components/resources/Resources";
import ThreadPool from "./components/threadpools/ThreadPool";
import Logs from "./components/logs/Logs";
import Discovery from "./components/discovery/Discovery";
import Settings from "./components/settings/Settings";
import AppMetrics from "./components/appMetrics/AppMetrics";
//Stores
import metricsStore from "./components/MetricsStore";
import stateStore from "./components/StateStore";
import logStore from "./components/logs/LogStore";

import drilldown from 'highcharts-drilldown';
import Highcharts from "highcharts";

const stores = {metricsStore, stateStore, logStore};

drilldown(Highcharts);
console.log("===============")

ReactDOM.render(
    <Provider {...stores}>
        <Router history={hashHistory}>
            <Route path="/" component={App}>
                <IndexRoute component={Dashboard}/>
                <Route path="/resources" component={Resources}/>
                <Route path="/thread-pools" component={ThreadPool}/>
                <Route path="/logs" component={Logs}/>
                <Route path="/discovery" component={Discovery}/>
                <Route path="/app-metrics" component={AppMetrics}/>
                <Route path="/settings" component={Settings}/>
            </Route>
        </Router>
    </Provider>
    , document.getElementById('app'));
