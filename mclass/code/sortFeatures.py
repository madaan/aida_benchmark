#sg
import sys
from collections import defaultdict
def main(fileName):
    f = open(fileName)
    for line in f:
        
        features = line.split(" ")
        label = features[0] 
        features = features[1:len(features) - 1] #strip the newline and the label 
        featureDict = defaultdict(float)
        for f in features:
            ff = f.split(':')
            featureDict[int(ff[0])] = float(ff[1])
            
        print label,
        for key in sorted(featureDict.keys()):
            print "%d:%f" % (key, featureDict[key]),
        print

if __name__ == '__main__':
    main(sys.argv[1])
