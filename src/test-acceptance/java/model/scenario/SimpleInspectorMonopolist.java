package model.scenario;

import model.gui.paper2.MonopolistScenarioFactory;

import javax.swing.*;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 5/22/15.
 */
public class SimpleInspectorMonopolist {


    public  static void main(String[] args)
    {


        MonopolistScenarioFactory inspector = new MonopolistScenarioFactory();

        JFrame frame = new JFrame();
        frame.setContentPane(inspector.getSettingPanel());
   //     frame.setSize(400,800);
        frame.setVisible(true);

    }
}
