#include<iostream>
#include<string>
#include<set>
#include<vector>
using namespace std;
class DmItem
{
public:
	set<int> wordset;
	vector<int> words;
	string title;

	DmItem() :title("")
	{
	}
};

class DmItemOnlyTag
{
public:
	string title;
	vector<string> tags;
};