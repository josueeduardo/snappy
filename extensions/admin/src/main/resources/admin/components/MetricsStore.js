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
                this.resourceSummaries = this.resources.map((resource) => {
                    const latestData = resource.metrics.length === 0 ? {
                        totalRequestTime: 0,
                        maxRequestTime: 0,
                        minRequestTime: -1,
                        totalRequests: 0,
                        responses: {}
                    } : resource.metrics[resource.metrics.length - 1].data;

                    return {id: resource.id, url: resource.url, method: resource.method, metrics: latestData};

                })
            })
            .catch((err) => {
                console.error("Error fetching metrics data");
                throw err;
            });
    }

}

export default new MetricsStore;