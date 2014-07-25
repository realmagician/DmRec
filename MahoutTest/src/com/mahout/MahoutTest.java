package com.mahout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import dmfmrec.sinaapp.mahout.MahoutEntry;
import dmfmrec.sinaapp.mahout.model.MostSimilarItem;

public class MahoutTest
{
    class UserBasedRecommenderBuilder implements RecommenderBuilder
    {
        public Recommender buildRecommender(DataModel model) throws TasteException
        {
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            return new GenericUserBasedRecommender(model, neighborhood, similarity);
        }

    }

    class ItemBasedRecommenderBuilder implements RecommenderBuilder
    {
        public Recommender buildRecommender(DataModel model) throws TasteException
        {
            ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
            return new GenericItemBasedRecommender(model, similarity);
        }

    }

    public void SimpleTestFoo()
    {
        try
        {
            DataModel dataModel = new FileDataModel(new File("uMahout.data"));
            // test for user based cf
            UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
            System.out.println(dataModel.getMaxPreference());
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
            ArrayList<RecommendedItem> recomendations = (ArrayList<RecommendedItem>) recommender.recommend(2, 3);
            for (RecommendedItem recommendedItem : recomendations)
            {
                System.out.println(recommendedItem);
            }

            // test for item based cf
            ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
            ItemBasedRecommender recommenderItemBased = new GenericItemBasedRecommender(dataModel, itemSimilarity);
            ArrayList<RecommendedItem> recommendedItems = (ArrayList<RecommendedItem>) recommenderItemBased.recommend(2, 3);
            for (RecommendedItem recommendedItem : recommendedItems)
            {
                System.out.println(recommendedItem);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void CFTestWithEvaluationFoo()
    {
        // test the result of cf
        try
        {
            DataModel dataModel = new FileDataModel(new File("uMahout.data"));
            RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
            RecommenderBuilder builder = new UserBasedRecommenderBuilder();
            double result = evaluator.evaluate(builder, null, dataModel, 0.9, 0.1);
            System.out.println(result);

            RecommenderBuilder builder2 = new ItemBasedRecommenderBuilder();
            result = evaluator.evaluate(builder2, null, dataModel, 0.9, 0.1);
            System.out.println(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class mostSimilarItem
    {
        public int itemId;
        public double similarity;

        public mostSimilarItem(int itemId, double similarity)
        {
            this.itemId = itemId;
            this.similarity = similarity;
        }
    }

    class MovieItem
    {
        public String title;
        public ArrayList<Integer> genres = new ArrayList<Integer>(19);
    }

    public void ContentBasedFoo()
    {
        try
        {
            // read movie info
            File genreFile = new File("../MockData/u.genre");
            InputStreamReader genreInStream = new InputStreamReader(new FileInputStream(genreFile));
            BufferedReader genreReader = new BufferedReader(genreInStream);
            HashMap<Integer, String> genreMap = new HashMap<Integer, String>();
            while (true)
            {
                String line = genreReader.readLine();
                if (line == null)
                {
                    break;
                }

                String[] strs = line.split("\\|");
                if (strs.length < 2)
                {
                    continue;
                }

                genreMap.put(Integer.valueOf(strs[1]), strs[0]);
            }

            genreReader.close();

            File itemsFile = new File("../MockData/u.item");
            InputStreamReader itemsInStream = new InputStreamReader(new FileInputStream(itemsFile));
            BufferedReader itemsReader = new BufferedReader(itemsInStream);
            HashMap<Integer, MovieItem> itemsMap = new HashMap<Integer, MovieItem>();
            while (true)
            {
                String line = itemsReader.readLine();
                if (line == null)
                {
                    break;
                }

                String[] strs = line.split("\\|");
                if (strs.length < 24)
                {
                    continue;
                }

                MovieItem movieItem = new MovieItem();
                movieItem.title = strs[1];
                for (int i = 5; i < strs.length; i++)
                {
                    movieItem.genres.add(Integer.valueOf(strs[i]));
                }

                itemsMap.put(Integer.valueOf(strs[0]), movieItem);
            }

            itemsReader.close();

            // test contended based
            DataModel model = new FileDataModel(new File("../MockData/uMahout_withgenre.data"));
            // UserSimilarity similarity = new
            // EuclideanDistanceSimilarity(model);
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            similarity.setPreferenceInferrer(new PreferenceInferrer()
            {
                public void refresh(Collection<Refreshable> alreadyRefreshed)
                {
                }

                public float inferPreference(long userID, long itemID) throws TasteException
                {
                    // set 0 if there is no preference from userID to itemID,
                    // or UserSimilarity will ignore the difference
                    // between corresponding userIDs
                    return 0;
                }
            });

            // int[] testCountIds = { 1, 3, 4, 10, 98, 80, 34, 78, 98, 11 };
            int testContentId = 97;
            int itemsCount = itemsMap.size();
            ArrayList<mostSimilarItem> mostSimilar = new ArrayList<mostSimilarItem>();
            for (int i = 1; i <= itemsCount; i++)
            {
                if (testContentId == i)
                {
                    continue;
                }

                double sim = similarity.userSimilarity(testContentId, i);
                mostSimilar.add(new mostSimilarItem(i, sim));
            }

            // TODO: use heap instead
            Collections.sort(mostSimilar, new Comparator<mostSimilarItem>()
            {
                public int compare(mostSimilarItem arg0, mostSimilarItem arg1)
                {
                    if (arg0.similarity == arg1.similarity)
                    {
                        return 0;
                    }

                    if (arg0.similarity > arg1.similarity)
                    {
                        return -1;
                    }

                    return 1;
                }
            });

            // print result
            System.out.print(itemsMap.get(testContentId).title + ": ");
            for (int j = 0; j < itemsMap.get(testContentId).genres.size(); j++)
            {
                if (itemsMap.get(testContentId).genres.get(j) == 1)
                {
                    System.out.print(genreMap.get(j + 1) + " ");
                }
            }

            System.out.println("\n most similar items:");

            for (int i = 0; i < mostSimilar.size() && i < 5; i++)
            {
                System.out.println(itemsMap.get(mostSimilar.get(i).itemId).title + " " + mostSimilar.get(i).similarity);
                MovieItem movieItem = itemsMap.get(mostSimilar.get(i).itemId);
                for (int j = 0; j < movieItem.genres.size(); j++)
                {
                    if (movieItem.genres.get(j) == 1)
                    {
                        System.out.print(genreMap.get(j + 1) + " ");
                    }
                }

                System.out.println();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void MahoutTestFoo()
    {
        // getMostSimilarIds
        MahoutEntry mahoutEntry = new MahoutEntry(true);
        ArrayList<MostSimilarItem> ids = mahoutEntry.getMostSimilarIds(1);
        if (ids != null)
        {
            for (int i = 0; i < ids.size(); i++)
            {
                System.out.println(ids.get(i).itemId);
            }
        }
        else
        {
            System.out.println("null");
        }
    }

    public void Foo()
    {
        MahoutTest mainObject = new MahoutTest();
        // mainObject.SimpleTestFoo();
        // mainObject.CFTestWithEvaluationFoo();
        // mainObject.ContentBasedFoo();
        mainObject.MahoutTestFoo();
    }
}
