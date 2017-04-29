import React from "react";
import Header from "./Header";
import SideMenu from "./SideMenu";

export default class App extends React.Component {

    render() {
        return (
            <div>
                <Header  />
                <SideMenu currentPath={this.props.location.pathname}/>

                {React.Children.map(this.props.children, child =>
                    React.cloneElement(child, {key: this.props.location.pathname})
                )}

            </div>
        )
    }


}