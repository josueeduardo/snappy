import React from "react";
import Header from "./Header";
import SideMenu from "./SideMenu";
import {inject} from "mobx-react";
import ContainerHeader from "./ContainerHeader";

@inject("metricsStore")
export default class App extends React.Component {

    componentWillMount() {
        this.props.metricsStore.fetch();
    }

    render() {
        return (
            <div>
                <Header  />
                <SideMenu currentPath={this.props.location.pathname}/>

                <div class="main-content">
                    <ContainerHeader />

                    <div class="container">

                        {React.Children.map(this.props.children, child =>
                            React.cloneElement(child, {key: this.props.location.pathname})
                        )}

                    </div>
                </div>
            </div>
        )
    }


}