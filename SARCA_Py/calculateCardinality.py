def calculateDir(mainDirection, headingDirection):
    return mainDirection[min(range(len(mainDirection)), key = lambda i: abs(mainDirection[i]-headingDirection))]