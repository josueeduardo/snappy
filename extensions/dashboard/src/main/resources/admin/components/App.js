import React from "react";
import Header from "./Header";
import Home from "./Home";
import SideMenu from "./SideMenu";

export default class App extends React.Component {

    render() {
        return (
            <div>
                <Header/>
                <SideMenu />
                <Home/>
            </div>
        )
    }


}