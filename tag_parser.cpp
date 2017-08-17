// Solution to vatsalchanana's problem in HackerRank
// https://www.hackerrank.com/challenges/attribute-parser

#include <iostream>
#include <algorithm>
#include <map>
#include <sstream>
using namespace std;

stringstream hrml_stream;
void trim(string &s) {
    s.erase(s.begin(), std::find_if(s.begin(), s.end(), [](int ch) {
        return !std::isspace(ch);
    }));
    s.erase(std::find_if(s.rbegin(), s.rend(), [](int ch) {
        return !std::isspace(ch);
    }).base(), s.end());
}

class tag {
  public:
    string name;
    tag() {};
    tag(string tag_name) : name(tag_name) {};
    map<string, string> attributes;
    map<string, tag> tags;
};

map<string, string> read_attributes() {
    map<string, string> attributes;
    string key, key_i, value;
    char c;
    bool done = false, key_found;
    while(!done) {
        hrml_stream >> key;
        if(key==">") break;
        value="";
        key_found=false;
        for(char c:key) {
            if(key_found) value+=c;
            if(c!='=' && !key_found) key_i+=c;
            if(c=='=' && !key_found) key_found=true;
        }
        trim(key); trim(value);
        if(value=="") hrml_stream >> c >> value;
        if(c!='=') throw string("Error reading '=' in tag attribute, unexpected character '") + c + "'\n";
        if(value.back()=='>') {
            value.pop_back();
            done = true;
        }
        trim(value);
        if(value[0]=='"' && value.back()=='"') { value.erase(0, 1); value.pop_back(); }
        attributes[key] = value;
    }
    return attributes;
}

void read_child_tags(tag&);

bool read_tag(tag &parent) {
    char c = ' ';
    string name;
    tag t;
    hrml_stream >> c;
    if(c==' ') return false;
    if(c!='<') throw string("Error reading '<' in tag, unexpected character '") + c + "'\n";
    
    hrml_stream >> name;
    if(name[0]=='/') {
        while(name.back()!='>') cin >> name;
        return false;
    }
    
    if(name.back()!='>') t.attributes = read_attributes();
    else name.pop_back();
    trim(name);
    t.name = name;
    read_child_tags(t);
    parent.tags[name] = t;
    return true;
}

void read_child_tags(tag &self) {
    bool not_done = true;
    while(not_done) not_done = read_tag(self);
}

int main() {
    int N, Q;
    char c;
    string hrml_string, line;
    cin >> N >> Q;
    cin.ignore();
    tag hrml_data = tag();
    for(int i=0; i<N; i++) {
        getline(cin, line);
        hrml_string += line;
        if(i!=N-1) hrml_string += '\n';
    }
    hrml_stream = stringstream(hrml_string);
    try {
        while(hrml_stream.tellg()!=-1) read_tag(hrml_data);
    }
    catch(string s) { cout << s; std::terminate(); }
    
    for(int i=0; i<Q; i++) {
        string q, name="", value="";
        stringstream qs; char c=' ';
        tag *index;
        cin >> q;
        qs = stringstream(q);
        index = &hrml_data;
        while(1) {
            qs >> c;
            if(c=='~' || c=='.') {
                try { index = &(index->tags.at(name)); }
                catch(exception&) { value = "Not Found!"; break; }
                if(c=='~') break;
                name = "";
            }
            else name += c;
        }
        
        qs >> name;
        try { if(value=="") value = index->attributes.at(name); }
        catch(exception&) { value = "Not Found!"; }
        cout << value << '\n';
    }
    return 0;
}
