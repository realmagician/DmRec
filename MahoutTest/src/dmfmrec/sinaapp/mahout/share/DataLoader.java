package dmfmrec.sinaapp.mahout.share;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import dmfmrec.sinaapp.mahout.MahoutEntry;
import dmfmrec.sinaapp.mahout.model.DongmanItem;
import dmfmrec.sinaapp.mahout.model.TagLayer;

public class DataLoader
{
    private static DataLoader instance = null;

    private DataLoader()
    {
    }

    public static DataLoader getInstance()
    {
        if (instance == null)
        {
            instance = new DataLoader();
        }

        return instance;
    }

    public void readItemSql(ArrayList<DongmanItem> dongmanItems)
    {
        if (dongmanItems == null)
        {
            return;
        }

        Connection con = null;
        if (MahoutEntry.mock)
        {
            con = SqlUtilMock.getInstance().getConnection();
        }
        else
        {
            con = SqlUtil.getInstance().getConnection();
        }

        if (con == null)
        {
            return;
        }

        try
        {
            Statement stmt;
            stmt = con.createStatement();
            String sql = "select * from tb_basic";
            ResultSet rSet = stmt.executeQuery(sql);
            while (rSet.next())
            {
                DongmanItem dmItem = new DongmanItem();
                // for each dm
                dmItem.id = rSet.getInt(1);
                dmItem.title = rSet.getString(2);
                dmItem.country = rSet.getString(7);
                // end for each dm
                dongmanItems.add(dmItem);
            }

            for (int i = 0; i < dongmanItems.size(); i++)
            {
                // for author
                sql = String.format("select author_id from tb_comic_author_relation where comic_id=%d order by author_id", dongmanItems.get(i).id);
                ResultSet authorSet = stmt.executeQuery(sql);
                while (authorSet.next())
                {
                    dongmanItems.get(i).author.add(authorSet.getInt(1));
                }
                // end for author

                // for cat
                sql = String.format("select category_id from tb_comic_category_relation where comic_id=%d order by category_id", dongmanItems.get(i).id);
                ResultSet catSet = stmt.executeQuery(sql);
                TagLayer catLayer = new TagLayer();
                catLayer.layer = 1;
                while (catSet.next())
                {
                    catLayer.genres.add(catSet.getInt(1));
                }

                dongmanItems.get(i).taglayers.add(catLayer);
                // end for cat

                // for tag
                sql = String.format("select tag_id from tb_comic_tag_relation where comic_id=%d order by tag_id", dongmanItems.get(i).id);
                ResultSet tagSet = stmt.executeQuery(sql);
                TagLayer tagLayer = new TagLayer();
                tagLayer.layer = 2;
                while (tagSet.next())
                {
                    tagLayer.genres.add(tagSet.getInt(1));
                }

                dongmanItems.get(i).taglayers.add(tagLayer);
                // end for tag

                // for cast
                sql = String.format("select cast_id from tb_comic_cast_relation where comic_id=%d order by cast_id", dongmanItems.get(i).id);
                ResultSet castSet = stmt.executeQuery(sql);
                while (castSet.next())
                {
                    dongmanItems.get(i).castIds.add(castSet.getInt(1));
                }
                // end for cast

                // for cast
                sql = String.format("select actor_id from tb_comic_actor_relation where comic_id=%d order by actor_id", dongmanItems.get(i).id);
                ResultSet actorSet = stmt.executeQuery(sql);
                while (actorSet.next())
                {
                    dongmanItems.get(i).castIds.add(actorSet.getInt(1));
                }
                // end for cast
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void readItemsMock(ArrayList<DongmanItem> dongmanItems, String filePath) throws IOException
    {
        if (dongmanItems == null || filePath == null)
        {
            return;
        }

        File file = new File(filePath);
        InputStreamReader itemsInStream = new InputStreamReader(new FileInputStream(file));
        BufferedReader itemsReader = new BufferedReader(itemsInStream);
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

            DongmanItem item = new DongmanItem();
            item.id = Long.valueOf(strs[0]);
            for (int i = 5; i < strs.length; i++)
            {
                item.genres.add(Integer.valueOf(strs[i]));
            }

            dongmanItems.add(item);
        }

        itemsReader.close();
    }
}
