package dmfmrec.sinaapp.mahout;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import dmfmrec.sinaapp.mahout.model.DongmanItem;
import dmfmrec.sinaapp.mahout.model.MostSimilarItem;
import dmfmrec.sinaapp.mahout.share.DataLoader;
import dmfmrec.sinaapp.mahout.share.TopHeap;

public class MahoutEntry
{
    public static boolean mock = true;
    public static int topN = 50;
    public static String prefFilePath = "saestor://mock/uMahout_withgenre.data";
    public static String itemFilePath = "saestor://mock/u.item";

    public double authorWeight = 0.3;
    public double actorWeight = 0.3;

    public ArrayList<DongmanItem> dongmanItems;
    public UserSimilarity tagSimilarity = null;
    public UserSimilarity catSimilarity = null;

    public MahoutEntry(boolean useMock)
    {
        MahoutEntry.mock = useMock;
        this.dongmanItems = new ArrayList<DongmanItem>();
        DataLoader.getInstance().readItemSql(dongmanItems);
        catSimilarity = getUserSimilarity(DataModleProvider.getInstance().getListDatatModel(dongmanItems, 0));
        tagSimilarity = getUserSimilarity(DataModleProvider.getInstance().getListDatatModel(dongmanItems, 1));
    }

    private UserSimilarity getUserSimilarity(DataModel model)
    {
        if (model == null)
        {
            return null;
        }

        UserSimilarity userSimilarity = null;
        try
        {
            userSimilarity = new LogLikelihoodSimilarity(model);
            // userSimilarity.setPreferenceInferrer(new PreferenceInferrer()
            // {
            // public void refresh(Collection<Refreshable> alreadyRefreshed)
            // {
            // }
            //
            // public float inferPreference(long userID, long itemID) throws
            // TasteException
            // {
            // return 0;
            // }
            // });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return userSimilarity;
    }

    private double titleSim(String str1, String str2)
    {
        double sim = 0;
        // TODO: need to change similarity algorithm
        int index1 = 0, index2 = 0;
        while (index1 < str1.length() && index2 < str2.length())
        {
            if (str1.charAt(index1) == str2.charAt(index2))
            {
                sim = sim + 1;
            }

            index1++;
            index2++;
        }

        return sim / Math.max(str1.length(), str2.length());
    }

    public double getSimilarity(long id1, long id2, int index1, int index2)
    {
        double sim = 0;
        try
        {
            // for cat
            double catSim = catSimilarity != null ? catSimilarity.userSimilarity(id1, id2) : 0;
            if (Double.isNaN(catSim))
            {
                catSim = 0;
            }

            // for tag
            double tagSim = tagSimilarity != null ? tagSimilarity.userSimilarity(id1, id2) : 0;
            if (Double.isNaN(tagSim))
            {
                tagSim = 0;
            }

            // for author
            double authorSim = 0;
            for (int i = 0; i < dongmanItems.get(index1).author.size() && authorSim <= 0; i++)
            {
                for (int j = 0; j < dongmanItems.get(index2).author.size(); j++)
                {
                    if (dongmanItems.get(index1).author.get(i) == dongmanItems.get(index2).author.get(j))
                    {
                        authorSim = authorWeight;
                        break;
                    }
                }
            }

            // for title
            double titleSim = titleSim(dongmanItems.get(index1).title, dongmanItems.get(index2).title);

            // for actor
            double actorSim = 0;
            int unionCount = dongmanItems.get(index1).actorIds.size() + dongmanItems.get(index2).actorIds.size();
            if (unionCount == 0)
            {
                actorSim = 0;
            }
            else
            {
                for (int i = 0; i < dongmanItems.get(index1).actorIds.size(); i++)
                {
                    for (int j = 0; j < dongmanItems.get(index2).actorIds.size(); j++)
                    {
                        if (dongmanItems.get(index1).actorIds.get(i) == dongmanItems.get(index2).actorIds.get(j))
                        {
                            actorSim = actorSim + 1;
                        }
                    }
                }

                actorSim /= (dongmanItems.get(index1).actorIds.size() + dongmanItems.get(index2).actorIds.size() - actorSim);
            }

            // for similarity
            // for japan and china
            String country1 = dongmanItems.get(index1).country;
            String country2 = dongmanItems.get(index2).country;
            if ((country1.contains("日本") || country1.contains("中国") || country1.contains("美国")) && !country1.equals(country2))
            {
                sim = 0;
            }
            else
            {
                sim = 3 * catSim + 2 * tagSim + 0.5 * titleSim + 2 * authorSim + actorSim;
            }
        }
        catch (Exception e)
        {
        }

        return sim;
    }

    public ArrayList<MostSimilarItem> getMostSimilarIds(long itemId)
    {
        SimilarityComparer<MostSimilarItem> comparer = new SimilarityComparer<MostSimilarItem>()
        {
            public boolean Compare(MostSimilarItem item1, MostSimilarItem item2)
            {
                if (item1.similarity >= item2.similarity)
                {
                    return true;
                }

                return false;
            }
        };

        int index = 0;
        for (int i = 0; i < dongmanItems.size(); i++)
        {
            if (dongmanItems.get(i).id == itemId)
            {
                index = i;
                break;
            }
        }

        TopHeap<MostSimilarItem> topHeap = new TopHeap<MostSimilarItem>(topN, comparer);
        for (int i = 0; i < dongmanItems.size(); i++)
        {
            if (dongmanItems.get(i).id == itemId)
            {
                continue;
            }

            double sim = getSimilarity(itemId, dongmanItems.get(i).id, index, i);
            MostSimilarItem mostSimilarItem = new MostSimilarItem(dongmanItems.get(i).id, sim);
            topHeap.addOrReplace(mostSimilarItem);
        }

        ArrayList<MostSimilarItem> topIds = new ArrayList<MostSimilarItem>(topN);
        for (int i = 0; i < topN && i < topHeap.getSize(); i++)
        {
//            System.out.println(topHeap.getHeap().get(i + 1).similarity);
            MostSimilarItem tmpItem = new MostSimilarItem(Long.valueOf(topHeap.getHeap().get(i + 1).itemId), topHeap.getHeap().get(i + 1).similarity);
            topIds.add(tmpItem);
        }

        return topIds;
    }

    public ArrayList<Long> getMostSimilarIdsMock(long itemId)
    {
        try
        {
            UserSimilarity dongmanSimilarity = null;

            // if (MahoutEntry.mock)
            // {
            // // DataLoader.getInstance().readItemsMock(dongmanItems,
            // itemFilePath);
            // // model =
            // DataModleProvider.getInstance().getMockFileDataModel(prefFilePath);
            //
            // }
            // else
            // {
            // // TODO: read from MySql
            // DataLoader.getInstance().readItemsMock(dongmanItems,
            // itemFilePath);
            // model =
            // DataModleProvider.getInstance().getMockFileDataModel(prefFilePath);
            // }

            DataModel model = null;
            dongmanSimilarity = new PearsonCorrelationSimilarity(model);
            dongmanSimilarity.setPreferenceInferrer(new PreferenceInferrer()
            {
                public void refresh(Collection<Refreshable> alreadyRefreshed)
                {
                }

                public float inferPreference(long userID, long itemID) throws TasteException
                {
                    return 0;
                }
            });

            SimilarSorter similarSorter = new SimilarSorter();
            SimilarityComparer<MostSimilarItem> comparer = new SimilarityComparer<MostSimilarItem>()
            {
                public boolean Compare(MostSimilarItem item1, MostSimilarItem item2)
                {
                    if (item1.similarity >= item2.similarity)
                    {
                        return true;
                    }

                    return false;
                }
            };

            ArrayList<Long> ids = similarSorter.getTopItemIds(dongmanSimilarity, dongmanItems, comparer, itemId, MahoutEntry.topN);

            return ids;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
