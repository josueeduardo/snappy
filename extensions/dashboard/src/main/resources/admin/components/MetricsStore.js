import {action, computed, map, observable} from "mobx";
import API from "../API";


class Metrics {
    @observable maxMemory = 0;
    @observable totalMemory = 0;
    @observable freeMemory = 0;
    @observable usedMemory = 0;
}

class AccountStore {
    @observable metrics = new Metrics();

    constructor() {
        setInterval(() => {
            this.fetch();
        }, 3000)
    }

    fetch() {
        API.get("/metrics")
            .then((res) => {
                this.metrics = res.data;
            })
            .catch((err) => {
                console.error("Error fetching data");
                throw err;
            });
    }

}
export default new AccountStore;