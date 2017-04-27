import React from "react";
import ReactDOM from "react-dom";
import {Router, Route, IndexRoute, hashHistory, browserHistory} from "react-router";
import {Provider} from "mobx-react";
import App from "./components/App.js";
// import Debugger from "./components/Debugger";
//Stores
// import stateStore from "./components/StateStore";
// import uploadStore from "./components/upload/UploadStore";
// import videoStore from "./components/video/VideoStore";
// import forumStore from "./components/forum/ForumStore";
// import commentStore from "./components/comment/CommentStore";
// import accountStore from "./components/account/AccountStore";
// import locationStore from "./components/location/LocationStore";
//
// const stores = {stateStore, uploadStore, videoStore, commentStore, accountStore, locationStore, forumStore};
const stores = {};

ReactDOM.render(
    <Provider {...stores}>
        <Router history={browserHistory}>
            <Route path="/" component={App}>
                {/*<IndexRoute component={Home}/>*/}
                {/*<Route path="/videos" component={Videos}/>*/}
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
