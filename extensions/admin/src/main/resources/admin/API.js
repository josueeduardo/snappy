import axios from "axios";
const ENVIRONMENT = {
    development: "http://localhost:3000",
    integration: "http://localhost:9100",
    production: ""
};

const DEFAULT_ENV = "development";
class API {
    static instance;
    static envKey;

    constructor() {
        API.envKey = process.env.NODE_ENV;
        API.envKey = API.envKey ? API.envKey : DEFAULT_ENV;

        API.envKey = API.envKey.trim().toLowerCase();
        let url = ENVIRONMENT[API.envKey];
        console.log("==== " + API.envKey + ":" + url + " ====");

        this.instance = axios.create({
            baseURL: url,
            // timeout: 1000,
            // headers: {'X-Custom-Header': 'foobar'}
        });
    }

    axios() {
        return this.instance;
    }

    baseUrl() {
        return ENVIRONMENT[API.envKey];
    }
}

export default new API().axios();




