import axios from "axios";
const ENVIRONMENT = {
    DEV: "http://localhost:3000",
    INT: "http://localhost:9100",
    PROD: "",
};

const DEFAULT_ENV = "dev";
class API {
    static instance;
    static envKey;

    constructor() {
        API.envKey = process.env.NODE_ENV;
        API.envKey = API.envKey ? API.envKey : DEFAULT_ENV;

        API.envKey = API.envKey.trim().toUpperCase();
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




