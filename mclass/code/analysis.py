#sg
 
#true label and test label have been used inter changeably 
def main(trueLabelFileName, trainLabelFile, resultsFileName):
    fileTrue = open(trueLabelFileName, 'r')
    fileSVM = open(resultsFileName, 'r')
    fileTrain = open(trainLabelFile, 'r')

    trainLabels = [int(l.strip()) for l in fileTrain]
    trueLabels = [int(l.strip()) for l in fileTrue]
    resLabels = [int(l.strip()) for l in fileSVM]


    for i in range(0, len(trueLabels)):
        if(trueLabels[i] != resLabels[i]):
            print "True = %d Predicted = %d" % (trueLabels[i], resLabels[i]),

            if(trueLabels[i] in trainLabels):
                print " in training data"
            else:
                print " not in training data"



def predictedClassRatios(trueDist, trainDist, resDist):
    
    from collections import defaultdict
    fileTrue = open(trueDist, 'r')
    fileSVM = open(resDist, 'r')
    fileTrain = open(trainDist, 'r')
    
    trainRatio = defaultdict(float)
    testRatio = defaultdict(float)
    resRatio = defaultdict(float)
    totalTrain = 0
    totalTest = 0

    #read the ratios
    for l in fileTrain:
        entry = l.strip().split(" ")
        totalTrain = totalTrain + 1
        trainRatio[int(entry[0])] = float(entry[1])

    for l in fileSVM:
        entry = l.strip().split(" ")
        totalTest = totalTest + 1
        resRatio[int(entry[0])] = float(entry[1])

    for l in fileTrue:
        entry = l.strip().split(" ")
        testRatio[int(entry[0])] = float(entry[1])


        
    for i in resRatio.keys():
        print "%d %f" % (int(i), resRatio[i])



  




if __name__ == '__main__':
    testLabelFile = "testLabels"
    trainLabelFile = "trainLabels"
    res = "resultsnonzero"
    main(testLabelFile, trainLabelFile, res)

