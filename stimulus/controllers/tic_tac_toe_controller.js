import { Controller } from "stimulus";

export default class extends Controller {

    static get targets() {
        return [];
    }

    placeSymbol(e) {
        const pos = e.srcElement.getAttribute("data-pos");
        this.socket.send(JSON.stringify({"type" : "place-symbol",
                                         "pos" : parseInt(pos)}));
    }

    connect(){
        this.socket = new WebSocket(`ws://${window.location.host}${window.location.pathname}/ws`);
        Turbo.connectStreamSource(this.socket);
    }
}
