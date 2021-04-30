def getsum(BitTree,i): 
    result = 0
    i = i + 1
    while i > 0:
        result += BitTree[i]
        i -= i & (-i)
    return result
  
def updatebit(BitTree , n , i ,v): 
    i += 1
    while i <= n: 
        BitTree[i] += v 
        i += i & (-i) 
  
  
def construct(arr, n): 
    BitTree = [0] * (n + 1)
    for i in range(n):
        updatebit(BitTree, n, i, arr[i])
    return BitTree
