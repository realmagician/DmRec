package dmfmrec.sinaapp.mahout;

public interface SimilarityComparer<T>
{
    public boolean Compare(T item1, T item2);
}
