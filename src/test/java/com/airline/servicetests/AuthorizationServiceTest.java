package com.airline.servicetests;



import com.airline.model.Employee;
import com.airline.service.AuthorizationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationServiceTest {

    private AuthorizationService authorizationService;

    private Employee administrator;
    private Employee reservationAgent;
    private Employee airportStaff;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();

        administrator = createEmployee("ADMINISTRATOR");
        reservationAgent = createEmployee("RESERVATION_AGENT");
        airportStaff = createEmployee("AIRPORT_STAFF");
    }

    private Employee createEmployee(String position) {
        return new Employee(
                1L,
                "Test",
                "Employee",
                position,
                "0700000000",
                "test@example.com",
                "ACTIVE",
                "testuser",
                "testPasswordHash"
        );
    }

    // =========================================
    // Role identification
    // =========================================

    @Test
    void isAdministratorReturnsTrueForAdministrator() {
        assertTrue(authorizationService.isAdministrator(administrator));
    }

    @Test
    void isAdministratorReturnsFalseForOtherRoles() {
        assertFalse(authorizationService.isAdministrator(reservationAgent));
        assertFalse(authorizationService.isAdministrator(airportStaff));
    }

    @Test
    void isReservationAgentReturnsTrueForReservationAgent() {
        assertTrue(authorizationService.isReservationAgent(reservationAgent));
    }

    @Test
    void isReservationAgentReturnsFalseForOtherRoles() {
        assertFalse(authorizationService.isReservationAgent(administrator));
        assertFalse(authorizationService.isReservationAgent(airportStaff));
    }

    @Test
    void isAirportStaffReturnsTrueForAirportStaff() {
        assertTrue(authorizationService.isAirportStaff(airportStaff));
    }

    @Test
    void isAirportStaffReturnsFalseForOtherRoles() {
        assertFalse(authorizationService.isAirportStaff(administrator));
        assertFalse(authorizationService.isAirportStaff(reservationAgent));
    }

    // =========================================
    // Employee management permissions
    // =========================================

    @Test
    void onlyAdministratorCanAddEmployees() {
        assertTrue(authorizationService.canAddEmployee(administrator));
        assertFalse(authorizationService.canAddEmployee(reservationAgent));
        assertFalse(authorizationService.canAddEmployee(airportStaff));
    }

    @Test
    void onlyAdministratorCanManageEmployees() {
        assertTrue(authorizationService.canManageEmployees(administrator));
        assertFalse(authorizationService.canManageEmployees(reservationAgent));
        assertFalse(authorizationService.canManageEmployees(airportStaff));
    }

    // =========================================
    // Reservation permissions
    // =========================================

    @Test
    void administratorAndReservationAgentCanCreateReservations() {
        assertTrue(authorizationService.canCreateReservation(administrator));
        assertTrue(authorizationService.canCreateReservation(reservationAgent));
        assertFalse(authorizationService.canCreateReservation(airportStaff));
    }

    // =========================================
    // Flight management permissions
    // =========================================

    @Test
    void onlyAdministratorCanAddFlights() {
        assertTrue(authorizationService.canAddFlight(administrator));
        assertFalse(authorizationService.canAddFlight(reservationAgent));
        assertFalse(authorizationService.canAddFlight(airportStaff));
    }

    @Test
    void onlyAdministratorCanDeleteFlights() {
        assertTrue(authorizationService.canDeleteFlight(administrator));
        assertFalse(authorizationService.canDeleteFlight(reservationAgent));
        assertFalse(authorizationService.canDeleteFlight(airportStaff));
    }

    @Test
    void onlyAdministratorCanAddFlightSegments() {
        assertTrue(authorizationService.canAddFlightSeg(administrator));
        assertFalse(authorizationService.canAddFlightSeg(reservationAgent));
        assertFalse(authorizationService.canAddFlightSeg(airportStaff));
    }

    // =========================================
    // Passenger check-in permissions
    // =========================================

    @Test
    void administratorAndAirportStaffCanCheckInPassengers() {
        assertTrue(authorizationService.canCheckInPassenger(administrator));
        assertTrue(authorizationService.canCheckInPassenger(airportStaff));
        assertFalse(authorizationService.canCheckInPassenger(reservationAgent));
    }

    // =========================================
    // Null employee handling
    // =========================================

    @Test
    void allPermissionMethodsReturnFalseForNullEmployee() {
        assertFalse(authorizationService.isAdministrator(null));
        assertFalse(authorizationService.isReservationAgent(null));
        assertFalse(authorizationService.isAirportStaff(null));

        assertFalse(authorizationService.canAddEmployee(null));
        assertFalse(authorizationService.canManageEmployees(null));
        assertFalse(authorizationService.canCreateReservation(null));

        assertFalse(authorizationService.canAddFlight(null));
        assertFalse(authorizationService.canDeleteFlight(null));
        assertFalse(authorizationService.canAddFlightSeg(null));

        assertFalse(authorizationService.canCheckInPassenger(null));
    }
}
