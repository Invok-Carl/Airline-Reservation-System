
package com.airline.service;

import com.airline.dao.*;
import com.airline.model.*;
import com.airline.util.connection.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final BookingSegmentDAO bookingSegmentDAO;
    private final SeatDAO seatDao;
    private final PassengerDAO passengerDAO;
    private final FlightDAO flightDAO;
    private final FareDAO fareDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.bookingSegmentDAO = new BookingSegmentDAO();
        this.seatDao = new SeatDAO();
        this.passengerDAO = new PassengerDAO();
        this.flightDAO = new FlightDAO();
        this.fareDAO = new FareDAO();
    }

    public ReservationService(
            ReservationDAO reservationDAO,
            BookingSegmentDAO bookingSegmentDAO,
            SeatDAO seatDao,
            PassengerDAO passengerDAO,
            FlightDAO flightDAO,
            FareDAO fareDAO
    ) {
        this.reservationDAO = reservationDAO;
        this.bookingSegmentDAO = bookingSegmentDAO;
        this.seatDao = seatDao;
        this.passengerDAO = passengerDAO;
        this.flightDAO = flightDAO;
        this.fareDAO = fareDAO;
    }

    /**
     * Creates one reservation containing one or more booking segments.
     *
     * The incoming BookingSegment objects must contain:
     * passenger_id, flight_id, seat_id, and fare_id.
     *
     * reservation_id and aircraft_id are assigned by this service.
     *
     * @param createdBy ID of the customer/user creating the reservation
     * @param requestedSegments segments requested by the customer
     * @return the generated reservation ID
     */
    public long createReservation(
            long createdBy,
            List<BookingSegment> requestedSegments
    ) throws SQLException {

        validateRequest(createdBy, requestedSegments);

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                BigDecimal totalAmount = BigDecimal.ZERO;
                Set<String> passengerFlightPairs = new HashSet<>();

                /*
                 * Validate every requested segment before inserting
                 * the reservation or any booking segments.
                 */
                for (BookingSegment segment : requestedSegments) {

                    long passengerId = segment.getPassenger_id();
                    long flightId = segment.getFlight_id();
                    long seatId = segment.getSeat_id();

                    String pairKey = passengerId + ":" + flightId;

                    if (!passengerFlightPairs.add(pairKey)) {
                        throw new IllegalArgumentException(
                                "The same passenger cannot be booked "
                                        + "twice on the same flight in one reservation."
                        );
                    }

                    if (!passengerExists(connection, passengerId)) {
                        throw new IllegalArgumentException(
                                "Passenger does not exist: " + passengerId
                        );
                    }

                    Flight flight =
                            getFlightDetails(connection, flightId);

                    if (flight == null) {
                        throw new IllegalArgumentException(
                                "Flight does not exist: " + flightId
                        );
                    }

                    if (hasActiveBooking(
                            connection, passengerId, flightId)) {
                        throw new IllegalArgumentException(
                                "This passenger already has an active "
                                        + "booking for flight " + flightId
                        );
                    }

                    if (!isSeatAvailable(
                            connection,
                            flightId,
                            seatId
                    )) {
                        throw new IllegalArgumentException(
                                "Seat " + seatId
                                        + " is unavailable for flight " + flightId
                        );
                    }

                    Fare fare = getFare(connection, flightId, seatId);

                    if (fare == null) {
                        throw new IllegalArgumentException(
                                "No fare exists for seat " + seatId
                                        + " on flight " + flightId
                        );
                    }
                    if(fare.getTotal_fare() == null){
                        throw new IllegalArgumentException(
                                "Total fare for " + fare.getFare_id()
                                        + "does not exist"
                                );
                    }

                    segment.setFare_id(fare.getFare_id());
                    totalAmount = totalAmount.add(fare.getTotal_fare());

                }

                Reservation reservation = new Reservation();

                reservation.setBooking_reference(
                        generateBookingReference()
                );
                reservation.setReservation_date(LocalDateTime.now());
                reservation.setStatus("PENDING");
                reservation.setTotal_amount(totalAmount);
                reservation.setCreated_by(createdBy);

                long reservationId =
                        reservationDAO.createReservation(
                                connection, reservation
                        );

                /*
                 * Every segment belongs to the newly created reservation.
                 * If any insertion fails, the entire transaction rolls back.
                 */
                for (BookingSegment segment : requestedSegments) {
                    segment.setReservation_id(reservationId);

                    bookingSegmentDAO.addBookingSegment(
                            connection, segment
                    );
                }

                connection.commit();

                return reservationId;

            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }

    private void validateRequest(
            long createdBy,
            List<BookingSegment> requestedSegments
    ) {
        if (createdBy <= 0) {
            throw new IllegalArgumentException(
                    "createdBy must be a valid positive ID."
            );
        }

        if (requestedSegments == null || requestedSegments.isEmpty()) {
            throw new IllegalArgumentException(
                    "A reservation must contain at least one booking segment."
            );
        }

        for (BookingSegment segment : requestedSegments) {
            if (segment == null) {
                throw new IllegalArgumentException(
                        "A booking segment cannot be null."
                );
            }

            if (segment.getPassenger_id() <= 0
                    || segment.getFlight_id() <= 0
                    || segment.getSeat_id() <= 0) {
                throw new IllegalArgumentException(
                        "Every segment must contain valid passenger, "
                                + "flight, and seat IDs."
                );
            }
        }
    }

    private boolean passengerExists(
            Connection connection,
            long passengerId
    ) throws SQLException {
        Passenger passenger = passengerDAO.getPassengerById(connection, passengerId);
        return passenger != null;
    }

    private Flight getFlightDetails(
            Connection connection,
            long flightId
    ) throws SQLException {
        return flightDAO.findFlightById(connection, flightId);
    }

    private boolean hasActiveBooking(
            Connection connection,
            long passengerId,
            long flightId
    ) throws SQLException {
            return bookingSegmentDAO.hasActiveBookingForPassengerAndFlight(connection
            , passengerId, flightId);
    }

    private boolean isSeatAvailable(
            Connection connection,
            long flightId,
            long seatId
    ) throws SQLException {
        return seatDao.isSeatAvailable(connection, flightId, seatId);
    }

    private Fare getFare(
            Connection connection,
            long flightId,
            long seatId
    ) throws SQLException {
        Fare fare = fareDAO.getFareByFlightIdAndSeatId(connection, flightId, seatId);
        return fare;
    }

    private String generateBookingReference() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
    }
}
