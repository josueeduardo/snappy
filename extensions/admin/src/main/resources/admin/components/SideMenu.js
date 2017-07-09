import React from "react";
import {inject, observer} from "mobx-react";
import {Link, withRouter} from "react-router";

@withRouter @observer
export default class SideMenu extends React.Component {
    render() {
        return (
            <div>
                <div class="sidebar-background">
                    <div class="primary-sidebar-background"></div>
                </div>

                <div class="primary-sidebar">
                    <ul class="nav navbar-collapse collapse navbar-collapse-primary">
                        <MenuItem label="Dashboard" target="/" icon="icon-dashboard" currentPath={this.props.currentPath}/>
                        <MenuItem label="Resources" target="/resources" icon="icon-exchange" currentPath={this.props.currentPath}/>
                        <MenuItem label="Logs" target="/logs" icon="icon-file-alt" currentPath={this.props.currentPath}/>
                        <MenuItem label="Thread Pools" target="/thread-pools" icon="icon-cogs" currentPath={this.props.currentPath}/>
                        <MenuItem label="Discovery" target="/discovery" icon="icon-globe" currentPath={this.props.currentPath}/>
                        <MenuItem label="App metrics" target="/app-metrics" icon=" icon-bar-chart" currentPath={this.props.currentPath}/>
                        <MenuItem label="Settings" target="/settings" icon="icon-wrench" currentPath={this.props.currentPath}/>
                    </ul>
                </div>
            </div>
        )
    }
}

class MenuItem extends React.Component {

    isActive() {
        const cp = this.props.currentPath;
        if(this.props.target === "/")
            return cp === this.props.target;
        else
            return cp.lastIndexOf(this.props.target, 0) === 0
    }

    render() {
        const active = this.isActive();
        const icon = this.props.icon + " icon-2x";
        return (
            <li class={active ? 'active' : ''}>
                <span class="glow"></span>
                <Link to={this.props.target}>
                    <i class={icon}></i>
                    <span>{this.props.label}</span>
                </Link>
            </li>
        )
    }
}
