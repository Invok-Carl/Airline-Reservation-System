package com.airline.service;

import com.airline.model.Employee;

public class AuthorizationService {

    public boolean isAdministrator(Employee employee) {
        return employee != null
                && employee.getPosition().equals("ADMINISTRATOR");
    }

    public boolean isReservationAgent(Employee employee) {
        return employee != null
                && employee.getPosition().equals("RESERVATION_AGENT");
    }

    public boolean isAirportStaff(Employee employee) {
        return employee != null
                && employee.getPosition().equals("AIRPORT_STAFF");
    }

    public boolean canAddEmployee(Employee employee) {
        return isAdministrator(employee);
    }

    public boolean canManageEmployees(Employee employee) {
        return isAdministrator(employee);
    }

    public boolean canCreateReservation(Employee employee) {
        return isAdministrator(employee)
                || isReservationAgent(employee);
    }

    public boolean canCheckInPassenger(Employee employee) {
        return isAdministrator(employee)
                || isAirportStaff(employee);
    }
}