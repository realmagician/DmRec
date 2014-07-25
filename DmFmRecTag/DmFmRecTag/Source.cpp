#include<iostream>
#include<fstream>
#include<string>
#include<vector>
#include<map>
#include<set>
#include<stdlib.h>
#include"DmItem.h"
using namespace std;
const int MAP_SIZE = 10000;
int dmCount = 0;

class wordSim
{
public:
	int hashCode;
	double simIncrease;

	wordSim(int hashCode = 0, double simIncrease = 0)
	{
		this->hashCode = hashCode;
		this->simIncrease = simIncrease;
	}
};

int readDmCount()
{
	int count = 0;
	ifstream fin;
	fin.open("dmList.data", ios_base::in);
	string line;
	while (getline(fin, line))
	{
		count++;
	}

	fin.clear();
	fin.close();
	return count;
}

bool isWord(string &word)
{
	if (word.length() <= 2)
	{
		return false;
	}

	return true;
}

int BKDRHash(const char *str)
{
	int seed = 131; // 31 131 1313 13131 131313 etc..
	int hash = 0;

	while (*str)
	{
		hash = hash * seed + (*str++);
	}

	return (hash & 0x7FFFFFFF);
}

int JoinCount(DmItem &item1, DmItem &item2)
{
	int joinCount = 0;
	int i = 0;
	int j = 0;
	while (i < item1.words.size() && j < item2.words.size())
	{
		if (item1.words.at(i) == item2.words.at(j))
		{
			joinCount++;
			i++;
			j++;
			continue;
		}

		if (item1.words.at(i) < item2.words.at(j))
		{
			i++;
		}
		else
		{
			j++;
		}
	}

	return joinCount;
}

void getTagMapping(map<int,string> &tagMap)
{
	ifstream fin;
	fin.open("final.data", ios_base::in);
	string line;

	while (getline(fin, line))
	{
		int pos = line.find_first_of(":");
		if (pos == -1)
		{
			continue;
		}

		string rawTag = line.substr(0, pos);
		string realTag = line.substr(pos+1, line.length());
		int hashcode = BKDRHash(rawTag.c_str());
		tagMap[hashcode] = realTag;
		cout << realTag << endl;
	}

	fin.close();
	fin.clear();
}

void getTags(vector<DmItem> &dms, map<int, string> &wordMap, map<int, int> &wordCountMap, vector<wordSim> &simArray)
{	
	vector<int> cons(dms.size());
	int index = 0;
	cout << "counting" << endl;
	for (map<int, string>::iterator iter = wordMap.begin(); iter != wordMap.end(); iter++)
	{		
		index++;
		if (1.0f*wordCountMap[iter->first] / dmCount > 0.1f)
		{
			ofstream lout;
			lout.open("tooMany.data", ios_base::app);
			lout << wordMap[iter->first] << " " << wordCountMap[iter->first] << endl;
			lout.close();
			lout.clear();
		}

		if (1.0f*wordCountMap[iter->first] / dmCount > 0.1f || wordCountMap[iter->first] < 50)
		{
			continue;
		}

		int hashCode = iter->first;
		string word = iter->second;
		double simIncrease = 0;
		for (int i = 0; i<dms.size(); ++i)
		{
			int contains = dms.at(i).wordset.find(hashCode) != dms.at(i).wordset.end() ? 1 : 0;
			cons.at(i) = contains;
		}

		int dmSize = dms.size();
		for (int i = 0; i<dmSize; ++i)
		{
			int con1 = cons.at(i);
			for (int j = 0; j<dmSize; ++j)
			{
				int con2 = cons.at(j);
				if (i == j || con1 == 0 && con2 == 0)
				{
					continue;
				}

				int joinCount = JoinCount(dms.at(i), dms.at(j));
				//int joinCount = 0;
				int unionCount = dms.at(i).words.size() + dms.at(j).words.size() - 2 * joinCount;
				int tmpIncrease = 1.0*(con1*con2*unionCount - (con1 + con2)*joinCount) / (1.0f*unionCount*(unionCount - con1 - con2));
				simIncrease += tmpIncrease * tmpIncrease;
				//simIncrease += 1;
			}
		}

		cout << index << " " << dmSize << " " << wordMap.size() << endl;
		ofstream tout;
		tout.open("test.out", ios_base::app);
		tout << iter->second << " " << wordCountMap[iter->first] << endl;
		tout.close();
		tout.clear();
		simIncrease = simIncrease / (wordMap.size()*wordMap.size());
		simArray.push_back(wordSim(hashCode, simIncrease));
	}

	ofstream out;
	out.open("tag.data", ios_base::out);
	out << wordMap.size() << endl;
	cout << wordMap.size() << endl;
	for (int end = simArray.size() - 1; end > 0; --end)
	{
		for (int i = 0; i < end; ++i)
		{
			if (simArray.at(i).simIncrease < simArray.at(i + 1).simIncrease)
			{
				wordSim t;
				t.hashCode = simArray.at(i).hashCode;
				t.simIncrease = simArray.at(i).simIncrease;
				simArray.at(i).hashCode = simArray.at(i + 1).hashCode;
				simArray.at(i).simIncrease = simArray.at(i + 1).simIncrease;
				simArray.at(i + 1).hashCode = t.hashCode;
				simArray.at(i + 1).simIncrease = t.simIncrease;
			}
		}
	}

	for (int i = 0; i<simArray.size(); ++i)
	{
		out << wordMap[simArray.at(i).hashCode] << " " << simArray.at(i).simIncrease << " " << wordCountMap[simArray.at(i).hashCode] << endl;
	}

	out.close();
	out.clear();
	cout << "complete" << endl;
}

void readRawData(vector<DmItem> &dms, map<int, string> &wordMap, map<int, int> &wordCountMap)
{
	int wordCount = 0;
	int lineCount = 0;
	ifstream fin;
	fin.open("dmList.data", ios_base::in);
	string line;

	while (getline(fin, line))
	{
		DmItem dmItem;
		int pos = line.find_first_of("{{}}");
		if (pos == -1)
		{
			continue;
		}

		dmItem.title = line.substr(0, pos);
		int desStart = line.find("{{}}", pos + 4) + 4;
		if (desStart == -1)
		{
			continue;
		}
		int desEnd = line.find("{{}}", desStart);
		if (desEnd == -1)
		{
			continue;
		}

		string desStr = line.substr(desStart, desEnd - desStart);

		string des;
		int wordStart = 0;
		int wordEnd = 0;
		int desStrLength = desStr.length();
		//get des
		while (wordStart < desStrLength)
		{
			wordEnd = desStr.find("[[]]", wordStart);
			if (wordEnd == -1)
			{
				break;
			}

			des = desStr.substr(wordStart, wordEnd - wordStart);
			if (isWord(des))
			{
				int hash = BKDRHash(des.c_str());
				if (wordMap.find(hash) == wordMap.end())
				{
					wordCount++;
					wordMap[hash] = des;
					wordCountMap[hash] = 0;
				}

				if (dmItem.wordset.find(hash) == dmItem.wordset.end())
				{
					dmItem.wordset.insert(hash);
					dmItem.words.push_back(hash);
					wordCountMap[hash] = wordCountMap[hash] + 1;
				}
			}

			wordStart = wordEnd + 4;
		}

		for (int wi = dmItem.words.size() - 1; wi > 0; --wi)
		{
			for (int wj = 0; wj < wi; ++wj)
			{
				if (dmItem.words.at(wj) > dmItem.words.at(wj + 1))
				{
					int t = dmItem.words.at(wj);
					dmItem.words.at(wj) = dmItem.words.at(wj + 1);
					dmItem.words.at(wj + 1) = t;
				}
			}
		}

		dms.at(lineCount++) = dmItem;
	}

	fin.close();
	fin.clear();
}

void foo()
{
	vector<wordSim> simArray;
	map<int, string> tagMap;

	vector<DmItem> dms(dmCount);
	map<int, string> wordMap;
	map<int, int> wordCountMap;
	readRawData(dms, wordMap, wordCountMap);
	getTags(dms, wordMap, wordCountMap, simArray);
}

void countTag()
{
	vector<DmItemOnlyTag> dmOnlytags(dmCount);
	map<string, int> tagCountMap;
	int lineCount = 0;
	ifstream fin;
	fin.open("dmList.data", ios_base::in);
	string line;

	while (getline(fin, line))
	{
		DmItemOnlyTag dmOnlytag;
		int pos = line.find_first_of("{{}}");
		if (pos == -1)
		{
			continue;
		}

		dmOnlytag.title = line.substr(0, pos);
		int posEnd = line.find("{{}}", pos + 4);
		pos = line.find("{{}}", posEnd + 4) + 4;
		posEnd = line.find("{{}}", pos);
		if (posEnd == -1)
		{
			continue;
		}
		string tagStr = line.substr(pos, posEnd - pos);
		//cout << pos << " " << posEnd << " " << line.length() << endl;
		//cout << tagStr << endl; getchar();

		string tag;
		int tagStart = 0;
		int tagEnd = 0;
		int desStrLength = tagStr.length();
		//get des
		while (tagStart < desStrLength)
		{
			tagEnd = tagStr.find("[[]]", tagStart);
			if (tagEnd == -1)
			{
				break;
			}

			tag = tagStr.substr(tagStart, tagEnd - tagStart);
			//cout << tag << endl;

			bool isUnique = true;
			for (int doi = 0; doi < dmOnlytag.tags.size(); ++doi)
			{
				if (tag.compare(dmOnlytag.tags.at(doi)) == 0)
				{
					isUnique = false;
					break;
				}
			}

			if (isUnique)
			{
				dmOnlytag.tags.push_back(tag);
				if (tagCountMap.find(tag) == tagCountMap.end())
				{
					tagCountMap[tag] = 0;
				}

				tagCountMap[tag] ++;
			}
			tagStart = tagEnd + 4;
		}

		dmOnlytags.at(lineCount++) = dmOnlytag;
	}

	ofstream fout;
	fout.open("tag_count.data",ios_base::out);
	for (map<string, int>::iterator iter = tagCountMap.begin(); iter != tagCountMap.end(); iter++)
	{
		if (iter->second > 1)
		{
			fout << iter->first << " " << iter->second << endl;
		}		
	}

	fout.close();
	fout.clear();

	int totalTagCount = tagCountMap.size();
	cout << totalTagCount << endl;
	vector<int> yus;
	int totoalStep = 100;
	int step = totalTagCount / totoalStep;
	for (int i = 0; i < totoalStep; ++i)
	{
		yus.push_back(i*step);
		int count = 0;
		if (i == 0)
		{
			continue;
		}

		for (map<string, int>::iterator iter = tagCountMap.begin(); iter != tagCountMap.end(); iter++)
		{
			if (iter->second > yus.at(i - 1) && iter->second < yus.at(i))
			{
				count++;
			}
		}

		cout << "less than: " << yus.at(i) << " " << count << endl;
	}

	fin.close();
	fin.clear();
}

void makeFinalTagFoo()
{
	map<int, string> tagMap;
	vector<DmItem> dms(dmCount);
	map<int, string> wordMap;
	map<int, int> wordCountMap;
	readRawData(dms, wordMap, wordCountMap);
	getTagMapping(tagMap);
	
	ofstream fout;
	fout.open("dmwithtag.data", ios_base::out);
	for (int i = 0; i < dms.size(); ++i)
	{
		string title = dms.at(i).title;
		string tagStr = "";
		bool isFirst = true;
		for (int wi = 0; wi < dms.at(i).words.size(); ++wi)
		{
			int hashcode = dms.at(i).words.at(wi);
			if (wordMap.find(hashcode) == wordMap.end() || tagMap.find(hashcode) == tagMap.end())
			{
				continue;
			}

			string realTag = tagMap[hashcode];
			if (tagStr.find(realTag) != -1)
			{
				continue;
			}
			if (!isFirst)
			{
				tagStr += ",";
			}

			isFirst = false;
			tagStr += realTag;
		}

		fout << title << "::" << tagStr << endl;
	}

	fout.close();
	fout.clear();
}


int main()
{	
	dmCount = readDmCount();
	//foo();
	makeFinalTagFoo();
	//countTag();
	system("pause");
	return 0;
}