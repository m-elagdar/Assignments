//measures the longest sequential match between two words efficiently
//part of the solution to the problem "Magic Spells" by "Dalimil" at https://www.hackerrank.com/challenges/magic-spells/

#include <iostream>
using namespace std;

int main() {
    string string1, string2;
    cin >> string1 >> string2;
    int size1=string1.size(), size2=string2.size();
    int position_counts[size2];     //stores the size of the longest match at every string2 position
    fill(position_counts, position_counts+size2, 0);

    for(int i=0; i<size1; i++) {
        for(int j=size2-1; j>=0; j--) {    //descending to avoid matching an 'string1[i]' to 'string2[j]' and 'string2[j+n]' serially
            if(string1[i]!=string2[j]) continue;

            //the longest match at any position replaces any shorter match
            int max_count_ji=0;     //get the longest previous match
            for(int k=0; k<j; k++)
                { max_count_ji = position_counts[k]>max_count_ji ? position_counts[k] : max_count_ji; }   //k<j to avoid matching backwards

            position_counts[j] = max_count_ji+1;     //add 1 to the longest match
        }
    }

    int max_count=0;
    for(int i=0; i<size2; i++) { max_count = position_counts[i]>max_count ? position_counts[i]:max_count; }
    cout << "Max length: " << max_count << '\n';
}
