/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import model.MacroII;
import org.slf4j.helpers.MessageFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-13
 * @see
 */
public class LogToFile implements LogListener {


    private final LogLevel minimumLogLevel;

    private FileWriter writer;

    private final MacroII model;

    public LogToFile(Path file, LogLevel minimumLogLevel, MacroII model) {

        this.minimumLogLevel = minimumLogLevel;
        this.model = model;
        try {
            Files.deleteIfExists(file);
            Files.createFile(file);
            this.writer = new FileWriter(file.toFile());
        } catch (IOException e) {
            System.err.println("Failed to create file!");
            e.printStackTrace();
        }
    }

    public LogToFile(File file, LogLevel minimumLogLevel, MacroII model) {

        this.minimumLogLevel = minimumLogLevel;
        this.model = model;
        try {
            this.writer = new FileWriter(file);
        } catch (IOException e) {
            System.err.println("Failed to create file!");
            e.printStackTrace();
        }
    }

    /**
     * get notified of a log event!
     *
     * @param logEvent
     */
    @Override
    public void handleNewEvent(LogEvent logEvent)
    {

        if(writer != null && logEvent.getLevel().compareTo(minimumLogLevel)>=0)
        {
            String message =  "date: " + model.getMainScheduleTime() + ", phase: " + model.getCurrentPhase() + " -----> " +
            MessageFormatter.arrayFormat(logEvent.getMessage(),logEvent.getAdditionalParameters()).getMessage() + "\n";

            try {
                System.out.println(message);
                writer.write(message);
                writer.flush();
            }
            catch (Exception e){}
        }




    }
}
