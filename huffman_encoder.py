# a simple text encoder using the Huffman coding algorithm

class TreeNode:
    parent, child0, child1 = None, None, None
    is_leaf = False
    code = ''
    def __init__(self, child0, child1):
        self.count = child0.count + child1.count
        self.child0, self.child1 = child0, child1
    def set_children_codes(self):
        self.child0.code = self.code + '0'
        self.child1.code = self.code + '1'
        if not self.child0.is_leaf: self.child0.set_children_codes()
        if not self.child1.is_leaf: self.child1.set_children_codes()
    def get_children_codes(self):
        children_codes = []
        if self.child0.is_leaf: children_codes.append([self.child0.char,self.child0.count, self.child0.code])
        else: children_codes.extend(self.child0.get_children_codes())
        if self.child1.is_leaf: children_codes.append([self.child1.char,self.child1.count, self.child1.code])
        else: children_codes.extend(self.child1.get_children_codes())
        return children_codes
        
class TreeLeaf:
    parent = None
    is_leaf= True
    code = ''
    def __init__(self, item):
        self.char = item[0]
        self.count = item[1]
        
class TreeRoots:
    def __init__(self, roots):
        self.roots = roots
        self.count = len(roots)
    def pop_min(self):
        min_i=0
        for i in range(len(self.roots)):
            if self.roots[i].count<self.roots[min_i].count:
                min_i=i
        min_root = self.roots[min_i]
        del self.roots[min_i]
        self.count -=1
        return min_root
    def add(self, root):
        self.roots.append(root)
        self.count +=1
    
def compress_huffman(text):
    char_frequency = {char: text.count(char) for char in text}
    tree_leafs = [TreeLeaf(item) for item in char_frequency.items()]
    tree_roots = TreeRoots(tree_leafs)
    while tree_roots.count>1:
        child0 = tree_roots.pop_min()
        child1 = tree_roots.pop_min()
        parent = TreeNode(child0, child1)
        child0.parent = child1.parent = parent
        tree_roots.add(parent)
    tree_root = tree_roots.roots[0]
    tree_root.set_children_codes()
    return tree_root.get_children_codes()

text = 'test text'
char_codes = compress_huffman(text)
print('char\tfreq\tcode')
original_size = len(text)*8
compressed_size = 0
for char_code in char_codes:
    compressed_size += char_code[1]*len(char_code[2])
    print('{}\t{}\t{}'.format(char_code[0], char_code[1], char_code[2]))
print('compression ratio: {:.3}'.format(original_size/compressed_size))