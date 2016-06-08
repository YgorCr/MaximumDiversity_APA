/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tabuc1;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Igor
 */
public class TabuC1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        int N = 2000, M = 200, Dim = 2, Iterations = 1000, intervalStart = 0, intervalEnd = 999;
        
        String inputPath = "/Users/Igor/Desktop/TabuC1_" + 
                "N" + N + "_M" + M + "_I" + Iterations + ".txt";
        
        GenerateInstanceFile(inputPath, N, M, Dim, Iterations, intervalStart, intervalEnd, 500);
        
        Instancia inst = ReadyFile(inputPath);
        Ponto p = s_center(inst.Set);
        
        Ponto[] Selection = TabuC1(inst, null);
        
        double z = CalculaZ(Selection);
        System.out.println(z);
        
        WriteImage("/Users/Igor/Desktop/TabuC1_" + 
                "N" + N + "_M" + M + "_I" + Iterations + ".png", Selection, inst.Set, 1000, 1000, z, true);
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
    
    public static void RunTabu(int N, int M, int Iterations, int start, int end, int raio, PaintPanel panel) throws Exception{
        String inputPath = "/Users/Igor/Desktop/TabuC1_" + 
                "N" + N + "_M" + M + "_I" + Iterations + ".txt";
        
        GenerateInstanceFile(inputPath, N, M, 2, Iterations, start, end, raio);
        
        Instancia inst = ReadyFile(inputPath);
        Ponto p = s_center(inst.Set);
        
        Ponto[] Selection = TabuC1(inst, panel);
        
        double z = ValueOfSelection(Selection, CalculateDistances(Selection), Selection);
        System.out.println(z);
        
        WriteImage("/Users/Igor/Desktop/TabuC1_" + 
                "N" + N + "_M" + M + "_I" + Iterations + ".png", Selection, inst.Set, 1000, 1000, z, true);
    }
    
    public static final double Beta = 0.1;
    public static final double Kappa = 0.0001;
    public static Ponto[] TabuC1(Instancia inst, PaintPanel panel){
        int[] freq = new int[inst.Set.length];
        int maxFreq = 1;
        double maxQuality = 1;
        double[] quality = new double[inst.Set.length];
        double[][] distances = CalculateDistances(inst.Set);
        Ponto[] Sel = null;
        
        for(int iteractions = 0 ; iteractions < inst.Interactions ; ++iteractions){
            Ponto[] set = new Ponto[inst.Set.length];
            for (int i = 0 ; i < set.length ; ++i) {
                set[i] = inst.Set[i].Clone();
            }
            
            Ponto centro = s_center(set);
            Sel = new Ponto[inst.M];
            int indexOfNextSel = 0;
            while(indexOfNextSel < inst.M){
                double maxDistanceToCenter = -1;
                int indexOfP = -1;
                double range = Range(set, centro);
                for (int j = 0; j < set.length ; ++j) {
                    if(set[j] != null){
                        double distanceToCenter = set[j].distance(centro);
                        double dLinha = distanceToCenter -
                                Beta*range*(((double)freq[j])/((double)maxFreq)) +
                                Kappa*range*(quality[j]/maxQuality);
                        if(dLinha > maxDistanceToCenter){
                            maxDistanceToCenter = dLinha;
                            indexOfP = j;
                        }
                    }
                }
                Sel[indexOfNextSel++] = set[indexOfP];
                freq[indexOfP]++;
                if(freq[indexOfP] > maxFreq){
                    maxFreq = freq[indexOfP];
                }
                set[indexOfP] = null;
                centro = s_center(Sel);
            }
            double z = ValueOfSelection(Sel, distances, set);
            
            for(int j = 0 ; j < set.length ; ++j){
                if(set[j] == null){
                    quality[j] = (quality[j]*(freq[j] - 1) + z)/(freq[j]);
                    if(quality[j] > maxQuality){
                        maxQuality = quality[j];
                    }
                }
            }
            if(panel != null){
                BufferedImage img = WriteImage("", Sel, inst.Set, 1000, 1000, z, false);
                panel.setImage(img);
                panel.repaint();
            }
        }
        
        return Sel;
    }
    
    public static double Range(Ponto[] Set, Ponto centro){
        double maxDist =  -1;
        double minDist = -1;
        for(int i = 0 ; i < Set.length ; ++i){
            if(Set[i] != null){
                double dist = Set[i].distance(centro.x, centro.y);
                if(dist > maxDist){
                    maxDist = dist;
                }
                if(dist < minDist){
                    minDist = dist;
                }
            }
        }
        
        return maxDist - minDist;
    }
    
    public static Ponto s_center(Ponto[] Set){
        Ponto result = new Ponto(0, 0);
        
        for (Ponto point : Set) {
            if(point != null){
                result.setX(result.x + point.y);
                result.setY(result.y + point.y);
            }
        }
        
        result.setX(result.x/Set.length);
        result.setY(result.y/Set.length);
        return result;
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
        for(int i = 0 ; i < Set.length ; ++i){
            if(Set[i] == null){
                for(int j = i + 1 ; j < Set.length ; ++j){
                    if(Set[j] == null){
                        z += distances[i][j];
                    }
                }
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
    
    public static boolean sim = false;
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
