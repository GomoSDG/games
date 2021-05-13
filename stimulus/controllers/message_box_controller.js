import { Controller } from "stimulus";

export default class extends Controller {
    connect() {
        console.log("Connected! Notifications", this.element);
    }

    initialize() {
        setTimeout(() => {
            this.element.classList.add("is-hidden");
        }, 5000);
    }
}
