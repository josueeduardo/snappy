import {observable} from "mobx";


class Metrics {
    @observable maxMemory = 0;
    @observable totalMemory = 0;
    @observable freeMemory = 0;
    @observable usedMemory = 0;
}

class LogStore {
    @observable logLines = [];
    @observable reading = false;
    eventSource = null;


    fetchLogs(element) {
        let xhr = new XMLHttpRequest();
        xhr.open('GET', 'http://localhost:3000/logs', true);
        xhr.responseType = 'blob';

        xhr.onload = function (e) {
            // response is unsigned 8 bit integer
            let responseArray = new Uint8Array(this.response);
            let logString = String.fromCharCode.apply(null, new Uint16Array(responseArray));
            console.log(logString);
            element.innerHTML += logString;

        };

        xhr.send();
    }

    appendHtml(el, str) {
        let fragment = document.createDocumentFragment();
        fragment.innerHTML = str;
        while (fragment.children.length > 0) {
            el.appendChild(fragment.children[0]);
        }
    }

    disconnect() {
        if (this.eventSource) {
            this.eventSource.close();
        }
    }

    connect(element, tailf) {
        element.innerHTML = '';
        if (tailf) {
            let span = document.createElement("span");
            span.id = "tailf-loading";
            span.innerHTML = "<br /><i class=\"icon-spinner icon-spin\"></i> Waiting for new logs...";

            element.appendChild(span);
        }
        this.disconnect();

        if (!!window.EventSource) {
            this.eventSource = new EventSource('http://localhost:9100/logs?tailf=' + tailf.toString());
            // eventSource.onmessage = (e) => {
            //     console.log(e.data);
            //     element.innerHTML += <p>e.data</p>;
            //
            // };

            this.eventSource.onmessage = (e) => {
                this.reading = true;

                console.log(e.data);

                let parsedLine = "";
                if (e.data.indexOf("\tat") !== -1) {
                    parsedLine += "<span class=\"padded-log\">" + e.data + "<br /></span>";
                } else {
                    parsedLine += "<span>" + e.data + "<br /></span>";
                }

                let fragment = document.createElement("div");
                fragment.innerHTML = parsedLine;
                element.appendChild(fragment);
                if (tailf) {
                    element.appendChild(document.getElementById("tailf-loading"));
                }

            };

            this.eventSource.onerror = (e) => {
                this.eventSource.close();
                throw e;
            };
        } else {
            // not supported
        }
    }

}
export default new LogStore;