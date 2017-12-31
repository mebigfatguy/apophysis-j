package org.apophysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class JSStringList implements Constants {

    /*****************************************************************************/
    // FIELDS

    private List<String> lines = new ArrayList<>();

    /*****************************************************************************/

    public JSStringList() {
    }

    /*****************************************************************************/

    public void add(String line) {
        lines.add(line);
    }

    /*****************************************************************************/

    public void saveToFile(String filename) {
        try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            for (String line : lines) {
                w.println(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*****************************************************************************/

    public void loadFromFile(String filename) throws IOException {
        lines = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new FileReader(filename))) {
            while (true) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                lines.add(line);
            }
        }
    }

    /*****************************************************************************/

} // End of class JSStringList
