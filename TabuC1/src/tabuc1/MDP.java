package tabuc1;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import javax.imageio.ImageIO;

public class MDP {
        static double KAPPA = 0.5;
        static int nIter = 5;
        
        
        public static void GRASPC1(int n, int m, int iterations, int start, int end, int raio, PaintPanel panel)
            {
            String filename = "/Users/Igor/Desktop/TabuC1_" + 
                    "N" + n + "_M" + m + "_I" + iterations + ".txt"; 
            //gera arquivo teste
            try
            {
                GenerateInstanceFile(filename, n, m, 2, iterations, start, end, raio);
            }
            catch(Exception e)
            {
            }
            Random rng = new Random(System.currentTimeMillis());
            // TODO Auto-generated method stub
            ArrayList<LinkedList<Double>> points = ReadPoints(filename);
            LinkedList<Double> centroid = calcCentroid(points);
            
            nIter = readParameter("Iterations", filename);
            int maxTolerance = (int)Math.ceil((float)nIter/5);            
            
            boolean hasImproved = false;
            
            System.out.println("Centroid do SET: " + centroid.toString());
            
            double bestKnown = Double.NEGATIVE_INFINITY;
            double worstKnown = Double.POSITIVE_INFINITY;
            
            for (int iter = 0, aux = maxTolerance; iter < nIter; iter++, aux--)
            {   
                if (aux == 0){
                    // aumenta o KAPPA (fica mais exigente!)
                    
                    if (KAPPA < 0.9)
                        KAPPA += 0.1;                  
                    
                    aux = maxTolerance;
                    
                }
                Double[] dists = new Double[points.size()];                   
                double d_max, d_min;

                //cálculo de d(s_i, s_c)
                for (int i = 0; i < points.size(); i++)
                {
                    dists[i] = calcDist(points.get(i), centroid);                    
                }

                d_max = Double.NEGATIVE_INFINITY;
                d_min = Double.POSITIVE_INFINITY;

                //determinação de d_max e d_min
                ArrayList<LinkedList<Double>> selected_points = new ArrayList<>();

                while( selected_points.size() < m)
                {
                    ArrayList<Integer> rcl = new ArrayList<>();
                    //finding d_max, d_min
                    for (Double dist : dists) {
                        if (dist < 0) {
                            continue;
                        }
                        if (dist > d_max) {
                            d_max = dist;
                        }
                        if (dist < d_min) {
                            d_min = dist;
                        }
                    }
                    //System.out.println("< 1 : " + d_max + " " + d_min);
                    for (int i = 0; i < dists.length; i++)
                    {
                        if (dists[i] < 0)
                            continue;
                        // é bom candidato?
                        if (dists[i] >= (d_min + KAPPA*(d_max - d_min)))
                            rcl.add(i);
                    }

                    //seleciona um elemento entre os melhores candidatos
                    int chosen_one = rng.nextInt(rcl.size());

                    // adiciona ao conjunto de pontos selecionados
                    selected_points.add(points.get(rcl.get(chosen_one)));

                    //remove do conjunto S
                    dists[rcl.get(chosen_one)] = -1.0;

                    //recalcula o centróide, mas agora baseado no conjunto de es-
                    //colhidos

                    centroid = calcCentroid(selected_points);

                    //recálculo de d(s_i, s_c)
                    for (int i = 0; i < points.size(); i++)
                    {   
                        // elemento está presente no rcl
                        if (dists[i] < 0)
                            continue;

                        dists[i] = calcDist(points.get(i), centroid);                    
                    }

                    d_max = Double.NEGATIVE_INFINITY;
                    d_min = Double.POSITIVE_INFINITY;
                    //limpa candidatos para próxima iteração
                    rcl.clear();

                    //System.out.println("Size: " + selected_points.toString());
                }
                BufferedImage b = WriteImage("solution_" + iter + "_.png", selected_points, points, calcSolValue(selected_points));
                //System.out.println("Total: " + calcSolValue(selected_points) + " com KAPPA = " + KAPPA);
                double totalSol = calcSolValue(selected_points);
                
                if (totalSol > bestKnown){
                    bestKnown = totalSol;
                    aux = maxTolerance;
                }
                
                if (totalSol < worstKnown){
                    worstKnown = totalSol;
                }
                if(panel != null)
                {
                    panel.setImage(b);
                    panel.repaint();
                }
            }            
        }
        
	public static void main(String[] args)
	{
            String filename = "TabuC1_N1000_M10_I2000.txt";   
            //gera arquivo teste
            try
            {
                //GenerateInstanceFile("3000_50.dat", 1000, 4, 2, 10000, 0, 1000);
            }
            catch(Exception e)
            {
            }
            Random rng = new Random(System.currentTimeMillis());
            // TODO Auto-generated method stub
            ArrayList<LinkedList<Double>> points = ReadPoints(filename);
            LinkedList<Double> centroid = calcCentroid(points);
            int m = readParameter("M", filename);
            
            nIter = readParameter("Iterations", filename);
            int maxTolerance = (int)Math.ceil((float)nIter/5);            
            
            boolean hasImproved = false;
            
            System.out.println("Centroid do SET: " + centroid.toString());
            
            double bestKnown = Double.NEGATIVE_INFINITY;
            double worstKnown = Double.POSITIVE_INFINITY;
            ArrayList<LinkedList<Double>> best_solution = null;
            ArrayList<LinkedList<Double>> worst_solution = null;
            
            for (int iter = 0, aux = maxTolerance; iter < nIter; iter++, aux--)
            {   
                if (aux == 0){
                    // aumenta o KAPPA (fica mais exigente!)
                    
                    if (KAPPA < 0.9)
                        KAPPA += 0.1;                  
                    
                    aux = maxTolerance;
                    
                }
                Double[] dists = new Double[points.size()];                   
                double d_max, d_min;

                //cálculo de d(s_i, s_c)
                for (int i = 0; i < points.size(); i++)
                {
                    dists[i] = calcDist(points.get(i), centroid);                    
                }

                d_max = Double.NEGATIVE_INFINITY;
                d_min = Double.POSITIVE_INFINITY;

                //determinação de d_max e d_min
                ArrayList<LinkedList<Double>> selected_points = new ArrayList<>();

                while( selected_points.size() < m)
                {
                    ArrayList<Integer> rcl = new ArrayList<>();
                    //finding d_max, d_min
                    for (Double dist : dists) {
                        if (dist < 0) {
                            continue;
                        }
                        if (dist > d_max) {
                            d_max = dist;
                        }
                        if (dist < d_min) {
                            d_min = dist;
                        }
                    }
                    //System.out.println("< 1 : " + d_max + " " + d_min);
                    for (int i = 0; i < dists.length; i++)
                    {
                        if (dists[i] < 0)
                            continue;
                        // é bom candidato?
                        if (dists[i] >= (d_min + KAPPA*(d_max - d_min)))
                            rcl.add(i);
                    }

                    //seleciona um elemento entre os melhores candidatos
                    int chosen_one = rng.nextInt(rcl.size());

                    // adiciona ao conjunto de pontos selecionados
                    selected_points.add(points.get(rcl.get(chosen_one)));

                    //remove do conjunto S
                    dists[rcl.get(chosen_one)] = -1.0;

                    //recalcula o centróide, mas agora baseado no conjunto de es-
                    //colhidos

                    centroid = calcCentroid(selected_points);

                    //recálculo de d(s_i, s_c)
                    for (int i = 0; i < points.size(); i++)
                    {   
                        // elemento está presente no rcl
                        if (dists[i] < 0)
                            continue;

                        dists[i] = calcDist(points.get(i), centroid);                    
                    }

                    d_max = Double.NEGATIVE_INFINITY;
                    d_min = Double.POSITIVE_INFINITY;
                    //limpa candidatos para próxima iteração
                    rcl.clear();

                    //System.out.println("Size: " + selected_points.toString());
                }
                //WriteImage("solution_" + iter + "_.png", selected_points, points);
                System.out.println("Total: " + calcSolValue(selected_points) + " com KAPPA = " + KAPPA);
                double totalSol = calcSolValue(selected_points);
                
                if (totalSol > bestKnown){
                    bestKnown = totalSol;
                    best_solution = selected_points;
                    aux = maxTolerance;
                }
                
                if (totalSol < worstKnown){
                    worstKnown = totalSol;
                    worst_solution = selected_points;
                }
                
            }
            System.out.print("Melhor solução: " + bestKnown);
            WriteImage("bestsolution.png", best_solution, points, bestKnown);
            WriteImage("worstsolution.png", worst_solution, points, worstKnown);
	}
	
        private static int readParameter(String param, String filename)
        {
            String firstLine = null;
            try (BufferedReader br = new BufferedReader(new FileReader(filename)))
            {
               firstLine = br.readLine();
            }catch(Exception e){}
            
            switch(param)
            {
                case "N":
                    return Integer.parseInt(firstLine.split(" ")[0]);
                case "M":
                    return Integer.parseInt(firstLine.split(" ")[1]);
                case "Iterations":
                    return Integer.parseInt(firstLine.split(" ")[3]);
                case "Dimension":
                    return Integer.parseInt(firstLine.split(" ")[2]);
                default:
                    return 0;
            }
        }
        
        public static void drawCenteredCircle(Graphics2D g, int x, int y, int r) {
            x = x-(r/2);
            y = y-(r/2);
            g.fillOval(x,y,r,r);
        }
        
        public static BufferedImage WriteImage(String outputFileName, ArrayList<LinkedList<Double>> Sel, ArrayList<LinkedList<Double>> Set, double value){
            
            BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gEntrada = bi.createGraphics();

            gEntrada.setPaint(Color.BLUE);
            for (LinkedList<Double> point : Set) {
                drawCenteredCircle(gEntrada, point.get(0).intValue(), point.get(1).intValue(), 7);
            }

            gEntrada.setStroke(new BasicStroke(3));
            gEntrada.setPaint(Color.RED);
            for (LinkedList<Double> de : Sel) {
                for (LinkedList<Double> para : Sel) {
                    gEntrada.drawLine(de.get(0).intValue(), de.get(1).intValue(), para.get(0).intValue(), para.get(1).intValue());
                }
            }

            gEntrada.setPaint(Color.GREEN);
            for (LinkedList<Double> point : Sel) {
                drawCenteredCircle(gEntrada, point.get(0).intValue(), point.get(1).intValue(), 8);
            }

            gEntrada.setPaint(Color.GREEN);
            for (LinkedList<Double> point : Sel) {
                drawCenteredCircle(gEntrada, point.get(0).intValue(), point.get(1).intValue(), 8);
            }

            gEntrada.setFont(new Font("TimesRoman", Font.PLAIN, 40));
            gEntrada.setPaint(Color.WHITE);
            gEntrada.drawString(""+value, 20, 40);

            gEntrada.setPaint(Color.BLACK);
            gEntrada.drawString(""+value, 20, 70);

            //File outputfile2 = new File(outputFileName);
            //ImageIO.write(bi, "png", outputfile2); 
            return bi;
            
        }
        
        
        private static double calcSolValue(ArrayList<LinkedList<Double>> solution)
        {
            Double total = 0.0;
            //calcular as distancias
            for (int i = 0; i < solution.size(); i++)
            {
                for (int j = i; j < solution.size(); j++)
                {
                    if (i != j)
                    {
                        total+= calcDist(solution.get(i), solution.get(j));
                    }                       
                }
            }
            
            return total;
        }
	private static ArrayList<LinkedList<Double>> ReadPoints(String filename)
	{
		ArrayList<LinkedList<Double>> points = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename)))
		{
		    String line;		    
		    br.readLine();
		    while ((line = br.readLine()) != null) 
		    {
		       // process the line.
		    	String[] coordinates = line.split(" ");
		    	LinkedList<Double> point = new LinkedList<>();
		    	
		    	for(String cor: coordinates)
		    	{
		    		point.add(Double.parseDouble(cor));
		    	}
		    	
		    	points.add(point);
		    }
		    
		    System.out.printf("Foram lidos %d pontos.\n", points.size());
		    
		    return points;
		}
		catch(Exception ex)
		{	
                    System.out.printf("Message %s", ex.getStackTrace());
                    System.exit(0);
		}
		
		return null;
	}
	
	private static Double calcDist(LinkedList<Double> p1, LinkedList<Double> p2)
	{
		double total = 0;
		
		Iterator<Double> p1Iter, p2Iter;
		
		p1Iter = p1.iterator();
		p2Iter = p2.iterator();
		
		while(p1Iter.hasNext())
		{
			total += Math.pow((double)(p1Iter.next() - p2Iter.next()), 2.0);
		}
		
		total = Math.sqrt(total);
		
		return total;
	}
        
        private static LinkedList<Double> calcCentroid(ArrayList<LinkedList<Double>> points)
        {
            double total = points.size();
            int dim = points.get(0).size();
            ArrayList<Double> centroid = new ArrayList<Double>();
            
            for (int i = 0; i < dim; i++)
            {
                centroid.add(0.0);
            }
            
            for(LinkedList<Double> p : points)
            {
                Iterator<Double> p1 = p.iterator();
                
                for (int i = 0; i < centroid.size(); i++)
                {
                    centroid.set(i, centroid.get(i) + p1.next().intValue());
                }
            }
            
            LinkedList<Double> retCentroid = new LinkedList<>();
            
            for (int i = 0; i < centroid.size(); i++)
            {
                retCentroid.add(centroid.get(i)/total);
            }
            
            return retCentroid;
        }
           
        public static void WriteImage(String outputFileName, ArrayList<LinkedList<Double>> Sel, ArrayList<LinkedList<Double>> Set){
        try {
            BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gEntrada = bi.createGraphics();
            gEntrada.setPaint(Color.BLUE);
            
            for (LinkedList<Double> point : Set) {
                bi.setRGB(point.get(0).intValue(), point.get(1).intValue(), Color.blue.getRGB());
            }
            
            gEntrada.setPaint(Color.RED);
            for (LinkedList<Double> de : Sel) {
                for (LinkedList<Double> para : Sel) {
                    gEntrada.drawLine(de.get(0).intValue(), de.get(1).intValue(), para.get(0).intValue(), para.get(1).intValue());
                }
            }
            
            for (LinkedList<Double> point : Sel) {
                bi.setRGB(point.get(0).intValue(), point.get(1).intValue(), Color.GREEN.getRGB());;
            }
            
            File outputfile2 = new File(outputFileName);
            ImageIO.write(bi, "png", outputfile2); 
        } catch (IOException e) {
            
        }
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

}
