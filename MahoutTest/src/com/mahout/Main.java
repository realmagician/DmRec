package com.mahout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

import dmfmrec.sinaapp.mahout.MahoutEntry;
import dmfmrec.sinaapp.mahout.model.MostSimilarItem;

public class Main
{

    public static void PrintEntryInfo(MahoutEntry entry)
    {
        if (entry == null)
        {
            return;
        }

        for (int ei = 0; ei < entry.dongmanItems.size(); ei++)
        {
            System.out.println(entry.dongmanItems.get(ei).id);
            // for (int ci = 0; ci < entry.dongmanItems.get(ei).castIds.size();
            // ci++)
            // {
            // System.out.print(entry.dongmanItems.get(ei).castIds.get(ci) +
            // " ");
            // }
            // System.out.println();
            //
            // for (int ai = 0; ai < entry.dongmanItems.get(ei).author.size();
            // ai++)
            // {
            // System.out.print(entry.dongmanItems.get(ei).author.get(ai) +
            // " ");
            // }
            // System.out.println();

            for (int ti = 0; ti < entry.dongmanItems.get(ei).taglayers.size(); ti++)
            {
                ArrayList<Integer> list = entry.dongmanItems.get(ei).taglayers.get(ti).genres;
                for (int i = 0; i < list.size(); i++)
                {
                    System.out.print(list.get(i) + " ");
                }
                System.out.println();
            }
            System.out.println();

        }
    }

    public static void main(String[] args)
    {
        MahoutEntry entry = new MahoutEntry(true);
        // PrintEntryInfo(entry);
        String filePath = "simList.out";
        try
        {
            File file = new File(filePath);
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
            for (int i = 0; i < entry.dongmanItems.size(); i++)
            {
                System.out.println(entry.dongmanItems.get(i).id);
                ArrayList<MostSimilarItem> list = entry.getMostSimilarIds(entry.dongmanItems.get(i).id);
                String simlistStr = entry.dongmanItems.get(i).id + ":";
                for (int li = 0; li < list.size(); li++)
                {
                    if (li != 0)
                    {
                        simlistStr += ",";
                    }
                    simlistStr += list.get(li).itemId + "|" + list.get(li).similarity;
                    String sql = String.format(Locale.US, "insert into tb_comic_sim (id, com_id, sim) values (%d,%d,%f);", entry.dongmanItems.get(i).id,
                            list.get(li).itemId, list.get(li).similarity);
                    writer.write(sql + "\n");
                }

                System.out.println();
                // writer.write(simlistStr + "\n");
            }

            writer.close();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
