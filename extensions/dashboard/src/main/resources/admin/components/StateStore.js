import {action, computed, map, observable} from "mobx";
import API from "../API";




class StateStore {
    @observable pageTitle = "Dashboard";
    @observable pageDescription = "System overview";
    @observable headerIcon = "icon-dashboard";

}
export default new StateStore;