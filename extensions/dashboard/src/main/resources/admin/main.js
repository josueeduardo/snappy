import React from "react";
import ReactDOM from "react-dom";
import {Router, Route, IndexRoute, hashHistory, browserHistory} from "react-router";
import {Provider} from "mobx-react";
import App from "./components/App.js";
import Home from "./components/Home";
import Resources from "./components/resources/Resources";
import ThreadPool from "./components/threadpools/ThreadPool";
import Logs from "./components/logs/Logs";
import Discovery from "./components/discovery/Discovery";
// import Debugger from "./components/Debugger";
//Stores
// import stateStore from "./components/StateStore";
// import uploadStore from "./components/upload/UploadStore";
// import videoStore from "./components/video/VideoStore";
// import forumStore from "./components/forum/ForumStore";
// import commentStore from "./components/comment/CommentStore";
// import accountStore from "./components/account/AccountStore";
// import locationStore from "./components/location/LocationStore";
import metricsStore from "./components/MetricsStore";
//
// const stores = {stateStore, uploadStore, videoStore, commentStore, accountStore, locationStore, forumStore};
const stores = {metricsStore};

ReactDOM.render(
    <Provider {...stores}>
        <Router history={browserHistory}>
            <Route path="/" component={App}>
                <IndexRoute component={Home}/>
                <Route path="/resources" component={Resources}/>
                <Route path="/thread-pools" component={ThreadPool}/>
                <Route path="/logs" component={Logs}/>
                <Route path="/discovery" component={Discovery}/>
                <Route path="/settings" component={Discovery}/>
                {/*<Route path="/uploads" component={Upload}/>*/}
                {/*<Route path="/videos/:videoId/:chapterId" component={Video}/>*/}
                {/*<Route path="/forum" component={Forum}>*/}
                    {/*<IndexRoute component={QuestionsList}/>*/}
                    {/*<Route path="new-question" component={NewQuestion}/>*/}
                    {/*<Route path=":threadId" component={Thread}/>*/}
                {/*</Route>*/}
                {/*<Route path="/account" component={Account}>*/}
                    {/*<IndexRoute component={Login}/>*/}
                    {/*<Route path="signup" component={Signup}/>*/}
                    {/*<Route path="created" component={SignedUp}/>*/}
                    {/*<Route path="confirmation" component={Confirmation}/>*/}
                {/*</Route>*/}
                {/*<Route path="/users/:userId" component={UserProfile}/>*/}
            </Route>
        </Router>
    </Provider>
    , document.getElementById('app'));


// ReactDOM.render(
//     <Debugger />
//     , document.getElementById('dev-tools'));
