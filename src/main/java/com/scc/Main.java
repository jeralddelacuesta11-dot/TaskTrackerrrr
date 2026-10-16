
package org.example;

import org.example.dao.DatabaseConnection;
import org.example.ui.Menu;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        Menu menu = new Menu();
        menu.start();
    }
}
