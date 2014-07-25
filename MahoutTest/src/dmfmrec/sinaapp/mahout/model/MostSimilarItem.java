package dmfmrec.sinaapp.mahout.model;

public class MostSimilarItem
{
    public long itemId;
    public double similarity;

    public MostSimilarItem(long itemId, double similarity)
    {
        this.itemId = itemId;
        this.similarity = similarity;
    }
}
