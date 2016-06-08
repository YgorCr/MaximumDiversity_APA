/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tabuc1;

import java.awt.geom.Point2D;

public class Ponto extends Point2D{
    public double x;
    public double y;
    
    public Ponto(double x, double y){
        this.x = x;
        this.y = y;
    }
    
    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }
    
    public void setX(double x){
        this.x = x;
    }
    
    public void setY(double y){
        this.y = y;
    }
    
    @Override
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Ponto Clone(){
        return new Ponto(this.x, this.y);
    }
}