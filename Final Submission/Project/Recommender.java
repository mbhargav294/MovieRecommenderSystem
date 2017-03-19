/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;


/**
 *
 * @author mbhargav
 * @author kneha
 */
public class Recommender {

    /**
     * @param args the command line arguments
     */
    static HashMap<Integer, String> movies = new HashMap<Integer, String>();
    static HashMap<Integer, Double> movieRating = new HashMap<Integer, Double>();
    static HashMap<Integer, Integer> movieViewers = new HashMap<Integer, Integer>();

    static HashMap<Integer, HashMap<Integer, Double>> ratings = new HashMap<Integer, HashMap<Integer, Double>>();
    

    static Map<Double, List<Integer>> nearest = new TreeMap<Double, List<Integer>>();
    static Map<Double, List<String>> recomendedMovies = new TreeMap<Double, List<String>>();
    
    public static void initialize() throws FileNotFoundException
    {
        Scanner scan = new Scanner(new FileReader("./ml-10M100K/movies.dat"));
        while(scan.hasNextLine())
        {
            String[] movie = scan.nextLine().split("::");
            movies.put(Integer.parseInt(movie[0]), movie[1]);
        }

        scan = new Scanner(new FileReader("./ml-10M100K/ratings.dat"));
        while(scan.hasNextLine())
        {
            String[] rating = scan.nextLine().split("::");
            int user = Integer.parseInt(rating[0]);
            int movie = Integer.parseInt(rating[1]);
            double mRating = Double.parseDouble(rating[2]);
            if(ratings.containsKey(user))
            {
                ratings.get(user).put(movie, mRating);
            }
            else
            {
                HashMap<Integer, Double> first = new HashMap<Integer, Double>();
                first.put(movie, mRating);
                ratings.put(user, first);
            }
            if(movieRating.containsKey(movie))
            {
                movieRating.put(movie, movieRating.get(movie) + mRating);
                movieViewers.put(movie, movieViewers.get(movie) + 1);
            }
            else
            {
                movieRating.put(movie, mRating);
                movieViewers.put(movie, 1);
            }
        }
        
        for(int i:movieRating.keySet())
        {
            movieRating.put(i, -1 * movieRating.get(i)/movieViewers.get(i));
        }
    }
    
    public static Map<Double, List<String>> findRecomendations(int usr)
    {
        nearest.clear();
        recomendedMovies.clear();
        if(!ratings.containsKey(usr))
            return null;
        HashMap<Integer, Double> ourUserMovies = ratings.get(usr);
        for(int i:ratings.keySet())
        {
            if(i == usr)
                continue;
            HashMap<Integer, Double> currUserMovies = ratings.get(i);
            double distance = 0.0;
            for(int j:ourUserMovies.keySet())
            {
                if(currUserMovies.containsKey(j))
                    distance += Math.abs((ourUserMovies.get(j) - currUserMovies.get(j)) * (ourUserMovies.get(j) - currUserMovies.get(j)));
            }
            if(distance == 0.0)
                distance = Double.MAX_VALUE;
            distance *= -1;
            if(nearest.containsKey(distance))
            {
                nearest.get(distance).add(i);
            }
            else
            {
                ArrayList<Integer> ar = new ArrayList<Integer>();
                ar.add(i);
                nearest.put(distance, ar);
            }
            
            //System.out.println(i + "->" + Math.sqrt(distance));
        }
        
        int k = 20;
        Set<Integer> unseen = new HashSet<Integer>();
        ArrayList<Integer> kNearestNeighbours = new ArrayList<Integer>();
        for(double i:nearest.keySet())
        {
            if(kNearestNeighbours.size()>=k)
                break;
            for(int j : nearest.get(i))
            {
                kNearestNeighbours.add(j);
                unseen.addAll(ratings.get(j).keySet());
                if(kNearestNeighbours.size()>=k)
                    break;
            }
        }
        unseen.removeAll(ourUserMovies.keySet());
        
        for(int i : unseen)
        {
            if(recomendedMovies.containsKey(movieRating.get(i)))
            {
                recomendedMovies.get(movieRating.get(i)).add(movies.get(i));
            }
            else
            {
                ArrayList<String> ar = new ArrayList<String>();
                ar.add(movies.get(i));
                recomendedMovies.put(movieRating.get(i), ar);
            }
        }
        return recomendedMovies;
    }
}