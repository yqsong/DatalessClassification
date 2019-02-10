function converData (dimension)

dimension = 200;

% labelFolderName = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics';
% docFolderName = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/document-1-rec.autos-sci.electronics';
% outputFileName = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics-1-new.mat';

labelFolderName = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-rec.motorcycles';
docFolderName = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/document-1-rec.autos-rec.motorcycles';
outputFileName = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-rec.motorcycles-1-new.mat';

%read labels
labelData = readFolder(labelFolderName, dimension);
%read data
docData = readFolder(docFolderName, dimension);

%load(outputFileName);

labelNum = size(labelData, 2)

globalIndex = 1;

indexMap =  containers.Map('KeyType','int32','ValueType','int32');
inverseIndexMap = containers.Map('KeyType','int32','ValueType','int32');
for i = 1:labelNum
    i
    sample = labelData{i};
    indices = sample{2};
    for j = 1:size(indices)
        if indexMap.isKey(indices(j)) == false
            indexMap(indices(j)) = globalIndex;
            inverseIndexMap(globalIndex) = indices(j);
            globalIndex = globalIndex + 1;
        end
    end
end


dataNum = size(docData, 2)

for i = 1:dataNum
    if mod(i, 10) == 0
        i
    end
    sample = docData{i};
    indices = sample{2};
    for j = 1:size(indices)
        if indexMap.isKey(indices(j)) == false
            indexMap(indices(j)) = globalIndex;
            inverseIndexMap(globalIndex) = indices(j);
            globalIndex = globalIndex + 1;
        else
            a = 0;
        end
    end
end

save (outputFileName, 'labelData', 'docData', 'indexMap', 'inverseIndexMap');

end