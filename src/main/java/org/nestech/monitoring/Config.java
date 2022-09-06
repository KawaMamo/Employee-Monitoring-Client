package org.nestech.monitoring;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config {

    Properties prop = new Properties();
    String fileName = "app.config";

    public Config(String fileName) throws IOException {
        FileReader reader=new FileReader(fileName);
        prop.load(reader);
    }

    public void write() throws IOException {
        prop.store(new FileWriter(fileName), "#properties list");
    }

    public Properties getProp(){
        return prop;
    }


}
