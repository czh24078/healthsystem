package com.healthsys.ui.debug;

import com.healthsys.dao.DoctorDAO;
import com.healthsys.common.entity.Doctor;

public class DbChecker {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: DbChecker <username> <password>");
            System.exit(1);
        }
        String username = args[0];
        String password = args[1];

        DoctorDAO dao = new DoctorDAO();
        Doctor d = dao.getByUsername(username);
        if (d == null) {
            System.out.println("Doctor not found for username=" + username);
            System.exit(2);
        }
        System.out.println("Found doctor: id=" + d.getDoctorId() + ", username=" + d.getUsername() + ", name=" + d.getName());
        System.out.println("Stored password hash: '" + d.getPasswordHash() + "'");
        if (password.equals(d.getPasswordHash())) {
            System.out.println("Password match: OK");
            System.exit(0);
        } else {
            System.out.println("Password match: FAIL");
            System.exit(3);
        }
    }
}
