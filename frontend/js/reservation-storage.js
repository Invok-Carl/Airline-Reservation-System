
const ReservationStorage = {

    KEY: "currentReservation",

    get() {
        const data = sessionStorage.getItem(this.KEY);

        if (!data) {
            return null;
        }

        try {
            return JSON.parse(data);
        } catch (error) {
            console.error("Invalid reservation data:", error);
            return null;
        }
    },

    save(reservation) {
        sessionStorage.setItem(
            this.KEY,
            JSON.stringify(reservation)
        );
    },

    getOrCreate() {
        let reservation = this.get();

        if (!reservation) {
            reservation = {
                passengers: [],
                flights: []
            };

            this.save(reservation);
        }

        return reservation;
    },

    clear() {
        sessionStorage.removeItem(this.KEY);
    }
};
