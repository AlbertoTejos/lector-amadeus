
package lectorair.systemtray.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import lectorair.configurar.FrameConfiguracion;


public class ConfiguracionListener implements ActionListener{

    @Override
    public void actionPerformed(ActionEvent e) {
        FrameConfiguracion frame= new FrameConfiguracion();
    }
    
}
