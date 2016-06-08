/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tabuc1;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Igor
 */
public class PaintPanel extends JPanel{
    private BufferedImage img;

    public PaintPanel(BufferedImage img) {
        this.img = img;
    }
    
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        
        if(this.img != null){
            g.drawImage(this.img, 0, 0, this);
        }
    }
    
    public void setImage(BufferedImage img){
        this.img = img;
        this.repaint();
    }
    
    public BufferedImage getImage(){
        return this.img;
    }
}