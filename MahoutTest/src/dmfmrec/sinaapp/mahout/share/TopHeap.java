package dmfmrec.sinaapp.mahout.share;

import java.util.ArrayList;

import dmfmrec.sinaapp.mahout.SimilarityComparer;

public class TopHeap<T>
{
    private ArrayList<T> heap = null;
    private int size;
    private int topN;
    private SimilarityComparer<T> comparer = null;

    public TopHeap(int topN, SimilarityComparer<T> comparer)
    {
        this.heap = new ArrayList<T>(topN + 1);
        this.heap.add(null);
        this.size = 0;
        this.topN = topN;
        this.comparer = comparer;
    }

    public int getSize()
    {
        return heap.size() - 1;
    }

    public ArrayList<T> getHeap()
    {
        return heap;
    }

    public boolean add(T item)
    {
        if (this.size >= this.topN)
        {
            return false;
        }

        this.size++;
        // this.heap.set(this.size, item);
        this.heap.add(item);
        int index = this.size;
        while (index > 1)
        {
            int parentIndex = index / 2;
            if (comparer.Compare(this.heap.get(parentIndex), item))
            {
                this.heap.set(index, this.heap.get(parentIndex));
                index = parentIndex;
            }
            else
            {
                break;
            }
        }

        this.heap.set(index, item);
        return true;
    }

    public void addOrReplace(T item)
    {
        if (this.size < this.topN)
        {
            this.add(item);
        }
        else
        {
            if (comparer.Compare(this.heap.get(1), item))
            {
                return;
            }

            this.heap.set(1, item);
            adjustHeap();
        }
    }

    public T PopTop()
    {
        T topItem = this.heap.get(1);
        this.heap.set(1, this.heap.get(this.size));
        this.size--;
        adjustHeap();
        return topItem;
    }

    private void adjustHeap()
    {
        int index = 1;
        T item = this.heap.get(index);
        while (index < this.size)
        {
            int childIndex = index * 2;
            if (childIndex > this.size)
            {
                break;
            }

            if (childIndex + 1 <= this.size && comparer.Compare(this.heap.get(childIndex), this.heap.get(childIndex + 1)))
            {
                childIndex++;
            }

            if (comparer.Compare(item, this.heap.get(childIndex)))
            {
                this.heap.set(index, this.heap.get(childIndex));
                index = childIndex;
            }
            else
            {
                break;
            }
        }

        this.heap.set(index, item);
    }
}
