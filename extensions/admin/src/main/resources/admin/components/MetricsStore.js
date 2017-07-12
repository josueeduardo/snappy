import {observable} from "mobx";
import API from "../API";


class MemoryUsage {
    @observable maxMemory = 0;
    @observable totalMemory = 0;
    @observable freeMemory = 0;
    @observable usedMemory = 0;
}

class ServerStats {
    @observable memory = new MemoryUsage();
}

class MetricsStore {
    //memory + thread pool
    @observable stats = new ServerStats();
    @observable metrics = {};
    @observable resources = [];
    @observable resourceSummaries = [];

    constructor() {

    }

    fetchStats() {
        API.get("/stats")
            .then((res) => {
                this.stats = res.data;
            })
            .catch((err) => {
                console.error("Error fetching stats data");
                throw err;
            });
    }

    fetchAppMetrics() {
        API.get("/metrics")
            .then((res) => {
                this.metrics = res.data;
            })
            .catch((err) => {
                console.error("Error fetching metrics data");
                throw err;
            });
    }

    fetchResources() {
        API.get("/resources")
            .then((res) => {
                this.resources = res.data;
            })
            .catch((err) => {
                console.error("Error fetching metrics data");
                throw err;
            });
    }

}

export default new MetricsStore;