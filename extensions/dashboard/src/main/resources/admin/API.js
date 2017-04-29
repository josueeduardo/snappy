import axios from "axios";
const ENVIRONMENT = {
    DEV: "http://localhost:3000",
    PROD: "",
};

const DEFAULT_ENV = "dev";
class API {
    static instance;

    constructor() {
        let envKey = process.env.NODE_ENV;
        envKey = envKey ? envKey : DEFAULT_ENV;

        envKey = envKey.trim().toUpperCase();
        let url = ENVIRONMENT[envKey];
        console.log("==== " + envKey + ":" + url + " ====");

        this.instance = axios.create({
            baseURL: url,
            // timeout: 1000,
            // headers: {'X-Custom-Header': 'foobar'}
        });
    }

    axios() {
        return this.instance;
    }
}

export default new API().axios();




