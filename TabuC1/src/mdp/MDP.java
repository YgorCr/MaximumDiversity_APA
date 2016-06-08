/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import tabuc1.PaintPanel;
import static tabuc1.TabuC1.WriteImage;

/**
 *
 * @author Igor
 */
public class MDP {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        int N = 1000, M = 10, Dim = 2, Iterations = 2000, intervalStart = 0, intervalEnd = 999, raio = 500;
     
        String inputPath = "/Users/Igor/Desktop/TabuC1_" + 
                "N" + N + "_M" + M + "_I" + Iterations + ".txt";
        
        GenerateInstanceFile(inputPath, N, M, Dim, Iterations, intervalStart, intervalEnd, raio);
        
        Instancia inst = ReadyFile(inputPath);
        
        Point[] result = MDP(inst.Set, M, null);
        
        Ponto[] Sel = new Ponto[result.length];
        for (int i = 0 ; i < Sel.length ; ++i) {
            Sel[i] = new Ponto(result[i].x, result[i].y);
        }
        
        double z = ValueOfSelection(Sel, CalculateDistances(inst.Set), inst.Set);
        System.out.println(z);
        
        WriteImage("/Users/Igor/Desktop/MDP_" + 
                "N" + N + "_M" + M + "_I" + Iterations + ".png", Sel, inst.Set, 1000, 1000, z, true);
    }
    
    public static void RunMDP(int N, int M, int Iterations, int start, int end, int raio, PaintPanel panel) throws Exception{
        String inputPath = "/Users/Igor/Desktop/TabuC1_" + 
                "N" + N + "_M" + M + "_I" + Iterations + ".txt";
        
        GenerateInstanceFile(inputPath, N, M, 2, Iterations, start, end, raio);
        
        Instancia inst = ReadyFile(inputPath);
        
        Point[] result = MDP(inst.Set, M, panel);
        
        Ponto[] Sel = new Ponto[result.length];
        for (int i = 0 ; i < Sel.length ; ++i) {
            Sel[i] = new Ponto(result[i].x, result[i].y);
        }
        
        double z = CalculaZ(Sel);
                
        WriteImage("/Users/Igor/Desktop/MDP_" + 
                "N" + N + "_M" + M + "_I" + Iterations + ".png", Sel, inst.Set, 1000, 1000, z, true);
    }
    
    public static Point[] MDP(Ponto[] set, int m, PaintPanel panel){
        LinkedList<Ponto> result = new LinkedList<>();
        
        if(set.length < 2){
            return result.toArray(new Ponto[0]);
        }
        
        if(m == set.length){
            return set;
        }
        
        switch(m){
            case 0:
                return result.toArray(new Ponto[0]);
            case 1:
                return result.toArray(new Ponto[]{ set[(int)(Math.random()*set.length)] });
        }
        
        double maiorDistancia = 0;
        Ponto MaisDistante1 = null;
        Ponto MaisDistante2 = null;
        for(int i = 0 ; i < set.length ; ++i){
            for(int j = i+1; j < set.length; ++j){
                double distancia = set[i].distance(set[j].x, set[j].y);
                set[i].Insert(set[j], distancia);
                set[j].Insert(set[i], distancia);
                
                if(distancia > maiorDistancia){
                    maiorDistancia = distancia;
                    MaisDistante1 = set[i];
                    MaisDistante2 = set[j];
                }
            }
        }
        
        result.add(MaisDistante1);
        result.add(MaisDistante2);
        for (Ponto p : set) {
            p.Remove(MaisDistante1);
            p.Remove(MaisDistante2);
        }
        
        while(result.size() < m){
            LinkedList<LinkedList<Ponto>> seen = new LinkedList<>();
            for(int j = 0 ; j < result.size() ; ++j){
                LinkedList<Ponto> temp = new LinkedList<>();
                seen.add(temp);
            }
            
            for(int i = 0 ; i < result.get(0).MaisDistantes.size() ; ++i){
                boolean adicionou = false;
                for(int j = 0 ; j < result.size() ; ++j){
                    Ponto current = result.get(j).MaisDistantes.get(i);
                    seen.get(j).add(current);
                    boolean vistoPorTodos = true;
                    for (LinkedList<Ponto> linkedList : seen) {
                        vistoPorTodos = vistoPorTodos && linkedList.contains(current);
                        if(!vistoPorTodos){
                            break;
                        }
                    }
                    if(!vistoPorTodos){
                        continue;
                    }
                    else{
                        result.add(current);
                        for (Ponto p : set) {
                            p.Remove(current);
                        }
                        System.out.println(result.size());
                        adicionou = true;
                        break;
                    }
                }
                if(adicionou){
                    break;   
                }
            }
            if(panel != null){
                BufferedImage img = WriteImage("", result.toArray(new Ponto[result.size()]), set, 1000, 1000, CalculaZ(result.toArray(new Ponto[result.size()])), false);
                panel.setImage(img);
                panel.repaint();
            }
        }
        
        return result.toArray(new Ponto[result.size()]);
    }
    
    public static double CalculaZ(Ponto[] sel){
        double z = 0.0;
        for(int i = 0; i < sel.length ; ++i){
            for(int j = i + 1; j < sel.length ; ++j){
                z += sel[i].distance(sel[j].x, sel[j].y);
            }
        }
        
        return z;
    }
    
    public static double[][] CalculateDistances(Ponto[] Set){
        double[][] result = new double[Set.length][Set.length];
        for(int i = 0 ; i < Set.length ; ++i){
            for(int j = i + 1 ; j < Set.length ; ++j){
                result[i][j] = Set[i].distance(Set[j]);
                result[j][i] = result[i][j];
            }
        }
        
        return result;
    }
    
    public static double ValueOfSelection(Ponto[] Sel, double[][] distances, Ponto[] Set){
        double z = 0;
        for(int i = 0 ; i < Sel.length ; ++i){
            for(int j = i + 1 ; j < Sel.length ; ++j){
                z += distances[i][j];
            }
        }
        
        return z;
    }
    
    public static double ValueOfPonto(double[][] distances, Ponto[] Set, int indexOfP){
        double result = 0;
        
        for(int i = 0 ; i < Set.length ; ++i){
            result += distances[indexOfP][i];
        }
        
        return result;
    }
    
    public static Instancia ReadyFile(String filePath){
        File fEntrada = new File(filePath);
        
        StringBuilder str = new StringBuilder();
        try{
            List<String> lines = Files.readAllLines(fEntrada.toPath(), Charset.forName("UTF-8"));
            for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
                str.append(iterator.next()).append(System.lineSeparator());
            }
        }
        catch(Exception e){ }
        
        String[] lines = str.toString().split(System.lineSeparator());
        String[] instanceParameters = lines[0].split(" ");
        
        Instancia inst = new Instancia();
        inst.N = Integer.parseInt(instanceParameters[0]);
        inst.M = Integer.parseInt(instanceParameters[1]);
        inst.Dimensao = Integer.parseInt(instanceParameters[2]);
        inst.Interactions = Integer.parseInt(instanceParameters[3]);
        inst.Set = new Ponto[inst.N];
        
        int j = 0;
        for(int i = 1; i < lines.length ; ++i){
            String[] coords = lines[i].split(" ");
            inst.Set[j++] = new Ponto(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
        }
        
        return inst;
    }
    
    public static BufferedImage WriteImage(String outputFileName, Ponto[] Sel, Ponto[] Set, int width, int height, double value, boolean writeToFile){
        try {
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gEntrada = bi.createGraphics();
            /*if(sim){
                sim = !sim;
                gEntrada.setPaint(Color.BLACK);
                gEntrada.fillRect(0, 0, width, height);
            }
            else{
                sim = !sim;
                gEntrada.setPaint(Color.WHITE);
                gEntrada.fillRect(0, 0, width, height);
            }*/
            
            gEntrada.setPaint(Color.BLUE);
            for (Ponto point : Set) {
                drawCenteredCircle(gEntrada, (int)point.x, (int)point.y, 7);
            }
            
            gEntrada.setStroke(new BasicStroke(2));
            gEntrada.setPaint(Color.RED);
            for (Ponto de : Sel) {
                for (Ponto para : Sel) {
                    gEntrada.drawLine((int)de.x, (int)de.y, (int)para.x, (int)para.y);
                }
            }
            
            gEntrada.setPaint(Color.GREEN);
            for (Ponto point : Sel) {
                drawCenteredCircle(gEntrada, (int)point.x, (int)point.y, 8);
            }
            
            gEntrada.setFont(new Font("TimesRoman", Font.PLAIN, 40));
            gEntrada.setPaint(Color.WHITE);
            gEntrada.drawString(""+value, 20, 40);
            
            gEntrada.setPaint(Color.BLACK);
            gEntrada.drawString(""+value, 20, 70);
            
            if(writeToFile){
                File outputfile2 = new File(outputFileName);
                ImageIO.write(bi, "png", outputfile2);
            }
            
            return bi;
        } catch (IOException e) {
            
        }
        return null;
    }
    
    private static String GenerateInstanceFile(String outputFile, int N, int M, int Dimensoes, int interactions, int intervalStart, int intervalEnd, double radius) throws IOException{
        File fSaida = new File(outputFile);

        StringBuilder str = new StringBuilder();

        str.append(N).append(" ").append(M).append(" ").append(Dimensoes).append(" ").
                append(interactions).append(System.lineSeparator());

        for(int i = 0 ; i < N ; ++i){
            int[] ponto = new int[Dimensoes];
            double soma = 0;
            for(int j = 0 ; j < Dimensoes ; ++j){
                ponto[j] = (int)(Math.random()*(intervalEnd-intervalStart)+intervalStart);
            }
            
            for(int j = 0 ; j < Dimensoes ; ++j){
                soma += Math.pow(ponto[j] - ((intervalEnd - intervalStart)/2), 2);
            }
            
            if(Math.sqrt(soma) > radius){
                --i;
            }
            else{
                for(int j = 0 ; j < Dimensoes ; ++j){
                    str.append(ponto[j]).append(" ");
                }
                str.append(System.lineSeparator());
            }
        }
        
        String linesArray[] = str.toString().split(System.lineSeparator());
        LinkedList<String> lines = new LinkedList<>();
        for (String line : linesArray) {
            lines.add(line);
        }

        Files.write(fSaida.toPath(), lines, Charset.forName("UTF-8"));

        return outputFile;
    }
        
    public static void drawCenteredCircle(Graphics2D g, int x, int y, int r) {
        x = x-(r/2);
        y = y-(r/2);
        g.fillOval(x,y,r,r);
    }
}

class Ponto extends Point{
    public LinkedList<Ponto> MaisDistantes;
    public LinkedList<java.lang.Double> Distancias;
    
    public Ponto(int x, int y){
        super(x, y);
        MaisDistantes = new LinkedList<>();
        Distancias = new LinkedList<>();
    }
    
    public void Insert(Ponto p, double distance){
        if(MaisDistantes.isEmpty() && Distancias.isEmpty()){
            MaisDistantes.add(p);
            Distancias.add(new java.lang.Double(distance));
        }
        else{
            for(int i = 0 ; i < Distancias.size() ; ++i){
                if(Distancias.get(i) <= distance){
                    Distancias.add(i, distance);
                    MaisDistantes.add(i, p);
                    break;
                }
                if(i == Distancias.size() - 1){
                    Distancias.addLast(distance);
                    MaisDistantes.addLast(p);
                    break;
                }
            }
        }
    }
    
    public void Remove(Ponto p){
        int indexOfP = MaisDistantes.indexOf(p);
        if(indexOfP != -1){
            MaisDistantes.remove(indexOfP);
            Distancias.remove(indexOfP);
        }
    }
}
