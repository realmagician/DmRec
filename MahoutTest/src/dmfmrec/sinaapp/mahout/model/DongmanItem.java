package dmfmrec.sinaapp.mahout.model;

import java.util.ArrayList;

public class DongmanItem
{
    public long id;
    public String title;
    public String country;

    // for dm302
    public ArrayList<Integer> author = new ArrayList<Integer>();
    public ArrayList<TagLayer> taglayers = new ArrayList<TagLayer>(); // contain 0 or 1, for category and tag
    public ArrayList<Integer> castIds = new ArrayList<Integer>(); // contain id
    public ArrayList<Integer> actorIds = new ArrayList<Integer>(); // contain id

    // for movielen mock
    public ArrayList<Integer> genres = new ArrayList<Integer>(19);


    public DongmanItem()
    {
    }
}
