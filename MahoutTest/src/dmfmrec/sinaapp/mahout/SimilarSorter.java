package dmfmrec.sinaapp.mahout;

import java.util.ArrayList;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import dmfmrec.sinaapp.mahout.model.DongmanItem;
import dmfmrec.sinaapp.mahout.model.MostSimilarItem;
import dmfmrec.sinaapp.mahout.share.TopHeap;

public class SimilarSorter
{
    public ArrayList<Long> getTopItemIds()
    {
        return null;
    }
    
    public ArrayList<Long> getTopItemIds(UserSimilarity dongmanSimilarity, ArrayList<DongmanItem> items, SimilarityComparer<MostSimilarItem> comparer, long dmId, int topN)
    {
        if (dongmanSimilarity == null || items == null || items.size() == 0)
        {
            return null;
        }

        TopHeap<MostSimilarItem> topHeap = new TopHeap<MostSimilarItem>(topN, comparer);
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++)
        {
            double sim;
            try
            {
                sim = dongmanSimilarity.userSimilarity(dmId, items.get(itemIndex).id);
            }
            catch (TasteException e)
            {
                sim = 0;
                e.printStackTrace();
            }

            MostSimilarItem mostSimilarItem = new MostSimilarItem(items.get(itemIndex).id, sim);
            topHeap.addOrReplace(mostSimilarItem);
        }

        ArrayList<Long> topIds = new ArrayList<Long>(topN);
        for (int i = 0; i < topN; i++)
        {
            System.out.println(topHeap.getHeap().get(i+1).similarity);
            topIds.add(Long.valueOf(topHeap.getHeap().get(i + 1).itemId));
        }

        return topIds;
    }
}
