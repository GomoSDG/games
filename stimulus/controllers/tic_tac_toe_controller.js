import { Controller } from "stimulus";

export default class extends Controller {

    static get targets() {
        return [];
    }

    placeSymbol(e) {
        const pos = e.srcElement.getAttribute("data-pos");

        console.log(e.srcElement);

        console.log("Sending message", this.user, this.socket);

        this.socket.send(JSON.stringify({"type" : "place-symbol",
                                         "pos" : parseInt(pos)}));
    }

    processMessage = (m) => {
        let jsonMessage;
        try {
            console.log("Message is: ", m.data);
            jsonMessage = JSON.parse(m.data);
            console.log("Parsed message: ", this, jsonMessage);
            this.user = jsonMessage;
        } catch (e) {
            // do nothing
            console.log(e);
        }
    }

    connect(){
        console.log("this has connected!");
        this.socket = new WebSocket(`ws://${window.location.host}${window.location.pathname}/ws`);
        console.log("This is: ", this);
        Turbo.connectStreamSource(this.socket);
        this.socket.addEventListener("message", this.processMessage, false);
    }
}
