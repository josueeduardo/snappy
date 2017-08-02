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
    @observable resourcesEnabled = false;
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

    fetchResourceMetricsStatus() {
        API.get("/resources/status")
            .then((res) => {
                this.resourcesEnabled = res.data.enabled;
            })
            .catch((err) => {
                console.error("Error on fetchResourceMetricsStatus");
                throw err;
            });
    }

    setResourceMetricEnabled(enabled) {
        API.put("/resources/status", {enabled: enabled})
            .then((res) => {
                this.resourcesEnabled = enabled;
            })
            .catch((err) => {
                console.error("Error on setResourceMetricEnabled");
                throw err;
            });
    }

}

export default new MetricsStore;