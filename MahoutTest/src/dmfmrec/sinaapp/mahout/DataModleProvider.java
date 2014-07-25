package dmfmrec.sinaapp.mahout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import dmfmrec.sinaapp.mahout.model.DongmanItem;

public class DataModleProvider
{
    private static DataModleProvider instance;
    private int tagCount = 19;

    private DataModleProvider()
    {
    }

    public static DataModleProvider getInstance()
    {
        if (instance == null)
        {
            instance = new DataModleProvider();
        }

        return instance;
    }

    public DataModel getListDatatModel(ArrayList<DongmanItem> items, int index)
    {
        FastByIDMap<PreferenceArray> preferences = new FastByIDMap<PreferenceArray>();
        for (int di = 0; di < items.size(); di++)
        {
            ArrayList<Integer> list = items.get(di).taglayers.get(index).genres;
            long key = items.get(di).id;
            PreferenceArray pref = new GenericUserPreferenceArray(list.size());
            pref.setUserID(0, key);
            preferences.put(key, pref);
            for (int i = 0; i < list.size(); i++)
            {
                preferences.get(key).setItemID(i, list.get(i));
                preferences.get(key).setValue(i, 1);
            }
        }
        return new GenericDataModel(preferences);
    }

    public DataModel getMockFileDataModel(String prefFilePath) throws Exception
    {
        // 必须保证pref的userId是集中排列的，且要事先计算出该user的pref的个数
        FastByIDMap<PreferenceArray> preferences = new FastByIDMap<PreferenceArray>();
        File file = new File(prefFilePath);
        InputStreamReader itemsInStream = new InputStreamReader(new FileInputStream(file));
        BufferedReader itemsReader = new BufferedReader(itemsInStream);
        int prefIndex = 0;
        long curId = -1;
        while (true)
        {
            String line = itemsReader.readLine();
            if (line == null)
            {
                break;
            }

            String[] strs = line.split(",");
            if (strs.length < 3)
            {
                continue;
            }

            long key = Long.valueOf(strs[0]);
            if (curId == -1 || curId != key)
            {
                curId = key;
                prefIndex = 0;
                tagCount = 19;
            }

            if (!preferences.containsKey(key))
            {
                PreferenceArray pref = new GenericUserPreferenceArray(tagCount);
                pref.setUserID(prefIndex, key);
                pref.setItemID(prefIndex, Long.valueOf(strs[1]));
                pref.setValue(prefIndex, Long.valueOf(strs[2]));
                preferences.put(key, pref);
            }
            else
            {
                if (prefIndex < tagCount)
                {
                    preferences.get(key).setItemID(prefIndex, Long.valueOf(strs[1]));
                    preferences.get(key).setValue(prefIndex, Long.valueOf(strs[2]));
                }
            }

            prefIndex++;
        }

        itemsReader.close();

        return new GenericDataModel(preferences);
    }
}
