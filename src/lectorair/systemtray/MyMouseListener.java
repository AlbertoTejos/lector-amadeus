
package lectorair.systemtray;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import lectorair.systemtray.listeners.*;


public class MyMouseListener implements MouseListener{

    private PopupMenu popup = new PopupMenu();

    public PopupMenu getPopup() {
        return popup;
    }


    public MyMouseListener() {

        
        MenuItem ItemRestaurar = new MenuItem("Configurar");
        popup.add(ItemRestaurar);
        ItemRestaurar.addActionListener(new ConfiguracionListener());
        
        
        MenuItem SalirItem = new MenuItem("Salir");
        popup.add(SalirItem);
        SalirItem.addActionListener(new ExitListener());
        
    }
    
    

    @Override
    public void mouseClicked(MouseEvent e) {   
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    

}
